package de.microbob.controller;

import de.microbob.model.KonfigurationsDatei;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class EditierenController {

    @FXML
    private TextArea editTA;

    private KonfigurationsDatei dateiToEdit;

    @FXML
    void initialize() {
    }

    @FXML
    void onSpeichern(ActionEvent event) throws IOException {
        Files.deleteIfExists(Paths.get(dateiToEdit.getAbsoulterPfad()));

        File absoulterPfad = new File(dateiToEdit.getAbsoulterPfad());
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(absoulterPfad, false))) {
            String newContent = editTA.getText();
            writer.write(newContent);
        }

        ((Stage) editTA.getScene().getWindow()).close();
    }

    public KonfigurationsDatei getDateiToEdit() {
        return dateiToEdit;
    }

    public void setDateiToEdit(KonfigurationsDatei dateiToEdit) {
        this.dateiToEdit = dateiToEdit;

        StringBuilder fileContent = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(dateiToEdit.getAbsoulterPfad()))) {
            reader.lines().forEach(l -> {
                fileContent.append(l);
                fileContent.append("\n");
            });

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        editTA.setText(fileContent.toString());
    }
}
