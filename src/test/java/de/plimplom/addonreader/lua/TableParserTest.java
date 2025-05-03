package de.plimplom.addonreader.lua;

import com.google.gson.JsonElement;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class TableParserTest {

    @Test
    void parseLuaTableToJson() throws IOException {
        var resource = getClass().getClassLoader().getResourceAsStream("plimplomGamba.lua");
        var res = TableParser.parseLuaTableToJsonNative(resource);

        var highLow = res.getAsJsonObject().get("HighLow");

        assertNotNull(highLow);

        assertEquals(2, highLow.getAsJsonObject().size());

        var highLowSpecific = highLow.getAsJsonObject().get("Specific");

        assertNotNull(highLowSpecific);

        assertEquals(3, highLowSpecific.getAsJsonObject().size());
        assertTrue(highLowSpecific.getAsJsonObject().has("test"));
        assertTrue(highLowSpecific.getAsJsonObject().has("Plimplom"));
        assertTrue(highLowSpecific.getAsJsonObject().has("test2"));

        assertEquals(2, highLowSpecific.getAsJsonObject().get("test").getAsJsonObject().size());
        assertTrue(highLowSpecific.getAsJsonObject().get("test").getAsJsonObject().has("Plimplom"));
        assertTrue(highLowSpecific.getAsJsonObject().get("test").getAsJsonObject().has("test2"));
        assertEquals(-53778, highLowSpecific.getAsJsonObject().get("test").getAsJsonObject().get("Plimplom").getAsLong());
        assertEquals(61800, highLowSpecific.getAsJsonObject().get("test").getAsJsonObject().get("test2").getAsLong());

        assertEquals(2, highLowSpecific.getAsJsonObject().get("Plimplom").getAsJsonObject().size());
        assertTrue(highLowSpecific.getAsJsonObject().get("Plimplom").getAsJsonObject().has("test"));
        assertTrue(highLowSpecific.getAsJsonObject().get("Plimplom").getAsJsonObject().has("test2"));
        assertEquals(53778, highLowSpecific.getAsJsonObject().get("Plimplom").getAsJsonObject().get("test").getAsLong());
        assertEquals(-89465, highLowSpecific.getAsJsonObject().get("Plimplom").getAsJsonObject().get("test2").getAsLong());

        assertEquals(2, highLowSpecific.getAsJsonObject().get("test2").getAsJsonObject().size());
        assertTrue(highLowSpecific.getAsJsonObject().get("test2").getAsJsonObject().has("test"));
        assertTrue(highLowSpecific.getAsJsonObject().get("test2").getAsJsonObject().has("Plimplom"));
        assertEquals(-61800, highLowSpecific.getAsJsonObject().get("test2").getAsJsonObject().get("test").getAsLong());
        assertEquals(89465, highLowSpecific.getAsJsonObject().get("test2").getAsJsonObject().get("Plimplom").getAsLong());


        var highLowAll = highLow.getAsJsonObject().get("All");

        assertNotNull(highLowAll);

        assertEquals(3, highLowAll.getAsJsonObject().size());
        assertTrue(highLowAll.getAsJsonObject().has("test"));
        assertTrue(highLowAll.getAsJsonObject().has("Plimplom"));
        assertTrue(highLowAll.getAsJsonObject().has("test2"));

        assertEquals(12842, highLowAll.getAsJsonObject().get("test").getAsLong());
        assertEquals(-40507, highLowAll.getAsJsonObject().get("Plimplom").getAsLong());
        assertEquals(27665, highLowAll.getAsJsonObject().get("test2").getAsLong());

        assertEquals(0, highLowAll.getAsJsonObject().asMap().values().stream().map(JsonElement::getAsLong).reduce(Long::sum).orElse(-1L));


        var deathroll = res.getAsJsonObject().get("Deathroll");
        assertNotNull(deathroll);
        assertEquals(6, deathroll.getAsJsonObject().size());

        assertTrue(deathroll.getAsJsonObject().has("1745081100;test-Plimplom"));
        assertEquals(1745081100, deathroll.getAsJsonObject().get("1745081100;test-Plimplom").getAsJsonObject().get("timestamp").getAsLong());
        assertEquals("test", deathroll.getAsJsonObject().get("1745081100;test-Plimplom").getAsJsonObject().get("winner").getAsString());
        assertEquals("Plimplom", deathroll.getAsJsonObject().get("1745081100;test-Plimplom").getAsJsonObject().get("loser").getAsString());
        assertEquals(5000, deathroll.getAsJsonObject().get("1745081100;test-Plimplom").getAsJsonObject().get("amount").getAsLong());

        assertTrue(deathroll.getAsJsonObject().has("1745080800;test-Plimplom"));
        assertEquals(1745080800, deathroll.getAsJsonObject().get("1745080800;test-Plimplom").getAsJsonObject().get("timestamp").getAsLong());
        assertEquals("test", deathroll.getAsJsonObject().get("1745080800;test-Plimplom").getAsJsonObject().get("winner").getAsString());
        assertEquals("Plimplom", deathroll.getAsJsonObject().get("1745080800;test-Plimplom").getAsJsonObject().get("loser").getAsString());
        assertEquals(5500, deathroll.getAsJsonObject().get("1745080800;test-Plimplom").getAsJsonObject().get("amount").getAsLong());

        assertTrue(deathroll.getAsJsonObject().has("1745080900;test-Plimplom"));
        assertEquals(1745080900, deathroll.getAsJsonObject().get("1745080900;test-Plimplom").getAsJsonObject().get("timestamp").getAsLong());
        assertEquals("test", deathroll.getAsJsonObject().get("1745080900;test-Plimplom").getAsJsonObject().get("winner").getAsString());
        assertEquals("Plimplom", deathroll.getAsJsonObject().get("1745080900;test-Plimplom").getAsJsonObject().get("loser").getAsString());
        assertEquals(5000, deathroll.getAsJsonObject().get("1745080900;test-Plimplom").getAsJsonObject().get("amount").getAsLong());

        assertTrue(deathroll.getAsJsonObject().has("1745080200;Plimplom-test"));
        assertEquals(1745080200, deathroll.getAsJsonObject().get("1745080200;Plimplom-test").getAsJsonObject().get("timestamp").getAsLong());
        assertEquals("Plimplom", deathroll.getAsJsonObject().get("1745080200;Plimplom-test").getAsJsonObject().get("winner").getAsString());
        assertEquals("test", deathroll.getAsJsonObject().get("1745080200;Plimplom-test").getAsJsonObject().get("loser").getAsString());
        assertEquals(15000, deathroll.getAsJsonObject().get("1745080200;Plimplom-test").getAsJsonObject().get("amount").getAsLong());

        assertTrue(deathroll.getAsJsonObject().has("1745081600;test-Plimplom"));
        assertEquals(1745081600, deathroll.getAsJsonObject().get("1745081600;test-Plimplom").getAsJsonObject().get("timestamp").getAsLong());
        assertEquals("test", deathroll.getAsJsonObject().get("1745081600;test-Plimplom").getAsJsonObject().get("winner").getAsString());
        assertEquals("Plimplom", deathroll.getAsJsonObject().get("1745081600;test-Plimplom").getAsJsonObject().get("loser").getAsString());
        assertEquals(5000, deathroll.getAsJsonObject().get("1745081600;test-Plimplom").getAsJsonObject().get("amount").getAsLong());

        assertTrue(deathroll.getAsJsonObject().has("1745081000;test-Plimplom"));
        assertEquals(1745081000, deathroll.getAsJsonObject().get("1745081000;test-Plimplom").getAsJsonObject().get("timestamp").getAsLong());
        assertEquals("test", deathroll.getAsJsonObject().get("1745081000;test-Plimplom").getAsJsonObject().get("winner").getAsString());
        assertEquals("Plimplom", deathroll.getAsJsonObject().get("1745081000;test-Plimplom").getAsJsonObject().get("loser").getAsString());
        assertEquals(5000, deathroll.getAsJsonObject().get("1745081000;test-Plimplom").getAsJsonObject().get("amount").getAsLong());
    }

    @Test
    void testEmptyFile() throws Exception {
        var resource = getClass().getClassLoader().getResourceAsStream("empty.lua");
        var res = TableParser.parseLuaTableToJsonNative(resource);

        assertNotNull(res);

        assertEquals(0, res.getAsJsonObject().size());
    }

}