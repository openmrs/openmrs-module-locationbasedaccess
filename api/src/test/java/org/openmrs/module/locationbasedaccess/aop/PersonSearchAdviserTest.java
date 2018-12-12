/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */

package org.openmrs.module.locationbasedaccess.aop;

import org.junit.Before;
import org.junit.Test;
import org.openmrs.Location;
import org.openmrs.Person;
import org.openmrs.User;
import org.openmrs.api.LocationService;
import org.openmrs.api.PersonService;
import org.openmrs.api.UserService;
import org.openmrs.api.context.Context;
import org.openmrs.module.locationbasedaccess.aop.common.AOPContextSensitiveTest;
import org.openmrs.module.locationbasedaccess.aop.common.TestWithAOP;
import org.openmrs.module.locationbasedaccess.aop.interceptor.PersonServiceInterceptorAdvice;
import java.util.List;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertEquals;

public class PersonSearchAdviserTest extends AOPContextSensitiveTest {

    private UserService userService;
    private PersonService personService;
    private LocationService locationService;
    private static final String XML_FILENAME_WITH_PERSON_DATA = "include/PatientTestData.xml";
    private static final String XML_FILENAME_WITH_ADMIN_USER_DATA = "include/AdminUserData.xml";

    /**
     * Configured values in the demo dataset
     **/
    private static final int DEMO_PERSON_ATTRIBUTE_TYPE_ID = 10;
    private static final int DEMO_LOCATION1_ID = 1;
    private static final int DEMO_LOCATION2_ID = 2;
    private static final int DEMO_USER1_ID = 6001;
    private static final int DEMO_USER2_ID = 6002;
    private static final int DEMO_PERSON1_ID = 2;
    private static final int DEMO_PERSON2_ID = 3;
    private static final int DEMO_PERSON3_ID = 4;
    private static final int DEMO_PATIENT_YEAR_OF_BIRTH = 1950;
    private static final String DEMO_PATIENT_GENDER = "M";
    private static final String DEMO_PATIENT_FIRST_NAME = "Patient";
    private static final String DEMO_PATIENT1_ID = "5631b434-78aa-102b-91a0-001e378eb67e";
    private static final String DEMO_PATIENT2_ID = "5631b434-78aa-102b-91a0-001e378eb68e";
    private static final String DEMO_PATIENT3_ID = "5631b434-78aa-102b-91a0-001e378eb69e";
    private static final String DEMO_LOCATION_USER_PROPERTY = "locationUuid";
    private static final String DEMO_ADMIN_USERNAME = "admin";
    private static final String DEMO_ADMIN_PASSWORD = "test";
    private static final String DEMO_NORMAL_USER_PASSWORD = "userServiceTest";

    @Override
    protected void setInterceptorAndServices(TestWithAOP testCase) {
        testCase.setInterceptor(PersonServiceInterceptorAdvice.class);
        testCase.addService(PersonService.class);
    }

    @Before
    public void setUp() throws Exception {
        userService = Context.getUserService();
        personService = Context.getPersonService();
        locationService = Context.getLocationService();
        executeDataSet(XML_FILENAME_WITH_ADMIN_USER_DATA);
        executeDataSet(XML_FILENAME_WITH_PERSON_DATA);
    }

    @Test
    public void getPeople_adminShouldGetPeopleFromAllLocations() {
        Context.authenticate(DEMO_ADMIN_USERNAME, DEMO_ADMIN_PASSWORD);
        User authenticatedUser = Context.getAuthenticatedUser();
        assertNotNull(authenticatedUser);
        assertTrue(authenticatedUser.isSuperUser());

        Location location1 = locationService.getLocation(DEMO_LOCATION1_ID);
        Location location2 = locationService.getLocation(DEMO_LOCATION2_ID);

        List<Person> personList = personService.getPeople(DEMO_PATIENT_FIRST_NAME, false);

        assertEquals(3, personList.size());
        assertNull(personList.get(0).getAttribute(DEMO_PERSON_ATTRIBUTE_TYPE_ID));

        assertNotNull(personList.get(1).getAttribute(DEMO_PERSON_ATTRIBUTE_TYPE_ID));
        assertEquals(location1.getUuid(), personList.get(1).getAttribute(DEMO_PERSON_ATTRIBUTE_TYPE_ID).getValue());

        assertNotNull(personList.get(2).getAttribute(DEMO_PERSON_ATTRIBUTE_TYPE_ID));
        assertEquals(location2.getUuid(), personList.get(2).getAttribute(DEMO_PERSON_ATTRIBUTE_TYPE_ID).getValue());
    }

