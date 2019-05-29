#!/usr/bin/env bash
if [ ! -d .venv/markdown/ ]
then
  virtualenv -p python3 .venv/markdown
  source .venv/markdown/bin/activate
  pip install markdownPP
  deactivate
fi
