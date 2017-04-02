package com.compname.lob.service.impl;

import java.io.File;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;


import com.compname.lob.domain.config.MasterAppConfig;
import com.compname.lob.domain.report.ReportData;
import com.compname.lob.service.impl.dao.datasource.StorProcRefCursorDao;
import com.compname.lob.service.impl.dao.masterapp.MasterappDaoImpl;
import com.compname.lob.utils.DatasourceUtils;

/**
 * MasterAppConversionService
 * 
 * @author vegirl1
 * @since Jun 12, 2015
 * @version $Revision$
 */
public class MasterAppConversionService extends StreamConversionServiceImpl<MasterAppConfig> {

    @Autowired
    private MasterAppConfig masterAppConfig;

    @Autowired
    MasterappDaoImpl        masterappDao;

    /**
     * Class constructor.
     * 
     */
    @Autowired
    public MasterAppConversionService(@Qualifier("compasStorProcRefCursorDao") StorProcRefCursorDao storProcRefCursorDao) {
        super(storProcRefCursorDao);
    }

    /**
     * {@inheritDoc}
     * 
     * @see com.compname.lob.service.StreamConversionService#processWorkOrder(java.util.List)
     */
    @Override
    public void processWorkOrder(List<File> workOrderFiles) {
        // vegirl1: No WorkOrder to process for MasterApp (loaded manually...)
    }

    /**
     * {@inheritDoc}
     * 
     * @see com.compname.lob.service.StreamConversionService#createResultReport(com.compname.lob.domain.AbstractProperties,
     *      com.compname.lob.domain.report.ReportData)
     */
    @Override
    public void createResultReport(ReportData data) {
        // vegirl1: No result reports for Masterapp in Phase 1
        if (CollectionUtils.isEmpty(data.getWorkOrderKeyValues())) {
            LOG.info("The List of Groups to generate reports is empty");
            return;
        }

        // loop through the List of files to create
        for (String fileType : masterAppConfig.getFilesToCreate()) {
            data.setFileType(fileType);
            // create file for each WorkOrder(ML Group)
            for (Map<String, Object> woKey : data.getWorkOrderKeyValues()) {

                DatasourceUtils.setDbParameterValues(masterAppConfig, woKey);

                String extractType = woKey.get("EXTRACTION_TYPE").toString();
                String fileName = masterAppConfig.getFileNames().get(fileType + "." + extractType.toLowerCase());
                // don't have reports for each type of extraction
                if (StringUtils.isEmpty(fileName)) {
                    break;
                }

                data.setFileName(DatasourceUtils.buildFileName(fileName, masterAppConfig.getParameters()));

                try {
                    super.retrieveReportData(masterAppConfig, fileType, data);
                    super.createExcelReport(masterAppConfig, data);
                } catch (ServiceException ex) {
                    Long workOrderKey = Long.parseLong(woKey.get("WO_KEY").toString());
                    LOG.error("Failed to Create Result Report '{}' for WorkOrder '{}';\n ErrorInfo(s): {} ", data.getFileName(),
                            workOrderKey, ex.getMessage());
                    String processName = extractType + "_" + fileType;
                    masterappDao.logError(workOrderKey, ex.getMessage(), "createResultReport()", processName.toUpperCase());
                }
            }
        }
    }
}
