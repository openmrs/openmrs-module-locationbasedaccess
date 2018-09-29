package org.openmrs.module.locationbasedaccess.aop.common;

import java.util.HashMap;
import java.util.Map;
import org.aopalliance.aop.Advice;
import org.aopalliance.intercept.MethodInterceptor;
import org.junit.After;
import org.junit.Before;
import org.openmrs.api.OpenmrsService;
import org.openmrs.api.context.Context;
import org.openmrs.module.AdvicePoint;
import org.openmrs.test.BaseModuleContextSensitiveTest;

public abstract class AOPContextSensitiveTest extends BaseModuleContextSensitiveTest implements TestWithAOP {

    private Class<?> interceptorClass;

    private Map<Class<?>, Advice> servicesMap = new HashMap<Class<?>, Advice>();

    public void setInterceptor(Class<? extends MethodInterceptor> interceptorClass) {
        this.interceptorClass = interceptorClass;
    }

    public void addService(Class<? extends OpenmrsService> serviceClass) {
        servicesMap.put(serviceClass, null);
    }

    abstract protected void setInterceptorAndServices(TestWithAOP testCase);

    @Before
    public void setupAOP() throws Exception {

        setInterceptorAndServices(this);

        for (Class<?> serviceClass : servicesMap.keySet()) {
            Advice advice = (Advice) (new AdvicePoint(serviceClass.getCanonicalName(), Context.loadClass(interceptorClass
                    .getCanonicalName()))).getClassInstance();
            servicesMap.put(serviceClass, advice);
            Context.addAdvice(Context.loadClass(serviceClass.getCanonicalName()), advice);
        }
    }

    @After
    public void tearDownAOP() throws Exception {
        for (Class<?> serviceClass : servicesMap.keySet()) {
            Context.removeAdvice(Context.loadClass(serviceClass.getCanonicalName()), servicesMap.get(serviceClass));
        }
    }

}
