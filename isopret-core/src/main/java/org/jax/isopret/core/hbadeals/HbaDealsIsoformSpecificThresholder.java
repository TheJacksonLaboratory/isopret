package org.jax.isopret.core.hbadeals;

import org.jax.isopret.core.transcript.AccessionNumber;
import org.monarchinitiative.phenol.analysis.AssociationContainer;
import org.monarchinitiative.phenol.analysis.DirectAndIndirectTermAnnotations;
import org.monarchinitiative.phenol.analysis.StudySet;
import org.monarchinitiative.phenol.ontology.data.TermId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

/**
 * THis class is used to perform Bayesian false discovery rate control.
 * The HBA-DEALS probability resulted for each gene expression assessment (or splicing) is
 * a Posterior Error Probability (PEP). By the linearity of expected value we
 * can add these probabilities at any threshold in order to get the total
 * expected number of false discoveries. If we thus rank the observations by PEP (from smallest to largest)
 * and choose the rank just before the rank where the cumulative mean of the FDR (called qvalue after John Storey),
 * this is where we set the threshold.
 */
public class HbaDealsIsoformSpecificThresholder {
    private static final Logger LOGGER = LoggerFactory.getLogger(HbaDealsIsoformSpecificThresholder.class);

    private static final double DEFAULT_THRESHOLD = 0.05;

    /** Probability threshold for expression results that attains fdrThreshold FDR for expression. */
    private final double expressionThreshold;
    /** Probability threshold for splicing results that attains fdrThreshold FDR for splicing. */
    private final double splicingThreshold;

    private final StudySet dgeStudy;
    private final StudySet dgePopulation;
    private final StudySet dasStudy;
    private final StudySet dasPopulation;

    private final Map<String, HbaDealsResult> rawResults;

    private final double fdrThreshold;


    public Map<String, HbaDealsResult> getRawResults() {
        return rawResults;
    }

    public int getTotalGeneCount() {
        return this.rawResults.size();
    }

    public double getFdrThreshold() {
        return fdrThreshold;
    }

    /**
     * Find the FDR thresholds for splicing and expression
     * @param results Map of HBA-DEALS analysis results (key: gene symbol)
     */
    public HbaDealsIsoformSpecificThresholder(Map<String, HbaDealsResult> results,
                                              double fdrThreshold,
                                              AssociationContainer<TermId> geneContainer,
                                              AssociationContainer<TermId> transcriptContainer) {

        this.rawResults = results;
        this.fdrThreshold = fdrThreshold;
        List<Double> expressionProbs = results
                .values()
                .stream()
                .map(HbaDealsResult::getExpressionP)
                .collect(Collectors.toList());
        ProbThreshold probThresholdExpression = new ProbThreshold(expressionProbs, fdrThreshold);
        this.expressionThreshold = probThresholdExpression.getQvalueThreshold();
        List<Double> splicingProbs = results
                .values()
                .stream()
                .map(HbaDealsResult::getSplicingPlist)
                .flatMap(List::stream)
                .collect(Collectors.toList());
        ProbThreshold probThresholdSplicing = new ProbThreshold(splicingProbs, fdrThreshold);
        this.splicingThreshold = probThresholdSplicing.getQvalueThreshold();
        Set<TermId> dgeSignificant = results
                .values()
                .stream()
                .filter(r -> r.getExpressionP() <= this.expressionThreshold)
                .map(HbaDealsResult::getGeneAccession)
                .map(AccessionNumber::toTermId)
                .collect(Collectors.toSet());
        Set<TermId> dgePopulation = results
                .values()
                .stream()
                .map(HbaDealsResult::getGeneAccession)
                .map(AccessionNumber::toTermId)
                .collect(Collectors.toSet());
        Map<TermId, DirectAndIndirectTermAnnotations> assocMap
                = geneContainer.getAssociationMap(dgeSignificant);
        this.dgeStudy = new StudySet("DGE Study", assocMap);
        assocMap = geneContainer.getAssociationMap(dgePopulation);
        this.dgePopulation = new StudySet("DGE Population", assocMap);

        Set<TermId> dasIsoformStudy = results
                .values()
                .stream()
                .flatMap(r -> r.getTranscriptResults().stream())
                .filter(tr -> tr.getP() <= splicingThreshold)
                .map(HbaDealsTranscriptResult::getTranscriptId)
                .map(AccessionNumber::toTermId)
                .collect(Collectors.toSet());
        Set<TermId> dasIsoformPopulation = results
                .values()
                .stream()
                .flatMap(r -> r.getTranscriptResults().stream())
                .map(HbaDealsTranscriptResult::getTranscriptId)
                .map(AccessionNumber::toTermId)
                .collect(Collectors.toSet());
        assocMap = transcriptContainer.getAssociationMap(dasIsoformStudy);
        this.dasStudy = new StudySet("DAS Study", assocMap);
        assocMap = geneContainer.getAssociationMap(dasIsoformPopulation);
        this.dasPopulation = new StudySet("DAS Population", assocMap);
    }


    public StudySet getDgeStudy() {
        return this.dgeStudy;
    }

    public int getDgeGeneCount() {
        return this.dgeStudy.getAnnotatedItemCount();
    }

    public StudySet getDgePopulation() {
        return this.dgePopulation;
    }



    public StudySet getDasStudy() {
        return this.dasStudy;
    }

    public int getDasGeneCount() {
        return this.dasStudy.getAnnotatedItemCount();
    }

    public StudySet getDasPopulation() {
        return this.dasPopulation;
    }


    public double getExpressionThreshold() {
        return expressionThreshold;
    }

    public double getSplicingThreshold() {
        return splicingThreshold;
    }


}
