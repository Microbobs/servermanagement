package de.microbob.controller;

import de.microbob.MainApplication;
import de.microbob.constant.ServerTyp;
import de.microbob.model.ConfigurationFile;
import de.microbob.model.Server;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ConfigureController {

    @FXML
    private Button chooseBtn;

    @FXML
    private ListView<String> configurationLV;

    private Server serverInConfig;
    private List<ConfigurationFile> configurationFiles;

    private MainApplication mainApplication;

    @FXML
    void initialize() {

    }

    @FXML
    void onChoose(ActionEvent event) throws IOException {
        ObservableList<String> selectedItems = configurationLV.getSelectionModel().getSelectedItems();

        if (selectedItems != null && selectedItems.size() == 1) {
            String selectedFilename = selectedItems.get(0);

            List<ConfigurationFile> selectedKonfigurationsdateien = configurationFiles.stream()
                    .filter(k -> selectedFilename.equals(k.getFilename()))
                    .collect(Collectors.toList());

            if (selectedKonfigurationsdateien.size() == 1) {
                ConfigurationFile configurationFile = selectedKonfigurationsdateien.get(0);

                mainApplication.getHostServices().showDocument(configurationFile.getAbsoultePath());
            }
        } else {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Bitte w\u00E4hlen Sie genau eine Datei aus.");
            ((Stage) alert.getDialogPane().getScene().getWindow()).getIcons()
                    .add(new Image(getClass().getClassLoader().getResourceAsStream("images/main_icon.png")));
            alert.setTitle("Fehler - Konfigurationsdatei ausw\u00E4hlen");
            alert.getDialogPane().getStylesheets().add("css/main_dark.css");
            alert.showAndWait();
        }

    }

    public void setServerInConfig(Server serverInConfig) {
        this.serverInConfig = serverInConfig;

        Path serverPath = Paths.get(serverInConfig.getPath());
        ServerTyp typ = serverInConfig.getTyp();

        if (Files.exists(serverPath)) {
            List<Path> directoriesToSearch = new ArrayList<>();
            switch (typ) {
                case TOMCAT:
                    directoriesToSearch.add(Paths.get(serverPath.toString(), "shared"));
                    directoriesToSearch.add(Paths.get(serverPath.toString(), "conf"));
                    break;
                case WILDFLY:
                    directoriesToSearch.add(Paths.get(serverPath.toString(), "standalone/configuration"));
                    break;
            }

            configurationFiles = new ArrayList<>();

            try {
                for (Path toSearch : directoriesToSearch) {
                    Files.walk(toSearch)
                            .peek(System.out::println)
                            .filter(p -> {
                                String pathString = p.toString();
                                return pathString.endsWith(".properties")
                                        || pathString.endsWith(".conf")
                                        || pathString.endsWith(".xml");
                            })
                            .peek(System.out::println)
                            .forEach(p -> configurationFiles
                                    .add(new ConfigurationFile(p.getFileName().toString(), p.toAbsolutePath().toString())));

                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            ObservableList<String> listViewElements = FXCollections.observableList(new ArrayList<>());
            configurationFiles.forEach(k -> listViewElements.add(k.getFilename()));

            configurationLV.setItems(listViewElements);
        }
    }

    public void setMainController(MainApplication mainApplication) {
        this.mainApplication = mainApplication;
    }
}
