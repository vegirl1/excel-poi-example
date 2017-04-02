package com.compname.lob.service.impl.config;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;


import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.compname.lob.domain.config.AbstractProperties;
import com.compname.lob.domain.config.ClaimsConfig;
import com.compname.lob.domain.config.EligibilityConfig;
import com.compname.lob.domain.config.MasterAppConfig;
import com.compname.lob.domain.config.WorkOrderSheetNames;
import com.compname.lob.domain.report.DbParameter;
import com.compname.lob.service.PropertiesConfigService;
import com.compname.lob.utils.ExcelUtils;
import com.compname.lob.utils.ValidationUtils;

/**
 * Work Order Configuration Service Implementation
 * 
 * @author vegirl1
 * @since May 28, 2015
 * @version $Revision$
 */
public class PropertiesConfigServiceImpl implements PropertiesConfigService {

    private static final Logger LOG = LoggerFactory.getLogger(PropertiesConfigServiceImpl.class);

    @Autowired
    private Environment         env;

    /**
     * Class constructor.
     * 
     * @throws ServiceException
     * 
     */
    public PropertiesConfigServiceImpl() throws ServiceException {
    }

    /**
     * {@inheritDoc}
     * 
     * @see com.compname.lob.service.PropertiesConfigService#getClaimsAppConfiguration()
     */
    @Override
    public ClaimsConfig getClaimsConfiguration() throws ServiceException {
        LOG.debug("Building ClaimsConfig()");

        ClaimsConfig claimsConfig = new ClaimsConfig();
        setConfiguration(claimsConfig);
        return claimsConfig;
    }

    /**
     * {@inheritDoc}
     * 
     * @see com.compname.lob.service.PropertiesConfigService#getMasterAppConfiguration()
     */
    @Override
    public MasterAppConfig getMasterAppConfiguration() throws ServiceException {

        LOG.debug("Building MasterAppConfig()");

        MasterAppConfig masterAppConfig = new MasterAppConfig();
        setConfiguration(masterAppConfig);
        return masterAppConfig;
    }

    /**
     * {@inheritDoc}
     * 
     * @see com.compname.lob.service.PropertiesConfigService#getEligibilityConfiguration()
     */
    @Override
    public EligibilityConfig getEligibilityConfiguration() throws ServiceException {
        return buildEligibilityConfiguration();
    }

    /**
     * Build WorkOrderConfig object
     * 
     * @return WorkOrderConfig
     * @throws ServiceException
     */
    private EligibilityConfig buildEligibilityConfiguration() throws ServiceException {

        LOG.debug("Building EligibilityConfig()");

        EligibilityConfig config = new EligibilityConfig();

        config.setInputPath(env.getProperty(config.getStreamName() + ".in.path"));
        config.setInputErrorPath(env.getProperty(config.getStreamName() + ".in_error.path"));
        config.setInputArchivePath(env.getProperty(config.getStreamName() + ".in_archive.path"));

        config.setWorkOrderFilenamePattern(env.getProperty(config.getStreamName() + ".workorder.filename.pattern"));

        config.setEmailEnabled(new Boolean(env.getProperty(config.getStreamName() + ".email.enabled")));
        config.setEmailFrom(env.getProperty(config.getStreamName() + ".email.from"));
        config.setEmailSubject(env.getProperty(config.getStreamName() + ".email.subject"));
        config.setEmailTo(env.getProperty(config.getStreamName() + ".email.to"));
        config.setEmailCc(env.getProperty(config.getStreamName() + ".email.cc"));

        List<String> sheetToProcess = getSheetToProcess(config);
        Map<String, List<String>> sheetCells = getSheetCells(config, sheetToProcess);

        config.setSheetToProcess(sheetToProcess);
        config.setSheetCells(sheetCells);
        config.setSheetTableDescriptions(getSheetTables(sheetCells));

        config.setPlanMappingTables(Arrays.asList(env.getProperty(config.getStreamName() + ".plan.mapp.tables").split(
                AbstractProperties.COMMA)));
        config.setCertificateMappingTables(Arrays.asList(env.getProperty(config.getStreamName() + ".certificate.mapp.tables")
                .split(AbstractProperties.COMMA)));
        config.setBenefitMappingTables(Arrays.asList(env.getProperty(config.getStreamName() + ".benefit.mapp.tables").split(
                AbstractProperties.COMMA)));
        config.setDeductibleMappingTables(Arrays.asList(env.getProperty(config.getStreamName() + ".deductible.mapp.tables").split(
                AbstractProperties.COMMA)));

        setConfiguration(config);

        return config;
    }

