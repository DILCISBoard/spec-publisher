package eu.dilcis.csip.structure;

import java.io.IOException;
import java.io.Writer;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import eu.dilcis.csip.profile.Example;
import eu.dilcis.csip.profile.Requirement;

final class RequirementsSource extends Source {
    static final RequirementsSource fromValues(final String name, final String heading,
            final List<Requirement> requirements, final Set<Example> examples) {
        return new RequirementsSource(name, heading, requirements, examples);
    }

    public final List<Requirement> requirements;

    public final Set<Example> examples;

    private RequirementsSource(final String name, final String heading,
            final List<Requirement> requirements, final Set<Example> examples) {
        super(Source.SourceType.REQUIREMENTS, name, heading, null);
        this.requirements = Collections.unmodifiableList(requirements);
        this.examples = Collections.unmodifiableSet(examples);
    }

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();
        builder.append("RequirementsSource [");
        if (type != null)
            builder.append("type=").append(type).append(", ");
        if (name != null)
            builder.append("name=").append(name).append(", ");
        if (heading != null)
            builder.append("heading=").append(heading).append(", ");
        if (label != null)
            builder.append("label=").append(label);
        builder.append("]");
        return builder.toString();
    }

    @Override
    public void serialise(final Writer destination, final boolean isPdf) throws IOException {
        Utilities.serialiseToTemplate("eu/dilcis/csip/out/table_markdown.mustache",
                Map.of("requirements", this.requirements, "caption", this.heading, "examples", this.examples, "pdf",
                        isPdf),
                destination);
    }
}