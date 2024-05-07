package eu.dilcis.csip.profile;

import java.util.List;

import org.xml.sax.Attributes;

public final class Example {
    final String id;
    final String label;
    final List<String> content;

    private Example(String id, String label, List<String> content) {
        this.id = id;
        this.label = label;
        this.content = content;
    }

    static Example fromValues(String id, String label, List<String> content) {
        return new Example(id, label, content);
    }

    static class Builder extends XmlFragmentGenerator {
        private String id;
        private String label;

        Builder(Attributes attributes) {
            this.id = Utilities.getId(attributes);
            this.label = (Utilities.getLabel(attributes));
        }

        Builder id(String id) {
            this.id = id;
            return this;
        }

        Builder label(String label) {
            this.label = label;
            return this;
        }

        Example build() {
            return Example.fromValues(id, label, content);
        }
    }
}
