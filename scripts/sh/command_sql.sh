#!/bin/bash

port=$1

if [ $# -eq 0 ]; then
    fn_local_select_port
    port=$option
fi

case "$SH_MODE" in
  secure)
    fn_fail_check ${installdir}/cockroach sql \
    --host=${host} --port=${port} \
    --certs-dir=${certsdir}
    ;;
  insecure)
    fn_fail_check ${installdir}/cockroach sql \
      --host=${host} --port=${port} \
      --insecure
    ;;
esac
