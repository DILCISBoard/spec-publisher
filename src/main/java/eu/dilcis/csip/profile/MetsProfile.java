package eu.dilcis.csip.profile;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import eu.dilcis.csip.profile.Requirement.RequirementId;

public final class MetsProfile {
    public static final class Details {
        static Details fromValues(final URI uri, final String title) {
            return new Details(uri, title);
        }

        public final URI uri;

        public final String title;

        private Details(final URI uri, final String title) {
            this.uri = uri;
            this.title = title;
        }
    }

    public static MetsProfile fromValues(final Details details, final List<Details> relatedProfiles,
            final List<Requirement> requirements) {
        return new MetsProfile(details, relatedProfiles, requirements);
    }

    public final Details details;

    public final List<Details> relatedProfiles;

    private final List<RequirementId> orderedRequirements;
    private final Map<RequirementId, Requirement> requirements;

    private MetsProfile(final Details details, final List<Details> relatedProfiles,
            final List<Requirement> requirements) {
        this.details = details;
        this.relatedProfiles = relatedProfiles;
        this.orderedRequirements = requirements.stream().map(r -> r.id).collect(Collectors.toList());
        this.requirements = requirements.stream().collect(Collectors.toMap(r -> r.id, r -> r));
    }

    public final List<Requirement> getRequirements() {
        return orderedRequirements.stream().map(this.requirements::get).collect(Collectors.toList());
    }

    public final List<Requirement> getRequirementsBySection(final Section section) {
        return orderedRequirements.stream().map(this.requirements::get).filter(r -> section.equals(r.details.section))
                .collect(Collectors.toList());
    }

    public List<Section> getSections() {
        return orderedRequirements.stream().map(this.requirements::get).map(r -> r.details.section)
                .distinct().collect(Collectors.toList());
    }

    public final Requirement getRequirementById(final RequirementId id) {
        return this.requirements.get(id);
    }

    public final URI getUri() {
        return details.uri;
    }
}
