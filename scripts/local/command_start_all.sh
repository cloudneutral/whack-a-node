#!/bin/bash

node=0;
let port1=${rpcportbase}
let port2=${rpcportbase}+1
let port3=${rpcportbase}+2

for zone in "${LOCALITY_ZONE[@]}"
do
    let node=($node+1)
    let offset=${node}-1
    let rpcport=${rpcportbase}+$offset
    let httpport=${httpportbase}+$offset
    let sqlport=${sqlportbase}+$offset

    join=${host}:${port1},${host}:${port2},${host}:${port3}
    mempool="10%"

    echo -e "n${node} [--sql-addr=port=${host}:${sqlport}, --locality=${zone}]"
done

fn_print_info "Start up to ${#LOCALITY_ZONE[@]} nodes in '$SECURITY_MODE' mode"
fn_local_node_range

let port1=${rpcportbase}
let port2=${rpcportbase}+1
let port3=${rpcportbase}+2

for node in $nodes
do
    let offset=${node}-1
    let rpcport=${rpcportbase}+$offset
    let httpport=${httpportbase}+$offset
    let sqlport=${sqlportbase}+$offset

    join=${host}:${port1},${host}:${port2},${host}:${port3}
    zone=${LOCALITY_ZONE[node - 1]}
    mempool="10%"

    fn_local_node_status $rpcport

    if [ "${status}" != "0" ]; then
      fn_print_warn "Node ${node} is already running! [--sql-addr=port=${host}:${sqlport}, --locality=${zone}]"
    else
      fn_local_start
    fi
done
