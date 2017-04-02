package com.compname.lob.service.impl.validation;

import java.util.List;
import java.util.Map;
import java.util.Set;

import junit.framework.Assert;

import org.apache.commons.lang.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;


import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.compname.lob.domain.report.DbParameter;
import com.compname.lob.domain.workorder.EligibilityWorkOrder;
import com.compname.lob.domain.workorder.WorkOrderRequestDate;
import com.compname.lob.service.impl.dao.datasource.StorProcRefCursorDao;
import com.compname.lob.service.impl.dao.eligibility.EligibilityDao;
import com.compname.lob.service.mock.MockDataBuilder;
import com.compname.lob.utils.ValidationUtils;

/**
 * EligibilityValidationRulesTest
 * 
 * @author vegirl1
 * @since Aug 18, 2015
 * @version $Revision$
 */
@RunWith(MockitoJUnitRunner.class)
public class EligibilityValidationServiceTest {

    @Mock
    private EligibilityDao                   eligibilityDao;

    @Mock
    StorProcRefCursorDao                     storProcRefCursorDao;

    private EligibilityValidationServiceImpl eligibilityValidationRules;

    private EligibilityWorkOrder             eligibilityWorkOrder;
    List<ErrorInfo>                          errorInfos;

    @Before
    public void setUp() throws Exception {

        eligibilityWorkOrder = MockDataBuilder.mockEligibilityWorkOrder();
        eligibilityValidationRules = new EligibilityValidationServiceImpl(eligibilityDao);

        List<DbParameter> dummyParamList = Matchers.any();
        Mockito.when(storProcRefCursorDao.getRefCursorValues(Mockito.anyString(), dummyParamList)).thenReturn(
                MockDataBuilder.getExistingWorkOrderDataSourceValues());

        Mockito.when(eligibilityDao.isValidSlacGroupNumber(eligibilityWorkOrder.getSlacGroupNumber())).thenReturn(true);
        Mockito.when(eligibilityDao.isValidSlacGroupNumber("32048")).thenReturn(true);
        Mockito.when(eligibilityDao.isValidSlacGroupNumber("-99999")).thenReturn(false);

        Mockito.when(eligibilityDao.isSAGGroup("32048")).thenReturn(true, true);
        Mockito.when(eligibilityDao.isSAGGroup("-99999")).thenReturn(false);

        Mockito.when(eligibilityDao.getDeductibleClassesAsList(eligibilityWorkOrder.getSlacGroupNumber())).thenReturn(
                "100,101,102,103,104");

        errorInfos = Lists.newArrayList();
    }

    @Test
    public void testValidateSlacGroup() throws ServiceException {
        eligibilityValidationRules.validateSlacGroup("32048", errorInfos);
        Assert.assertTrue(errorInfos.size() == 0);

        eligibilityValidationRules.validateSlacGroup("-99999", errorInfos);
        Assert.assertTrue(errorInfos.size() == 1);
    }

    @Test
    public void testValidateDateFormat() throws ServiceException {
        eligibilityValidationRules.validateDateFormat(Lists.newArrayList("20150707", "20150808", "20150909"), errorInfos);
        Assert.assertTrue(errorInfos.size() == 0);

        eligibilityValidationRules.validateDateFormat(Lists.newArrayList("20150707", "20150808", "20159999"), errorInfos);
        Assert.assertTrue(errorInfos.size() == 1);
    }

    @Test
    public void testValidateMandatoryDates() throws ServiceException {
        eligibilityValidationRules.validateMandatoryDates(Lists.newArrayList("20150707", "20150808", "20150909"), "ErrMsg",
                errorInfos);
        Assert.assertTrue(errorInfos.size() == 0);

        eligibilityValidationRules.validateMandatoryDates(Lists.newArrayList("20150707", "", null), "ErrMsg", errorInfos);
        Assert.assertTrue(errorInfos.size() == 1);
    }

    @Test
    public void testValidateRequestDates() throws ServiceException {
        String ERR_MSG_INITIAL_GOLIVE_DATES = "The Eligibility Initial Request date {0} of the work order can’t be greater than the Go Live date {1}.";

        eligibilityValidationRules.validateRequestDates("20150707", "20150808", ERR_MSG_INITIAL_GOLIVE_DATES, errorInfos);
        Assert.assertTrue(errorInfos.size() == 0);

        eligibilityValidationRules.validateRequestDates("20151212", "20150808", ERR_MSG_INITIAL_GOLIVE_DATES, errorInfos);
        Assert.assertTrue(errorInfos.size() == 1);

        eligibilityValidationRules.validateRequestDates("1111", "20150808", ERR_MSG_INITIAL_GOLIVE_DATES, errorInfos);
        Assert.assertTrue(errorInfos.size() == 2);

        eligibilityValidationRules.validateRequestDates("20150808", null, ERR_MSG_INITIAL_GOLIVE_DATES, errorInfos);
        Assert.assertTrue(errorInfos.size() == 3);

        eligibilityValidationRules.validateRequestDates("20150808", "20150808", ERR_MSG_INITIAL_GOLIVE_DATES, errorInfos);
        Assert.assertTrue(errorInfos.size() == 4);
    }

