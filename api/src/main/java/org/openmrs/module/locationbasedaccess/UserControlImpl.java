package org.openmrs.module.locationbasedaccess;

import org.aopalliance.aop.Advice;
import org.openmrs.module.locationbasedaccess.common.Constants;
import org.openmrs.module.locationbasedaccess.common.UserControlAdvice;
import org.springframework.aop.Advisor;
import org.springframework.aop.support.StaticMethodMatcherPointcutAdvisor;
import java.lang.reflect.Method;

/**
 * It will used to implement the AOP Methods for UserServiceImpl class
 */
public class UserControlImpl extends StaticMethodMatcherPointcutAdvisor implements AOPService,Advisor {

    @Override
    public boolean matches(Method method, Class targetClass) {
        // For UserServiceImpl.saveUser()
        if (method.getName().equals(Constants.SAVE_USER_METHOD_NAME)) {
            return true;
        }
        // For UserServiceImpl.createUser()
        else if (method.getName().equals(Constants.CREATE_USER_METHOD_NAME)) {
            return true;
        }
        return false;
    }

    @Override
    public Advice getAdvice() {
        return new UserControlAdvice(Constants.USER_OBJ_NAME);
    }

}
