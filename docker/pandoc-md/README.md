Using Docker to Publish E-ARK Specifications
============================================

E-ARK-CSIP site tasks
----------------------

### Local Docker publication of GitHub pages
The markdown generation tasks have to have been run separately.

Run from the project docs folder:

```bash
docker run -it --rm -v "$PWD":/usr/src/app -p "4000:4000" -e JEKYLL_GITHUB_TOKEN=<gh-token-here>  starefossen/github-pages
```

E-ARK-CSIP PDF tasks
--------------------

### Creating the Docker image

### Publishing the PDF

```bash
docker run -it --rm -v "$PWD:/source" eark-pandoc ./pandoc-pre-post.sh
docker run -it --rm -v "$PWD:/source" eark-pandoc ./pandoc-gen-csip.sh
```
