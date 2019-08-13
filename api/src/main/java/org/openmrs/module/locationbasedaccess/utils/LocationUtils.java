/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 * <p>
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */

package org.openmrs.module.locationbasedaccess.utils;

import org.apache.commons.lang3.StringUtils;
import org.openmrs.Location;
import org.openmrs.Person;
import org.openmrs.PersonAttribute;
import org.openmrs.PersonAttributeType;
import org.openmrs.User;
import org.openmrs.api.context.Context;
import org.openmrs.module.locationbasedaccess.LocationBasedAccessConstants;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class LocationUtils {

    public static Location getPersonLocation(Person person) {
        String locationAttributeUuid = Context.getAdministrationService().getGlobalProperty(LocationBasedAccessConstants.LOCATION_ATTRIBUTE_GLOBAL_PROPERTY_NAME);
        if (StringUtils.isNotBlank(locationAttributeUuid)) {
            final PersonAttributeType personAttributeType = Context.getPersonService().getPersonAttributeTypeByUuid(locationAttributeUuid);
            PersonAttribute personAttribute = person.getAttribute(personAttributeType);
            if (personAttribute != null) {
                Location personLocation = Context.getLocationService().getLocationByUuid(personAttribute.getValue());
                return personLocation;
            }
        }
        return null;
    }

    public static Boolean doesPersonBelongToGivenLocations(Person person, PersonAttributeType personAttributeType, List<String> sessionLocationsLists) {
        PersonAttribute personAttribute = person.getAttribute(personAttributeType);
        return (personAttribute != null && sessionLocationsLists.contains(personAttribute.getValue()));
    }

    public static Boolean doesUserBelongToGivenLocations(User user, List<String> sessionLocationsLists){
        List<String> accessibleLocationsList = getUserAccessibleLocationUuids(user);
        if(accessibleLocationsList != null ){
            for(String location : accessibleLocationsList ){
                if(!StringUtils.isBlank(location) && sessionLocationsLists.contains(location)){
                    return true;
                }
            }
        }
        return false ;
    }

    public static Boolean doesUsersForPersonBelongToGivenLocations(Person person, List<String> sessionLocationsLists) {
        List<User> userList = Context.getUserService().getUsersByPerson(person, false);
        for (Iterator<User> iterator = userList.iterator(); iterator.hasNext(); ) {
            if(LocationUtils.doesUserBelongToGivenLocations(iterator.next(), sessionLocationsLists)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Used to get the accessible locations for the user. It will first get from the user property. if the user
     * property is not available, then check for the session location. If both are not available then return null.
     * @param authenticatedUser Authenticated user
     * @return accessibleLocationUuids the list of accessible Locations uuid
     */
    public static List <String> getUserAccessibleLocationUuids(User authenticatedUser) {
        if (authenticatedUser == null) {
            return null;
        }
        List <String> accessibleLocationUuids ;
        String accessibleLocationUuid = authenticatedUser.getUserProperty(LocationBasedAccessConstants.LOCATION_USER_PROPERTY_NAME);
        if (StringUtils.isBlank(accessibleLocationUuid)) {
            Integer sessionLocationId = Context.getUserContext().getLocationId();
            if (sessionLocationId != null) {
                accessibleLocationUuid = Context.getLocationService().getLocation(sessionLocationId).getUuid();
            }
        }
        accessibleLocationUuids = Arrays.asList(accessibleLocationUuid.split(","));
        return accessibleLocationUuids;
    }

    public static void createPersonAttributeTypeForLocation() {
        if(null == Context.getPersonService().getPersonAttributeTypeByUuid(LocationBasedAccessConstants.PERSONATTRIBUTETYPE_UUID)){
            PersonAttributeType personAttributeType = new PersonAttributeType();
            personAttributeType.setName(LocationBasedAccessConstants.PERSONATTRIBUTETYPE_NAME);
            personAttributeType.setFormat(LocationBasedAccessConstants.PERSONATTRIBUTETYPE_FORMAT);
            personAttributeType.setUuid(LocationBasedAccessConstants.PERSONATTRIBUTETYPE_UUID);
            Context.getPersonService().savePersonAttributeType(personAttributeType);
        }
    }
    
}
