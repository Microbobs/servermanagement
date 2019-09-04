package de.microbob.constant;

public enum ServerStatus {
    UNKNOWN("Unknown"), RUNNING("Running"), STOPPED("Stopped");

    private String displayname;

    ServerStatus(String displayname) {
        this.displayname = displayname;
    }

    public String getDisplayname() {
        return displayname;
    }

    public void setDisplayname(String displayname) {
        this.displayname = displayname;
    }

    @Override
    public String toString() {
        return displayname;
    }
}
