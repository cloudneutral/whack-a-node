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

  if [ "$option" == "cloud" ]; then
    read -p "Enter your cluster ID [empty]: " CC_CLUSTERID
    read -p "Enter path to local CA certificate [empty]: " CC_SSL_ROOT_CERT
    read -p "Enter API secret key [empty]: " CC_API_KEY
    read -p "Enter DB Console URL [empty]: " ADMIN_URL
    read -p "Enter regional database host [empty]: " DB_HOST
    read -p "Enter database name [defaultdb]: " DB_PASSWORD
    read -p "Enter SQL user name [craig]: " DB_USER
    read -p "Enter SQL user password [cockroach]: " DB_PASSWORD

    echo "CC_CLUSTERID=\"${CC_CLUSTERID}\"" >> "${rootdir}/settings.cfg"
    echo "CC_SSL_ROOT_CERT=\"${CC_SSL_ROOT_CERT}\"" >> "${rootdir}/settings.cfg"
    echo "CC_API_KEY=\"${CC_API_KEY}\"" >> "${rootdir}/settings.cfg"
    echo "ADMIN_URL=\"${ADMIN_URL}\"" >> "${rootdir}/settings.cfg"
    echo "DB_HOST=\"${DB_HOST}\"" >> "${rootdir}/settings.cfg"
    echo "DB_NAME=\"${DB_NAME:-defaultdb}\"" >> "${rootdir}/settings.cfg"
    echo "DB_USER=\"${DB_USER:-craig}\"" >> "${rootdir}/settings.cfg"
    echo "DB_PASSWORD=\"${DB_PASSWORD:-cockroach}\"" >> "${rootdir}/settings.cfg"
  fi

  echo -e "Done!"
  echo -e "Edit or delete the 'settings.cfg' file to change these settings."
  echo -e "See README.md for how to configure other settings."

  exit 0
fi

source "${rootdir}/settings.cfg"
source "${configdir}/settings-${SECURITY_MODE}.cfg"
source "${configdir}/settings-local.cfg"
source "${configdir}/settings-service.cfg"
