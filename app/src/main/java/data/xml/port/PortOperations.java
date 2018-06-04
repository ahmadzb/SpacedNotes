package data.xml.port;

import android.content.Context;
import android.os.Build;

import org.jdom2.Attribute;
import org.jdom2.DataConversionException;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.TreeMap;
import java.util.concurrent.locks.ReentrantLock;

import data.preference.ContentPreferences;
import data.xml.util.BufferedDocumentProvider;
import data.xml.util.DocumentInitializer;

/**
 * Created by Ahmad on 02/17/18.
 * All rights reserved.
 */

public class PortOperations {
    private static TreeMap<Integer, PortConnection> portConnectionMap = new TreeMap<>();

    public static long getGlobalId(long localId, Port port) {
        return (localId << Port.idShiftCount) | port.getPort();
    }

    public static int getPortsCount() {
        int count = 0;
        for (int i = 0; i < Port.idShiftCount; i++) {
            count = (count << 1) | 1;
        }
        count++;
        return count;
    }

    public static int getActivePortsCount() {
        return getActivePorts().size();
    }

    public static ArrayList<Port> getActivePorts() {
        int portCount = getPortsCount();
        ArrayList<Port> activePorts = new ArrayList<>(portCount);
        for (int i = 0; i < portCount; i++) {
            Port port = connectionFor(i).getPort();
            if (port.isActive()) {
                activePorts.add(port);
            }
        }
        return activePorts;
    }

    public static Port getFirstInactivePort() {
        int portCount = getPortsCount();
        for (int i = 0; i < portCount; i++) {
            Port port = connectionFor(i).getPort();
            if (!port.isActive()) {
                return port;
            }
        }
        return null;
    }

    public static void setCurrentPortAsync(int port, Context context) {
        ContentPreferences.Port.setCurrentPort(port, context);
        PortConnection portConnection = connectionFor(port);
        portConnection.lockConnection();
        Port p = portConnection.getPort();
        p.setActive(true);
        p.setLastAgentName(Build.MODEL);
        portConnection.writePortAsync(p);
        portConnection.unlockConnection();
    }

    public static void setCurrentPort(int port, Context context) {
        ContentPreferences.Port.setCurrentPort(port, context);
        PortConnection portConnection = connectionFor(port);
        portConnection.lockConnection();
        Port p = portConnection.getPort();
        p.setActive(true);
        p.setLastAgentName(Build.MODEL);
        portConnection.writePort(p);
        portConnection.unlockConnection();
    }

    public static PortConnection getCurrentPortConnectionIfExists(Context context) {
        Integer currentPort = ContentPreferences.Port.getCurrentPort(context);
        if (currentPort == null)
            return null;
        return connectionFor(currentPort);
    }

    public static PortConnection getCurrentPortConnection(Context context) {
        Integer currentPort = ContentPreferences.Port.getCurrentPort(context);
        if (currentPort == null)
            throw new RuntimeException("current port connection does not exist");
        return connectionFor(currentPort);
    }
    public static Port getCurrentPort(Context context) {
        Integer currentPort = ContentPreferences.Port.getCurrentPort(context);
        if (currentPort == null)
            return null;
        return connectionFor(currentPort).getPort();
    }

    public static void waitUntilNoOperations() {
        for (PortConnection portConnection : portConnectionMap.values()) {
            portConnection.waitUntilNoOperation();
        }
    }

    public static void reloadAll() {
        for (PortConnection portConnection : portConnectionMap.values()) {
            portConnection.reload();
        }
    }

    public static void reload(int port) {
        PortConnection portOps = portConnectionMap.get(port);
        if (portOps != null) {
            portOps.reload();
        }
    }

    public static PortConnection connectionFor(int port) {
        PortConnection portOps = portConnectionMap.get(port);
        if (portOps == null) {
            portOps = connectionFor(data.storage.Port.getPortFile(port), port);
            portConnectionMap.put(port, portOps);
        }
        return portOps;
    }

    public static PortConnection connectionFor(File portFile, int port) {
        return new PortConnection(portFile, port);
    }

    public static class PortConnection {
        private Port port;
        private BufferedDocumentProvider documentProvider;
        private ReentrantLock connectionLock = new ReentrantLock();

        private PortConnection(File portFile, final int port) {
            this.documentProvider = BufferedDocumentProvider.newInstance(
                    portFile,
                    new DocumentInitializer() {
                        @Override
                        public Document initializeDocument() {
                            return toDocument(Port.newInstance(port));
                        }
                    }
            );
        }

        public void lockConnection() {
            connectionLock.lock();
        }

        public void unlockConnection() {
            connectionLock.unlock();
        }

        public void waitUntilNoOperation() {
            connectionLock.lock();
            documentProvider.waitUntilNoWrite();
            connectionLock.unlock();
        }

