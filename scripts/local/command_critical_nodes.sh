#!/bin/bash

if [ ! -f ${certsdir}/local_api_key ]; then
  fn_print_error "No API key found, run: ./cluster-admin login"
  exit 1
fi

apikey=$(<${certsdir}/local_api_key)

case "$SECURITY_MODE" in
  secure)
    # Doesnt work (401)
    curl --fail-with-body --insecure --request POST \
    --url "${ADMIN_URL}/_status/critical_nodes" \
    --cookie "${apikey}"
    ;;
  insecure)
    curl --fail-with-body --request POST \
    --url "${ADMIN_URL}/_status/critical_nodes"
    ;;
  *)
    echo "Bad security mode: $SECURITY_MODE"
    exit 1
esac

if [ $? -eq 0 ]; then
    fn_print_ok "The request was successful"
    exitcode="0"
else
    fn_print_error "There was an error: $?"
    exitcode="1"
fi

core_exit.sh



