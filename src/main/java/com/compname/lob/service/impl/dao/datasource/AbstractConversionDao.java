package com.compname.lob.service.impl.dao.datasource;

import java.sql.Date;

import com.compname.lob.domain.workorder.AbstractWorkOrder;

/**
 * AbstractConversionDao
 * 
 * @author vegirl1
 * @since Aug 14, 2015
 * @version $Revision$
 */
public interface AbstractConversionDao {

    /**
     * Service that retrieves the WorkOrder Id from data base.
     * 
     * @return
     */
    Long getWorkOrderId();

    /**
     * Service that retrieves the COMPAS batch date from data base.
     * 
     * @return
     */
    Date getCompassBatchDate();

    /**
     * Service that log the error into data base.
     * 
     * @param workOrderKey
     * @param errorMessage
     * @param methodName
     * @param processName
     * @return
     */
    void logError(Long workOrderKey, String errorMessage, String methodName, String processName);

    /**
     * Service that checks if provided group is a SLAC valid one .
     * 
     * @param slacGroup
     * @return
     */
    boolean isValidSlacGroupNumber(String slacGroup);

    /**
     * Service that checks if provided group is a valid SAG (self admin group) one .
     * 
     * @param slacGroup
     * @return
     */
    boolean isSAGGroup(String slacGroup);

    /**
     * Service that checks if a work order exists for provided group and type.
     * 
     * @param slacGroup
     * @param recordType
     * @return
     */
    boolean isWorkOrderExists(String slacGroup, String recordType);

    /**
     * Service that will retrieve an existing work order for provided group and type.
     * 
     * @param slacGroup
     * @param recordType
     * @param existingWorkOrder
     * @return T extends AbstractWorkOrder
     */
    <T extends AbstractWorkOrder> void retrieveExistingWorkOrder(String slacGroup, String recordType, T existingWorkOrder);

    /**
     * Service that compare the SLAC existing Group Plan (classes and divisions) with the provided by WO one
     * 
     * @param slacGroup
     * @return null if NO differences, a String containing differences
     */
    String getSlacAndWorkOrdPlanDiff(String slacGroup);

    /**
     * Service that return the Certificate mapping as a String List
     * 
     * @param woKey
     * @return null if NO mapping, a Certificate mapping as a String List
     */
    String getCertMappingAsList(Long woKey);

    /**
     * Service that return the Group Classes as a string
     * 
     * @param slacGroup
     * @return null if NO classes, a Group Classes as a String List
     */
    String getDeductibleClassesAsList(String slacGroup);

    /**
     * Service that return the Group Classes as a string
     * 
     * @param workOrderKey
     * @param extractionType
     * @return set WorkOrder Process Status To Error
     */
    void setWorkOrderProcessStatusToError(Long workOrderKey, String extractionType);

}
