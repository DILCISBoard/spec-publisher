package eu.dilcis.csip.profile;

import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;

import org.junit.Test;
import org.xml.sax.SAXException;

public class MetsProfileParserTest {

    @Test(expected = IllegalArgumentException.class)
    public void testFromNullProfilePath() throws SAXException, IOException {
        MetsProfileParser parser = MetsProfileParser.getInstance();
        parser.processXmlProfile(null);
    }

    @Test(expected = FileNotFoundException.class)
    public void testFromNotExistPath() throws SAXException, IOException {
        MetsProfileParser parser = MetsProfileParser.getInstance();
        parser.processXmlProfile(Paths.get("random/path/of/rubbish"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFromDirPath() throws SAXException, IOException {
        MetsProfileParser parser = MetsProfileParser.getInstance();
        parser.processXmlProfile(Paths.get("./"));
    }

    @Test(expected = SAXException.class)
    public void testFromEmptyFile() throws SAXException, IOException {
        MetsProfileParser parser = MetsProfileParser.getInstance();
        parser.processXmlProfile(
                Paths.get(ClassLoader.getSystemResource("eu/dilcis/csip/structure/profile/empty").getPath()));
    }

    @Test(expected = SAXException.class)
    public void testFromNotXmlFile() throws SAXException, IOException {
        MetsProfileParser parser = MetsProfileParser.getInstance();
        parser.processXmlProfile(
                Paths.get(ClassLoader.getSystemResource("eu/dilcis/csip/structure/profile/plain.txt").getPath()));
    }

    @Test
    public void testProcessXmlProfile() throws SAXException, IOException {
        MetsProfileParser parser = MetsProfileParser.getInstance();
        MetsProfile profile = parser.processXmlProfile(
                Paths.get(ClassLoader.getSystemResource("eu/dilcis/csip/structure/profile/oldCSIP.xml").getPath()));
        assertNotNull(profile);
        List<Requirement> requirements = profile.getRequirements();
        assertNotNull(requirements);
        assertNotEquals(0, requirements.size());
    }
}
