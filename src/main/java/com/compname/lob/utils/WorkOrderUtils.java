package com.compname.lob.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.util.IOUtils;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;


import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.io.Files;
import com.compname.lob.domain.config.AbstractProperties;
import com.compname.lob.domain.config.EligibilityConfig;
import com.compname.lob.domain.config.WorkOrderSheetNames;
import com.compname.lob.domain.workorder.ClaimsWorkOrder;
import com.compname.lob.domain.workorder.DrugClaimsWorkOrder;
import com.compname.lob.domain.workorder.EligibilityWorkOrder;
import com.compname.lob.domain.workorder.WorkOrderRequestDate;

/**
 * WorkOrderUtils
 * 
 * @author vegirl1
 * @since Jun 5, 2015
 * @version $Revision$
 */
public final class WorkOrderUtils {

    private static final Logger LOG = LoggerFactory.getLogger(WorkOrderUtils.class);

    public static InputStream getWorkOrderInputStream(File file) {

        InputStream in;
        try {
            in = new FileInputStream(file);
        } catch (FileNotFoundException ex) {
            LOG.error("File '{}' not found. Error message :{}", file.getName(), ex.getLocalizedMessage());
            in = null;
        }
        return in;
    }

    public static void moveFiles(List<File> sourceFiles, String targetFolder, boolean addTimeStamp) {
        SimpleDateFormat dateTimeformat = new SimpleDateFormat(AbstractProperties.DATETIME_FORMAT);
        for (File file : sourceFiles) {
            String targetFile = targetFolder + AbstractProperties.SLASH + file.getName();

            if (addTimeStamp) {
                targetFile = targetFolder + AbstractProperties.SLASH + FilenameUtils.getBaseName(file.getName())
                        + AbstractProperties.UNDERSCORE + dateTimeformat.format(new GregorianCalendar().getTime())
                        + AbstractProperties.DOT + FilenameUtils.getExtension(file.getName());
            }

            try {
                Files.move(file, new File(targetFile));
            } catch (Exception e) {
                LOG.error("Failed to move files '{}'. Error message :", sourceFiles.toString(), e.getLocalizedMessage());
                throw new RuntimeException(e);
            }
        }
    }

    public static EligibilityWorkOrder getEligibilityWorkOrderContent(InputStream inputStream, EligibilityConfig config)
            throws ServiceException {

        EligibilityWorkOrder eligibilityWorkOrder = new EligibilityWorkOrder();

        Map<String, List<Map<String, String>>> sheetCells;

        String sheetIndex = StringUtils.EMPTY;
        XSSFWorkbook workbook = ExcelUtils.getWorkBook(inputStream);
        try {
            for (String sheetName : config.getSheetToProcess()) {

                XSSFSheet sheet = workbook.getSheet(sheetName);
                if (sheet == null) {
                    LOG.info("The sheet '{}' is not found in the provided Eligibility Excel WorkOrder ", sheetName);
                    if (WorkOrderSheetNames.getBySheetName(sheetName).isIsSheetMandatory()) {
                        throw new Exception("The Mandatory sheet (" + sheetName + ") is missing.");
                    } else {
                        // try to read the next sheet
                        continue;
                    }
                }

                sheetIndex = config.getStreamName() + AbstractProperties.DOT + ExcelUtils.SHEET + AbstractProperties.DOT
                        + WorkOrderSheetNames.getBySheetName(sheetName).name();

                LOG.debug("Processing sheet: '{}' ", sheet.getSheetName());
                sheetCells = Maps.newLinkedHashMap();
                for (String cell : config.getSheetCells().get(sheetIndex)) {
                    if (StringUtils.containsIgnoreCase(cell, AbstractProperties.WORK_ORDER_TABLE)) {

                        CellRangeAddress refCelRange = getTableCellRange(sheet, sheetIndex, cell,
                                config.getSheetTableDescriptions());
                        if (refCelRange != null) {
                            String tableName = StringUtils.substringBefore(cell, AbstractProperties.WORK_ORDER_TABLE_DELIMITER);
                            sheetCells.put(tableName, getTableCellContent(sheet, refCelRange));
                        }
                    } else {

                        CellRangeAddress cellRangeAddress = CellRangeAddress.valueOf(cell);
                        Map<String, String> cellMap = ExcelUtils.getCellRangeContent(sheet, cellRangeAddress);
                        List<Map<String, String>> cellColumns = Lists.newArrayList();
                        cellColumns.add(cellMap);
                        sheetCells.put(cell, cellColumns);
                    }
                }

                LOG.debug("Mapping sheet '{}' data's to EligibilityWorkOrder", sheet.getSheetName());
                mapSheetDatasToEligibilityWorkOrder(WorkOrderSheetNames.getBySheetName(sheetName), sheetCells,
                        eligibilityWorkOrder, config);
            }

        } catch (Exception ex) {
            String errorInfoMessage = "Exception when reading Excel Workbook : " + "\n" + ex.getMessage();
            LOG.error(errorInfoMessage);

            throw new ServiceException(errorInfoMessage, Lists.newArrayList(ErrorInfo.createWith(
                    "WorkOrderUtils.getEligibilityWorkOrderContent()", AbstractProperties.FAMILY_GROUP_BENEFIT, ErrorInfo.ERROR,
                    errorInfoMessage)), ExceptionType.BUSINESS_EXCEPTION);
        } finally {
            IOUtils.closeQuietly(workbook);
        }
        return eligibilityWorkOrder;
    }

