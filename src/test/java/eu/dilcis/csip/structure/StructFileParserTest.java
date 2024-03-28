package eu.dilcis.csip.structure;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.yaml.snakeyaml.Yaml;

import eu.dilcis.csip.profile.Profiles;
import eu.dilcis.csip.profile.Requirement;
import eu.dilcis.csip.structure.SpecificationStructure.Section;
import eu.dilcis.csip.structure.SpecificationStructure.Table;

public class StructFileParserTest {
    private static final List<Requirement> requirements = new ArrayList<>();
    private static final Path STRUCT_ROOT = Path
            .of(ClassLoader.getSystemResource("eu/dilcis/csip/structure/").getPath());
    private StructFileParser parser = StructFileParser.parserInstance(requirements);

    @Test(expected = IllegalArgumentException.class)
    public void testFromNullYamlStream() throws ParseException {
        parser.fromYamlStream(null, STRUCT_ROOT);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFromYamlStreamNullRoot() throws ParseException {
        InputStream is = ClassLoader.getSystemResourceAsStream("eu/dilcis/csip/structure/empty.yaml");
        parser.fromYamlStream(is, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFromYamlStreamFileRoot() throws ParseException {
        Path fileRoot = Path
                .of(ClassLoader.getSystemResource("eu/dilcis/csip/structure/empty.yaml").getPath());
        InputStream is = ClassLoader.getSystemResourceAsStream("eu/dilcis/csip/structure/empty.yaml");
        parser.fromYamlStream(is, fileRoot);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFromYamlStreamRootNotExist() throws ParseException {
        Path missingRoot = Path.of("random/path/of/rubbish");
        InputStream is = ClassLoader.getSystemResourceAsStream("eu/dilcis/csip/structure/empty.yaml");
        parser.fromYamlStream(is, missingRoot);
    }

    @Test(expected = ParseException.class)
    public void testFromEmptyYamlStream() throws ParseException {
        InputStream is = ClassLoader.getSystemResourceAsStream("eu/dilcis/csip/structure/empty.yaml");
        parser.fromYamlStream(is, STRUCT_ROOT);
    }

    @Test(expected = ParseException.class)
    public void testFromNonYamlStream() throws ParseException {
        InputStream is = ClassLoader.getSystemResourceAsStream("eu/dilcis/csip/structure/text_not.yaml");
        parser.fromYamlStream(is, STRUCT_ROOT);
    }

    @Test(expected = ParseException.class)
    public void testSectionFromYamlStreamNoName() throws ParseException {
        InputStream is = ClassLoader.getSystemResourceAsStream("eu/dilcis/csip/structure/section_no_name.yaml");
        parser.fromYamlStream(is, STRUCT_ROOT);
    }

    @Test(expected = ParseException.class)
    public void testSectionFromYamlStreamNoPath() throws ParseException {
        InputStream is = ClassLoader.getSystemResourceAsStream("eu/dilcis/csip/structure/section_no_path.yaml");
        parser.fromYamlStream(is, STRUCT_ROOT);
    }

    @Test(expected = ParseException.class)
    public void testSectionFromYamlStreamNoType() throws ParseException {
        InputStream is = ClassLoader.getSystemResourceAsStream("eu/dilcis/csip/structure/section_no_type.yaml");
        parser.fromYamlStream(is, STRUCT_ROOT);
    }

    @Test(expected = ParseException.class)
    public void testSectionFromYamlStreamDirPath() throws ParseException {
        InputStream is = ClassLoader.getSystemResourceAsStream("eu/dilcis/csip/structure/section_dir_path.yaml");
        parser.fromYamlStream(is, STRUCT_ROOT);
    }

    @Test(expected = ParseException.class)
    public void testSectionFromYamlStreamMissingPath() throws ParseException {
        InputStream is = ClassLoader.getSystemResourceAsStream("eu/dilcis/csip/structure/section_missing_path.yaml");
        parser.fromYamlStream(is, STRUCT_ROOT);
    }

    @Test
    public void testSectionFromSource() throws ParseException {
        InputStream is = ClassLoader.getSystemResourceAsStream("eu/dilcis/csip/structure/section.yaml");
        SpecificationStructure structure = parser.fromYamlStream(is, STRUCT_ROOT);
        assertNotNull(structure);
        assertEquals("Parsed Structure should have 1 section", 1, structure.sections.size());
        Section section = structure.sections.get(0);
        assertNotNull(section);
        assertEquals("context", section.name);
        assertTrue(Files.isRegularFile(section.source));
    }

    @Test(expected = ParseException.class)
    public void testTableFromYamlStreamNoName() throws ParseException {
        InputStream is = ClassLoader.getSystemResourceAsStream("eu/dilcis/csip/structure/table_no_name.yaml");
        parser.fromYamlStream(is, STRUCT_ROOT);
    }

    @Test(expected = ParseException.class)
    public void testTableFromYamlStreamNoHeading() throws ParseException {
        InputStream is = ClassLoader.getSystemResourceAsStream("eu/dilcis/csip/structure/table_no_heading.yaml");
        parser.fromYamlStream(is, STRUCT_ROOT);
    }

    @Test(expected = ParseException.class)
    public void testTableFromYamlStreamNoType() throws ParseException {
        InputStream is = ClassLoader.getSystemResourceAsStream("eu/dilcis/csip/structure/table_no_type.yaml");
        parser.fromYamlStream(is, STRUCT_ROOT);
    }

    @Test(expected = ParseException.class)
    public void testTableFromYamlStreamEmptyReqs() throws ParseException {
        InputStream is = ClassLoader.getSystemResourceAsStream("eu/dilcis/csip/structure/table_empty_reqs.yaml");
        parser.fromYamlStream(is, STRUCT_ROOT);
    }

    @Test(expected = ParseException.class)
    public void testTableFromYamlStreamEmptyReqEles() throws ParseException {
        InputStream is = ClassLoader.getSystemResourceAsStream("eu/dilcis/csip/structure/table_empty_req_eles.yaml");
        parser.fromYamlStream(is, STRUCT_ROOT);
    }

    @Test(expected = ParseException.class)
    public void testTableFromYamlStreamNullReqEles() throws ParseException {
        InputStream is = ClassLoader.getSystemResourceAsStream("eu/dilcis/csip/structure/table_null_req_eles.yaml");
        parser.fromYamlStream(is, STRUCT_ROOT);
    }

    @Test
    public void testTableFromSource() throws ParseException {
        InputStream is = ClassLoader.getSystemResourceAsStream("eu/dilcis/csip/structure/table.yaml");
        final List<Map<String, Object>> mapList = new Yaml().load(is);
        
        Table table = parser.tableFromMap((String) mapList.get(0).get("name"), mapList.get(0));
        assertNotNull(table);
        assertEquals("requirements.METS.package", table.name);
        assertTrue(Files.isRegularFile(table.source));
        assertEquals("Table should have 6 requirments.", 6, table.requirements.size());
        assert table.requirements.contains(Profiles.requirementIdFromString("GEO_3"));
        assert table.requirements.contains(Profiles.requirementIdFromString("GEO_4"));
        assert table.requirements.contains(Profiles.requirementIdFromString("GEO_6"));
    }

    @Test
    public void testFromYamlStream() throws ParseException {
        InputStream is = ClassLoader.getSystemResourceAsStream("eu/dilcis/csip/structure/struct_test.yaml");
        SpecificationStructure structure = parser.fromYamlStream(is, STRUCT_ROOT);
        assertNotNull(structure);
        assertEquals("Parsed Structure should have 6 sections", 6, structure.sections.size());
        for (SpecificationStructure.Section section : structure.sections) {
            assertNotNull(section);
            assertNotNull(section.name);
            assertNotNull(section.source);
            assertNotNull(section.type);
        }
    }
}
