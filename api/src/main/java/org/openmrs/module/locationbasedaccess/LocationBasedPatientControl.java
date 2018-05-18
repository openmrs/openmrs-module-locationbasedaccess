package org.openmrs.module.locationbasedaccess;


import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.aopalliance.aop.Advice;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.openmrs.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.Advisor;
import org.springframework.aop.framework.ReflectiveMethodInvocation;
import org.springframework.aop.support.StaticMethodMatcherPointcutAdvisor;
import org.openmrs.api.context.Context;

public class LocationBasedPatientControl extends StaticMethodMatcherPointcutAdvisor implements Advisor {


    private Logger log = LoggerFactory.getLogger(this.getClass());

    public boolean matches(Method method, Class targetClass) {
        // use the savePatient Method from patientServiceImpl
        if (method.getName().equals("savePatient"))
            return true;

        return false;
    }

    @Override
    public Advice getAdvice() {
        log.info("Getting new around advice");
        return new LocationBasedPatientControlAdvice();
    }

    private Location getDefaultAccessLocation() {
        List<Location> allLocations = Context.getLocationService().getAllLocations();
        Location defaultAccessLocation;
        if (allLocations.size() > 0 ) {
            defaultAccessLocation = allLocations.get(0);
            log.info("Default location for the location based access context has been added (Name : " + defaultAccessLocation.getName() + ")");
        }
        else {
            defaultAccessLocation = null;
            log.info("Could not add the default location for the location based access context");
        }
        return defaultAccessLocation;
    }

    private  PersonAttribute createLocationAttribute(String loggedInPersonAccessLocationUuid, Person person) {
        PersonAttribute personAttribute = new PersonAttribute();
        personAttribute.setAttributeType(Context.getPersonService().getPersonAttributeTypeByName("accessLocation"));
        personAttribute.setValue(loggedInPersonAccessLocationUuid);
        personAttribute.setPerson(person);
        return personAttribute;
    }

    private class LocationBasedPatientControlAdvice implements MethodInterceptor {
        public Object invoke(MethodInvocation invocation) throws Throwable {

            log.info("Before " + invocation.getMethod().getName() + ".");

            // get the method arguments of savePatient method in patientServiceImpl
            Object[] arguments = invocation.getArguments();
            Object patientObj = arguments[0];

            if(patientObj!=null) {
                // Convert the object to Patient
                Patient patient = (Patient)patientObj;
                Set<PersonAttribute> listOfPersonAttributes = patient.getPerson().getAttributes();

                // get current logged in user from the Context
                User loggedInUser = Context.getAuthenticatedUser();
                PersonAttribute loggedInPersonAttribute = loggedInUser.getPerson().getAttribute("accessLocation");
                if(loggedInPersonAttribute!=null) {
                    String loggedInPersonAccessLocationUuid = loggedInPersonAttribute.getValue();
                    Location loggedInLocation = Context.getLocationService().getLocationByUuid(loggedInPersonAccessLocationUuid);
                    log.info("Location Adding : Logged in user location UUID : " + loggedInPersonAccessLocationUuid);
                    log.info("Location Adding : Logged in user location Name : " + loggedInLocation.getName());

                    listOfPersonAttributes.add(createLocationAttribute(loggedInPersonAccessLocationUuid, patient.getPerson()));
                }
                else {
                    log.info("Location Adding : No Location found for logged in user");
                    // need to add default location

                    Location defaultLocation = getDefaultAccessLocation();
                    log.info("Location Adding : Logged in user location UUID : " + defaultLocation.getUuid());
                    log.info("Location Adding : Logged in user location Name : " + defaultLocation.getName());

                    if (defaultLocation!=null) {
                        listOfPersonAttributes.add(createLocationAttribute(defaultLocation.getUuid(), patient.getPerson()));
                    }

                }

                patient.getPerson().setAttributes(listOfPersonAttributes);

                ReflectiveMethodInvocation methodInvocation = (ReflectiveMethodInvocation) invocation;
                methodInvocation.setArguments(arguments);
                log.info("During " + invocation.getMethod().getName() + ". Patient Name : " + patient.getGivenName());
            }

            // the proceed() method does not have to be called
            Object obj = invocation.proceed();

            log.info("After " + invocation.getMethod().getName() + ".");

            return obj;
        }
    }

}