/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 * <p>
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */

package org.openmrs.module.locationbasedaccess.aop;

import org.aopalliance.aop.Advice;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.User;
import org.openmrs.api.context.Context;
import org.openmrs.api.context.Daemon;
import org.openmrs.module.locationbasedaccess.utils.LocationUtils;
import org.springframework.aop.Advisor;
import org.springframework.aop.support.StaticMethodMatcherPointcutAdvisor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class UserSearchAdviser extends StaticMethodMatcherPointcutAdvisor implements Advisor {

    private static final Log log = LogFactory.getLog(UserSearchAdviser.class);

    @Override
    public boolean matches(Method method, Class targetClass) {
        if (method.getName().equals("getUsers")) {
            return true;
        }
        else if (method.getName().equals("getAllUsers")) {
            return true;
        }
        else if (method.getName().equals("getUser")) {
            return true;
        }
        else if (method.getName().equals("getUserByUuid")) {
            return true;
        }
        else if (method.getName().equals("getUserByUsername")) {
            return true;
        }
        else if (method.getName().equals("getUserByName")) {
            return true;
        }
        else if (method.getName().equals("getUsersByPerson")) {
            return true;
        }
        return false;
    }

    @Override
    public Advice getAdvice() {
        return new UserSearchAdvise();
    }

    private class UserSearchAdvise implements MethodInterceptor {
        public Object invoke(MethodInvocation invocation) throws Throwable {
            User authenticatedUser = Context.getAuthenticatedUser();
            if (authenticatedUser == null) {
                return null;
            }
            Object object = invocation.proceed();
            if (Daemon.isDaemonUser(authenticatedUser) || authenticatedUser.isSuperUser()) {
                return object;
            }
            String accessibleLocationUuid = LocationUtils.getUserAccessibleLocationUuid(authenticatedUser);
            if (accessibleLocationUuid != null) {
                if(object instanceof List) {
                    List<User> userList = (List<User>) object;
                    for (Iterator<User> iterator = userList.iterator(); iterator.hasNext(); ) {
                        User user = iterator.next();
                        if (!LocationUtils.doesUserBelongToGivenLocation(user, accessibleLocationUuid)) {
                            if (!authenticatedUser.getUuid().equals(user.getUuid())) {
                                iterator.remove();
                            }
                        }
                    }
                    object = userList;
                }
                else if(object instanceof User) {
                    User user = (User) object;
                    if (!LocationUtils.doesUserBelongToGivenLocation(user, accessibleLocationUuid)) {
                        if (!authenticatedUser.getUuid().equals(user.getUuid())) {
                            object = null;
                        }
                    }
                }
            }
            else {
                log.debug("Search User : Null Session Location in the UserContext");
                if(object instanceof User) {
                    // If the sessionLocationId is null, then return null for a User instance
                    return null;
                }
                else {
                    // If the sessionLocationId is null, then return a empty list
                    return new ArrayList<User>();
                }
            }
            return object;
        }
    }
}