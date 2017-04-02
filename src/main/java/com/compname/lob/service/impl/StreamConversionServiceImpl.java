package com.compname.lob.service.impl;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.commons.collections.CollectionUtils;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.util.IOUtils;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.io.PatternFilenameFilter;
import com.compname.lob.domain.config.AbstractProperties;
import com.compname.lob.domain.report.DbParameter;
import com.compname.lob.domain.report.ReportData;
import com.compname.lob.service.StreamConversionService;
import com.compname.lob.service.impl.dao.datasource.StorProcRefCursorDao;
import com.compname.lob.utils.DatasourceUtils;
import com.compname.lob.utils.ExcelUtils;

/**
 * Stream Conversion Service Implementation
 * 
 * @author vegirl1
 * @since Jun 12, 2015
 * @version $Revision$
 */
public abstract class StreamConversionServiceImpl<E extends AbstractProperties> implements StreamConversionService<E> {

    protected static final Logger      LOG = LoggerFactory.getLogger(StreamConversionServiceImpl.class);

    private final StorProcRefCursorDao storProcRefCursorDao;

    /**
     * Class constructor.
     * 
     */
    public StreamConversionServiceImpl(StorProcRefCursorDao storProcRefCursorDao) {
        this.storProcRefCursorDao = storProcRefCursorDao;
    }

    /**
     * {@inheritDoc}
     * 
     * @see com.compname.lob.service.StreamConversionService#getWorkOrderFiles(java.lang.String)
     */
    @Override
    public List<File> getWorkOrderFiles(E config) {
        Pattern pattern = Pattern.compile(config.getWorkOrderFilenamePattern());
        File directory = new File(config.getInputPath());
        FilenameFilter fileNameFilter = new PatternFilenameFilter(pattern);
        List<File> files = Lists.newArrayList();

        File[] fileArrays = directory.listFiles(fileNameFilter);
        if (fileArrays == null) {
            LOG.info("Provided input path '{}' is not a valid directory", config.getInputPath());
        } else {
            files.addAll(Arrays.asList(fileArrays));
        }
        LOG.info("WorkOrder file(s) to be processed : {}", files);
        return files;
    }

    /**
     * {@inheritDoc}
     * 
     * @throws ServiceException
     * 
     * @see com.compname.lob.service.StreamConversionService#getReportDatas(com.compname.lob.domain.config.AbstractProperties)
     */
    @Override
    public ReportData getInitialReportData(E config) throws ServiceException {
        ReportData reportData = new ReportData();
        List<Map<String, Object>> dsValues = getRefCursorValues(config.getMaincursorDataSource(), config.getParameters());
        reportData.setWorkOrderKeyValues(dsValues);
        return reportData;
    }

    protected void retrieveReportData(E config, String fileType, ReportData data) throws ServiceException {
        Map<String, List<String>> datasourceColumnNames = Maps.newHashMap();
        Map<String, List<Map<String, Object>>> datasourceValues = Maps.newHashMap();
        Map<String, List<String>> columnHeaders = Maps.newHashMap();

        for (String sheetName : config.getFileSheetNames().get(fileType)) {

            String datasourceName = config.getDatasourceNames().get(fileType + AbstractProperties.DOT + sheetName);
            List<Map<String, Object>> dsValues = getRefCursorValues(datasourceName, config.getParameters());

            // after call of refCursor set it's column names
            Map<String, Object> tableRow = dsValues.get(0);
            List<String> refCursorColumnNames = Lists.newLinkedList(tableRow.keySet());
            List<String> refCursorHeaders = Lists.newLinkedList();
            for (String key : tableRow.keySet()) {
                refCursorHeaders.add(tableRow.get(key).toString());
            }

            datasourceColumnNames.put(datasourceName, refCursorColumnNames);
            columnHeaders.put(datasourceName, refCursorHeaders);

            // first line contains row headers
            dsValues.remove(0);
            datasourceValues.put(datasourceName, dsValues);

        }
        data.setDatasourceColumnNames(datasourceColumnNames);
        data.setColumnHeaders(columnHeaders);
        data.setDatasourceValues(datasourceValues);
    }

