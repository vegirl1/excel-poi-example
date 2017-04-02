package com.compname.lob.aop;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Log Thrown Exception
 * 
 * @author vegirl1
 * @since Sep 28, 2015
 * @version $Revision$
 */
@Aspect
public class LogExceptionAspect {

    private static final Logger LOG = LoggerFactory.getLogger(LogExceptionAspect.class);

    @AfterThrowing(pointcut = "com.compname.lob.aop.SystemPointcut.inDataSourceConfig() || com.compname.lob.aop.SystemPointcut.inBusinessService()", throwing = "ex")
    public void doAfterThrowing(JoinPoint joinPoint, Exception ex) {
        LOG.error("Error Message : {}", ex.getMessage());
        LOG.error("Thrown in Method : {}", joinPoint.getSignature());
    }
}
