package de.microbob.constant;

public enum ServerStatus {
    UNKNOWN("Unbekannt"), RUNNING("L\u00E4uft"), STOPPED("Gestoppt");

    private String anzeigename;

    ServerStatus(String anzeigename) {
        this.anzeigename = anzeigename;
    }

    public String getAnzeigename() {
        return anzeigename;
    }

    public void setAnzeigename(String anzeigename) {
        this.anzeigename = anzeigename;
    }

    @Override
    public String toString() {
        return anzeigename;
    }
}
