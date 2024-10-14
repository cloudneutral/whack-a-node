#!/bin/bash

case "$SH_MODE" in
  secure)
    ${installdir}/cockroach init --certs-dir=${certsdir} --host=${host}:${rpcportbase}

    ${installdir}/cockroach sql --certs-dir=${certsdir} --host=${host}:${sqlportbase} \
    -e "CREATE USER IF NOT EXISTS craig WITH PASSWORD 'cockroach'"

    ${installdir}/cockroach sql --certs-dir=${certsdir} --host=${host}:${sqlportbase} \
    -e "ALTER ROLE craig WITH PASSWORD 'cockroach'; GRANT ADMIN to craig;"

    if [ -f "${configdir}/init.sql" ]; then
       ${installdir}/cockroach sql --certs-dir=${certsdir} --host=${host}:${sqlportbase} < ${configdir}/init.sql
    fi

    ;;
  insecure)
    ${installdir}/cockroach init --insecure --host=${host}:${rpcportbase}

    ${installdir}/cockroach sql --insecure --host=${host}:${sqlportbase} \
    -e "CREATE USER IF NOT EXISTS craig; GRANT ADMIN to craig;"

    ${installdir}/cockroach sql --insecure --host=${host}:${sqlportbase} \
    -e "ALTER ROLE craig WITH PASSWORD NULL"

    if [ -f "${configdir}/init.sql" ]; then
       ${installdir}/cockroach sql --insecure --host=${host}:${sqlportbase} < ${configdir}/init.sql
    fi

    ;;
esac


fn_print_ok