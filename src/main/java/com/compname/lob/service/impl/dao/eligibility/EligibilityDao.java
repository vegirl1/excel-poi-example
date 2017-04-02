package com.compname.lob.service.impl.dao.eligibility;

import com.compname.lob.domain.workorder.EligibilityWorkOrder;
import com.compname.lob.service.impl.dao.datasource.AbstractConversionDao;

/**
 * WorkOrder Dao Interface
 * 
 * @author vegirl1
 * @since May 26, 2015
 * @version $Revision$
 */
public interface EligibilityDao extends AbstractConversionDao {

    boolean isSAGGroup(String slacGroup);

    void setEligibilityWorkOrder(EligibilityWorkOrder eligibilityWorkOrder);

    void setEligibilityRunDates(EligibilityWorkOrder eligibilityWorkOrder);

    void addEligibilityWorkOrderPlanMapping(EligibilityWorkOrder eligibilityWorkOrder);

    void addEligibilityWorkOrderBenefitMapping(EligibilityWorkOrder eligibilityWorkOrder);

    void addEligibilityWorkOrderCertificateMapping(EligibilityWorkOrder eligibilityWorkOrder);

    void setClaimsWorkOrder(EligibilityWorkOrder eligibilityWorkOrder);

    void setWorkOrderRecordStatus(Long workOrderKey, String recordStatus);

    void deleteLoadedWorkOrderInfo(EligibilityWorkOrder eligibilityWorkOrder);

    void setWorkOrderProcessFlags(EligibilityWorkOrder eligibilityWorkOrder);

    void addPerScriptDeductible(EligibilityWorkOrder eligibilityWorkOrder);
}
