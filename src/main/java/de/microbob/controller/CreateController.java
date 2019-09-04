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

public class CreateController {

    public static final String LABELTEXT_HOST = "Host";
    private static final String LABELTEXT_PATH = "Path";

    @FXML
    private VBox createVB;

    @FXML
    private Button filesearchBtn;

    @FXML
    private ChoiceBox<ServerTyp> typCB;

    @FXML
    private CheckBox remoteCB;

    @FXML
    private Label pathLabel;

    @FXML
    private TextField pathTF;

    @FXML
    private TextField nameTF;

    @FXML
    private TextField portTF;

    @FXML
    private HBox userHB;

    @FXML
    private TextField userTF;

    @FXML
    private HBox passwordHB;

    @FXML
    private PasswordField passwordPF;

    @FXML
    private HBox remotePathHB;

    @FXML
    private TextField remotePathTF;

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

        createVB.getChildren().remove(userHB);
        createVB.getChildren().remove(passwordHB);
        createVB.getChildren().remove(remotePathHB);
    }

    @FXML
    void onFileSearch(ActionEvent event) {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        File directory = directoryChooser.showDialog(stage);
        if (directory != null) {
            pathTF.setText(directory.getAbsolutePath());
        }
    }

    @FXML
    void onSave(ActionEvent event) {
        String name = nameTF.getText();
        String path = pathTF.getText();
        String port = portTF.getText();
        String user = userTF.getText();
        String password = passwordPF.getText();
        String remotePath = remotePathTF.getText();
        ServerTyp typ = typCB.getValue();

        boolean baseFieldsFilled =
                name != null && !name.isEmpty() && path != null && !path.isEmpty() && typ != null && port != null && !port.isEmpty();
        boolean remoteFieldsFilled =
                (remoteCB.isSelected() && user != null && !user.isEmpty() && password != null && !password.isEmpty()
                        && remotePath != null && !remotePath.isEmpty()) || !remoteCB.isSelected();
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
            server.setPath(path);
            server.setPort(port);
            server.setLocal(!remoteCB.isSelected());
            server.setTyp(typ);
            if (remoteCB.isSelected()) {
                server.setUser(user);
                server.setPassword(password);
                server.setRemotePath(remotePath);

                OperationSystem os = OperationSystem.WIN;
                if (remotePath != null && remotePath.startsWith("/")) {
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

        ObservableList<Node> vbChildren = createVB.getChildren();
        if (remote) {
            pathLabel.setText(LABELTEXT_HOST);
            filesearchBtn.setVisible(false);

            if (!vbChildren.contains(userHB)) {
                vbChildren.add(4, userHB);
                vbChildren.add(5, passwordHB);
                vbChildren.add(6, remotePathHB);
            }
        } else {
            pathLabel.setText(LABELTEXT_PATH);
            filesearchBtn.setVisible(true);

            vbChildren.remove(userHB);
            vbChildren.remove(passwordHB);
            vbChildren.remove(remotePathHB);
        }
        createVB.autosize();
        stage.sizeToScene();
    }

    public void setServerToEdit(Server server) {
        serverInEdit = server;

        nameTF.setText(server.getName());
        pathTF.setText(server.getPath());
        portTF.setText(server.getPort());
        typCB.setValue(server.getTyp());
        if (server.isLocal()) {
            remoteCB.setSelected(false);
        } else {
            remoteCB.setSelected(true);
            remotePathTF.setText(server.getRemotePath());
            userTF.setText(server.getUser());
            passwordPF.setText(server.getPassword());
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
