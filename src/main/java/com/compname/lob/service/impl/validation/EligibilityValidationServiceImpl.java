package com.compname.lob.service.impl.validation;

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Qualifier;


import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.compname.lob.domain.config.AbstractProperties;
import com.compname.lob.domain.config.WorkOrderSheetNames;
import com.compname.lob.domain.workorder.AbstractWorkOrder;
import com.compname.lob.domain.workorder.EligibilityWorkOrder;
import com.compname.lob.domain.workorder.WorkOrderRequestDate;
import com.compname.lob.service.impl.dao.eligibility.EligibilityDao;
import com.compname.lob.utils.ValidationUtils;

/**
 * EligibilityValidationServiceImpl. All those validation implements the rules defined in
 * "Load the Eligibility/Claim Work Order to the Work Order Tables" from Eligibility functional specification
 * 
 * @author vegirl1
 * @since Aug 17, 2015
 * @version $Revision$
 */
public class EligibilityValidationServiceImpl extends WorkOrderValidationServiceImpl<EligibilityWorkOrder> {

    private static final String ERR_MSG_INITIAL_GOLIVE_DATES       = "The Eligibility Initial Request date {0} should be less than the Go Live date {1}.";
    private static final String ERR_MSG_INITIAL_DELTA1_DATES       = "The Eligibility Initial Request date {0} should be less than the Delta Request date {1}.";
    private static final String ERR_MSG_DELTA1_DELTA2_DATES        = "The Eligibility Delta Request date #1 {0} should be less than the Delta Request date #2 {1}.";
    private static final String ERR_MSG_RERUN_INDICATOR            = "Re-Run initial Request or Re-Run Delta Request has invalid format.";
    private static final String ERR_MSG_WRONG_OLD_NEW_CERT_NR      = "Invalid Old-New Certificate Number (No Formula in certificate number cell).";
    private static final String ERR_MSG_WRONG_CERT_CONV_TAB        = "Invalid Certificate Conversion Tab, old-new certificates mapping not provided.";

    private static final String ERR_MSG_NO_AUTOGENERATION          = "Auto generation option is not supported.";

    private static final String ERR_MSG_WRONG_CERT_INDICATOR       = "'Cert conversions required' and 'Are the certs auto generated' valid values are 'Yes' or 'No'";
    private static final String ERR_MSG_WRONG_SAG_FORMAT           = "Invalid SAG Format for the Group.";
    private static final String ERR_MSG_WRONG_TRNS_FORMAT_VALUES   = "Vload Format, Gipsy Format and SAG Format.  Only one format must be set to 'Yes'"
                                                                           + " and all other format must be set to 'No'.";
    private static final String ERR_MSG_WRONG_TRANSFORM_FORMAT     = "For Vload, Gipsy and SAG Format valid values are 'Yes' or 'No'";
    private static final String ERR_MSG_WRONG_PLAN_GROUP           = "The SLAC Group number {0} from the WorkOrder file name does NOT match to the SLAC Group provided in Plan Mapping.";
    private static final String ERR_MSG_WRONG_DEDUCTIBLE_GROUP     = "One of the Groups provided in the Per Script Deductible table, does not match with : SLAC Group number {0} from the WorkOrder.";
    private static final String ERR_MSG_WRONG_DEDUCTIBLE_INDICATOR = "Valid values for Indicator in Per Script Deductible table are 'Yes' or 'No'.";

    private static final String ERR_MSG_WRONG_VLOAD_PLAN           = "Vload Format = 'Yes' and Manuconnect conversion/ Core Plan (Health/Dental/Cost Plus) is not provided.";
    private static final String ERR_MSG_WRONG_GIPSY_PLAN           = "Gipsy Format = 'Yes' and Gipsy conversion/ Core Plan (Health/Dental/Cost Plus) is not provided.";
    private static final String ERR_MSG_WRONG_SAG_PLAN             = "When SAG Format = 'Yes' only one Core plan should be provided.";
    private static final String ERR_MSG_WRONG_PLAN_MAPPING         = "A group can be mapped to only one Plan.";
    private static final String ERR_MSG_WRONG_PLAN_MAPPING_VALUES  = "Invalid Plan Mapping values. (No empty values, no duplicates and fields length must be respected)";

    private static final String ERR_MSG_RERUN_ELIG_CLAIM_YES       = "When have an Initial ReRun = 'Yes' for Eligibility should have the Initial ReRun = 'Yes' for Claims as well.";

