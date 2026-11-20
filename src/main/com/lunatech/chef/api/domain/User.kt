package com.lunatech.chef.api.domain

import java.util.UUID

data class NewUser(
    val name: String,
    val emailAddress: String,
    val officeUuid: UUID?,
    val optOutLunches: Boolean = false,
)

data class User(
    val uuid: UUID,
    val name: String,
    val emailAddress: String,
    val officeUuid: UUID?,
    val optOutLunches: Boolean = false,
    val isInactive: Boolean = false,
    val isDeleted: Boolean = false,
) {
    companion object {
        fun fromNewUser(newUser: NewUser): User =
            User(
                uuid = UUID.randomUUID(),
                name = newUser.name,
                emailAddress = newUser.emailAddress,
                officeUuid = newUser.officeUuid,
                optOutLunches = newUser.optOutLunches,
            )
    }
}
