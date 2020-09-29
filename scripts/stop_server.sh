#!/bin/bash

pgrep nginx

## AWS AMI Linux 2 ##
isExistApp=`pgrep nginx`
if [[ -n  $isExistApp ]]; then
    systemctl stop nginx
    echo "- nginx stopped"
else
    echo "- no nginx to stop"
fi
