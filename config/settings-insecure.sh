################################################
# CockroachDB Self-Hosted Insecure Configuration
################################################

# Load balancer status URL
SH_LB_URL=http://localhost:7070
# Admin endpoint URL
ADMIN_URL=http://localhost:8080

#####################################################
# Modify below only if needed (local port collisions)
#####################################################

##################
# Connect settings
##################

# User name with ADMIN privilege
DB_USER=root
# User secret
DB_PASSWORD=
# Connection URL
DB_URL="postgresql://localhost:26257?sslmode=disable"
# Do not modify
DEPLOY_OPTION=sh
# Self-hosted security posture
SH_MODE=insecure

##################
# Service settings
##################

# Service frontend URL
APP_SERVICE_URL=http://localhost:9090
# JDBC datasource url
APP_JDBC_URL="jdbc:postgresql://localhost:26257/defaultdb?sslmode=disable"
# Application parameters
APP_PARAMS="\
--spring.server.port=9090 \
--application.adminUrl=${ADMIN_URL} \
--spring.profiles.active=insecure \
--spring.datasource.url=${APP_JDBC_URL} \
--spring.datasource.username=${DB_USER}"
# Executable jar
APP_JARFILE=${rootdir}/whackanode.jar