    @Test
    public void getPeople_nonAdminUserWithLocationPropertyShouldGetOnlyAccessiblePersonInformation() {
        User normalUser = userService.getUser(DEMO_USER2_ID);
        Context.authenticate(normalUser.getUsername(), DEMO_NORMAL_USER_PASSWORD);
        assertFalse(normalUser.isSuperUser());

        Location location1 = locationService.getLocation(DEMO_LOCATION1_ID);

        assertEquals(1, normalUser.getUserProperties().size());
        String locationUserProperty = normalUser.getUserProperty(DEMO_LOCATION_USER_PROPERTY);
        assertNotNull(locationUserProperty);
        assertEquals(location1.getUuid(), locationUserProperty);

        List<Person> personList = personService.getPeople(DEMO_PATIENT_FIRST_NAME, false);
        assertEquals(1, personList.size());

        Person person = personList.get(0);
        assertNotNull(person.getAttribute(DEMO_PERSON_ATTRIBUTE_TYPE_ID));
        assertEquals(location1.getUuid(), person.getAttribute(DEMO_PERSON_ATTRIBUTE_TYPE_ID).getValue());
    }

    @Test
    public void getPeople_nonAdminUserWithOutLocationPropertyShouldNotGetPersonInformation() {
        User normalUser = userService.getUser(DEMO_USER1_ID);
        Context.authenticate(normalUser.getUsername(), DEMO_NORMAL_USER_PASSWORD);
        assertFalse(normalUser.isSuperUser());

        assertEquals(0, normalUser.getUserProperties().size());

        List<Person> personList = personService.getPeople(DEMO_PATIENT_FIRST_NAME, false);
        assertEquals(0, personList.size());
    }

    @Test
    public void getPerson_adminShouldGetPersonFromAllLocations() {
        Context.authenticate(DEMO_ADMIN_USERNAME, DEMO_ADMIN_PASSWORD);
        User authenticatedUser = Context.getAuthenticatedUser();
        assertNotNull(authenticatedUser);
        assertTrue(authenticatedUser.isSuperUser());

        Location location1 = locationService.getLocation(DEMO_LOCATION1_ID);
        Location location2 = locationService.getLocation(DEMO_LOCATION2_ID);

        Person person1 = personService.getPerson(DEMO_PERSON1_ID);
        assertNotNull(person1);
        assertNull(person1.getAttribute(DEMO_PERSON_ATTRIBUTE_TYPE_ID));

        Person person2 = personService.getPerson(DEMO_PERSON2_ID);
        assertNotNull(person2);
        assertEquals(location1.getUuid(), person2.getAttribute(DEMO_PERSON_ATTRIBUTE_TYPE_ID).getValue());

        Person person3 = personService.getPerson(DEMO_PERSON3_ID);
        assertNotNull(person3);
        assertEquals(location2.getUuid(), person3.getAttribute(DEMO_PERSON_ATTRIBUTE_TYPE_ID).getValue());
    }

    @Test
    public void getPerson_nonAdminUserWithLocationPropertyShouldGetOnlyAccessiblePersonInformation() {
        User normalUser = userService.getUser(DEMO_USER2_ID);
        Context.authenticate(normalUser.getUsername(), DEMO_NORMAL_USER_PASSWORD);
        assertFalse(normalUser.isSuperUser());

        Location location1 = locationService.getLocation(DEMO_LOCATION1_ID);

        assertEquals(1, normalUser.getUserProperties().size());

        String locationUserProperty = normalUser.getUserProperty(DEMO_LOCATION_USER_PROPERTY);
        assertNotNull(locationUserProperty);
        assertEquals(location1.getUuid(), locationUserProperty);

        Person person1 = personService.getPerson(DEMO_PERSON1_ID);
        assertNull(person1);

        Person person2 = personService.getPerson(DEMO_PERSON2_ID);
        assertNotNull(person2);
        assertEquals(location1.getUuid(), person2.getAttribute(DEMO_PERSON_ATTRIBUTE_TYPE_ID).getValue());

        Person person3 = personService.getPerson(DEMO_PERSON3_ID);
        assertNull(person3);
    }

