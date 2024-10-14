#!/bin/bash

# Ansi color coding on/off
ansi=on

# Deployment option, pick one of:
# settings-insecure.sh - Self-hosted cluster in insecure mode
# settings-secure.sh   - Self-hosted cluster in secure mode
# settings-cloud.sh    - Cloud Dedicated/Standard

source "${configdir}/settings-insecure.sh"
#source "${configdir}/settings-secure.sh"
#source "${configdir}/settings-cloud.sh"

# Local cluster management (DO NOT REMOVE)
source "${configdir}/settings-local.sh"
