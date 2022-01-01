package org.jax.isopret.core.visualization;

import org.jax.isopret.core.hbadeals.HbaDealsResult;
import org.jax.isopret.core.hbadeals.HbaDealsTranscriptResult;
import org.jax.isopret.core.interpro.DisplayInterproAnnotation;
import org.jax.isopret.core.interpro.InterproEntry;
import org.jax.isopret.core.transcript.AccessionNumber;
import org.jax.isopret.core.transcript.AnnotatedGene;
import org.jax.isopret.core.transcript.Transcript;
import org.monarchinitiative.svart.Contig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.util.*;
import java.util.stream.Collectors;

/**
 * This class acts as an interface between the HtmlVisualizer and the analysis results and
 * should be used if the input data represent Ensembl ids.
 *
 * @author Peter N Robinson
 */
public class EnsemblVisualizable implements Visualizable {
    private static final Logger LOGGER = LoggerFactory.getLogger(EnsemblVisualizable.class);

    /**
     * Number of all annotated transcripts of some gene
     */
    private final int totalTranscriptCount;

    /**
     * All annotated transcripts of some gene that were expressed according to HBA deals
     */
    private final List<Transcript> expressedTranscripts;

    /**
     * All annotated transcripts of a gene; just those transcripts that were expressed according to HBA deals
     */
    private final List<IsoformVisualizable> isoformVisualizables;

    private final HbaDealsResult hbaDealsResult;

    private final String chromosome;

    private final AnnotatedGene agene;

    private final boolean differentiallyExpressed;

    private final boolean differentiallySpliced;

    private final int significantIsoforms;

    private final String isoformSvg;

    private final int isoformSvgHeight;

    private final String proteinSvg;

    private final int proteinSvgHeight;


    private final double splicingThreshold;

    private final GoAnnotationMatrix annotationMatrix;

    private final int i;

    @Deprecated
    public EnsemblVisualizable(AnnotatedGene agene){
        this(agene, null);
    }

    public EnsemblVisualizable(AnnotatedGene agene, GoAnnotationMatrix goAnnotationMatrix) {
        this.agene = agene;
        this.annotationMatrix = goAnnotationMatrix;
        this.totalTranscriptCount = agene.getTranscripts().size();
        this.expressedTranscripts = agene.getExpressedTranscripts();
        this.hbaDealsResult = agene.getHbaDealsResult();
        String chr = this.expressedTranscripts.stream().map(Transcript::contig).map(Contig::name).findAny().orElse("n/a");
        this.chromosome = chr.startsWith("chr") ? chr : "chr" + chr;
        this.differentiallyExpressed = agene.passesExpressionThreshold();
        this.differentiallySpliced = agene.passesSplicingThreshold();
        this.splicingThreshold = agene.getSplicingThreshold();
        this.i = 0;
        this.significantIsoforms = (int) hbaDealsResult.getTranscriptMap().values().stream().filter(HbaDealsTranscriptResult::isSignificant).count();
        this.isoformVisualizables = agene.getHbaDealsResult().getTranscriptMap().values().
                stream().
                map(EnsemblIsoformVisualizable::new)
                .collect(Collectors.toList());
        AbstractSvgGenerator svggen = TranscriptSvgGenerator.factory(agene);
        this.isoformSvg = svggen.getSvg();
        this.isoformSvgHeight = svggen.getSvgHeight();
        svggen = ProteinSvgGenerator.factory(agene);

        if (!agene.hasInterproAnnotations()) {
            this.proteinSvg = ProteinSvgGenerator.empty(agene.getHbaDealsResult().getSymbol());
            this.proteinSvgHeight = 50;
        } else {
            AbstractSvgGenerator psvggen = ProteinSvgGenerator.factory(agene);
            this.proteinSvg = psvggen.getSvg();
            this.proteinSvgHeight = svggen.getSvgHeight();
        }

    }

    public int getI() {
        return i;
    }

    @Override
    public String getGeneSymbol() {
        return hbaDealsResult.getSymbol();
    }

    @Override
    public String getGeneAccession() {
        return this.hbaDealsResult.getGeneAccession().getAccessionString();
    }

    private String getEnsemblUrl(String accession) {
        return String.format("https://ensembl.org/Homo_sapiens/Gene/Summary?db=core;g=%s", accession);
    }

    private String getEnsemblTranscriptUrl(String accession) {
        //https://useast.ensembl.org/Homo_sapiens/Transcript/Summary?db=core;g=ENSG00000181026;r=15:88626612-88632281;t=ENST00000557927
        return String.format("https://ensembl.org/Homo_sapiens/Gene/Summary?db=core;t=%s", accession);
    }

    private String getHtmlAnchor(String accession) {
        String url = getEnsemblUrl(accession);
        return String.format("<a href=\"%s\" target=\"__blank\">%s</a>\n", url, accession);
    }

    @Override
    public int getTotalTranscriptCount() {
        return this.totalTranscriptCount;
    }

