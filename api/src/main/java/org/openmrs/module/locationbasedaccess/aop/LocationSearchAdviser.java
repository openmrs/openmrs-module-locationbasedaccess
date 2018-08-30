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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.module.locationbasedaccess.aop.interceptor.LocationServiceInterceptorAdvice;
import org.springframework.aop.Advisor;
import org.springframework.aop.support.StaticMethodMatcherPointcutAdvisor;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

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
        return new LocationServiceInterceptorAdvice(restrictedGetMethodNames);
    }
}