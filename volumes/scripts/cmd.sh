#!/bin/bash
service mysqld start && \
cd /home/sunweekstar/redis && \
redis-server redis.conf && \
. /usr/local/scripts/wfServiceScripts/activitiStart.sh && \
. /usr/local/scripts/ipfsScripts/ipfsStart.sh && \
. /usr/local/scripts/nacosScripts/createNacosConfig.sh && \
peer node start
