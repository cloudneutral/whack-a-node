###############################################
# CockroachDB Self-Hosted Secure Configuration
###############################################

# Load balancer status URL
SH_LB_URL=http://localhost:7070
# Admin endpoint URL
ADMIN_URL=https://localhost:443

#####################################################
# Modify below only if needed (local port collisions)
#####################################################

##################
# Connect settings
##################

# User name with ADMIN privilege
DB_USER=craig
# User secret
DB_PASSWORD=cockroach
# Connection URL
DB_URL="postgresql://root@localhost:26257/?sslcert=${certsdir}/client.root.crt&sslkey=${certsdir}/client.root.key&sslmode=verify-full&sslrootcert=${certsdir}/ca.crt"
# Do not modify
DEPLOY_OPTION=sh
# Self-hosted security posture
SH_MODE=secure

##################
# Service settings
##################

# Service frontend URL
APP_SERVICE_URL=http://localhost:9090
# JDBC datasource url
APP_JDBC_URL="jdbc:postgresql://localhost:26257/defaultdb?ssl=true&sslmode=require&sslrootcert=${certsdir}/ca.crt&sslkey=${certsdir}/client.craig.key.pk8&sslcert=${certsdir}/client.craig.crt"
# Application parameters
APP_PARAMS="\
--spring.server.port=9090 \
--application.adminUrl=${ADMIN_URL} \
--spring.profiles.active=secure \
--spring.datasource.url=${APP_JDBC_URL} \
--spring.datasource.username=${DB_USER} \
--spring.datasource.password=${DB_PASSWORD}"
# Executable jar
APP_JARFILE=${rootdir}/whackanode.jar
