package com.lunatech.chef.api.persistence.services

import com.lunatech.chef.api.domain.ReportEntry
import org.apache.poi.ss.usermodel.CellStyle
import org.apache.poi.xssf.usermodel.XSSFRow
import org.apache.poi.xssf.usermodel.XSSFSheet
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.ByteArrayOutputStream
import java.time.LocalDate
import java.time.temporal.WeekFields
import java.util.Locale

class ExcelService {
    data class ReportEntryWeek(
        val weekNumber: Int,
        val name: String,
        val city: String,
        val country: String,
    )

    private val startingRow = 2
    private val defaultColumnWidth = 30

    private fun toWeekNumber(date: LocalDate): Int {
        val weekfields = WeekFields.of(Locale.getDefault())
        return date.get(weekfields.weekOfWeekBasedYear())
    }

    fun exportToExcel(report: List<ReportEntry>): ByteArray {
        val reportWithWeek =
            report.map { entry -> ReportEntryWeek(toWeekNumber(entry.date), entry.name, entry.city, entry.country) }

        val workbook = XSSFWorkbook()
        val cellStyle = workbook.createCellStyle()
        val font = workbook.createFont()
        font.bold = true
        cellStyle.setFont(font)

        val out = ByteArrayOutputStream()
        try {
            writeReport(workbook, reportWithWeek, cellStyle)
            workbook.write(out)
        } finally {
            out.close()
            workbook.close()
        }

        return out.toByteArray()
    }

    private fun writeReport(
        workbook: XSSFWorkbook,
        report: List<ReportEntryWeek>,
        cellStyle: CellStyle,
    ) {
        if (report.isEmpty()) {
            workbook.createSheet("No attendants")
        } else {
            report
                .groupBy { (week, _, _, _) -> week }
                .forEach { (weekNumber, reportByWeek) ->
                    val sheet = workbook.createSheet("Week $weekNumber")
                    sheet.defaultColumnWidth = defaultColumnWidth

                    initializeSheet(
                        sheet = sheet,
                        weekNumber = weekNumber.toString(),
                        cellStyle = cellStyle,
                    )
                    reportByWeek.groupBy { (_, _, city, _) -> city }.toList().withIndex().forEach { indexedValue ->
                        val index = indexedValue.index
                        val (city, reportByCity) = indexedValue.value
                        val row = sheet.getRow(startingRow) ?: sheet.createRow(startingRow)
                        val users = reportByCity.map { (_, name, _, _) -> name }

                        writeSecondRow(
                            row,
                            "$city: (total ${users.size})",
                            cellStyle,
                            index,
                        )
                        writeUserData(sheet, users, index)
                    }
                }
        }
    }

    private fun initializeSheet(
        sheet: XSSFSheet,
        weekNumber: String,
        cellStyle: CellStyle,
    ) {
        val row = sheet.createRow(0)
        val firstRow = arrayOf("Week number:", weekNumber)

        firstRow.withIndex().forEach { (index, cellValue) ->
            val cell = row.createCell(index)
            cell.setCellValue(cellValue)

            if (cellValue == "Week number:") {
                cell.setCellStyle(cellStyle)
            }
        }
    }

    private fun writeSecondRow(
        row: XSSFRow,
        cellValue: String,
        cellStyle: CellStyle,
        columnIndex: Int,
    ) {
        val secondRowCell = row.createCell(columnIndex)
        secondRowCell.setCellValue(cellValue)
        secondRowCell.setCellStyle(cellStyle)
    }

    private fun writeUserData(
        sheet: XSSFSheet,
        users: List<String>,
        columnIndex: Int,
    ) {
        val rowSkips = 3
        users.withIndex().forEach { (index, user) ->
            val row = sheet.getRow(index + rowSkips) ?: sheet.createRow(index + rowSkips)

            val cell = row.createCell(columnIndex)
            cell.setCellValue(user)
        }
    }
}
