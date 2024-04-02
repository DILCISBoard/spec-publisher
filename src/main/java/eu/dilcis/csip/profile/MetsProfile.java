package eu.dilcis.csip.profile;

import java.net.URI;
import java.util.List;
import java.util.Map;

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
    public static MetsProfile fromValues(final Details details, final List<Details> relatedProfiles, final Map<RequirementId, Requirement> requirements) {
        return new MetsProfile(details, relatedProfiles, requirements);
    }
    public final Details details;

    public final List<Details> relatedProfiles;

    public final Map<RequirementId, Requirement> requirements;
    private MetsProfile(final Details details, final List<Details> relatedProfiles, final Map<RequirementId, Requirement> requirements) {
        this.details = details;
        this.relatedProfiles = relatedProfiles;
        this.requirements = requirements;
    }
}
