package com.lunatech.chef.api.config

import io.ktor.server.application.Application

val Application.envKind get() = environment.config.property("ktor.environment").getString()
val Application.isDev get() = envKind == "dev"
