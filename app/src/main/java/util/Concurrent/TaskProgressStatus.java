package util.Concurrent;

/**
 * Created by Ahmad on 02/16/18.
 * All rights reserved.
 */

public class TaskProgressStatus {
    private String status;
    private Integer statusResId;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Integer getStatusResId() {
        return statusResId;
    }

    public void setStatusResId(Integer statusResId) {
        this.statusResId = statusResId;
    }
}