    /**
     * Get Sheet To Process from property file
     * 
     * @return List<Integer>
     * @throws ServiceException
     */
    private <T extends AbstractProperties> List<String> getSheetToProcess(T config) throws ServiceException {

        String sheetToProcess = env.getProperty(config.getStreamName() + ".listOfSheetToProcess");

        if (StringUtils.isEmpty(sheetToProcess)) {
            String errorInfoMessage = "SheetToProcess property not found";
            LOG.error(errorInfoMessage);
            throw new ServiceException(errorInfoMessage, Lists.newArrayList(ErrorInfo.createWith(
                    "WorkOrderConfigServiceImpl.getSheetToProcess()", AbstractProperties.FAMILY_GROUP_BENEFIT, ErrorInfo.FATAL,
                    errorInfoMessage)), ExceptionType.BUSINESS_EXCEPTION);
        }

        List<String> sheetList = Arrays.asList(sheetToProcess.split(AbstractProperties.COMMA));

        LOG.debug("Getting sheetsToProcess property via Spring Environment '{}' ", sheetList);
        return sheetList;
    }

    /**
     * Get Sheet Cells to process
     * 
     * @param List<Integer> sheetToProcess
     * @return Map<String, List<String>>
     * @throws ServiceException
     */
    private <T extends AbstractProperties> Map<String, List<String>> getSheetCells(T config, List<String> sheetToProcess)
            throws ServiceException {
        Map<String, List<String>> sheetCells = Maps.newLinkedHashMap();

        for (String sheetName : sheetToProcess) {

            sheetName = WorkOrderSheetNames.getBySheetName(sheetName).name();

            String sheetId = config.getStreamName() + AbstractProperties.DOT + ExcelUtils.SHEET + AbstractProperties.DOT
                    + sheetName;
            String cells = env.getProperty(sheetId);

            if (StringUtils.isEmpty(cells)) {
                String errorInfoMessage = "Sheet Cells To Process property ('" + sheetId + "') not found";
                LOG.error(errorInfoMessage);
                throw new ServiceException(errorInfoMessage, Lists.newArrayList(ErrorInfo.createWith(
                        "WorkOrderConfigServiceImpl.getSheetCells()", AbstractProperties.FAMILY_GROUP_BENEFIT, ErrorInfo.FATAL,
                        errorInfoMessage)), ExceptionType.BUSINESS_EXCEPTION);
            }
            sheetCells.put(sheetId, Arrays.asList(cells.split(EligibilityConfig.COMMA)));
        }
        LOG.debug("Got sheetCells properties via Spring Environment '{}' ", sheetCells);
        return sheetCells;
    }

    /**
     * Get Sheet Tables to process
     * 
     * @param List<Integer> sheetToProcess
     * @return Map<String, List<String>>
     * @throws ServiceException
     */
    private Map<String, String> getSheetTables(Map<String, List<String>> sheetCells) {

        Map<String, String> sheetTableNames = Maps.newHashMap();

        for (String sheetName : sheetCells.keySet()) {
            for (String cellValue : sheetCells.get(sheetName)) {
                if (StringUtils.containsIgnoreCase(cellValue, AbstractProperties.WORK_ORDER_TABLE + AbstractProperties.DOT)) {
                    String tableName = StringUtils.substringBefore(cellValue, AbstractProperties.WORK_ORDER_TABLE_DELIMITER);
                    String key = sheetName + EligibilityConfig.DOT + tableName;
                    sheetTableNames.put(key, env.getProperty(key));
                }
            }
        }
        LOG.debug("Got sheetTableNames properties via Spring Environment '{}' ", sheetTableNames);
        return sheetTableNames;
    }

    private <T extends AbstractProperties> void setConfiguration(T config) throws ServiceException {

        // set workOrderKeysDataSource
        config.setMaincursorDataSource(StringUtils.trim(env.getProperty(config.getStreamName() + ".maincursor")));
        // set out path
        config.setOutputPath(StringUtils.trim(env.getProperty(config.getStreamName() + ".out.path")));
        // set list of files to create
        setFilesToCreate(config);
        // set file names to be created
        setFileNames(config);
        // set each file sheet names
        setFileSheetNames(config);
        // set each file sheet headers
        setFileSheetHeaders(config);
        // set each sheet data source
        setFileSheetDataSources(config);
        // set data source parameters
        setDbParameters(config);

        ValidationUtils.validateStreamProperties(config);

    }

