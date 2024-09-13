To create the docker image:

```
podman build -t lunatech-chef-api -f Dockerfile .
```

To run docker the image:

```
podman run -it -m 1024m --env POSTGRES_HOST_AUTH_METHOD=trust --name chef-api -p 5432:5432 lunatech-chef-api -c log_statement=all
```

To run docker image and clean it on shutdown:

```
podman run -it -rm -m 1024m --env POSTGRES_HOST_AUTH_METHOD=trust --name postgres -p 5432:5432 lunatech-chef-api -c log_statement=all
```