    private static final String MCNT_LABEL                         = "MCNT";
    private static final String GIPSY_LABEL                        = "GIPSY";

    /**
     * Class constructor.
     * 
     */
    public EligibilityValidationServiceImpl(@Qualifier("eligibilityDao") EligibilityDao eligibilityDao) {
        super(eligibilityDao);
    }

    /**
     * {@inheritDoc}
     * 
     * @see com.compname.lob.service.WorkOrderValidationService#validateWorkOrder(com.compname.lob.domain.workorder.AbstractWorkOrder)
     */
    @Override
    public void validateWorkOrder(EligibilityWorkOrder workOrder, List<ErrorInfo> errorInfos) {

        validateSlacGroup(workOrder.getSlacGroupNumber(), errorInfos);
        validateSlacGroupWithPlanMapping(workOrder, errorInfos);
        validateDateFormat(getDatesToValidateFormat(workOrder), errorInfos);
        validateMandatoryDates(getMandatoryDates(workOrder), WorkOrderValidationServiceImpl.ERR_MSG_WO_MANDATORY_DATES, errorInfos);
        validateRequestDates(workOrder.getInitialRequestRunDate().getRequestRunDate(), workOrder.getConversionDate(),
                ERR_MSG_INITIAL_GOLIVE_DATES, errorInfos);
        validateRequestDates(workOrder.getInitialRequestRunDate().getRequestRunDate(), workOrder.getDeltaFirstRequestRunDate()
                .getRequestRunDate(), ERR_MSG_INITIAL_DELTA1_DATES, errorInfos);
        validateDeltaRequestDates(workOrder.getDeltaRequestRunDates(), ERR_MSG_DELTA1_DELTA2_DATES, errorInfos);
        validateYesNoIndicatorValues(Lists.newArrayList(workOrder.getReRunInitialRequest(), workOrder.getReRunDeltaRequest()),
                ERR_MSG_RERUN_INDICATOR, errorInfos);

        List<String> loadTypes = Lists.newArrayList(workOrder.getVLOADFormat(), workOrder.getSAGFormat(),
                workOrder.getGipsyFormat());
        validateLoadTypeFormat(loadTypes, errorInfos);
        validateLoadTypeValues(loadTypes, errorInfos);
        validateLoadTypePlan(workOrder, errorInfos);

        validateSagLoadTypeGroup(workOrder.getSlacGroupNumber(), workOrder.getSAGFormat(), errorInfos);

        // retrieve existing WorkOrders from DB(if exists) and set them to the current Eligibility
        EligibilityWorkOrder existingEligWorkOrder = new EligibilityWorkOrder();
        retrieveExistingWorkOrders(workOrder.getSlacGroupNumber(), EligibilityWorkOrder.RECORD_TYPE_ELIG, existingEligWorkOrder);
        setExistingWorkOrderToCurrent(workOrder, existingEligWorkOrder);
        //
        validateRerunIndicatorYesValues(workOrder, errorInfos);
        validateRerunIndicatorNoValues(workOrder, errorInfos);

        validateRerunIndicatorYesEligClaim(workOrder, errorInfos);

        // Certificates tab
        validateCertificateIndicators(Lists.newArrayList(workOrder.getCertConversionRequired(), workOrder.getCertAutoGenerated()),
                errorInfos);

        validateCertificateConversionRequired(workOrder, errorInfos);
        validateCertificateMapping(workOrder, errorInfos);
        validateCertificateReMapping(workOrder, errorInfos);

        // PerScriptDeductible tab
        validatePerScriptDeductibleGroupNumber(workOrder, errorInfos);
        validatePerScriptDeductibleClasses(workOrder, errorInfos);
        validatePerScriptDeductibleIndicators(workOrder, errorInfos);
    }

    protected void validateSlacGroupWithPlanMapping(EligibilityWorkOrder workOrder, List<ErrorInfo> errorInfos) {
        Map<String, List<List<String>>> planMaps = workOrder.getPlanMaps();
        Set<String> planGroups = Sets.newHashSet();

        for (String key : planMaps.keySet()) {
            for (List<String> values : planMaps.get(key)) {
                if (StringUtils.isNotEmpty(values.get(0))) {
                    planGroups.add(values.get(0));
                }
            }
        }

        if (!(planGroups.size() == 1 && planGroups.contains(workOrder.getSlacGroupNumber()))) {
            errorInfos.add(ErrorInfo.createWith("EligibilityValidationRules.validateSlacGroupWithPlanMapping()",
                    AbstractProperties.FAMILY_GROUP_BENEFIT, ErrorInfo.ERROR,
                    MessageFormat.format(ERR_MSG_WRONG_PLAN_GROUP, workOrder.getSlacGroupNumber())));
        }

    }

