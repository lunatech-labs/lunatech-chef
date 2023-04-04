package com.lunatech.chef.api.domain

import java.time.LocalDate

data class ReportEntry(val date: LocalDate, val name: String, val city: String, val country: String)
