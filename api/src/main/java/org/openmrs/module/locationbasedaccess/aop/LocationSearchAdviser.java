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
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Location;
import org.openmrs.User;
import org.openmrs.api.context.Context;
import org.openmrs.api.context.Daemon;
import org.openmrs.module.locationbasedaccess.LocationBasedAccessConstants;
import org.openmrs.module.locationbasedaccess.utils.LocationUtils;
import org.springframework.aop.Advisor;
import org.springframework.aop.support.StaticMethodMatcherPointcutAdvisor;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.ArrayList;

public class LocationSearchAdviser extends StaticMethodMatcherPointcutAdvisor implements Advisor {

    private static final Log log = LogFactory.getLog(LocationSearchAdviser.class);
    Set<String> restrictedGetMethodNames = new HashSet<String>();

    public LocationSearchAdviser() {
        restrictedGetMethodNames.add("getDefaultLocation");
        restrictedGetMethodNames.add("getDefaultLocationFromSting");
        restrictedGetMethodNames.add("getLocationByUuid");
        restrictedGetMethodNames.add("getAllLocations");
        restrictedGetMethodNames.add("getLocations");
        restrictedGetMethodNames.add("getLocationsByTag");
        restrictedGetMethodNames.add("getRootLocations");
        // TODO : How to address the restrictions for getLocation() method
    }

    @Override
    public boolean matches(Method method, Class targetClass) {
        if(restrictedGetMethodNames.contains(method.getName())) {
            return true;
        }
        return false;
    }

    @Override
    public Advice getAdvice() {
        return new LocationSearchAdvise();
    }

    private class LocationSearchAdvise implements MethodInterceptor {
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
                String accessibleLocationUuid = authenticatedUser.getUserProperty(LocationBasedAccessConstants.LOCATION_USER_PROPERTY_NAME);
                if (StringUtils.isBlank(accessibleLocationUuid)) {
                    Integer sessionLocationId = Context.getUserContext().getLocationId();
                    accessibleLocationUuid = Context.getLocationService().getLocation(sessionLocationId).getUuid();
                }

                if (accessibleLocationUuid != null) {
                    if(object instanceof List) {
                        List<Location> locationList = (List<Location>) object;
                        for (Iterator<Location> iterator = locationList.iterator(); iterator.hasNext(); ) {
                            if(!LocationUtils.compare(accessibleLocationUuid, iterator.next().getUuid())) {
                                iterator.remove();
                            }
                        }
                        object = locationList;
                    }
                    else if(object instanceof Location) {
                        if(!LocationUtils.compare(accessibleLocationUuid, ((Location)object).getUuid())) {
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
}