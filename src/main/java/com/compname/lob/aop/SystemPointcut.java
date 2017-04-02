package com.compname.lob.aop;

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;

/**
 * Define system Point cuts
 * 
 * @author vegirl1
 * @since Sep 28, 2015
 * @version $Revision$
 */
@Aspect
public class SystemPointcut {

    @Pointcut("execution(* com.compname.lob.beans.DataSourceConfiguration.get*())")
    public void inDataSourceConfig() {
    }

    @Pointcut("within(com.compname.lob.service.impl..*)")
    public void inBusinessService() {
    }

}
