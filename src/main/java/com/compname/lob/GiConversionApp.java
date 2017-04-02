package com.compname.lob;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.support.AbstractApplicationContext;


import com.google.common.base.Stopwatch;
import com.google.common.collect.Lists;
import com.compname.lob.beans.SpringConfiguration;
import com.compname.lob.domain.config.ClaimsConfig;
import com.compname.lob.domain.config.EligibilityConfig;
import com.compname.lob.domain.config.MainOptions;
import com.compname.lob.domain.config.MasterAppConfig;
import com.compname.lob.service.impl.ClaimsConversionService;
import com.compname.lob.service.impl.EligibilityConversionService;
import com.compname.lob.service.impl.MasterAppConversionService;

public class GiConversionApp {

    private static final Logger LOG                      = LoggerFactory.getLogger(GiConversionApp.class);

    private static final String SYSTEM_PROPERTY_ROOTPATH = "rootPath";

    /**
     * Entry point of the application.
     * 
     * @throws ParseException
     * @throws ServiceException
     */
    public static void main(String[] args) throws Exception {

        LOG.info("Started  executions with the following options: ['{}']", Arrays.asList(args).toString());

        Options options = getDefinedMainOptions();

        CommandLine cmd;

        try {
            cmd = new PosixParser().parse(options, args);
        } catch (ParseException pe) {
            LOG.error("Wrong option(s) porvided, execution stopped. Error message : {}", pe.getLocalizedMessage());
            return;
        }

        if (cmd.getOptions().length == 0) {
            LOG.error("No option porvided, execution stopped.");
            return;
        }

        if (StringUtils.isEmpty(System.getProperty(SYSTEM_PROPERTY_ROOTPATH))) {
            // root path should be defined as -DrootPath option
            LOG.error("No root path provided, execution stopped.");
            return;
        }

        LOG.debug("Build AbstractApplicationContext conetxt for GiConversionApp ");
        AbstractApplicationContext context = new AnnotationConfigApplicationContext(SpringConfiguration.class);

        if (cmd.hasOption(MainOptions.OPTION_ELIGIBILITY.getValue())) {
            processEligibilityOptions(context, cmd);
        }

        if (cmd.hasOption(MainOptions.OPTION_CLAIMS.getValue())
                && (cmd.hasOption(MainOptions.OPTION_CLAIM_PREDETERMINATION.getValue()) || cmd
                        .hasOption(MainOptions.OPTION_CLAIM_REVERSAL.getValue()))) {
            processClaimsOptions(context, cmd);
        }

        if (cmd.hasOption(MainOptions.OPTION_MASTERAPP.getValue())) {
            processMasterappOptions(context, cmd);
        }

        context.close();
        LOG.debug("Closed AbstractApplicationContext conetxt for GiConversionApp ");
        LOG.info("Done ... GiConversionApp executions for the following options: ['{}']", Arrays.asList(args).toString());
    }

    /**
     * processClaimsOptions
     * 
     * @param cmd
     * @param context
     * @throws ServiceException
     */
    private static void processClaimsOptions(AbstractApplicationContext context, CommandLine cmd) throws ServiceException {
        if (cmd.hasOption(MainOptions.OPTION_CLAIM_PREDETERMINATION.getValue())
                || cmd.hasOption(MainOptions.OPTION_CLAIM_REVERSAL.getValue())) {

            LOG.debug("Get ClaimsConversionService & claimsConfig beans from context ");
            ClaimsConversionService claimsService = (ClaimsConversionService) context.getBean("claimsConversionService");
            ClaimsConfig claimsConfig = (ClaimsConfig) context.getBean("claimsConfig");

            LOG.info("Starting to create Excel reports for claims");

            setClaimsReprotsToGenerate(cmd, claimsConfig);

            Stopwatch timer = Stopwatch.createStarted();

            claimsService.createResultReport(claimsService.getInitialReportData(claimsConfig));

            LOG.info("Execution of ClaimsConversionService.createResultReport() took: " + timer.stop());
        } else {
            LOG.info("Got a Claim stream options that is not implemented yet...");
        }
    }

