package com.compname.lob.service;


import com.compname.lob.domain.config.ClaimsConfig;
import com.compname.lob.domain.config.EligibilityConfig;
import com.compname.lob.domain.config.MasterAppConfig;

/**
 * Properties Configuration Service
 * 
 * @author vegirl1
 * @since May 28, 2015
 * @version $Revision$
 */
public interface PropertiesConfigService {

    EligibilityConfig getEligibilityConfiguration() throws ServiceException;

    MasterAppConfig getMasterAppConfiguration() throws ServiceException;

    ClaimsConfig getClaimsConfiguration() throws ServiceException;

}
