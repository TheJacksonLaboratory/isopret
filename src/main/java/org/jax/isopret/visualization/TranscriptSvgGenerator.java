package org.jax.isopret.visualization;


import org.jax.isopret.except.IsopretRuntimeException;
import org.jax.isopret.hbadeals.HbaDealsResult;
import org.jax.isopret.hbadeals.HbaDealsTranscriptResult;
import org.jax.isopret.transcript.AccessionNumber;
import org.jax.isopret.transcript.AnnotatedGene;
import org.jax.isopret.transcript.Transcript;
import org.monarchinitiative.svart.*;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


/**
 * Structural variant (SV) Scalar Vector Graphic (SVG) generator.
 *
 * @author Peter N Robinson
 */
public class TranscriptSvgGenerator extends AbstractSvgGenerator {
    static final int SVG_WIDTH = 1400;

    static final int HEIGHT_FOR_SV_DISPLAY = 200;
    static final int HEIGHT_PER_DISPLAY_ITEM = 100;

    /**
     * List of transcripts that are affected by the SV and that are to be shown in the SVG.
     */
    protected final List<Transcript> affectedTranscripts;

    /**
     * Number of base pairs from left to right boundary of the display area
     */
    private final double genomicSpan;
    /**
     * Leftmost position (most 5' on chromosome).
     */
    protected final int genomicMinPos;
    /**
     * Rightmost position (most 3' on chromosome).
     */
    protected final int genomicMaxPos;
    /**
     * Equivalent to {@link #genomicMinPos} minus an offset so that the display items are not at the very edge.
     */
    protected final int paddedGenomicMinPos;
    /**
     * Equivalent to {@link #genomicMaxPos} plus an offset so that the display items are not at the very edge.
     */
    protected final int paddedGenomicMaxPos;
    /**
     * Number of base pairs from left to right boundary of the entire canvas
     */
    private final double paddedGenomicSpan;
    /**
     * Minimum position of the scale TODO shouldnt this be {@link #genomicMinPos} ???
     */
    private final double scaleMinPos;

    private final double scaleMaxPos;

    private final int scaleBasePairs;


    protected final double INTRON_MIDPOINT_ELEVATION = 10.0;
    /**
     * Height of the symbols that represent the transcripts
     */
    private final double EXON_HEIGHT = 20;
    /**
     * Y skip to put text underneath transcripts. Works with {@link #writeTranscriptName}
     */
    protected final double Y_SKIP_BENEATH_TRANSCRIPTS = 30;

    private final HbaDealsResult hbaDealsResult;

    private final boolean differentiallyExpressed;

    private final double splicingThreshold;

    private final static int FIRST_THIRD_OF_SVG = 1;
    private final static int SECOND_THIRD_OF_SVG = 2;
    private final static int THIRD_THIRD_OF_SVG = 3;

    /**
     *
     * @param annotatedGene an object with all information about a gene that is relevant for making the SVG/HTML output
     * @return list of transcripts
     */
    private List<Transcript> getAffectedTranscripts(AnnotatedGene annotatedGene) {
        List<Transcript> transcripts = annotatedGene.getTranscripts();
        HbaDealsResult result = annotatedGene.getHbaDealsResult();
        Map<AccessionNumber, HbaDealsTranscriptResult> transcriptMap = result.getTranscriptMap();
        return transcripts
                .stream()
                .filter(t -> transcriptMap.containsKey(t.accessionId()))
                .collect(Collectors.toList());
    }


