package eu.dilcis.csip.profile;

import org.junit.Test;

import nl.jqno.equalsverifier.EqualsVerifier;

import eu.dilcis.csip.profile.Requirement.RequirementId;

public class Requirement_EqualsTest {
    @Test
    public void testEqualsHashId() {
        EqualsVerifier.forClass(RequirementId.class).verify();
    }

    @Test
    public void testEqualsHashDetails() {
        EqualsVerifier.forClass(Requirement.Details.class).verify();
    }

    @Test
    public void testEqualsHashRequirement() {
        EqualsVerifier.forClass(Requirement.class).verify();
    }
}
