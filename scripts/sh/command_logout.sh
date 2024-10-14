#!/bin/bash

${installdir}/cockroach auth-session logout ${DB_USER} --url ${DB_URL}
