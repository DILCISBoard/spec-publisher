package eu.dilcis.csip.profile;

import java.util.List;
import java.util.Objects;

import org.xml.sax.Attributes;

public final class Example {
    static class Builder extends XmlFragmentGenerator {
        private String id;
        private String label;

        Builder(final Attributes attributes) {
            this.id = Utilities.getId(attributes);
            this.label = Utilities.getLabel(attributes);
        }

        @Override
        public int hashCode() {
            return Objects.hash(id, label);
        }

        @Override
        public boolean equals(final Object obj) {
            if (this == obj)
                return true;
            if (!(obj instanceof Builder))
                return false;
            final Builder other = (Builder) obj;
            return Objects.equals(id, other.id) && Objects.equals(label, other.label);
        }

        Builder id(final String id) {
            this.id = id;
            return this;
        }

        Builder label(final String label) {
            this.label = label;
            return this;
        }

        Example build() {
            return Example.fromValues(id, label, content);
        }
    }

    static Example fromValues(final String id, final String label, final List<String> content) {
        return new Example(id, label, content);
    }

    final String id;

    final String label;

    final List<String> content;

    private Example(final String id, final String label, final List<String> content) {
        this.id = id;
        this.label = label;
        this.content = content;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, label, content);
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj)
            return true;
        if (!(obj instanceof Example))
            return false;
        final Example other = (Example) obj;
        return Objects.equals(id, other.id) && Objects.equals(label, other.label)
                && Objects.equals(content, other.content);
    }
}
