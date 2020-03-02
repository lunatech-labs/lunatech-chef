package com.lunatech.chef.api.persistence

import java.util.UUID

data class Location(
    val uuid:UUID,
    val city: String,
    val country: String,
    val isDeleted: Boolean = false
)

