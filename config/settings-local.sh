#################################################
# CockroachDB Local Cluster Configuration
#
# These settings only apply for local CockroachDB
# cluster creation and management.
#################################################

# Advertised host name
host=localhost
# Base port for RPC traffic
rpcportbase=25258
# Base port number for data traffic (leaving 26257 for LB)
sqlportbase=26258
# Base port number for HTTP traffic (leaving 8080 for LB)
httpportbase=8081
# Cockroach version to download with install command:
#   https://www.cockroachlabs.com/docs/releases/?filters=mac
version="v24.2.3.darwin-10.9-amd64"

# Each item in selected zone array below counts as one node.
# The exiting haproxy.cfg file is pre-configured for 18 nodes
# but works for anything less as well.

# 3 nodes by default up to 18 nodes in 6 regions and 2 potential super regions.
# Locality flags can be anything, as long as it follows the key/value tuple syntax:
#   https://www.cockroachlabs.com/docs/v24.2/cockroach-start.html#locality
LOCALITY_ZONE=(
  'region=eu-north-1,zone=eu-north-1a'
  'region=eu-north-1,zone=eu-north-1b'
  'region=eu-north-1,zone=eu-north-1c'
#  'region=eu-north-1,zone=eu-north-1c'
#  'region=eu-central-1,zone=eu-central-1a'
#  'region=eu-central-1,zone=eu-central-1b'
#  'region=eu-central-1,zone=eu-central-1c'
#  'region=eu-west-1,zone=eu-west-1a'
#  'region=eu-west-1,zone=eu-west-1b'
#  'region=eu-west-1,zone=eu-west-1c'
#  'region=us-east-1,zone=us-east-1a'
#  'region=us-east-1,zone=us-east-1b'
#  'region=us-east-1,zone=us-east-1c'
#  'region=us-east-2,zone=us-east-2a'
#  'region=us-east-2,zone=us-east-2b'
#  'region=us-east-2,zone=us-east-2c'
#  'region=us-west-1,zone=us-west-1a'
#  'region=us-west-1,zone=us-west-1b'
#  'region=us-west-1,zone=us-west-1c'
)
