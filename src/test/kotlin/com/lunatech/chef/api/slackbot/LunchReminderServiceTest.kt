package com.lunatech.chef.api.slackbot

import com.lunatech.chef.api.persistence.TestDatabase
import com.lunatech.chef.api.persistence.TestFixtures.aDish
import com.lunatech.chef.api.persistence.TestFixtures.aMenu
import com.lunatech.chef.api.persistence.TestFixtures.aSchedule
import com.lunatech.chef.api.persistence.TestFixtures.aUser
import com.lunatech.chef.api.persistence.TestFixtures.anAttendance
import com.lunatech.chef.api.persistence.TestFixtures.anOffice
import com.lunatech.chef.api.persistence.TestFixtures.uniqueEmail
import com.lunatech.chef.api.persistence.services.AttendancesForSlackbotService
import com.lunatech.chef.api.persistence.services.AttendancesService
import com.lunatech.chef.api.persistence.services.DishesService
import com.lunatech.chef.api.persistence.services.MenusService
import com.lunatech.chef.api.persistence.services.OfficesService
import com.lunatech.chef.api.persistence.services.SchedulesService
import com.lunatech.chef.api.persistence.services.UsersService
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.util.UUID

private class FakeSlackApi(
    private val users: List<SlackUser>,
    private val failOpenFor: Set<String> = emptySet(),
    private val nullChannelFor: Set<String> = emptySet(),
    private val postMessageResult: Boolean = true,
) : SlackApi {
    val openedConversations = mutableListOf<String>()
    val postedMessages = mutableListOf<Triple<String, String, String>>()

    override suspend fun usersList(): List<SlackUser> = users

    override suspend fun openConversation(userId: String): String? {
        if (userId in failOpenFor) throw RuntimeException("boom")
        if (userId in nullChannelFor) return null
        openedConversations.add(userId)
        return "channel-$userId"
    }

    override suspend fun postMessage(
        channel: String,
        text: String,
        attachmentsJson: String,
    ): Boolean {
        postedMessages.add(Triple(channel, text, attachmentsJson))
        return postMessageResult
    }
}

class LunchReminderServiceTest {
    private lateinit var attendancesForSlackbotService: AttendancesForSlackbotService
    private lateinit var attendancesService: AttendancesService
    private lateinit var schedulesService: SchedulesService
    private lateinit var usersService: UsersService

    private lateinit var officeUuid: UUID
    private lateinit var menuUuid: UUID
    private lateinit var userUuid: UUID
    private lateinit var userEmail: String

    @BeforeEach
    fun setup() {
        val database = TestDatabase.getDatabase()
        TestDatabase.resetDatabase()
        attendancesForSlackbotService = AttendancesForSlackbotService(database)
        val officesService = OfficesService(database)
        val dishesService = DishesService(database)
        val menusService = MenusService(database)
        schedulesService = SchedulesService(database)
        usersService = UsersService(database)
        attendancesService = AttendancesService(database, usersService)

        val office = anOffice(city = "Rotterdam")
        officesService.insert(office)
        officeUuid = office.uuid
        val dish = aDish(name = "Pasta", isVegetarian = true)
        dishesService.insert(dish)
        val menu = aMenu(name = "Lunch Menu", dishesUuids = listOf(dish.uuid))
        menusService.insert(menu)
        menuUuid = menu.uuid
        userEmail = uniqueEmail("bot")
        val user = aUser(name = "Bot Target", emailAddress = userEmail, officeUuid = officeUuid)
        usersService.insert(user)
        userUuid = user.uuid
    }

    private fun insertMissingAttendance(daysFromNow: Long) {
        val schedule = aSchedule(menuUuid = menuUuid, date = LocalDate.now().plusDays(daysFromNow), officeUuid = officeUuid)
        schedulesService.insert(schedule)
        attendancesService.insert(anAttendance(scheduleUuid = schedule.uuid, userUuid = userUuid, isAttending = null))
    }