    /**
     * The constructor calculates the left and right boundaries for display
     *
     * @param annotatedGene Object with transcripts and annotations
     */
    private TranscriptSvgGenerator(int height, AnnotatedGene annotatedGene) {
        super(SVG_WIDTH, height);
        this.affectedTranscripts = getAffectedTranscripts(annotatedGene);
        this.hbaDealsResult = annotatedGene.getHbaDealsResult();

        this.genomicMinPos = affectedTranscripts.stream()
                .map(t -> t.withStrand(Strand.POSITIVE))
                .mapToInt(GenomicRegion::start)
                .min()
                .orElse(0);
        this.genomicMaxPos = affectedTranscripts.stream()
                .map(t -> t.withStrand(Strand.POSITIVE))
                .mapToInt(GenomicRegion::end)
                .max()
                .orElse(this.genomicMinPos + 1000); // We should never actually need the orElse
        this.genomicSpan = this.genomicMaxPos - this.genomicMinPos;
        this.paddedGenomicMinPos = genomicMinPos - (int) (0.05 * (this.genomicSpan));
        this.paddedGenomicMaxPos = genomicMaxPos + (int) (0.23 * (this.genomicSpan));
        this.paddedGenomicSpan = this.paddedGenomicMaxPos - this.paddedGenomicMinPos;
        this.scaleBasePairs = 1 + this.genomicMaxPos - this.genomicMinPos;
        this.scaleMinPos = translateGenomicToSvg(genomicMinPos);
        this.scaleMaxPos = translateGenomicToSvg(genomicMaxPos);
        this.differentiallyExpressed = annotatedGene.passesExpressionThreshold();
        this.splicingThreshold = annotatedGene.getSplicingThreshold();
    }


    /**
     * @return SVG coordinate that corresponds to a genomic position.
     */
    protected double translateGenomicToSvg(int genomicCoordinate) {
        double pos = genomicCoordinate - paddedGenomicMinPos;
        if (pos < 0) {
            throw new IsopretRuntimeException("Bad left boundary (genomic coordinate-"); // should never happen
        }
        double prop = pos / paddedGenomicSpan;
        return prop * SVG_WIDTH;
    }


    /**
     * Write a coding exon
     *
     * @param start start position in SVG sapce of a CDS exon
     * @param end end position in SVG sapce of a CDS exon
     * @param ypos vertical position to draw exon
     * @param writer file handle
     * @throws IOException if we cannot write the exon
     */
    protected void writeCdsExon(double start, double end, int ypos, Writer writer) throws IOException {
        double width = end - start;
        double Y = ypos - 0.5 * EXON_HEIGHT;
        String rect = String.format("<rect x=\"%f\" y=\"%f\" width=\"%f\" height=\"%f\" rx=\"2\" " +
                        "style=\"stroke:%s; fill: %s\" />\n",
                start, Y, width, EXON_HEIGHT, DARKGREEN, GREEN);
        writer.write(rect);
    }

    /**
     * WRite a non-coding (i.e., UTR) exon of a non-coding gene
     *
     * @param start  start position in SVG coordinates of the UTR exon
     * @param end    end position in SVG coordinates of the UTR exon
     * @param ypos   yposition on SVG canvas
     * @param writer file handle
     * @throws IOException if we cannot write
     */
    protected void writeUtrExon(double start, double end, int ypos, Writer writer) throws IOException {
        double width = end - start;
        double Y = ypos - 0.5 * EXON_HEIGHT;
        String rect = String.format("<rect x=\"%f\" y=\"%f\" width=\"%f\" height=\"%f\"  " +
                        "style=\"stroke:%s; fill: %s\" />\n",
                start, Y, width, EXON_HEIGHT, DARKGREEN, YELLOW);
        writer.write(rect);
    }


