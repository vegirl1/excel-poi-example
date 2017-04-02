package com.compname.lob.service.impl.test;

import junit.framework.Assert;

import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.core.env.Environment;


import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.compname.lob.domain.config.ClaimsConfig;
import com.compname.lob.domain.report.DbParameter;
import com.compname.lob.service.impl.config.PropertiesConfigServiceImpl;

/**
 * PropertiesConfigServiceTest
 * 
 * @author vegirl1
 * @since Aug 12, 2015
 * @version $Revision$
 */
@RunWith(MockitoJUnitRunner.class)
public class PropertiesConfigServiceTest {

    private PropertiesConfigServiceImpl configService;

    @Mock
    Environment                         env;

    @Before
    public void setUp() throws Exception {

        // Define how your mock object should behave
        Mockito.when(this.env.getProperty("claims.out.path")).thenReturn("out.path");
        Mockito.when(this.env.getProperty("claims.maincursor")).thenReturn("maincursor");
        Mockito.when(this.env.getProperty("claims.file.typenames")).thenReturn("predetermination,reversal");
        Mockito.when(this.env.getProperty("claims.file.name.predetermination.initial")).thenReturn("predetermination.initial");
        Mockito.when(this.env.getProperty("claims.file.name.predetermination.delta")).thenReturn("predetermination.delta");
        Mockito.when(this.env.getProperty("claims.file.name.reversal.delta")).thenReturn("reversal.delta");
        Mockito.when(this.env.getProperty("claims.sheet.names.predetermination")).thenReturn("Health-Dental_Pred");
        Mockito.when(this.env.getProperty("claims.sheet.names.reversal")).thenReturn("Claims_Reversals");

        Mockito.when(this.env.getProperty("claims.sheet.header.predetermination.Health-Dental_Pred")).thenReturn(
                "Health & Dental Pre-Determinations Report");
        Mockito.when(this.env.getProperty("claims.sheet.header.reversal.Claims_Reversals")).thenReturn("Claims Reversal Report");

        Mockito.when(this.env.getProperty("claims.sheet.datasource.predetermination.Health-Dental_Pred")).thenReturn(
                "datasource.predetermination");
        Mockito.when(this.env.getProperty("claims.sheet.datasource.reversal.Claims_Reversals")).thenReturn("datasource.reversal");

        Mockito.when(this.env.getProperty("claims.parameters")).thenReturn("p_WorkOrderKey,p_ExtractionType,p_ML_GR_NUM");

        Mockito.when(this.env.getProperty("claims.parameter.type.p_ML_GR_NUM")).thenReturn("VARCHAR");
        Mockito.when(this.env.getProperty("claims.parameter.type.p_wo_key")).thenReturn("NUMBER");
        Mockito.when(this.env.getProperty("claims.parameter.type.p_Extraction_type")).thenReturn("VARCHAR");
        Mockito.when(this.env.getProperty("claims.parameter.value.p_wo_key")).thenReturn("maincursor.WO_KEY");
        Mockito.when(this.env.getProperty("claims.parameter.value.p_Extraction_type")).thenReturn("maincursor.EXTRACTION_TYPE");
        Mockito.when(this.env.getProperty("claims.parameter.value.p_ML_GR_NUM")).thenReturn("maincursor.ML_GR_NUM");

        configService = new PropertiesConfigServiceImpl();
        configService.setEnv(env);
    }

    @Test
    public void testGetClaimsConfiguration() throws ServiceException {

        ClaimsConfig claimConfig = configService.getClaimsConfiguration();
        Assert.assertTrue(claimConfig.getOutputPath().equals("out.path"));
        Assert.assertTrue(claimConfig.getMaincursorDataSource().equals("maincursor"));
        Assert.assertTrue(claimConfig.getFilesToCreate().size() == 2);
        Assert.assertTrue(claimConfig.getFileNames().size() == 3);
        Assert.assertTrue(claimConfig.getFileNames().size() == 3);
        Assert.assertTrue(claimConfig.getFileSheetNames().size() == 2);
        Assert.assertTrue(claimConfig.getFileSheetHeaders().size() == 2);
        Assert.assertTrue(claimConfig.getDatasourceNames().size() == 2);
        Assert.assertTrue(claimConfig.getParameters().size() == 3);

        for (final String s : Lists.newArrayList("p_WorkOrderKey", "p_ExtractionType", "p_ML_GR_NUM")) {
            // if parameter is not present it will throw an exception
            DbParameter value = Iterables.find(claimConfig.getParameters(), new Predicate<DbParameter>() {
                public boolean apply(DbParameter input) {
                    return StringUtils.equalsIgnoreCase(s, input.getName());
                };
            });
            Assert.assertNotNull(value);
        }
    }
}