    @Test
    public void testValidateRerunIndicator() throws ServiceException {
        eligibilityValidationRules.validateYesNoIndicatorValues(Lists.newArrayList("yes", "No"), "ErrMsg", errorInfos);
        Assert.assertTrue(errorInfos.size() == 0);

        eligibilityValidationRules.validateYesNoIndicatorValues(Lists.newArrayList("No", "N"), "ErrMsg", errorInfos);
        Assert.assertTrue(errorInfos.size() == 0);

        eligibilityValidationRules.validateYesNoIndicatorValues(Lists.newArrayList("Y", "Ok"), "ErrMsg", errorInfos);
        Assert.assertTrue(errorInfos.size() == 1);
    }

    @Test
    public void testValidateLoadTypeFormat() throws ServiceException {
        List<String> loadTypes = Lists.newArrayList("yes", "No", "NO");

        eligibilityValidationRules.validateLoadTypeFormat(loadTypes, errorInfos);
        Assert.assertTrue(errorInfos.size() == 0);

        loadTypes.set(0, "On");
        eligibilityValidationRules.validateLoadTypeFormat(loadTypes, errorInfos);
        Assert.assertTrue(errorInfos.size() == 1);
    }

    @Test
    public void testValidateLoadTypeValues() throws ServiceException {
        List<String> loadTypes = Lists.newArrayList("YES", "NO", "NO");

        eligibilityValidationRules.validateLoadTypeValues(loadTypes, errorInfos);
        Assert.assertTrue(errorInfos.size() == 0);

        loadTypes.set(1, "Yes");
        eligibilityValidationRules.validateLoadTypeValues(loadTypes, errorInfos);
        Assert.assertTrue(errorInfos.size() == 1);

        loadTypes = Lists.newArrayList("No", "No", "No");
        eligibilityValidationRules.validateLoadTypeValues(loadTypes, errorInfos);
        Assert.assertTrue(errorInfos.size() == 2);
    }

    @Test
    public void testValidateSagLoadTypeGroup() throws ServiceException {
        eligibilityValidationRules.validateSagLoadTypeGroup("32048", "Yes", errorInfos);
        Assert.assertTrue(errorInfos.size() == 0);

        eligibilityValidationRules.validateSagLoadTypeGroup("32048", "No", errorInfos);
        Assert.assertTrue(errorInfos.size() == 1);

        eligibilityValidationRules.validateSagLoadTypeGroup("-99999", "Yes", errorInfos);
        Assert.assertTrue(errorInfos.size() == 2);
    }

    @Test
    public void testValidateCertificateIndicators() throws ServiceException {
        List<String> certificateIndicators = Lists.newArrayList("YES", "NO");

        eligibilityValidationRules.validateCertificateIndicators(certificateIndicators, errorInfos);
        Assert.assertTrue(errorInfos.size() == 0);

        certificateIndicators.set(0, "On");
        eligibilityValidationRules.validateCertificateIndicators(certificateIndicators, errorInfos);
        Assert.assertTrue(errorInfos.size() == 1);
    }

    @Test
    public void testValidateCertificateConversionRequired() throws ServiceException {

        eligibilityWorkOrder.setCertAutoGenerated("No");

        eligibilityWorkOrder.setStartingCertNumber(StringUtils.EMPTY);
        eligibilityWorkOrder.setCertificateMaps(MockDataBuilder.getCertificateMaps());
        eligibilityValidationRules.validateCertificateConversionRequired(eligibilityWorkOrder, errorInfos);
        Assert.assertTrue(errorInfos.size() == 0);

        eligibilityWorkOrder.setCertAutoGenerated("Yes");
        eligibilityValidationRules.validateCertificateConversionRequired(eligibilityWorkOrder, errorInfos);
        Assert.assertTrue(errorInfos.size() == 1);

        eligibilityWorkOrder.setCertAutoGenerated("No");
        eligibilityWorkOrder.setStartingCertNumber(StringUtils.EMPTY);
        eligibilityWorkOrder.setCertificateMaps(null);
        eligibilityValidationRules.validateCertificateConversionRequired(eligibilityWorkOrder, errorInfos);
        Assert.assertTrue(errorInfos.size() == 2);

        //
        Map<String, List<List<String>>> certificateMaps = Maps.newLinkedHashMap();
        List<List<String>> certificateLists = Lists.newLinkedList();
        certificateMaps.put("table.CERT_MAPP", certificateLists);
        eligibilityWorkOrder.setStartingCertNumber(StringUtils.EMPTY);
        eligibilityWorkOrder.setCertificateMaps(certificateMaps);
        eligibilityValidationRules.validateCertificateConversionRequired(eligibilityWorkOrder, errorInfos);
        Assert.assertTrue(errorInfos.size() == 3);

    }

