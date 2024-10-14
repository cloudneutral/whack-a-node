#!/bin/bash

command_disrupt.sh() {
	modulefile="${FUNCNAME[0]}"
	source "${scriptsdir}/$DEPLOY_OPTION/${modulefile}"
}

command_login.sh() {
	modulefile="${FUNCNAME[0]}"
	source "${scriptsdir}/$DEPLOY_OPTION/${modulefile}"
}

command_logout.sh() {
	modulefile="${FUNCNAME[0]}"
	source "${scriptsdir}/$DEPLOY_OPTION/${modulefile}"
}

command_nodes.sh() {
	modulefile="${FUNCNAME[0]}"
	source "${scriptsdir}/$DEPLOY_OPTION/${modulefile}"
}

command_critical_nodes.sh(){
  modulefile="${FUNCNAME[0]}"
	source "${scriptsdir}/$DEPLOY_OPTION/${modulefile}"
}

command_open.sh() {
	modulefile="${FUNCNAME[0]}"
	source "${scriptsdir}/$DEPLOY_OPTION/${modulefile}"
}

command_recover.sh() {
	modulefile="${FUNCNAME[0]}"
	source "${scriptsdir}/$DEPLOY_OPTION/${modulefile}"
}

command_sql.sh() {
	modulefile="${FUNCNAME[0]}"
	source "${scriptsdir}/$DEPLOY_OPTION/${modulefile}"
}

command_status.sh() {
	modulefile="${FUNCNAME[0]}"
	source "${scriptsdir}/$DEPLOY_OPTION/${modulefile}"
}

core_getopt.sh() {
	modulefile="${FUNCNAME[0]}"
	source "${scriptsdir}/$DEPLOY_OPTION/${modulefile}"
}

core_exit.sh() {
	modulefile="${FUNCNAME[0]}"
	source "${scriptsdir}/${modulefile}"
}

command_run_service.sh(){
  modulefile="${FUNCNAME[0]}"
	source "${scriptsdir}/core/${modulefile}"
}

command_start_service.sh(){
  modulefile="${FUNCNAME[0]}"
	source "${scriptsdir}/core/${modulefile}"
}

command_stop_service.sh(){
  modulefile="${FUNCNAME[0]}"
	source "${scriptsdir}/core/${modulefile}"
}

command_install.sh(){
  modulefile="${FUNCNAME[0]}"
	source "${scriptsdir}/core/${modulefile}"
}

