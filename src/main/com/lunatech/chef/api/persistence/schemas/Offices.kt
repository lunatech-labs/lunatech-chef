package com.lunatech.chef.api.persistence.schemas

import com.lunatech.chef.api.domain.Office
import org.ktorm.dsl.QueryRowSet
import org.ktorm.schema.BaseTable
import org.ktorm.schema.boolean
import org.ktorm.schema.uuid
import org.ktorm.schema.varchar

object Offices : BaseTable<Office>("offices") {
    val uuid = uuid("uuid").primaryKey()
    val city = varchar("city")
    val country = varchar("country")
    val isDeleted = boolean("is_deleted")

    override fun doCreateEntity(
        row: QueryRowSet,
        withReferences: Boolean,
    ) = Office(
        uuid = row[uuid] ?: DEFAULT_UUID,
        city = row[city] ?: DEFAULT_STRING,
        country = row[country] ?: DEFAULT_STRING,
        isDeleted = row[isDeleted] ?: DEFAULT_FALSE,
    )
}
