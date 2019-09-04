package de.microbob.model;

import de.microbob.constant.OperationSystem;
import de.microbob.constant.ServerStatus;
import de.microbob.constant.ServerTyp;

public class Server {

    private String name;
    private String pfad;
    private String port;

    private ServerTyp typ;

    private String benutzer;

    private String passwort;
    private String remotePfad;
    private OperationSystem operationSystem;

    private transient ServerStatus status;

    private transient String webapps;

    private boolean isLocal;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPfad() {
        return pfad;
    }

    public void setPfad(String pfad) {
        this.pfad = pfad;
    }

    public ServerTyp getTyp() {
        return typ;
    }

    public void setTyp(ServerTyp typ) {
        this.typ = typ;
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public boolean isLocal() {
        return isLocal;
    }

    public void setLocal(boolean local) {
        isLocal = local;
    }

    public ServerStatus getStatus() {
        return status;
    }

    public void setStatus(ServerStatus status) {
        this.status = status;
    }

    public String getWebapps() {
        return webapps;
    }

    public void setWebapps(String webapps) {
        this.webapps = webapps;
    }

    public String getBenutzer() {
        return benutzer;
    }

    public void setBenutzer(String benutzer) {
        this.benutzer = benutzer;
    }

    public String getPasswort() {
        return passwort;
    }

    public void setPasswort(String passwort) {
        this.passwort = passwort;
    }

    public String getRemotePfad() {
        return remotePfad;
    }

    public void setRemotePfad(String remotePfad) {
        this.remotePfad = remotePfad;
    }

    public OperationSystem getOperationSystem() {
        return operationSystem;
    }

    public void setOperationSystem(OperationSystem operationSystem) {
        this.operationSystem = operationSystem;
    }
}

