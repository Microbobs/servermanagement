package de.microbob.controller;

import de.microbob.model.ConfigurationFile;
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

public class FileEditController {

    @FXML
    private TextArea editTA;

    private ConfigurationFile fileToEdit;

    @FXML
    void initialize() {
    }

    @FXML
    void onSpeichern(ActionEvent event) throws IOException {
        Files.deleteIfExists(Paths.get(fileToEdit.getAbsoultePath()));

        File absoultePath = new File(fileToEdit.getAbsoultePath());
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(absoultePath, false))) {
            String newContent = editTA.getText();
            writer.write(newContent);
        }

        ((Stage) editTA.getScene().getWindow()).close();
    }

    public ConfigurationFile getFileToEdit() {
        return fileToEdit;
    }

    public void setFileToEdit(ConfigurationFile fileToEdit) {
        this.fileToEdit = fileToEdit;

        StringBuilder fileContent = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(fileToEdit.getAbsoultePath()))) {
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
