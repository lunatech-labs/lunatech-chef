package com.lunatech.chef.api.domain

import java.time.LocalDate

data class AttendeeReportEntry(
    val date: LocalDate,
    val name: String,
    val city: String,
    val externalAttendees: Int,
)
