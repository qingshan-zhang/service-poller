package se.kry.codetest;

public class ServiceStatus {
    private final String url;
    private Status status;

    public ServiceStatus(String url) {
        this.url = url;
        this.status = Status.UNKNOWN;
    }

    public Status getStatus() {
        return status;
    }

    public String getUrl() {
        return url;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public enum Status {
        OK,
        FAIL,
        UNKNOWN
    }
}