        public void reload() {
            connectionLock.lock();
            documentProvider.reloadDocument();
            connectionLock.unlock();
        }

        public Port getPort() {
            connectionLock.lock();
            try {
                if (this.port != null) {
                    return this.port;
                }
                Document document = documentProvider.getDocument();
                return fromDocument(document);
            } catch (JDOMException | IOException e) {
                throw new RuntimeException(e);
            } finally {
                connectionLock.unlock();
            }
        }

        public void writePort(Port port) {
            connectionLock.lock();
            this.port = port;
            Document document = toDocument(port);
            documentProvider.writeDocument(document);
            connectionLock.unlock();
        }

        public void writePortAsync(Port port) {
            connectionLock.lock();
            this.port = port;
            Document document = toDocument(port);
            documentProvider.writeDocumentAsync(document);
            connectionLock.unlock();
        }

        private static Port fromDocument(Document document) throws DataConversionException {
            Element portElement = document.getRootElement();
            Attribute attr;
            if ((attr = portElement.getAttribute(PortContract.port)) != null) {
                Port portInstance = Port.newInstance(attr.getIntValue());
                if ((attr = portElement.getAttribute(PortContract.lastPortName)) != null) {
                    portInstance.setLastAgentName(attr.getValue());
                }
                if ((attr = portElement.getAttribute(PortContract.isActive)) != null) {
                    portInstance.setActive(attr.getBooleanValue());
                }
                if ((attr = portElement.getAttribute(PortContract.lastLocalCaptureId)) != null) {
                    portInstance.setLastLocalCaptureId(attr.getLongValue());
                }
                if ((attr = portElement.getAttribute(PortContract.lastLocalProfileId)) != null) {
                    portInstance.setLastLocalProfileId(attr.getLongValue());
                }

                for (Element child : portElement.getChildren()) {
                    if (child.getName().equals(PortContract.LastLocalIds.itemName)) {
                        if ((attr = child.getAttribute(PortContract.LastLocalIds.forProfileId)) != null) {
                            Port.LastLocalIds lastLocalIds = portInstance.getLastLocalIds(attr.getLongValue());

                            if ((attr = child.getAttribute(PortContract.LastLocalIds.noteId)) != null) {
                                lastLocalIds.setLastNoteId(attr.getLongValue());
                            }
                            if ((attr = child.getAttribute(PortContract.LastLocalIds.noteDataId)) != null) {
                                lastLocalIds.setLastNoteDataId(attr.getLongValue());
                            }
                            if ((attr = child.getAttribute(PortContract.LastLocalIds.typeId)) != null) {
                                lastLocalIds.setLastTypeId(attr.getLongValue());
                            }
                            if ((attr = child.getAttribute(PortContract.LastLocalIds.typeElementId)) != null) {
                                lastLocalIds.setLastTypeElementId(attr.getLongValue());
                            }
                            if ((attr = child.getAttribute(PortContract.LastLocalIds.pictureId)) != null) {
                                lastLocalIds.setLastPictureId(attr.getLongValue());
                            }
                            if ((attr = child.getAttribute(PortContract.LastLocalIds.labelId)) != null) {
                                lastLocalIds.setLastLabelId(attr.getLongValue());
                            }
                            if ((attr = child.getAttribute(PortContract.LastLocalIds.labelListId)) != null) {
                                lastLocalIds.setLastLabelListId(attr.getLongValue());
                            }
                            if ((attr = child.getAttribute(PortContract.LastLocalIds.scheduleId)) != null) {
                                lastLocalIds.setLastScheduleId(attr.getLongValue());
                            }
                            if ((attr = child.getAttribute(PortContract.LastLocalIds.occurrenceId)) != null) {
                                lastLocalIds.setLastOccurrenceId(attr.getLongValue());
                            }
                            if ((attr = child.getAttribute(PortContract.LastLocalIds.revisionPastId)) != null) {
                                lastLocalIds.setLastRevisionPastId(attr.getLongValue());
                            }
                        }
                    } else if (child.getName().equals(PortContract.LogMetadata.itemName)) {
                        Port.LogMetadata metadata = new Port.LogMetadata();
                        metadata.setLastOperationId(child.getAttribute(
                                PortContract.LogMetadata.lastOperationId).getLongValue());
                        metadata.setLastOperationTime(child.getAttribute(
                                PortContract.LogMetadata.lastOperationTime).getLongValue());
                        metadata.setCurrentLogIndex(child.getAttribute(
                                PortContract.LogMetadata.currentLogIndex).getIntValue());
                        Collection<Element> sChildren = child.getChildren();
                        metadata.setLogs(new ArrayList<Port.LogMetadata.Log>(sChildren.size()));
                        for (Element sChild : sChildren) {
                            Port.LogMetadata.Log log = new Port.LogMetadata.Log();
                            log.setFromOperationId(sChild.getAttribute(PortContract.LogMetadata.Log.fromOperationId).getLongValue());
                            log.setToOperationId(sChild.getAttribute(PortContract.LogMetadata.Log.toOperationId).getLongValue());
                            log.setFromOperationTime(sChild.getAttribute(PortContract.LogMetadata.Log.fromOperationTime).getLongValue());
                            log.setToOperationTime(sChild.getAttribute(PortContract.LogMetadata.Log.toOperationTime).getLongValue());
                            log.setIndex(sChild.getAttribute(PortContract.LogMetadata.Log.index).getIntValue());
                            metadata.getLogs().add(log);
                        }

                        portInstance.setLogMetadata(metadata);
                    }
                }

                return portInstance;
            }
            return null;
        }

