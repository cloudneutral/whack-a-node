#!/bin/bash

if [ ! -f ${rootdir}/settings.cfg ]; then
  echo -e "#########################"
  echo -e "# Welcome to whack-a-node"
  echo -e "#########################"
  echo -e ""
  echo -e "Deployment modes include:"
  echo -e "cloud     - CockroachDB Cloud Dedicated/Standard"
  echo -e "secure    - CockroachDB local cluster in secure mode"
  echo -e "insecure  - CockroachDB local cluster in insecure mode"
  echo -e ""

  PS3='Please select CockroachDB cluster deployment mode: '

  select option in "cloud" "secure" "insecure" "<Exit>" ;  do
    case $option in
      "<Exit>")
        exit 0
        ;;
      "cloud")
        echo "DEPLOY_MODE=\"cloud\"" > "${rootdir}/settings.cfg"
        break
        ;;
      "secure"|"insecure")
        echo "DEPLOY_MODE=\"local\"" > "${rootdir}/settings.cfg"
        break
        ;;
    esac
  done

  echo "SECURITY_MODE=\"${option}\"" >> "${rootdir}/settings.cfg"
  echo "ansi=\"on\"" >> "${rootdir}/settings.cfg"

  echo -e "Edit or delete the 'settings.cfg' file to change these settings."
  echo -e "Configure connection parameters in 'config/settings-${option}.cfg'."
  exit 0
fi

source "${rootdir}/settings.cfg"
source "${configdir}/settings-${SECURITY_MODE}.cfg"
source "${configdir}/settings-local.cfg"
source "${configdir}/settings-service.cfg"
