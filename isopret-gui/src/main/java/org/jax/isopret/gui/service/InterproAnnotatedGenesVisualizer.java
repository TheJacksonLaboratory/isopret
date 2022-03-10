package org.jax.isopret.gui.service;

import org.jax.isopret.core.analysis.InterproOverrepResult;
import org.jax.isopret.core.go.GoTermIdPlusLabel;
import org.jax.isopret.core.hbadeals.HbaDealsTranscriptResult;
import org.jax.isopret.core.interpro.DisplayInterproAnnotation;
import org.jax.isopret.core.transcript.AccessionNumber;
import org.jax.isopret.core.transcript.AnnotatedGene;
import org.jax.isopret.core.visualization.GoAnnotationRow;
import org.jax.isopret.core.visualization.HtmlUtil;
import org.jax.isopret.core.visualization.Visualizable;
import org.monarchinitiative.phenol.ontology.data.TermId;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Display HTML file with genes and isoforms annotated to a given Interpro entry.
 * We show all genes with one or more isoforms that (1) are annotated to the interpro entry in question and (2) are
 * significantly differentially spliced.
 * @author Peter Robinson
 */
public class InterproAnnotatedGenesVisualizer extends AnnotatedGenesVisualizer {

    private final InterproOverrepResult interproResult;
    /** Genes we will include in the HTML display. */
    private final  List<AnnotatedGene> includedGenes;
    /** GO annotations for differential isoforms. */
    private final Map<GoTermIdPlusLabel, Integer> countsMap;

    private final List<Visualizable> visualizables;

    public InterproAnnotatedGenesVisualizer(InterproOverrepResult interpro, IsopretService isopretService) {
        super(interpro, isopretService);
        this.interproResult = interpro;
        String targetInterproAccession = this.interproResult.interproAccession();
        double splicingPepThreshold = isopretService.getSplicingPepThreshold();
        List<AnnotatedGene> annotatedGeneList = isopretService.getAnnotatedGeneList();
        this.includedGenes = new ArrayList<>();
        Set<TermId> transcriptAccessions = new HashSet<>();
        for (AnnotatedGene gene : annotatedGeneList) {
            Map<AccessionNumber, List<DisplayInterproAnnotation>> transcriptMap = gene.getTranscriptToInterproHitMap();
            boolean includeThisGene = false;
            for (HbaDealsTranscriptResult tresult: gene.getHbaDealsResult().getTranscriptResults()) {
                if (tresult.isSignificant(splicingPepThreshold)) {
                    AccessionNumber acc = tresult.getTranscriptId();
                    if (transcriptMap.get(acc).stream()
                            .anyMatch(d -> d.getInterproEntry().getIntroproAccession().equals(targetInterproAccession))){
                        includeThisGene = true;
                        transcriptAccessions.add(acc.toTermId());
                    }
                }
            }
            if (includeThisGene) {
                includedGenes.add(gene);
            }
        }
        // when we get here, we want to display only the genes in "incudedGenes".
        // let's get a Table with their GO annotations.
        Set<TermId> geneIds = new HashSet<>();
        this.countsMap = isopretService.getGoAnnotationsForTranscript(transcriptAccessions);
        Set<String> includedSymbols = includedGenes
                .stream()
                .map(AnnotatedGene::getSymbol)
                .collect(Collectors.toSet());
        this.visualizables = isopretService.getGeneVisualizables(includedSymbols);
    }


    public String export() {
        StringBuilder sb = new StringBuilder();
        sb.append(htmlHeader);
        sb.append(htmlTop());
        sb.append(getGoTable());
        sb.append(getGeneULwithLinks());
        for (var viz : annotatedGenes) {
            String html = getHtml(viz);
            sb.append(wrapInArticle(html, viz.getGeneSymbol()));
        }
        sb.append(bottom);
        return sb.toString();
    }


    private String getGoTable() {
        StringBuilder sb = new StringBuilder();
        sb.append(htmlTableHeader());
        //Map<GoTermIdPlusLabel, Integer> countsMap
        List<Map.Entry<GoTermIdPlusLabel, Integer>> list = new ArrayList<>(countsMap.entrySet());
        list.sort(Map.Entry.<GoTermIdPlusLabel, Integer>comparingByValue().reversed());
        for (var entry : list) {
            sb.append(getRow(entry.getKey(), entry.getValue()));
        }
        sb.append("</table>\n");
        return sb.toString();
    }

    private String getRow(GoTermIdPlusLabel goTermIdPlusLabel, Integer count) {
        StringBuilder sb = new StringBuilder();
        sb.append("<tr><td>").append(goTermIdPlusLabel.getLabel()).append("</td>");
        sb.append("<td>").append(goTermIdPlusLabel.getId()).append("</td>");
        sb.append("<td>").append(count).append("</td></tr>\n");
        return sb.toString();
    }

    private String htmlTableHeader() {
        StringBuilder sb = new StringBuilder();
        sb.append("<table class=\"go\">");
        sb.append("<tr>");
        sb.append("<th width=\"400px\";>GO term</th><th>id</th>\"");
        sb.append("<th>Annotated transcripts</th>");
        sb.append("</tr>");
        return sb.toString();
    }

    private final static String HTML_HEADER = """
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
             gotable.th
             {
               vertical-align: bottom;
               text-align: center;
             }
             
             gotable.th span
             {
               -ms-writing-mode: tb-rl;
               -webkit-writing-mode: vertical-rl;
               writing-mode: vertical-rl;
               transform: rotate(180deg);
               white-space: nowrap;
               padding: 5px 10px;
                margin: 0 auto;
             }
            </style>
            <body>
            """;

    private static final String HTML_FOOTER = """
            </body>
            </html>
            """;



    String header() {
        return String.format(
                """
                <!doctype html>
                <html class="no-js" lang="">
                <head>
                <meta charset="utf-8">
                <meta http-equiv="x-ua-compatible" content="ie=edge">
                <title>Isopret: Differentially spliced genes annotated to %s (%s)</title>
                <meta name="viewport" content="width=device-width, initial-scale=1, shrink-to-fit=no">
                %s
                </head>
                """,
                this.interproResult.interproDescription(), this.interproResult.interproAccession(), HtmlUtil.css);
    }

    protected static final String bottom = """
           <span id="tooltip" display="none" style="position: absolute; display: none;"></span>
           </main>
           <footer>
               <p>Isopret &copy; 2022</p>
           </footer>
            </body>
            </html>
            """;



    public String getTitle() {
        return String.format("Isopret: %d differentially spliced genes annotated to %s (%s)",
                includedGenes.size(), this.interproResult.interproDescription(), this.interproResult.interproAccession());
    }


    private String getGeneULwithLinks() {
        StringBuilder sb = new StringBuilder();
        sb.append("<p>A total of ").append(includedGenes.size()).append(" genes that are annotated to ");
        sb.append(this.interproResult.interproDescription()).append(" (").append(this.interproResult.interproAccession()).append(") were ");
        sb.append("identified as differentially spliced. ");
        sb.append("</p>");
        for (var viz : visualizables) {
            sb.append("<li><a href=\"#").append(viz.getGeneSymbol()).append("\">").append(viz.getGeneSymbol()).append("</a></li>");
        }
        sb.append("<br/><br/>");
        return sb.toString();
    }


}