    public static void mapSheetDatasToEligibilityWorkOrder(WorkOrderSheetNames sheetName,
            Map<String, List<Map<String, String>>> sheetCellValues, EligibilityWorkOrder eligibilityWorkOrder,
            EligibilityConfig eligibilityConfig) {

        List<Map<String, String>> cellValues = Lists.newArrayList();
        for (String key : sheetCellValues.keySet()) {
            cellValues.addAll(sheetCellValues.get(key));
        }

        switch (sheetName) {
            case eligibility_work_order:

                setEligibilityWorkOrder(sheetCellValues, eligibilityWorkOrder, eligibilityConfig, cellValues);
                break;

            case benefit_mapping:
                eligibilityWorkOrder.setBenefitMaps(findTablesValues(eligibilityConfig.getBenefitMappingTables(), sheetCellValues));
                break;

            case certificate_mapping:
                eligibilityWorkOrder.setCertConversionRequired(findCellValue("B2", cellValues));
                eligibilityWorkOrder.setCertAutoGenerated(findCellValue("B3", cellValues));
                eligibilityWorkOrder.setStartingCertNumber(findCellValue("B4", cellValues));

                eligibilityWorkOrder.setCertificateMaps(findTablesValues(eligibilityConfig.getCertificateMappingTables(),
                        sheetCellValues));
                break;

            case claim_work_order:
                setClaimWorkOrder(eligibilityWorkOrder, cellValues);

                break;
            case per_script_deductible:
                eligibilityWorkOrder.setDeductibleMaps(findTablesValues(eligibilityConfig.getDeductibleMappingTables(),
                        sheetCellValues));
                break;

            default:
                LOG.info("mapSheetDatasToEligibilityWorkOrder() No mapping defined for provided sheetIndex '{}'",
                        sheetName.getSheetName());

        }
    }

    /**
     * setClaimWorkOrder
     * 
     * @param eligibilityWorkOrder
     * @param cellValues
     */
    private static void setClaimWorkOrder(EligibilityWorkOrder eligibilityWorkOrder, List<Map<String, String>> cellValues) {
        ClaimsWorkOrder claimsWorkOrder = new ClaimsWorkOrder();
        claimsWorkOrder.setClientName(findCellValue("B3", cellValues));
        claimsWorkOrder.setConversionDate(findCellValue("B6", cellValues));
        claimsWorkOrder.setClaimBackdatedEffectiveDate(findCellValue("B7", cellValues));
        claimsWorkOrder.setClaimReimbursement(findCellValue("B8", cellValues));
        claimsWorkOrder.setClaimPayDirect(findCellValue("B9", cellValues));
        //
        WorkOrderRequestDate initialRequestDate = new WorkOrderRequestDate(findCellValue("B14", cellValues),
                WorkOrderRequestDate.CURRENT);
        claimsWorkOrder.setInitialRequestRunDate(initialRequestDate);
        claimsWorkOrder.setReRunInitialRequest(findCellValue("B15", cellValues));

        //
        WorkOrderRequestDate deltaRequestDate = new WorkOrderRequestDate(findCellValue("B18", cellValues),
                WorkOrderRequestDate.DELTA);
        List<WorkOrderRequestDate> deltaRequestRunDates = Lists.newLinkedList();
        deltaRequestRunDates.add(deltaRequestDate);
        claimsWorkOrder.setDeltaRequestRunDates(deltaRequestRunDates);
        //
        claimsWorkOrder.setReRunDeltaRequest(findCellValue("B19", cellValues));
        eligibilityWorkOrder.setClaimsWorkOrder(claimsWorkOrder);

        DrugClaimsWorkOrder drugClaimsWorkOrder = new DrugClaimsWorkOrder();
        BeanUtils.copyProperties(claimsWorkOrder, drugClaimsWorkOrder);
        eligibilityWorkOrder.setDrugClaimsWorkOrder(drugClaimsWorkOrder);
    }