    /**
     * This method writes one Jannovar transcript as a cartoon where the UTRs are shown in one color and the
     * the coding exons are shown in another color. TODO -- decide what to do with non-coding genes
     *
     * @param tmod   transcript representation
     * @param ypos   The y position where we will write the cartoon
     * @param writer file handle
     * @throws IOException if we cannot write.
     */
    private void writeCodingTranscript(Transcript tmod, int ypos, Writer writer) throws IOException {
        Transcript transcript = tmod.withStrand(Strand.POSITIVE);
        // guaranteed not null for coding
        @SuppressWarnings("OptionalGetWithoutIsPresent") GenomicRegion cds = transcript.cdsRegion().get();

        double cdsStart = translateGenomicToSvg(cds.start());
        double cdsEnd = translateGenomicToSvg(cds.end());
        List<GenomicRegion> exons = transcript.exons();
        double minX = Double.MAX_VALUE;
        double maxX = Double.MIN_VALUE;
        // write a line for UTR, otherwise write a box
        for (GenomicRegion exon : exons) {
            double exonStart = translateGenomicToSvg(exon.start());
            double exonEnd = translateGenomicToSvg(exon.end());
            if (exonStart < minX) minX = exonStart;
            if (exonEnd > maxX) maxX = exonEnd;
            if (exonStart >= cdsStart && exonEnd <= cdsEnd) {
                writeCdsExon(exonStart, exonEnd, ypos, writer);
            } else if (exonStart <= cdsEnd && exonEnd > cdsEnd) {
                // in this case, the 3' portion of the exon is UTR and the 5' is CDS
                writeCdsExon(exonStart, cdsEnd, ypos, writer);
                writeUtrExon(cdsEnd, exonEnd, ypos, writer);
            } else if (exonStart < cdsStart && exonEnd > cdsStart) {
                writeUtrExon(exonStart, cdsStart, ypos, writer);
                writeCdsExon(cdsStart, exonEnd, ypos, writer);
            } else {
                writeUtrExon(exonStart, exonEnd, ypos, writer);
            }
        }
        writeIntrons(exons, ypos, writer);
        writeTranscriptName(tmod, minX, maxX, ypos, writer);
        writeFoldChange(transcript.accessionId(), ypos, writer);
    }

    private void writeNonCodingTranscript(Transcript tmod, int ypos, Writer writer) throws IOException {
        Transcript transcript = tmod.withStrand(Strand.POSITIVE);
        List<GenomicRegion> exons = transcript.exons();
        double minX = Double.MAX_VALUE;
        double maxX = Double.MIN_VALUE;
        // write a line for UTR, otherwise write a box
        for (GenomicRegion exon : exons) {
            double exonStart = translateGenomicToSvg(exon.start());
            double exonEnd = translateGenomicToSvg(exon.end());
            if (exonStart < minX) minX = exonStart;
            if (exonEnd > maxX) maxX = exonEnd;
            writeUtrExon(exonStart, exonEnd, ypos, writer);
        }
        writeIntrons(exons, ypos, writer);
        writeTranscriptName(transcript, minX, maxX, ypos, writer);
        writeFoldChange(transcript.accessionId(), ypos, writer);
    }


    private double getLogFoldChage(AccessionNumber id) {
        Map<AccessionNumber, HbaDealsTranscriptResult> transcriptResultMap = hbaDealsResult.getTranscriptMap();
        if (!transcriptResultMap.containsKey(id)) return 0.0;
        double fc = transcriptResultMap.get(id).getFoldChange();
        return Math.log(fc) / Math.log(2);
    }

    private String getFormatedPvalue(AccessionNumber id) {
        Map<AccessionNumber, HbaDealsTranscriptResult> transcriptResultMap = hbaDealsResult.getTranscriptMap();
        double logFC = getLogFoldChage(id);
        if (!transcriptResultMap.containsKey(id)) return String.valueOf(logFC);
        double p = transcriptResultMap.get(id).getP();
        boolean differential = p < this.splicingThreshold;
        return getFormatedPvalue(logFC, p, differential);
    }

    private String getFormatedPvalue(double logFc, double pval, boolean differential) {
        if (! differential) {
            return String.format("%.2f", logFc);
        } else if (pval > 0.001) {
            return String.format("%.2f; p=%.4f (*)", logFc, pval);
        } else if (pval == 0.0) {
            return String.format("%.2f; p=0.00 (*)", logFc);
        } else {
            return String.format("%.2f; p=%.2E (*)", logFc, pval);
        }
    }


