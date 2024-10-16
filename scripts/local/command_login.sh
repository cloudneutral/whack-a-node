#!/bin/bash

mkdir -p ${datadir}

case "$SECURITY_MODE" in
  secure)
    fn_fail_check ${installdir}/cockroach auth-session login ${DB_USER} \
    --url ${DB_URL} \
    --only-cookie \
    --certs-dir=${certsdir} > ${certsdir}/local_api_key
    ;;
  insecure)
    fn_fail_check ${installdir}/cockroach auth-session login ${DB_USER} \
    --url ${DB_URL} \
    --only-cookie \
    --insecure > ${certsdir}/local_api_key
    ;;
  *)
    echo "Bad security mode: $SECURITY_MODE"
    exit 1
esac

cat "${certsdir}/local_api_key"
