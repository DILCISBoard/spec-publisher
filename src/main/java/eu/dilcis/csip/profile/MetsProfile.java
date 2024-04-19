package eu.dilcis.csip.profile;

import java.net.URI;
import java.util.Collections;
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
            final List<Requirement> requirements, final Map<String, Example> examples,
            final List<Appendix> appendices, final List<ExternalSchema> schemas, final List<ControlledVocabulary> vocabularies) {
        return new MetsProfile(details, relatedProfiles, requirements, examples, appendices, schemas, vocabularies);
    }

    public final Details details;

    public final List<Details> relatedProfiles;

    private final List<RequirementId> orderedRequirements;
    private final Map<RequirementId, Requirement> requirements;
    private final Map<String, Example> examples;
    private final List<Appendix> appendices;
    private final List<ExternalSchema> schemas;
    private final List<ControlledVocabulary> vocabularies;

    private MetsProfile(final Details details, final List<Details> relatedProfiles,
            final List<Requirement> requirements, final Map<String, Example> examples, final List<Appendix> appendices,
            final List<ExternalSchema> schemas, final List<ControlledVocabulary> vocabularies) {
        this.details = details;
        this.relatedProfiles = relatedProfiles;
        this.orderedRequirements = requirements.stream().map(r -> r.id).collect(Collectors.toList());
        this.requirements = requirements.stream().collect(Collectors.toMap(r -> r.id, r -> r));
        this.examples = examples;
        this.appendices = appendices;
        this.schemas = schemas;
        this.vocabularies = vocabularies;
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

    public final Example getExampleById(final String id) {
        return this.examples.get(id);
    }

    public final URI getUri() {
        return details.uri;
    }

    public List<Appendix> getAppendices() {
        return Collections.unmodifiableList(this.appendices);
    }

    public List<ExternalSchema> getSchema() {
        return Collections.unmodifiableList(this.schemas);
    }

    public List<ControlledVocabulary> getVocabularies() {
        return Collections.unmodifiableList(this.vocabularies);
    }
}
