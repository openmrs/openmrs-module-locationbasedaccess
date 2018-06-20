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
import org.openmrs.Patient;
import org.openmrs.PersonAttribute;
import org.openmrs.PersonAttributeType;
import org.openmrs.api.context.Context;
import org.springframework.aop.Advisor;
import org.springframework.aop.support.StaticMethodMatcherPointcutAdvisor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class PatientSearchAdviser extends StaticMethodMatcherPointcutAdvisor implements Advisor {

    private static final Log log = LogFactory.getLog(PatientSearchAdviser.class);
    public static final  String LOCATION_ATTRIBUTE_GLOBAL_PROPERTY_NAME = "locationbasedaccess.locationAttributeUuid";

    @Override
    public boolean matches(Method method, Class targetClass) {
        if (method.getName().equals("getPatients")) {
            return true;
        }
        return false;
    }

    @Override
    public Advice getAdvice() {
        return new PatientSearchAdvise();
    }

    private class PatientSearchAdvise implements MethodInterceptor {

        public Object invoke(MethodInvocation invocation) throws Throwable {
            Integer sessionLocationId = Context.getUserContext().getLocationId();
            String locationAttributeUuid = Context.getAdministrationService().getGlobalProperty(LOCATION_ATTRIBUTE_GLOBAL_PROPERTY_NAME);
            List<Patient> patientList = (List<Patient>) invocation.proceed();
            if (StringUtils.isNotBlank(locationAttributeUuid)) {
                final PersonAttributeType personAttributeType = Context.getPersonService().getPersonAttributeTypeByUuid(locationAttributeUuid);
                if (sessionLocationId != null) {
                    String sessionLocationUuid = Context.getLocationService().getLocation(sessionLocationId).getUuid();

                    for (Iterator<Patient> iterator = patientList.iterator(); iterator.hasNext(); ) {
                        Patient patient = iterator.next();
                        PersonAttribute personAttribute = patient.getAttribute(personAttributeType);
                        if ((personAttribute == null && !Context.getAuthenticatedUser().isSuperUser()) ||
                            personAttribute !=null && !compare(personAttribute.getValue(), sessionLocationUuid)) {
                            iterator.remove();
                        }
                    }
                } else {
                    // If the sessionLocationId is null, then return a empty list
                    log.debug("Search Patient : Null Session Location in the UserContext");
                    return new ArrayList<Patient>();
                }
            }
            return patientList;
        }

        private Boolean compare(String value1, String value2) {
            return (StringUtils.isNotBlank(value1) && StringUtils.isNotBlank(value2)) && value1.equals(value2);
        }
    }
}
