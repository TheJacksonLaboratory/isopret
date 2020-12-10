package org.jax.isopret.prosite;

import org.jax.isopret.TestBase;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class PrositeMapParserTest extends TestBase {
    /**
     * Test that we correctly extract a map from Prosite accession numbers such as
     * PS50141 to the corresponding motif/domain names, such as A_DEAMIN_EDITASE
     */
    @Test
    public void testPrositeMapParser() {
        PrositeMapParser parser = getPrositeMapParser();
        Map<String,String> nameMap = parser.getPrositeNameMap();
        assertTrue(nameMap.containsKey("PS50141"));
        assertEquals("A_DEAMIN_EDITASE", nameMap.get("PS50141"));
        assertTrue(nameMap.containsKey("PS00001"));
        assertEquals("ASN_GLYCOSYLATION", nameMap.get("PS00001"));
        assertTrue(nameMap.containsKey("PS00004"));
        assertEquals("CAMP_PHOSPHO_SITE", nameMap.get("PS00004"));
        assertTrue(nameMap.containsKey("PS50137"));
        assertEquals("DS_RBD", nameMap.get("PS50137"));
        assertTrue(nameMap.containsKey("PS50139"));
        assertEquals("Z_BINDING", nameMap.get("PS50139"));
    }

    /**
     * Test that we correctly extract a map from Prosite accession numbers such as
     * ENST00000648231	ENSG00000160710	PS50137	319	387
     * ENST00000648231	ENSG00000160710	PS50137	208	276
     * ENST00000648231	ENSG00000160710	PS50139	1	65
     * ENST00000648231	ENSG00000160710	PS50141	591	926
     * ENST00000648231	ENSG00000160710	PS50137	431	499
     */
    @Test
    public void testPrositeHitParser() {
        PrositeMapParser parser = getPrositeMapParser();
        Map<String, PrositeMapping> prositeMappingMap = parser.getPrositeMappingMap();
        String enstId = "ENST00000648231";
        assertTrue(prositeMappingMap.containsKey(enstId));
        PrositeMapping prositeMapping = prositeMappingMap.get(enstId);
        assertEquals("ENSG00000160710", prositeMapping.getGeneID());
        List<PrositeHit> hitList = prositeMapping.getHits();
        assertEquals(5, hitList.size());
        PrositeHit hit1 = new PrositeHit("PS50137",319, 387);
        assertTrue(hitList.stream().anyMatch(t -> t.equals(hit1)));
        PrositeHit fakeHit = new PrositeHit("PSFAKE",42, 43);
        assertFalse(hitList.stream().anyMatch(t -> t.equals(fakeHit)));
    }


}
