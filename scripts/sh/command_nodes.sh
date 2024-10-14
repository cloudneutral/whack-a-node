#!/bin/bash

case "$SH_MODE" in
  secure)
    apikey=$(<${datadir}/.local_api_key)
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
esac

if [ $? -eq 0 ]; then
    exitcode="0"
else
    exitcode="1"
fi

core_exit.sh



