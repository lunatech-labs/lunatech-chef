package com.lunatech.chef.api.domain

import java.util.UUID

data class NewOffice(
    val city: String,
    val country: String,
)

data class Office(
    val uuid: UUID,
    val city: String,
    val country: String,
    val isDeleted: Boolean = false,
) {
    companion object {
        fun fromNewOffice(newOffice: NewOffice): Office {
            return Office(
                uuid = UUID.randomUUID(),
                city = newOffice.city,
                country = newOffice.country,
            )
        }
    }
}
