[![Java CI with Maven](https://github.com/cloudneutral/whack-a-node/actions/workflows/maven.yml/badge.svg?branch=main)](https://github.com/cloudneutral/whack-a-node/actions/workflows/maven.yml)

<!-- TOC -->
* [About](#about)
  * [How it works](#how-it-works)
  * [Screenshots](#screenshots)
* [Terms of Use](#terms-of-use)
* [Building and Running](#building-and-running)
  * [Prerequisites](#prerequisites)
  * [Install the JDK](#install-the-jdk)
  * [Building](#building)
    * [Clone the project](#clone-the-project)
    * [Build the artifacts](#build-the-artifacts)
* [Configuration](#configuration)
* [Running](#running)
  * [Insecure Self-Hosted Configuration](#insecure-self-hosted-configuration)
  * [Secure Self-Hosted Configuration](#secure-self-hosted-configuration)
  * [Cockroach Cloud Configuration](#cockroach-cloud-configuration)
* [Future Work](#future-work)
<!-- TOC -->

# About

<img align="left" src="logo.png" width="128" /> Whack-a-node (WAN) is a tool for 
controlling and visualizing CockroachDB cluster failures and the impact on application
workloads. The main features include:

- Visualize cluster health
- Visualize client-side workload impact on disruptions
- Push-button feature for node disruption and recovery

In more detail, it supports the following platforms/versions:

- CockroachDB Cloud v22.2+
  - Requires a feature flag enabled for the organization (file a support request) 
- CockroachDB Self-Hosted v22.2+
  - Local or remote (local setup scripts available)
  - Secure or insecure mode
- MacOS (main platform)
- Linux

## How it works

Whack-a-node has two main parts: 

1. A spring-boot web app for the visuals (with a REST API) 
1. Bash scripts for retrieving cluster status, causing disruptions, recover nodes etc.

In addition, there are bash scripts also for installing and configuring a local 
CockroachDB cluster.

The scripts can be used independently of the web app. The web app uses the bash 
scripts to pass commands to the CockroachDB cluster and JDBC to run simple 
opt-in workloads. The web app can disrupt and recover independent nodes both
in self-hosted clusters and Cockroach Cloud.

## Screenshots

Landing page showing the cluster layout:

![ui1](.github/ui-1.png)

Listing client side mini-workloads against the database:

![ui2](.github/ui-2.png)

# Terms of Use

This tool is not supported by Cockroach Labs. Use of this tool is entirely at your
own risk and Cockroach Labs makes no guarantees or warranties about its operation.

See [MIT](LICENSE.txt) for terms and conditions.

# Building and Running

The only building needed is the visualization spring boot app that also 
acts as the control plane (a single executable jar) and database client.

## Prerequisites

- Java 17+ JDK
    - https://openjdk.org/projects/jdk/17/
    - https://www.oracle.com/java/technologies/downloads/#java17
- Maven 3+ (optional, embedded wrapper available)
    - https://maven.apache.org/

## Install the JDK

MacOS (using sdkman):

    curl -s "https://get.sdkman.io" | bash
    sdk list java
    sdk install java 17.0 (pick version)  

Ubuntu:

    sudo apt-get install openjdk-17-jdk

## Building

### Clone the project

    git clone git@github.com:cloudneutral/whack-a-node.git && cd whack-a-node

### Build the artifacts

Maven is used to build the Java app component, bootstrapped by Tanuki Maven wrapper:

    ./mvnw clean install

# Configuration

Every aspect of whack-a-node can be configured through the files 
available in the `config` directory:

1. [settings.sh](config/settings.sh) - Where you pick the cluster type (cloud or self-hosted).
1. [settings-cloud.sh](config/settings-cloud.sh) - Settings for using an existing CockroachDB Cloud cluster (you can't create one through this tool).
1. [settings-insecure.sh](config/settings-insecure.sh) - Settings for using an existing or new local CockroachDB self-hosted cluster in insecure mode.
1. [settings-secure.sh](config/settings-secure.sh) - Settings for using an existing or new local CockroachDB self-hosted cluster in secure mode.
1. [settings-local.sh](config/settings-local.sh) - Settings for a local CockroachDB cluster.
1. [haproxy.cfg](config/haproxy.cfg) - Default HAProxy configuration for a local CockroachDB cluster.
  
More details are found in the configuration file comments. 

Notice that the web app will use the same bash scripts when querying for cluster status, 
causing disruption and so on. The application dependency is uni-directional, meaning that 
you can use the scripts in isolation without using the visualization app.

# Running

To see the different commands, run:

    chmod +x ./cluster-admin
    ./cluster-admin

## Insecure Self-Hosted Configuration

Getting started with a new, local self-hosted insecure cluster (default):

    ./cluster-admin install
    ./cluster-admin start-all
    ./cluster-admin init
    ./cluster-admin start-lb
    ./cluster-admin start-service
    ./cluster-admin open

The app should now be available at http://localhost:9090. You can 
either use the app web ui or shell commands to kill and restart nodes.

Running the inverse to shut things down:

    ./cluster-admin stop-all
    ./cluster-admin stop-lb
    ./cluster-admin stop-service

## Secure Self-Hosted Configuration

Edit [config/settings.sh](config/settings.sh) and change
to source `settings-secure.sh`.

Getting started with a new, local self-hosted secure cluster:

    ./cluster-admin install
    ./cluster-admin certs
    ./cluster-admin start-all
    ./cluster-admin init
    ./cluster-admin start-lb
    ./cluster-admin start-service
    ./cluster-admin login
    ./cluster-admin open

Now the app should be available at https://localhost. You can 
either use the app web ui or shell commands to kill and restart nodes.

## Cockroach Cloud Configuration

1. Edit [config/settings.sh](config/settings.sh) and change to source `settings-cloud.sh`.
1. Edit [config/settings-cloud.sh](config/settings-cloud.sh) and change the credentials to match your cluster.

To authenticate, you need the CA root certificate for your [cluster](https://cockroachlabs.cloud/) stored locally:

    curl --create-dirs -o $HOME/Library/CockroachCloud/certs/00000000-0000-0000-0000-000000000000/odin-ca.crt 'https://cockroachlabs.cloud/clusters/00000000-0000-0000-0000-000000000000/cert'

Then install a CockroachDB binary, login and start the app with:

    ./cluster-admin install
    ./cluster-admin login
    ./cluster-admin start-service
    ./cluster-admin open

# Future Work

- [DONE] Send SQL traffic from app to observe app-side effects
- Testing is incomplete for CC disruption/recovery.
- Support other distributed DBs

---

That is all, move out!