    protected void validateDeltaRequestDates(List<WorkOrderRequestDate> deltaRequestRunDates, String errorMessage,
            List<ErrorInfo> errorInfos) {
        Iterator<WorkOrderRequestDate> iter = deltaRequestRunDates.iterator();
        WorkOrderRequestDate runDate = iter.next();
        while (iter.hasNext()) {
            WorkOrderRequestDate nextRunDate = iter.next();
            if (StringUtils.isNotEmpty(nextRunDate.getRequestRunDate())) {
                validateRequestDates(runDate.getRequestRunDate(), nextRunDate.getRequestRunDate(), errorMessage, errorInfos);
            }
            runDate = nextRunDate;
        }
    }

    protected void validateLoadTypeFormat(List<String> loadTypes, List<ErrorInfo> errorInfos) {
        // Yes or No
        if (Iterables.size(filterYesNoValues(loadTypes)) != loadTypes.size()) {
            errorInfos.add(ErrorInfo.createWith("EligibilityValidationRules.validateLoadTypeFormat()",
                    AbstractProperties.FAMILY_GROUP_BENEFIT, ErrorInfo.ERROR, ERR_MSG_WRONG_TRANSFORM_FORMAT));
        }
    }

    protected void validateLoadTypeValues(List<String> loadTypes, List<ErrorInfo> errorInfos) {
        // Only 1 from list must be Yes, all others to No
        Iterable<String> validDates = Iterables.filter(loadTypes, new Predicate<String>() {
            public boolean apply(String input) {
                return (AbstractWorkOrder.YES_FLAGS_LIST.contains(input));
            }
        });

        if (Iterables.size(validDates) != 1) {
            errorInfos.add(ErrorInfo.createWith("EligibilityValidationRules.validateLoadTypeValues()",
                    AbstractProperties.FAMILY_GROUP_BENEFIT, ErrorInfo.ERROR, ERR_MSG_WRONG_TRNS_FORMAT_VALUES));
        }
    }

    protected void validateLoadTypePlan(EligibilityWorkOrder workOrder, List<ErrorInfo> errorInfos) {

        String errMsg = StringUtils.EMPTY;

        boolean hasMunuconect = ValidationUtils.hasPlanMapping(workOrder, MCNT_LABEL);
        boolean hasGipsy = ValidationUtils.hasPlanMapping(workOrder, GIPSY_LABEL);

        if (hasMunuconect && hasGipsy)
            errMsg = ERR_MSG_WRONG_PLAN_MAPPING;
        else if (!hasMunuconect && AbstractWorkOrder.YES_FLAGS_LIST.contains(workOrder.getVLOADFormat())) {
            errMsg = ERR_MSG_WRONG_VLOAD_PLAN;
        } else if (!hasGipsy && AbstractWorkOrder.YES_FLAGS_LIST.contains(workOrder.getGipsyFormat())) {
            errMsg = ERR_MSG_WRONG_GIPSY_PLAN;
        } else if (AbstractWorkOrder.YES_FLAGS_LIST.contains(workOrder.getSAGFormat()) && !hasMunuconect && !hasGipsy) {
            errMsg = ERR_MSG_WRONG_SAG_PLAN;
        }

        if (StringUtils.isNotEmpty(errMsg)) {
            errorInfos.add(ErrorInfo.createWith("EligibilityValidationRules.validateLoadTypePlan()",
                    AbstractProperties.FAMILY_GROUP_BENEFIT, ErrorInfo.ERROR, errMsg));
        }

        if (ValidationUtils.isNotValidPlanMapValues(workOrder.getPlanMaps())) {
            errorInfos.add(ErrorInfo.createWith("EligibilityValidationRules.validateLoadTypePlan()",
                    AbstractProperties.FAMILY_GROUP_BENEFIT, ErrorInfo.ERROR, ERR_MSG_WRONG_PLAN_MAPPING_VALUES));
        }
    }

