#!/bin/bash

fn_print_dots "Login to get API key"

mkdir -p ${certsdir}

fn_fail_check ${installdir}/cockroach auth-session login ${DB_USER} \
--url ${DB_URL} --only-cookie > ${certsdir}/cloud_api_key

fn_print_ok "Success"

exitcode="0"
core_exit.sh
