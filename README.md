# Lunatech Chef API

###This project is a WIP. The idea is to replace the current lunatech-lunch-planner with lunatech-chef.



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
The API can be tested by running the requests available in folder `request`, using IntelliJ.
Only authorized request are accepted. These are the steps:

1. Update the token in `http-client.env.json`.

    To get a new token you have to launch the application locally, and login using the Google sign-in.
After a successful login a token will be printed. Copy and past it in `http-client.env.json` in `token`.

2. Update the session in `http-client.env.json`.

    Run the login request in `1-login.http`.
Running this request will output a `CHEF_SESSION` string. Update the `session` in `http-client.env.json` with that string.

3. You are now ready to run the other requests.
