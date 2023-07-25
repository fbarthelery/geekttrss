#!/bin/zsh
set -e

zmodload zsh/zutil

script_path="${0}"
script_root="$(dirname "${script_path}")"
version_file="$script_root/version_codes.json"

usage() {
  echo "update_version_codes.sh [-l|--latest] <version_name> <version_code>"
}

add_code_mapping() {
  local name="$1"
  local code="$2"
  local version_file="$3"
  local tmp=$(mktemp)
  jq --arg param_version_name "$name" \
    --arg param_version_code "$code" \
    '.version_codes[$param_version_name] = $param_version_code' \
    "$version_file" > "$tmp"
  mv "$tmp" "$version_file"
}

add_code_mapping_update_latest() {
  local name="$1"
  local code="$2"
  local version_file="$3"
  local tmp=$(mktemp)
  jq --arg param_version_name "$name" \
    --arg param_version_code "$code" \
    '.latest = $param_version_name | .version_codes[$param_version_name] = $param_version_code' \
    "$version_file" > "$tmp"
  mv "$tmp" "$version_file"
}

zparseopts -D -F -K -- \
  {h,-help}=show_help \
  {l,-latest}=set_latest || {
    usage && return 1
  }

[[ -z "$show_help" ]] || { usage && return }
version_name="$1"
version_code="$2"

[[ -z "$version_name" || -z "$version_code" ]] && { usage && return 1 }

if [[ -n "$set_latest" ]] ; then
  add_code_mapping_update_latest "$version_name" "$version_code" "$version_file"
else
  add_code_mapping "$version_name" "$version_code" "$version_file"
fi