    @Test
    public void testValidateCertificateConversionWithMaps() throws ServiceException {

        eligibilityWorkOrder.setCertConversionRequired("yes");
        eligibilityWorkOrder.setCertAutoGenerated("N");
        eligibilityWorkOrder.setStartingCertNumber(StringUtils.EMPTY);

        Map<String, List<List<String>>> certificateMaps = MockDataBuilder.getCertificateMaps();

        eligibilityWorkOrder.setCertificateMaps(certificateMaps);

        eligibilityValidationRules.validateCertificateMapping(eligibilityWorkOrder, errorInfos);
        Assert.assertTrue(errorInfos.size() == 0);

        certificateMaps.get("table.CERT_MAPP").add(Lists.newArrayList("12", "007"));
        eligibilityWorkOrder.setCertificateMaps(certificateMaps);
        eligibilityValidationRules.validateCertificateMapping(eligibilityWorkOrder, errorInfos);
        Assert.assertTrue(errorInfos.size() == 0);

        certificateMaps.get("table.CERT_MAPP").add(5, Lists.newArrayList("0000009", "1"));
        eligibilityWorkOrder.setCertificateMaps(certificateMaps);
        eligibilityValidationRules.validateCertificateMapping(eligibilityWorkOrder, errorInfos);
        Assert.assertTrue(errorInfos.size() == 0);

        certificateMaps.get("table.CERT_MAPP").add(6, Lists.newArrayList("o1", "111222333"));
        eligibilityWorkOrder.setCertificateMaps(certificateMaps);
        eligibilityValidationRules.validateCertificateMapping(eligibilityWorkOrder, errorInfos);
        Assert.assertTrue(errorInfos.size() == 1);

        certificateMaps.get("table.CERT_MAPP").add(6, Lists.newArrayList("123456789", "111aaa333"));
        eligibilityWorkOrder.setCertificateMaps(certificateMaps);
        eligibilityValidationRules.validateCertificateMapping(eligibilityWorkOrder, errorInfos);
        Assert.assertTrue(errorInfos.size() == 2);

        certificateMaps.get("table.CERT_MAPP").add(6, Lists.newArrayList("123456789", null));
        eligibilityWorkOrder.setCertificateMaps(certificateMaps);
        eligibilityValidationRules.validateCertificateMapping(eligibilityWorkOrder, errorInfos);
        Assert.assertTrue(errorInfos.size() == 3);

        certificateMaps.get("table.CERT_MAPP").add(6, Lists.newArrayList(null, "123456789"));
        eligibilityWorkOrder.setCertificateMaps(certificateMaps);
        eligibilityValidationRules.validateCertificateMapping(eligibilityWorkOrder, errorInfos);
        Assert.assertTrue(errorInfos.size() == 4);

        // case for duplicates
        certificateMaps.get("table.CERT_MAPP").add(6, Lists.newArrayList("12", "007"));
        eligibilityWorkOrder.setCertificateMaps(certificateMaps);
        eligibilityValidationRules.validateCertificateMapping(eligibilityWorkOrder, errorInfos);
        Assert.assertTrue(errorInfos.size() == 5);

    }

    @Test
    public void testvalidateCertificateReMapping() throws ServiceException {

        eligibilityWorkOrder.setCertConversionRequired("yes");
        eligibilityWorkOrder.setCertAutoGenerated("N");
        eligibilityWorkOrder.setStartingCertNumber(StringUtils.EMPTY);

        Map<String, List<List<String>>> certificateMaps = MockDataBuilder.getCertificateMaps();

        eligibilityWorkOrder.setCertificateMaps(certificateMaps);
        eligibilityWorkOrder.setExistingWorkOrderId(1l);

        Mockito.when(eligibilityDao.getCertMappingAsList(1l)).thenReturn(
                "111222330-9998887700R,R333222331-999888771,111222332-999888772,111222337-999888777");

        eligibilityValidationRules.validateCertificateReMapping(eligibilityWorkOrder, errorInfos);

        Assert.assertTrue(errorInfos.size() == 2);
        Assert.assertTrue(errorInfos.get(0).getTypeCode().equals(ErrorInfo.WARNING));
    }

