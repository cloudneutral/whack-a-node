#!/bin/bash

#if [ ! -f ${rootdir}/init.sql ]; then
#  cat  ${configdir}/init.sql > ${rootdir}/init.sql
#fi

case "$SECURITY_MODE" in
  secure)
    ${installdir}/cockroach init --certs-dir=${certsdir} --host=${host}:${rpcportbase}

    ${installdir}/cockroach sql --certs-dir=${certsdir} --host=${host}:${sqlportbase} \
    -e "CREATE USER IF NOT EXISTS ${DB_USER} WITH PASSWORD '${DB_PASSWORD}'"

    ${installdir}/cockroach sql --certs-dir=${certsdir} --host=${host}:${sqlportbase} \
    -e "ALTER ROLE ${DB_USER} WITH PASSWORD '${DB_PASSWORD}'; GRANT ADMIN to ${DB_USER};"

    ${installdir}/cockroach sql --certs-dir=${certsdir} --host=${host}:${sqlportbase} < ${configdir}/init.sql

    ;;
  insecure)
    ${installdir}/cockroach init --insecure --host=${host}:${rpcportbase}

    ${installdir}/cockroach sql --insecure --host=${host}:${sqlportbase} \
    -e "CREATE USER IF NOT EXISTS ${DB_USER}; GRANT ADMIN to ${DB_USER};"

    ${installdir}/cockroach sql --insecure --host=${host}:${sqlportbase} \
    -e "ALTER ROLE ${DB_USER} WITH PASSWORD NULL"

    ${installdir}/cockroach sql --insecure --host=${host}:${sqlportbase} < ${configdir}/init.sql

    ;;
  *)
    echo "Bad security mode: $SECURITY_MODE"
    exit 1
esac

fn_print_ok