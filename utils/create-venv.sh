#!/usr/bin/env bash
# Conditional check to see if markdownPP is installed
# Acknowledgements to https://stackoverflow.com/questions/592620/how-to-check-if-a-program-exists-from-a-bash-script
command -v markdown-pp >/dev/null 2>&1 || {
  # IF no markdown-pp command
  # Grab the temp directory name
  # Acknowledgements to https://unix.stackexchange.com/questions/174817/finding-the-correct-tmp-dir-on-multiple-platforms
  tmpdir=$(dirname "$(mktemp -u)")
  if [ ! -d "$tmpdir/.venv-markdown/" ]
  then
    # IF no virtualenv exists
    virtualenv -p python "$tmpdir/.venv-markdown"
    # shellcheck source=/tmp/.venv-markdown/bin/activate
    source "$tmpdir/.venv-markdown/bin/activate"
    pip install markdownPP
  fi
}
