package data.xml.log.operator;

import android.content.Context;
import android.provider.DocumentsProvider;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.TreeMap;

import javax.annotation.Nullable;

import data.storage.Log;
import data.xml.port.Port;
import data.xml.util.BufferedDocumentProvider;
import data.xml.util.DocumentInitializer;

/**
 * Created by Ahmad on 02/02/18.
 * All rights reserved.
 */

public class LogDocuments {
    static final int logFileLengthThreshold = 1024 * 1024;

    private static TreeMap<Long, BufferedDocumentProvider> cache = new TreeMap<>();


    static void reloadLogDocuments() {
        for (BufferedDocumentProvider documentProvider : cache.values()) {
            documentProvider.reloadDocument();
        }
    }

    //========================================= Document ===========================================
    private static BufferedDocumentProvider getDocumentProvider(int port, int index, @Nullable DocumentInitializer initializer) throws JDOMException, IOException {
        BufferedDocumentProvider documentProvider = getCachedDocumentProvider(port, index);
        if (documentProvider == null) {
            File documentFile = Log.getLogFile(port, index);
            documentProvider = BufferedDocumentProvider.newInstance(documentFile, initializer);
            putDocumentProvider(port, index, documentProvider);
        }
        return documentProvider;
    }

    static Document getDocument(int port, int index, @Nullable DocumentInitializer initializer) throws JDOMException, IOException {
        return getDocumentProvider(port, index, initializer).getDocument();
    }

    static void writeDocument(final Document document, int port, int index) {
        try {
            getDocumentProvider(port, index, null).writeDocument(document);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static BufferedDocumentProvider getCachedDocumentProvider(int port, int index) {
        long pattern = (index << Port.idShiftCount) | port;
        return cache.get(pattern);
    }

    private static BufferedDocumentProvider putDocumentProvider(int port, int index, BufferedDocumentProvider document) {
        long pattern = (index << Port.idShiftCount) | port;
        return cache.put(pattern, document);
    }

    static boolean doesExceedSizeLimit(final  int port, int index) {
        if (index < LogContract.StartLogIndex) {
            return true;
        }
        File documentFile = Log.getLogFile(port, index);
        return documentFile.length() > logFileLengthThreshold;
    }
}
