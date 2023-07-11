# Docker Image for Pandoc

This [`Dockerfile`](./Dockerfile) builds a Docker image for [Pandoc](https://pandoc.org/) v2.5, which is used in the E-ARK publication workflow. The [Pandoc version was released in November 2018](https://pandoc.org/releases.html#pandoc-2.5-2018-11-27). An upgrade to [v2.19](https://pandoc.org/releases.html#pandoc-2.19.2-2022-08-22) release in August 2022 might be a good idea. An upgrade to [v3 or higher](https://pandoc.org/releases.html#pandoc-3.0-2023-01-18) might require more work/fixing breaking changes.

The Docker image DOES NOT come with the `spec-publisher` project installed, which is confusing and a historical hangover. The Docker image is used to assemble the collateral produced by the `spec-publisher` project and execute Pandoc on them.

## How it works

It's a little rough and ready for now. The box simply provides a working installation of Pandoc v2.5 and a `bash` entrypoint for running code. The base image is [`debian:stable`](https://hub.docker.com/_/debian) from the offical Debian Docker Hub repository. This is a little large and better options are available.

Generally the box will be invoked as part of a publication workflow, e.g. from the `spec-publisher` project. The box is invoked with a `bash` command, which is usually a script, passed to the image `ENTRYPOINT`.
