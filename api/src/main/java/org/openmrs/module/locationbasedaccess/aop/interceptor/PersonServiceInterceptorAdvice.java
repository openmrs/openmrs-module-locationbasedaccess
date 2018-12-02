/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 * <p>
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.locationbasedaccess.aop.interceptor;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Person;
import org.openmrs.PersonAttributeType;
import org.openmrs.User;
import org.openmrs.api.context.Context;
import org.openmrs.api.context.Daemon;
import org.openmrs.module.locationbasedaccess.LocationBasedAccessConstants;
import org.openmrs.module.locationbasedaccess.utils.LocationUtils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class PersonServiceInterceptorAdvice  implements MethodInterceptor {

    private static final Log log = LogFactory.getLog(PersonServiceInterceptorAdvice.class);

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
        String locationAttributeUuid = Context.getAdministrationService().getGlobalProperty(LocationBasedAccessConstants.LOCATION_ATTRIBUTE_GLOBAL_PROPERTY_NAME);
        if (StringUtils.isNotBlank(locationAttributeUuid) && (invocation.getMethod().getName() != "getPersonAttributeTypeByUuid")) {
            final PersonAttributeType personAttributeType = Context.getPersonService().getPersonAttributeTypeByUuid(locationAttributeUuid);
            if (accessibleLocationUuid != null) {
                if(object instanceof List) {
                    List<Person> personList = (List<Person>) object;
                    for (Iterator<Person> iterator = personList.iterator(); iterator.hasNext(); ) {
                        Person thisPerson = iterator.next();
                        if (!LocationUtils.doesPersonBelongToGivenLocation(thisPerson, personAttributeType, accessibleLocationUuid)) {
                            if (!LocationUtils.doesUsersForPersonBelongToGivenLocation(thisPerson, accessibleLocationUuid)) {
                                if (!thisPerson.getUuid().equals(authenticatedUser.getPerson().getUuid())) {
                                    iterator.remove();
                                }
                            }
                        }
                    }
                    object = personList;
                }
                else if(object instanceof Person) {
                    Person thisPerson = (Person)object;
                    if (!LocationUtils.doesPersonBelongToGivenLocation(thisPerson, personAttributeType, accessibleLocationUuid)) {
                        if (!LocationUtils.doesUsersForPersonBelongToGivenLocation(thisPerson, accessibleLocationUuid)) {
                            if (!thisPerson.getUuid().equals(authenticatedUser.getPerson().getUuid())) {
                                object = null;
                            }
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
