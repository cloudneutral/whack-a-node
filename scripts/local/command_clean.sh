#!/bin/bash

fn_print_warn "This command will delete all CockroachDB data files!"

fn_continueYesNo "Delete '${datadir}' - are you sure?"
rm -rf ${datadir}

fn_continueYesNo "Delete '${certsdir}' - are you sure?"
rm -rf ${certsdir}

fn_continueYesNo "Delete '${installdir}' - are you sure?"
rm -rf ${installdir}