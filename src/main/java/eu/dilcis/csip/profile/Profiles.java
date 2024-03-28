package eu.dilcis.csip.profile;

import eu.dilcis.csip.profile.Requirement.RequirementId;

public class Profiles {
    private Profiles() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static final RequirementId requirementIdFromString(final String idString) {
        return RequirementId.fromIdString(idString);
    }
}
