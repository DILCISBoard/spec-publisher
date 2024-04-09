package eu.dilcis.csip.structure;

import org.junit.Test;

import eu.dilcis.csip.structure.SpecificationStructure.Section;
import eu.dilcis.csip.structure.SpecificationStructure.Table;
import nl.jqno.equalsverifier.EqualsVerifier;

public class StructTypesTest {
    @Test
    public void testEqualsHashSection() {
        EqualsVerifier.forClass(Section.class).verify();
    }

    @Test
    public void testEqualsHashTable() {
        EqualsVerifier.forClass(Table.class).withIgnoredFields("caption", "requirements").verify();
    }

    @Test
    public void testEqualsHashSpecStruct() {
        EqualsVerifier.forClass(SpecificationStructure.class).verify();
    }
}