    /**
     * setEligibilityWorkOrder
     * 
     * @param sheetCellValues
     * @param eligibilityWorkOrder
     * @param eligibilityConfig
     * @param cellValues
     */
    private static void setEligibilityWorkOrder(Map<String, List<Map<String, String>>> sheetCellValues,
            EligibilityWorkOrder eligibilityWorkOrder, EligibilityConfig eligibilityConfig, List<Map<String, String>> cellValues) {
        eligibilityWorkOrder.setClientName(findCellValue("B3", cellValues));
        eligibilityWorkOrder.setConversionDate(findCellValue("B6", cellValues));
        eligibilityWorkOrder.setTerminationDate(findCellValue("B8", cellValues));
        eligibilityWorkOrder.setVLOADFormat(findCellValue("B9", cellValues));
        eligibilityWorkOrder.setGipsyFormat(findCellValue("B10", cellValues));
        eligibilityWorkOrder.setSAGFormat(findCellValue("B11", cellValues));
        eligibilityWorkOrder.setReRunInitialRequest(findCellValue("B12", cellValues));
        //
        WorkOrderRequestDate initialRequestDate = new WorkOrderRequestDate(findCellValue("B13", cellValues),
                WorkOrderRequestDate.CURRENT);
        eligibilityWorkOrder.setInitialRequestRunDate(initialRequestDate);
        //
        eligibilityWorkOrder.setDataIntegrityReport(findCellValue("B14", cellValues));
        eligibilityWorkOrder.setActiveClassAndDivExcpRprtRunDate(findCellValue("B15", cellValues));
        eligibilityWorkOrder.setPharmacareRptRunDate(findCellValue("B16", cellValues));
        eligibilityWorkOrder.setWaiverOfPremiumRunDate(findCellValue("B17", cellValues));
        eligibilityWorkOrder.setGipsyEmailReportRunDate(findCellValue("B18", cellValues));
        eligibilityWorkOrder.setPharmaCareEnrollDate(findCellValue("B19", cellValues));

        // set delta date(s)
        List<WorkOrderRequestDate> deltaRequestRunDates = Lists.newLinkedList();

        WorkOrderRequestDate deltaFirstRequestDate = new WorkOrderRequestDate(WorkOrderRequestDate.DELTA_DATE_1,
                WorkOrderRequestDate.DELTA);

        deltaRequestRunDates.add(deltaFirstRequestDate);

        String delta2Date = findCellValue("B23", cellValues);
        if (StringUtils.isNotEmpty(delta2Date)) {
            WorkOrderRequestDate deltaSecondRequestDate = new WorkOrderRequestDate(WorkOrderRequestDate.DELTA_DATE_2,
                    WorkOrderRequestDate.DELTA);
            deltaRequestRunDates.add(deltaSecondRequestDate);
        }
        eligibilityWorkOrder.setDeltaRequestRunDates(deltaRequestRunDates);

        //
        eligibilityWorkOrder.setReRunDeltaRequest(findCellValue("B24", cellValues));
        eligibilityWorkOrder.setFutureTransactionReportRunDate(findCellValue("B25", cellValues));
        eligibilityWorkOrder.setCertifcateMappingReportRunDate(findCellValue("B26", cellValues));
        //
        eligibilityWorkOrder.setPlanMaps(findTablesValues(eligibilityConfig.getPlanMappingTables(), sheetCellValues));
    }

    public static String findCellValue(final String cellRef, List<Map<String, String>> cellValues) {

        String cellValue = StringUtils.EMPTY;

        Optional<Map<String, String>> optMap = Iterables.tryFind(cellValues, new Predicate<Map<String, String>>() {
            public boolean apply(Map<String, String> input) {
                return input.keySet().contains(cellRef);
            };
        });

        if (optMap.isPresent()) {
            cellValue = optMap.get().get(cellRef);
        }

        return cellValue;
    }

    public static Map<String, List<List<String>>> findTablesValues(List<String> tableNames,
            Map<String, List<Map<String, String>>> cellValues) {

        Map<String, List<List<String>>> tablesValues = Maps.newLinkedHashMap();

        for (String tableName : tableNames) {
            if (cellValues.keySet().contains(tableName)) {
                List<List<String>> tableValues = Lists.newLinkedList();
                for (Map<String, String> map : cellValues.get(tableName)) {
                    tableValues.add(Lists.newArrayList(map.values()));
                }
                tablesValues.put(tableName, tableValues);
            }
        }
        return tablesValues;
    }

