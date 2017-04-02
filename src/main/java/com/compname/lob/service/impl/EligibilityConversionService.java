package com.compname.lob.service.impl;

import java.io.File;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.util.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;


import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.compname.lob.domain.config.AbstractProperties;
import com.compname.lob.domain.config.EligibilityConfig;
import com.compname.lob.domain.report.ReportData;
import com.compname.lob.domain.workorder.AbstractWorkOrder;
import com.compname.lob.domain.workorder.ClaimsWorkOrder;
import com.compname.lob.domain.workorder.DrugClaimsWorkOrder;
import com.compname.lob.domain.workorder.EligibilityWorkOrder;
import com.compname.lob.service.NotificationService;
import com.compname.lob.service.WorkOrderValidationService;
import com.compname.lob.service.impl.dao.datasource.StorProcRefCursorDao;
import com.compname.lob.service.impl.dao.eligibility.EligibilityDao;
import com.compname.lob.service.impl.dao.transaction.TransactionCommand;
import com.compname.lob.service.impl.dao.transaction.TransactionService;
import com.compname.lob.utils.DatasourceUtils;
import com.compname.lob.utils.WorkOrderUtils;

/**
 * EligibilityConversionService
 * 
 * @author vegirl1
 * @since Jun 12, 2015
 * @version $Revision$
 */
public class EligibilityConversionService extends StreamConversionServiceImpl<EligibilityConfig> {

    private static final Logger                      LOG = LoggerFactory.getLogger(EligibilityConversionService.class);

    @Autowired
    private TransactionService                       transactionService;

    @Autowired
    private EligibilityConfig                        eligibilityConfig;

    @Autowired
    private EligibilityDao                           eligibilityDao;

    @Autowired
    @Qualifier("eligibilityValidation")
    WorkOrderValidationService<EligibilityWorkOrder> eligibilityValidation;

    @Autowired
    @Qualifier("claimValidation")
    WorkOrderValidationService<ClaimsWorkOrder>      claimValidation;

    @Autowired
    @Qualifier("drugClaimValidation")
    WorkOrderValidationService<DrugClaimsWorkOrder>  drugClaimValidation;

    @Autowired
    private NotificationService                      notificationService;

    private Map<File, String>                        failedWorkOrders;
    private Map<File, String>                        warningWorkOrders;

    /**
     * Class constructor.
     * 
     */
    @Autowired
    public EligibilityConversionService(@Qualifier("compasStorProcRefCursorDao") StorProcRefCursorDao storProcRefCursorDao) {
        super(storProcRefCursorDao);
    }

    /**
     * {@inheritDoc}
     * 
     * @see com.compname.lob.service.StreamConversionService#processWorkOrder(java.util.List)
     */
    @Override
    public void processWorkOrder(List<File> workOrderFiles) {

        EligibilityWorkOrder eligibilityWorkOrder = null;
        failedWorkOrders = Maps.newLinkedHashMap();
        warningWorkOrders = Maps.newLinkedHashMap();

        for (File excelFile : workOrderFiles) {
            InputStream in = WorkOrderUtils.getWorkOrderInputStream(excelFile);
            if (in != null) {
                try {
                    LOG.info("Processing eligibility WorkOrder file '{}'", excelFile.getName());
                    // read excel file
                    eligibilityWorkOrder = WorkOrderUtils.getEligibilityWorkOrderContent(in, eligibilityConfig);

                    // get group number from file name
                    setSlacGroupNumberToWorkOrders(eligibilityWorkOrder, excelFile);

                    // validate WorkOrder data's (all found errors will be thrown as ServiceException)
                    validateWorkOrdersBeforePersistData(eligibilityWorkOrder, excelFile);

                    // persist to DB
                    if (eligibilityWorkOrder.getExistingWorkOrderId() == null) {
                        // new WorkOrder - insert data
                        saveWorkOrder(eligibilityWorkOrder);
                    } else {
                        // existing WorkOrder - update data
                        updateWorkOrder(eligibilityWorkOrder);
                    }

                    // post Insert/Update validation
                    validateWorkOrdersAfterPersistData(eligibilityWorkOrder, excelFile);

                } catch (ServiceException ex) {

                    String errorInfoMessage = "Failed to process '" + excelFile.getName() + "' WorkOrder Excel file.;\n "
                            + "ErrorInfo(s):" + WorkOrderUtils.getErrorInfoDescriptions(ex.getErrorInfos());
                    LOG.error(errorInfoMessage);

                    setWorkOrderError(eligibilityWorkOrder, errorInfoMessage);

                    failedWorkOrders.put(excelFile, errorInfoMessage);

                } finally {
                    IOUtils.closeQuietly(in);
                }
            } else {
                String errorInfoMessage = "Failed to read '" + excelFile.getName() + "' WorkOrder Excel file";
                LOG.error(errorInfoMessage);
                failedWorkOrders.put(excelFile, errorInfoMessage);
            }
        }

        cleanUp(workOrderFiles);

    }

