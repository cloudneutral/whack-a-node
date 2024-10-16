#!/bin/bash

format=$1

if [ $# -eq 0 ]; then
    format="json"
fi

case "$SECURITY_MODE" in
  secure)
    fn_fail_check ${installdir}/cockroach node status --all --url ${DB_URL} \
    --certs-dir=${certsdir} \
    --format ${format}
    ;;
  insecure)
    fn_fail_check ${installdir}/cockroach node status --all --url ${DB_URL} \
    --insecure \
    --format ${format}
    ;;
  *)
    echo "Bad security mode: $SECURITY_MODE"
    exit 1
esac
