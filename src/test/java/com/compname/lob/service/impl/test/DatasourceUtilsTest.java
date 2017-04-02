package com.compname.lob.service.impl.test;

import java.util.List;
import java.util.Map;

import junit.framework.Assert;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;


import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.compname.lob.domain.config.ClaimsConfig;
import com.compname.lob.domain.report.DbParameter;
import com.compname.lob.utils.DatasourceUtils;

/**
 * DatasourceUtilsTest
 * 
 * @author vegirl1
 * @since Aug 3, 2015
 * @version $Revision$
 */
@RunWith(MockitoJUnitRunner.class)
public class DatasourceUtilsTest {

    @Mock
    private ClaimsConfig      claimsConfig;

    private List<DbParameter> parameters = Lists.newLinkedList();
    private String            fileNameTemplate;
    private String            storProcSignature;

    @Before
    public void setUp() throws Exception {

        DbParameter param1 = new DbParameter("p_ML_GR_NUM", "VARCHAR", "maincursor.ML_GR_NUM", "999999");
        DbParameter param2 = new DbParameter("p_WorkOrderKey", "NUMBER", "maincursor.WO_KEY", "1111");
        DbParameter param3 = new DbParameter("po_result", "SYS_REFCURSOR", "SYS_REFCURSOR", "SYS_REFCURSOR");
        DbParameter param4 = new DbParameter("p_ExtractionType", "VARCHAR", "mainOption.extractionType", null);

        parameters.add(param1);
        parameters.add(param2);
        parameters.add(param3);
        parameters.add(param4);

        fileNameTemplate = "G[p_ML_GR_NUM]_Pre-Determinations_{0,date,yyyy-MM-dd-HHmmss}";

        storProcSignature = "SL_GLH_HLT_DTL_CLM_RPT.ExtractClaimRevesalReportData(p_WorkOrderKey,po_result)";

        Mockito.when(claimsConfig.getParameters()).thenReturn(parameters);
    }

    @Test
    public void testBuildFileName() {
        String fileName = DatasourceUtils.buildFileName(fileNameTemplate, parameters);
        Assert.assertNotNull(fileName);
        Assert.assertTrue(StringUtils.contains(fileName, "G999999_Pre-Determinations_"));
    }

    @Test
    public void testGetStorProcName() {
        String fileName = DatasourceUtils.getStorProcName(storProcSignature);
        Assert.assertNotNull(fileName);
        Assert.assertTrue(StringUtils.equals(fileName, "SL_GLH_HLT_DTL_CLM_RPT.ExtractClaimRevesalReportData"));
    }

    @Test
    public void testGetStorProcParameters() {
        List<DbParameter> storProcParameters = DatasourceUtils.getStorProcParameters(storProcSignature, parameters);
        Assert.assertNotNull(storProcParameters);
        Assert.assertTrue(storProcParameters.size() == 2);
        Assert.assertTrue(storProcParameters.size() < parameters.size());
    }

    @Test
    public void testValidateEligibilityWorkOrderFailed() throws ServiceException {
        Map<String, Object> values = Maps.newHashMap();
        values.put("ML_GR_NUM", "789");
        values.put("WO_KEY", "222");

        DatasourceUtils.setDbParameterValues(claimsConfig, values);
        Assert.assertTrue(CollectionUtils.isNotEmpty(claimsConfig.getParameters()));
        Assert.assertTrue(claimsConfig.getParameters().get(0).getName().equals("p_ML_GR_NUM"));
        Assert.assertTrue(claimsConfig.getParameters().get(0).getValue().equals("789"));

    }
}