    public static CellRangeAddress getTableCellRange(XSSFSheet sheet, String sheetIndex, String tableCellReference,
            Map<String, String> sheetTableNames) {

        CellRangeAddress refCelRange = null;

        String cellRef = StringUtils.substringAfter(tableCellReference, AbstractProperties.WORK_ORDER_TABLE_DELIMITER);
        CellReference firstCellRef = ExcelUtils.getFirstCellReferenceFromRange(cellRef);
        CellReference lastCellRef = ExcelUtils.getLastCellReferenceFromRange(cellRef);

        if (firstCellRef.getRow() == -1 && lastCellRef.getRow() == -1) {
            String tableName = StringUtils.substringBefore(tableCellReference, AbstractProperties.WORK_ORDER_TABLE_DELIMITER);

            String sheetTableDesc = sheetTableNames.get(sheetIndex + AbstractProperties.DOT + tableName);

            String firstHeader = StringUtils.substringBefore(sheetTableDesc, AbstractProperties.WORK_ORDER_TABLE_DELIMITER);
            String secondHeader = StringUtils.substringAfter(sheetTableDesc, AbstractProperties.WORK_ORDER_TABLE_DELIMITER);

            // Iterate through each rows from first sheet
            Iterator<Row> rowIterator = sheet.iterator();
            while (rowIterator.hasNext()) {
                Row row = rowIterator.next();
                if (containsRowCellText(row, firstCellRef.getCol(), firstHeader)) {
                    row = rowIterator.next();
                    if (containsRowCellText(row, firstCellRef.getCol(), secondHeader)) {
                        // 2 rows contain the Table descriptions, get current row + 2 to skip the table header
                        refCelRange = new CellRangeAddress(row.getRowNum() + 2, row.getRowNum() + 2, firstCellRef.getCol(),
                                lastCellRef.getCol());
                        break;
                    }
                }
            }
        } else {
            refCelRange = new CellRangeAddress(firstCellRef.getRow(), lastCellRef.getRow(), firstCellRef.getCol(),
                    lastCellRef.getCol());
        }

        return refCelRange;
    }

    public static boolean containsRowCellText(Row row, int cellNumber, String searchedText) {
        Cell cell = row.getCell(cellNumber);
        String cellText = ExcelUtils.getCellContentAsString(cell);

        return StringUtils.isNotEmpty(cellText) && StringUtils.isNotEmpty(searchedText) && searchedText.equals(cellText);
    }

    public static List<Map<String, String>> getTableCellContent(XSSFSheet sheet, CellRangeAddress refCellRange) {

        List<Map<String, String>> tableValues = Lists.newLinkedList();
        Map<String, String> rowValues = Maps.newLinkedHashMap();
        CellRangeAddress cellRange = refCellRange;

        boolean flag = true;
        while (flag) {
            rowValues = ExcelUtils.getCellRangeContent(sheet, cellRange);
            if (isCellRangeContentNotBlank(rowValues)) {
                tableValues.add(rowValues);
                cellRange = new CellRangeAddress(cellRange.getFirstRow() + 1, cellRange.getLastRow() + 1,
                        cellRange.getFirstColumn(), cellRange.getLastColumn());
            } else {
                flag = false;
            }
        }
        return tableValues;
    }

    /**
     * Checks if a row Value is not empty (""), not null and not whitespace only.
     * 
     * @param Map<String, String> rowValues
     * 
     * @return boolean
     */

    public static boolean isCellRangeContentNotBlank(Map<String, String> rowValues) {
        boolean isNotBlank = false;
        for (String s : rowValues.keySet()) {
            isNotBlank = StringUtils.isNotBlank(rowValues.get(s));
            if (isNotBlank) {
                break;
            }
        }
        return isNotBlank;
    }

    public static String getGroupNumberFromFileName(String fileName) {
        String groupNumber = StringUtils.EMPTY;
        Pattern p = Pattern.compile(AbstractProperties.GROUP_FORMAT_PATTERN);
        Matcher m = p.matcher(fileName);
        if (m.find()) {
            groupNumber = m.group();
        }
        return groupNumber;
    }

    public static String getErrorInfoDescriptions(List<ErrorInfo> errorInfos) {
        StringBuilder errorInfoDescription = new StringBuilder();
        for (ErrorInfo err : errorInfos) {
            errorInfoDescription.append(err.getDescription()).append("; \n");
        }
        return errorInfoDescription.toString();
    }

}
