#!/bin/bash

${installdir}/cockroach node status --all --url ${DB_URL} --format json

exitcode="0"
core_exit.sh
