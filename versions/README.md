Version codes
=============

This directory contains utilities to publish information on released versions of the application.

The file `version_codes.json` is a simple json files with the following keys:

    * "latest": a String value with the version name of the latest release
    * "version_codes":  a Map<String, Long> of version name to the corresponding version_codes

`version_codes.json` should be updated with each release by using the `update_version_codes.sh` script
