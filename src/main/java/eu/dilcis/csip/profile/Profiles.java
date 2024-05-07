package eu.dilcis.csip.profile;

import eu.dilcis.csip.profile.Requirement.RequirementId;

public class Profiles {
    public static final RequirementId requirementIdFromString(final String idString) {
        return RequirementId.fromIdString(idString);
    }

    public static final MetsProfileParser metsProfileParser() {
        return MetsProfileParser.getInstance();
    }

    private Profiles() {
        throw new UnsupportedOperationException("Utility class");
    }
}
