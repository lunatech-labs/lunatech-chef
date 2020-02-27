#!/usr/bin/env bash
createuser --username "$POSTGRES_USER" lunatech-chef-api --superuser
createdb --username "$POSTGRES_USER" -O lunatech-chef-api lunatech-chef-api
createdb --username "$POSTGRES_USER" -O lunatech-chef-api lunatech-chef-api-test
