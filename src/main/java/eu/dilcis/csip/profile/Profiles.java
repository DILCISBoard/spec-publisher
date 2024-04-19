package eu.dilcis.csip.profile;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import eu.dilcis.csip.profile.Requirement.RequirementId;

public class Profiles {
    public static final RequirementId requirementIdFromString(final String idString) {
        return RequirementId.fromIdString(idString);
    }

    public static final MetsProfileParser metsProfileParser() {
        return MetsProfileParser.getInstance();
    }

    public static final Set<Example> examplesFromRequirments(final Collection<MetsProfile> profiles,
            final List<RequirementId> requirements) {
        final Set<Example> examples = new HashSet<>();
        for (final RequirementId requirementId : requirements) {
            for (final MetsProfile profile : profiles) {
                if (profile.getRequirementById(requirementId) != null) {
                    Requirement requirement = profile.getRequirementById(requirementId);
                    for (final String exampleId : requirement.examples) {
                        examples.add(profile.getExampleById(exampleId));
                    }
                    break;
                }
            }
        }
        return examples;
    }

    private Profiles() {
        throw new UnsupportedOperationException("Utility class");
    }
}
