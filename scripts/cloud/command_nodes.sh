#!/bin/bash

# This is pretty thin
#curl --location "https://cockroachlabs.cloud/api/v1/clusters/${CC_CLUSTERID}/nodes" \
#--header "Authorization: Bearer ${CC_API_KEY}"

if [ ! -f ${certsdir}/cloud_api_key ]; then
  fn_print_error "No API key found, run: ./cluster-admin login"
  exit 1
fi

apikey=$(<${certsdir}/cloud_api_key)

# Remove cookie prefix and suffix
apikey=$(echo ${apikey} | sed -e "s/^session=//" -e 's/;.*$//')

curl --connect-timeout 5 --max-time 5 --fail-with-body --insecure --request GET \
  --url "${ADMIN_URL}/api/v2/nodes/" \
  --header "X-Cockroach-API-Session: ${apikey}"

if [ $? -eq 0 ]; then
    exitcode="0"
else
    exitcode="1"
fi

core_exit.sh
