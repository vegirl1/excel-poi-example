package com.compname.lob.domain.workorder;

import java.util.List;


import com.google.common.collect.Lists;

/**
 * AbstarctWorkOrder
 * 
 * @author vegirl1
 * @since Jun 15, 2015
 * @version $Revision$
 */
public abstract class AbstractWorkOrder extends DomainModelObject {

    private static final long          serialVersionUID             = -6478571772356062063L;

    public static final String         RECORD_STATUS_SUCCESS        = "SUCCESS";
    public static final String         RECORD_STATUS_FAILURE        = "FAILURE";
    public static final String         RECORD_STATUS_UNPROCESSED    = "UNPROCESSED";

    public static final List<String>   YES_FLAGS_LIST               = Lists.newArrayList("Yes", "Y", "YES", "yes", "y");
    public static final List<String>   NO_FLAGS_LIST                = Lists.newArrayList("No", "N", "NO", "no", "n");

    public static final String         Y_LABEL                      = "Y";
    public static final String         N_LABEL                      = "N";

    private String                     woType;

    private Long                       workOrderId;
    private Long                       existingWorkOrderId;

    private String                     slacGroupNumber;

    private String                     clientName;
    private String                     conversionDate;
    private String                     terminationDate;

    private String                     reRunInitialRequest;
    private String                     reRunDeltaRequest;

    private WorkOrderRequestDate       initialRequestRunDate;
    private List<WorkOrderRequestDate> deltaRequestRunDates         = Lists.newLinkedList();

    private WorkOrderRequestDate       existingInitialRequestRunDate;
    private List<WorkOrderRequestDate> existingDeltaRequestRunDates = Lists.newLinkedList();

    /**
     * Class constructor.
     * 
     */
    public AbstractWorkOrder(String woType) {
        this.woType = woType;
    }

    public WorkOrderRequestDate getDeltaFirstRequestRunDate() {
        return deltaRequestRunDates.get(0);
    }

    public WorkOrderRequestDate getExistingDeltaFirstRequestRunDate() {
        return existingDeltaRequestRunDates.get(0);
    }

    /**
     * Getter method of the <code>"workOrderId"</code> class attribute.
     * 
     * @return the workOrderId.
     */
    public Long getWorkOrderId() {
        return this.workOrderId;
    }

    /**
     * Setter method of the <code>"workOrderId"</code> class attribute.
     * 
     * @param workOrderId the workOrderId to set.
     */
    public void setWorkOrderId(Long workOrderId) {
        this.workOrderId = workOrderId;
    }

    /**
     * Getter method of the <code>"slacGroupNumber"</code> class attribute.
     * 
     * @return the slacGroupNumber.
     */
    public String getSlacGroupNumber() {
        return this.slacGroupNumber;
    }

    /**
     * Setter method of the <code>"slacGroupNumber"</code> class attribute.
     * 
     * @param SlacGroupNumber the slacGroupNumber to set.
     */
    public void setSlacGroupNumber(String slacGroupNumber) {
        this.slacGroupNumber = slacGroupNumber;
    }

    /**
     * Getter method of the <code>"clientName"</code> class attribute.
     * 
     * @return the clientName.
     */
    public String getClientName() {
        return this.clientName;
    }

    /**
     * Setter method of the <code>"clientName"</code> class attribute.
     * 
     * @param ClientName the clientName to set.
     */
    public void setClientName(String clientName) {
        this.clientName = clientName;
    }

    /**
     * Getter method of the <code>"conversionDate"</code> class attribute.
     * 
     * @return the conversionDate.
     */
    public String getConversionDate() {
        return this.conversionDate;
    }

    /**
     * Setter method of the <code>"conversionDate"</code> class attribute.
     * 
     * @param ConversionDate the conversionDate to set.
     */
    public void setConversionDate(String conversionDate) {
        this.conversionDate = conversionDate;
    }

    /**
     * Getter method of the <code>"reRunInitialRequest"</code> class attribute.
     * 
     * @return the reRunInitialRequest.
     */
    public String getReRunInitialRequest() {
        return this.reRunInitialRequest;
    }

    /**
     * Setter method of the <code>"reRunInitialRequest"</code> class attribute.
     * 
     * @param ReRunInitialRequest the reRunInitialRequest to set.
     */
    public void setReRunInitialRequest(String reRunInitialRequest) {
        this.reRunInitialRequest = reRunInitialRequest;
    }

