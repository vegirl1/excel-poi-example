package com.compname.lob.service.impl;

import java.io.File;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;


import com.compname.lob.domain.config.ClaimsConfig;
import com.compname.lob.domain.report.ReportData;
import com.compname.lob.service.impl.dao.claims.ClaimsDao;
import com.compname.lob.service.impl.dao.datasource.StorProcRefCursorDao;
import com.compname.lob.utils.DatasourceUtils;

/**
 * ClaimsConversionService
 * 
 * @author vegirl1
 * @since Jun 12, 2015
 * @version $Revision$
 */
public class ClaimsConversionService extends StreamConversionServiceImpl<ClaimsConfig> {

    @Autowired
    private ClaimsConfig claimsConfig;

    @Autowired
    private ClaimsDao    claimsDao;

    /**
     * Class constructor.
     * 
     */
    @Autowired
    public ClaimsConversionService(@Qualifier("dwStorProcRefCursorDao") StorProcRefCursorDao storProcRefCursorDao) {
        super(storProcRefCursorDao);
    }

    @Override
    public void createResultReport(ReportData data) {

        if (CollectionUtils.isEmpty(data.getWorkOrderKeyValues())) {
            LOG.info("The List of Groups to generate reports is empty");
            return;
        }

        // loop through the List of files to create
        for (String fileType : getClaimsConfig().getFilesToCreate()) {
            data.setFileType(fileType);
            // create file for each WorkOrder(ML Group)
            for (Map<String, Object> woKey : data.getWorkOrderKeyValues()) {

                DatasourceUtils.setDbParameterValues(getClaimsConfig(), woKey);

                String extractType = woKey.get("EXTRACTION_TYPE").toString();
                String fileName = getClaimsConfig().getFileNames().get(fileType + "." + extractType.toLowerCase());
                // don't have reports for each type of extraction
                if (StringUtils.isEmpty(fileName)) {
                    continue;
                }

                data.setFileName(DatasourceUtils.buildFileName(fileName, getClaimsConfig().getParameters()));

                try {
                    super.retrieveReportData(getClaimsConfig(), fileType, data);
                    super.createExcelReport(getClaimsConfig(), data);
                } catch (ServiceException ex) {
                    Long workOrderKey = Long.parseLong(woKey.get("WO_KEY").toString());
                    LOG.error("Failed to Create Result Report '{}' for WorkOrder '{}';\n ErrorInfo(s): {} ", data.getFileName(),
                            workOrderKey, ex.getMessage());
                    String processName = extractType + "_" + fileType;
                    getClaimsDao().setWorkOrderProcessStatusToError(workOrderKey, extractType);
                    getClaimsDao().logError(workOrderKey, ex.getMessage(), "createResultReport()", processName.toUpperCase());
                }
            }
        }
    }

    /**
     * {@inheritDoc}
     * 
     * @see com.compname.lob.service.StreamConversionService#processWorkOrder(java.util.List)
     */
    @Override
    public void processWorkOrder(List<File> workOrderFiles) {
        // vegirl1: No separate WorkOrder to process for Claims (loaded with Eligibility)
    }

    /**
     * Getter method of the <code>"claimsConfig"</code> class attribute.
     * 
     * @return the claimsConfig.
     */
    public ClaimsConfig getClaimsConfig() {
        return this.claimsConfig;
    }

    /**
     * Setter method of the <code>"claimsConfig"</code> class attribute.
     * 
     * @param claimsConfig the claimsConfig to set.
     */
    public void setClaimsConfig(ClaimsConfig aClaimsConfig) {
        this.claimsConfig = aClaimsConfig;
    }

    /**
     * Getter method of the <code>"claimsDao"</code> class attribute.
     * 
     * @return the claimsDao.
     */
    public ClaimsDao getClaimsDao() {
        return this.claimsDao;
    }

    /**
     * Setter method of the <code>"claimsDao"</code> class attribute.
     * 
     * @param claimsDao the claimsDao to set.
     */
    public void setClaimsDao(ClaimsDao aClaimsDao) {
        this.claimsDao = aClaimsDao;
    }

}
