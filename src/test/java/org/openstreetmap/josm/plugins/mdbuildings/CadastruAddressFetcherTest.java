package org.openstreetmap.josm.plugins.mdbuildings;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

public class CadastruAddressFetcherTest {

    @Test
    public void testExpandAbbreviations() {
        // Simple replacements
        assertEquals("Strada Mihai Eminescu", CadastruAddressFetcher.expandAbbreviations("str. Mihai Eminescu"));
        assertEquals("Bulevardul Ștefan cel Mare", CadastruAddressFetcher.expandAbbreviations("bd. Ștefan cel Mare"));
        assertEquals("Stradela Florilor", CadastruAddressFetcher.expandAbbreviations("str-la Florilor"));
        assertEquals("Stradela Florilor", CadastruAddressFetcher.expandAbbreviations("str-la. Florilor"));
        assertEquals("Piața Centrală", CadastruAddressFetcher.expandAbbreviations("p-ța Centrală"));

        // Exact words
        assertEquals("Șoseaua Hîncești", CadastruAddressFetcher.expandAbbreviations("șos. Hîncești"));
        assertEquals("Pasajul pietonal", CadastruAddressFetcher.expandAbbreviations("pas. pietonal"));
        assertEquals("Fundătura Liniștii", CadastruAddressFetcher.expandAbbreviations("fun. Liniștii"));

        // Localities
        assertEquals("Municipiul Chișinău", CadastruAddressFetcher.expandAbbreviations("mun. Chișinău"));
        assertEquals("Municipiul Ungheni", CadastruAddressFetcher.expandAbbreviations("mun. Ungheni"));
        assertEquals("Orașul Chișinău", CadastruAddressFetcher.expandAbbreviations("or. Chișinău"));
        assertEquals("Satul Colonița", CadastruAddressFetcher.expandAbbreviations("sat. Colonița"));
        assertEquals("Comuna Stăuceni", CadastruAddressFetcher.expandAbbreviations("com. Stăuceni"));
        assertEquals("Raionul Ialoveni", CadastruAddressFetcher.expandAbbreviations("r-nul Ialoveni"));
        assertEquals("Localitatea-stație de cale ferată Bălți", CadastruAddressFetcher.expandAbbreviations("loc. st. cf. Bălți"));
        assertEquals("Drumul național M1", CadastruAddressFetcher.expandAbbreviations("dr. naț. M1"));

        // Edge cases
        assertNull(CadastruAddressFetcher.expandAbbreviations(null));
        assertNull(CadastruAddressFetcher.expandAbbreviations("null"));
        assertNull(CadastruAddressFetcher.expandAbbreviations("   "));

        // No replacements should happen
        assertEquals("Strada", CadastruAddressFetcher.expandAbbreviations("Strada"));
        assertEquals("Vasile Alecsandri", CadastruAddressFetcher.expandAbbreviations("Vasile Alecsandri"));

        // M. Eminescu should keep the text (warning issued at runtime only)
        assertEquals("M. Eminescu", CadastruAddressFetcher.expandAbbreviations("M. Eminescu"));

        // Mixed capitalization
        assertEquals("Strada Test", CadastruAddressFetcher.expandAbbreviations("STR. Test"));
        assertEquals("Satul Test", CadastruAddressFetcher.expandAbbreviations("Sat. Test"));
        assertEquals("Bulevardul Dacia", CadastruAddressFetcher.expandAbbreviations("Bd. Dacia"));
    }

    @Test
    public void testStripSettlementType() {
        // Standard prefixes should be stripped
        assertEquals("Chișinău", CadastruAddressFetcher.stripSettlementType("Municipiul Chișinău"));
        assertEquals("Fălești", CadastruAddressFetcher.stripSettlementType("Orașul Fălești"));
        assertEquals("Dumbrava", CadastruAddressFetcher.stripSettlementType("Satul Dumbrava"));
        assertEquals("Stăuceni", CadastruAddressFetcher.stripSettlementType("Comuna Stăuceni"));
        assertEquals("Ialoveni", CadastruAddressFetcher.stripSettlementType("Raionul Ialoveni"));
        assertEquals("Găgăuzia", CadastruAddressFetcher.stripSettlementType("UTA Găgăuzia"));
        // Special name must stay intact
        assertEquals("Localitatea-stație de cale ferată Bălți",
            CadastruAddressFetcher.stripSettlementType("Localitatea-stație de cale ferată Bălți"));
        // Null handled
        assertNull(CadastruAddressFetcher.stripSettlementType(null));
    }
}
