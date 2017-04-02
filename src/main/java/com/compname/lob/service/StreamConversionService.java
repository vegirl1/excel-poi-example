package com.compname.lob.service;

import java.io.File;
import java.util.List;


import com.compname.lob.domain.config.AbstractProperties;
import com.compname.lob.domain.report.ReportData;

/**
 * StreamConversionService
 * 
 * @author vegirl1
 * @since Jun 12, 2015
 * @version $Revision$
 */
public interface StreamConversionService<E extends AbstractProperties> {

    List<File> getWorkOrderFiles(E config);

    void processWorkOrder(List<File> workOrderFiles);

    ReportData getInitialReportData(E config) throws ServiceException;

    void createResultReport(ReportData data);

}
