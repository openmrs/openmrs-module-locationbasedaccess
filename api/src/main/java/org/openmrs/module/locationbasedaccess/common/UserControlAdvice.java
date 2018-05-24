/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.locationbasedaccess.common;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.openmrs.Patient;
import org.openmrs.Person;
import org.openmrs.User;
import org.openmrs.PersonAttribute;
import org.springframework.aop.framework.ReflectiveMethodInvocation;
import java.util.Set;

public class UserControlAdvice implements MethodInterceptor {

    private String ObjectName;
    private int PersonObjIdInArgs;

    public UserControlAdvice(String ObjectName) {
        this.ObjectName = ObjectName;
        this.PersonObjIdInArgs = 0;
    }

    private Person getPersonFromObject(Object obj) {
        if(this.ObjectName == Constants.PATIENT_OBJ_NAME) {
            Patient patient = (Patient)obj;
            return patient.getPerson();
        }
        else if(this.ObjectName == Constants.USER_OBJ_NAME) {
            User user = (User)obj;
            return user.getPerson();
        }
        else {
            return null;
        }
    }

    public Object invoke(MethodInvocation invocation) throws Throwable {
        // get the method arguments of savePatient method in patientServiceImpl
        Object[] arguments = invocation.getArguments();
        Object Obj = arguments[PersonObjIdInArgs];

        if(Obj!=null) {
            // Get the person object
            Person person = getPersonFromObject(Obj);
            if(Helper.getAccessLocationPersonAttribute(person) == null) {
                // Person without exiting accessLocation Attribute
                Set<PersonAttribute> listOfPersonAttributes = person.getAttributes();
                listOfPersonAttributes.add(Helper.createLocationAttribute(Helper.getAccessLocation().getUuid(), person));
                person.setAttributes(listOfPersonAttributes);
            }
            else {
                // Person with exiting accessLocation Attribute
                PersonAttribute personAttribute = person.getAttribute(Constants.ACCESS_LOCATION_PERSON_ATTRIBUTE_TYPE_NAME);
                personAttribute.setValue(Helper.getAccessLocation().getUuid());
            }
            ReflectiveMethodInvocation methodInvocation = (ReflectiveMethodInvocation) invocation;
            methodInvocation.setArguments(arguments);
        }
        // the proceed() method does not have to be called
        Object obj = invocation.proceed();
        return obj;
    }
}
