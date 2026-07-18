# Lunatech Chef API

This project implements a web application for the planning of meals at the Lunatech offices.

Only users with a Lunatech email address can login to the application.
The login was integrated with Lunatech Keycloak.

This codebase hosts both the backend and the frontend of the application.

### Tech stack

Backend in [Kotlin](https://kotlinlang.org/) with [Ktor framework](https://ktor.io/docs/welcome.html)
and [Ktorm ORM framework](https://www.ktorm.org/).

Frontend in [ReactJs](https://react.dev/) with [ReduxJs](https://redux.js.org/)
and [React Router](https://reactrouter.com/en/main).

Depends on a [Postgres DB](https://www.postgresql.org/) and uses DB evolutions by [Flyway](https://flywaydb.org/).

### How to use Lunatech-chef

Lunatech-chef is available at https://lunch.lunatech.nl

A **normal user** is able to:

- see future planned meals date and location
- signup / unsign for a meal
- see who else signed up for meals

An **admin user** is able to also do the meal planning and access monthly reports.
The meal planning requires the following to be created, in the order mentioned:

- Office(s)
- Dish(es)
- Menu(s) with one or more dishes added
- A schedule is created by associating a menu with a date and an office location.

- After that each user can choose if they wish to attend a scheduled meal or not.

### Slack bot

Chef includes the LunchBot Slack integration. A Quartz job (cron `SLACKBOT_CRON`, production runs Monday and Tuesday 10:00 Europe/Amsterdam) sends a DM to every employee who has not answered for a scheduled lunch in the next 4 days. Button answers arrive on `POST /slack` and update the attendance. Configuration lives in the `slackbot` block of `application.conf` (`SLACK_USER_TOKEN`, `SLACKBOT_CRON`).

### Contributing

If you wish to contribute or just see how you can run the application locally see
the [Contributing guide](CONTRIBUTING.md).



