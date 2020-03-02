package com.lunatech.chef.api.persistence.schemas

import java.util.UUID
import me.liuwj.ktorm.entity.Entity

interface Attendance : Entity<Attendance> {
    val uuid: UUID
    val scheduleUuid: Schedule
    val userUuid: User
    val isAttending: Boolean
}
