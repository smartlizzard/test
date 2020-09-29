#!/bin/bash -e
if [ ! -x /usr/sbin/nginx ]; then
    echo "Nginx not installed"
    echo "- installing nginx"
    yum -y install nginx
    echo "- installed nginx"
fi
