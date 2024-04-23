package eu.dilcis.csip.structure;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import eu.dilcis.csip.profile.Appendix;
import eu.dilcis.csip.profile.ControlledVocabulary;
import eu.dilcis.csip.profile.ExternalSchema;
import eu.dilcis.csip.profile.MetsProfile;
import eu.dilcis.csip.structure.Source.SourceType;

final class MetsSource extends Source {
    static final MetsSource fromValues(final String name, final String heading, final String label,
            final String section,
            final Collection<MetsProfile> profiles) {
        return new MetsSource(name, heading, label, section, profiles);
    }

    private final String section;

    private final Collection<MetsProfile> profiles;

    private MetsSource(final String name, final String heading,
            final String label, final String section, final Collection<MetsProfile> profiles) {
        super(Source.SourceType.METS, name, heading, label);
        this.section = section;
        this.profiles = Collections.unmodifiableCollection(profiles);
    }

    @Override
    public void serialise(final Writer destination, final boolean isPdf) throws IOException {
        final Map<String, Object> context = new HashMap<>();
        context.put("label", label);
        context.put("name", name);
        if ("Appendix".equals(section)) {
            serialiseAppendices(destination, context);
        } else if ("external_schema".equals(section)) {
            serialiseExternalSchema(destination, context);
        } else if ("vocabulary".equals(section)) {
            serialiseVocabs(destination, context);
        } else if ("requirements".equals(section)) {
            for (MetsProfile profile : this.profiles) {
                context.put("profile", profile);
                context.put("requirements", profile.getRequirements());
                Utilities.serialiseToTemplate("eu/dilcis/csip/out/requirements.mustache", context,
                        destination);
            }
        }
    }

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();
        builder.append("MetsSource [");
        if (type != null)
            builder.append("type=").append(type).append(", ");
        if (name != null)
            builder.append("name=").append(name).append(", ");
        if (heading != null)
            builder.append("heading=").append(heading).append(", ");
        if (label != null)
            builder.append("label=").append(label).append(", ");
        if (section != null)
            builder.append("section=").append(section).append(", ");
        if (profiles != null)
            builder.append("profiles=").append(profiles);
        builder.append("]");
        return builder.toString();
    }

    private void serialiseAppendices(final Writer dest, final Map<String, Object> context) throws IOException {
        final List<Appendix> appendices = new ArrayList<>();
        for (final MetsProfile profile : this.profiles) {
            appendices.addAll(profile.getAppendices());
        }
        context.put("appendices", appendices);
        Utilities.serialiseToTemplate("eu/dilcis/csip/out/appendices.mustache", context, dest);
    }

    private void serialiseExternalSchema(final Writer dest, final Map<String, Object> context) throws IOException {
        final List<ExternalSchema> schema = new ArrayList<>();
        for (final MetsProfile profile : this.profiles) {
            schema.addAll(profile.getSchema());
        }
        context.put("schema", schema);
        Utilities.serialiseToTemplate("eu/dilcis/csip/out/schema.mustache", context, dest);
    }

    private void serialiseVocabs(final Writer dest, final Map<String, Object> context) throws IOException {
        final List<ControlledVocabulary> vocabularies = new ArrayList<>();
        for (final MetsProfile profile : this.profiles) {
            vocabularies.addAll(profile.getVocabularies());
        }
        context.put("vocabularies", vocabularies);
        Utilities.serialiseToTemplate("eu/dilcis/csip/out/vocabularies.mustache", context, dest);
    }
}