    private void writeFoldChange(AccessionNumber id, int ypos, Writer writer) throws IOException {
        double fc = getLogFoldChage(id);
        double startpos = translateGenomicToSvg(this.genomicMaxPos) + 25.0;
        writer.write(String.format("<line x1=\"%f\" y1=\"%f\" x2=\"%f\" y2=\"%f\" stroke=\"black\"/>\n",
                startpos, (double) ypos, startpos + 30, (double) ypos));
        double width = 20.0;
        double boxstart = startpos + 5.0;
        double factor = 25; // multiple logFC by this to get height
        String rect;
        if (fc > 0.0) {
            double height = fc * factor;
            double ybase = (double) ypos - height;
            rect = String.format("<rect x=\"%f\" y=\"%f\" width=\"%f\" height=\"%f\" rx=\"2\" " +
                            "style=\"stroke:%s; fill: %s\" />\n",
                    boxstart, ybase, width, height, BLACK, GREEN);
        } else {
            double height = fc * -factor;
            rect = String.format("<rect x=\"%f\" y=\"%f\" width=\"%f\" height=\"%f\" rx=\"2\" " +
                            "style=\"stroke:%s; fill: %s\" />\n",
                    boxstart, (double) ypos, width, height, BLACK, RED);
        }
        writer.write(rect);
        double xpos = startpos + width + 15;
        String txt = String.format("<text x=\"%f\" y=\"%f\" style=\"fill:%s;font-size:24px;\">%s</text>\n",
                xpos, (double) ypos, PURPLE, getFormatedPvalue(id));
        writer.write(txt);

    }

    /**
     * Differential can be used buy expression or isoform to indicated if we show n.s. for the probability.
     * @param logFc log fold change
     * @param prob probability
     * @param differential is this element differentially expressed/spliced?
     * @param ypos vertical position to draw
     * @param writer file handle
     * @throws IOException if we cannot write
     */
    private void writeFoldChange(double logFc, double prob, boolean differential, int ypos, Writer writer) throws IOException {
        double startpos = translateGenomicToSvg(this.genomicMaxPos) + 25.0;
        writer.write(String.format("<line x1=\"%f\" y1=\"%f\" x2=\"%f\" y2=\"%f\" stroke=\"black\"/>\n",
                startpos, (double) ypos, startpos + 30, (double) ypos));
        double width = 20.0;
        double boxstart = startpos + 5.0;
        double factor = 25; // multiple logFC by this to get height
        String rect;
        if (logFc > 0.0) {
            double height = logFc * factor;
            double ybase = (double) ypos - height;
            rect = String.format("<rect x=\"%f\" y=\"%f\" width=\"%f\" height=\"%f\" rx=\"2\" " +
                            "style=\"stroke:%s;font-size:24px; fill: %s\" />\n",
                    boxstart, ybase, width, height, BLACK, GREEN);
        } else {
            double height = logFc * -factor;
            rect = String.format("<rect x=\"%f\" y=\"%f\" width=\"%f\" height=\"%f\" rx=\"2\" " +
                            "style=\"stroke:%s;fill:%s\" />\n",
                    boxstart, (double) ypos, width, height, BLACK, RED);
        }
        writer.write(rect);
        double xpos = startpos + width + 15;
        String txt = String.format("<text x=\"%f\" y=\"%f\" style=\"fill:%s;font-size:24px;\">%s</text>\n",
                xpos, (double) ypos, PURPLE, getFormatedPvalue(logFc, prob, differential));
        writer.write(txt);

    }


