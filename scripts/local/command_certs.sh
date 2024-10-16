#!/bin/bash

fn_continueYesNo "Generate local security certificates to ${certsdir}?"

mkdir -p ${certsdir}

cockroach cert create-ca --certs-dir=${certsdir} --ca-key=${certsdir}/ca.key
cockroach cert create-node localhost $(hostname) --certs-dir=${certsdir} --ca-key=${certsdir}/ca.key
cockroach cert create-client root --certs-dir=${certsdir} --ca-key=${certsdir}/ca.key --also-generate-pkcs8-key
cockroach cert create-client ${DB_USER} --certs-dir=${certsdir} --ca-key=${certsdir}/ca.key --also-generate-pkcs8-key

fn_print_ok "Done"