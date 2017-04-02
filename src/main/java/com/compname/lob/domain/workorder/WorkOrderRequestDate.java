package com.compname.lob.domain.workorder;

import org.apache.commons.lang.StringUtils;


/**
 * WorkOrderRequestDate
 * 
 * @author vegirl1
 * @since Oct 9, 2015
 * @version $Revision$
 */
public class WorkOrderRequestDate extends DomainModelObject {

    private static final long  serialVersionUID = 3978865697993505611L;

    public static final String CURRENT          = "CURRENT";
    public static final String DELTA            = "DELTA";
    // Delta was put out of scope
    public static final String DELTA_DATE_1     = "20990101";
    public static final String DELTA_DATE_2     = "20990102";

    private Long               requestRunId;
    private String             requestRunDate;
    private String             requestRunType;
    private String             processedRunDate;
    private String             processedRunStatus;
    private boolean            deleteMemberes;
    private boolean            deleteRunDate;

    /**
     * Class constructor.
     * 
     */
    public WorkOrderRequestDate() {
    }

    public WorkOrderRequestDate(String runDate, String runType) {
        this.requestRunDate = runDate;
        this.requestRunType = runType;
    }

    public static WorkOrderRequestDate createWith(Long requestRunId, String requestRunDate, String requestRunType,
            String processedRunDate, String processedRunStatus) {
        WorkOrderRequestDate workOrderRequestDate = new WorkOrderRequestDate();
        workOrderRequestDate.setRequestRunId(requestRunId);
        workOrderRequestDate.setRequestRunDate(requestRunDate);
        workOrderRequestDate.setRequestRunType(requestRunType);
        workOrderRequestDate.setProcessedRunDate(processedRunDate);
        workOrderRequestDate.setProcessedRunStatus(processedRunStatus);
        return workOrderRequestDate;
    }

    /**
     * Getter method of the <code>"requestRunDate"</code> class attribute.
     * 
     * @return the requestRunDate.
     */
    public String getRequestRunDate() {
        return this.requestRunDate;
    }

    /**
     * Setter method of the <code>"requestRunDate"</code> class attribute.
     * 
     * @param RequestRunDate the requestRunDate to set.
     */
    public void setRequestRunDate(String aRequestRunDate) {
        this.requestRunDate = aRequestRunDate;
    }

    /**
     * Getter method of the <code>"requestRunType"</code> class attribute.
     * 
     * @return the requestRunType.
     */
    public String getRequestRunType() {
        return this.requestRunType;
    }

    /**
     * Setter method of the <code>"requestRunType"</code> class attribute.
     * 
     * @param RequestRunType the requestRunType to set.
     */
    public void setRequestRunType(String aRequestRunType) {
        this.requestRunType = aRequestRunType;
    }

    /**
     * Getter method of the <code>"processedRunDate"</code> class attribute.
     * 
     * @return the processedRunDate.
     */
    public String getProcessedRunDate() {
        return this.processedRunDate;
    }

    /**
     * Setter method of the <code>"processedRunDate"</code> class attribute.
     * 
     * @param ProcessedRunDate the processedRunDate to set.
     */
    public void setProcessedRunDate(String aProcessedRunDate) {
        this.processedRunDate = aProcessedRunDate;
    }

    /**
     * Getter method of the <code>"requestRunId"</code> class attribute.
     * 
     * @return the requestRunId.
     */
    public Long getRequestRunId() {
        return requestRunId;
    }

    /**
     * Setter method of the <code>"requestRunId"</code> class attribute.
     * 
     * @param RequestRunId the requestRunId to set.
     */
    public void setRequestRunId(Long aRequestRunId) {
        this.requestRunId = aRequestRunId;
    }

    /**
     * Getter method of the <code>"processedRunStatus"</code> class attribute.
     * 
     * @return the processedRunStatus.
     */
    public String getProcessedRunStatus() {
        return this.processedRunStatus;
    }

    /**
     * Setter method of the <code>"processedRunStatus"</code> class attribute.
     * 
     * @param ProcessedRunStatus the processedRunStatus to set.
     */
    public void setProcessedRunStatus(String aProcessedRunStatus) {
        this.processedRunStatus = aProcessedRunStatus;
    }

    /**
     * Getter method of the <code>"deleteRunDate"</code> class attribute.
     * 
     * @return the deleteRunDate.
     */
    public boolean isDeleteRunDate() {
        return this.deleteRunDate;
    }

    /**
     * Setter method of the <code>"deleteRunDate"</code> class attribute.
     * 
     * @param DeleteRunDate the deleteRunDate to set.
     */
    public void setDeleteRunDate(boolean aDeleteRunDate) {
        this.deleteRunDate = aDeleteRunDate;
    }

    /**
     * Getter method of the <code>"deleteMemberes"</code> class attribute.
     * 
     * @return the deleteMemberes.
     */
    public boolean isDeleteMemberes() {
        return this.deleteMemberes;
    }

    /**
     * Setter method of the <code>"deleteMemberes"</code> class attribute.
     * 
     * @param DeleteMemberes the deleteMemberes to set.
     */
    public void setDeleteMemberes(boolean aDeleteMemberes) {
        this.deleteMemberes = aDeleteMemberes;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final WorkOrderRequestDate other = (WorkOrderRequestDate) obj;

        return StringUtils.equals(this.requestRunDate, other.requestRunDate);

    }
    @Override
    public int hashCode() {
        return super.hashCode();
    }

}
