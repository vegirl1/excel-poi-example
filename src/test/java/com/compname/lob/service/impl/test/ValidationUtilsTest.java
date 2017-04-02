package com.compname.lob.service.impl.test;

import java.text.MessageFormat;
import java.util.List;
import java.util.Map;

import junit.framework.Assert;

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
import com.compname.lob.domain.workorder.EligibilityWorkOrder;
import com.compname.lob.utils.ValidationUtils;

/**
 * PropertyValidationUtilsTest
 * 
 * @author vegirl1
 * @since Aug 3, 2015
 * @version $Revision$
 */
@RunWith(MockitoJUnitRunner.class)
public class ValidationUtilsTest {

    @Mock
    private EligibilityWorkOrder eligibilityWorkOrder;

    @Mock
    private ClaimsConfig         claimsConfig;

    @Before
    public void setUp() throws Exception {
        Mockito.when(claimsConfig.getMaincursorDataSource()).thenReturn("MaincursorDataSource");
        Mockito.when(claimsConfig.getOutputPath()).thenReturn("/out");
        Mockito.when(claimsConfig.getFilesToCreate()).thenReturn(Lists.newArrayList("file_1", "file_2"));

        Map<String, List<String>> fileSheetNames = Maps.newHashMap();
        fileSheetNames.put("file_1", Lists.newArrayList("sheet1", "sheet2"));
        Mockito.when(claimsConfig.getFileSheetNames()).thenReturn(fileSheetNames);

        Map<String, String> fileNames = Maps.newHashMap();
        fileNames.put("file_1", "File Name");
        Mockito.when(claimsConfig.getFileNames()).thenReturn(fileNames);

        Map<String, String> fileSheetHeaders = Maps.newHashMap();
        fileSheetHeaders.put("file_1", "File Sheet Header");
        Mockito.when(claimsConfig.getFileSheetHeaders()).thenReturn(fileSheetHeaders);

        Map<String, String> datasourceNames = Maps.newHashMap();
        datasourceNames.put("file_1.sheet1", "StorProc()");
        Mockito.when(claimsConfig.getDatasourceNames()).thenReturn(datasourceNames);

        List<DbParameter> parameters = Lists.newArrayList();
        DbParameter param = new DbParameter("p_wo", "VARCHAR", "mainCursor", null);
        parameters.add(param);
        Mockito.when(claimsConfig.getParameters()).thenReturn(parameters);
    }

    @Test
    public void testValidateStreamProperties() throws ServiceException {
        ValidationUtils.validateStreamProperties(claimsConfig);
        Assert.assertTrue(true);
    }

    @Test(expected = ServiceException.class)
    public void testValidateStreamPropertiesNoMaincursorDataSource() throws ServiceException {
        Mockito.when(claimsConfig.getMaincursorDataSource()).thenReturn("");
        ValidationUtils.validateStreamProperties(claimsConfig);
        Assert.assertTrue(true);
    }

    @Test(expected = ServiceException.class)
    public void testValidateStreamPropertiesNoOutputPath() throws ServiceException {
        Mockito.when(claimsConfig.getOutputPath()).thenReturn("");
        ValidationUtils.validateStreamProperties(claimsConfig);
        Assert.assertTrue(true);
    }

    @Test(expected = ServiceException.class)
    public void testValidateStreamPropertiesNoFilesToCreate() throws ServiceException {
        Mockito.when(claimsConfig.getFilesToCreate()).thenReturn(null);
        ValidationUtils.validateStreamProperties(claimsConfig);
        Assert.assertTrue(true);
    }

    @Test(expected = ServiceException.class)
    public void testValidateStreamPropertiesNoFileSheetNames() throws ServiceException {
        Mockito.when(claimsConfig.getFileSheetNames()).thenReturn(null);
        ValidationUtils.validateStreamProperties(claimsConfig);
        Assert.assertTrue(true);
    }

    @Test(expected = ServiceException.class)
    public void testValidateStreamPropertiesNoFileNames() throws ServiceException {
        Mockito.when(claimsConfig.getFileNames()).thenReturn(null);
        ValidationUtils.validateStreamProperties(claimsConfig);
        Assert.assertTrue(true);
    }

    @Test(expected = ServiceException.class)
    public void testValidateStreamPropertiesNoFileSheetHeaders() throws ServiceException {
        Mockito.when(claimsConfig.getFileSheetHeaders()).thenReturn(null);
        ValidationUtils.validateStreamProperties(claimsConfig);
        Assert.assertTrue(true);
    }

    @Test(expected = ServiceException.class)
    public void testValidateStreamPropertiesNoDatasourceNames() throws ServiceException {
        Mockito.when(claimsConfig.getDatasourceNames()).thenReturn(null);
        ValidationUtils.validateStreamProperties(claimsConfig);
        Assert.assertTrue(true);
    }

    @Test(expected = ServiceException.class)
    public void testValidateStreamPropertiesNoParameters() throws ServiceException {
        Mockito.when(claimsConfig.getParameters()).thenReturn(null);
        ValidationUtils.validateStreamProperties(claimsConfig);
        Assert.assertTrue(true);
    }

    @Test
    public void testMessageFormat() throws ServiceException {

        String s = MessageFormat.format("Re-Run (Initial/Delta) = 'No' and the conversion exists for the group {0}.", "11111");
        System.out.println(s);
        Assert.assertTrue(StringUtils.isNotEmpty(s));
    }

    @Test
    public void testIsNotValidPlanMapValueLength() {

        List<List<String>> plans = Lists.newArrayList();

        plans.add(Lists.newArrayList("12345", "001", "101", "612345", "1", "1", "A"));

        Assert.assertFalse(ValidationUtils.isNotValidPlanMapValueLength(plans));

        plans.add(Lists.newArrayList(null, "001", "101", "612345", "1", "1", "A"));
        Assert.assertFalse(ValidationUtils.isNotValidPlanMapValueLength(plans));

        plans.add(Lists.newArrayList("1234567", "0014", "1014", "61234578", "1000", "10000", "ABBBB"));
        Assert.assertTrue(ValidationUtils.isNotValidPlanMapValueLength(plans));

    }
}
