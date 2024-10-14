#!/bin/bash

fn_fail_check ${installdir}/cockroach sql --url ${DB_URL}

exitcode="0"
core_exit.sh
