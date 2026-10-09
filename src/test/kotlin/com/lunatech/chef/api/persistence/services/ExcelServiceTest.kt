package com.lunatech.chef.api.persistence.services

import com.lunatech.chef.api.domain.AttendeeReportEntry
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.io.ByteArrayInputStream
import java.time.LocalDate
import java.time.temporal.WeekFields
import java.util.Locale

class ExcelServiceTest {
    private val excelService = ExcelService()

    private fun weekNumberOf(date: LocalDate): Int {
        val weekFields = WeekFields.of(Locale.getDefault())
        return date.get(weekFields.weekOfWeekBasedYear())
    }

    private fun workbookOf(report: List<AttendeeReportEntry>) = XSSFWorkbook(ByteArrayInputStream(excelService.exportToExcel(report)))

    @Test
    fun `creates a single 'No attendants' sheet when the report is empty`() {
        workbookOf(emptyList()).use { workbook ->
            assertEquals(1, workbook.numberOfSheets)
            assertEquals("No attendants", workbook.getSheetAt(0).sheetName)
        }
    }

    @Test
    fun `names the sheet after the week number and writes it as a bold header`() {
        val date = LocalDate.of(2024, 1, 8)
        val report = listOf(AttendeeReportEntry(date, "Alice", "Rotterdam", externalAttendees = 0))

        workbookOf(report).use { workbook ->
            val expectedWeek = weekNumberOf(date)
            val sheet = workbook.getSheetAt(0)
            assertEquals("Week $expectedWeek", sheet.sheetName)

            val labelCell = sheet.getRow(0).getCell(0)
            val valueCell = sheet.getRow(0).getCell(1)
            assertEquals("Week number:", labelCell.stringCellValue)
            assertEquals(expectedWeek.toString(), valueCell.stringCellValue)

            assertTrue(workbook.getFontAt(labelCell.cellStyle.fontIndex).bold)
            assertFalse(workbook.getFontAt(valueCell.cellStyle.fontIndex).bold)
        }
    }

    @Test
    fun `groups entries from different weeks into their own sheets`() {
        val firstWeekDate = LocalDate.of(2024, 1, 8)
        val secondWeekDate = LocalDate.of(2024, 1, 22)
        val report =
            listOf(
                AttendeeReportEntry(firstWeekDate, "Alice", "Rotterdam", externalAttendees = 0),
                AttendeeReportEntry(secondWeekDate, "Bob", "Rotterdam", externalAttendees = 0),
            )

        workbookOf(report).use { workbook ->
            assertEquals(2, workbook.numberOfSheets)
            val sheetNames = (0 until workbook.numberOfSheets).map { workbook.getSheetAt(it).sheetName }
            assertEquals(
                setOf("Week ${weekNumberOf(firstWeekDate)}", "Week ${weekNumberOf(secondWeekDate)}"),
                sheetNames.toSet(),
            )
        }
    }

    @Test
    fun `lists every internal attendee and writes the combined total next to the attendee count headers`() {
        val date = LocalDate.of(2024, 1, 8)
        val report =
            listOf(
                AttendeeReportEntry(date, "Alice", "Rotterdam", externalAttendees = 3),
                AttendeeReportEntry(date, "Bob", "Rotterdam", externalAttendees = 3),
            )

        workbookOf(report).use { workbook ->
            val sheet = workbook.getSheetAt(0)
            val headerRow = sheet.getRow(2)
            assertEquals("Rotterdam: 2 internal attendees", headerRow.getCell(0).stringCellValue)
            assertEquals("Rotterdam: 3 external attendees", headerRow.getCell(1).stringCellValue)
            assertEquals("Total: 5", headerRow.getCell(2).stringCellValue)
            assertTrue(workbook.getFontAt(headerRow.getCell(2).cellStyle.fontIndex).bold)

            assertEquals("Alice", sheet.getRow(4).getCell(0).stringCellValue)
            assertEquals("Bob", sheet.getRow(5).getCell(0).stringCellValue)
        }
    }

    @Test
    fun `keeps each city's headers, attendees and total in their own non-overlapping columns`() {
        val date = LocalDate.of(2024, 1, 8)
        val report =
            listOf(
                AttendeeReportEntry(date, "Alice", "Rotterdam", externalAttendees = 3),
                AttendeeReportEntry(date, "Bob", "Rotterdam", externalAttendees = 3),
                AttendeeReportEntry(date, "Carl", "Amsterdam", externalAttendees = 2),
            )

        workbookOf(report).use { workbook ->
            val sheet = workbook.getSheetAt(0)
            val headerRow = sheet.getRow(2)
            assertEquals("Rotterdam: 2 internal attendees", headerRow.getCell(0).stringCellValue)
            assertEquals("Rotterdam: 3 external attendees", headerRow.getCell(1).stringCellValue)
            assertEquals("Total: 5", headerRow.getCell(2).stringCellValue)
            assertEquals("Amsterdam: 1 internal attendees", headerRow.getCell(3).stringCellValue)
            assertEquals("Amsterdam: 2 external attendees", headerRow.getCell(4).stringCellValue)
            assertEquals("Total: 3", headerRow.getCell(5).stringCellValue)

            assertEquals("Alice", sheet.getRow(4).getCell(0).stringCellValue)
            assertEquals("Bob", sheet.getRow(5).getCell(0).stringCellValue)
            assertEquals("Carl", sheet.getRow(4).getCell(3).stringCellValue)
        }
    }

    @Test
    fun `applies the default column width to every sheet`() {
        val date = LocalDate.of(2024, 1, 8)
        val report = listOf(AttendeeReportEntry(date, "Alice", "Rotterdam", externalAttendees = 0))

        workbookOf(report).use { workbook ->
            assertEquals(30, workbook.getSheetAt(0).defaultColumnWidth)
        }
    }
}
