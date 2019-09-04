package de.microbob;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.UIKeyboardInteractive;
import com.jcraft.jsch.UserInfo;
import de.microbob.constant.OperationSystem;
import de.microbob.constant.ServerStatus;
import de.microbob.constant.ServerTyp;
import de.microbob.controller.ConfigureController;
import de.microbob.controller.CreateController;
import de.microbob.exception.NotImplementedException;
import de.microbob.model.Server;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Callback;
import javafx.util.Duration;
import javafx.util.Pair;

import java.awt.*;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class MainApplication extends Application {

    private static final String SERVERS_FILENAME = "servers.json";

    private static final String currentDir = System.getProperty("user.dir");
    private static final Path PATH_TO_INPUT = Paths.get(currentDir, SERVERS_FILENAME);
    private static final String CATALINA_HOME = "CATALINA_HOME";

    public static boolean darktheme = true;

    private static Map<Server, Session> sessionForServer = new HashMap<>();

    //        For Custom Titlebar
//    @FXML
//    public Button minimizeBtn;
//
//    @FXML
//    public Button maximizeBtn;
//
//    @FXML
//    public Button closeBtn;

    @FXML
    public Button createBtn;
    @FXML
    public Button editBtn;
    @FXML
    public Button configureBtn;
    @FXML
    public Button inExplorerBtn;
    @FXML
    public Button deleteBtn;

    @FXML
    private TableView<Server> serverTV;

    @FXML
    private TableColumn<Server, ImageView> iconTC;

    @FXML
    private TableColumn<Server, String> serverTC;

    @FXML
    private TableColumn<Server, String> webappsTC;

    @FXML
    private TableColumn<Server, String> statusTC;

    @FXML
    private TableColumn<Server, String> actionTC;

    private static ObservableList<Server> servers;

    public static void main(String[] parameters) {
        if (parameters.length > 0) {
            darktheme = Boolean.parseBoolean(parameters[0]);
        }
        launch(parameters);
    }

    @Override
    public void start(Stage primaryStage) throws IOException {
        VBox root = FXMLLoader.load(getClass().getClassLoader().getResource("fxml/main.fxml"));
        Scene mainScene = new Scene(root);
        if (darktheme) {
            mainScene.getStylesheets().add("css/main_dark.css");
        }


        primaryStage.setTitle("Servermanagerment");
//        primaryStage.initStyle(StageStyle.UNDECORATED);
        primaryStage.setScene(mainScene);
        primaryStage.getIcons().add(new Image(getClass().getClassLoader().getResourceAsStream("images/main_icon.png")));
        primaryStage.show();
    }

    @Override
    public void stop() throws IOException {
        Gson gson = new Gson();

        servers.stream()
                .filter(s -> s.getPassword() != null)
                .forEach(s -> s.setPassword(new String(Base64.getEncoder().encode(s.getPassword().getBytes()))));
        String jsonString = gson.toJson(servers);

        //Sessions beenden
        sessionForServer.values().forEach(Session::disconnect);

        //json-Datei aktualisieren
        if (jsonString != null && !jsonString.isEmpty()) {
            Files.deleteIfExists(PATH_TO_INPUT);

            try (BufferedWriter output = new BufferedWriter(new FileWriter(PATH_TO_INPUT.toFile()))) {
                output.write(jsonString);
            }
        }
    }

    @Override
    public void init() throws IOException {
        if (Files.exists(PATH_TO_INPUT)) {
            StringBuilder inputStringBuilder = new StringBuilder();
            try (BufferedReader input = new BufferedReader(new FileReader(PATH_TO_INPUT.toFile()))) {
                String line = input.readLine();

                while (line != null) {
                    inputStringBuilder.append(line);
                    line = input.readLine();
                }
            }

            String inputString = inputStringBuilder.toString();

            if (!inputString.isEmpty()) {
                Gson gson = new Gson();
                Type listType = new TypeToken<List<Server>>() {
                }.getType();
                List<Server> persitedServers = gson.fromJson(inputString, listType);

                if (persitedServers != null) {
                    persitedServers.stream()
                            .filter(s -> s.getPassword() != null)
                            .forEach(s -> s.setPassword(new String(Base64.getDecoder().decode(s.getPassword().getBytes()))));
                    servers = FXCollections.observableList(persitedServers);
                }
            }
        }

        if (servers == null) {
            servers = FXCollections.observableList(new ArrayList<>());
        }
    }

    @FXML
    public void initialize() {
//        For Custom Titlebar
//        ImageView minimizeIcon = new ImageView(new Image(getClass().getClassLoader().getResource("images/minimize.png").toString()));
//        minimizeIcon.setFitHeight(30);
//        minimizeIcon.setFitWidth(45);
//        minimizeBtn.setGraphic(minimizeIcon);
//
//        ImageView maximizeIcon = new ImageView(new Image(getClass().getClassLoader().getResource("images/maximize.png").toString()));
//        maximizeIcon.setFitHeight(30);
//        maximizeIcon.setFitWidth(45);
//        maximizeBtn.setGraphic(maximizeIcon);
//
//        ImageView closeIcon = new ImageView(new Image(getClass().getClassLoader().getResource("images/close.png").toString()));
//        closeIcon.setFitHeight(30);
//        closeIcon.setFitWidth(45);
//        closeBtn.setGraphic(closeIcon);

        statusTC.getStyleClass().add("status-column");

        serverTC.setCellValueFactory(new PropertyValueFactory<>("name"));

        Callback<TableColumn<Server, String>, TableCell<Server, String>> webappsCellFactory = //
                new Callback<TableColumn<Server, String>, TableCell<Server, String>>() {
                    @Override
                    public TableCell<Server, String> call(final TableColumn<Server, String> param) {
                        return new TableCell<Server, String>() {
                            final HBox hBox = new HBox();

                            {
                                hBox.setSpacing(1);
                            }

                            @Override
                            public void updateItem(String item, boolean empty) {
                                super.updateItem(item, empty);
                                if (empty) {
                                    setGraphic(null);
                                    setText(null);
                                } else {
                                    Server selectedServer = getTableView().getItems().get(getIndex());

                                    if (selectedServer != null) {
                                        String webappsString = selectedServer.getWebapps();

                                        ObservableList<Node> children = hBox.getChildren();
                                        children.clear();
                                        if (webappsString != null) {
                                            String[] webapps = webappsString.split(" ");


                                            for (String webapp : webapps) {
                                                Hyperlink link = new Hyperlink(webapp);
                                                String webappWithoutKomma = webapp.replaceAll(",", "");

                                                StringBuilder urlStringBuiler = new StringBuilder("http://");
                                                if (selectedServer.isLocal()) {
                                                    urlStringBuiler.append("localhost");
                                                } else {
                                                    urlStringBuiler.append(selectedServer.getPath());
                                                }

                                                String urlWebapp = webappWithoutKomma.equalsIgnoreCase("ROOT") ? "" : webappWithoutKomma;
                                                urlStringBuiler.append(":").append(selectedServer.getPort()).append("/").append(urlWebapp);

                                                link.setOnAction(t -> getHostServices().showDocument(urlStringBuiler.toString()));

                                                children.add(link);
                                            }


                                            setGraphic(hBox);
                                            setText(null);
                                        }
                                    }
                                }
                            }
                        };
                    }
                };

        webappsTC.setCellFactory(webappsCellFactory);

        Callback<TableColumn<Server, String>, TableCell<Server, String>> statusCellFactory
                = //
                new Callback<TableColumn<Server, String>, TableCell<Server, String>>() {
                    @Override
                    public TableCell<Server, String> call(final TableColumn<Server, String> param) {
                        return new TableCell<Server, String>() {
                            {
                                this.setStyle("-fx-padding: 5px 10px 5px 10px;");
                            }

                            @Override
                            public void updateItem(String item, boolean empty) {
                                super.updateItem(item, empty);
                                if (empty) {
                                    setGraphic(null);
                                    setText(null);
                                } else {
                                    Server selectedServer = getTableView().getItems().get(getIndex());

                                    if (selectedServer != null) {
                                        ServerStatus status = selectedServer.getStatus();

                                        if (status != null) {

                                            switch (status) {
                                                case UNKNOWN:
                                                    this.setStyle("-fx-background-color: #be9117");
                                                    break;
                                                case RUNNING:
                                                    this.setStyle("-fx-background-color: #479651");
                                                    break;
                                                case STOPPED:
                                                    this.setStyle("-fx-background-color: #c75450");
                                                    break;
                                            }

                                            setText(status.getDisplayname());
                                        }
                                    }
                                }
                            }
                        };
                    }
                };

        statusTC.setCellFactory(statusCellFactory);

        Callback<TableColumn<Server, String>, TableCell<Server, String>> aktionCellFactory
                = //
                new Callback<TableColumn<Server, String>, TableCell<Server, String>>() {
                    @Override
                    public TableCell<Server, String> call(final TableColumn<Server, String> param) {
                        return new TableCell<Server, String>() {

                            final VBox vBox = new VBox();
                            final Button startBtn = new Button("Start");
                            final Button stopBtn = new Button("Stop");

                            {
                                vBox.setAlignment(Pos.CENTER);
                                startBtn.getStyleClass().add("start-button");
                                stopBtn.getStyleClass().add("stop-button");
                            }

                            @Override
                            public void updateItem(String item, boolean empty) {
                                super.updateItem(item, empty);
                                if (empty) {
                                    setGraphic(null);
                                    setText(null);
                                } else {
                                    Server selectedServer = getTableView().getItems().get(getIndex());

                                    if (selectedServer != null) {
                                        ServerStatus status = selectedServer.getStatus();
                                        ObservableList<Node> vBoxChildren = vBox.getChildren();

                                        if (ServerStatus.STOPPED == status) {
                                            startBtn.setOnAction(event -> startServer(selectedServer));

                                            vBoxChildren.remove(stopBtn);
                                            if (!vBoxChildren.contains(startBtn)) {
                                                vBoxChildren.add(startBtn);
                                            }

                                            setGraphic(vBox);
                                            setText(null);
                                        } else if (ServerStatus.RUNNING == status) {
                                            stopBtn.setOnAction(event -> stopServer(selectedServer));

                                            vBoxChildren.remove(startBtn);
                                            if (!vBoxChildren.contains(stopBtn)) {
                                                vBoxChildren.add(stopBtn);
                                            }

                                            setGraphic(vBox);
                                            setText(null);
                                        }
                                    }
                                }
                            }
                        };
                    }
                };

        actionTC.setCellFactory(aktionCellFactory);

        Callback<TableColumn<Server, ImageView>, TableCell<Server, ImageView>> iconCellFactory = new Callback<TableColumn<Server, ImageView>, TableCell<Server, ImageView>>() {
            @Override
            public TableCell<Server, ImageView> call(TableColumn<Server, ImageView> param) {
                return new TableCell<Server, ImageView>() {
                    final ImageView iconIV = new ImageView();

                    @Override
                    public void updateItem(ImageView item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setGraphic(null);
                            setText(null);
                        } else {
                            Server selectedServer = getTableView().getItems().get(getIndex());

                            if (selectedServer != null) {
                                ServerTyp typ = selectedServer.getTyp();

                                URL iconUrl = null;
                                switch (typ) {
                                    case TOMCAT:
                                        iconUrl = getClass().getClassLoader().getResource("images/apache-tomcat-icon.png");
                                        break;
                                    case WILDFLY:
                                        //                                        iconUrl = getClass().getClassLoader().getResource("images/wildfly_icon.png");
                                        break;
                                }

                                if (iconUrl != null) {
                                    iconIV.setImage(new Image(iconUrl.toString()));
                                    iconIV.setFitHeight(30);
                                    iconIV.setFitWidth(30);

                                    iconIV.setFitHeight(30);
                                    iconIV.setFitWidth(30);

                                    setGraphic(iconIV);
                                    setText(null);
                                }
                            }
                        }
                    }
                };
            }
        };

        iconTC.setCellFactory(iconCellFactory);
        iconTC.getStyleClass().

                add("icon-column");

        serverTV.setRowFactory(tv ->

        {
            TableRow<Server> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && (!row.isEmpty())) {
                    Server rowData = row.getItem();
                    handleActionForServer(rowData);
                }
            });
            row.setOnKeyReleased(event -> {
                if (KeyCode.SPACE == event.getCode() && !row.isEmpty()) {
                    Server rowData = row.getItem();
                    handleActionForServer(rowData);
                }
            });
            return row;
        });

        serverTV.setItems(servers);

        initTableViewData();
    }

    private void handleActionForServer(Server server) {
        if (server.getStatus() == ServerStatus.RUNNING) {
            stopServer(server);
        } else if (server.getStatus() == ServerStatus.STOPPED) {
            startServer(server);
        }
    }

    private void startServer(Server server) {
        String pathToServer = server.getPath();
        System.out.println("Starting Server " + server.getName()
                + "   " + pathToServer);

        if (server.isLocal()) {
            startLocalServer(server);
        } else {
            try {
                startRemoteServer(server);
            } catch (JSchException | IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void stopServer(Server server) {
        String pathToServer = server.getPath();
        System.out.println("Stopping Server " + server.getName()
                + "   " + pathToServer);

        if (server.isLocal()) {
            stopLocalServer(server);
        } else {
            try {
                stopRemoteServer(server);
            } catch (JSchException | IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void initTableViewData() {
        System.out.println("Main Thread: " + Thread.currentThread().getName());

        UpdateServersService service = new UpdateServersService(servers, serverTV);
        service.setPeriod(Duration.seconds(5));
        service.start();
    }

    @FXML
    void onCreate(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getClassLoader().getResource("fxml/erfassen.fxml"));
        VBox root = loader.load();

        CreateController createController = loader.getController();
        createController.setMainApplication(this);

        Stage stage = showNewWindow(root, "Create server");

        createController.setStage(stage);

        stage.showAndWait();
        Platform.runLater(() -> serverTV.refresh());
    }

    @FXML
    void onEdit(ActionEvent event) throws IOException {
        Optional<Server> toEdit = getSelectedServer();

        if (toEdit.isPresent()) {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getClassLoader().getResource("fxml/erfassen.fxml"));
            VBox root = loader.load();

            CreateController createController = loader.getController();
            createController.setMainApplication(this);

            Stage stage = showNewWindow(root, "Edit server");

            createController.setStage(stage);
            createController.setServerToEdit(toEdit.get());

            stage.showAndWait();
            Platform.runLater(() -> serverTV.refresh());
        }
    }

    @FXML
    void onDelete(ActionEvent event) {

        ButtonType buttonTypeJa = new ButtonType("Yes", ButtonBar.ButtonData.OK_DONE);
        ButtonType buttonTypeNein = new ButtonType("No", ButtonBar.ButtonData.CANCEL_CLOSE);
        Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION, "Are you sure about deleting the server?",
                buttonTypeJa, buttonTypeNein);
        ((Stage) confirmDialog.getDialogPane().getScene().getWindow()).getIcons()
                .add(new Image(getClass().getClassLoader().getResourceAsStream("images/main_icon.png")));
        confirmDialog.setTitle("Confirmation");
        confirmDialog.setHeaderText("Deleting Server");
        confirmDialog.getDialogPane().getStylesheets().add("css/main_dark.css");

        Optional<ButtonType> buttonType = confirmDialog.showAndWait();

        if (buttonType.isPresent() && buttonType.get().getButtonData() == ButtonBar.ButtonData.OK_DONE) {
            Optional<Server> toDelete = getSelectedServer();

            if (toDelete.isPresent()) {
                servers.remove(toDelete.get());
                Platform.runLater(() -> serverTV.refresh());
            }
        }
    }

    @FXML
    public void onInExplorer(ActionEvent event) {
        Optional<Server> selectedServer = getSelectedServer();

        if (selectedServer.isPresent()) {
            Server server = selectedServer.get();

            if (server.isLocal()) {
                try {
                    Path pathToServer = Paths.get(server.getPath());

                    if (Files.exists(pathToServer)) {
                        Desktop.getDesktop().open(pathToServer.toFile());
                    } else {
                        Alert alertUrl = new Alert(Alert.AlertType.ERROR, "The directory of the server does not exist!");
                        ((Stage) alertUrl.getDialogPane().getScene().getWindow()).getIcons()
                                .add(new Image(getClass().getClassLoader().getResourceAsStream("images/main_icon.png")));
                        alertUrl.setTitle("Error in Path");
                        alertUrl.getDialogPane().getStylesheets().add("css/main_dark.css");
                        alertUrl.showAndWait();
                    }
                } catch (InvalidPathException e) {
                    Alert alertUrl = new Alert(Alert.AlertType.ERROR, "The configured path of the server is invalid");
                    ((Stage) alertUrl.getDialogPane().getScene().getWindow()).getIcons()
                            .add(new Image(getClass().getClassLoader().getResourceAsStream("images/main_icon.png")));
                    alertUrl.setTitle("Error in Path");
                    alertUrl.getDialogPane().getStylesheets().add("css/main_dark.css");
                    alertUrl.showAndWait();
                } catch (IOException e) {
                    Alert alertUrl = new Alert(Alert.AlertType.ERROR, "The configured path of the server could not be opened");
                    ((Stage) alertUrl.getDialogPane().getScene().getWindow()).getIcons()
                            .add(new Image(getClass().getClassLoader().getResourceAsStream("images/main_icon.png")));
                    alertUrl.setTitle("Error in Path");
                    alertUrl.getDialogPane().getStylesheets().add("css/main_dark.css");
                    alertUrl.showAndWait();
                }
            } else {
                throw new NotImplementedException();
            }
        }
    }

    private Optional<Server> getSelectedServer() {
        Server selectedServer = null;

        TableView.TableViewSelectionModel<Server> selectionModel = serverTV.getSelectionModel();
        int focusedIndex = selectionModel.getFocusedIndex();
        if (focusedIndex >= 0) {
            String serverName = serverTV.getItems().get(focusedIndex).getName();


            for (Server server : servers) {
                if (serverName.equals(server.getName())) {
                    selectedServer = server;
                    break;
                }
            }
        }
        return Optional.ofNullable(selectedServer);
    }

    @FXML
    void onConfigure(ActionEvent event) throws IOException {
        Optional<Server> selectedServer = getSelectedServer();

        if (selectedServer.isPresent()) {
            if (selectedServer.get().isLocal()) {
                FXMLLoader loader = new FXMLLoader();
                loader.setLocation(getClass().getClassLoader().getResource("fxml/konfigurieren.fxml"));
                VBox root = loader.load();

                ConfigureController konfigurierenController = loader.getController();
                konfigurierenController.setMainController(this);
                konfigurierenController.setServerInConfig(selectedServer.get());

                Stage stage = showNewWindow(root, "Konfigurationsdatei ausw\u00E4hlen");
                stage.show();
            } else {
                throw new NotImplementedException();
            }
        }
    }

    private void startLocalServer(Server selectedServer) {
        String pfadToServer = selectedServer.getPath();
        ServerTyp typ = selectedServer.getTyp();
        switch (typ) {
            case TOMCAT:
                Path pathToSkript;
                if (isWindows()) {
                    pathToSkript = Paths.get(pfadToServer, "/bin/startup.bat");
                } else {
                    pathToSkript = Paths.get(pfadToServer, "/bin/startup.sh");
                }

                executeSkriptIfExists(pfadToServer, pathToSkript);
                break;
            case WILDFLY:
                break;
        }
    }

    private void stopLocalServer(Server selectedServer) {
        String pfadToServer = selectedServer.getPath();
        ServerTyp typ = selectedServer.getTyp();
        switch (typ) {
            case TOMCAT:
                Path pathToSkript;
                if (isWindows()) {
                    pathToSkript = Paths.get(pfadToServer, "/bin/shutdown.bat");
                } else {
                    pathToSkript = Paths.get(pfadToServer, "/bin/shutdown.sh");
                }

                executeSkriptIfExists(pfadToServer, pathToSkript);
                break;
            case WILDFLY:
                break;
        }
    }

    private void executeSkriptIfExists(String pfadToServer, Path pathToCommand) {
        if (Files.exists(pathToCommand)) {
            ProcessBuilder processBuilder = new ProcessBuilder();

            Map<String, String> environment = processBuilder.environment();
            environment.put(CATALINA_HOME, pfadToServer);

            if (isWindows()) {
                processBuilder.command("cmd", "/c", "start", "\"\"", pathToCommand.toString(), "&", "exit");
            } else {
                processBuilder.command(pathToCommand.toString());
            }
            try {
                processBuilder.start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void startRemoteServer(Server selectedServer) throws JSchException, IOException {

        Session session = sessionForServer.get(selectedServer);
        if (session == null || !session.isConnected()) {
            JSch jSch = new JSch();

            Optional<Pair<String, String>> oUsernameAndPassword = getUsernameAndPassword(selectedServer);

            if (oUsernameAndPassword.isPresent()) {
                Pair<String, String> usernameAndPassword = oUsernameAndPassword.get();

                session = jSch.getSession(usernameAndPassword.getKey(), selectedServer.getPath(), 22);
                session.setPassword(usernameAndPassword.getValue());
                session.setUserInfo(new FxUserInfo());
                session.connect();
                sessionForServer.put(selectedServer, session);
            }
        }

        if (session != null) {
            ChannelExec channel = (ChannelExec) session.openChannel("exec");

            ServerTyp typ = selectedServer.getTyp();
            OperationSystem os = selectedServer.getOperationSystem();
            String command = "";

            switch (typ) {
                case TOMCAT:
                    switch (os) {
                        case WIN:
                            throw new NotImplementedException();
                            //                            command = selectedServer.getRemotePfad() + "/bin/shutdown.bat";
                            //                            break;
                        case UNIX:
                            command = selectedServer.getRemotePath() + "/bin/startup.sh";
                            break;
                    }
                    break;
                case WILDFLY:
                    break;
            }

            channel.setCommand(command);
            channel.setInputStream(null);
            channel.setErrStream(System.err);


            runExec(channel);
        }
    }

    private void stopRemoteServer(Server selectedServer) throws JSchException, IOException {
        Session session = sessionForServer.get(selectedServer);

        if (session == null || !session.isConnected()) {
            JSch jSch = new JSch();
            Optional<Pair<String, String>> oUsernameAndPassword = getUsernameAndPassword(selectedServer);

            if (oUsernameAndPassword.isPresent()) {
                Pair<String, String> usernameAndPassword = oUsernameAndPassword.get();

                session = jSch.getSession(usernameAndPassword.getKey(), selectedServer.getPath(), 22);
                session.setPassword(usernameAndPassword.getValue());
                FxUserInfo userinfo = new FxUserInfo();
                session.setUserInfo(userinfo);
                session.connect();
            }
        }

        if (session != null) {
            ChannelExec channel = (ChannelExec) session.openChannel("exec");

            ServerTyp typ = selectedServer.getTyp();
            OperationSystem os = selectedServer.getOperationSystem();
            String command = "";

            switch (typ) {
                case TOMCAT:
                    switch (os) {
                        case WIN:
                            throw new NotImplementedException();
                            //                            command = selectedServer.getRemotePfad() + "/bin/shutdown.bat";
                            //                            break;
                        case UNIX:
                            command = selectedServer.getRemotePath() + "/bin/shutdown.sh";
                            break;
                    }
                    break;
                case WILDFLY:
                    break;
            }

            channel.setCommand(command);
            channel.setInputStream(null);
            channel.setErrStream(System.err);

            runExec(channel);
        }
    }

    private static void runExec(ChannelExec channel) throws IOException, JSchException {
        InputStream in = channel.getInputStream();
        channel.connect();

        byte[] tmp = new byte[1024];
        while (true) {
            while (in.available() > 0) {
                int i = in.read(tmp, 0, 1024);
                if (i < 0) {
                    break;
                }
                System.out.print(new String(tmp, 0, i));
            }
            if (channel.isClosed()) {
                if (in.available() > 0) {
                    continue;
                }
                System.out.println("exit-status: " + channel.getExitStatus());
                break;
            }
            try {
                Thread.sleep(500);
            } catch (Exception ee) {
                ee.printStackTrace();
            }
        }

        channel.disconnect();
    }

    public static Optional<Pair<String, String>> getUsernameAndPassword(Server selectedServer) {
        Pair<String, String> resultPair = null;

        String serverUser = selectedServer.getUser();

        String username;
        if (serverUser != null) {
            username = serverUser;
        } else {
            TextInputDialog inputDialog = new TextInputDialog();

            inputDialog.setTitle("User");
            inputDialog.setContentText("Enter a username please");
            Optional<String> oHost = inputDialog.showAndWait();
            username = oHost.orElse(null);
        }

        if (username != null) {

            String passwort = selectedServer.getPassword();

            if (passwort == null || passwort.isEmpty()) {
                Dialog<String> passwordDialog = createPasswordDialog("Enter a password please");

                Optional<String> oPasswort = passwordDialog.showAndWait();
                if (oPasswort.isPresent()) {
                    selectedServer.setPassword(oPasswort.get());
                    resultPair = new Pair<>(username, oPasswort.get());
                }
            } else {
                resultPair = new Pair<>(username, passwort);
            }
        }

        return Optional.ofNullable(resultPair);
    }

    private static Dialog<String> createPasswordDialog(String message) {
        Dialog<String> passwordDialog = new Dialog<>();
        ((Stage) passwordDialog.getDialogPane().getScene().getWindow()).getIcons()
                .add(new Image(MainApplication.class.getClassLoader().getResourceAsStream("images/main_icon.png")));
        passwordDialog.setTitle("Password");
        passwordDialog.setHeaderText(message);
        passwordDialog.getDialogPane().getStylesheets().add("css/main_dark.css");

        ButtonType enterButtonTyp = new ButtonType("Okay", ButtonBar.ButtonData.OK_DONE);
        passwordDialog.getDialogPane().getButtonTypes().addAll(enterButtonTyp);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        PasswordField password = new PasswordField();
        password.setPromptText("Password");

        grid.add(new Label("Password:"), 0, 0);
        grid.add(password, 1, 0);

        passwordDialog.getDialogPane().setContent(grid);

        Platform.runLater(password::requestFocus);

        passwordDialog.setResultConverter(dialogButton -> {
            if (dialogButton == enterButtonTyp) {
                return password.getText();
            }
            return null;
        });
        return passwordDialog;
    }

    public static List<Server> getServers() {
        return servers;
    }

    private Stage showNewWindow(VBox root, String title) {
        Scene sceneToShow = new Scene(root);
        if (darktheme) {
            sceneToShow.getStylesheets().add("css/main_dark.css");
        }
        Stage stage = new Stage();
        stage.setTitle(title);
        stage.setScene(sceneToShow);
        stage.getIcons().add(new Image(getClass().getClassLoader().getResourceAsStream("images/main_icon.png")));

        return stage;
    }

    private boolean isWindows() {
        return System.getProperty("os.name").toLowerCase().contains("win");
    }

    static class FxUserInfo implements UserInfo, UIKeyboardInteractive {

        String password;

        @Override
        public String[] promptKeyboardInteractive(String destination,
                                                  String name,
                                                  String instruction,
                                                  String[] prompt,
                                                  boolean[] echo) {
            Dialog<ButtonType> eingabeDialog = new Dialog<>();
            ((Stage) eingabeDialog.getDialogPane().getScene().getWindow()).getIcons()
                    .add(new Image(getClass().getClassLoader().getResourceAsStream("images/main_icon.png")));
            eingabeDialog.getDialogPane().getStylesheets().add("css/main_dark.css");
            eingabeDialog.setTitle("Input");

            ButtonType okType = new ButtonType("Ok", ButtonBar.ButtonData.OK_DONE);
            ButtonType cancelType = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
            eingabeDialog.getDialogPane().getButtonTypes().addAll(okType, cancelType);

            eingabeDialog.setHeaderText(destination + ": " + name);
            eingabeDialog.setContentText(instruction);

            GridPane gridPane = new GridPane();

            TextField[] texts = new TextField[prompt.length];

            for (int i = 0; i < prompt.length; i++) {
                Label textLabel = new Label(prompt[i]);

                if (echo[i]) {
                    texts[i] = new TextField();
                } else {
                    texts[i] = new PasswordField();
                }

                gridPane.addRow(i, textLabel, texts[i]);
            }

            eingabeDialog.getDialogPane().setContent(gridPane);

            Optional<ButtonType> buttonType = eingabeDialog.showAndWait();

            if (buttonType.isPresent() && ButtonBar.ButtonData.OK_DONE == buttonType.get().getButtonData()) {
                String[] response = new String[prompt.length];
                for (int i = 0; i < prompt.length; i++) {
                    response[i] = texts[i].getText();
                }
                return response;
            } else {
                return null;  // cancel
            }
        }

        @Override
        public String getPassphrase() {
            return null;
        }

        @Override
        public String getPassword() {
            return password;
        }

        @Override
        public boolean promptPassword(String message) {
            Dialog<String> passwordDialog = createPasswordDialog(message);
            Optional<String> oPassword = passwordDialog.showAndWait();

            boolean result = false;
            if (oPassword.isPresent()) {
                password = oPassword.get();
                result = true;
            }

            return result;
        }

        @Override
        public boolean promptPassphrase(String s) {
            return true;
        }

        @Override
        public boolean promptYesNo(String str) {
            //            boolean result = false;
            //
            //            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            //            alert.setTitle("Best\u00E4tigung");
            //            alert.setHeaderText("Achtung");
            //            alert.setContentText(str);
            //            alert.getDialogPane().getStylesheets().add("css/main_dark.css");
            //
            //            Optional<ButtonType> buttonType = alert.showAndWait();
            //
            //            if (buttonType.isPresent() && ButtonBar.ButtonData.OK_DONE == buttonType.get().getButtonData()) {
            //                result = true;
            //            }

            return true;
        }

        @Override
        public void showMessage(String message) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            ((Stage) alert.getDialogPane().getScene().getWindow()).getIcons()
                    .add(new Image(getClass().getClassLoader().getResourceAsStream("images/main_icon.png")));
            alert.setTitle("Information");
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.getDialogPane().getStylesheets().add("css/main_dark.css");

            alert.showAndWait();
        }

    }

    //For custom titlebar
//    @FXML
//    void onClose(ActionEvent event) {
//        Stage stage = (Stage) ((Button) event.getSource()).getScene().getWindow();
//        stage.close();
//    }
//
//    @FXML
//    void onMaximize(ActionEvent event) {
//        Stage stage = (Stage) ((Button) event.getSource()).getScene().getWindow();
//        stage.setMaximized(!stage.isMaximized());
//
//        if (stage.isMaximized()) {
//            ImageView nonMaximizeIcon = new ImageView(
//                    new Image(getClass().getClassLoader().getResource("images/nonMaximize.png").toString()));
//            nonMaximizeIcon.setFitHeight(30);
//            nonMaximizeIcon.setFitWidth(45);
//            maximizeBtn.setGraphic(nonMaximizeIcon);
//        } else {
//            ImageView maximizeIcon = new ImageView(new Image(getClass().getClassLoader().getResource("images/maximize.png").toString()));
//            maximizeIcon.setFitHeight(30);
//            maximizeIcon.setFitWidth(45);
//            maximizeBtn.setGraphic(maximizeIcon);
//        }
//    }
//
//    @FXML
//    void onMinimize(ActionEvent event) {
//        Stage stage = (Stage) ((Button) event.getSource()).getScene().getWindow();
//        stage.setIconified(!stage.isIconified());
//    }
//
//    final Delta dragDelta = new Delta();
//
//    @FXML
//    void onMousePressed(MouseEvent mouseEvent) {
//        Stage stage = (Stage) createBtn.getScene().getWindow();
//
//        dragDelta.x = stage.getX() - mouseEvent.getScreenX();
//        dragDelta.y = stage.getY() - mouseEvent.getScreenY();
//    }
//
//    @FXML
//    void onMouseDragged(MouseEvent mouseEvent) {
//        Stage stage = (Stage) createBtn.getScene().getWindow();
//
//        stage.setX(mouseEvent.getScreenX() + dragDelta.x);
//        stage.setY(mouseEvent.getScreenY() + dragDelta.y);
//    }
//
//    // records relative x and y co-ordinates.
//    class Delta {
//        double x, y;
//    }

    static class JSCHLogger implements com.jcraft.jsch.Logger {

        @Override
        public boolean isEnabled(int pLevel) {
            return true; // here, all levels enabled
        }

        @Override
        public void log(int pLevel, String pMessage) {
            String level = "";

            switch (pLevel) {
                case DEBUG:
                    level = "DEBUG";
                    break;
                case INFO:
                    level = "INFO";
                    break;
                case WARN:
                    level = "WARN";
                    break;
                case ERROR:
                    level = "ERROR";
                    break;
                case FATAL:
                    level = "FATAL";
                    break;
            }

            System.out.println("pLevel = [" + level + "], pMessage = [" + pMessage + "]");
        }

    }

    public static Map<Server, Session> getSessionForServer() {
        return sessionForServer;
    }
}
