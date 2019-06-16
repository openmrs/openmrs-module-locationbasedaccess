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

    public static Boolean doesPersonBelongToGivenLocation(Person person, PersonAttributeType personAttributeType, String sessionLocationUuid) {
        PersonAttribute personAttribute = person.getAttribute(personAttributeType);
        return (personAttribute != null && compare(personAttribute.getValue(), sessionLocationUuid));
    }

    public static Boolean doesUserBelongToGivenLocation(User user, String sessionLocationUuid) {
        String userLocationProperty = user.getUserProperty(LocationBasedAccessConstants.LOCATION_USER_PROPERTY_NAME);
        return (userLocationProperty != null && compare(userLocationProperty, sessionLocationUuid));
    }

    public static Boolean doesUsersForPersonBelongToGivenLocation(Person person, String sessionLocationUuid) {
        List<User> userList = Context.getUserService().getUsersByPerson(person, false);
        for (Iterator<User> iterator = userList.iterator(); iterator.hasNext(); ) {
            if(LocationUtils.doesUserBelongToGivenLocation(iterator.next(), sessionLocationUuid)) {
                return true;
            }
        }
        return false;
    }

    public static Boolean compare(String value1, String value2) {
        return (StringUtils.isNotBlank(value1) && StringUtils.isNotBlank(value2)) && value1.equals(value2);
    }

    /**
     * Used to get the accessible location for the user. It will first get from the user property. if the user
     * property is not available, then check for the session location. If both are not available then return null.
     * @param authenticatedUser Authenticated user
     * @return accessible Location uuid
     */
    public static String getUserAccessibleLocationUuid(User authenticatedUser) {
        if (authenticatedUser == null) {
            return null;
        }
        String accessibleLocationUuid = authenticatedUser.getUserProperty(LocationBasedAccessConstants.LOCATION_USER_PROPERTY_NAME);
        if (StringUtils.isBlank(accessibleLocationUuid)) {
            Integer sessionLocationId = Context.getUserContext().getLocationId();
            if (sessionLocationId != null) {
                accessibleLocationUuid = Context.getLocationService().getLocation(sessionLocationId).getUuid();
            }
        }
        return accessibleLocationUuid;
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
