package com.compname.lob.service.impl.validation;

import java.util.List;

import org.springframework.beans.factory.annotation.Qualifier;


import com.google.common.collect.Lists;
import com.compname.lob.domain.workorder.ClaimsWorkOrder;
import com.compname.lob.service.impl.dao.eligibility.EligibilityDao;

/**
 * ClaimValidationServiceImpl. All those validation implements the rules defined in "Claim Work Order Validation and Reload" from GB
 * Conversion Work Order Load functional specification
 * 
 * @author vegirl1
 * @since Oct 7, 2015
 * @version $Revision$
 */
public class ClaimValidationServiceImpl extends WorkOrderValidationServiceImpl<ClaimsWorkOrder> {

    private static final String ERR_MSG_CLAIM_INITIAL_DELTA1_DATES = "The Claims Initial Request date {0} should be less than the Delta Request date {1}.";
    private static final String ERR_MSG_CLAIM_GOLIVE_DELTA1_DATES  = "The Claims Conversion/Go Live Date {0} should be less than the Delta Request date {1}.";
    private static final String ERR_MSG_CLAIM_YES_NO_INDICATOR     = "Re-Run initial Request, Re-Run Delta Request and Pay Direct have invalid format.";

    /**
     * Class constructor.
     * 
     */
    public ClaimValidationServiceImpl(@Qualifier("eligibilityDao") EligibilityDao eligibilityDao) {
        super(eligibilityDao);
    }

    /**
     * {@inheritDoc}
     * 
     * @see com.compname.lob.service.WorkOrderValidationService#validateWorkOrder(com.compname.lob.domain.workorder.AbstractWorkOrder)
     */
    @Override
    public void validateWorkOrder(ClaimsWorkOrder workOrder, List<ErrorInfo> errorInfos) {

        validateDateFormat(getDatesToValidateFormat(workOrder), errorInfos);
        validateMandatoryDates(getMandatoryDates(workOrder), WorkOrderValidationServiceImpl.ERR_MSG_WO_MANDATORY_DATES, errorInfos);

        validateRequestDates(workOrder.getInitialRequestRunDate().getRequestRunDate(), workOrder.getDeltaFirstRequestRunDate()
                .getRequestRunDate(), ERR_MSG_CLAIM_INITIAL_DELTA1_DATES, errorInfos);

        validateRequestDates(workOrder.getConversionDate(), workOrder.getDeltaFirstRequestRunDate().getRequestRunDate(),
                ERR_MSG_CLAIM_GOLIVE_DELTA1_DATES, errorInfos);

        validateYesNoIndicatorValues(
                Lists.newArrayList(workOrder.getReRunInitialRequest(), workOrder.getReRunDeltaRequest(),
                        workOrder.getClaimPayDirect()), ERR_MSG_CLAIM_YES_NO_INDICATOR, errorInfos);

        // retrieve existing WorkOrders from DB(if exists) and set them to the current Claim
        ClaimsWorkOrder existingWorkOrder = new ClaimsWorkOrder();
        retrieveExistingWorkOrders(workOrder.getSlacGroupNumber(), ClaimsWorkOrder.RECORD_TYPE_CLAIM, existingWorkOrder);
        setExistingWorkOrderToCurrent(workOrder, existingWorkOrder);

        validateRerunIndicatorYesValues(workOrder, errorInfos);
        validateRerunIndicatorNoValues(workOrder, errorInfos);
    }

    private List<String> getDatesToValidateFormat(ClaimsWorkOrder workOrder) {
        List<String> dates = Lists.newArrayList();
        dates.addAll(getMandatoryDates(workOrder));
        addStringDateToList(workOrder.getConversionDate(), dates);
        addStringDateToList(workOrder.getClaimBackdatedEffectiveDate(), dates);
        return dates;
    }

    private List<String> getMandatoryDates(ClaimsWorkOrder workOrder) {
        List<String> dates = Lists.newArrayList();
        dates.add(workOrder.getInitialRequestRunDate().getRequestRunDate());
        dates.add(workOrder.getDeltaFirstRequestRunDate().getRequestRunDate());
        dates.add(workOrder.getConversionDate());
        return dates;
    }

}
