package com.compname.lob.service.impl.test;

import java.io.File;
import java.util.List;

import junit.framework.Assert;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;


import com.google.common.collect.Lists;
import com.compname.lob.domain.config.ClaimsConfig;
import com.compname.lob.domain.config.EligibilityConfig;
import com.compname.lob.domain.report.DbParameter;
import com.compname.lob.domain.report.ReportData;
import com.compname.lob.service.impl.ClaimsConversionService;
import com.compname.lob.service.impl.EligibilityConversionService;
import com.compname.lob.service.impl.dao.claims.ClaimsDao;
import com.compname.lob.service.impl.dao.datasource.StorProcRefCursorDao;
import com.compname.lob.service.mock.MockDataBuilder;

/**
 * EligibilityConversionServiceTest
 * 
 * @author vegirl1
 * @since Aug 5, 2015
 * @version $Revision$
 */
@RunWith(MockitoJUnitRunner.class)
public class StreamConversionServiceTest {

    public static final String           EXCEL_FILE_NAME       = "G699999_Pre-Determinations_2015-08-06-142355.xlsx";
    public static final String           EXCEL_DELTA_FILE_NAME = "G699999_Delta_Pre-Determinations_2015-08-06-142355.xlsx";
    public static final String           EXCEL_OUT_DIR         = "work/claims/out";

    @Mock
    private EligibilityConversionService eligibilityConversionService;

    @Mock
    private ClaimsConversionService      claimsConversionService;

    @Mock
    private EligibilityConfig            eligibilityConfig;

    @Mock
    private ClaimsConfig                 claimsConfig;

    @Mock
    private ClaimsDao                    claimsDao;

    @Mock
    private StorProcRefCursorDao         storProcRefCursorDao;

    private ReportData                   reportData;

    @Before
    public void setUp() throws Exception {
        //
        Mockito.when(eligibilityConfig.getWorkOrderFilenamePattern()).thenReturn(".*ITWORKorder.*\\d{1,6}\\D*.xlsx");
        Mockito.when(eligibilityConfig.getInputPath()).thenReturn("src/test/resources/sample-workorder/eligibility");
        Mockito.when(eligibilityConversionService.getWorkOrderFiles(Mockito.any(EligibilityConfig.class))).thenCallRealMethod();

        //
        Mockito.when(claimsConfig.getWorkOrderFilenamePattern()).thenReturn(".*ITWORKorder.*\\d{1,6}\\D*.xlsx");
        Mockito.when(claimsConfig.getInputPath()).thenReturn("src/test/dummy");
        Mockito.when(claimsConversionService.getWorkOrderFiles(Mockito.any(ClaimsConfig.class))).thenCallRealMethod();

        Mockito.when(claimsConversionService.getInitialReportData(Mockito.any(ClaimsConfig.class))).thenReturn(
                MockDataBuilder.mockReportData());
        Mockito.when(claimsConfig.getFilesToCreate()).thenReturn(Lists.newArrayList("predetermination"));
        Mockito.when(claimsConfig.getFileNames()).thenReturn(MockDataBuilder.getFileNames());
        Mockito.when(claimsConfig.getParameters()).thenReturn(MockDataBuilder.getDbParameters());
        Mockito.when(claimsConfig.getFileSheetNames()).thenReturn(MockDataBuilder.getFileSheetNames());
        Mockito.when(claimsConfig.getDatasourceNames()).thenReturn(MockDataBuilder.getDatasourceNames());

        Mockito.when(claimsConfig.buildPathFileName(Mockito.anyString())).thenReturn(EXCEL_OUT_DIR + "/" + EXCEL_FILE_NAME,
                EXCEL_OUT_DIR + "/" + EXCEL_DELTA_FILE_NAME);

        Mockito.when(claimsConfig.getFileSheetHeaders()).thenReturn(MockDataBuilder.getFileSheetHeaders());

        Mockito.when(claimsConversionService.getClaimsConfig()).thenReturn(claimsConfig);
        Mockito.when(claimsConversionService.getClaimsDao()).thenReturn(claimsDao);
        Mockito.when(claimsConversionService.getStorProcRefCursorDao()).thenReturn(storProcRefCursorDao);

        List<DbParameter> dummyParamList = Matchers.any();
        Mockito.when(storProcRefCursorDao.getRefCursorValues(Mockito.anyString(), dummyParamList)).thenReturn(
                MockDataBuilder.getDummyDataSourceValues());

        Mockito.doAnswer(Answers.CALLS_REAL_METHODS.get()).when(claimsConversionService)
                .createResultReport(Mockito.any(ReportData.class));
    }

    @Test
    public void testGetWorkOrderFiles() {
        List<File> workOrderFiles = eligibilityConversionService.getWorkOrderFiles(eligibilityConfig);
        Assert.assertNotNull(workOrderFiles);
        Assert.assertTrue(workOrderFiles.size() > 0);
        Assert.assertTrue(workOrderFiles.get(0).getName().equals("ITWORKorder 99999.xlsx"));
    }

    @Test
    public void testGetWorkOrderFiles_WrongPath() {
        List<File> workOrderFiles = claimsConversionService.getWorkOrderFiles(claimsConfig);
        Assert.assertNotNull(workOrderFiles);
        Assert.assertTrue(workOrderFiles.size() == 0);
    }

    @Test
    public void testInitReportData() throws ServiceException {
        ReportData reportData = claimsConversionService.getInitialReportData(claimsConfig);
        Assert.assertNotNull(reportData);
        Assert.assertTrue(reportData.getWorkOrderKeyValues().size() == 2);
    }

    @Test
    public void testCreateResultReport() {
        reportData = MockDataBuilder.mockReportData();
        claimsConversionService.createResultReport(reportData);
        File testedFile = new File(FilenameUtils.concat(EXCEL_OUT_DIR, EXCEL_FILE_NAME));
        Assert.assertNotNull(testedFile);
        Assert.assertTrue(testedFile.exists());
        FileUtils.deleteQuietly(testedFile);
        testedFile = new File(FilenameUtils.concat(EXCEL_OUT_DIR, EXCEL_FILE_NAME));
        Assert.assertFalse(testedFile.exists());
        // check delta
        testedFile = new File(FilenameUtils.concat(EXCEL_OUT_DIR, EXCEL_DELTA_FILE_NAME));
        Assert.assertNotNull(testedFile);
        Assert.assertTrue(testedFile.exists());
        FileUtils.deleteQuietly(testedFile);
        testedFile = new File(FilenameUtils.concat(EXCEL_OUT_DIR, EXCEL_DELTA_FILE_NAME));
        Assert.assertFalse(testedFile.exists());

    }

}
