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
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Encounter;
import org.openmrs.Location;
import org.openmrs.api.context.Context;
import org.openmrs.api.context.Daemon;
import org.springframework.aop.Advisor;
import org.springframework.aop.support.StaticMethodMatcherPointcutAdvisor;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class EncounterSearchAdviser extends StaticMethodMatcherPointcutAdvisor implements Advisor {

    private static final Log log = LogFactory.getLog(EncounterSearchAdviser.class);

    @Override
    public boolean matches(Method method, Class targetClass) {
        if (method.getName().equals("getEncounter")) {
            return true;
        }
        else if (method.getName().equals("getEncounterByUuid")) {
            return true;
        }
        else if (method.getName().equals("getEncounters")) {
            return true;
        }
        else if (method.getName().equals("getEncountersByPatientId")) {
            return true;
        }
        else if (method.getName().equals("getEncountersByPatient")) {
            return true;
        }
        else if (method.getName().equals("getEncountersByVisit")) {
            return true;
        }
        else if (method.getName().equals("getEncountersNotAssignedToAnyVisit")) {
            return true;
        }
        else if (method.getName().equals("getEncountersByVisitsAndPatient")) {
            return true;
        }
        else if (method.getName().equals("getAllEncounters")) {
            return true;
        }
        return false;
    }

    @Override
    public Advice getAdvice() {
        return new EncounterSearchAdvise();
    }

    private class EncounterSearchAdvise implements MethodInterceptor {
        public Object invoke(MethodInvocation invocation) throws Throwable {
            if (Context.getAuthenticatedUser() == null) {
                return null;
            }

            Object object = invocation.proceed();
            if (Daemon.isDaemonUser(Context.getAuthenticatedUser()) || Context.getAuthenticatedUser().isSuperUser()) {
                return object;
            }

            Integer sessionLocationId = Context.getUserContext().getLocationId();
            if (sessionLocationId != null) {
                String sessionLocationUuid = Context.getLocationService().getLocation(sessionLocationId).getUuid();

                if (object instanceof List) {
                    List<Encounter> encounterList = (List<Encounter>) object;
                    object = removeEncountersIfNotBelongToGivenLocation(encounterList, sessionLocationUuid);
                } else if (object instanceof Map) {
                    Map<Integer, List<Encounter>> encounterMap = (Map<Integer, List<Encounter>>) object;
                    Iterator<Map.Entry<Integer, List<Encounter>>> mapIterator = encounterMap.entrySet().iterator();
                    while (mapIterator.hasNext()) {
                        Map.Entry<Integer, List<Encounter>> entry = mapIterator.next();
                        List<Encounter> encounterList = entry.getValue();
                        entry.setValue(removeEncountersIfNotBelongToGivenLocation(encounterList, sessionLocationUuid));
                        //TODO: remove the entry from the map, if the encounter list is empty and update the map index
                    }
                    object = encounterMap;
                } else if (object instanceof Encounter) {
                    if (!doesEncounterBelongToGivenLocation((Encounter) object, sessionLocationUuid)) {
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
        return (location != null && LocationUtils.compare(location.getUuid(), sessionLocationUuid));
    }
}
