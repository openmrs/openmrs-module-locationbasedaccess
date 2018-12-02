package org.openmrs.module.locationbasedaccess.aop.common;

import org.aopalliance.intercept.MethodInterceptor;
import org.openmrs.api.OpenmrsService;

public interface TestWithAOP {

    /**
     * Sets an AOP method interceptor class to be active on the test case.
     *
     * @param interceptorClass Eg. "MyInterceptor.class"
     */
    void setInterceptor(Class<? extends MethodInterceptor> interceptorClass);

    /**
     * Sets an AOP method interceptor to be active on the test case.
     *
     * @param interceptor Eg. "MyInterceptor"
     */
    void setInterceptor(MethodInterceptor interceptor);

    /**
     * Hooks the OpenMRS Services
     *
     * @param serviceClass
     */
    void addService(Class<? extends OpenmrsService> serviceClass);
}
