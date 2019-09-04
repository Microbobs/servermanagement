package de.microbob.model;

public class ConfigurationFile {

    private String filename;

    private String absoultePath;

    public ConfigurationFile(String filename, String absoultePath) {
        this.filename = filename;
        this.absoultePath = absoultePath;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getAbsoultePath() {
        return absoultePath;
    }

    public void setAbsoultePath(String absoultePath) {
        this.absoultePath = absoultePath;
    }
}