    @Override
    public int getExpressedTranscriptCount() {
        return this.expressedTranscripts.size();
    }

    @Override
    public String getGeneUrl() {
        return getEnsemblUrl(getGeneAccession());
    }

    @Override
    public String getChromosome() {
        return this.chromosome;
    }

    @Override
    public double getExpressionPval() {
        return this.hbaDealsResult.getExpressionP();
    }

    @Override
    public double getMostSignificantSplicingPval() {
        return this.hbaDealsResult.getSmallestSplicingP();
    }

    @Override
    public double getExpressionFoldChange() {
        double logFc = getExpressionLogFoldChange();
        return Math.exp(logFc);
    }

    @Override
    public double getExpressionLogFoldChange() {
        return this.hbaDealsResult.getExpressionFoldChange();
    }

    @Override
    public String getIsoformSvg() {
        return this.isoformSvg;
    }

    @Override
    public String getProteinSvg() {
        return this.proteinSvg;
    }


    /**
     * @return a matrix of data to display the isoforms
     */
    @Override
    public List<IsoformVisualizable> getIsoformVisualizable() {
        return this.isoformVisualizables;
    }

    /**
     * @return a list of interpro annotations that cover the isoforms of the gene that are expressed in our data
     */
    @Override
    public List<DisplayInterproAnnotation> getInterproForExpressedTranscripts() {
        Map<AccessionNumber, List<DisplayInterproAnnotation>> interproMap = this.agene.getTranscriptToInterproHitMap();
        Set<DisplayInterproAnnotation> interpro = new HashSet<>();
        for (AccessionNumber transcriptId : this.hbaDealsResult.getTranscriptMap().keySet()) {
            if (interproMap.containsKey(transcriptId)) {
                interpro.addAll(interproMap.get(transcriptId));
            }
        }
        List<DisplayInterproAnnotation> sortedList = new ArrayList<>(interpro);
        Collections.sort(sortedList);
        return sortedList;
    }

    /**
     * Note that this gives us a list of nterpro entries that are used to annotate the isoforms
     * The DisplayInterproAnnotation provides more detail about the locations of individual
     * annotations and is used to generate the protein SVG
     *
     * @return Object to visualize Interpro entries
     */
    @Override
    public List<InterproVisualizable> getInterproVisualizable() {
        Map<AccessionNumber, List<DisplayInterproAnnotation>> interproMap = this.agene.getTranscriptToInterproHitMap();
        Set<InterproEntry> interpro = new HashSet<>();
        for (AccessionNumber transcriptId : this.hbaDealsResult.getTranscriptMap().keySet()) {
            if (interproMap.containsKey(transcriptId)) {
                List<DisplayInterproAnnotation> diaList = interproMap.get(transcriptId);
                for (var dia : diaList) {
                    interpro.add(dia.getInterproEntry());
                }
            }
        }
        return interpro.stream().map(InterproVisualizable::new).sorted().collect(Collectors.toList());
    }

    @Override
    public GoAnnotationMatrix getGoAnnotationMatrix() {
        return this.annotationMatrix;
    }

    @Override
    public boolean isDifferentiallyExpressed() {
        return this.differentiallyExpressed;
    }

    @Override
    public boolean isDifferentiallySpliced() {
        return this.differentiallySpliced;
    }

    @Override
    public String getNofMsplicing() {
        if (isoformVisualizables.size() == 0) {
            return "n/a";
        }
        return String.format("%d/%d", this.significantIsoforms, isoformVisualizables.size());
    }

    @Override
    public double getBestSplicingPval() {
        return hbaDealsResult.getSmallestSplicingP();
    }

    private final static String HTML_FOR_SVG_HEADER = """
            <!doctype html>
            <html class="no-js" lang="">

            <head>
              <meta charset="utf-8">
              <meta http-equiv="x-ua-compatible" content="ie=edge">
               <style>
            html, body {
               padding: 0;
               margin: 20;
               font-size:14px;
            }

            body {
               font-family:"DIN Next", Helvetica, Arial, sans-serif;
               line-height:1.25;
               background-color:white   ;
                max-width:1200px;
                margin-left:auto;
                margin-right:auto;
             }
            </style>
            <body>
            """;

    private static final String HTML_FOR_SVG_FOOTER = """
            </body>
            </html>
            """;


    @Override
    public String getIsoformHtml() {
        String html = HTML_FOR_SVG_HEADER +
                getIsoformSvg() +
                HTML_FOR_SVG_FOOTER;
        return html;
    }

    @Override
    public String getProteinHtml() {
        String html = HTML_FOR_SVG_HEADER +
                getProteinSvg() +
                HTML_FOR_SVG_FOOTER;
        return html;
    }

    @Override
    public int getIsoformSvgHeight() {
        return this.isoformSvgHeight;
    }

    @Override
    public int getProteinSvgHeight() {
        return this.proteinSvgHeight;
    }
}
