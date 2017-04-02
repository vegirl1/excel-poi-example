package com.compname.lob.service.impl.dao.claims;

import java.sql.Date;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import com.compname.lob.service.impl.dao.datasource.AbstractConversionDaoImpl;

/**
 * ClaimsDaoImpl uses the DW db to generate the result Excel reports. NOT used for CLAIM WorkOrder!
 * 
 * @author vegirl1
 * @since Aug 11, 2015
 * @version $Revision$
 */
public class ClaimsDaoImpl extends AbstractConversionDaoImpl implements ClaimsDao {

    /**
     * Class constructor.
     * 
     */
    @Autowired
    public ClaimsDaoImpl(@Qualifier("dataSourceDW") DataSource dataSource) {
        super(dataSource);
    }

    /**
     * {@inheritDoc}
     * 
     * @see com.compname.lob.service.impl.dao.claims.ClaimsDao#getCompassBatchDate()
     */
    @Override
    public Date getCompassBatchDate() {
        String sql = "select SL_GLH_CLM_WO_UPDATES_PKG.Fnc_getCompassBatchDate from dual";
        return super.getNamedParameterJdbcTemplate().getJdbcOperations().queryForObject(sql, Date.class);
    }
}