    /**
     * validateWorkOrdersBeforePersistData
     * 
     * @param eligibilityWorkOrder
     * @throws ServiceException
     */
    private void validateWorkOrdersBeforePersistData(EligibilityWorkOrder eligibilityWorkOrder, File workOrderFile)
            throws ServiceException {
        LOG.info("Validating ELIG/CLAIM Work Orders for Group Number: '{}' ", eligibilityWorkOrder.getSlacGroupNumber());

        List<ErrorInfo> errorInfos = Lists.newArrayList();

        try {
            eligibilityValidation.validateWorkOrder(eligibilityWorkOrder, errorInfos);
            claimValidation.validateWorkOrder(eligibilityWorkOrder.getClaimsWorkOrder(), errorInfos);
            drugClaimValidation.validateWorkOrder(eligibilityWorkOrder.getDrugClaimsWorkOrder(), errorInfos);
        } catch (Exception e) {
            errorInfos.add(ErrorInfo.createWith("EligibilityConversionService.validateWorkOrdersBeforePersistData()",
                    AbstractProperties.FAMILY_GROUP_BENEFIT, ErrorInfo.ERROR,
                    "Unexpected exception thrown in the WO validation. Check application log files for error details."));

            LOG.error("Unexpected exception thrown in the WO validation. Error Message: " + e.getMessage());
        }
        // check if any warnings
        if (CollectionUtils.isNotEmpty(errorInfos)) {
            Iterable<ErrorInfo> warningInfos = Iterables.filter(errorInfos, new Predicate<ErrorInfo>() {
                public boolean apply(ErrorInfo input) {
                    return ErrorInfo.WARNING.equals(input.getTypeCode());
                }
            });

            if (Iterables.size(warningInfos) > 0) {
                List<ErrorInfo> warningList = Lists.newArrayList(warningInfos);
                warningWorkOrders.put(workOrderFile, WorkOrderUtils.getErrorInfoDescriptions(warningList));
                errorInfos.removeAll(warningList);
            }
        }

        if (CollectionUtils.isNotEmpty(errorInfos)) {
            throw new ServiceException("Eligibility WorkOrder data's are not valid", errorInfos);
        }
    }

    /**
     * validateWorkOrdersAfterPersistData
     * 
     * @param eligibilityWorkOrder
     * @throws ServiceException
     */
    private void validateWorkOrdersAfterPersistData(EligibilityWorkOrder eligibilityWorkOrder, File workOrderFile) {

        LOG.debug("validateWorkOrdersAfterPersistData for Group Number: '{}' ", eligibilityWorkOrder.getSlacGroupNumber());
        if (!AbstractWorkOrder.YES_FLAGS_LIST.contains(eligibilityWorkOrder.getSAGFormat())) {
            String result = eligibilityDao.getSlacAndWorkOrdPlanDiff(eligibilityWorkOrder.getSlacGroupNumber());
            if (StringUtils.isNotEmpty(result)) {
                if (warningWorkOrders.containsKey(workOrderFile)) {
                    warningWorkOrders.put(workOrderFile, warningWorkOrders.get(workOrderFile) + result);
                } else {
                    warningWorkOrders.put(workOrderFile, result);
                }
            }
        }
    }