        private static Document toDocument(Port port) {
            Element portElement = new Element(PortContract.itemName);
            portElement.setAttribute(PortContract.port, String.valueOf(port.getPort()));
            if (port.getLastAgentName() != null) {
                portElement.setAttribute(PortContract.lastPortName, port.getLastAgentName());
            }
            portElement.setAttribute(PortContract.isActive, String.valueOf(port.isActive()));
            portElement.setAttribute(PortContract.lastLocalCaptureId, String.valueOf(port.getLastLocalCaptureId()));
            portElement.setAttribute(PortContract.lastLocalProfileId, String.valueOf(port.getLastLocalProfileId()));
            for (Port.LastLocalIds lastLocalIds : port.getLastLocalIdsIterable()) {
                Element child = new Element(PortContract.LastLocalIds.itemName);
                child.setAttribute(PortContract.LastLocalIds.forProfileId, String.valueOf(lastLocalIds.getProfileId()));
                child.setAttribute(PortContract.LastLocalIds.noteId, String.valueOf(lastLocalIds.getLastNoteId()));
                child.setAttribute(PortContract.LastLocalIds.noteDataId, String.valueOf(lastLocalIds.getLastNoteDataId()));
                child.setAttribute(PortContract.LastLocalIds.typeId, String.valueOf(lastLocalIds.getLastTypeId()));
                child.setAttribute(PortContract.LastLocalIds.typeElementId, String.valueOf(lastLocalIds.getLastTypeElementId()));
                child.setAttribute(PortContract.LastLocalIds.pictureId, String.valueOf(lastLocalIds.getLastPictureId()));
                child.setAttribute(PortContract.LastLocalIds.labelId, String.valueOf(lastLocalIds.getLastLabelId()));
                child.setAttribute(PortContract.LastLocalIds.labelListId, String.valueOf(lastLocalIds.getLastLabelListId()));
                child.setAttribute(PortContract.LastLocalIds.scheduleId, String.valueOf(lastLocalIds.getLastScheduleId()));
                child.setAttribute(PortContract.LastLocalIds.occurrenceId, String.valueOf(lastLocalIds.getLastOccurrenceId()));
                child.setAttribute(PortContract.LastLocalIds.revisionPastId, String.valueOf(lastLocalIds.getLastRevisionPastId()));
                portElement.addContent(child);
            }

            if (port.getLogMetadata() != null) {
                Element metadataElement = new Element(PortContract.LogMetadata.itemName);
                portElement.addContent(metadataElement);
                metadataElement.setAttribute(PortContract.LogMetadata.currentLogIndex, String.valueOf(port.getLogMetadata().getCurrentLogIndex()));
                metadataElement.setAttribute(PortContract.LogMetadata.lastOperationId, String.valueOf(port.getLogMetadata().getLastOperationId()));
                metadataElement.setAttribute(PortContract.LogMetadata.lastOperationTime, String.valueOf(port.getLogMetadata().getLastOperationTime()));
                for (Port.LogMetadata.Log log : port.getLogMetadata().getLogs()) {
                    Element logElement = new Element(PortContract.LogMetadata.Log.itemName);
                    logElement.setAttribute(PortContract.LogMetadata.Log.fromOperationId,
                            String.valueOf(log.getFromOperationId()));
                    logElement.setAttribute(PortContract.LogMetadata.Log.toOperationId,
                            String.valueOf(log.getToOperationId()));
                    logElement.setAttribute(PortContract.LogMetadata.Log.fromOperationTime,
                            String.valueOf(log.getFromOperationTime()));
                    logElement.setAttribute(PortContract.LogMetadata.Log.toOperationTime,
                            String.valueOf(log.getToOperationTime()));
                    logElement.setAttribute(PortContract.LogMetadata.Log.index,
                            String.valueOf(log.getIndex()));
                    metadataElement.addContent(logElement);
                }
            }

            return new Document(portElement);
        }
    }
}
