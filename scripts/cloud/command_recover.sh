#!/bin/bash

curl --fail-with-body --request PUT \
  --url "https://cockroachlabs.cloud/api/v1/clusters/${CC_CLUSTERID}/disrupt" \
  --header "Authorization: Bearer ${CC_API_KEY}"

if [ $? -eq 0 ]; then
    fn_print_ok "The request was successful"
    exitcode="0"
else
    fn_print_error "There was an error: $?"
    exitcode="1"
fi

core_exit.sh