    protected void validateSagLoadTypeGroup(String slacGroup, String sagLoadTypeValue, List<ErrorInfo> errorInfos) {
        // If Yes then Group is defined as SAG in Compass If No then Group is defined as NON SAG
        boolean isSagGroup = eligibilityDao.isSAGGroup(slacGroup);

        if ((AbstractWorkOrder.YES_FLAGS_LIST.contains(sagLoadTypeValue) && !isSagGroup)
                || AbstractWorkOrder.NO_FLAGS_LIST.contains(sagLoadTypeValue) && isSagGroup) {
            errorInfos.add(ErrorInfo.createWith("EligibilityValidationRules.validateSagLoadTypeGroup()",
                    AbstractProperties.FAMILY_GROUP_BENEFIT, ErrorInfo.ERROR, ERR_MSG_WRONG_SAG_FORMAT));
        }
    }

    // validate certificate tab
    protected void validateCertificateIndicators(List<String> certificateIndicators, List<ErrorInfo> errorInfos) {
        // Yes or No
        if (Iterables.size(filterYesNoValues(certificateIndicators)) != certificateIndicators.size()) {
            errorInfos.add(ErrorInfo.createWith("EligibilityValidationRules.validateCertificateIndicators()",
                    AbstractProperties.FAMILY_GROUP_BENEFIT, ErrorInfo.ERROR, ERR_MSG_WRONG_CERT_INDICATOR));
        }
    }

    protected void validateCertificateConversionRequired(EligibilityWorkOrder workOrder, List<ErrorInfo> errorInfos) {

        if (AbstractWorkOrder.YES_FLAGS_LIST.contains(workOrder.getCertConversionRequired())
                && AbstractWorkOrder.YES_FLAGS_LIST.contains(workOrder.getCertAutoGenerated())) {
            errorInfos.add(ErrorInfo.createWith("EligibilityValidationRules.validateCertificateConversionRequired()",
                    AbstractProperties.FAMILY_GROUP_BENEFIT, ErrorInfo.ERROR, ERR_MSG_NO_AUTOGENERATION));
        }

        if (AbstractWorkOrder.YES_FLAGS_LIST.contains(workOrder.getCertConversionRequired())
                && ValidationUtils.isMapListEmpty(workOrder.getCertificateMaps())) {
            errorInfos.add(ErrorInfo.createWith("EligibilityValidationRules.validateCertificateConversionRequired()",
                    AbstractProperties.FAMILY_GROUP_BENEFIT, ErrorInfo.ERROR, ERR_MSG_WRONG_CERT_CONV_TAB));
        }

    }

    protected void validateCertificateMapping(EligibilityWorkOrder workOrder, List<ErrorInfo> errorInfos) {
        if (ValidationUtils.isNotValidOldNewCertificateNumber(workOrder)) {
            errorInfos.add(ErrorInfo.createWith("EligibilityValidationRules.validateCertificateMappingExists()",
                    AbstractProperties.FAMILY_GROUP_BENEFIT, ErrorInfo.ERROR, ERR_MSG_WRONG_OLD_NEW_CERT_NR));
        }

    }

