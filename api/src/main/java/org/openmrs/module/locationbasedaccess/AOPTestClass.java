package org.openmrs.module.locationbasedaccess;

import java.lang.reflect.Method;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.AfterReturningAdvice;

public class AOPTestClass implements AfterReturningAdvice {

    private Logger log = LoggerFactory.getLogger(this.getClass());

    private int count1 = 0;
    private int count2 = 0;
    private int count3 = 0;

    public void afterReturning(Object returnValue, Method method, Object[] args, Object target) throws Throwable {
        if (method.getName().equals("getPatient")) {
            log.info("Method: " + method.getName() + ". After advice called " + (++count1) + " time(s) now.");
        }

        if (method.getName().equals("getPatients")) {
            log.info("Method: " + method.getName() + ". After advice called " + (++count2) + " time(s) now.");
        }

        if (method.getName().equals("getAllPatients")) {
            log.info("Method: " + method.getName() + ". After advice called " + (++count3) + " time(s) now.");
        }
    }

}