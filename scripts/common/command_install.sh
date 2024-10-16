#!/bin/bash

fn_continueYesNo "Download and install ${version} to ${installdir}?"

mkdir -p ${installdir}

cd ${installdir} || exit

case "$OSTYPE" in
  darwin*)
        curl https://binaries.cockroachdb.com/cockroach-${version}.tgz | tar -xz; cp -i cockroach-${version}/cockroach ${installdir}
        ;;
  *)
        wget https://binaries.cockroachdb.com/cockroach-${version}.tgz; tar -xvf cockroach-${version}.tgz; cp -i cockroach-${version}/cockroach ${installdir}
        ;;
esac

