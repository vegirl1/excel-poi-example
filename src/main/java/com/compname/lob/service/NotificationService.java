package com.compname.lob.service;

import java.io.File;
import java.util.List;
import java.util.Map;

import com.compname.lob.domain.config.EligibilityConfig;

/**
 * NotificationService
 * 
 * @author vegirl1
 * @since Jul 7, 2015
 * @version $Revision$
 */
public interface NotificationService {

    static final String LONG_DATETIME_FORMAT = "EEE, d MMM yyyy hh:mm aaa";

    void sendEligibilityLoadResultNotification(EligibilityConfig config, List<File> processedFiles, Map<File, String> failedFiles,
            Map<File, String> warningWorkOrders);
}
