package com.lunatech.chef.api.domain

import java.util.UUID

data class MenuName(val uuid: UUID, val name: String, val isDeleted: Boolean = false)
