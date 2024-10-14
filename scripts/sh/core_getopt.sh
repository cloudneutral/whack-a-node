#!/bin/bash

moduleselfname="$(basename "$(readlink -f "${BASH_SOURCE[0]}")")"

getopt=$1
shift

case "${getopt}" in
    login)
        command_login.sh $*
        ;;
    logout)
        command_logout.sh $*
        ;;
    disrupt)
        command_disrupt.sh $*
        ;;
    recover)
        command_recover.sh $*
        ;;
    nodes)
        command_nodes.sh $*
        ;;
    critical-nodes)
        command_critical_nodes.sh $*
        ;;
    status)
        command_status.sh $*
        ;;
    sql)
        command_sql.sh $*
        ;;
    open)
        command_open.sh $*
        ;;
    install)
        command_install.sh $*
        ;;
    clean)
        command_clean.sh $*
        ;;
    #####################################
    # Local cluster
    #####################################
    drain)
        command_drain.sh $*
        ;;
    decom)
        command_decommission.sh $*
        ;;
    recom)
        command_recommission.sh $*
        ;;
    certs)
        command_certs.sh $*
        ;;
    init)
        command_init.sh $*
        ;;
    start)
        command_start.sh $*
        ;;
    start-all)
        command_start_all.sh $*
        ;;
    stop-all)
        command_stop_all.sh $*
        ;;
    kill-all)
        command_kill_all.sh $*
        ;;
    stop)
        command_stop.sh $*
        ;;
    kill)
        command_kill.sh $*
        ;;
    start-lb)
        command_start_lb.sh $*
        ;;
    stop-lb)
        command_stop_lb.sh $*
        ;;
    run-service)
        command_run_service.sh $*
        ;;
    start-service)
        command_start_service.sh $*
        ;;
    stop-service)
        command_stop_service.sh $*
        ;;
    *)
    if [ -n "${getopt}" ]; then
        echo -e "${red}Unknown command${default}: $0 ${getopt}"
    fi
    echo -e "${green}Usage: $0 [command]${default}"
    echo -e "${default}Whack-a-node Cluster Admin${default}"
    echo -e ""
    echo -e "${default}Deployment option: ${green}${DEPLOY_OPTION}${default}"
    echo -e "${default}Self-hosted mode: ${green}${SH_MODE}${default}"
    echo -e ""
    echo -e "${lightyellow}Cluster Commands${default}"
    {
        echo -e "${cyan}login\t${default}     | Get API authentication token"
        echo -e "${cyan}logout\t${default}     | Close API session"
        echo -e "${cyan}disrupt\t${default}     | Disrupt cluster"
        echo -e "${cyan}recover\t${default}     | Recover from disruption"
        echo -e "${cyan}nodes\t${default}     | Print node details"
        echo -e "${cyan}critical-nodes\t${default}     | Print critical nodes report"
        echo -e "${cyan}status\t${default}     | Print node status"
        echo -e "${cyan}sql\t${default}     | Connect to SQL console"
        echo -e "${cyan}open\t${default}     | Connect to DB console"
        echo -e "${cyan}install\t${default}     | Download CockroachDB server binary"
    } | column -s $'\t' -t

    echo -e ""
    echo -e "${lightgreen}Service Commands${default}"
    {
        echo -e "${green}start-service\t${default}      | Start the WAN service"
        echo -e "${green}run-service\t${default}      | Run the WAN service"
        echo -e "${green}stop-service\t${default}      | Stop the WAN service"
    } | column -s $'\t' -t

    echo -e ""
    echo -e "${magenta}Local Cluster Commands${default}"
    {
        echo -e "${lightmagenta}start\t${default}          | Start one node"
        echo -e "${lightmagenta}stop\t${default}          | Stop one node gracefully"
        echo -e "${lightmagenta}kill\t${default}          | Kill one node"
        echo -e "${lightmagenta}start-all\t${default}          | Start all nodes"
        echo -e "${lightmagenta}stop-all\t${default}          | Stop all nodes gracefully"
        echo -e "${lightmagenta}kill-all\t${default}          | Kill all nodes"
        echo -e "${lightmagenta}start-lb\t${default}          | Start HAProxy load balancer"
        echo -e "${lightmagenta}stop-lb\t${default}          | Stop HAProxy load balancer"
        echo -e "${lightmagenta}certs\t${default}          | Generate certificates (secure mode only)"
        echo -e "${lightmagenta}init\t${default}          | Initialize cluster"
        echo -e "${lightmagenta}clean\t${default}          | Clean all data files"
        echo -e "${lightmagenta}drain\t${default}          | Drain one node"
        echo -e "${lightmagenta}decom\t${default}          | Decommission one node"
        echo -e "${lightmagenta}recom\t${default}          | Recommission one node"
    } | column -s $'\t' -t

esac