    @Test
    public void testValidateDeltaRequestDates() {
        List<WorkOrderRequestDate> deltaRequestRunDates = Lists.newLinkedList();
        WorkOrderRequestDate date1 = new WorkOrderRequestDate("20151212", WorkOrderRequestDate.DELTA);
        WorkOrderRequestDate date2 = new WorkOrderRequestDate("20151112", WorkOrderRequestDate.DELTA);
        WorkOrderRequestDate date3 = new WorkOrderRequestDate("20151012", WorkOrderRequestDate.DELTA);
        deltaRequestRunDates.add(date1);
        deltaRequestRunDates.add(date2);
        deltaRequestRunDates.add(date3);

        eligibilityValidationRules.validateDeltaRequestDates(deltaRequestRunDates, "Wrong Delta dates", errorInfos);
        Assert.assertTrue(errorInfos.size() == 2);
    }

    @Test
    public void testValidateLoadTypePlan() {

        eligibilityWorkOrder.setVLOADFormat("Yes");
        eligibilityWorkOrder.setGipsyFormat("No");
        eligibilityWorkOrder.setSAGFormat("No");

        eligibilityValidationRules.validateLoadTypePlan(eligibilityWorkOrder, errorInfos);
        Assert.assertTrue(errorInfos.size() == 0);

        List<List<String>> dummyPlans = MockDataBuilder.getPlanMapping();
        eligibilityWorkOrder.getPlanMaps().put("table.GIPSY-CORE", dummyPlans);

        eligibilityValidationRules.validateLoadTypePlan(eligibilityWorkOrder, errorInfos);
        Assert.assertTrue(errorInfos.size() == 1);

        // remove one element from mapping
        eligibilityWorkOrder.getPlanMaps().get("table.GIPSY-CORE").get(0).set(2, null);
        // this validation will return 2 errors
        eligibilityValidationRules.validateLoadTypePlan(eligibilityWorkOrder, errorInfos);
        Assert.assertTrue(errorInfos.size() == 3);

        // case for duplicates, rebuild plan mapping
        eligibilityWorkOrder.getPlanMaps().remove("table.MCNT-CORE");
        eligibilityWorkOrder.getPlanMaps().remove("table.GIPSY-CORE");
        eligibilityWorkOrder.setVLOADFormat("No");
        eligibilityWorkOrder.setGipsyFormat("Yes");
        eligibilityWorkOrder.getPlanMaps().put("table.GIPSY-CORE", dummyPlans);
        eligibilityWorkOrder.getPlanMaps().get("table.GIPSY-CORE")
                .add(Lists.newArrayList("12345", "011", "101", "612345", "1", "1", "A"));
        eligibilityValidationRules.validateLoadTypePlan(eligibilityWorkOrder, errorInfos);
        Assert.assertTrue(errorInfos.size() == 4);

    }

    @Test
    public void testDuplicatedCertificates() {
        List<List<String>> certMaps = MockDataBuilder.getCertificateMaps().get("table.CERT_MAPP");
        certMaps.add(certMaps.get(0));
        List<String> oldCertList = ValidationUtils.getListElementsByIndex(certMaps, 0, 0, StringUtils.EMPTY, 0);
        Set<String> testSet = Sets.newHashSet(oldCertList);
        Assert.assertTrue(certMaps.size() > testSet.size());
    }

    @Test
    public void testValidatePerScriptDeductibleGroupNumber() {
        // empty deductible
        eligibilityWorkOrder.setDeductibleMaps(MockDataBuilder.getEmptyDeductibleMaps());
        eligibilityValidationRules.validatePerScriptDeductibleGroupNumber(eligibilityWorkOrder, errorInfos);
        Assert.assertTrue(errorInfos.size() == 0);

        eligibilityWorkOrder.setDeductibleMaps(MockDataBuilder.getDeductibleMaps());
        eligibilityValidationRules.validatePerScriptDeductibleGroupNumber(eligibilityWorkOrder, errorInfos);
        Assert.assertTrue(errorInfos.size() == 0);

        eligibilityWorkOrder.getDeductibleMaps().get("table.DEDUCTIBLE").get(0).set(0, "00000");
        eligibilityValidationRules.validatePerScriptDeductibleGroupNumber(eligibilityWorkOrder, errorInfos);
        Assert.assertTrue(errorInfos.size() == 1);

        eligibilityWorkOrder.getDeductibleMaps().get("table.DEDUCTIBLE").get(0).set(0, null);
        eligibilityValidationRules.validatePerScriptDeductibleGroupNumber(eligibilityWorkOrder, errorInfos);
        Assert.assertTrue(errorInfos.size() == 2);
    }

