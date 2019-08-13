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
import org.openmrs.Patient;
import org.openmrs.PersonAttributeType;
import org.openmrs.User;
import org.openmrs.api.context.Context;
import org.openmrs.api.context.Daemon;
import org.openmrs.module.locationbasedaccess.LocationBasedAccessConstants;
import org.openmrs.module.locationbasedaccess.utils.LocationUtils;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class PatientServiceInterceptorAdvice implements MethodInterceptor {

    private static final Log log = LogFactory.getLog(PatientServiceInterceptorAdvice.class);

    public Object invoke(MethodInvocation invocation) throws Throwable {
        User authenticatedUser = Context.getAuthenticatedUser();
        if (authenticatedUser == null) {
            return null;
        }
        Object object = invocation.proceed();
        String lbacRestriction = Context.getAdministrationService().getGlobalProperty(LocationBasedAccessConstants.PATIENT_RESTRICTION_GLOBAL_PROPERTY_NAME);
        if (Daemon.isDaemonUser(authenticatedUser) || authenticatedUser.isSuperUser() || !(lbacRestriction.equals("true"))) {
            return object;
        }

        List<String> accessibleLocationUuids = LocationUtils.getUserAccessibleLocationUuids(authenticatedUser);
        String locationAttributeUuid = Context.getAdministrationService().getGlobalProperty(LocationBasedAccessConstants.LOCATION_ATTRIBUTE_GLOBAL_PROPERTY_NAME);
        if (StringUtils.isNotBlank(locationAttributeUuid)) {
            final PersonAttributeType personAttributeType = Context.getPersonService().getPersonAttributeTypeByUuid(locationAttributeUuid);
            if (accessibleLocationUuids != null) {
                if(object instanceof List) {
                    List<Patient> patientList = (List<Patient>) object;
                    for (Iterator<Patient> iterator = patientList.iterator(); iterator.hasNext(); ) {
                        if(!LocationUtils.doesPersonBelongToGivenLocations(iterator.next().getPerson(), personAttributeType, accessibleLocationUuids)) {
                            iterator.remove();
                        }
                    }
                    object = patientList;
                }
                else if(object instanceof Patient) {
                    if(!LocationUtils.doesPersonBelongToGivenLocations(((Patient)object).getPerson(), personAttributeType, accessibleLocationUuids)) {
                        object = null;
                    }
                }
            } else {
                log.debug("Search Patient : Null Session Location in the UserContext");
                if(object instanceof Patient) {
                    // If the sessionLocationId is null, then return null for a Patient instance
                    return null;
                }
                else {
                    // If the sessionLocationId is null, then return a empty list
                    return new ArrayList<Patient>();
                }
            }
        }
        return object;
    }
}
