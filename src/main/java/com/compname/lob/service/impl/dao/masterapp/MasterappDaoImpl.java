package com.compname.lob.service.impl.dao.masterapp;

import javax.sql.DataSource;

import com.compname.lob.service.impl.dao.datasource.AbstractConversionDaoImpl;

/**
 * MasterappDaoImpl
 * 
 * @author vegirl1
 * @since Aug 14, 2015
 * @version $Revision$
 */
public class MasterappDaoImpl extends AbstractConversionDaoImpl implements MasterappDao {

    /**
     * Class constructor.
     * 
     * @param dataSource
     */
    public MasterappDaoImpl(DataSource dataSource) {
        super(dataSource);
    }

}
