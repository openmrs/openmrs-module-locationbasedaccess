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
import org.openmrs.module.locationbasedaccess.aop.interceptor.EncounterServiceInterceptorAdvice;
import org.springframework.aop.Advisor;
import org.springframework.aop.support.StaticMethodMatcherPointcutAdvisor;
import java.lang.reflect.Method;

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
        return new EncounterServiceInterceptorAdvice();
    }

}
