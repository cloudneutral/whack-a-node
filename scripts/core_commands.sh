#!/bin/bash

command_disrupt.sh() {
	modulefile="${FUNCNAME[0]}"
	source "${scriptsdir}/$DEPLOY_MODE/${modulefile}"
}

command_login.sh() {
	modulefile="${FUNCNAME[0]}"
	source "${scriptsdir}/$DEPLOY_MODE/${modulefile}"
}

command_logout.sh() {
	modulefile="${FUNCNAME[0]}"
	source "${scriptsdir}/$DEPLOY_MODE/${modulefile}"
}

command_nodes.sh() {
	modulefile="${FUNCNAME[0]}"
	source "${scriptsdir}/$DEPLOY_MODE/${modulefile}"
}

command_critical_nodes.sh(){
  modulefile="${FUNCNAME[0]}"
	source "${scriptsdir}/$DEPLOY_MODE/${modulefile}"
}

command_open.sh() {
	modulefile="${FUNCNAME[0]}"
	source "${scriptsdir}/$DEPLOY_MODE/${modulefile}"
}

command_recover.sh() {
	modulefile="${FUNCNAME[0]}"
	source "${scriptsdir}/$DEPLOY_MODE/${modulefile}"
}

command_sql.sh() {
	modulefile="${FUNCNAME[0]}"
	source "${scriptsdir}/$DEPLOY_MODE/${modulefile}"
}

command_status.sh() {
	modulefile="${FUNCNAME[0]}"
	source "${scriptsdir}/$DEPLOY_MODE/${modulefile}"
}

core_getopt.sh() {
	modulefile="${FUNCNAME[0]}"
	source "${scriptsdir}/$DEPLOY_MODE/${modulefile}"
}

core_exit.sh() {
	modulefile="${FUNCNAME[0]}"
	source "${scriptsdir}/${modulefile}"
}

core_functions.sh() {
	modulefile="${FUNCNAME[0]}"
	source "${scriptsdir}/${modulefile}"
}
