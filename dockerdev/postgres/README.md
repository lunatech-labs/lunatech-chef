To create docker image:
docker build -t lunatech-chef-api .

To run docker image:
docker run -it -m 1024m --env POSTGRES_HOST_AUTH_METHOD=trust --name chef-api -p 5432:5432 lunatech-chef-api

To run docker image and clean it on shutdown:
docker run -it -m 1024m --env POSTGRES_HOST_AUTH_METHOD=trust --name postgres -p 5432:5432 lunatech-chef-api