    @Test
    public void getPerson_nonAdminUserWithOutLocationPropertyShouldNotGetPersonInformation() {
        User normalUser = userService.getUser(DEMO_USER1_ID);
        Context.authenticate(normalUser.getUsername(), DEMO_NORMAL_USER_PASSWORD);
        assertFalse(normalUser.isSuperUser());

        assertEquals(0, normalUser.getUserProperties().size());

        Person person1 = personService.getPerson(DEMO_PERSON1_ID);
        assertNull(person1);

        Person person2 = personService.getPerson(DEMO_PERSON2_ID);
        assertNull(person2);

        Person person3 = personService.getPerson(DEMO_PERSON3_ID);
        assertNull(person3);
    }

    @Test
    public void getPersonByUuid_adminShouldGetPeopleFromAllLocations() {
        Context.authenticate(DEMO_ADMIN_USERNAME, DEMO_ADMIN_PASSWORD);
        User authenticatedUser = Context.getAuthenticatedUser();
        assertNotNull(authenticatedUser);
        assertTrue(authenticatedUser.isSuperUser());

        Location location1 = locationService.getLocation(DEMO_LOCATION1_ID);
        Location location2 = locationService.getLocation(DEMO_LOCATION2_ID);

        Person person1 = personService.getPersonByUuid(DEMO_PATIENT1_ID);
        assertNotNull(person1);
        assertNull(person1.getAttribute(DEMO_PERSON_ATTRIBUTE_TYPE_ID));

        Person person2 = personService.getPersonByUuid(DEMO_PATIENT2_ID);
        assertNotNull(person2);
        assertEquals(location1.getUuid(), person2.getAttribute(DEMO_PERSON_ATTRIBUTE_TYPE_ID).getValue());

        Person person3 = personService.getPersonByUuid(DEMO_PATIENT3_ID);
        assertNotNull(person3);
        assertEquals(location2.getUuid(), person3.getAttribute(DEMO_PERSON_ATTRIBUTE_TYPE_ID).getValue());
    }

    @Test
    public void getPersonByUuid_nonAdminUserWithLocationPropertyShouldGetOnlyAccessiblePersonInformation() {
        User normalUser = userService.getUser(DEMO_USER2_ID);
        Context.authenticate(normalUser.getUsername(), DEMO_NORMAL_USER_PASSWORD);
        assertFalse(normalUser.isSuperUser());

        Location location1 = locationService.getLocation(DEMO_LOCATION1_ID);

        assertEquals(1, normalUser.getUserProperties().size());

        String locationUserProperty = normalUser.getUserProperty(DEMO_LOCATION_USER_PROPERTY);
        assertNotNull(locationUserProperty);
        assertEquals(location1.getUuid(), locationUserProperty);

        Person person1 = personService.getPersonByUuid(DEMO_PATIENT1_ID);
        assertNull(person1);

        Person person2 = personService.getPersonByUuid(DEMO_PATIENT2_ID);
        assertNotNull(person2);
        assertEquals(location1.getUuid(), person2.getAttribute(DEMO_PERSON_ATTRIBUTE_TYPE_ID).getValue());

        Person person3 = personService.getPersonByUuid(DEMO_PATIENT3_ID);
        assertNull(person3);
    }

    @Test
    public void getPersonByUuid_nonAdminUserWithOutLocationPropertyShouldNotGetPersonInformation() {
        User normalUser = userService.getUser(DEMO_USER1_ID);
        Context.authenticate(normalUser.getUsername(), DEMO_NORMAL_USER_PASSWORD);
        assertFalse(normalUser.isSuperUser());

        assertEquals(0, normalUser.getUserProperties().size());

        Person person1 = personService.getPersonByUuid(DEMO_PATIENT1_ID);
        assertNull(person1);

        Person person2 = personService.getPersonByUuid(DEMO_PATIENT2_ID);
        assertNull(person2);

        Person person3 = personService.getPersonByUuid(DEMO_PATIENT3_ID);
        assertNull(person3);
    }
}