    private List<Map<String, Object>> getRefCursorValues(String dataSourceName, List<DbParameter> dbParameters)
            throws ServiceException {
        String storProcName = DatasourceUtils.getStorProcName(dataSourceName);
        List<DbParameter> storProcParamValues = DatasourceUtils.getStorProcParameters(dataSourceName, dbParameters);
        List<Map<String, Object>> dsValues = getStorProcRefCursorDao().getRefCursorValues(storProcName, storProcParamValues);
        return dsValues;
    }

    protected void createExcelReport(E config, ReportData data) throws ServiceException {
        LOG.debug("Create Result Report(s) for {} stream ", config.getClass().getName());

        String path = config.buildPathFileName(data.getFileName());
        LOG.info("Creating Result Report '{}'", path);

        OutputStream outputStream = null;
        XSSFWorkbook workbook = new XSSFWorkbook();

        XSSFCellStyle defaultCellStyle = ExcelUtils.getDefaultCellStyle(workbook);
        XSSFCellStyle tableHeaderCellStyle = ExcelUtils.getTableHeaderCellStyle(workbook);
        XSSFCellStyle tableCellStyle = ExcelUtils.getTableCellStyle(workbook);
        CreationHelper creationHelper = workbook.getCreationHelper();

        try {
            for (String sheetName : config.getFileSheetNames().get(data.getFileType())) {

                String datasourceName = config.getDatasourceNames().get(data.getFileType() + AbstractProperties.DOT + sheetName);
                List<String> datasourceColumns = data.getDatasourceColumnNames().get(datasourceName);
                List<String> columnHeaders = data.getColumnHeaders().get(datasourceName);
                List<Map<String, Object>> datasourceValues = data.getDatasourceValues().get(datasourceName);

                XSSFSheet sheet = workbook.createSheet(sheetName);
                // Create sheat Header
                CellReference headerCellRef = new CellReference(ExcelUtils.SHEET_HEADER_FIRSTCELL);
                ExcelUtils.buildSheetHeader(sheet, defaultCellStyle, creationHelper, headerCellRef, config.getFileSheetHeaders()
                        .get(data.getFileType() + AbstractProperties.DOT + sheetName));

                // Create Table Header
                CellReference tableHeaderCellRef = new CellReference(ExcelUtils.SHEET_TABLE_FIRSTCELL);
                ExcelUtils.buildTableRowHeader(sheet, tableHeaderCellStyle, creationHelper, tableHeaderCellRef, columnHeaders);

                // Create table rows
                CellReference tableCellRef = new CellReference(tableHeaderCellRef.getRow() + 1, tableHeaderCellRef.getCol());
                if (CollectionUtils.isEmpty(datasourceValues)) {
                    ExcelUtils.buildTableRowNoDataFound(sheet, defaultCellStyle, creationHelper, tableCellRef,
                            datasourceColumns.size() - 1);
                } else {
                    ExcelUtils.buildTableRows(sheet, tableCellStyle, creationHelper, tableCellRef, datasourceColumns,
                            datasourceValues);
                }

                // Auto size columns
                ExcelUtils.autoSizeColumns(sheet, tableHeaderCellRef, datasourceColumns.size());

            }

            // write to file
            outputStream = new FileOutputStream(path);
            workbook.write(outputStream);

        } catch (IOException ex) {
            String errorInfoMessage = "Failed to generate output report: " + data.getFileName() + "Error message : "
                    + ex.getLocalizedMessage();
            LOG.error(errorInfoMessage);
            throw new ServiceException(errorInfoMessage, Lists.newArrayList(ErrorInfo.createWith("createExcelReport()",
                    AbstractProperties.FAMILY_GROUP_BENEFIT, ErrorInfo.ERROR, errorInfoMessage)), ExceptionType.BUSINESS_EXCEPTION);
        } finally {
            IOUtils.closeQuietly(outputStream);
            IOUtils.closeQuietly(workbook);
        }
    }

    /**
     * Getter method of the <code>"storProcRefCursorDao"</code> class attribute.
     * 
     * @return the storProcRefCursorDao.
     */
    public StorProcRefCursorDao getStorProcRefCursorDao() {
        return this.storProcRefCursorDao;
    }

}
