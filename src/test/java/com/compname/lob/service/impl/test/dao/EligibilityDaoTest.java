package com.compname.lob.service.impl.test.dao;

import java.sql.Date;
import java.util.Calendar;

import javax.sql.DataSource;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import com.compname.lob.service.impl.dao.eligibility.EligibilityDao;
import com.compname.lob.service.impl.dao.eligibility.EligibilityDaoImpl;

/**
 * EligibilityDaoTest
 * 
 * @author vegirl1
 * @since Sep 17, 2015
 * @version $Revision$
 */
@RunWith(MockitoJUnitRunner.class)
public class EligibilityDaoTest {

    @Before
    public void setUp() throws Exception {
    }

    @Test
    public void testGetCompassBatchDate() {
        Calendar c = Calendar.getInstance();
        c.set(Calendar.HOUR, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);

        java.util.Date date = c.getTime();

        final Date batchDate = new Date(date.getTime());
        DataSource mockCompassDS = Mockito.mock(DataSource.class);
        EligibilityDao dao = Mockito.spy(new EligibilityDaoImpl(mockCompassDS));

        Mockito.doAnswer(new Answer<Date>() {
            public Date answer(final InvocationOnMock invocation) throws Throwable {
                return batchDate;
            }
        }).when(dao).getCompassBatchDate();

        Assert.assertEquals("date", batchDate, dao.getCompassBatchDate());
    }
}
