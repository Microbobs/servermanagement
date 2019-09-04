package de.microbob.model;

public class KonfigurationsDatei {

    private String dateiname;

    private String absoulterPfad;

    public KonfigurationsDatei(String dateiname, String absoluterPfad) {
        this.dateiname = dateiname;
        this.absoulterPfad = absoluterPfad;
    }

    public String getDateiname() {
        return dateiname;
    }

    public void setDateiname(String dateiname) {
        this.dateiname = dateiname;
    }

    public String getAbsoulterPfad() {
        return absoulterPfad;
    }

    public void setAbsoulterPfad(String absoulterPfad) {
        this.absoulterPfad = absoulterPfad;
    }
}
