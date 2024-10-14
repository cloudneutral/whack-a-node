#!/bin/bash

port=$1

if [ $# -eq 0 ]; then
    fn_local_select_rpc_port
    port=$option
fi

case "$SH_MODE" in
  secure)
    fn_fail_check ${installdir}/cockroach node decommission ${node} --host ${host}:${port} --certs-dir=${certsdir}
    ;;
  insecure)
    fn_fail_check ${installdir}/cockroach node decommission ${node} --host ${host}:${port} --insecure
    ;;
esac

fn_print_ok "Node ${node} on RPC port ${port} decommissioned!"