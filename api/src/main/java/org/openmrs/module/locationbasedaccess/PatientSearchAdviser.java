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
import org.openmrs.Role;
import org.openmrs.api.context.Context;
import org.springframework.aop.Advisor;
import org.springframework.aop.support.StaticMethodMatcherPointcutAdvisor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class PatientSearchAdviser extends StaticMethodMatcherPointcutAdvisor implements Advisor {

    private static final Log log = LogFactory.getLog(PatientSearchAdviser.class);

    @Override
    public boolean matches(Method method, Class targetClass) {
        if (method.getName().equals("getPatients")) {
            return true;
        }
        else if (method.getName().equals("getPatient")) {
            return true;
        }
        else if (method.getName().equals("getPatientByUuid")) {
            return true;
        }
        return false;
    }

    @Override
    public Advice getAdvice() {
        return new PatientSearchAdvise();
    }

    private class PatientSearchAdvise implements MethodInterceptor {
        public synchronized Object invoke(MethodInvocation invocation) throws Throwable {
            log.error("Method Name : " + invocation.getMethod().getName());
            Integer sessionLocationId = Context.getUserContext().getLocationId();
            if(Context.getUserContext() == null) {
                 log.error("UserContext : null");
            }
            else {
                log.error("UserContext : Not null");
                log.error("UserContext : " + Context.getUserContext().getLocation());
                log.error("UserContext : " + Context.getUserContext().getAuthenticatedUser());
                log.error("UserContext : " + Context.getUserContext().getAllRoles().size());
                for(Role role : Context.getUserContext().getAllRoles()) {
                    log.error("UserContext role : " + role);
                }
            }

            String locationAttributeUuid = Context.getAdministrationService().getGlobalProperty(LocationBasedAccessConstants.LOCATION_ATTRIBUTE_GLOBAL_PROPERTY_NAME);
            Object object = invocation.proceed();
            if (StringUtils.isNotBlank(locationAttributeUuid)) {
                final PersonAttributeType personAttributeType = Context.getPersonService().getPersonAttributeTypeByUuid(locationAttributeUuid);
                if (sessionLocationId != null) {
                    String sessionLocationUuid = Context.getLocationService().getLocation(sessionLocationId).getUuid();
                    if(object instanceof List) {
                        List<Patient> patientList = (List<Patient>) object;
                        log.error("Patient List : " + patientList.size());
                        for (Iterator<Patient> iterator = patientList.iterator(); iterator.hasNext(); ) {
                            if(!doesPatientBelongToGivenLocation(iterator.next(), personAttributeType, sessionLocationUuid)) {
                                iterator.remove();
                            }
                        }
                        object = patientList;
                        log.error("Patient List : " + patientList.size());
                    }
                    else if(object instanceof Patient) {
                        log.error("Patient : " + ((Patient)object).getFamilyName());
                        if(!doesPatientBelongToGivenLocation((Patient)object, personAttributeType, sessionLocationUuid)) {
                            object = null;
                            log.error("Patient : removed");
                        }
                    }
                } else {
                    log.debug("Search Patient : Null Session Location in the UserContext");
                    if(object instanceof Patient) {
                        // If the sessionLocationId is null, then return null for a Patient instance
                        log.error("Patient : " + ((Patient)object).getFamilyName());
                        return null;
                    }
                    else {
                        // If the sessionLocationId is null, then return a empty list
                        log.error("Patient List : Null");
                        return new ArrayList<Patient>();
                    }
                }
            }
            return object;
        }

        private Boolean doesPatientBelongToGivenLocation(Patient patient, PersonAttributeType personAttributeType, String sessionLocationUuid) {
            PersonAttribute personAttribute = patient.getAttribute(personAttributeType);
            return ((personAttribute == null && Context.getAuthenticatedUser().isSuperUser()) ||
                    personAttribute != null && compare(personAttribute.getValue(), sessionLocationUuid));
        }

        private Boolean compare(String value1, String value2) {
            return (StringUtils.isNotBlank(value1) && StringUtils.isNotBlank(value2)) && value1.equals(value2);
        }
    }
}
