package com.compname.lob.service.impl.validation;

import java.text.MessageFormat;
import java.util.List;

import org.apache.commons.lang.StringUtils;


import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.compname.lob.domain.config.AbstractProperties;
import com.compname.lob.domain.workorder.AbstractWorkOrder;
import com.compname.lob.service.WorkOrderValidationService;
import com.compname.lob.service.impl.dao.eligibility.EligibilityDao;
import com.compname.lob.utils.SqlDateUtils;

/**
 * WorkOrderValidationServiceImpl
 * 
 * @author vegirl1
 * @since Oct 7, 2015
 * @version $Revision$
 */
public abstract class WorkOrderValidationServiceImpl<E extends AbstractWorkOrder> implements WorkOrderValidationService<E> {

    private static final String    ERR_MSG_SLAC_GROUP_NOT_EXISTS   = "Provided Slac Group Number {0} doesn't exist in COMPAS db.";
    private static final String    ERR_MSG_NOT_VALID_DATE_FORMAT   = "Provided dates must have a valid YYYYMMDD date format.";
    private static final String    ERR_MSG_RERUN_YES_WO_NOT_EXISTS = "Re-Run (Initial/Delta) format = 'Yes' and no conversion exist for the group {0}.";
    private static final String    ERR_MSG_RERUN_NO_WO_EXISTS      = "Re-Run (Initial/Delta) = 'No' and the conversion exists for the group {0}.";
    protected static final String  ERR_MSG_WO_MANDATORY_DATES      = "Conversion Date, Initial Request Run Date and Delta Run Date must be not empty.";
    private static final String    ERR_MSG_WRONG_DATES             = "Provided wrong value(s) for date(s), can not compare them.";

    protected final EligibilityDao eligibilityDao;

    /**
     * Class constructor.
     * 
     */
    public WorkOrderValidationServiceImpl(EligibilityDao eligibilityDao) {
        this.eligibilityDao = eligibilityDao;
    }

    protected void validateSlacGroup(String slacGroup, List<ErrorInfo> errorInfos) {
        // provided group exists in Compass
        if (!eligibilityDao.isValidSlacGroupNumber(slacGroup)) {
            errorInfos.add(ErrorInfo.createWith("WorkOrderValidationServiceImpl.validateSlacGroup()",
                    AbstractProperties.FAMILY_GROUP_BENEFIT, ErrorInfo.ERROR,
                    MessageFormat.format(ERR_MSG_SLAC_GROUP_NOT_EXISTS, slacGroup)));
        }
    }

    protected void validateDateFormat(List<String> dates, List<ErrorInfo> errorInfos) {
        // date format must be YYYYMMDD
        Iterable<String> validDates = Iterables.filter(dates, new Predicate<String>() {
            public boolean apply(String input) {
                return (SqlDateUtils.stringToSqlDate(input, SqlDateUtils.SQL_DATE_FORMAT) != null);
            }
        });

        if (Iterables.size(validDates) != dates.size()) {
            errorInfos.add(ErrorInfo.createWith("WorkOrderValidationServiceImpl.validateDateFormat()",
                    AbstractProperties.FAMILY_GROUP_BENEFIT, ErrorInfo.ERROR, ERR_MSG_NOT_VALID_DATE_FORMAT));
        }
    }

    protected void validateMandatoryDates(List<String> dates, String errorMessage, List<ErrorInfo> errorInfos) {
        // date must be not empty
        Iterable<String> validDates = Iterables.filter(dates, new Predicate<String>() {
            public boolean apply(String input) {
                return (StringUtils.isNotEmpty(input));
            }
        });

        if (Iterables.size(validDates) != dates.size()) {
            errorInfos.add(ErrorInfo.createWith("WorkOrderValidationServiceImpl.validateMandatoryDates()",
                    AbstractProperties.FAMILY_GROUP_BENEFIT, ErrorInfo.ERROR, errorMessage));
        }
    }

    protected void validateRequestDates(String firstDate, String secondDate, String errorMessage, List<ErrorInfo> errorInfos) {
        // should be firstDate < secondDate

        boolean isNotValidDate = false;
        String errMsg = StringUtils.EMPTY;

        java.sql.Date date1 = SqlDateUtils.stringToSqlDate(firstDate, SqlDateUtils.SQL_DATE_FORMAT);
        java.sql.Date date2 = SqlDateUtils.stringToSqlDate(secondDate, SqlDateUtils.SQL_DATE_FORMAT);

        if (date1 == null || date2 == null) {
            isNotValidDate = true;
            errMsg = ERR_MSG_WRONG_DATES;
        } else if (date1.compareTo(date2) >= 0) {
            isNotValidDate = true;
            errMsg = errorMessage;
        }

        if (isNotValidDate) {
            errorInfos.add(ErrorInfo.createWith("WorkOrderValidationServiceImpl.validateRequestDates()",
                    AbstractProperties.FAMILY_GROUP_BENEFIT, ErrorInfo.ERROR,
                    MessageFormat.format(errMsg, StringUtils.defaultString(firstDate), StringUtils.defaultString(secondDate))));

        }
    }

