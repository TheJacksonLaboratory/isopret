package org.jax.isopret.gui.controller;


import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import org.jax.isopret.gui.configuration.IsopretDataLoadTask;
import org.jax.isopret.gui.service.IsopretService;
import org.jax.isopret.gui.widgets.PopupFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;
import java.util.ResourceBundle;

/**
 * A Java app to help design probes for Capture Hi-C
 * @author Peter Robinson
 * @version 0.0.1 (2021-11-27)
 */
@Component
public class MainController implements Initializable {
    private final static Logger LOGGER = LoggerFactory.getLogger(MainController.class.getName());
    public Tab dgeTab;
    public Tab dasTab;
    @FXML
    private ProgressBar analysisPB;
    @FXML
    private Label analysisLabel;

    @FXML
    private BorderPane rootNode;
    @FXML
    private Label downloadDataSourceLabel;
    @FXML
    private ProgressIndicator transcriptDownloadPI;
    private final ObservableList<String> goMethodList = FXCollections.observableArrayList("Term for Term",
            "Parent-Child Union", "Parent-Child Intersect");
    @FXML
    private ChoiceBox<String> goChoiceBox;
    private final ObservableList<String> mtcMethodList = FXCollections.observableArrayList("None",
            "Bonferroni", "Bonferroni-Holm","Sidak","Benjamini-Hochberg","Benjamini-Yekutieli");

    /** The tab pane with setup, analysis, gene views. etc */
    @FXML
    TabPane tabPane;
    /** The 'first' tab of IsopretFX for setting things up.  */
    @FXML
    private Tab setupTab;
    /** The 'second' tab of IsopretFX that shows a summary of the analysis and a list of Viewpoints.  */
    @FXML
    private Tab analysisTab;

    @FXML
    private ChoiceBox<String> mtcChoiceBox;



    @Autowired
    private IsopretService service;

    @Autowired
    private AnalysisController analysisController;

    @Autowired
    private Properties pgProperties;

