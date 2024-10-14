#!/bin/bash

case "$SH_MODE" in
  secure)
    fn_fail_check ${installdir}/cockroach auth-session login ${DB_USER} \
    --url ${DB_URL} \
    --only-cookie \
    --certs-dir=${certsdir} > ${datadir}/.local_api_key
    ;;
  insecure)
    fn_fail_check ${installdir}/cockroach auth-session login ${DB_USER} \
    --url ${DB_URL} \
    --only-cookie \
    --insecure > ${datadir}/.local_api_key
    ;;
esac

cat "${datadir}/.local_api_key"
