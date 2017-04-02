package com.compname.lob.domain.report;

import java.util.List;
import java.util.Map;


/**
 * ReportDatas
 * 
 * @author vegirl1
 * @since Jul 28, 2015
 * @version $Revision$
 */
public class ReportData extends DomainModelObject {

    private static final long                      serialVersionUID = -2190845821576688703L;

    private List<Map<String, Object>>              workOrderKeyValues;

    private String                                 fileName;
    private String                                 fileType;

    // Map<file.sheet, List<ColName>>
    private Map<String, List<String>>              datasourceColumnNames;

    // Map<file.sheet, List<ColHeader>>
    private Map<String, List<String>>              columnHeaders;

    // Map<file.sheet, List<Map<Col1,Value1>>>
    private Map<String, List<Map<String, Object>>> datasourceValues;

    /**
     * Class constructor.
     * 
     */
    public ReportData() {
    }

    /**
     * Getter method of the <code>"workOrderKeyValues"</code> class attribute.
     * 
     * @return the workOrderKeyValues.
     */
    public List<Map<String, Object>> getWorkOrderKeyValues() {
        return this.workOrderKeyValues;
    }

    /**
     * Setter method of the <code>"workOrderKeyValues"</code> class attribute.
     * 
     * @param WorkOrderKeyValues the workOrderKeyValues to set.
     */
    public void setWorkOrderKeyValues(List<Map<String, Object>> workOrderKeyValues) {
        this.workOrderKeyValues = workOrderKeyValues;
    }

    /**
     * Getter method of the <code>"datasourceColumnNames"</code> class attribute.
     * 
     * @return the datasourceColumnNames.
     */
    public Map<String, List<String>> getDatasourceColumnNames() {
        return this.datasourceColumnNames;
    }

    /**
     * Setter method of the <code>"datasourceColumnNames"</code> class attribute.
     * 
     * @param datasourceColumnNames the datasourceColumnNames to set.
     */
    public void setDatasourceColumnNames(Map<String, List<String>> datasourceColumnNames) {
        this.datasourceColumnNames = datasourceColumnNames;
    }

    /**
     * Getter method of the <code>"datasourceValues"</code> class attribute.
     * 
     * @return the datasourceValues.
     */
    public Map<String, List<Map<String, Object>>> getDatasourceValues() {
        return this.datasourceValues;
    }

    /**
     * Setter method of the <code>"datasourceValues"</code> class attribute.
     * 
     * @param datasourceValues the datasourceValues to set.
     */
    public void setDatasourceValues(Map<String, List<Map<String, Object>>> datasourceValues) {
        this.datasourceValues = datasourceValues;
    }

    /**
     * Getter method of the <code>"fileName"</code> class attribute.
     * 
     * @return the fileName.
     */
    public String getFileName() {
        return fileName;
    }

    /**
     * Setter method of the <code>"fileName"</code> class attribute.
     * 
     * @param FileName the fileName to set.
     */
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    /**
     * Getter method of the <code>"fileType"</code> class attribute.
     * 
     * @return the fileType.
     */
    public String getFileType() {
        return fileType;
    }

    /**
     * Setter method of the <code>"fileType"</code> class attribute.
     * 
     * @param FileType the fileType to set.
     */
    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    /**
     * Getter method of the <code>"columnHeaders"</code> class attribute.
     * 
     * @return the columnHeaders.
     */
    public Map<String, List<String>> getColumnHeaders() {
        return columnHeaders;
    }

    /**
     * Setter method of the <code>"columnHeaders"</code> class attribute.
     * 
     * @param ColumnHeaders the columnHeaders to set.
     */
    public void setColumnHeaders(Map<String, List<String>> columnHeaders) {
        this.columnHeaders = columnHeaders;
    }

}
