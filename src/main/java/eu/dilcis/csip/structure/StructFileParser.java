package eu.dilcis.csip.structure;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.yaml.snakeyaml.Yaml;

import eu.dilcis.csip.profile.MetsProfile;
import eu.dilcis.csip.profile.Profiles;
import eu.dilcis.csip.profile.Requirement.RequirementId;
import eu.dilcis.csip.structure.SpecificationStructure.Section;
import eu.dilcis.csip.structure.SpecificationStructure.SourceType;
import eu.dilcis.csip.structure.SpecificationStructure.Table;

public final class StructFileParser {
    public static StructFileParser parserInstance(final Collection<MetsProfile> profiles) {
        return new StructFileParser(profiles);
    }
    static Table tableFromMap(final Path root, final String name, final Map<String, Object> entryMap) throws ParseException {
        try {
            final String heading = stringFromMap(name, "heading", entryMap);
            final Path path = pathFromMap(root, name, entryMap);
            if (Files.isDirectory(path)) {
                throw new ParseException("Target file is a directory: " + path + " for table: " + name);
            }
            @SuppressWarnings("unchecked")
            final List<String> reqIds = (List<String>) entryMap.get("requirements");
            if (reqIds == null || reqIds.isEmpty()) {
                throw new ParseException("No requirements found for table: " + name);
            }
            return SpecificationStructure.tableFromValues(name, path, heading, getRequirementIds(reqIds));
        } catch (final ClassCastException e) {
            throw new ParseException("Invalid key value for table: " + name, e);
        } catch (final NoSuchElementException e) {
            throw new ParseException("Not all requirements found in profiles for table: " + name, e);
        }
    }

    private static List<RequirementId> getRequirementIds(final List<String> reqIds) throws ParseException {
        final List<RequirementId> ids = new ArrayList<>();
        for (final String reqId : reqIds) {
            if (reqId == null || reqId.isEmpty()) {
                throw new ParseException("Invalid requirement id: " + reqId);
            }
            final RequirementId id = Profiles.requirementIdFromString(reqId);
            if (id == null) {
                throw new ParseException("Invalid requirement id: " + reqId);
            }
            ids.add(id);
        }
        return ids;
    }

    private static Path pathFromMap(final Path root, final String name, final Map<String, Object> entryMap) throws ParseException {
        try {
            return root.resolve(stringFromMap(name, "path", entryMap));
        } catch (final InvalidPathException e) {
            throw new ParseException("Invalid path value for section: " + name, e);
        }
    }

    private static String stringFromMap(final String name, final String key, final Map<String, Object> entryMap)
            throws ParseException {
        try {
            final String value = (String) entryMap.get(key);
            if (value == null) {
                throw new ParseException("No key:" + key + " found for entry:" + name);
            }
            return value;
        } catch (final ClassCastException e) {
            throw new ParseException("Invalid value for key:" + key + " found for entry:" + name, e);
        }
    }

    private final Map<URI, MetsProfile> profiles;

    private Path root;

    private StructFileParser(final Collection<MetsProfile> profiles) {
        this.profiles = profiles.stream().collect(Collectors.toMap(MetsProfile::getUri, Function.identity()));
    }

    public SpecificationStructure parseStructureFile(final Path source) throws ParseException {
        if (source == null) {
            throw new IllegalArgumentException("Parameter source is null");
        }
        if (!Files.exists(source) || Files.isDirectory(source)) {
            throw new IllegalArgumentException("File does not exist, or is a directory: " + source);
        }
        this.root = source.getParent();
        return this.fromSource(source);
    }

    SpecificationStructure fromYamlStream(final InputStream source, final Path root) throws ParseException {
        if (source == null) {
            throw new IllegalArgumentException("Parameter source is null");
        }
        if (root == null || !Files.exists(root) || !Files.isDirectory(root)) {
            throw new IllegalArgumentException("Parameter root is null, or is not an existing directory.");
        }
        this.root = root;
        final List<SpecificationStructure.Section> sections = new ArrayList<>();
        for (final Map<String, Object> entryMap : this.parseYamlStream(source)) {
            sections.add(this.sectionFromMapEntry(entryMap));
        }
        return SpecificationStructure.fromSections(sections);
    }

    private Section sectionFromMap(final String name, final SourceType type, final Map<String, Object> entryMap)
            throws ParseException {
        final Path path = pathFromMap(this.root, name, entryMap);
        if (!Files.exists(path) || Files.isDirectory(path)) {
            throw new ParseException("Source file does not exist, or is a directory: " + path);
        }
        return SpecificationStructure.sectionFromValues(name, path, type);
    }

    private Section sectionFromMapEntry(final Map<String, Object> entryMap) throws ParseException {
        final String name = (String) entryMap.get("name");
        if (name == null) {
            throw new ParseException("No name found for entry.");
        }
        final SourceType type = SourceType.fromString((String) entryMap.get("type"));
        if (type == null) {
            throw new ParseException("No type found for entry: " + name);
        }
        if (SourceType.TABLE.equals(type)) {
            return tableFromMap(this.root, name, entryMap);
        }
        if (SourceType.METS.equals(type)) {
            return this.sectionFromMetsMapEntry(name, type, entryMap);
        }
        return this.sectionFromMap(name, type, entryMap);
    }

    private Section sectionFromMetsMapEntry(final String name, final SourceType type,
            final Map<String, Object> entryMap) throws ParseException {
        return this.sectionFromMap(name, type, entryMap);
    }

    private List<Map<String, Object>> parseYamlStream(final InputStream source) throws ParseException {
        try {
            final List<Map<String, Object>> mapList = new Yaml().load(source);
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
            throw new ParseException("Exception caught when parsing source: " + root, e);
        }
    }
}
