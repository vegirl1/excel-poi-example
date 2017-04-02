package com.compname.lob.domain.config;

import java.util.List;
import java.util.Map;

import com.google.common.collect.Maps;
import com.compname.lob.domain.report.DbParameter;
import com.compname.lob.utils.ExcelUtils;

/**
 * Abstract Config domain object
 * 
 * @author vegirl1
 * @since Jun 9, 2015
 * @version $Revision$
 */
public abstract class AbstractProperties extends DomainModelObject {

    private static final long         serialVersionUID           = -6279925548323662346L;

    public static final String        FAMILY_GROUP_BENEFIT       = "gb";
    public static final String        DOT                        = ".";
    public static final String        COMMA                      = ",";
    public static final String        SEMICOLON                  = ";";
    public static final String        SINGLE_QUOTE               = "'";
    public static final String        WORK_ORDER_TABLE           = "table";
    public static final String        WORK_ORDER_TABLE_DELIMITER = "|";
    public static final String        SLASH                      = "/";
    public static final String        UNDERSCORE                 = "_";
    public static final String        DASH                       = "-";

    public static final String        DATETIME_FORMAT            = "yyyyMMddHHmmss";
    public static final String        GROUP_FORMAT_PATTERN       = "(\\d{1,6})";

    public static final int           SLAC_GROUP_LENGTH          = 5;
    public static final int           CERTIFICATE_LENGTH         = 9;
    public static final int           SLAC_DIVISION_LENGTH       = 3;
    public static final int           SLAC_CLASS_LENGTH          = 3;
    public static final int           MANU_GROUP_LENGTH          = 6;
    public static final int           MANU_DIVISION_LENGTH       = 3;

    public static final String        ERROR_TYPE_ERROR           = "ERROR";
    public static final String        INITIAL                    = "INITIAL";
    public static final String        DELTA                      = "DELTA";
    public static final String        FUTURE                     = "FUTURE";

    private final String              streamName;

    /** WorkOrder Load parameters */
    // vegirl1; for Phase 1, as of July 27, 2015 have WorkOrder only for Eligibility
    private String                    workOrderFilenamePattern;
    private String                    inputPath;
    private String                    inputErrorPath;
    private String                    inputArchivePath;

    // input file (Excel WorkOrders) sheets to process&load
    private List<String>              sheetToProcess;
    private Map<String, List<String>> sheetCells;
    /** END WorkOrder Load parameters */

    /** OUT Excel Reports config */
    // vegirl1; for Phase 1, as of July 27, 2015 have to generate Excel Reports only for Claims
    private String                    outputPath;

    private String                    maincursorDataSource;

    // List of output type files (<predetermination,reversal,etc.>)
    private List<String>              filesToCreate;

    // Map<predetermination,G%s_Pre-Determinations_%s>
    private Map<String, String>       fileNames;

    // Map<predetermination,List<sheet1_name,sheet2_name>>
    private Map<String, List<String>> fileSheetNames;

    // Map<predetermination.sheetA_name,Claims Pred Report>
    private Map<String, String>       fileSheetHeaders;

    // Map<predetermination.sheetA_name, Table1/StorProc>
    private Map<String, String>       datasourceNames;

    private List<DbParameter>         parameters;
    /** END OUT Reports config */

    // java main app. options
    public static final String        MAINOPTION_EXTRACTIONTYPE  = "mainOption.extractionType";
    private Map<String, String>       mainOptions                = Maps.newHashMap();

    /**
     * Class constructor.
     * 
     */
    public AbstractProperties(String streamName) {
        this.streamName = streamName;
    }

    /**
     * Getter method of the <code>"workOrderFilenamePattern"</code> class attribute.
     * 
     * @return the workOrderFilenamePattern.
     */
    public String getWorkOrderFilenamePattern() {
        return this.workOrderFilenamePattern;
    }

    /**
     * Setter method of the <code>"workOrderFilenamePattern"</code> class attribute.
     * 
     * @param workOrderFilenamePattern the workOrderFilenamePattern to set.
     */
    public void setWorkOrderFilenamePattern(String workOrderFilenamePattern) {
        this.workOrderFilenamePattern = workOrderFilenamePattern;
    }

    /**
     * Getter method of the <code>"inputPath"</code> class attribute.
     * 
     * @return the inputPath.
     */
    public String getInputPath() {
        return this.inputPath;
    }

    /**
     * Setter method of the <code>"inputPath"</code> class attribute.
     * 
     * @param InputPath the inputPath to set.
     */
    public void setInputPath(String inputPath) {
        this.inputPath = inputPath;
    }

    /**
     * Getter method of the <code>"inputErrorPath"</code> class attribute.
     * 
     * @return the inputErrorPath.
     */
    public String getInputErrorPath() {
        return this.inputErrorPath;
    }

    /**
     * Setter method of the <code>"inputErrorPath"</code> class attribute.
     * 
     * @param InputErrorPath the inputErrorPath to set.
     */
    public void setInputErrorPath(String inputErrorPath) {
        this.inputErrorPath = inputErrorPath;
    }

    /**
     * Getter method of the <code>"inputArchivePath"</code> class attribute.
     * 
     * @return the inputArchivePath.
     */
    public String getInputArchivePath() {
        return this.inputArchivePath;
    }

    /**
     * Setter method of the <code>"inputArchivePath"</code> class attribute.
     * 
     * @param InputArchivePath the inputArchivePath to set.
     */
    public void setInputArchivePath(String inputArchivePath) {
        this.inputArchivePath = inputArchivePath;
    }

