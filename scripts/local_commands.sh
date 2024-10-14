#!/bin/bash

command_start.sh(){
  modulefile="${FUNCNAME[0]}"
	source "${scriptsdir}/sh/${modulefile}"
}

command_start_all.sh(){
  modulefile="${FUNCNAME[0]}"
	source "${scriptsdir}/sh/${modulefile}"
}

command_start_lb.sh(){
  modulefile="${FUNCNAME[0]}"
	source "${scriptsdir}/sh/${modulefile}"
}

command_stop_all.sh(){
  modulefile="${FUNCNAME[0]}"
	source "${scriptsdir}/sh/${modulefile}"
}

command_kill_all.sh(){
  modulefile="${FUNCNAME[0]}"
	source "${scriptsdir}/sh/${modulefile}"
}

command_stop.sh(){
  modulefile="${FUNCNAME[0]}"
	source "${scriptsdir}/sh/${modulefile}"
}

command_kill.sh(){
  modulefile="${FUNCNAME[0]}"
	source "${scriptsdir}/sh/${modulefile}"
}

command_stop_lb.sh(){
  modulefile="${FUNCNAME[0]}"
	source "${scriptsdir}/sh/${modulefile}"
}

command_init.sh(){
  modulefile="${FUNCNAME[0]}"
	source "${scriptsdir}/sh/${modulefile}"
}

command_certs.sh(){
  modulefile="${FUNCNAME[0]}"
	source "${scriptsdir}/sh/${modulefile}"
}

command_clean.sh(){
  modulefile="${FUNCNAME[0]}"
	source "${scriptsdir}/sh/${modulefile}"
}

command_drain.sh() {
	modulefile="${FUNCNAME[0]}"
	source "${scriptsdir}/sh/${modulefile}"
}

command_decommission.sh() {
	modulefile="${FUNCNAME[0]}"
	source "${scriptsdir}/sh/${modulefile}"
}

command_recommission.sh() {
	modulefile="${FUNCNAME[0]}"
	source "${scriptsdir}/sh/${modulefile}"
}

