#!/bin/bash -e
if [ ! -x /usr/sbin/nginx ]; then
    echo "Nginx not installed"
    echo "- installing nginx"
    sudo apt-get install -y software-properties-common python-software-properties
    sudo add-apt-repository ppa:nginx/stable
    sudo apt-get update
    sudo apt-get install nginx -y
    echo "- installed nginx"
fi