    /**
     * Getter method of the <code>"outputPath"</code> class attribute.
     * 
     * @return the outputPath.
     */
    public String getOutputPath() {
        return this.outputPath;
    }

    /**
     * Setter method of the <code>"outputPath"</code> class attribute.
     * 
     * @param OutputPath the outputPath to set.
     */
    public void setOutputPath(String outputPath) {
        this.outputPath = outputPath;
    }

    /**
     * Getter method of the <code>"sheetToProcess"</code> class attribute.
     * 
     * @return the sheetToProcess.
     */
    public List<String> getSheetToProcess() {
        return this.sheetToProcess;
    }

    /**
     * Setter method of the <code>"sheetToProcess"</code> class attribute.
     * 
     * @param SheetToProcess the sheetToProcess to set.
     */
    public void setSheetToProcess(List<String> sheetToProcess) {
        this.sheetToProcess = sheetToProcess;
    }

    /**
     * Getter method of the <code>"sheetCells"</code> class attribute.
     * 
     * @return the sheetCells.
     */
    public Map<String, List<String>> getSheetCells() {
        return this.sheetCells;
    }

    /**
     * Setter method of the <code>"sheetCells"</code> class attribute.
     * 
     * @param SheetCells the sheetCells to set.
     */
    public void setSheetCells(Map<String, List<String>> sheetCells) {
        this.sheetCells = sheetCells;
    }

    /**
     * Getter method of the <code>"fileSheetNames"</code> class attribute.
     * 
     * @return the fileSheetNames.
     */
    public Map<String, List<String>> getFileSheetNames() {
        return this.fileSheetNames;
    }

    /**
     * Setter method of the <code>"fileSheetNames"</code> class attribute.
     * 
     * @param FileSheetNames the fileSheetNames to set.
     */
    public void setFileSheetNames(Map<String, List<String>> fileSheetNames) {
        this.fileSheetNames = fileSheetNames;
    }

    /**
     * Getter method of the <code>"streamName"</code> class attribute.
     * 
     * @return the streamName.
     */
    public String getStreamName() {
        return streamName;
    }

    public String buildPathFileName(String fileName) {
        return this.outputPath + SLASH + fileName + DOT + ExcelUtils.EXCEL_FORMAT;
    }

    /**
     * Getter method of the <code>"datasourceNames"</code> class attribute.
     * 
     * @return the datasourceNames.
     */
    public Map<String, String> getDatasourceNames() {
        return this.datasourceNames;
    }

    /**
     * Setter method of the <code>"datasourceNames"</code> class attribute.
     * 
     * @param DatasourceNames the datasourceNames to set.
     */
    public void setDatasourceNames(Map<String, String> datasourceNames) {
        this.datasourceNames = datasourceNames;
    }

    /**
     * Getter method of the <code>"mainOptions"</code> class attribute.
     * 
     * @return the mainOptions.
     */
    public Map<String, String> getMainOptions() {
        return mainOptions;
    }

    /**
     * Setter method of the <code>"mainOptions"</code> class attribute.
     * 
     * @param MainOptions the mainOptions to set.
     */
    public void setMainOptions(Map<String, String> mainOptions) {
        this.mainOptions = mainOptions;
    }

    /**
     * Getter method of the <code>"filesToCreate"</code> class attribute.
     * 
     * @return the filesToCreate.
     */
    public List<String> getFilesToCreate() {
        return filesToCreate;
    }

    /**
     * Setter method of the <code>"filesToCreate"</code> class attribute.
     * 
     * @param FilesToCreate the filesToCreate to set.
     */
    public void setFilesToCreate(List<String> filesToCreate) {
        this.filesToCreate = filesToCreate;
    }

    /**
     * Getter method of the <code>"fileNames"</code> class attribute.
     * 
     * @return the fileNames.
     */
    public Map<String, String> getFileNames() {
        return fileNames;
    }

    /**
     * Setter method of the <code>"fileNames"</code> class attribute.
     * 
     * @param FileNames the fileNames to set.
     */
    public void setFileNames(Map<String, String> fileNames) {
        this.fileNames = fileNames;
    }

    /**
     * Getter method of the <code>"fileSheetHeaders"</code> class attribute.
     * 
     * @return the fileSheetHeaders.
     */
    public Map<String, String> getFileSheetHeaders() {
        return fileSheetHeaders;
    }

    /**
     * Setter method of the <code>"fileSheetHeaders"</code> class attribute.
     * 
     * @param FileSheetHeaders the fileSheetHeaders to set.
     */
    public void setFileSheetHeaders(Map<String, String> fileSheetHeaders) {
        this.fileSheetHeaders = fileSheetHeaders;
    }

    /**
     * Getter method of the <code>"parameters"</code> class attribute.
     * 
     * @return the parameters.
     */
    public List<DbParameter> getParameters() {
        return parameters;
    }

    /**
     * Setter method of the <code>"parameters"</code> class attribute.
     * 
     * @param Parameters the parameters to set.
     */
    public void setParameters(List<DbParameter> parameters) {
        this.parameters = parameters;
    }

    /**
     * Getter method of the <code>"maincursorDataSource"</code> class attribute.
     * 
     * @return the maincursorDataSource.
     */
    public String getMaincursorDataSource() {
        return this.maincursorDataSource;
    }

    /**
     * Setter method of the <code>"maincursorDataSource"</code> class attribute.
     * 
     * @param MaincursorDataSource the maincursorDataSource to set.
     */
    public void setMaincursorDataSource(String maincursorDataSource) {
        this.maincursorDataSource = maincursorDataSource;
    }
}
