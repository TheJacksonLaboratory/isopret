package org.jax.isopret.transcript;

import org.jax.isopret.except.IsopretRuntimeException;


public class AccessionNumber {

    enum Database {ENSEMBL, REFSEQ, NCBIGENE}

    enum Category { GENE, TRANSCRIPT }

    private final Database database;

    private final Category category;

    private final int accession;

    private AccessionNumber(Database d, Category category,int acc) {
        this.database = d;
        this.category = category;
        this.accession = acc;
    }

    public String getAccessionString() {
        if (this.category == Category.GENE) {
            return getGeneAccession();
        } else if (this.category == Category.TRANSCRIPT) {
            return getTranscriptAccession();
        } else {
            throw new IsopretRuntimeException("Unrecognized category"); // should never happen
        }
    }

    public int getAccessionNumber() {
        return this.accession;
    }

    private String getGeneAccession() {
        if (this.database == Database.ENSEMBL) {
            return ensgAccessionToString(this.accession);
        } else {
            throw new IsopretRuntimeException("Unrecognized database"); // should never happen
        }
    }

    private String getTranscriptAccession() {
        if (this.database == Database.ENSEMBL) {
            return enstAccessionToString(this.accession);
        } else {
            throw new IsopretRuntimeException("Unrecognized database"); // should never happen
        }
    }


    public static AccessionNumber ensemblGene(String ensg) {
        return new AccessionNumber(Database.ENSEMBL, Category.GENE,ensgAccessionToInt(ensg));
    }

    public static AccessionNumber ensemblTranscript(String enst) {
        return new AccessionNumber(Database.ENSEMBL, Category.TRANSCRIPT,enstAccessionToInt(enst));
    }


    /**
     * An ensembl gene id of the form ENSG00000139618 or ENSG00000139618.2
     * @param ensg
     * @return
     */
    public static int ensgAccessionToInt(String ensg) {
        if (! ensg.startsWith("ENSG")) {
            throw new IsopretRuntimeException("Ensembl gene id must start with ENSG but we got \"" + ensg + "\"");
        }
        ensg = ensg.substring(4);
        int i = ensg.indexOf(".");
        if (i > 0) {
            ensg = ensg.substring(0,i);
        }
        return Integer.parseInt(ensg);
    }

    /**
     * Return original Ensembl string (without version number)
     * @param ensg
     * @return
     */
    public static String ensgAccessionToString(int ensg) {
        if (ensg<1) {
            throw new IsopretRuntimeException("Negative integer used for Ensembl id:" + ensg);
        }
        return String.format("ENSG%011d", ensg);
    }

    /**
     * An ensembl transcript id of the form ENST00000560355 or ENST00000560355.1
     * @param ensg
     * @return
     */
    public static int enstAccessionToInt(String ensg) {
        if (! ensg.startsWith("ENST")) {
            throw new IsopretRuntimeException("Ensembl transcript id must start with ENST but we got \"" + ensg + "\"");
        }
        ensg = ensg.substring(4);
        int i = ensg.indexOf(".");
        if (i > 0) {
            ensg = ensg.substring(0,i);
        }
        return Integer.parseInt(ensg);
    }

    /**
     * Return original Ensembl string (without version number)
     * @param enst
     * @return
     */
    public static String enstAccessionToString(int enst) {
        if (enst<1) {
            throw new IsopretRuntimeException("Negative integer used for Ensembl id:" + enst);
        }
        return String.format("ENST%011d", enst);
    }

}
