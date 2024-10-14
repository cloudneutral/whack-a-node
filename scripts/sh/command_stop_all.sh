#!/bin/bash

fn_local_pids

fn_continueYesNo "Stop all nodes?"

for pid in $(ps -ef | grep "cockroach" | grep "sql-addr=" | awk '{print $2}')
do
  fn_local_stop "$pid"
done
