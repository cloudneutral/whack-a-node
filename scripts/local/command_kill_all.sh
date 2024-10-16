#!/bin/bash

fn_local_pids

fn_continueYesNo "Kill all nodes?"

for pid in $(ps -ef | grep "cockroach" | grep "sql-addr=" | awk '{print $2}')
do
  fn_local_kill "$pid"
done
