#!/usr/bin/env bash

/usr/bin/snc $@ -o /tmp/output.js > /dev/null
node /tmp/output.js