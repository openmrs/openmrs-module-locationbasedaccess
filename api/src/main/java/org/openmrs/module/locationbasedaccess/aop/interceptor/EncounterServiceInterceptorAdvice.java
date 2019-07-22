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
import org.openmrs.Encounter;
import org.openmrs.Location;
import org.openmrs.User;
import org.openmrs.api.context.Context;
import org.openmrs.api.context.Daemon;
import org.openmrs.module.locationbasedaccess.LocationBasedAccessConstants;
import org.openmrs.module.locationbasedaccess.utils.LocationUtils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class EncounterServiceInterceptorAdvice implements MethodInterceptor {

    private static final Log log = LogFactory.getLog(EncounterServiceInterceptorAdvice.class);

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
            if (object instanceof List) {
                List<Encounter> encounterList = (List<Encounter>) object;
                object = removeEncountersIfNotBelongToGivenLocation(encounterList, accessibleLocationUuid);
            } else if (object instanceof Map) {
                Map<Integer, List<Encounter>> encounterMap = (Map<Integer, List<Encounter>>) object;
                Iterator<Map.Entry<Integer, List<Encounter>>> mapIterator = encounterMap.entrySet().iterator();
                while (mapIterator.hasNext()) {
                    Map.Entry<Integer, List<Encounter>> entry = mapIterator.next();
                    List<Encounter> encounterList = entry.getValue();
                    entry.setValue(removeEncountersIfNotBelongToGivenLocation(encounterList, accessibleLocationUuid));
                    //TODO: remove the entry from the map, if the encounter list is empty and update the map index
                }
                object = encounterMap;
            } else if (object instanceof Encounter) {
                if (!doesEncounterBelongToGivenLocation((Encounter) object, accessibleLocationUuid)) {
                    object = null;
                }
            }
        } else {
            log.debug("Search Encounter : Null Session Location in the UserContext");
            if (object instanceof Encounter) {
                // If the sessionLocationId is null, then return null for a Encounter instance
                return null;
            } else if (object instanceof Map) {
                // If the sessionLocationId is null, then return a empty map
                return new HashMap<Integer, List<Encounter>>();
            } else {
                // If the sessionLocationId is null, then return a empty list
                return new ArrayList<Encounter>();
            }
        }
        return object;
    }

    private List<Encounter> removeEncountersIfNotBelongToGivenLocation(List<Encounter> encounterList, String sessionLocationUuid) {
        for (Iterator<Encounter> iterator = encounterList.iterator(); iterator.hasNext(); ) {
            Encounter thisEncounter = iterator.next();
            if(!doesEncounterBelongToGivenLocation(thisEncounter, sessionLocationUuid)) {
                iterator.remove();
            }
        }
        return encounterList;
    }

    public static Boolean doesEncounterBelongToGivenLocation(Encounter encounter, String sessionLocationUuid) {
        Location location = encounter.getLocation();
        String encounterAccessType = Context.getAdministrationService().getGlobalProperty(LocationBasedAccessConstants.ENCOUNTER_RESTRICTION_TYPE_ENCOUNTER_LOCATION);
        if(LocationBasedAccessConstants.ENCOUNTER_RESTRICTION_TYPE_PATIENT_LOCATION ==encounterAccessType) {
            location = LocationUtils.getPersonLocation(encounter.getPatient());
        }
        return (location != null && LocationUtils.compare(location.getUuid(), sessionLocationUuid));
    }
}
