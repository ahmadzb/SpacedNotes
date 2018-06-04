package data.xml.log.operator;

import org.jdom2.Element;

import java.util.Comparator;

/**
 * Created by Ahmad on 02/19/18.
 * All rights reserved.
 */

public class Operation {
    private long id;
    private int port;
    private long time;
    private long profileId;
    private String operator;
    private Element element;

    private Operation() {

    }

    public static Operation newInstance() {
        return new Operation();
    }

    public long getId() {
        return id;
    }

    public Operation setId(long id) {
        this.id = id;
        return this;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public long getTime() {
        return time;
    }

    public Operation setTime(long time) {
        this.time = time;
        return this;
    }

    public long getProfileId() {
        return profileId;
    }

    public Operation setProfileId(long profileId) {
        this.profileId = profileId;
        return this;
    }

    public String getOperator() {
        return operator;
    }

    public Operation setOperator(String operator) {
        this.operator = operator;
        return this;
    }

    public Element getElement() {
        return element;
    }

    public Operation setElement(Element element) {
        this.element = element;
        return this;
    }

    public static class OperationTimeComparator implements Comparator<Operation> {
        @Override
        public int compare(Operation o1, Operation o2) {
            return Long.compare(o1.getTime(), o2.getTime());
        }
    }
}
