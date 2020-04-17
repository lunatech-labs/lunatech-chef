# Lunatech Chef API

This project implements an API that allows to plan for meals at Lunatech.

In order to plan a meal you need to create Locations, Dishes, Menus, add Dishes to a Menu and then associate a date and a Location to a Menu.
After a User can choose to Attend a meal

The `requests` folder contains examples (and tests) on how to use the API and what is the order needed for everything to come together properly

## To get started:

### Compile and run the docker container:

In `/dockerdev/postgres` run:

```aidl
docker build -t lunatech-chef-api .
```
To run docker image and clean it on shutdown:
```aidl
docker run -it -m 1024m --env POSTGRES_HOST_AUTH_METHOD=trust --name postgres -p 5432:5432 lunatech-chef-api
```

#### To start the API:
```aidl
gradle run
```

### To test the API
Run the request on folder `request`, using IntelliJ.

Directly on the browser on `localhost:8080`
