package com.lunatech.chef.api.routes

import java.util.UUID

const val UUID_ROUTE = "/{uuid}"
const val UUID_PARAM = "uuid"

fun String?.toUUIDOrNull(): UUID? = runCatching { UUID.fromString(this) }.getOrNull()