    /**
     * processEligibilityOptions
     * 
     * @param context
     */
    private static void processEligibilityOptions(AbstractApplicationContext context, CommandLine cmd) throws ServiceException {

        LOG.debug("Get EligibilityConversionService & eligibilityConfig beans from context ");

        EligibilityConversionService eligibilityService = (EligibilityConversionService) context
                .getBean("eligibilityConversionService");
        EligibilityConfig eligibilityConfig = (EligibilityConfig) context.getBean("eligibilityConfig");

        // input - load WorkOrder
        if (cmd.hasOption(MainOptions.OPTION_LOAD_WORKORDER.getValue())) {

            LOG.info("Starting to proccess eligibility input work order file(s)");

            Stopwatch timer = Stopwatch.createStarted();

            List<File> workOrderFileNames = eligibilityService.getWorkOrderFiles(eligibilityConfig);
            eligibilityService.processWorkOrder(workOrderFileNames);

            LOG.info("Execution of EligibilityConversionService.processWorkOrder() took: " + timer.stop());
        }

        // output - generate Excel Reports
        if (cmd.hasOption(MainOptions.OPTION_OUTPUT_REPORTS.getValue())) {

            LOG.info("Starting to create Excel reports for eligibility");

            Stopwatch timer = Stopwatch.createStarted();

            eligibilityService.createResultReport(eligibilityService.getInitialReportData(eligibilityConfig));

            LOG.info("Execution of EligibilityConversionService.createResultReport() took: " + timer.stop());

        }
    }

    /**
     * processMasterappOptions
     * 
     * @param cmd
     * @param context
     * @throws ServiceException
     */
    private static void processMasterappOptions(AbstractApplicationContext context, CommandLine cmd) throws ServiceException {
        if (cmd.hasOption(MainOptions.OPTION_OUTPUT_REPORTS.getValue())) {

            LOG.debug("Get masterappConversionService & masterAppConfig beans from context ");

            MasterAppConversionService masterappService = (MasterAppConversionService) context
                    .getBean("masterappConversionService");
            MasterAppConfig masterAppConfig = (MasterAppConfig) context.getBean("masterAppConfig");

            LOG.info("Starting to create Excel reports for masterapp");

            Stopwatch timer = Stopwatch.createStarted();

            masterappService.createResultReport(masterappService.getInitialReportData(masterAppConfig));

            LOG.info("Execution of MasterAppConversionService.createResultReport() took: " + timer.stop());
        } else {
            LOG.info("Got a Masterapp stream options that is not implemented yet...");
        }
    }

    /**
     * getDefinedMainOptions
     * 
     */
    private static Options getDefinedMainOptions() {
        Options options = new Options();
        options.addOption(new Option(MainOptions.OPTION_ELIGIBILITY.getValue(), "proccess eligibility"));
        options.addOption(new Option(MainOptions.OPTION_CLAIMS.getValue(), "proccess claims"));
        options.addOption(new Option(MainOptions.OPTION_MASTERAPP.getValue(), "proccess masterapp"));
        options.addOption(new Option(MainOptions.OPTION_LOAD_WORKORDER.getValue(), "load work order"));
        options.addOption(new Option(MainOptions.OPTION_CLAIM_PREDETERMINATION.getValue(), "generate predetermination excel report"));
        options.addOption(new Option(MainOptions.OPTION_CLAIM_REVERSAL.getValue(), "generate claims reversal excel report"));
        options.addOption(new Option(MainOptions.OPTION_OUTPUT_REPORTS.getValue(), "generate output excel report(s)"));

        return options;
    }

    /**
     * setClaimsReprotsToGenerate
     * 
     * @param cmd
     * @param claimsConfig
     */
    private static void setClaimsReprotsToGenerate(CommandLine cmd, ClaimsConfig claimsConfig) {
        List<String> filesToCreate = Lists.newArrayList();
        if (cmd.hasOption(MainOptions.OPTION_CLAIM_PREDETERMINATION.getValue())) {
            filesToCreate.add(MainOptions.OPTION_CLAIM_PREDETERMINATION.getValue());
        }

        if (cmd.hasOption(MainOptions.OPTION_CLAIM_REVERSAL.getValue())) {
            filesToCreate.add(MainOptions.OPTION_CLAIM_REVERSAL.getValue());
        }

        claimsConfig.setFilesToCreate(filesToCreate);
    }
}
