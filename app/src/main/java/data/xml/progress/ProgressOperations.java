package data.xml.progress;

import android.content.Context;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import data.xml.log.operator.LogContract;
import data.xml.util.BufferedDocumentProvider;
import data.xml.util.DocumentInitializer;

/**
 * Created by Ahmad on 02/10/18.
 * All rights reserved.
 */

public class ProgressOperations {
    private static BufferedDocumentProvider documentReader;
    private static Progress progress;

    public static void reloadProgress() {
        if (documentReader != null) {
            documentReader.reloadDocument();
        }
        progress = null;
    }

    public static void waitUntilNoOperations() {
        if (documentReader != null) {
            documentReader.waitUntilNoWrite();
        }
    }

    private static BufferedDocumentProvider getDocumentReader(Context context) {
        if (documentReader == null) {
            documentReader = BufferedDocumentProvider.newInstance(
                    data.storage.Progress.getFile(context),
                    new DocumentInitializer() {
                        @Override
                        public Document initializeDocument() {
                            Element element = new Element(ProgressContract.itemName);
                            return new Document(element);
                        }
                    }
            );
        }
        return documentReader;
    }

    public static Progress getProgress(Context context) {
        if (progress == null) try {
            progress = new Progress();
            Document document = getDocumentReader(context).getDocument();
            Collection<Element> elements = document.getRootElement().getChildren();
            for (Element element : elements) {
                if (element.getName().equals(ProgressContract.PortProgress.itemName)) {
                    Progress.PortProgress portProgress = new Progress.PortProgress();
                    portProgress.setPort(element.getAttribute(ProgressContract.PortProgress.port).getIntValue());
                    portProgress.setLastPerformedOperationId(element.getAttribute(
                            ProgressContract.PortProgress.lastPerformedOperation).getLongValue());
                    progress.setPortProgress(portProgress);
                }
            }
        } catch (JDOMException|IOException e) {
            e.printStackTrace();
        }
        return progress.clone();
    }

    public static void writeProgress(Progress progress, Context context, boolean async) {
        ProgressOperations.progress = progress.clone();
        Element progressElement = new Element(ProgressContract.itemName);
        for (Progress.PortProgress portProgress : progress.getIterable()) {
            Element agentElement = new Element(ProgressContract.PortProgress.itemName);
            agentElement.setAttribute(ProgressContract.PortProgress.port, String.valueOf(portProgress.getPort()));
            agentElement.setAttribute(ProgressContract.PortProgress.lastPerformedOperation,
                    String.valueOf(portProgress.getLastPerformedOperationId()));
            progressElement.addContent(agentElement);
        }
        Document document = new Document(progressElement);
        if (async) {
            getDocumentReader(context).writeDocumentAsync(document);
        } else {
            getDocumentReader(context).writeDocument(document);
        }
    }

    public static class Progress {

        private ArrayList<PortProgress> portProgresses;

        private Progress() {
            portProgresses = new ArrayList<>();
        }

        public long getLastPerformedOperationId(int port) {
            PortProgress portProgress = getPortProgress(port);
            if (portProgress == null) {
                portProgress = new PortProgress(port, LogContract.StartOperationId - 1);
                setPortProgress(portProgress);
            }
            return portProgress.getLastPerformedOperationId();
        }

        public void setLastPerformedOperationId(long lastPerformedOperationId, int port) {
            PortProgress portProgress = getPortProgress(port);
            if (portProgress == null) {
                portProgress = new PortProgress(port, LogContract.StartOperationId - 1);
                setPortProgress(portProgress);
            }
            portProgress.setLastPerformedOperationId(lastPerformedOperationId);
        }

        private PortProgress getPortProgress(int port) {
            for (PortProgress portProgress : portProgresses) {
                if (portProgress.getPort() == port) {
                    return portProgress;
                }
            }
            return null;
        }

        private void setPortProgress(PortProgress portProgress) {
            boolean agentFound = false;
            for (int i = 0; i < portProgresses.size() && !agentFound; i++) {
                if (portProgresses.get(i).getPort() == portProgress.getPort()) {
                    portProgresses.set(i, portProgress);
                    agentFound = true;
                }
            }
            if (!agentFound) {
                portProgresses.add(portProgress);
            }
        }

        private Iterable<PortProgress> getIterable() {
            return portProgresses;
        }

        public Progress clone() {
            Progress clone = new Progress();
            clone.portProgresses = new ArrayList<>(portProgresses.size());
            for (PortProgress portProgress : portProgresses) {
                clone.portProgresses.add(portProgress.clone());
            }
            return clone;
        }

        private static class PortProgress {
            private int port;
            private long lastPerformedOperationId;

            public PortProgress(int port, long lastPerformedOperationId) {
                this.port = port;
                this.lastPerformedOperationId = lastPerformedOperationId;
            }

            public PortProgress() {
            }

            public int getPort() {
                return port;
            }

            public void setPort(int port) {
                this.port = port;
            }

            public long getLastPerformedOperationId() {
                return lastPerformedOperationId;
            }

            public void setLastPerformedOperationId(long lastPerformedOperationId) {
                this.lastPerformedOperationId = lastPerformedOperationId;
            }

            public PortProgress clone() {
                return new PortProgress(port, lastPerformedOperationId);
            }
        }
    }
}
