package de.microbob.controller;

import de.microbob.MainApplication;
import de.microbob.constant.ServerTyp;
import de.microbob.model.KonfigurationsDatei;
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

public class KonfigurierenController {

    @FXML
    private Button auswaehlenBtn;

    @FXML
    private ListView<String> konfigurationLV;

    private Server serverInKonf;
    private List<KonfigurationsDatei> konfigurationsDateien;

    private MainApplication mainApplication;

    @FXML
    void initialize() {

    }

    @FXML
    void onAuswaehlen(ActionEvent event) throws IOException {
        ObservableList<String> selectedItems = konfigurationLV.getSelectionModel().getSelectedItems();

        if (selectedItems != null && selectedItems.size() == 1) {
            String selectedFilename = selectedItems.get(0);

            List<KonfigurationsDatei> selectedKonfigurationsdateien = konfigurationsDateien.stream()
                    .filter(k -> selectedFilename.equals(k.getDateiname()))
                    .collect(Collectors.toList());

            if (selectedKonfigurationsdateien.size() == 1) {
                KonfigurationsDatei konfigurationsDatei = selectedKonfigurationsdateien.get(0);

                mainApplication.getHostServices().showDocument(konfigurationsDatei.getAbsoulterPfad());
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

    public void setServerInKonf(Server serverInKonf) {
        this.serverInKonf = serverInKonf;

        Path serverPath = Paths.get(serverInKonf.getPfad());
        ServerTyp typ = serverInKonf.getTyp();

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

            konfigurationsDateien = new ArrayList<>();

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
                            .forEach(p -> konfigurationsDateien
                                    .add(new KonfigurationsDatei(p.getFileName().toString(), p.toAbsolutePath().toString())));

                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            ObservableList<String> listViewElements = FXCollections.observableList(new ArrayList<>());
            konfigurationsDateien.forEach(k -> listViewElements.add(k.getDateiname()));

            konfigurationLV.setItems(listViewElements);
        }
    }

    public void setMainController(MainApplication mainApplication) {
        this.mainApplication = mainApplication;
    }
}
