package data.xml.util;

import org.jdom2.Document;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by Ahmad on 02/10/18.
 * All rights reserved.
 */

public class BufferedDocumentProvider {
    private static final int WRITE_STATE_IDLE = 0;
    private static final int WRITE_STATE_RUNNING = 1;
    private static final int WRITE_STATE_RUNNING_AND_PENDING = 2;
    private int writeState;
    private Observer writeStateLock = new Observer();
    private Observer waiter = new Observer();

    private Document currentDocument = null;
    private boolean isInTransaction = false;
    private boolean isTransactionSuccessful = false;

    private File documentFile;
    private DocumentInitializer initializer;

    //========================================= Document ===========================================
    private BufferedDocumentProvider() {

    }

    public static BufferedDocumentProvider newInstance(File documentFile, DocumentInitializer initializer) {
        BufferedDocumentProvider bufferedDocumentProvider = new BufferedDocumentProvider();
        bufferedDocumentProvider.documentFile = documentFile;
        bufferedDocumentProvider.initializer = initializer;
        return bufferedDocumentProvider;
    }

    public void waitUntilNoWrite() {
        boolean shouldWait = true;
        while (shouldWait) {
            synchronized (writeStateLock) {
                shouldWait = writeState != WRITE_STATE_IDLE;
            }
            if (shouldWait) {
                synchronized (waiter) {
                    try {
                        waiter.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    public void reloadDocument() {
        currentDocument = null;
    }

    public Document getDocument() throws JDOMException, IOException {
        if (currentDocument == null) {
            if (documentFile.exists()) {
                SAXBuilder builder = new SAXBuilder();
                currentDocument = builder.build(documentFile);
            } else {
                currentDocument = initializer.initializeDocument();
            }
        }
        return currentDocument;
    }

    public void writeDocumentAsync(Document document) {
        currentDocument = document;
        if (!isInTransaction) {
            boolean shouldTriggerWrite = false;
            synchronized (writeStateLock) {
                if (writeState == WRITE_STATE_IDLE) {
                    shouldTriggerWrite = true;
                    writeState = WRITE_STATE_RUNNING;
                } else if (writeState == WRITE_STATE_RUNNING) {
                    writeState = WRITE_STATE_RUNNING_AND_PENDING;
                } else if (writeState != WRITE_STATE_RUNNING_AND_PENDING){
                    throw new RuntimeException("writeState is in invalid state");
                }
            }
            if (shouldTriggerWrite) {
                Thread thread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        boolean shouldWrite = true;
                        while (shouldWrite) {
                            performWrite(currentDocument);
                            synchronized (writeStateLock) {
                                if (writeState == WRITE_STATE_RUNNING_AND_PENDING) {
                                    writeState = WRITE_STATE_RUNNING;
                                } else if (writeState == WRITE_STATE_RUNNING) {
                                    writeState = WRITE_STATE_IDLE;
                                    shouldWrite = false;
                                    synchronized (waiter) {
                                        waiter.notifyAll();
                                    }
                                } else {
                                    throw new RuntimeException("writeState is in invalid state");
                                }
                            }
                        }
                    }
                });
                thread.start();
            }
        }
    }

    public void writeDocument(Document document) {
        currentDocument = document;
        if (!isInTransaction) {
            performWrite(document);
        }
    }

    private synchronized void performWrite(Document document) {
        try {
            XMLOutputter xmlOutput = new XMLOutputter();
            // display xml
            xmlOutput.setFormat(Format.getPrettyFormat());
            FileOutputStream outputStream = new FileOutputStream(documentFile);
            xmlOutput.output(document.clone(), outputStream);
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //======================================= Transaction ==========================================
    public void beginTransaction() {
        isInTransaction = true;
    }

    public void setTransactionSuccessful() {
        if (isInTransaction) {
            isTransactionSuccessful = true;
        }
    }

    public void endTransaction() {
        boolean shouldWrite = isInTransaction && isTransactionSuccessful && currentDocument != null;

        isInTransaction = false;
        isTransactionSuccessful = false;

        if (shouldWrite) {
            writeDocumentAsync(currentDocument);
        }
    }

    //=========================================
    private static class Observer {
    }
}
