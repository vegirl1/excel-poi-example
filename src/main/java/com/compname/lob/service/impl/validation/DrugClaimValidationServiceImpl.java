package com.compname.lob.service.impl.validation;

import java.util.List;

import org.springframework.beans.factory.annotation.Qualifier;

import com.compname.lob.domain.workorder.DrugClaimsWorkOrder;
import com.compname.lob.service.impl.dao.eligibility.EligibilityDao;

/**
 * DrugClaimValidationServiceImpl
 * 
 * @author vegirl1
 * @since Oct 20, 2015
 * @version $Revision$
 */
public class DrugClaimValidationServiceImpl extends WorkOrderValidationServiceImpl<DrugClaimsWorkOrder> {

    /**
     * Class constructor.
     * 
     */
    public DrugClaimValidationServiceImpl(@Qualifier("eligibilityDao") EligibilityDao eligibilityDao) {
        super(eligibilityDao);
    }

    /**
     * {@inheritDoc}
     * 
     * @see com.compname.lob.service.WorkOrderValidationService#validateWorkOrder(com.compname.lob.domain.workorder.AbstractWorkOrder)
     */
    @Override
    public void validateWorkOrder(DrugClaimsWorkOrder workOrder, List<ErrorInfo> errorInfos) {

        // retrieve existing WorkOrders from DB(if exists) and set them to the current DrugClaim
        DrugClaimsWorkOrder existingWorkOrder = new DrugClaimsWorkOrder();
        retrieveExistingWorkOrders(workOrder.getSlacGroupNumber(), DrugClaimsWorkOrder.RECORD_TYPE_DRUGC, existingWorkOrder);
        setExistingWorkOrderToCurrent(workOrder, existingWorkOrder);

        // vegirl1, no other validation so far
    }

}
