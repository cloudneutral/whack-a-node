#!/bin/bash

${installdir}/cockroach auth-session logout ${DB_USER} --url ${DB_URL}

fn_print_ok "Success"

exitcode="0"
core_exit.sh
