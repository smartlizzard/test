server { listen  80;
  server_name example.com;
  access_log /var/log/example.com/nginx.access.log;
  error_log  /var/log/example.com/nginx.error.log;
  root /var/www/apps/example.com/public;
  charset utf-8;

  location / {
    rewrite ^ https://$host$request_uri? permanent;
  }
}

server {
  listen        443 ssl;
  server_name  example.com;
  access_log  /var/log/example.com/nginx.access.log;
  error_log   /var/log/example.com/nginx.error.log;
  ssl_certificate       /etc/nginx/ssl/example.com.pem;
  ssl_certificate_key   /etc/nginx/ssl/example.com.key;

  keepalive_timeout 5;
  root /var/www/apps/example.com/dist;
  charset utf-8;

  location ~ ^/(scripts.*js|styles|images) {
    gzip_static on;
    expires 1y;
    add_header Cache-Control public;
    add_header ETag "";

    break;
  }

  location /api1 {
    rewrite ^/api1/(.*) /$1 break;
    proxy_redirect off;
    proxy_pass https://api1.example.com;
    proxy_set_header X-Real-IP $remote_addr;
    proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
    proxy_set_header X-Forwarded-Proto https;
    proxy_set_header Authorization $http_authorization;
  }

  location /api2 {
    rewrite ^/api2/(.*) /$1 break;
    proxy_redirect off;
    proxy_pass https://api2.example.com;
    proxy_set_header X-Real-IP $remote_addr;
    proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
    proxy_set_header X-Forwarded-Proto https;
    proxy_set_header Authorization $http_authorization;
  }

  location / {
    try_files $uri /index.html;
  }
}

worker_processes  1;

events {
    worker_connections  1024;
}

http {
    server {
        listen 80;
        server_name  localhost;

        root   /usr/share/nginx/html;
        index  index.html index.htm;
        include /etc/nginx/mime.types;

        gzip on;
        gzip_min_length 1000;
        gzip_proxied expired no-cache no-store private auth;
        gzip_types text/plain text/css application/json application/javascript application/x-javascript text/xml application/xml application/xml+rss text/javascript;

        location / {
            try_files $uri $uri/ /index.html;
        }
    }
}

