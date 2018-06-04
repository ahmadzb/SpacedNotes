package data.xml.log.operator;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.TreeMap;

import javax.annotation.Nullable;

import data.xml.port.Port;
import data.xml.port.PortOperations;
import data.xml.util.DocumentInitializer;

/**
 * Created by Ahmad on 02/19/18.
 * All rights reserved.
 */

public class OperationCatalog {

    public static ArrayList<Operation> getOperations(int port, Filter filter, @Nullable DocumentCache cache) {
        Port.LogMetadata metadata = PortOperations.connectionFor(port).getPort().getLogMetadata();
        ArrayList<Operation> operations = new ArrayList<>();
        try {
            for (Port.LogMetadata.Log log : metadata.getLogs()) {
                if (doesInclude(log, filter)) {
                    Document document = null;
                    if (cache != null) {
                        document = cache.getDocument(port, log.getIndex(), null);
                    } else {
                        document = LogDocuments.getDocument(port, log.getIndex(), null);
                    }
                    if (document != null) {
                        Element logElement = document.getRootElement();
                        for (Element child : logElement.getChildren()) {
                            if (child.getName().equals(LogContract.Operation.itemName)) {
                                Operation operation = Operation.newInstance();
                                operation.setId(child.getAttribute(LogContract.Operation.id).getLongValue());
                                operation.setPort(port);
                                operation.setTime(child.getAttribute(LogContract.Operation.time).getLongValue());
                                operation.setOperator(child.getAttribute(LogContract.Operation.operator).getValue());
                                operation.setProfileId(child.getAttribute(LogContract.Operation.profileId).getLongValue());
                                operation.setElement(child.getChildren().get(0));
                                if (doesInclude(operation, filter)) {
                                    operations.add(operation);
                                }
                            }
                        }
                    }
                }
            }
        } catch (JDOMException|IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        return operations;
    }

    private static boolean doesInclude(Port.LogMetadata.Log log, Filter filter) {
        if (filter == null) return true;

        boolean include = true;

        //From
        if (include) {
            Boolean time = null;
            Boolean id = null;
            if (filter.fromTime != null) {
                time = filter.fromTime <= log.getToOperationTime();
            }
            if (filter.fromId != null) {
                id = filter.fromId <= log.getToOperationId();
            }
            if (time != null && id != null) {
                if (filter.fromResolution == Filter.RESOLUTION_AND) {
                    include = time && id;
                } else if (filter.fromResolution == Filter.RESOLUTION_OR) {
                    include = time || id;
                } else {
                    throw new RuntimeException("From resolution is not specified");
                }
            } else if (time != null) {
                include = time;
            } else if (id != null) {
                include = id;
            }
        }
        //To
        if (include) {
            Boolean time = null;
            Boolean id = null;
            if (filter.toTime != null) {
                time = filter.toTime >= log.getFromOperationTime();
            }
            if (filter.toId != null) {
                id = filter.toId >= log.getFromOperationId();
            }
            if (time != null && id != null) {
                if (filter.toResolution == Filter.RESOLUTION_AND) {
                    include = time && id;
                } else if (filter.toResolution == Filter.RESOLUTION_OR) {
                    include = time || id;
                } else {
                    throw new RuntimeException("To resolution is not specified");
                }
            } else if (time != null) {
                include = time;
            } else if (id != null) {
                include = id;
            }
        }

        return include;
    }

    private static boolean doesInclude(Operation operation, Filter filter) {
        if (filter == null) return true;

        boolean include = true;

        //from
        if (include) {
            Boolean time = null;
            Boolean id = null;
            if (filter.fromTime != null) {
                time = filter.fromTime <= operation.getTime();
            }
            if (filter.fromId != null) {
                id = filter.fromId <= operation.getId();
            }
            if (time != null && id != null) {
                if (filter.fromResolution == Filter.RESOLUTION_AND) {
                    include = time && id;
                } else if (filter.fromResolution == Filter.RESOLUTION_OR) {
                    include = time || id;
                } else {
                    throw new RuntimeException("From resolution is not specified");
                }
            } else if (time != null) {
                include = time;
            } else if (id != null) {
                include = id;
            }
        }
        //To
        if (include) {
            Boolean time = null;
            Boolean id = null;
            if (filter.toTime != null) {
                time = filter.toTime >= operation.getTime();
            }
            if (filter.toId != null) {
                id = filter.toId >= operation.getId();
            }
            if (time != null && id != null) {
                if (filter.toResolution == Filter.RESOLUTION_AND) {
                    include = time && id;
                } else if (filter.toResolution == Filter.RESOLUTION_OR) {
                    include = time || id;
                } else {
                    throw new RuntimeException("To resolution is not specified");
                }
            } else if (time != null) {
                include = time;
            } else if (id != null) {
                include = id;
            }
        }
        //Operator
        if (include) {
            if (filter.getOperator() != null) {
                include = filter.getOperator().equals(operation.getOperator());
            }
        }
        //ProfileId
        if (include) {
            if (filter.getProfileId() != null) {
                include = filter.getProfileId().equals(operation.getProfileId());
            }
        }

        return include;
    }

    public static class Filter {
        public static final int RESOLUTION_AND = 1;
        public static final int RESOLUTION_OR = 2;

        private Long fromTime;
        private Long fromId;
        private int fromResolution;

        private Long toTime;
        private Long toId;
        private int toResolution;

        private String operator;
        private Long profileId;

        private Filter() {

        }

        public static Filter newInstance() {
            return new Filter();
        }

        public Long getFromTime() {
            return fromTime;
        }

        public void setFromTime(Long fromTime) {
            this.fromTime = fromTime;
        }

        public Long getFromId() {
            return fromId;
        }

        public void setFromId(Long fromId) {
            this.fromId = fromId;
        }

        public int getFromResolution() {
            return fromResolution;
        }

        public void setFromResolution(int fromResolution) {
            this.fromResolution = fromResolution;
        }

        public Long getToTime() {
            return toTime;
        }

        public void setToTime(Long toTime) {
            this.toTime = toTime;
        }

        public Long getToId() {
            return toId;
        }

        public void setToId(Long toId) {
            this.toId = toId;
        }

        public int getToResolution() {
            return toResolution;
        }

        public void setToResolution(int toResolution) {
            this.toResolution = toResolution;
        }

        public String getOperator() {
            return operator;
        }

        public void setOperator(String operator) {
            this.operator = operator;
        }

        public Long getProfileId() {
            return profileId;
        }

        public void setProfileId(Long profileId) {
            this.profileId = profileId;
        }
    }

    public static class DocumentCache {
        TreeMap<Long, Document> cache = new TreeMap<>();

        private DocumentCache() {

        }

        public static DocumentCache newInstance() {
            return new DocumentCache();
        }

        public Document getDocument(int port, int index, @Nullable DocumentInitializer initializer) throws JDOMException, IOException {
            Document document = getCachedDocument(port, index);
            if (document == null) {
                document = LogDocuments.getDocument(port, index, initializer);
                if (document != null) {
                    putDocument(port, index, document);
                }
            }
            return document;
        }

        public Document getCachedDocument(int port, int index) {
            long pattern = (index << Port.idShiftCount) | port;
            return cache.get(pattern);
        }

        public Document putDocument(int port, int index, Document document) {
            long pattern = (index << Port.idShiftCount) | port;
            return cache.put(pattern, document);
        }

    }
}
