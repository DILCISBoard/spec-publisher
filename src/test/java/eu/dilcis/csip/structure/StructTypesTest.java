package eu.dilcis.csip.structure;

import org.junit.Test;

import nl.jqno.equalsverifier.EqualsVerifier;

public class StructTypesTest {
    @Test
    public void testEqualsHashSection() {
        EqualsVerifier.forClass(Source.class).verify();
    }

    @Test
    public void testEqualsHashTable() {
        EqualsVerifier.forClass(RequirementsSource.class).withIgnoredFields("requirements", "examples")
                .verify();
    }

    @Test
    public void testEqualsHashSpecStruct() {
        EqualsVerifier.forClass(SpecificationStructure.class).verify();
    }
}
