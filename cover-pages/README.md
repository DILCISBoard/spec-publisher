Creating Cover Pages
====================

Used to run off cover pages for other specifcations outside of full automatic publication.

The metadata files with titles, versions and dates are in `./metadata`. One PDF cover page file is generated per metadata file. The
`cover_pages.sh` script is used to generate the cover pages using the Docker image:

```bash
docker run --rm -v "$PWD:/source" -u $(id -u ${USER}):$(id -g ${USER}) --entrypoint /source/cover_pages.sh eark4all/spec-pdf-publisher
```

A bit hacky but it works for a one off job.