    protected void validateCertificateReMapping(EligibilityWorkOrder workOrder, List<ErrorInfo> errorInfos) {

        if (workOrder.getExistingWorkOrderId() == null) {
            return;
        }

        String certMapping = eligibilityDao.getCertMappingAsList(workOrder.getExistingWorkOrderId());

        if (StringUtils.isEmpty(certMapping)) {
            return;
        }

        // get Existing DB CertMapping
        List<String> oldCertList = Arrays.asList(certMapping.split(AbstractProperties.COMMA));

        // get Current WO CertMapping
        List<List<String>> certMaps = Lists.newArrayList();
        for (String key : workOrder.getCertificateMaps().keySet()) {
            certMaps = workOrder.getCertificateMaps().get(key);
        }
        List<String> newCertList = ValidationUtils.getListElementsByIndex(certMaps, 0, 1, AbstractProperties.DASH,
                AbstractProperties.CERTIFICATE_LENGTH);

        // Subtract one from other in both directions
        List<String> subtractNew = Lists.newArrayList(newCertList);
        subtractNew.removeAll(oldCertList);

        List<String> subtractOld = Lists.newArrayList(oldCertList);
        subtractOld.removeAll(newCertList);

        Collections.sort(subtractNew);
        Collections.sort(subtractOld);

        List<String> remappedCerts = Lists.newArrayList();
        List<String> oldCerts = Lists.newArrayList(subtractOld);
        List<String> newCerts = Lists.newArrayList(subtractNew);

        // build a re mapped Certificate(s)
        for (String newCert : subtractNew) {
            final String certSL = StringUtils.substringBefore(newCert, AbstractProperties.DASH);
            final String certML = StringUtils.substringAfter(newCert, AbstractProperties.DASH);
            Optional<String> remappedCert = Iterables.tryFind(subtractOld, new Predicate<String>() {
                public boolean apply(String input) {
                    return StringUtils.startsWith(input, certSL) || StringUtils.endsWith(input, certML);
                }
            });

            if (remappedCert.isPresent()) {
                remappedCerts.add(remappedCert.get());
                newCerts.remove(newCert);
                oldCerts.remove(remappedCert.get());

            }
        }

        if (CollectionUtils.isNotEmpty(oldCerts)) {
            errorInfos.add(ErrorInfo.createWith("EligibilityValidationRules.validateCertificateReMapping()",
                    AbstractProperties.FAMILY_GROUP_BENEFIT, ErrorInfo.WARNING,
                    "Certs received in previous run but missing in the current run: " + oldCerts.toString()));

        }
        if (CollectionUtils.isNotEmpty(newCerts)) {
            errorInfos.add(ErrorInfo.createWith("EligibilityValidationRules.validateCertificateReMapping()",
                    AbstractProperties.FAMILY_GROUP_BENEFIT, ErrorInfo.WARNING,
                    "Certs received in current run but missing in the previous run: " + newCerts.toString()));

        }

        if (CollectionUtils.isNotEmpty(remappedCerts)) {
            errorInfos.add(ErrorInfo.createWith("EligibilityValidationRules.validateCertificateReMapping()",
                    AbstractProperties.FAMILY_GROUP_BENEFIT, ErrorInfo.WARNING,
                    "Certs that have changed from one run to the next: " + remappedCerts.toString()));
        }
    }

    private List<String> getDatesToValidateFormat(EligibilityWorkOrder workOrder) {
        List<String> dates = Lists.newArrayList();
        dates.addAll(getMandatoryDates(workOrder));
        addStringDateToList(workOrder.getDataIntegrityReport(), dates);
        addStringDateToList(workOrder.getActiveClassAndDivExcpRprtRunDate(), dates);
        addStringDateToList(workOrder.getPharmaCareEnrollDate(), dates);
        addStringDateToList(workOrder.getWaiverOfPremiumRunDate(), dates);
        addStringDateToList(workOrder.getGipsyEmailReportRunDate(), dates);
        addStringDateToList(workOrder.getFutureTransactionReportRunDate(), dates);
        addStringDateToList(workOrder.getCertifcateMappingReportRunDate(), dates);
        addStringDateToList(workOrder.getPharmacareRptRunDate(), dates);
        addStringDateToList(workOrder.getTerminationDate(), dates);
        // skip first Delta Date that is mandatory
        for (int i = 1; i < workOrder.getDeltaRequestRunDates().size(); i++) {
            addStringDateToList(workOrder.getDeltaRequestRunDates().get(i).getRequestRunDate(), dates);
        }

        return dates;
    }

    private List<String> getMandatoryDates(EligibilityWorkOrder workOrder) {
        List<String> dates = Lists.newArrayList();

        dates.add(workOrder.getConversionDate());
        dates.add(workOrder.getInitialRequestRunDate().getRequestRunDate());
        dates.add(workOrder.getDeltaFirstRequestRunDate().getRequestRunDate());
        return dates;
    }

    protected void validateRerunIndicatorYesEligClaim(EligibilityWorkOrder workOrder, List<ErrorInfo> errorInfos) {
        if (AbstractWorkOrder.YES_FLAGS_LIST.contains(workOrder.getReRunInitialRequest())
                && AbstractWorkOrder.NO_FLAGS_LIST.contains(workOrder.getClaimsWorkOrder().getReRunInitialRequest())) {
            errorInfos.add(ErrorInfo.createWith("WorkOrderValidationServiceImpl.validateRerunIndicatorYesEligClaim()",
                    AbstractProperties.FAMILY_GROUP_BENEFIT, ErrorInfo.ERROR, ERR_MSG_RERUN_ELIG_CLAIM_YES));
        }
    }

