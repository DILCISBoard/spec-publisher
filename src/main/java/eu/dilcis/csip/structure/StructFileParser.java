package eu.dilcis.csip.structure;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.yaml.snakeyaml.Yaml;

import eu.dilcis.csip.profile.MetsProfile;
import eu.dilcis.csip.structure.SpecificationStructure.Part;

public final class StructFileParser {
    public static StructFileParser parserInstance(final Collection<MetsProfile> profiles) {
        return new StructFileParser(profiles);
    }

    private final SourceFactory sectionFactory;

    private Path root;

    private StructFileParser(final Collection<MetsProfile> profiles) {
        this.sectionFactory = SourceFactory.getInstance(profiles);
    }

    public SpecificationStructure parseStructureFile(final Path source) throws ParseException {
        if (source == null) {
            throw new IllegalArgumentException("Parameter source is null");
        }
        if (!Files.exists(source) || Files.isDirectory(source)) {
            throw new IllegalArgumentException("File does not exist, or is a directory: " + source);
        }
        this.root = source.toFile().getParentFile().toPath();
        return this.fromSource(source);
    }

    public List<MetsProfile> getProfiles() {
        return new ArrayList<>(this.sectionFactory.profiles.values());
    }

    SpecificationStructure fromYamlStream(final InputStream source, final Path root) throws ParseException {
        if (source == null) {
            throw new IllegalArgumentException("Parameter source is null");
        }
        if (root == null || !Files.exists(root) || !Files.isDirectory(root)) {
            throw new IllegalArgumentException("Parameter root is null, or is not an existing directory.");
        }
        this.root = root;
        final Map<Part, List<Source>> content = new EnumMap<>(Part.class);
        for (final Entry<String, List<Map<String, Object>>> entryMap : this.parseYamlStream(source).entrySet()) {
            final Part part = Part.fromString(entryMap.getKey());
            final List<Source> sections = new ArrayList<>();
            for (final Map<String, Object> entry : entryMap.getValue()) {
                sections.add(this.sectionFactory.sectionFromMapEntry(this.root, entry));
            }
            content.put(part, sections);
        }
        return SpecificationStructure.fromContentMap(content);
    }

    private Map<String, List<Map<String, Object>>> parseYamlStream(final InputStream source) throws ParseException {
        try {
            final Map<String, List<Map<String, Object>>> mapList = new Yaml().load(source);
            if (mapList == null || mapList.isEmpty()) {
                throw new ParseException("No entries found in source Stream.");
            }
            return mapList;
        } catch (final ClassCastException e) {
            throw new ParseException("YAML stream is not a list of objects.", e);
        }
    }

    private SpecificationStructure fromSource(final Path source) throws ParseException {
        try {
            return this.fromYamlStream(Files.newInputStream(source), this.root);
        } catch (ParseException | IOException e) {
            throw new ParseException("Exception caught when parsing source: " + source, e);
        }
    }
}
