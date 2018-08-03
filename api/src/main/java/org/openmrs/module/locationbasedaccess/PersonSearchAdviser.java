/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 * <p>
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */

package org.openmrs.module.locationbasedaccess;

import org.aopalliance.aop.Advice;
import org.aopalliance.intercept.MethodInvocation;
import org.aopalliance.intercept.MethodInterceptor;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Person;
import org.openmrs.PersonAttributeType;
import org.openmrs.api.context.Context;
import org.openmrs.api.context.Daemon;
import org.springframework.aop.Advisor;
import org.springframework.aop.support.StaticMethodMatcherPointcutAdvisor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class PersonSearchAdviser extends StaticMethodMatcherPointcutAdvisor implements Advisor {

    private static final Log log = LogFactory.getLog(PersonSearchAdviser.class);

    @Override
    public boolean matches(Method method, Class targetClass) {
        if (method.getName().equals("getPeople")) {
            return true;
        }
        else if (method.getName().equals("getPerson")) {
            return true;
        }
        else if (method.getName().equals("getPersonByUuid")) {
            return true;
        }
        else if (method.getName().equals("getSimilarPeople")) {
            return true;
        }
        return false;
    }

    @Override
    public Advice getAdvice() {
        return new PersonSearchAdvise();
    }

    private class PersonSearchAdvise implements MethodInterceptor {
        public Object invoke(MethodInvocation invocation) throws Throwable {
            if (Context.getAuthenticatedUser() == null) {
                return null;
            }
            Object object = invocation.proceed();
            if (Daemon.isDaemonUser(Context.getAuthenticatedUser()) || Context.getAuthenticatedUser().isSuperUser()) {
                return object;
            }
            Integer sessionLocationId = Context.getUserContext().getLocationId();
            String locationAttributeUuid = Context.getAdministrationService().getGlobalProperty(LocationBasedAccessConstants.LOCATION_ATTRIBUTE_GLOBAL_PROPERTY_NAME);
            if (StringUtils.isNotBlank(locationAttributeUuid)) {
                final PersonAttributeType personAttributeType = Context.getPersonService().getPersonAttributeTypeByUuid(locationAttributeUuid);
                if (sessionLocationId != null) {
                    String sessionLocationUuid = Context.getLocationService().getLocation(sessionLocationId).getUuid();
                    if(object instanceof List) {
                        List<Person> personList = (List<Person>) object;
                        for (Iterator<Person> iterator = personList.iterator(); iterator.hasNext(); ) {
                            Person thisPerson = iterator.next();
                            if(!LocationUtils.doesPersonBelongToGivenLocation(thisPerson, personAttributeType, sessionLocationUuid)) {
                                if(!LocationUtils.doesUsersForPersonBelongToGivenLocation(thisPerson, sessionLocationUuid)) {
                                    iterator.remove();
                                }
                            }
                        }
                        object = personList;
                    }
                    else if(object instanceof Person) {
                        if(!LocationUtils.doesPersonBelongToGivenLocation((Person)object, personAttributeType, sessionLocationUuid)) {
                            if(!LocationUtils.doesUsersForPersonBelongToGivenLocation((Person)object, sessionLocationUuid)) {
                                object = null;
                            }
                        }
                    }
                } else {
                    log.debug("Search Person : Null Session Location in the UserContext");
                    if(object instanceof Person) {
                        // If the sessionLocationId is null, then return null for a Person instance
                        return null;
                    }
                    else {
                        // If the sessionLocationId is null, then return a empty list
                        return new ArrayList<Person>();
                    }
                }
            }
            return object;
        }
    }
}
