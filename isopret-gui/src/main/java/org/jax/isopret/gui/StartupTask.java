package org.jax.isopret.gui;

import javafx.concurrent.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

/**
 * PROBABLY WE DO NOT NEED THIS.
 */
@Deprecated
public final class StartupTask extends Task<Void> {

    private static final Logger LOGGER = LoggerFactory.getLogger(StartupTask.class);


    private final Properties pgProperties;

    /**
     * pgProperties is derived from the settings file that is stored in the
     * @param pgProperties
     */
    public StartupTask(Properties pgProperties) {
        this.pgProperties = pgProperties;
    }

    /**
     * Read {@link Properties} and initialize app resources in the :
     *
     * <ul>
     * <li>HPO ontology</li>
     * </ul>
     *
     * @return nothing
     */
    @Override
    protected Void call() {




        /*
        This is the place where we deserialize HPO ontology if we know path to the OBO file.
        We need to make sure to set ontology property of `optionalResources` to null if loading fails.
        This way we ensure that GUI elements dependent on ontology presence (labels, buttons) stay disabled
        and that the user will be notified about the fact that the ontology is missing.
         */

       /* String hpoJsonPath = pgProperties.getProperty(OptionalHpoResource.HP_JSON_PATH_PROPERTY);
        String hpoAnnotPath = pgProperties.getProperty(OptionalHpoaResource.HPOA_PATH_PROPERTY);
        updateProgress(0.02, 1);
        if (hpoJsonPath != null) {
            final File hpJsonFile = new File(hpoJsonPath);
            updateProgress(0.03, 1);
            if (hpJsonFile.isFile()) {
                String msg = String.format("Loading HPO from file '%s'", hpJsonFile.getAbsoluteFile());
                updateMessage(msg);
                LOGGER.info(msg);
                final Ontology ontology = OntologyLoader.loadOntology(hpJsonFile);
                updateProgress(0.25, 1);
                optionalHpoResource.setOntology(ontology);
                updateProgress(0.30, 1);
                updateMessage("HPO loaded");
                LOGGER.info("Loaded HPO ontology");
            } else {
                optionalHpoResource.setOntology(null);
            }
        } else {
            String msg = "Need to set path to hp.json file (See edit menu)";
            updateMessage(msg);
            LOGGER.info(msg);
            optionalHpoResource.setOntology(null);
        }
        if (hpoAnnotPath != null) {
            String msg = String.format("Loading phenotype.hpoa from file '%s'", hpoAnnotPath);
            updateMessage(msg);
            LOGGER.info(msg);
            final File hpoAnnotFile = new File(hpoAnnotPath);
            updateProgress(0.71, 1);
            if (optionalHpoResource.getOntology() == null) {
                LOGGER.error("Cannot load phenotype.hpoa because HP ontology not loaded");
                return null;
            }
            if (hpoAnnotFile.isFile()) {
                updateProgress(0.78, 1);
                this.optionalHpoaResource.setAnnotationResources(hpoAnnotPath, optionalHpoResource.getOntology());
                updateProgress(0.95, 1);
                LOGGER.info("Loaded annotation maps");
            } else {
                optionalHpoaResource.initializeWithEmptyMaps();
                LOGGER.error("Cannot load phenotype.hpoa File was null");
            }


        } else {
            LOGGER.error("Cannot load phenotype.hpoa File path not found");
        }
        */
        updateProgress(1, 1);
        return null;
    }
}
