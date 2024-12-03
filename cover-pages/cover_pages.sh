#!/usr/bin/env bash
SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
cd "$SCRIPT_DIR" || exit

for FILE in metadata/*.md; do
    echo "Processing $FILE"
    ###
    # Pandoc options:
    # --from markdown \                               # Source fromat Markdown
    # --template ../pandoc/templates/eisvogel.latex \ # Use this latex template
    # --listings \                                    # Use listings package for code blocks
    # --table-of-contents \                           # Generate table of contents
    # --metadata-file ../pandoc/metadata.yaml \       # Additional Pandoc metadata
    # --include-before-body "../spec-publisher/res/md/common-intro.md" \
    # --include-after-body "../specification/postface/postface.md" \
    # --number-sections \                             # Generate Heading Numbers
    # eark-sip-pdf.md \                              # Input Markdown file
    # -o ./pdf/eark-csip.pdf                          # PDF Destinaton
    echo "PANDOC: Generating PDF document from markdown"
    pandoc  --from markdown \
            --template "$SCRIPT_DIR/eisvogel.latex" \
            --metadata-file metadata.yaml \
            "$FILE" \
            -o "${FILE%.md}.pdf"
    echo "PANDOC: Finished"
done