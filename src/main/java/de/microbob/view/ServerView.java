package de.microbob.view;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class ServerView {

    private StringProperty name;
    private StringProperty status;

    public ServerView(String name, String status) {
        this.name = new SimpleStringProperty(name);
        this.status = new SimpleStringProperty(status);
    }

    public String getName() {
        return name.getValue();
    }

    public void setName(String name) {
        this.name.setValue(name);
    }

    public StringProperty nameProperty() {
        return name;
    }

    public String getStatus() {
        return status.getValue();
    }

    public void setStatus(String status) {
        this.status.setValue(status);
    }

    public StringProperty statusProperty() {
        return status;
    }
}
