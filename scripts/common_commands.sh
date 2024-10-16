#!/bin/bash

## Common commands

command_run_service.sh(){
  modulefile="${FUNCNAME[0]}"
	source "${scriptsdir}/common/${modulefile}"
}

command_start_service.sh(){
  modulefile="${FUNCNAME[0]}"
	source "${scriptsdir}/common/${modulefile}"
}

command_stop_service.sh(){
  modulefile="${FUNCNAME[0]}"
	source "${scriptsdir}/common/${modulefile}"
}

command_install.sh(){
  modulefile="${FUNCNAME[0]}"
	source "${scriptsdir}/common/${modulefile}"
}
