package com.lunatech.chef.api.domain

import java.util.UUID

data class NewLocation(
    val city: String,
    val country: String,
)

data class Location(
    val uuid: UUID,
    val city: String,
    val country: String,
    val isDeleted: Boolean = false,
) {
    companion object {
        fun fromNewLocation(newLocation: NewLocation): Location {
            return Location(
                uuid = UUID.randomUUID(),
                city = newLocation.city,
                country = newLocation.country,
            )
        }
    }
}
