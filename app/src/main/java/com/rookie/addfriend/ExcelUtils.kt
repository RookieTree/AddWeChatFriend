package com.rookie.addfriend

import org.apache.poi.ss.usermodel.*
import org.apache.poi.xssf.usermodel.XSSFSheet
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.InputStream

/**
 * @description: Excel 工具类
 * @author: ODM
 * @date: 2020/4/11
 */
object ExcelUtils {
    /**
     * 读取Excel文件
     * @param file
     * @throws FileNotFoundException
     */
    @Throws(FileNotFoundException::class)
    fun readExcel(file: File?) {
        if (file == null) {
            logD("读取Excel出错，文件为空文件")
            return
        }
        val stream: InputStream = FileInputStream(file)
        try {
            val workbook = XSSFWorkbook(stream)
            val sheet: XSSFSheet = workbook.getSheetAt(0)
            val rowsCount: Int = sheet.getPhysicalNumberOfRows()
            val formulaEvaluator: FormulaEvaluator =
                workbook.getCreationHelper().createFormulaEvaluator()
            for (r in 0 until rowsCount) {
                val row: Row = sheet.getRow(r)
                val cellsCount = row.physicalNumberOfCells
                //每次读取一行的内容
                for (c in 0 until cellsCount) {
                    //将每一格子的内容转换为字符串形式
                    val value = getCellAsString(row, c)
                    val cellInfo = "r:$r; c:$c; v:$value"
                    logD(cellInfo)
                }
            }
        } catch (e: Exception) {
            /* proper exception handling to be here */
            logD(e.toString())
        }
    }

    @Throws(FileNotFoundException::class)
    suspend fun readExcelInContact(file: File?): List<ContactUser> {
        val constants = arrayListOf<ContactUser>()
        if (file == null) {
            logD("读取Excel出错，文件为空文件")
            return constants
        }
        val stream: InputStream = FileInputStream(file)
        try {
            val workbook = XSSFWorkbook(stream)
            val sheet: XSSFSheet = workbook.getSheetAt(0)
            val rowsCount: Int = sheet.physicalNumberOfRows
            val row0 = sheet.getRow(0)
            val name = getCellAsString(row0, 0)
            val phone =
                getCellAsString(row0, 1)
            val sayHi = getCellAsString(row0, 2)
            constants.add(ContactUser(phone, name, sayHi))
            //从第二行开始读
            for (r in 1 until rowsCount) {
                val row: Row = sheet.getRow(r)
                val name = getCellAsString(row, 0)
                val phone =
                    getCellAsString(row, 1)
                val sayHi = getCellAsString(row, 2)
                //每次读取一行的内容
                val contactUser = ContactUser(phone, name, sayHi)
                constants.add(contactUser)
            }
        } catch (e: Exception) {
            /* proper exception handling to be here */
            logD(e.toString())
        }
        return constants
    }

    /**
     * 读取excel文件中每一行的内容
     * @param row
     * @param c
     * @param formulaEvaluator
     * @return
     */
    private fun getCellAsString(row: Row, c: Int): String {
        var value = ""
        try {
            val cell = row.getCell(c)
            value = DataFormatter().formatCellValue(cell)
        } catch (e: NullPointerException) {
            /* proper error handling should be here */
            logD(e.toString())
        }
        return value
    }

    /**
     * 根据类型后缀名简单判断是否Excel文件
     * @param file 文件
     * @return 是否Excel文件
     */
    fun checkIfExcelFile(file: File?): Boolean {
        if (file == null) {
            return false
        }
        val name = file.name
        //”.“ 需要转义字符
        val list = name.split("\\.".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        //划分后的小于2个元素说明不可获取类型名
        if (list.size < 2) {
            return false
        }
        val typeName = list[list.size - 1]
        //满足xls或者xlsx才可以
        return "xls" == typeName || "xlsx" == typeName
    }
}