    @Test
    fun `sends one message per missing attendance but opens one conversation per user`() =
        runBlocking {
            insertMissingAttendance(1)
            insertMissingAttendance(2)
            val slack = FakeSlackApi(listOf(SlackUser("U1", false, userEmail)))

            LunchReminderService(attendancesForSlackbotService, usersService, slack).sendReminders()

            assertEquals(listOf("U1"), slack.openedConversations)
            assertEquals(2, slack.postedMessages.size)
            assertTrue(slack.postedMessages.all { it.first == "channel-U1" })
            assertTrue(slack.postedMessages.all { it.second == SlackMessages.SALUTATION })
            assertTrue(slack.postedMessages.all { it.third.contains("Rotterdam") })
        }

    @Test
    fun `skips users without a slack match and deleted slack users`() =
        runBlocking {
            insertMissingAttendance(1)
            val slack = FakeSlackApi(listOf(SlackUser("U9", true, userEmail)))

            LunchReminderService(attendancesForSlackbotService, usersService, slack).sendReminders()

            assertEquals(0, slack.postedMessages.size)
        }

    @Test
    fun `a failing user does not stop the others`() =
        runBlocking {
            insertMissingAttendance(1)
            val otherEmail = uniqueEmail("other")
            val other = aUser(name = "Other", emailAddress = otherEmail, officeUuid = officeUuid)
            usersService.insert(other)
            val schedule = aSchedule(menuUuid = menuUuid, date = LocalDate.now().plusDays(1), officeUuid = officeUuid)
            schedulesService.insert(schedule)
            attendancesService.insert(anAttendance(scheduleUuid = schedule.uuid, userUuid = other.uuid, isAttending = null))

            val slack =
                FakeSlackApi(
                    listOf(SlackUser("U1", false, userEmail), SlackUser("U2", false, otherEmail)),
                    failOpenFor = setOf("U1"),
                )

            LunchReminderService(attendancesForSlackbotService, usersService, slack).sendReminders()

            assertEquals(1, slack.postedMessages.size)
            assertEquals("channel-U2", slack.postedMessages[0].first)
        }

    @Test
    fun `matches chef and slack emails case-insensitively`() =
        runBlocking {
            insertMissingAttendance(1)
            val slack = FakeSlackApi(listOf(SlackUser("U1", false, userEmail.uppercase())))

            LunchReminderService(attendancesForSlackbotService, usersService, slack).sendReminders()

            assertEquals(listOf("U1"), slack.openedConversations)
            assertEquals(1, slack.postedMessages.size)
        }

    @Test
    fun `does nothing when there are no missing attendances`() =
        runBlocking {
            val slack = FakeSlackApi(listOf(SlackUser("U1", false, userEmail)))

            LunchReminderService(attendancesForSlackbotService, usersService, slack).sendReminders()

            assertEquals(0, slack.openedConversations.size)
            assertEquals(0, slack.postedMessages.size)
        }

    @Test
    fun `a false postMessage result does not stop remaining messages`() =
        runBlocking {
            insertMissingAttendance(1)
            insertMissingAttendance(2)
            val slack = FakeSlackApi(listOf(SlackUser("U1", false, userEmail)), postMessageResult = false)

            LunchReminderService(attendancesForSlackbotService, usersService, slack).sendReminders()

            assertEquals(2, slack.postedMessages.size)
        }

    @Test
    fun `an empty usersList leads to zero openConversation calls and no exception`() =
        runBlocking {
            insertMissingAttendance(1)
            val slack = FakeSlackApi(emptyList())

            LunchReminderService(attendancesForSlackbotService, usersService, slack).sendReminders()

            assertEquals(0, slack.openedConversations.size)
            assertEquals(0, slack.postedMessages.size)
        }

    // Enough active Slack members to pass the sync sanity guard
    private fun manySlackUsers(count: Int): List<SlackUser> = List(count) { SlackUser("F$it", false, "filler$it@lunatech.nl") }

    @Test
    fun `deactivates chef users that are not active in slack`() =
        runBlocking {
            insertMissingAttendance(1)
            val gone = aUser(name = "Gone", emailAddress = uniqueEmail("gone"), officeUuid = officeUuid)
            usersService.insert(gone)
            val slack = FakeSlackApi(manySlackUsers(100) + SlackUser("U1", false, userEmail))

            LunchReminderService(attendancesForSlackbotService, usersService, slack).sendReminders()

            assertTrue(usersService.getByUuid(gone.uuid).first().isInactive)
            assertFalse(usersService.getByUuid(userUuid).first().isInactive)
        }

