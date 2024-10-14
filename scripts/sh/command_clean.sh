#!/bin/bash

fn_print_warn "This command will delete all CockroachDB data files"
fn_continueYesNo "Delete '${datadir}' - are you sure?"

rm -rf -I ${datadir}