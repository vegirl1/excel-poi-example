package com.compname.lob.utils;

import java.io.InputStream;
import java.math.BigDecimal;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFDateUtil;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.ss.util.cellwalk.CellWalk;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.google.common.collect.Maps;

/**
 * Utility Class for Excel (XLSX format).
 * 
 * @author vegirl1 2015-05-28
 */
public final class ExcelUtils {

    public static final String    EXCEL_FORMAT              = "xlsx";
    public static final String    SHEET                     = "sheet";
    public static final String    SHEET_HEADER_FIRSTCELL    = "A1";
    public static final String    SHEET_TABLE_FIRSTCELL     = "A4";
    public static final String    CELL_DELIMITER            = ":";
    public static final String    NO_DATA_FOUND_LABEL       = "No Data Found";
    public static final String    AS_OF_LABEL               = "as of:";
    public static final String    AS_OF_CURRENT_DATE        = "{0,date,dd/MMM/yy}";
    public static final int       SHEET_HEADER_MERGED_CELLS = 4;

    public static final XSSFColor TABLE_HEADER_GREY         = new XSSFColor(new java.awt.Color(217, 217, 217));

    public static XSSFWorkbook getWorkBook(InputStream in) {
        XSSFWorkbook workbook;
        try {
            workbook = new XSSFWorkbook(in);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return workbook;
    }

    /**
     * @author vegirl1; get cell range from given sheet
     * 
     * @param XSSFSheet sheet
     * @param String cellRange
     * @return Map<String, String> cell values ex: {B2=Toto&Ko, B3=John Smith}
     */
    public static Map<String, String> getCellRangeContent(XSSFSheet sheet, CellRangeAddress refCelRange) {

        if (sheet == null || refCelRange == null) {
            return null;
        }

        CellWalk cellWalk = new CellWalk(sheet, refCelRange);
        CellHandlerUtils cellHandler = new CellHandlerUtils();
        Map<String, String> cellValues = Maps.newLinkedHashMap();

        cellHandler.setCellValues(cellValues);
        cellWalk.setTraverseEmptyCells(true);
        cellWalk.traverse(cellHandler);

        return cellValues;

    }

    /**
     * @author vegirl1; get First Cell Reference
     * 
     * @param String cellRange ex: A24:J24
     * 
     * @return CellReference of A24
     */
    public static CellReference getFirstCellReferenceFromRange(String cellRange) {
        return new CellReference(StringUtils.substringBefore(cellRange, CELL_DELIMITER));
    }

    /**
     * @author vegirl1; get Last Cell Reference
     * 
     * @param String cellRange ex: A24:J24
     * 
     * @return CellReference of J24
     */
    public static CellReference getLastCellReferenceFromRange(String cellRange) {
        return new CellReference(StringUtils.substringAfter(cellRange, CELL_DELIMITER));
    }

    public static String getCellContentAsString(Cell cell) {

        if (cell == null) {
            return null;
        }

        String result = null;

        try {
            if (cell.getCellType() == Cell.CELL_TYPE_STRING) {
                result = cell.toString().trim();
            } else if (cell.getCellType() == Cell.CELL_TYPE_NUMERIC) {

                if (HSSFDateUtil.isCellDateFormatted(cell)) {
                    SimpleDateFormat sdf = new SimpleDateFormat(SqlDateUtils.SQL_DATE_FORMAT);
                    Date dateCell = cell.getDateCellValue();
                    result = sdf.format(dateCell);
                } else {
                    result = new BigDecimal(cell.getNumericCellValue()).toString();
                    result = StringUtils.substringBefore(result, ".");
                }
            }
        } catch (NumberFormatException e) {
            result = null;
        }

        return result;
    }

    public static XSSFCellStyle getDefaultCellStyle(XSSFWorkbook workbook) {
        XSSFCellStyle cellStyle = workbook.createCellStyle();
        return cellStyle;
    }

    public static XSSFCellStyle getTableHeaderCellStyle(XSSFWorkbook workbook) {
        XSSFCellStyle cellStyle = workbook.createCellStyle();
        cellStyle.setBorderTop(XSSFCellStyle.BORDER_MEDIUM);
        cellStyle.setBorderRight(XSSFCellStyle.BORDER_THIN);
        cellStyle.setBorderLeft(XSSFCellStyle.BORDER_THIN);
        cellStyle.setBorderBottom(XSSFCellStyle.BORDER_MEDIUM);
        cellStyle.setFillForegroundColor(TABLE_HEADER_GREY);
        cellStyle.setFillPattern(XSSFCellStyle.SOLID_FOREGROUND);

        XSSFFont font = workbook.createFont();
        font.setBold(true);
        cellStyle.setFont(font);

        cellStyle.setWrapText(true);
        cellStyle.setVerticalAlignment(XSSFCellStyle.VERTICAL_TOP);

        return cellStyle;
    }

    public static XSSFCellStyle getTableCellStyle(XSSFWorkbook workbook) {
        XSSFCellStyle cellStyle = workbook.createCellStyle();
        cellStyle.setBorderTop(XSSFCellStyle.BORDER_THIN);
        cellStyle.setBorderRight(XSSFCellStyle.BORDER_THIN);
        cellStyle.setBorderLeft(XSSFCellStyle.BORDER_THIN);
        cellStyle.setBorderBottom(XSSFCellStyle.BORDER_THIN);
        return cellStyle;
    }

    /**
     * buildTableRowHeader
     * 
     * @param sheet
     * @param cellStyle
     * @param creationHelper
     * @param headerCellRef
     * @param headerNames
     */
    public static void buildSheetHeader(XSSFSheet sheet, XSSFCellStyle cellStyle, CreationHelper creationHelper,
            CellReference firstCellRef, String headerText) {

        XSSFRow rowHeader = sheet.createRow(firstCellRef.getRow());
        XSSFCell cell = rowHeader.createCell(firstCellRef.getCol());
        cell.setCellValue(creationHelper.createRichTextString(headerText));
        sheet.addMergedRegion(new CellRangeAddress(firstCellRef.getRow(), firstCellRef.getRow(), firstCellRef.getCol(),
                firstCellRef.getCol() + SHEET_HEADER_MERGED_CELLS));

        // set as of: dd/Month/Year
        XSSFRow rowHeaderDate = sheet.createRow(firstCellRef.getRow() + 1);
        cell = rowHeaderDate.createCell(firstCellRef.getCol());
        cell.setCellValue(creationHelper.createRichTextString(AS_OF_LABEL));
        cellStyle.setAlignment(XSSFCellStyle.ALIGN_RIGHT);
        cell.setCellStyle(cellStyle);

        cell = rowHeaderDate.createCell(firstCellRef.getCol() + 1);
        cell.setCellValue(creationHelper.createRichTextString(MessageFormat.format(AS_OF_CURRENT_DATE,
                new GregorianCalendar().getTime())));
    }

    /**
     * buildTableRowHeader
     * 
     * @param sheet
     * @param cellStyle
     * @param creationHelper
     * @param headerCellRef
     * @param headerNames
     */
    public static void buildTableRowHeader(XSSFSheet sheet, XSSFCellStyle cellStyle, CreationHelper creationHelper,
            CellReference firstCellRef, List<String> headerNames) {

        int i = 0;
        XSSFRow rowHeader = sheet.createRow(firstCellRef.getRow());
        for (String columnName : headerNames) {
            XSSFCell cell = rowHeader.createCell(firstCellRef.getCol() + i);
            cell.setCellValue(creationHelper.createRichTextString(columnName));
            cell.setCellStyle(cellStyle);
            i++;

        }
    }

    /**
     * buildTableRows
     * 
     * @param cellStyle
     * @param creationHelper
     * @param sheet
     * @param tableColumns
     * @param tableValues
     * @param tableCellRef
     */
    public static void buildTableRows(XSSFSheet sheet, XSSFCellStyle cellStyle, CreationHelper creationHelper,
            CellReference firstCellRef, List<String> tableColumns, List<Map<String, Object>> tableValues) {
        int colIndex = 0;
        int rowIndex = 0;

        for (Map<String, Object> map : tableValues) {

            XSSFRow rowTable = sheet.createRow(firstCellRef.getRow() + rowIndex);

            for (String columnName : tableColumns) {
                XSSFCell cell = rowTable.createCell(firstCellRef.getCol() + colIndex);
                Object rowValue = map.get(columnName);
                cell.setCellValue(creationHelper.createRichTextString(rowValue == null ? StringUtils.EMPTY : rowValue.toString()));
                cell.setCellStyle(cellStyle);
                colIndex++;
            }
            colIndex = 0;
            rowIndex++;
        }

    }

    public static void buildTableRowNoDataFound(XSSFSheet sheet, XSSFCellStyle cellStyle, CreationHelper creationHelper,
            CellReference firstCellRef, int celsToMerge) {

        XSSFRow rowTable = sheet.createRow(firstCellRef.getRow());
        XSSFCell cell = rowTable.createCell(firstCellRef.getCol());
        cell.setCellValue(creationHelper.createRichTextString(NO_DATA_FOUND_LABEL));

        cellStyle.setAlignment(XSSFCellStyle.ALIGN_CENTER);
        cell.setCellStyle(cellStyle);

        sheet.addMergedRegion(new CellRangeAddress(firstCellRef.getRow(), firstCellRef.getRow(), firstCellRef.getCol(),
                firstCellRef.getCol() + celsToMerge));

    }

    /**
     * autoSizeColumns
     * 
     * @param sheet
     * @param tableColumns
     * @param headerCellRef
     */
    public static void autoSizeColumns(XSSFSheet sheet, CellReference headerCellRef, int columnCnt) {
        for (int j = 0; j < columnCnt; j++) {
            sheet.autoSizeColumn(headerCellRef.getCol() + j);
        }
    }

}
