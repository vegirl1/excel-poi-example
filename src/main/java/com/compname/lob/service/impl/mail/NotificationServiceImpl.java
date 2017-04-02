package com.compname.lob.service.impl.mail;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;

import javax.mail.internet.MimeMessage;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.util.Assert;

import com.compname.lob.domain.config.AbstractProperties;
import com.compname.lob.domain.config.EligibilityConfig;
import com.compname.lob.service.NotificationService;

/**
 * NotificationServiceImpl
 * 
 * @author vegirl1
 * @since Jul 7, 2015
 * @version $Revision$
 */
public class NotificationServiceImpl implements NotificationService, InitializingBean {

    private static final Logger LOG = LoggerFactory.getLogger(NotificationServiceImpl.class);

    @Autowired
    private JavaMailSender      javaMailSender;

    /**
     * Class constructor.
     * 
     */
    public NotificationServiceImpl() {
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        Assert.notNull(javaMailSender, "JavaMailSender bean can't be null");

    }

    /**
     * {@inheritDoc}
     * 
     * @see com.compname.lob.service.NotificationService#sendEligibilityLoadResultNotification(com.compname.lob.domain.config.EligibilityConfig,
     *      java.util.List, java.util.Map)
     */
    @Override
    public void sendEligibilityLoadResultNotification(EligibilityConfig config, List<File> processedFiles,
            Map<File, String> failedFiles, Map<File, String> warningWorkOrders) {

        if (CollectionUtils.isEmpty(processedFiles) && MapUtils.isEmpty(failedFiles) && MapUtils.isEmpty(warningWorkOrders)) {
            return;
        }

        SimpleDateFormat emaildateTimeformat = new SimpleDateFormat(NotificationService.LONG_DATETIME_FORMAT);

        if (config.isEmailEnabled()) {

            StringBuilder text = new StringBuilder(config.getEmailSubject() + " on "
                    + emaildateTimeformat.format(new GregorianCalendar().getTime()) + " <br/> <br/>");

            text.append("<b>Successfully</b> processed file(s): <br/> ");
            for (File file : processedFiles) {
                text.append(" - " + file.getName() + "; <br/>");
            }

            if (MapUtils.isNotEmpty(failedFiles)) {
                text.append("<br/> <font color='red'> <b>Failed</b> to load the following file(s): </font> <br/>");
                for (File file : failedFiles.keySet()) {
                    text.append(" - " + file.getName() + ", <br/>");
                    text.append(" Error Message(s) : <br/>" + failedFiles.get(file).replace(AbstractProperties.SEMICOLON, ";<br/>")
                            + "<br/><br/>");
                }
            }

            if (MapUtils.isNotEmpty(warningWorkOrders)) {
                text.append("<br/> <font color='purple'> <b>Warning(s)</b> when loaded the following file(s):  </font> <br/>");
                for (File file : warningWorkOrders.keySet()) {
                    text.append(" - " + file.getName() + ", <br/>");
                    text.append(" Warning Message(s) : <br/>"
                            + warningWorkOrders.get(file).replace(AbstractProperties.SEMICOLON, ";<br/>") + "<br/><br/>");
                }
            }

            LOG.info("Sending Eligibility Load Result Notification email");
            sendMail(config.getEmailFrom(), config.getEmailTo(), config.getEmailSubject(), config.getEmailCc(), text.toString());
            LOG.debug("Sent Eligibility Load Result Notification, email text message: {}", text);
        }
    }

    private void sendMail(String mailFrom, String mailTo, String subject, String mailCc, String messageText) {
        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "utf-8");

            helper.setFrom(mailFrom);

            helper.setTo(StringUtils.split(mailTo, AbstractProperties.COMMA));
            helper.setSubject(subject);

            if (StringUtils.isNotEmpty(mailCc)) {
                helper.setCc(StringUtils.split(mailCc, AbstractProperties.COMMA));
            }

            helper.setText(messageText, true);

            javaMailSender.send(message);

        } catch (Exception e) {
            LOG.error("Failed to send an email with following subject '{}' to '{}' ", subject, mailTo);
        }
    }

}
