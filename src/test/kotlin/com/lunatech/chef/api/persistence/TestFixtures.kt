package com.lunatech.chef.api.persistence

import com.lunatech.chef.api.domain.Attendance
import com.lunatech.chef.api.domain.Dish
import com.lunatech.chef.api.domain.MenuWithDishesUuid
import com.lunatech.chef.api.domain.Office
import com.lunatech.chef.api.domain.RecurrentSchedule
import com.lunatech.chef.api.domain.Schedule
import com.lunatech.chef.api.domain.User
import java.time.LocalDate
import java.util.UUID

/**
 * Test data builders for creating domain objects with sensible defaults.
 * All builders return domain objects without inserting them into the database.
 */
object TestFixtures {
    // Office builder
    fun anOffice(
        uuid: UUID = UUID.randomUUID(),
        city: String = "Rotterdam",
        country: String = "Netherlands",
        isDeleted: Boolean = false,
    ): Office = Office(uuid, city, country, isDeleted)

    // Dish builder
    fun aDish(
        uuid: UUID = UUID.randomUUID(),
        name: String = "Test Pasta",
        description: String = "A delicious test dish",
        isVegetarian: Boolean = false,
        isHalal: Boolean = false,
        hasNuts: Boolean = false,
        hasSeafood: Boolean = false,
        hasPork: Boolean = false,
        hasBeef: Boolean = false,
        isGlutenFree: Boolean = false,
        isLactoseFree: Boolean = false,
        isDeleted: Boolean = false,
    ): Dish =
        Dish(
            uuid = uuid,
            name = name,
            description = description,
            isVegetarian = isVegetarian,
            isHalal = isHalal,
            hasNuts = hasNuts,
            hasSeafood = hasSeafood,
            hasPork = hasPork,
            hasBeef = hasBeef,
            isGlutenFree = isGlutenFree,
            isLactoseFree = isLactoseFree,
            isDeleted = isDeleted,
        )

    // Vegetarian dish shortcut
    fun aVegetarianDish(
        uuid: UUID = UUID.randomUUID(),
        name: String = "Vegetarian Salad",
        description: String = "A fresh vegetarian dish",
    ): Dish =
        aDish(
            uuid = uuid,
            name = name,
            description = description,
            isVegetarian = true,
        )

    // User builder
    fun aUser(
        uuid: UUID = UUID.randomUUID(),
        name: String = "Test User",
        emailAddress: String = "test.user@lunatech.nl",
        officeUuid: UUID? = null,
        isVegetarian: Boolean = false,
        hasHalalRestriction: Boolean = false,
        hasNutsRestriction: Boolean = false,
        hasSeafoodRestriction: Boolean = false,
        hasPorkRestriction: Boolean = false,
        hasBeefRestriction: Boolean = false,
        isGlutenIntolerant: Boolean = false,
        isLactoseIntolerant: Boolean = false,
        otherRestrictions: String = "",
        isInactive: Boolean = false,
        isDeleted: Boolean = false,
    ): User =
        User(
            uuid = uuid,
            name = name,
            emailAddress = emailAddress,
            officeUuid = officeUuid,
            isVegetarian = isVegetarian,
            hasHalalRestriction = hasHalalRestriction,
            hasNutsRestriction = hasNutsRestriction,
            hasSeafoodRestriction = hasSeafoodRestriction,
            hasPorkRestriction = hasPorkRestriction,
            hasBeefRestriction = hasBeefRestriction,
            isGlutenIntolerant = isGlutenIntolerant,
            isLactoseIntolerant = isLactoseIntolerant,
            otherRestrictions = otherRestrictions,
            isInactive = isInactive,
            isDeleted = isDeleted,
        )

    // Menu builder
    fun aMenu(
        uuid: UUID = UUID.randomUUID(),
        name: String = "Test Menu",
        dishesUuids: List<UUID> = emptyList(),
        isDeleted: Boolean = false,
    ): MenuWithDishesUuid = MenuWithDishesUuid(uuid, name, dishesUuids, isDeleted)

    // Schedule builder
    fun aSchedule(
        uuid: UUID = UUID.randomUUID(),
        menuUuid: UUID,
        date: LocalDate = LocalDate.now().plusDays(7),
        officeUuid: UUID,
        isDeleted: Boolean = false,
    ): Schedule = Schedule(uuid, menuUuid, date, officeUuid, isDeleted)

    // Recurrent schedule builder
    fun aRecurrentSchedule(
        uuid: UUID = UUID.randomUUID(),
        menuUuid: UUID,
        officeUuid: UUID,
        repetitionDays: Int = 7,
        nextDate: LocalDate = LocalDate.now().plusDays(7),
        isDeleted: Boolean = false,
    ): RecurrentSchedule = RecurrentSchedule(uuid, menuUuid, officeUuid, repetitionDays, nextDate, isDeleted)

    // Attendance builder
    fun anAttendance(
        uuid: UUID = UUID.randomUUID(),
        scheduleUuid: UUID,
        userUuid: UUID,
        isAttending: Boolean? = null,
        isDeleted: Boolean = false,
    ): Attendance = Attendance(uuid, scheduleUuid, userUuid, isAttending, isDeleted)

    // Helper to generate unique email addresses
    private var emailCounter = 0

    fun uniqueEmail(prefix: String = "user"): String {
        emailCounter++
        return "$prefix$emailCounter@lunatech.nl"
    }

    fun resetEmailCounter() {
        emailCounter = 0
    }
}
