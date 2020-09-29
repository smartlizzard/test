#!/bin/bash -e
if [ ! -x /usr/sbin/nginx ]; then
    echo "Nginx not installed"
    echo "- installing nginx"
    apt-get install nginx -y
    echo "- installed nginx"
fi
