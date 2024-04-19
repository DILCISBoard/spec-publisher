package eu.dilcis.csip.structure;

import org.junit.Test;

import nl.jqno.equalsverifier.EqualsVerifier;

public class StructTypesTest {
    @Test
    public void testEqualsHashSection() {
        EqualsVerifier.forClass(Section.class).verify();
    }

    @Test
    public void testEqualsHashTable() {
        EqualsVerifier.forClass(Table.class).withIgnoredFields("caption", "requirements", "examples").verify();
    }

    @Test
    public void testEqualsHashSpecStruct() {
        EqualsVerifier.forClass(SpecificationStructure.class).verify();
    }
}