    protected void validatePerScriptDeductibleGroupNumber(EligibilityWorkOrder workOrder, List<ErrorInfo> errorInfos) {
        Map<String, List<List<String>>> deductibleMaps = workOrder.getDeductibleMaps();
        Set<String> deductibleGroups = Sets.newHashSet();

        for (String key : deductibleMaps.keySet()) {
            for (List<String> values : deductibleMaps.get(key)) {
                deductibleGroups.add(values.get(0));
            }
        }

        if (CollectionUtils.isEmpty(deductibleGroups)) {
            // PerScriptDeductible table has no data
            return;
        } else if (!(deductibleGroups.size() == 1 && deductibleGroups.contains(workOrder.getSlacGroupNumber()))) {
            errorInfos.add(ErrorInfo.createWith("EligibilityValidationRules.validatePerScriptDeductibleGroupNumber()",
                    AbstractProperties.FAMILY_GROUP_BENEFIT, ErrorInfo.ERROR,
                    MessageFormat.format(ERR_MSG_WRONG_DEDUCTIBLE_GROUP, workOrder.getSlacGroupNumber())));
        }

    }

    protected void validatePerScriptDeductibleClasses(EligibilityWorkOrder workOrder, List<ErrorInfo> errorInfos) {

        String dbGroupClasses = eligibilityDao.getDeductibleClassesAsList(workOrder.getSlacGroupNumber());

        if (StringUtils.isEmpty(dbGroupClasses)) {
            return;
        }

        // get Existing DB Classes
        List<String> dbGroupClassesLists = Arrays.asList(dbGroupClasses.split(AbstractProperties.COMMA));

        // get Current WO deductible
        List<List<String>> deductibles = Lists.newArrayList();
        for (String key : workOrder.getDeductibleMaps().keySet()) {
            deductibles = workOrder.getDeductibleMaps().get(key);
        }
        List<String> woGroupClasses = ValidationUtils.getListElementsByIndex(deductibles, 1, 1, StringUtils.EMPTY,
                AbstractProperties.SLAC_CLASS_LENGTH);

        // Subtract one from other in both directions
        List<String> subtractDiff = Lists.newArrayList(woGroupClasses);
        subtractDiff.removeAll(dbGroupClassesLists);

        if (CollectionUtils.isNotEmpty(subtractDiff)) {
            errorInfos.add(ErrorInfo.createWith("EligibilityValidationRules.validatePerScriptDeductibleClasses()",
                    AbstractProperties.FAMILY_GROUP_BENEFIT, ErrorInfo.WARNING, "The following Classes provided in "
                            + WorkOrderSheetNames.per_script_deductible.getSheetName()
                            + " table does not exist in Compass system: " + subtractDiff.toString()));
        }

        if (CollectionUtils.isNotEmpty(woGroupClasses)) {

            Iterable<String> clasLengthLists = Iterables.filter(woGroupClasses, new Predicate<String>() {
                public boolean apply(String input) {
                    return StringUtils.isNotEmpty(input) && input.length() <= AbstractProperties.SLAC_CLASS_LENGTH;
                }
            });

            if (woGroupClasses.size() != Iterables.size(clasLengthLists)) {
                errorInfos.add(ErrorInfo.createWith("EligibilityValidationRules.validatePerScriptDeductibleClasses()",
                        AbstractProperties.FAMILY_GROUP_BENEFIT, ErrorInfo.ERROR, "The Classes provided in "
                                + WorkOrderSheetNames.per_script_deductible.getSheetName() + " table have wrong values."));
            }

        }

    }

    protected void validatePerScriptDeductibleIndicators(EligibilityWorkOrder workOrder, List<ErrorInfo> errorInfos) {
        // get Current WO deductible
        List<List<String>> deductibles = Lists.newArrayList();
        for (String key : workOrder.getDeductibleMaps().keySet()) {
            deductibles = workOrder.getDeductibleMaps().get(key);
        }
        List<String> woDeductibleIndicators = ValidationUtils.getListElementsByIndex(deductibles, 2, 2, StringUtils.EMPTY, 0);

        validateYesNoIndicatorValues(woDeductibleIndicators, ERR_MSG_WRONG_DEDUCTIBLE_INDICATOR, errorInfos);

    }

}
