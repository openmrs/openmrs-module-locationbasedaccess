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

import org.openmrs.Location;
import org.openmrs.Person;
import org.openmrs.PersonAttribute;
import org.openmrs.PersonAttributeType;
import org.openmrs.User;
import org.openmrs.api.context.Context;
import java.util.List;

/**
 * It will be used to initialize the common implementations for the usage
 */
public class Helper {

    /**
     * Used to create the PersonAttribute with access location information
     * @param locationUuid location uuid for the access
     * @param person Person object
     * @return person attribute with access location information
     */
    public static PersonAttribute createLocationAttribute(String locationUuid, Person person) {
        PersonAttribute personAttribute = new PersonAttribute();
        personAttribute.setAttributeType(Context.getPersonService().getPersonAttributeTypeByName(Constants.ACCESS_LOCATION_PERSON_ATTRIBUTE_TYPE_NAME));
        personAttribute.setValue(locationUuid);
        personAttribute.setPerson(person);
        return personAttribute;
    }

    /**
     * Used to get the default access location if the logged in user hasn't access location information
     * @return Location object
     */
    public static Location getDefaultAccessLocation() {
        List<Location> allLocations = Context.getLocationService().getAllLocations();
        Location defaultAccessLocation;
        if (allLocations.size() > 0 ) {
            defaultAccessLocation = allLocations.get(1);
        }
        else {
            defaultAccessLocation = null;
        }
        return defaultAccessLocation;
    }

    /**
     * Used to retrieve the access location information from the logged in user
     * @return Location Object
     */
    public static  Location getAccessLocation() {
        User loggedInUser = Context.getAuthenticatedUser();
        PersonAttribute loggedInPersonAttribute = getAccessLocationPersonAttribute(loggedInUser.getPerson());
        if(loggedInPersonAttribute!=null) {
            String loggedInPersonAccessLocationUuid = loggedInPersonAttribute.getValue();
            Location loggedInLocation = Context.getLocationService().getLocationByUuid(loggedInPersonAccessLocationUuid);
            return loggedInLocation;
        }
        else {
            Location defaultLocation = Helper.getDefaultAccessLocation();
            return defaultLocation;
        }
    }

    /**
     * Used to create the PersonAttributeType for Access Location
     */
    public static  void createAccessLocationPersonAttributeType() {
        if (Context.getPersonService().getPersonAttributeTypeByName(Constants.ACCESS_LOCATION_PERSON_ATTRIBUTE_TYPE_NAME) == null) {
            PersonAttributeType personAttributeType = new PersonAttributeType();
            personAttributeType.setName(Constants.ACCESS_LOCATION_PERSON_ATTRIBUTE_TYPE_NAME);
            personAttributeType.setFormat("java.lang.String");
            personAttributeType.setDescription("Given Access location for the person");
            Context.getPersonService().savePersonAttributeType(personAttributeType);
        }
    }

    /**
     * Used to get the accessLocation person attribute from the person
     * @param person Required Person Object
     * @return PersonAttribute
     */
    public static PersonAttribute getAccessLocationPersonAttribute(Person person) {
        return person.getAttribute(Constants.ACCESS_LOCATION_PERSON_ATTRIBUTE_TYPE_NAME);
    }
}
