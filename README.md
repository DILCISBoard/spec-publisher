# E-ARK Specification Publication

This repository contains a few tools used for the publication of E-ARK specifications:

- [A Java SAX based XML processor](./pom.xml) for METS profiles, which performs the following tasks:
  - [x] schema aware parsing and validation of the METS profile;
  - [x] for each `<structural_requirements>` sub-element of the profile generate:
    - a markdown table of the sections metadata requirements from the profile requirements to `<sub-element>/requirements.md`;
    - a markdown heading and code block for any XML examples referenced in the profile requirements to `<sub-element>/examples.md`;
  - [x] generates the following Appendices:
    - `vocabs.md` generated from the profile's `<controlled_vocabularies>` sub-elements;
    - `schema.md` generarated from the profile's `<external_schema>` elements;
    - `requirements.md`, a complete list of all profile requirements in a single table;
    - `examples.md` larger examples generated from the profile's `<Appendix>` elements;
  - [ ] checks for duplicate requirement ID allocation; and
  - [ ] reports gaps in the ID sequence (in preparation for deprecation reporting).
- [A Docker image](./docker/pandoc-md) for [Pandoc](https://pandoc.org/) v2.5, which is used in the E-ARK publication workflow. The [Pandoc version was released in November 2018](https://pandoc.org/releases.html#pandoc-2.5-2018-11-27). An upgrade to [v2.19](https://pandoc.org/releases.html#pandoc-2.19.2-2022-08-22) release in August 2022 might be a good idea. An upgrade to [v3 or higher](https://pandoc.org/releases.html#pandoc-3.0-2023-01-18) might require more work/fixing breaking changes.
- [Example metadata, templates and images](./pandoc/) to be used in the publication workflow.
- [The common introductory elements](./res/md/) used for all of the E-ARK specifications.
- [The metadata and images](./site/) required by the GitHub pages site and Jekyll build process:
  - [metadata reuired for navbar generation](site/_data);
  - [template html for header, footer and navbar](site/_includes);
  - [custom Jekyll page layouts](site/_layouts);
  - [stylesheets for the site](site/css); and
  - [logo images for the site and PDF](site/img).

## Overview

### Pre-requisites

The tools here are usually invoked as part of a publication workflow, e.g. from the [E-ARK CSIP project](https://github.com/DILCISBoard/E-ARK-CSIP). There are a few prerequisites for running the tools:

- [Java](https://www.java.com/) 8 or higher;
- [Maven](https://maven.apache.org/) 3.6.3 or higher; and
- [Docker](https://www.docker.com/).

As often as not these will need to be deployed on some kind of continuous integration environment. For purpsoes of the documentation examples we will be using [GitHub Actions](https://github.com/features/actions) workflows. The tools required are available/made available as required.

### Assumptions

There are a few assumptions regarding the publication workflow:

- the specification to be published is based around a [METS](https://www.loc.gov/standards/mets/) profile;
- the requirements, examples and appendices generated will be laid out in a speficic structure; and
- the generated site and PDF document will have the standard DILCIS look and feel, see <https://earkcsip.dilcis.eu/> and <https://earkcsip.dilcis.eu/pdf/eark-csip.pdf>.

All of the above can be subverted if required, but that's the path of least resistance.

### What it does

You can do the following:

- generate tables of requirements, example and appendices from a METS profile as GitHub flavoured Markdown;
- generated the same as Markdown suitable for conversion to PDF using Pandoc;
- use the templates and metadata to generate a GitHub pages site and PDF documents via the Pandoc Docker image.

## Typical Publication Process

1. Preparation
   Ensure that the specifcation is ready for publication and that details like the version number and publication date are as required.
2. Generated the pages site markdown from a METS profile
   This is typically done via the [spec-publisher](./pom.xml) Java project using the `master` branch, something like: `java -jar target/mets-profile-proc.jar -o ../profile/E-ARK-CSIP.xml`.
3. Create the pages site markdown
   Usually by running a Pandoc script using the Docker image, something like `docker run --rm -v "$PWD:/source" -u "$(id -u):$(id -g)" --entrypoint /source/create-site.sh eark4all/spec-pdf-publisher`.
4. Generate the PDF markdown from a METS profile
   This is typically done via the [spec-publisher](./pom.xml) Java project using the `feat/pdf-publication` branch, something like: `java -jar target/mets-profile-proc.jar -o ../profile/E-ARK-CSIP.xml`.
5. Create the PDF
   Usually by running a Pandoc script using the Docker image, something like `docker run --rm -v "$PWD:/source" -u "$(id -u):$(id -g)" --entrypoint /source/create-pdf.sh eark4all/spec-pdf-publisher`.
6. Use Jekyll to generate the website
   This is usually done using the GitHub pages Docker box, e.g. `docker run --rm -v "$PWD"/docs:/usr/src/app -v "$PWD"/_site:/_site -u "$(id -u):$(id -g)" starefossen/github-pages jekyll build -d /_site`, which uses the `./docs` directory as a source and generates a site in `./_site`.
7. Publish the generated site to GitHub.

## The spec-publisher Java Project

### Build from source

This is a [Java](https://www.java.com/) project and is built using [Maven](https://maven.apache.org/). You'll need a copy of this project sub-directory, from a git clone, `git clone https://github.com/DILCISBoard/E-ARK-CSIP.git` or a source package download.

Note that there are effectively 2 forks of this project one for bu
[source package download](https://github.com/DILCISBoard/E-ARK-CSIP/archive/master.zip).

From within this project sub-directory, e.g. `mets-profile-processor` issue the Maven command: `mvn clean package` to run tests and build.

### Class overview

It's just a basic SAX processor for the profile with some Markdown output.

#### `eu.dilcis.csip.MetsProfileProcessor`

Main entry point for fat JAR package, sequences parsing user input and running
the SAX handler.

#### `eu.dilcis.csip.ProcessorOptions`

Parses the `String` args array and records the user options in a dedicated
class.

#### `eu.dilcis.csip.profile.MetsProfileXmlHandler`

SAX event driven handler for METS Profile, parses `Requirements` lists from
Profile XML document.

#### `eu.dilcis.csip.OutputHandler`

Buffers XML element text and handles output (for now.....)

## ToDo ?

- [ ] Stronger data typing for [`eu.dilcis.csip.profile.MetsProfileXmlHandler.Requirement`](./src/main/java/eu/dilcis/csip/profile/MetsProfileXmlHandler.java)
- [ ] Requirement validation, e.g. non-empty fields etc.
- [ ] Group think for other validation activities.
- [x] Markdown table generation
- [ ] `index.md` file template selection
- [ ] `index.md` file template substitution
- [ ] Generalise vanilla METS Profile handling to base class
- [ ] fix SaxExceptions from OutputHandler class
