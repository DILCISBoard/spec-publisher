package eu.dilcis.csip.structure;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.yaml.snakeyaml.Yaml;

import eu.dilcis.csip.profile.Profiles;
import eu.dilcis.csip.profile.Requirement;
import eu.dilcis.csip.profile.Requirement.RequirementId;
import eu.dilcis.csip.structure.SpecificationStructure.Section;
import eu.dilcis.csip.structure.SpecificationStructure.SourceType;
import eu.dilcis.csip.structure.SpecificationStructure.Table;

final class StructFileParser {
    private final Map<RequirementId, Requirement> requirements;
    private Path root;

    private StructFileParser(final Map<RequirementId, Requirement> requirements) {
        this.requirements = Collections.unmodifiableMap(requirements);
    }

    SpecificationStructure parseStructureFile(final Path source) throws ParseException {
        if (source == null) {
            throw new IllegalArgumentException("Parameter source is null");
        }
        if (!Files.exists(source) || Files.isDirectory(source)) {
            throw new IllegalArgumentException("File does not exist, or is a directory: " + source);
        }
        this.root = source.getParent();
        return this.fromSource(source);
    }

    Table tableFromMap(final String name, final Map<String, Object> entryMap) throws ParseException {
        try {
            Path path = Files.createTempFile("table", "html");
            final String heading = (String) entryMap.get("heading");
            if (heading == null) {
                throw new ParseException("No heading found for table: " + name);
            }
            @SuppressWarnings("unchecked")
            final List<String> reqIds = (List<String>) entryMap.get("requirements");
            if (reqIds == null || reqIds.isEmpty()) {
                throw new ParseException("No requirements found for table: " + name);
            }
            return SpecificationStructure.tableFromValues(name, path, heading, getRequirementIds(reqIds));
        } catch (IOException e) {
            throw new ParseException("Exception caught when creating temporary file for table: " + name, e);
        } catch (ClassCastException e) {
            throw new ParseException("Invalid key value for table: " + name, e);
        }
    }

    private List<RequirementId> getRequirementIds(final List<String> reqIds) throws ParseException {
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

    private Section sectionFromMap(final String name, final SourceType type, final Map<String, Object> entryMap)
            throws ParseException {
        Path path = null;
        try {
            path = entryMap.get("path") != null ? this.root.resolve((String) entryMap.get("path"))
                    : null;
        } catch (final ClassCastException e) {
            throw new ParseException("Invalid path value for section: " + name, e);
        }
        if (path == null) {
            throw new ParseException("No path key found for section: " + name);
        }
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
            return this.tableFromMap(name, entryMap);
        }
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

    private SpecificationStructure fromSource(final Path source) throws ParseException {
        try {
            return this.fromYamlStream(Files.newInputStream(source), this.root);
        } catch (ParseException | IOException e) {
            throw new ParseException("Exception caught when parsing source: " + root, e);
        }
    }

    static StructFileParser parserInstance(final Map<RequirementId, Requirement> requirements) {
        return new StructFileParser(requirements);
    }

    static StructFileParser parserInstance(final List<Requirement> requirements) {
        return new StructFileParser(
                requirements.stream().collect(Collectors.toMap(Requirement::getId, Function.identity())));
    }
}
