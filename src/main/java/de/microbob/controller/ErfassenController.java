package de.microbob.controller;

import de.microbob.MainApplication;
import de.microbob.constant.OperationSystem;
import de.microbob.constant.ServerTyp;
import de.microbob.model.Server;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

import java.io.File;
import java.util.List;

import static de.microbob.MainApplication.*;

public class ErfassenController {

    public static final String LABELTEXT_HOST = "Host";
    private static final String LABELTEXT_PFAD = "Pfad";

    @FXML
    private VBox erfassenVB;

    @FXML
    private Button durchsuchenBtn;

    @FXML
    private ChoiceBox<ServerTyp> typCB;

    @FXML
    private CheckBox remoteCB;

    @FXML
    private Label pfadLabel;

    @FXML
    private TextField pfadTF;

    @FXML
    private TextField nameTF;

    @FXML
    private TextField portTF;

    @FXML
    private HBox benutzerHB;

    @FXML
    private TextField benutzerTF;

    @FXML
    private HBox passwortHB;

    @FXML
    private PasswordField passwortPF;

    @FXML
    private HBox remotePfadHB;

    @FXML
    private TextField remotePfadTF;

    private MainApplication mainApplication;
    private Stage stage;

    private Server serverInEdit = null;

    @FXML
    void initialize() {
        typCB.setItems(FXCollections.observableArrayList(ServerTyp.values()));

        portTF.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                portTF.setText(newValue.replaceAll("[^\\d]", ""));
            }
        });

        erfassenVB.getChildren().remove(benutzerHB);
        erfassenVB.getChildren().remove(passwortHB);
        erfassenVB.getChildren().remove(remotePfadHB);
    }

    @FXML
    void onDurchsuchen(ActionEvent event) {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        File directory = directoryChooser.showDialog(stage);
        if (directory != null) {
            pfadTF.setText(directory.getAbsolutePath());
        }
    }

    @FXML
    void onSpeichern(ActionEvent event) {
        String name = nameTF.getText();
        String pfad = pfadTF.getText();
        String port = portTF.getText();
        String benutzer = benutzerTF.getText();
        String passwort = passwortPF.getText();
        String remotePfad = remotePfadTF.getText();
        ServerTyp typ = typCB.getValue();

        boolean baseFieldsFilled =
                name != null && !name.isEmpty() && pfad != null && !pfad.isEmpty() && typ != null && port != null && !port.isEmpty();
        boolean remoteFieldsFilled =
                (remoteCB.isSelected() && benutzer != null && !benutzer.isEmpty() && passwort != null && !passwort.isEmpty()
                        && remotePfad != null && !remotePfad.isEmpty()) || !remoteCB.isSelected();
        if (baseFieldsFilled && remoteFieldsFilled) {
            Server server;

            boolean inEdit;
            if (serverInEdit == null) {
                server = new Server();
                inEdit = false;
            } else {
                inEdit = true;
                server = serverInEdit;
            }
            server.setName(name);
            server.setPfad(pfad);
            server.setPort(port);
            server.setLocal(!remoteCB.isSelected());
            server.setTyp(typ);
            if (remoteCB.isSelected()) {
                server.setBenutzer(benutzer);
                server.setPasswort(passwort);
                server.setRemotePfad(remotePfad);

                OperationSystem os = OperationSystem.WIN;
                if (remotePfad != null && remotePfad.startsWith("/")) {
                    os = OperationSystem.UNIX;
                }
                server.setOperationSystem(os);
            }

            if (!inEdit) {
                List<Server> servers = getServers();
                servers.add(server);
            }

            ((Node) (event.getSource())).getScene().getWindow().hide();
        } else {
            Alert alert = new Alert(Alert.AlertType.WARNING, "Bitte alle Felder ausf\u00FCllen!");
            ((Stage) alert.getDialogPane().getScene().getWindow()).getIcons()
                    .add(new Image(getClass().getClassLoader().getResourceAsStream("images/main_icon.png")));
            alert.setTitle("Pflichtfelder benötigt");
            alert.getDialogPane().getStylesheets().add("css/main_dark.css");
            alert.showAndWait();
        }
    }

    @FXML
    void onRemote(ActionEvent event) {
        boolean remote = remoteCB.isSelected();

        ObservableList<Node> vbChildren = erfassenVB.getChildren();
        if (remote) {
            pfadLabel.setText(LABELTEXT_HOST);
            durchsuchenBtn.setVisible(false);

            if (!vbChildren.contains(benutzerHB)) {
                vbChildren.add(4, benutzerHB);
                vbChildren.add(5, passwortHB);
                vbChildren.add(6, remotePfadHB);
            }
        } else {
            pfadLabel.setText(LABELTEXT_PFAD);
            durchsuchenBtn.setVisible(true);

            vbChildren.remove(benutzerHB);
            vbChildren.remove(passwortHB);
            vbChildren.remove(remotePfadHB);
        }
        erfassenVB.autosize();
        stage.sizeToScene();
    }

    public void setServerToEdit(Server server) {
        serverInEdit = server;

        nameTF.setText(server.getName());
        pfadTF.setText(server.getPfad());
        portTF.setText(server.getPort());
        typCB.setValue(server.getTyp());
        if (server.isLocal()) {
            remoteCB.setSelected(false);
        } else {
            remoteCB.setSelected(true);
            remotePfadTF.setText(server.getRemotePfad());
            benutzerTF.setText(server.getBenutzer());
            passwortPF.setText(server.getPasswort());
        }
        onRemote(null);
    }

    public void setMainApplication(MainApplication mainApplication) {
        this.mainApplication = mainApplication;
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public Stage getStage() {
        return stage;
    }
}