    /**
     * Write a line to indicate transcript (UTR) or a dotted line to indicate introns. The line forms
     * a triangle (inspired by the way Ensembl represents introns).
     *
     * @param exons list of exons on {@link Strand#POSITIVE} in sorted order (chromosome 5' to 3')
     * @param ypos  vertical midline
     * @throws IOException if we cannot write
     */
    private void writeIntrons(List<GenomicRegion> exons, int ypos, Writer writer) throws IOException {
        // if the gene does not have an intron, we are done
        if (exons.size() == 1)
            return;
        List<Integer> intronStarts = new ArrayList<>();
        List<Integer> intronEnds = new ArrayList<>();
        for (int i = 1; i < exons.size(); i++) {
            GenomicRegion previous = exons.get(i - 1);
            GenomicRegion current = exons.get(i);
            intronStarts.add(previous.end());
            intronEnds.add(current.start());
        }
        for (int i = 0; i < intronStarts.size(); i++) {
            double startpos = translateGenomicToSvg(intronStarts.get(i));
            double endpos = translateGenomicToSvg(intronEnds.get(i));
            double midpoint = 0.5 * (startpos + endpos);
            writer.write(String.format("<line x1=\"%f\" y1=\"%f\" x2=\"%f\" y2=\"%f\" stroke=\"black\"/>\n",
                    startpos, (double) ypos, midpoint, (double) ypos - INTRON_MIDPOINT_ELEVATION));
            writer.write(String.format("<line x1=\"%f\" y1=\"%f\" x2=\"%f\" y2=\"%f\" stroke=\"black\"/>\n",
                    midpoint, (double) ypos - INTRON_MIDPOINT_ELEVATION, endpos, (double) ypos));
        }
    }

    private int getRegionOfSvgCanvas(double xpos) {
        if (xpos/this.scaleMaxPos < 0.3333) {
            return FIRST_THIRD_OF_SVG;
        } else if (xpos/this.scaleMaxPos < 0.66667) {
            return SECOND_THIRD_OF_SVG;
        } else {
            return THIRD_THIRD_OF_SVG;
        }
    }

    private void writeTranscriptName(Transcript tmod, double minX, double maxX, int ypos, Writer writer) throws IOException {
        String symbol = tmod.hgvsSymbol();
        AccessionNumber accession = tmod.accessionId();
        Transcript txOnFwdStrand = tmod.withStrand(Strand.POSITIVE);
       // String positionString = String.format("%s:%d-%d (%s strand)", chrom, start, end, strand);
        String geneName = String.format("%s (%s)", symbol, accession.getAccessionString());
        double y = Y_SKIP_BENEATH_TRANSCRIPTS + ypos;
        int region = getRegionOfSvgCanvas(minX);
        String textAnchor = "start";
        double textBeginX = minX;
        switch (region) {
            case FIRST_THIRD_OF_SVG:
                textBeginX = minX;
                break;
            case SECOND_THIRD_OF_SVG:
                textBeginX = 0.5*(minX+maxX);
                textAnchor = "middle";
                break;
            case THIRD_THIRD_OF_SVG:
                textBeginX = maxX;
                textAnchor = "end";
                break;

            default:



        }
        String txt = String.format("<text x=\"%f\" y=\"%f\" style=\"fill:%s;font-size:24px;text-anchor:%s\">%s</text>\n",
                textBeginX, y, PURPLE, textAnchor, String.format("%s", geneName));
        writer.write(txt);
    }


    protected void writeScale(Writer writer, int ypos) throws IOException {
        int verticalOffset = 10;
        String line = String.format("<line x1=\"%f\" y1=\"%d\"  x2=\"%f\"  y2=\"%d\" style=\"stroke: #000000; fill:none;" +
                " stroke-width: 1px;" +
                " stroke-dasharray: 5 2\" />\n", this.scaleMinPos, ypos, this.scaleMaxPos, ypos);
        String leftVertical = String.format("<line x1=\"%f\" y1=\"%d\"  x2=\"%f\"  y2=\"%d\" style=\"stroke: #000000; fill:none;" +
                " stroke-width: 1px;\" />\n", this.scaleMinPos, ypos + verticalOffset, this.scaleMinPos, ypos - verticalOffset);
        String rightVertical = String.format("<line x1=\"%f\" y1=\"%d\"  x2=\"%f\"  y2=\"%d\" style=\"stroke: #000000; fill:none;" +
                " stroke-width: 1px;\" />\n", this.scaleMaxPos, ypos + verticalOffset, this.scaleMaxPos, ypos - verticalOffset);
        String sequenceLength = getSequenceLengthString(scaleBasePairs);
        writer.write(line);
        writer.write(leftVertical);
        writer.write(rightVertical);
        int y = ypos - 15;
        double xmiddle = 0.45 * (this.scaleMinPos + this.scaleMaxPos);
        String txt = String.format("<text x=\"%f\" y=\"%d\" style=\"fill:%s;font-size:24px;text-anchor:middle\">%s</text>\n",
                xmiddle, y, PURPLE, sequenceLength);
        writer.write(txt);
    }


