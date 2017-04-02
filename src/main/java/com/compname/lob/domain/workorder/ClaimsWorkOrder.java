package com.compname.lob.domain.workorder;

/**
 * ClaimsWorkOrder
 * 
 * @author vegirl1
 * @since Jun 15, 2015
 * @version $Revision$
 */
public class ClaimsWorkOrder extends AbstractWorkOrder {

    private static final long  serialVersionUID               = 3640652650288515563L;

    public static final String RECORD_TYPE_CLAIM              = "CLAIM";
    public static final String CLAIM_INITIAL_REQUEST_RUN_DATE = "20150912";

    private String             claimBackdatedEffectiveDate;
    private String             claimReimbursement;
    private String             claimPayDirect;

    /**
     * Class constructor.
     * 
     */
    public ClaimsWorkOrder() {
        super(RECORD_TYPE_CLAIM);
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
    public void setClaimBackdatedEffectiveDate(String claimBackdatedEffectiveDate) {
        this.claimBackdatedEffectiveDate = claimBackdatedEffectiveDate;
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
    public void setClaimReimbursement(String claimReimbursement) {
        this.claimReimbursement = claimReimbursement;
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
    public void setClaimPayDirect(String claimPayDirect) {
        this.claimPayDirect = claimPayDirect;
    }
}
