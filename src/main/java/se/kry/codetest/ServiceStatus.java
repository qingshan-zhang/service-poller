package se.kry.codetest;

public class ServiceStatus {
    private String url;
    private Status status;
    private String name;

    public ServiceStatus(String url, String name) {
        this.url = url;
        this.name = name;
        this.status = Status.UNKNOWN;
    }

    public Status getStatus() {
        return status;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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
