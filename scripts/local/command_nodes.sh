#!/bin/bash

case "$SECURITY_MODE" in
  secure)
    if [ ! -f ${certsdir}/local_api_key ]; then
      fn_print_error "No API key found, run: ./cluster-admin login"
      exit 1
    fi

    apikey=$(<${certsdir}/local_api_key)
    # Remove cookie prefix and suffix
    apikey=$(echo ${apikey} | sed -e "s/^session=//" -e 's/;.*$//')

    curl --connect-timeout 5 --max-time 5 --fail-with-body --insecure --request GET \
    --url "${ADMIN_URL}/api/v2/nodes/" \
    --header "X-Cockroach-API-Session: ${apikey}"
    ;;
  insecure)
    curl --connect-timeout 5 --max-time 5 --fail-with-body --request GET \
    --url "${ADMIN_URL}/api/v2/nodes/"
    ;;
  *)
    echo "Bad security mode: $SECURITY_MODE"
    exit 1
esac

if [ $? -eq 0 ]; then
    exitcode="0"
else
    exitcode="1"
fi

core_exit.sh