    /**
     * Getter method of the <code>"reRunDeltaRequest"</code> class attribute.
     * 
     * @return the reRunDeltaRequest.
     */
    public String getReRunDeltaRequest() {
        return this.reRunDeltaRequest;
    }

    /**
     * Setter method of the <code>"reRunDeltaRequest"</code> class attribute.
     * 
     * @param ReRunDeltaRequest the reRunDeltaRequest to set.
     */
    public void setReRunDeltaRequest(String reRunDeltaRequest) {
        this.reRunDeltaRequest = reRunDeltaRequest;
    }

    /**
     * Getter method of the <code>"existingWorkOrderId"</code> class attribute.
     * 
     * @return the existingWorkOrderId.
     */
    public Long getExistingWorkOrderId() {
        return existingWorkOrderId;
    }

    /**
     * Setter method of the <code>"existingWorkOrderId"</code> class attribute.
     * 
     * @param ExistingWorkOrderId the existingWorkOrderId to set.
     */
    public void setExistingWorkOrderId(Long existingWorkOrderId) {
        this.existingWorkOrderId = existingWorkOrderId;
    }

    /**
     * Getter method of the <code>"initialRequestRunDate"</code> class attribute.
     * 
     * @return the initialRequestRunDate.
     */
    public WorkOrderRequestDate getInitialRequestRunDate() {
        return this.initialRequestRunDate;
    }

    /**
     * Setter method of the <code>"initialRequestRunDate"</code> class attribute.
     * 
     * @param InitialRequestRunDate the initialRequestRunDate to set.
     */
    public void setInitialRequestRunDate(WorkOrderRequestDate initialRequestRunDate) {
        this.initialRequestRunDate = initialRequestRunDate;
    }

    /**
     * Getter method of the <code>"deltaRequestRunDate"</code> class attribute.
     * 
     * @return the deltaRequestRunDate.
     */
    public List<WorkOrderRequestDate> getDeltaRequestRunDates() {
        return this.deltaRequestRunDates;
    }

    /**
     * Setter method of the <code>"deltaRequestRunDate"</code> class attribute.
     * 
     * @param DeltaRequestRunDate the deltaRequestRunDate to set.
     */
    public void setDeltaRequestRunDates(List<WorkOrderRequestDate> deltaRequestRunDates) {
        this.deltaRequestRunDates = deltaRequestRunDates;
    }

    /**
     * Getter method of the <code>"existingInitialRequestRunDate"</code> class attribute.
     * 
     * @return the existingInitialRequestRunDate.
     */
    public WorkOrderRequestDate getExistingInitialRequestRunDate() {
        return this.existingInitialRequestRunDate;
    }

    /**
     * Setter method of the <code>"existingInitialRequestRunDate"</code> class attribute.
     * 
     * @param ExistingInitialRequestRunDate the existingInitialRequestRunDate to set.
     */
    public void setExistingInitialRequestRunDate(WorkOrderRequestDate aExistingInitialRequestRunDate) {
        this.existingInitialRequestRunDate = aExistingInitialRequestRunDate;
    }

    /**
     * Getter method of the <code>"existingDeltaRequestRunDates"</code> class attribute.
     * 
     * @return the existingDeltaRequestRunDates.
     */
    public List<WorkOrderRequestDate> getExistingDeltaRequestRunDates() {
        return this.existingDeltaRequestRunDates;
    }

    /**
     * Setter method of the <code>"existingDeltaRequestRunDates"</code> class attribute.
     * 
     * @param ExistingDeltaRequestRunDates the existingDeltaRequestRunDates to set.
     */
    public void setExistingDeltaRequestRunDates(List<WorkOrderRequestDate> aExistingDeltaRequestRunDates) {
        this.existingDeltaRequestRunDates = aExistingDeltaRequestRunDates;
    }

    /**
     * Getter method of the <code>"terminationDate"</code> class attribute.
     * 
     * @return the terminationDate.
     */
    public String getTerminationDate() {
        return terminationDate;
    }

    /**
     * Setter method of the <code>"terminationDate"</code> class attribute.
     * 
     * @param TerminationDate the terminationDate to set.
     */
    public void setTerminationDate(String terminationDate) {
        this.terminationDate = terminationDate;
    }

    /**
     * Getter method of the <code>"woType"</code> class attribute.
     * 
     * @return the woType.
     */
    public String getWoType() {
        return this.woType;
    }

    /**
     * Setter method of the <code>"woType"</code> class attribute.
     * 
     * @param woType the woType to set.
     */
    public void setWoType(String woType) {
        this.woType = woType;
    }

}
