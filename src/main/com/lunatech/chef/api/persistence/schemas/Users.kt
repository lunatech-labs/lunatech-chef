package com.lunatech.chef.api.persistence.schemas

import com.lunatech.chef.api.domain.User
import org.ktorm.dsl.QueryRowSet
import org.ktorm.schema.BaseTable
import org.ktorm.schema.boolean
import org.ktorm.schema.uuid
import org.ktorm.schema.varchar

object Users : BaseTable<User>("users") {
    val uuid = uuid("uuid").primaryKey()
    val name = varchar("name")
    val emailAddress = varchar("email_address")
    val officeUuid = uuid("office_uuid")
    val optOutLunches = boolean("opt_out_lunches")
    val isInactive = boolean("is_inactive")
    val isDeleted = boolean("is_deleted")

    override fun doCreateEntity(
        row: QueryRowSet,
        withReferences: Boolean,
    ) = User(
        uuid = row[uuid] ?: DEFAULT_UUID,
        name = row[name] ?: DEFAULT_STRING,
        emailAddress = row[emailAddress] ?: DEFAULT_STRING,
        officeUuid = row[officeUuid],
        optOutLunches = row[optOutLunches] ?: DEFAULT_FALSE,
        isInactive = row[isInactive] ?: DEFAULT_FALSE,
        isDeleted = row[isDeleted] ?: DEFAULT_FALSE,
    )
}
