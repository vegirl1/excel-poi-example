package com.compname.lob.service;

import java.util.List;


import com.compname.lob.domain.workorder.AbstractWorkOrder;

/**
 * WorkOrderValidationService
 * 
 * @author vegirl1
 * @since Aug 17, 2015
 * @version $Revision$
 */
public interface WorkOrderValidationService<T extends AbstractWorkOrder> {

    void validateWorkOrder(T workOrder, List<ErrorInfo> errorInfos);

}
