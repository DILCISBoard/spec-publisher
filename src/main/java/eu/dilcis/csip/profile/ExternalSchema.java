package eu.dilcis.csip.profile;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * @author <a href="mailto:carl@openpreservation.org">Carl Wilson</a>
 *         <a href="https://github.com/carlwilson">carlwilson AT github</a>
 *
 * @version 0.1
 * 
 *          Created 19 Nov 2018:23:24:31
 */

public final class ExternalSchema {
    public static class Builder {
        private String nm = Constants.EMPTY;
        private URI rl = URI.create(Constants.DEFAULT_URI);
        private String cntxt = Constants.EMPTY;
        private List<String> nt = new ArrayList<>();

        public Builder() {
            super();
        }

        public Builder name(final String name) {
            this.nm = name;
            return this;
        }

        public Builder url(final URI url) {
            this.rl = url.normalize();
            return this;
        }

        public Builder url(final String url) {
            this.rl = URI.create(url);
            return this;
        }

        public Builder context(final String context) {
            this.cntxt = context;
            return this;
        }

        public Builder note(final String note) {
            this.nt.add(note);
            return this;
        }

        public Builder notes(final List<String> notes) {
            this.nt = new ArrayList<>(notes);
            return this;
        }

        public ExternalSchema build() {
            return new ExternalSchema(this.nm, this.rl, this.cntxt, this.nt);
        }
    }

    public final String name;
    public final URI url;
    public final String context;

    public final List<String> note;

    @Override
    public int hashCode() {
        return Objects.hash(name, url, context);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!(obj instanceof ExternalSchema))
            return false;
        ExternalSchema other = (ExternalSchema) obj;
        return Objects.equals(name, other.name) && Objects.equals(url, other.url)
                && Objects.equals(context, other.context);
    }

    ExternalSchema(final String name, final URI url, final String context,
            final List<String> note) {
        super();
        this.name = name;
        this.url = url;
        this.context = context;
        this.note = Collections.unmodifiableList(note);
    }
}