    protected void validateYesNoIndicatorValues(List<String> rerunIndicators, String errorMessage, List<ErrorInfo> errorInfos) {
        // Yes or No
        if (Iterables.size(filterYesNoValues(rerunIndicators)) != rerunIndicators.size()) {
            errorInfos.add(ErrorInfo.createWith("WorkOrderValidationServiceImpl.validateRerunIndicator()",
                    AbstractProperties.FAMILY_GROUP_BENEFIT, ErrorInfo.ERROR, errorMessage));
        }
    }

    /**
     * filterYesNoValues
     * 
     * @param rerunIndicators
     * @return
     */
    protected Iterable<String> filterYesNoValues(List<String> rerunIndicators) {
        Iterable<String> validDates = Iterables.filter(rerunIndicators, new Predicate<String>() {
            public boolean apply(String input) {
                return (AbstractWorkOrder.YES_FLAGS_LIST.contains(input) || AbstractWorkOrder.NO_FLAGS_LIST.contains(input));
            }
        });
        return validDates;
    }

    /**
     * retrieveExistingWorkOrders
     * 
     * @param workOrder
     * @return
     */
    protected <T extends AbstractWorkOrder> void retrieveExistingWorkOrders(String slacGroup, String workOrderType,
            T existingWorkOrder) {
        eligibilityDao.retrieveExistingWorkOrder(slacGroup, workOrderType, existingWorkOrder);
    }

    /**
     * setExistingWorkOrderToCurrent
     * 
     * @param workOrder
     * @param existingWorkOrder
     */
    protected <T extends AbstractWorkOrder> void setExistingWorkOrderToCurrent(T currentWorkOrder, T existingWorkOrder) {
        currentWorkOrder.setExistingWorkOrderId(existingWorkOrder.getExistingWorkOrderId());
        currentWorkOrder.setExistingInitialRequestRunDate(existingWorkOrder.getExistingInitialRequestRunDate());
        currentWorkOrder.setExistingDeltaRequestRunDates(existingWorkOrder.getExistingDeltaRequestRunDates());
    }

    protected <T extends AbstractWorkOrder> void validateRerunIndicatorYesValues(T workOrder, List<ErrorInfo> errorInfos) {
        // if WorkOrder doesn't exists all rerun indicators must be No

        if ((AbstractWorkOrder.YES_FLAGS_LIST.contains(workOrder.getReRunInitialRequest()) || AbstractWorkOrder.YES_FLAGS_LIST
                .contains(workOrder.getReRunDeltaRequest())) && workOrder.getExistingWorkOrderId() == null) {
            errorInfos.add(ErrorInfo.createWith(
                    "WorkOrderValidationServiceImpl.validateRerunIndicatorYesValues()",
                    AbstractProperties.FAMILY_GROUP_BENEFIT,
                    ErrorInfo.ERROR,
                    MessageFormat.format(workOrder.getWoType() + " - " + ERR_MSG_RERUN_YES_WO_NOT_EXISTS,
                            workOrder.getSlacGroupNumber())));
        }
    }

    protected <T extends AbstractWorkOrder> void validateRerunIndicatorNoValues(T workOrder, List<ErrorInfo> errorInfos) {

        if (AbstractWorkOrder.NO_FLAGS_LIST.contains(workOrder.getReRunInitialRequest())
                && workOrder.getExistingWorkOrderId() != null
                && StringUtils.isNotEmpty(workOrder.getExistingInitialRequestRunDate().getProcessedRunDate())
                && AbstractWorkOrder.NO_FLAGS_LIST.contains(workOrder.getReRunDeltaRequest())) {
            errorInfos.add(ErrorInfo.createWith("WorkOrderValidationServiceImpl.validateRerunIndicatorNoValues()",
                    AbstractProperties.FAMILY_GROUP_BENEFIT, ErrorInfo.ERROR, MessageFormat.format(workOrder.getWoType() + " - "
                            + ERR_MSG_RERUN_NO_WO_EXISTS, workOrder.getSlacGroupNumber())));
        }
    }

    protected void addStringDateToList(String stringDate, List<String> dates) {
        if (StringUtils.isNotEmpty(stringDate)) {
            dates.add(stringDate);
        }
    }
}
