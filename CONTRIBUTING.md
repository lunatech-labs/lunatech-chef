# Contributing

Everyone is welcome to contribute to the application or make suggestions, but you may want to check first with the
project contact person Leonor Boga @LeonorLunatech.

# Setup development environment

In order to be able to run the lunatech-chef locally you need to have the following installed in your local machine:

- [JDK 11](https://sdkman.io/install)
- [Gradle](https://gradle.org/install)
- [Npm](https://docs.npmjs.com/downloading-and-installing-node-js-and-npm)
- [Docker](https://www.docker.com/products/docker-desktop/)

After cloning the project from Github you can check if it builds by running

```commandline
gradle buildAll
```

# Run Lunatech-chef

## Start docker with Postgres

Before starting the Lunatech-chef you need an instance of Postgress running. There's a docker file that provides one
already.

Build to the docker image:

```commandline
docker build -t lunatech-chef-api dockerdev/postgres/.
```

Run the docker image:

```commandline
docker run -it -m 1024m --env POSTGRES_HOST_AUTH_METHOD=trust --name postgres -p 5432:5432 lunatech-chef-api -c log_statement=all
```

## Setup environment variables

In `src/main/resources/` folder create a file `.override.conf` providing values for the following variables:

```shell
POSTGRESQL_ADDON_HOST = "localhost"
POSTGRESQL_ADDON_PORT = 5432
POSTGRESQL_ADDON_DB = "lunatech-chef-api"
POSTGRESQL_ADDON_USER = "lunatech-chef-api"
POSTGRESQL_ADDON_PASSWORD = ""

AUTH_SESSION_CLIENT_ID = "" //ask a current developer or check clever-cloud configuration
AUTH_SESSION_SECRET_KEY = "" //ask a current developer or check clever-cloud configuration

KTOR_ENV = dev

// you will need to setup these if you wish to test the specific funcionality:
// - schedules being created automatically
// - sending the monthly report email automatically 

// RECURRENT_SCHEDULES_CRON = "0 */10 * ? * *" // every 10 minutes for example

// MONTHLY_REPORTS_CRON = "0 */10 * ? * *" // every 10 minutes for example
// EMAIL_RECIPIENTS = "<test with your email>"
// SENDGRID_SMTP_HOST = "email-smtp.eu-west-1.amazonaws.com"
// SENDGRID_USERNAME = "" //ask a current developer or check clever-cloud configuration
// SENDGRID_PASSWORD = "" //ask a current developer or check clever-cloud configuration
```

In `frontend/` create the files `.env.development` and `.env.production`. The one that is needed depends on the way the
app is started so having both is advised.
Both take the same information:

```shell
REACT_APP_BASE_URL=http://localhost:8080
REACT_APP_REALMS_URL=https://keycloak.lunatech.com/realms/lunatech
REACT_APP_CLIENT_ID=lunachef-local
```

There are two types of users: normal users and admin users. To be able to see all option in the Lunatech-chef UI you
will need to
add yourself as an admin in the `application.conf` file.

## Start Lunatech-chef (FE and BE starting together)

```commandline
gradle buildAll
gradle run
```

The application will start at `http://localhost:8080`

## Start Lunatech-chef (FE and BE starting separately)

#### To start the backend API:

```commandline
gradle build
gradle run
```

#### To start the frontend:

```commandline
cd frontend/
npm start
```

The application will start at `http://localhost:3000`

# Test the API

The `requests` folder contains examples and tests on how to use the API and what is the order needed for everything to
come together properly.

Only authorized request are accepted. These are the steps:

1. Update the token in `http-client.env.json`.

To get a new token you have to launch the application locally and login using the Google sign-in.
After a successful login a token can be printed by adding a log statement in login.js `handleLogin` method.
Copy and paste it into `http-client.env.json` in `token`.

2. Update the session in `http-client.env.json`.

Run the login request in `1-login.http`.
Running this request will output a `CHEF_SESSION` string. Update the `session` in `http-client.env.json` with that
string.

3. You are now ready to run the other requests.

# Deploy Lunatech-chef

Lunatech-chef is hosted in clever-cloud. The deployment is done by rebasing `production` branch on `master` branch.
**This rebase will always be a fast-forward one**

**Never push a new version to production without making sure that it runs properly**

Before doing a deployment make sure you have the latest versions of `master` and `production` branches.
Rebase `production` on `master` and push it. Clever-cloud will do the deployment automatically.

```
$ git checkout master
[master]$ git pull
[master]$ git checkout production
[production]$ git pull
[production]$ git rebase master
[production]$ git push
```

On clever-cloud you can see `Lunatech-chef` in the list of deployed applications and also `Lunatech-chef-api-database`
running a
postgres DB that supports `Lunatech-chef`.