    @Test
    fun `a deactivated slack account counts as absent`() =
        runBlocking {
            insertMissingAttendance(1)
            val goneEmail = uniqueEmail("gone")
            val gone = aUser(name = "Gone", emailAddress = goneEmail, officeUuid = officeUuid)
            usersService.insert(gone)
            val slack = FakeSlackApi(manySlackUsers(100) + SlackUser("U1", false, userEmail) + SlackUser("U9", true, goneEmail))

            LunchReminderService(attendancesForSlackbotService, usersService, slack).sendReminders()

            assertTrue(usersService.getByUuid(gone.uuid).first().isInactive)
        }

    @Test
    fun `reactivates inactive chef users found in slack`() =
        runBlocking {
            insertMissingAttendance(1)
            val backEmail = uniqueEmail("back")
            val back = aUser(name = "Back", emailAddress = backEmail, officeUuid = officeUuid, isInactive = true)
            usersService.insert(back)
            val slack = FakeSlackApi(manySlackUsers(100) + SlackUser("U1", false, userEmail) + SlackUser("U2", false, backEmail))

            LunchReminderService(attendancesForSlackbotService, usersService, slack).sendReminders()

            assertFalse(usersService.getByUuid(back.uuid).first().isInactive)
        }

    @Test
    fun `matches activity sync emails case-insensitively`() =
        runBlocking {
            insertMissingAttendance(1)
            val mixedEmail = uniqueEmail("mixed")
            val mixed = aUser(name = "Mixed", emailAddress = mixedEmail, officeUuid = officeUuid)
            usersService.insert(mixed)
            val slack =
                FakeSlackApi(
                    manySlackUsers(100) + SlackUser("U1", false, userEmail) + SlackUser("U2", false, mixedEmail.uppercase()),
                )

            LunchReminderService(attendancesForSlackbotService, usersService, slack).sendReminders()

            assertFalse(usersService.getByUuid(mixed.uuid).first().isInactive)
        }

    @Test
    fun `skips the activity sync when the slack list is implausibly small`() =
        runBlocking {
            insertMissingAttendance(1)
            val gone = aUser(name = "Gone", emailAddress = uniqueEmail("gone"), officeUuid = officeUuid)
            usersService.insert(gone)
            val slack = FakeSlackApi(listOf(SlackUser("U1", false, userEmail)))

            LunchReminderService(attendancesForSlackbotService, usersService, slack).sendReminders()

            assertFalse(usersService.getByUuid(gone.uuid).first().isInactive)
            // reminders themselves still go out
            assertEquals(1, slack.postedMessages.size)
        }

    @Test
    fun `sync updates are visible through the by-email cache`() =
        runBlocking {
            insertMissingAttendance(1)
            val goneEmail = uniqueEmail("gone")
            val gone = aUser(name = "Gone", emailAddress = goneEmail, officeUuid = officeUuid)
            usersService.insert(gone)
            // prime the cache with the still-active row
            assertFalse(usersService.getByEmailAddress(goneEmail)!!.isInactive)
            val slack = FakeSlackApi(manySlackUsers(100) + SlackUser("U1", false, userEmail))

            LunchReminderService(attendancesForSlackbotService, usersService, slack).sendReminders()

            assertTrue(usersService.getByEmailAddress(goneEmail)!!.isInactive)
        }

    @Test
    fun `openConversation returning null does not post and does not affect other users`() =
        runBlocking {
            insertMissingAttendance(1)
            val otherEmail = uniqueEmail("other")
            val other = aUser(name = "Other", emailAddress = otherEmail, officeUuid = officeUuid)
            usersService.insert(other)
            val schedule = aSchedule(menuUuid = menuUuid, date = LocalDate.now().plusDays(1), officeUuid = officeUuid)
            schedulesService.insert(schedule)
            attendancesService.insert(anAttendance(scheduleUuid = schedule.uuid, userUuid = other.uuid, isAttending = null))

            val slack =
                FakeSlackApi(
                    listOf(SlackUser("U1", false, userEmail), SlackUser("U2", false, otherEmail)),
                    nullChannelFor = setOf("U1"),
                )

            LunchReminderService(attendancesForSlackbotService, usersService, slack).sendReminders()

            assertEquals(1, slack.postedMessages.size)
            assertEquals("channel-U2", slack.postedMessages[0].first)
        }
}
