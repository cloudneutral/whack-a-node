#!/bin/bash

fn_local_gen_haproxy() {
  case "$SH_MODE" in
    secure)
      fn_fail_check ${installdir}/cockroach gen haproxy --certs-dir=${certsdir} --host=${host}:${rpcportbase}
      ;;
    insecure)
      fn_fail_check ${installdir}/cockroach gen haproxy --insecure --host=${host}:${rpcportbase}
      ;;
  esac
}

if [ ! -f ${configdir}/haproxy.cfg ]; then
   fn_print_info "No ${configdir}/haproxy.cfg found - generating it"
   fn_local_gen_haproxy
fi

if [ -f .haproxy.pid ]; then
   fn_print_warn ".haproxy.pid found - already running?"
   exit 1
fi

fn_fail_check haproxy -D -f ${configdir}/haproxy.cfg -p .haproxy.pid

fn_print_ok "Started (pid: $(<.haproxy.pid))"