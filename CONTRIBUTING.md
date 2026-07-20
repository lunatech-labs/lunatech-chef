# Contributing

Everyone is welcome to contribute to the application or make suggestions, but you may want to check first with the
project contact person Leonor Boga @leo-bogastry.

# Setup development environment

In order to be able to run the lunatech-chef locally you need to have the following installed in your local machine:

- [JDK 21](https://sdkman.io/install)
- [Gradle](https://gradle.org/install)
- [Npm](https://docs.npmjs.com/downloading-and-installing-node-js-and-npm)
- [Docker](https://www.docker.com/products/docker-desktop/)

After cloning the project from Github you can check if it builds by running

```commandline
gradle buildAll
```

# Run Lunatech-chef

## Start Postgres and Keycloak with Docker

Local development uses two containers, defined in `docker-compose.yml`: a Postgres database and a Keycloak instance
that is pre-configured for this app. The Keycloak realm (the `lunachef` client, the `admin` client role, the
flat `roles` token mapper, the `backoffice` and `hrm` groups and two test users) is imported automatically from
`dockerdev/keycloak/lunatech-realm.json`.

```commandline
docker compose up -d
```

Keycloak runs on http://localhost:8081 (admin console login: `admin` / `admin`). Two test users are available, both
with password `lunachef`:

- `admin.user@lunatech.nl` is a member of the `backoffice` group and therefore a Chef admin
- `normal.user@lunatech.nl` is a regular user

To change the realm configuration, edit `dockerdev/keycloak/lunatech-realm.json` and recreate the container with
`docker compose up -d --force-recreate keycloak`. The import only runs for realms that do not exist yet, so an
existing container keeps its old state.

## Setup environment variables

In `src/main/resources/` folder create a file `override.conf` providing values for the following variables:

```shell
POSTGRESQL_ADDON_HOST = "localhost"
POSTGRESQL_ADDON_PORT = 5432
POSTGRESQL_ADDON_DB = "lunatech-chef-api"
POSTGRESQL_ADDON_USER = "lunatech-chef-api"
POSTGRESQL_ADDON_PASSWORD = ""

// point token verification at the local Keycloak from docker compose
JWK_PROVIDER = "http://localhost:8081/realms/lunatech/protocol/openid-connect/certs"
JWK_ISSUER = "http://localhost:8081/realms/lunatech"

KTOR_ENV = dev
MAILPACE_API_KEY = ""

// you will need to setup these if you wish to test the specific funcionality:
// - schedules being created automatically
// - sending the monthly report email automatically 

// RECURRENT_SCHEDULES_ENABLED = true
// RECURRENT_SCHEDULES_CRON = "0 */10 * ? * *" // every 10 minutes for example

// MONTHLY_REPORTS_ENABLED = true
// MONTHLY_REPORTS_CRON = "0 */10 * ? * *" // every 10 minutes for example
// EMAIL_RECIPIENTS = "<test with your email>"
// MAILPACE_API_KEY = "" //ask a current developer or check clever-cloud configuration

// you will need to setup these if you wish to test the Slack lunch bot:
// SLACKBOT_ENABLED = true
// SLACK_USER_TOKEN = "" // ask a current developer or check clever-cloud configuration
// SLACK_SIGNING_SECRET = "" // from the Slack app's Basic Information > App Credentials > Signing Secret;
                             // must match the value on clever-cloud exactly, or every interaction is rejected
```

In `frontend/` create the files `.env.development` and `.env.production`. The one that is needed depends on the way the
app is started so having both is advised.
Both take the same information:

```shell
REACT_APP_BASE_URL=http://localhost:8080
REACT_APP_REALMS_URL=http://localhost:8081/realms/lunatech
REACT_APP_CLIENT_ID=lunachef
```

There are two types of users: normal users and admin users. Admin access is granted through Keycloak: the app-specific
`admin` client role reaches the backend as a flat `roles` claim in the ID token, and members of the `backoffice` and
`hrm` groups hold that role. Locally, log in as `admin.user@lunatech.nl` for an admin account or
`normal.user@lunatech.nl` for a regular one (both use password `lunachef`).

## Start Lunatech-chef (FE and BE starting together)

```commandline
gradle buildAll
gradle devRun
```

The application will start at `http://localhost:8080`

`gradle devRun` starts the Postgres and Keycloak containers first (the same as `docker compose up -d`) and then runs
the application. Plain `gradle run` only runs the application and expects the containers to be running already. The
production deployment on Clever Cloud uses `gradle run`, so the compose setup never affects it.

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

1. Copy the [http-client.private.env.json.sample](http-client.private.env.json.sample) file to
   `http-client.private.env.json` (and make sure not to commit these secrets)

2. Update the token in `http-client.private.env.json`.

   To get a new token, launch the application locally and log in through Keycloak. The access token is stored in
   `sessionStorage` under the key `oidc.user:<REACT_APP_REALMS_URL>:<REACT_APP_CLIENT_ID>`; open the browser devtools
   console and run `JSON.parse(sessionStorage.getItem("oidc.user:...")).access_token` to print it, then copy it into
   `http-client.private.env.json` as `token`.

3. You are now ready to run the requests, starting with `1-me.http`.

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