    @Autowired
    ResourceLoader resourceLoader;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        Bindings.bindBidirectional(this.downloadDataSourceLabel.textProperty(), service.downloadDirProperty());
        this.transcriptDownloadPI.progressProperty().bind(service.downloadCompletenessProperty());
        goChoiceBox.setItems(goMethodList);
        goChoiceBox.getSelectionModel().selectFirst();
        goChoiceBox.valueProperty().addListener((observable, oldValue, newValue) -> service.setGoMethod(newValue));
        mtcChoiceBox.setItems(mtcMethodList);
        mtcChoiceBox.getSelectionModel().selectFirst();
        mtcChoiceBox.valueProperty().addListener((observable, oldValue, newValue) -> service.setMtcMethod(newValue));
        analysisPB.setProgress(0.0);
    }


    /**
     * Show the user a file chooser to select an output (results) file from HBA-DEALS.
     */
    @FXML
    private void chooseHbaDealsOutputFile(ActionEvent e) {
        e.consume();
        FileChooser chooser = new FileChooser();
        chooser.setInitialDirectory(new File(System.getProperty("user.home")));
        chooser.setTitle("Choose HBA-DEALS File");
        File file = chooser.showOpenDialog(rootNode.getScene().getWindow());
        if (file==null || file.getAbsolutePath().equals("")) {
            LOGGER.error("Could not get HBA-DEALS file");
            PopupFactory.displayError("Error","Could not get HBA-DEALS file.");
            return;
        }
        service.setHbaDealsFile(file);
    }

    @FXML
    private void downloadSources(ActionEvent e) {
        e.consume();
        if (service.sourcesDownloaded()) {
            LOGGER.info("Sources previously downloaded");
        }
        DirectoryChooser dirChooser = new DirectoryChooser();
        dirChooser.setInitialDirectory(new File(System.getProperty("user.home")));
        dirChooser.setTitle("Choose directory for downloading files required for isopret.");
        File file = dirChooser.showDialog(rootNode.getScene().getWindow());
        if (file==null || file.getAbsolutePath().equals("")) {
            LOGGER.error("Could not get directory for download");
            PopupFactory.displayError("Error","Could not get directory for download.");
            return;
        }
        service.downloadSources(file);
    }


    @FXML
    private void about(ActionEvent e) {
        String version = "TODO";
        String lastChangedDate = "TODO";
        PopupFactory.showAbout(version, lastChangedDate);
        e.consume();
    }

    /**
     * Write the settings from the current session to file and exit.
     */
    @FXML
    private void exitGui() {
        javafx.application.Platform.exit();
    }

    public void isopretAnalysis(ActionEvent actionEvent) {
        LOGGER.info("Do isopret analysis");
        Optional<File> downloadOpt = service.getDownloadDir();
        if (downloadOpt.isEmpty()) {
            PopupFactory.displayError("ERROR", "Could not find download directory");
            return;
        }
        Optional<File> hbadealsOpt = service.getHbaDealsFileOpt();
        if (hbadealsOpt.isEmpty()) {
            PopupFactory.displayError("ERROR", "HBA-DEALS file not found");
            return;
        }
        IsopretDataLoadTask task = new IsopretDataLoadTask(downloadOpt.get(), hbadealsOpt.get());
        this.analysisLabel.textProperty().bind(task.messageProperty());
        this.analysisPB.progressProperty().bind(task.progressProperty());
        task.setOnSucceeded(event -> {
            LOGGER.trace("Finished Gene Ontology analysis of HBA-DEALS results");
            this.service.setData(task); // add the results of analysis to Service
            this.analysisController.refreshListView(); // show stats.
            this.analysisController.refreshVPTable(); // uses HbaDealsGeneRow objects to populate table etc.
            SingleSelectionModel<Tab> selectionModel = tabPane.getSelectionModel();
            selectionModel.select(this.analysisTab);
            try {
                Resource r = resourceLoader.getResource(
                        "classpath:fxml/geneOntologyPane.fxml");
                if (! r.exists()) {
                    LOGGER.error("Could not initialize Gene Ontology pane (fxml file not found)");
                    return;
                }
                FXMLLoader loader = new FXMLLoader(Objects.requireNonNull(r.getURL()));
                loader.setControllerFactory(c -> new GeneOntologyController("DGE", service.getDgeGoTerms(), service.getGeneOntology()));
                ScrollPane p = loader.load();
                Tab dgeTab = new Tab("DGE");
                dgeTab.setId("DGE");
                dgeTab.setClosable(false);
                dgeTab.setContent(p);
                this.tabPane.getTabs().add(dgeTab);
                //
                GeneOntologyController gc1 = loader.getController();
                gc1.refreshGeneOntologyTable();
            } catch (IOException e) {
                e.printStackTrace();
            }
            // Now the same for DAS
            try {
                Resource r = resourceLoader.getResource(
                        "classpath:fxml/geneOntologyPane.fxml");
                if (! r.exists()) {
                    LOGGER.error("Could not initialize Gene Ontology pane (fxml file not found)");
                    return;
                }
                FXMLLoader loader = new FXMLLoader(Objects.requireNonNull(r.getURL()));
                loader.setControllerFactory(c -> new GeneOntologyController("DAS", service.getDasGoTerms(), service.getGeneOntology()));
                ScrollPane p = loader.load();
                Tab dasTab = new Tab("DAS");
                dasTab.setId("DAS");
                dasTab.setClosable(false);
                dasTab.setContent(p);
                this.tabPane.getTabs().add(dasTab);
                GeneOntologyController gc2 = loader.getController();
                gc2.refreshGeneOntologyTable();
            } catch (IOException e) {
                e.printStackTrace();
            }

        });
        task.setOnFailed(eh -> {
            Exception exc = (Exception)eh.getSource().getException();
            this.analysisLabel.setText("Failed!");
            PopupFactory.displayException("Error",
                    "Exception encountered while attempting to create digest file",
                    exc);
        });

        task.run();
    }

    public TabPane getMainTabPaneRef() {
        return this.tabPane;
    }
}