    /**
     * setFilesToCreate
     * 
     * @param claimsConfig
     */
    private <T extends AbstractProperties> void setFilesToCreate(T config) {

        String keyValue = StringUtils.trim(env.getProperty(config.getStreamName() + ".file.typenames"));

        if (StringUtils.isNotBlank(keyValue)) {
            List<String> filesToCreate = Arrays.asList(keyValue.split(AbstractProperties.COMMA));
            config.setFilesToCreate(filesToCreate);
        }
    }

    /**
     * setFileNames
     * 
     * @param claimsConfig
     */
    private <T extends AbstractProperties> void setFileNames(T config) {
        Map<String, String> fileNames = Maps.newHashMap();
        for (String fileType : config.getFilesToCreate()) {
            List<String> extractTypes = Lists.newArrayList(fileType + "." + "initial", fileType + "." + "delta", fileType + "."
                    + "future");
            for (String type : extractTypes) {
                String keyValue = StringUtils.trim(env.getProperty(config.getStreamName() + ".file.name." + type));
                if (StringUtils.isNotBlank(keyValue)) {
                    fileNames.put(type, keyValue);
                }
            }
        }
        config.setFileNames(fileNames);
    }

    /**
     * setFileSheetNames
     * 
     * @param claimsConfig
     */
    private <T extends AbstractProperties> void setFileSheetNames(T config) {
        Map<String, List<String>> fileSheetNames = Maps.newHashMap();
        for (String fileType : config.getFilesToCreate()) {
            String keyValue = StringUtils.trim(env.getProperty(config.getStreamName() + ".sheet.names." + fileType));
            List<String> sheetNames = Arrays.asList(keyValue.split(AbstractProperties.COMMA));
            fileSheetNames.put(fileType, sheetNames);
        }
        config.setFileSheetNames(fileSheetNames);
    }

    private <T extends AbstractProperties> void setFileSheetHeaders(T config) {
        // Map<predetermination.sheetA_name,Claims Pred Report>
        Map<String, String> fileSheetHeaders = getFileSheetInfo(config, ".sheet.header.");
        config.setFileSheetHeaders(fileSheetHeaders);
    }

    private <T extends AbstractProperties> void setFileSheetDataSources(T config) {
        // Map<predetermination.sheetA_name, StorProcName>
        Map<String, String> datasourceNames = getFileSheetInfo(config, ".sheet.datasource.");
        config.setDatasourceNames(datasourceNames);
    }

    private <T extends AbstractProperties> Map<String, String> getFileSheetInfo(T config, String prepertykey) {
        Map<String, String> fileSheetInfo = Maps.newHashMap();
        for (String file : config.getFileSheetNames().keySet()) {
            for (String sheet : config.getFileSheetNames().get(file)) {
                String sheetName = file + "." + sheet;
                String keyValue = StringUtils.trim(env.getProperty(config.getStreamName() + prepertykey + sheetName));
                fileSheetInfo.put(sheetName, keyValue);
            }
        }
        return fileSheetInfo;
    }

    /**
     * setFileSheetNames
     * 
     * @param claimsConfig
     */
    private <T extends AbstractProperties> void setDbParameters(T config) {

        List<DbParameter> parameters = Lists.newArrayList();

        String keyValue = StringUtils.trim(env.getProperty(config.getStreamName() + ".parameters"));
        List<String> params = Arrays.asList(keyValue.split(AbstractProperties.COMMA));
        for (String s : params) {
            DbParameter parameter = new DbParameter();
            parameter.setName(s);

            keyValue = StringUtils.trim(env.getProperty(config.getStreamName() + ".parameter.type." + s));
            parameter.setDataType(keyValue);

            keyValue = StringUtils.trim(env.getProperty(config.getStreamName() + ".parameter.value." + s));
            parameter.setValueSource(keyValue);

            if (StringUtils.contains(keyValue, AbstractProperties.SINGLE_QUOTE)) {
                keyValue = StringUtils.remove(keyValue, AbstractProperties.SINGLE_QUOTE);
                parameter.setValue(keyValue);
            }

            parameters.add(parameter);
        }

        config.setParameters(parameters);
    }

    /**
     * Getter method of the <code>"env"</code> class attribute.
     * 
     * @return the env.
     */
    public Environment getEnv() {
        return this.env;
    }

    /**
     * Setter method of the <code>"env"</code> class attribute.
     * 
     * @param env the env to set.
     */
    public void setEnv(Environment aEnv) {
        this.env = aEnv;
    }
}
