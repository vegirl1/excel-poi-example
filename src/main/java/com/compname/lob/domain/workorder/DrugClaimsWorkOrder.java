package com.compname.lob.domain.workorder;

/**
 * DrugClaimsWorkOrder
 * 
 * @author vegirl1
 * @since Aug 28, 2015
 * @version $Revision$
 */
public class DrugClaimsWorkOrder extends AbstractWorkOrder {

    private static final long  serialVersionUID  = -757135210361327900L;

    public static final String RECORD_TYPE_DRUGC = "DRUGC";
    public static final String INIT_RQST_RUN_DT  = "20150912";

    private String             claimBackdatedEffectiveDate;
    private String             claimReimbursement;
    private String             claimPayDirect;

    /**
     * Class constructor.
     * 
     */
    public DrugClaimsWorkOrder() {
        super(RECORD_TYPE_DRUGC);
    }

    /**
     * Getter method of the <code>"claimBackdatedEffectiveDate"</code> class attribute.
     * 
     * @return the claimBackdatedEffectiveDate.
     */
    public String getClaimBackdatedEffectiveDate() {
        return this.claimBackdatedEffectiveDate;
    }

    /**
     * Setter method of the <code>"claimBackdatedEffectiveDate"</code> class attribute.
     * 
     * @param ClaimBackdatedEffectiveDate the claimBackdatedEffectiveDate to set.
     */
    public void setClaimBackdatedEffectiveDate(String aClaimBackdatedEffectiveDate) {
        this.claimBackdatedEffectiveDate = aClaimBackdatedEffectiveDate;
    }

    /**
     * Getter method of the <code>"claimReimbursement"</code> class attribute.
     * 
     * @return the claimReimbursement.
     */
    public String getClaimReimbursement() {
        return this.claimReimbursement;
    }

    /**
     * Setter method of the <code>"claimReimbursement"</code> class attribute.
     * 
     * @param ClaimReimbursement the claimReimbursement to set.
     */
    public void setClaimReimbursement(String aClaimReimbursement) {
        this.claimReimbursement = aClaimReimbursement;
    }

    /**
     * Getter method of the <code>"claimPayDirect"</code> class attribute.
     * 
     * @return the claimPayDirect.
     */
    public String getClaimPayDirect() {
        return this.claimPayDirect;
    }

    /**
     * Setter method of the <code>"claimPayDirect"</code> class attribute.
     * 
     * @param ClaimPayDirect the claimPayDirect to set.
     */
    public void setClaimPayDirect(String aClaimPayDirect) {
        this.claimPayDirect = aClaimPayDirect;
    }

}