    /**
     * Get a string that represents a sequence length using bp, kb, or Mb as appropriate
     *
     * @param seqlen number of base bairs
     * @return String such as 432 bp, 4.56 kb or 1.23 Mb
     */
    protected String getSequenceLengthString(int seqlen) {
        if (seqlen < 1_000) {
            return String.format("%d bp", seqlen);
        } else if (seqlen < 1_000_000) {
            double kb = (double) seqlen / 1000.0;
            return String.format("%.2f kp", kb);
        } else {
            // if we get here, the sequence is at least one million bp
            double mb = (double) seqlen / 1000000.0;
            return String.format("%.2f Mp", mb);
        }
    }

    /**
     * Get a string containing an SVG representing the SV.
     *
     * @return an SVG string
     */
    public String getSvg() {
        StringWriter swriter = new StringWriter();
        try {
            writeHeader(swriter);
            write(swriter);
            writeFooter(swriter);
            return swriter.toString();
        } catch (IOException e) {
            return getSvgErrorMessage(e.getMessage());
        }
    }

    /**
     * Write the expression fold change and probability
     * @param writer file handle
     * @param ypos vertical position to draw
     * @throws IOException if we cannot write
     */
    private void writeGeneExpression(Writer writer, int ypos) throws IOException {
        int xpos = 1050;
        String txt = String.format("<text font-style=\"italic\" font-weight=\"bold\" font-size=\"1.2em\" x=\"%d\" y=\"%d\" style=\"fill:%s;font-size:24px;\">Expression:</text>\n",
                xpos, ypos, DARKBLUE);
        double expressionLog2FoldChange = this.hbaDealsResult.getExpressionFoldChange();
        double expressionPval = this.hbaDealsResult.getExpressionP();
        writeFoldChange(expressionLog2FoldChange, expressionPval, this.differentiallyExpressed, ypos, writer);
        writer.write(txt);
    }

    /**
     * Wirte an SVG (without header) representing this SV. Not intended to be used to create a stand-alone
     * SVG (for this, user {@link #getSvg()}
     *
     * @param writer a file handle
     */
    @Override
    public void write(Writer writer) {
        int y = 80;
        try {
            writeGeneExpression(writer, y);
            y += 100;
            for (var tmod : this.affectedTranscripts) {
                if (tmod.isCoding()) {
                    writeCodingTranscript(tmod, y, writer);
                } else {
                    writeNonCodingTranscript(tmod, y, writer);
                }
                y += HEIGHT_PER_DISPLAY_ITEM;
            }
            writeScale(writer, y);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static AbstractSvgGenerator factory(AnnotatedGene annotatedTranscript) {
        List<Transcript> transcripts = annotatedTranscript.getTranscripts();
        HbaDealsResult result = annotatedTranscript.getHbaDealsResult();
        Map<AccessionNumber, HbaDealsTranscriptResult> transcriptMap = result.getTranscriptMap();
        List<Transcript> affectedTranscripts = transcripts
                .stream()
                .filter(t -> transcriptMap.containsKey(t.accessionId()))
                .collect(Collectors.toList());
        int height = HEIGHT_FOR_SV_DISPLAY + affectedTranscripts.size() * HEIGHT_PER_DISPLAY_ITEM;
        return new TranscriptSvgGenerator(height,
                annotatedTranscript);
    }

}
