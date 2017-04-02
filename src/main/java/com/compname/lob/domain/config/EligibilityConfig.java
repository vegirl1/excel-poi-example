package com.compname.lob.domain.config;

import java.util.List;
import java.util.Map;

/**
 * EligibilityConfig
 * 
 * @author vegirl1
 * @since May 28, 2015
 * @version $Revision$
 */
public class EligibilityConfig extends AbstractProperties {

    private static final long   serialVersionUID = 2476364392214302362L;

    public static final String  STREAM_NAME      = "eligibility";

    private Map<String, String> sheetTableDescriptions;

    private List<String>        planMappingTables;
    private List<String>        certificateMappingTables;
    private List<String>        benefitMappingTables;
    private List<String>        deductibleMappingTables;

    // email's configurations
    private boolean             emailEnabled;
    private String              emailFrom;
    private String              emailSubject;
    private String              emailTo;
    private String              emailCc;

    /**
     * Class constructor.
     * 
     */
    public EligibilityConfig() {
        super(STREAM_NAME);
    }

    /**
     * Getter method of the <code>"sheetTableNames"</code> class attribute.
     * 
     * @return the sheetTableNames. sheetTableDescriptions
     */
    public Map<String, String> getSheetTableDescriptions() {
        return sheetTableDescriptions;
    }

    /**
     * Setter method of the <code>"sheetTableNames"</code> class attribute.
     * 
     * @param SheetTableNames the sheetTableNames to set.
     */
    public void setSheetTableDescriptions(Map<String, String> sheetTableDescriptions) {
        this.sheetTableDescriptions = sheetTableDescriptions;
    }

    /**
     * Getter method of the <code>"planMappingTables"</code> class attribute.
     * 
     * @return the planMappingTables.
     */
    public List<String> getPlanMappingTables() {
        return this.planMappingTables;
    }

    /**
     * Setter method of the <code>"planMappingTables"</code> class attribute.
     * 
     * @param PlanMappingTables the planMappingTables to set.
     */
    public void setPlanMappingTables(List<String> planMappingTables) {
        this.planMappingTables = planMappingTables;
    }

    /**
     * Getter method of the <code>"certificateMappingTables"</code> class attribute.
     * 
     * @return the certificateMappingTables.
     */
    public List<String> getCertificateMappingTables() {
        return this.certificateMappingTables;
    }

    /**
     * Setter method of the <code>"certificateMappingTables"</code> class attribute.
     * 
     * @param CertificateMappingTables the certificateMappingTables to set.
     */
    public void setCertificateMappingTables(List<String> certificateMappingTables) {
        this.certificateMappingTables = certificateMappingTables;
    }

    /**
     * Getter method of the <code>"benefitMappingTables"</code> class attribute.
     * 
     * @return the benefitMappingTables.
     */
    public List<String> getBenefitMappingTables() {
        return this.benefitMappingTables;
    }

    /**
     * Setter method of the <code>"benefitMappingTables"</code> class attribute.
     * 
     * @param BenefitMappingTables the benefitMappingTables to set.
     */
    public void setBenefitMappingTables(List<String> benefitMappingTables) {
        this.benefitMappingTables = benefitMappingTables;
    }

    /**
     * Getter method of the <code>"emailTo"</code> class attribute.
     * 
     * @return the emailTo.
     */
    public String getEmailTo() {
        return this.emailTo;
    }

    /**
     * Setter method of the <code>"emailTo"</code> class attribute.
     * 
     * @param EmailTo the emailTo to set.
     */
    public void setEmailTo(String aEmailTo) {
        this.emailTo = aEmailTo;
    }

    /**
     * Getter method of the <code>"emailCc"</code> class attribute.
     * 
     * @return the emailCc.
     */
    public String getEmailCc() {
        return this.emailCc;
    }

    /**
     * Setter method of the <code>"emailCc"</code> class attribute.
     * 
     * @param EmailCc the emailCc to set.
     */
    public void setEmailCc(String aEmailCc) {
        this.emailCc = aEmailCc;
    }

    /**
     * Getter method of the <code>"emailSubject"</code> class attribute.
     * 
     * @return the emailSubject.
     */
    public String getEmailSubject() {
        return emailSubject;
    }

    /**
     * Setter method of the <code>"emailSubject"</code> class attribute.
     * 
     * @param EmailSubject the emailSubject to set.
     */
    public void setEmailSubject(String aEmailSubject) {
        this.emailSubject = aEmailSubject;
    }

    /**
     * Getter method of the <code>"emailEnabled"</code> class attribute.
     * 
     * @return the emailEnabled.
     */
    public boolean isEmailEnabled() {
        return emailEnabled;
    }

    /**
     * Setter method of the <code>"emailEnabled"</code> class attribute.
     * 
     * @param EmailEnabled the emailEnabled to set.
     */
    public void setEmailEnabled(boolean aEmailEnabled) {
        this.emailEnabled = aEmailEnabled;
    }

    /**
     * Getter method of the <code>"emailFrom"</code> class attribute.
     * 
     * @return the emailFrom.
     */
    public String getEmailFrom() {
        return emailFrom;
    }

    /**
     * Setter method of the <code>"emailFrom"</code> class attribute.
     * 
     * @param EmailFrom the emailFrom to set.
     */
    public void setEmailFrom(String aEmailFrom) {
        this.emailFrom = aEmailFrom;
    }

    /**
     * Getter method of the <code>"deductibleMappingTables"</code> class attribute.
     * 
     * @return the deductibleMappingTables.
     */
    public List<String> getDeductibleMappingTables() {
        return deductibleMappingTables;
    }

    /**
     * Setter method of the <code>"deductibleMappingTables"</code> class attribute.
     * 
     * @param deductibleMappingTables the deductibleMappingTables to set.
     */
    public void setDeductibleMappingTables(List<String> deductibleMappingTables) {
        this.deductibleMappingTables = deductibleMappingTables;
    }
}