    private void saveWorkOrder(final EligibilityWorkOrder eligibilityWorkOrder) throws ServiceException {

        LOG.info("Save Work Order for Group Number: '{}' ", eligibilityWorkOrder.getSlacGroupNumber());
        try {
            transactionService.executeTransaction(new TransactionCommand() {

                @Override
                public void execute() {
                    // set Eligibility WorkOrder
                    eligibilityWorkOrder.setWorkOrderId(eligibilityDao.getWorkOrderId());
                    eligibilityDao.setEligibilityWorkOrder(eligibilityWorkOrder);
                    eligibilityDao.setEligibilityRunDates(eligibilityWorkOrder);
                    eligibilityDao.addEligibilityWorkOrderPlanMapping(eligibilityWorkOrder);
                    eligibilityDao.addEligibilityWorkOrderBenefitMapping(eligibilityWorkOrder);
                    eligibilityDao.addEligibilityWorkOrderCertificateMapping(eligibilityWorkOrder);

                    // set Claims WorkOrder (CLAIM & DRUGC))
                    eligibilityWorkOrder.getClaimsWorkOrder().setWorkOrderId(eligibilityDao.getWorkOrderId());
                    eligibilityWorkOrder.getDrugClaimsWorkOrder().setWorkOrderId(eligibilityDao.getWorkOrderId());
                    eligibilityDao.setClaimsWorkOrder(eligibilityWorkOrder);
                    eligibilityDao.addPerScriptDeductible(eligibilityWorkOrder);
                }
            });
        } catch (Exception e) {
            String errorInfoMessage = "Failed to SaveWorkOrder for SLAC Group Number: " + eligibilityWorkOrder.getSlacGroupNumber()
                    + " \n\n [Error details : " + e.getMessage() + "]";
            LOG.error(errorInfoMessage);
            throw new ServiceException(errorInfoMessage, Lists.newArrayList(ErrorInfo.createWith(
                    "EligibilityConversionService.saveWorkOrder()", AbstractProperties.FAMILY_GROUP_BENEFIT, ErrorInfo.ERROR,
                    errorInfoMessage)), ExceptionType.BUSINESS_EXCEPTION);
        }
    }

    private void updateWorkOrder(final EligibilityWorkOrder eligibilityWorkOrder) throws ServiceException {

        LOG.info("Update Work Order for Group Number: '{}' ", eligibilityWorkOrder.getSlacGroupNumber());
        try {
            transactionService.executeTransaction(new TransactionCommand() {

                @Override
                public void execute() {
                    // update Eligibility WorkOrder
                    eligibilityDao.setEligibilityWorkOrder(eligibilityWorkOrder);
                    eligibilityDao.setEligibilityRunDates(eligibilityWorkOrder);
                    eligibilityDao.setWorkOrderProcessFlags(eligibilityWorkOrder);
                    eligibilityDao.deleteLoadedWorkOrderInfo(eligibilityWorkOrder);
                    eligibilityDao.addEligibilityWorkOrderPlanMapping(eligibilityWorkOrder);
                    eligibilityDao.addEligibilityWorkOrderBenefitMapping(eligibilityWorkOrder);
                    eligibilityDao.addEligibilityWorkOrderCertificateMapping(eligibilityWorkOrder);

                    // update Claims WorkOrder (CLAIM & DRUGC) if exists
                    if (eligibilityWorkOrder.getClaimsWorkOrder().getExistingWorkOrderId() != null) {
                        eligibilityDao.setClaimsWorkOrder(eligibilityWorkOrder);
                        eligibilityDao.addPerScriptDeductible(eligibilityWorkOrder);
                    }
                }
            });
        } catch (Exception e) {

            String errorInfoMessage = "Failed to Update for SLAC Group Number: " + eligibilityWorkOrder.getSlacGroupNumber()
                    + " \n\n [Error details : " + e.getMessage() + "]";

            LOG.error(errorInfoMessage);
            throw new ServiceException(errorInfoMessage, Lists.newArrayList(ErrorInfo.createWith(
                    "EligibilityConversionService.updateWorkOrder()", AbstractProperties.FAMILY_GROUP_BENEFIT, ErrorInfo.ERROR,
                    errorInfoMessage)), ExceptionType.BUSINESS_EXCEPTION);
        }
    }

    private void setWorkOrderError(final EligibilityWorkOrder workOrder, final String errorInfoMessage) {

        if (workOrder == null) {
            return;
        }

        LOG.info("Set Work Order Error for Group Number: '{}' ", workOrder.getSlacGroupNumber());
        try {
            transactionService.executeTransaction(new TransactionCommand() {

                @Override
                public void execute() {
                    eligibilityDao.logError(workOrder.getExistingWorkOrderId(), errorInfoMessage, "setWorkOrderError()",
                            eligibilityConfig.getStreamName() + " WorkOrder Load");

                    eligibilityDao.setWorkOrderRecordStatus(workOrder.getExistingWorkOrderId(),
                            AbstractWorkOrder.RECORD_STATUS_FAILURE);

                }
            });
        } catch (Exception e) {
            LOG.error("Failed to Set Work Order Error for SLAC Group Number: " + workOrder.getSlacGroupNumber()
                    + " \n\n [Error details : " + e.getMessage() + "]");
        }
    }

