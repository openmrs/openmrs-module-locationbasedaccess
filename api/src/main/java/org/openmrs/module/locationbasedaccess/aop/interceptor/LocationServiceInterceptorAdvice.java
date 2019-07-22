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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Location;
import org.openmrs.User;
import org.openmrs.api.context.Context;
import org.openmrs.api.context.Daemon;
import org.openmrs.module.locationbasedaccess.utils.LocationUtils;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;


public class LocationServiceInterceptorAdvice implements MethodInterceptor {

    private static final Log log = LogFactory.getLog(LocationServiceInterceptorAdvice.class);
    private Set<String> restrictedGetMethodNames;

    public LocationServiceInterceptorAdvice(Set<String> restrictedGetMethodNames) {
        this.restrictedGetMethodNames = restrictedGetMethodNames;
    }

    public Object invoke(MethodInvocation invocation) throws Throwable {
        Method method = invocation.getMethod();
        Object object = invocation.proceed();
        // Allow get methods without authentications
        if(!Context.isAuthenticated() && restrictedGetMethodNames.contains(method.getName())) {
            return object;
        }
        User authenticatedUser = Context.getAuthenticatedUser();
        if (authenticatedUser != null && (Daemon.isDaemonUser(authenticatedUser) || authenticatedUser.isSuperUser())) {
            return object;
        }

        if(restrictedGetMethodNames.contains(method.getName())) {
            List<String> accessibleLocationUuids = LocationUtils.getUserAccessibleLocationUuids(authenticatedUser);
            if (accessibleLocationUuids != null) {
                if(object instanceof List) {
                    List<Location> locationList = (List<Location>) object;
                    for (Iterator<Location> iterator = locationList.iterator(); iterator.hasNext(); ) {
                        if(!accessibleLocationUuids.contains(iterator.next().getUuid())) {
                            iterator.remove();
                        }
                    }
                    object = locationList;
                }
                else if(object instanceof Location) {
                    if(!accessibleLocationUuids.contains(((Location)object).getUuid())) {
                        object = null;
                    }
                }
            }
            else {
                log.debug("Search Location : Null Session Location in the UserContext");
                if(object instanceof List) {
                    // If the sessionLocationId is null, then return a empty list
                    return new ArrayList<Location>();
                }
            }
        }
        return object;
    }
}
