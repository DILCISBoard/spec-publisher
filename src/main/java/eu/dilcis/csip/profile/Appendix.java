package eu.dilcis.csip.profile;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.xml.sax.Attributes;

public final class Appendix {
    static final class Builder extends XmlFragmentGenerator {
        private int number;
        private String label;

        Builder(final Attributes attributes) {
            this.number = Integer.valueOf(Utilities.getNumber(attributes));
            this.label = (Utilities.getLabel(attributes));
        }

        Builder number(final int number) {
            this.number = number;
            return this;
        }

        Builder label(final String label) {
            this.label = label;
            return this;
        }

        Appendix build() {
            return Appendix.fromValues(number, label, content);
        }
    }

    public static Appendix fromValues(final int number, final String label, final List<String> content) {
        return new Appendix(number, label, content);
    }

    public final int number;
    public final String label;
    public final List<String> content;

    private Appendix(final int number, final String label, final List<String> content) {
        this.number = number;
        this.label = label;
        this.content = Collections.unmodifiableList(content);
    }

    @Override
    public int hashCode() {
        return Objects.hash(number, label, content);
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj)
            return true;
        if (!(obj instanceof Appendix))
            return false;
        final Appendix other = (Appendix) obj;
        return number == other.number && Objects.equals(label, other.label) && Objects.equals(content, other.content);
    }
}