    /**
     * CleanUp loaded files and Notify if enabled
     * 
     * @param workOrderFiles
     */
    private void cleanUp(List<File> workOrderFiles) {
        // move to \in_archive folder successfully processed files
        workOrderFiles.removeAll(failedWorkOrders.keySet());
        WorkOrderUtils.moveFiles(workOrderFiles, eligibilityConfig.getInputArchivePath(), true);

        // move to \in_error folder files that had errors during processing
        WorkOrderUtils.moveFiles(Lists.newArrayList(failedWorkOrders.keySet()), eligibilityConfig.getInputErrorPath(), true);

        notificationService.sendEligibilityLoadResultNotification(eligibilityConfig, workOrderFiles, failedWorkOrders,
                warningWorkOrders);
    }

    /**
     * {@inheritDoc}
     * 
     * @see com.compname.lob.service.StreamConversionService#createResultReport(com.compname.lob.domain.config.AbstractProperties,
     *      com.compname.lob.domain.report.ReportData)
     */
    @Override
    public void createResultReport(ReportData data) {
        // vegirl1: No result reports for Eligibility in Phase 1

        if (CollectionUtils.isEmpty(data.getWorkOrderKeyValues())) {
            LOG.info("The List of Groups to generate reports is empty");
            return;
        }

        // loop through the List of files to create
        for (String fileType : eligibilityConfig.getFilesToCreate()) {
            data.setFileType(fileType);
            // create file for each WorkOrder(ML Group)
            for (Map<String, Object> woKey : data.getWorkOrderKeyValues()) {

                DatasourceUtils.setDbParameterValues(eligibilityConfig, woKey);

                String extractType = woKey.get("EXTRACTION_TYPE").toString();
                String fileName = eligibilityConfig.getFileNames().get(fileType + "." + extractType.toLowerCase());
                // don't have reports for each type of extraction
                if (StringUtils.isEmpty(fileName)) {
                    continue;
                }

                data.setFileName(DatasourceUtils.buildFileName(fileName, eligibilityConfig.getParameters()));

                try {
                    super.retrieveReportData(eligibilityConfig, fileType, data);
                    super.createExcelReport(eligibilityConfig, data);
                } catch (ServiceException ex) {
                    Long workOrderKey = Long.parseLong(woKey.get("WO_KEY").toString());
                    LOG.error("Failed to Create Result Report '{}' for WorkOrder '{}';\n ErrorInfo(s): {} ", data.getFileName(),
                            workOrderKey, ex.getMessage());
                    String processName = extractType + "_" + fileType;
                    eligibilityDao.setWorkOrderProcessStatusToError(workOrderKey, extractType);
                    eligibilityDao.logError(workOrderKey, ex.getMessage(), "createResultReport()", processName.toUpperCase());
                }
            }
        }
    }

    /**
     * setSlacGroupNumberToWorkOrders
     * 
     * @param eligibilityWorkOrder
     * @param excelFile
     */
    private void setSlacGroupNumberToWorkOrders(EligibilityWorkOrder eligibilityWorkOrder, File excelFile) {
        String slacGroupNumber = WorkOrderUtils.getGroupNumberFromFileName(excelFile.getName());
        slacGroupNumber = StringUtils.leftPad(slacGroupNumber, AbstractProperties.SLAC_GROUP_LENGTH, "0");

        eligibilityWorkOrder.setSlacGroupNumber(StringUtils.leftPad(slacGroupNumber, AbstractProperties.SLAC_GROUP_LENGTH, "0"));

        // might not have CLAIM WO
        if (eligibilityWorkOrder.getClaimsWorkOrder() != null) {
            eligibilityWorkOrder.getClaimsWorkOrder().setSlacGroupNumber(eligibilityWorkOrder.getSlacGroupNumber());
        }

        // might not have DRUGC WO
        if (eligibilityWorkOrder.getDrugClaimsWorkOrder() != null) {
            eligibilityWorkOrder.getDrugClaimsWorkOrder().setSlacGroupNumber(eligibilityWorkOrder.getSlacGroupNumber());
        }
    }
}