    @Test
    public void testValidatePerScriptDeductibleClasses() {
        // empty deductible
        eligibilityWorkOrder.setDeductibleMaps(MockDataBuilder.getEmptyDeductibleMaps());
        eligibilityValidationRules.validatePerScriptDeductibleClasses(eligibilityWorkOrder, errorInfos);
        Assert.assertTrue(errorInfos.size() == 0);

        eligibilityWorkOrder.setDeductibleMaps(MockDataBuilder.getDeductibleMaps());
        eligibilityValidationRules.validatePerScriptDeductibleClasses(eligibilityWorkOrder, errorInfos);
        Assert.assertTrue(errorInfos.size() == 0);

        eligibilityWorkOrder.getDeductibleMaps().get("table.DEDUCTIBLE").get(0).set(1, "000");
        eligibilityValidationRules.validatePerScriptDeductibleClasses(eligibilityWorkOrder, errorInfos);
        Assert.assertTrue(errorInfos.size() == 1);

        eligibilityWorkOrder.getDeductibleMaps().get("table.DEDUCTIBLE").get(0).set(1, null);
        eligibilityValidationRules.validatePerScriptDeductibleClasses(eligibilityWorkOrder, errorInfos);
        Assert.assertTrue(errorInfos.size() == 3);
    }

    @Test
    public void testValidatePerScriptDeductibleIndicators() {

        // empty deductible
        eligibilityWorkOrder.setDeductibleMaps(MockDataBuilder.getEmptyDeductibleMaps());
        eligibilityValidationRules.validatePerScriptDeductibleIndicators(eligibilityWorkOrder, errorInfos);
        Assert.assertTrue(errorInfos.size() == 0);

        eligibilityWorkOrder.setDeductibleMaps(MockDataBuilder.getDeductibleMaps());
        eligibilityValidationRules.validatePerScriptDeductibleIndicators(eligibilityWorkOrder, errorInfos);
        Assert.assertTrue(errorInfos.size() == 0);

        List<String> listYes = Lists.newArrayList("Yes", "YES", "yes", "Y", "y");
        List<String> listNo = Lists.newArrayList("No", "NO", "no", "N", "n");

        for (String yes : listYes) {
            eligibilityWorkOrder.getDeductibleMaps().get("table.DEDUCTIBLE").get(0).set(2, yes);
            eligibilityValidationRules.validatePerScriptDeductibleIndicators(eligibilityWorkOrder, errorInfos);
            Assert.assertTrue(errorInfos.size() == 0);
        }

        for (String no : listNo) {
            eligibilityWorkOrder.getDeductibleMaps().get("table.DEDUCTIBLE").get(0).set(2, no);
            eligibilityValidationRules.validatePerScriptDeductibleIndicators(eligibilityWorkOrder, errorInfos);
            Assert.assertTrue(errorInfos.size() == 0);
        }

        eligibilityWorkOrder.getDeductibleMaps().get("table.DEDUCTIBLE").get(0).set(2, "True");
        eligibilityValidationRules.validatePerScriptDeductibleIndicators(eligibilityWorkOrder, errorInfos);
        Assert.assertTrue(errorInfos.size() == 1);

        eligibilityWorkOrder.getDeductibleMaps().get("table.DEDUCTIBLE").get(0).set(2, null);
        eligibilityValidationRules.validatePerScriptDeductibleIndicators(eligibilityWorkOrder, errorInfos);
        Assert.assertTrue(errorInfos.size() == 2);
    }

    @Test
    public void testValidateWorkOrder() throws ServiceException {
        List<ErrorInfo> errorInfos = Lists.newArrayList();
        // the request dates should be in the future
        eligibilityWorkOrder.setConversionDate("21150915");
        eligibilityWorkOrder.setInitialRequestRunDate(WorkOrderRequestDate.createWith(null, "21150901",
                WorkOrderRequestDate.CURRENT, null, null));
        eligibilityWorkOrder.setDeltaRequestRunDates(Lists.newArrayList(WorkOrderRequestDate.createWith(null, "21150920",
                WorkOrderRequestDate.DELTA, null, null)));

        eligibilityWorkOrder.setCertAutoGenerated("No");

        eligibilityValidationRules.validateWorkOrder(eligibilityWorkOrder, errorInfos);
        Assert.assertTrue(errorInfos.size() == 0);
    }
}