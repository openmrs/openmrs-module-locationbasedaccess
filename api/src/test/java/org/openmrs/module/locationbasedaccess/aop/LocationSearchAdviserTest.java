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
import org.openmrs.GlobalProperty;
import org.openmrs.Location;
import org.openmrs.LocationTag;
import org.openmrs.User;
import org.openmrs.api.LocationService;
import org.openmrs.api.context.Context;
import org.openmrs.module.locationbasedaccess.aop.common.AOPContextSensitiveTest;
import org.openmrs.module.locationbasedaccess.aop.common.TestWithAOP;
import org.openmrs.module.locationbasedaccess.aop.interceptor.LocationServiceInterceptorAdvice;
import org.openmrs.util.OpenmrsConstants;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.openmrs.module.locationbasedaccess.LocationBasedAccessConstants.LOCATION_USER_PROPERTY_NAME;

public class LocationSearchAdviserTest extends AOPContextSensitiveTest {

    private LocationService locationService;

    private static final String LOCATION_TEST_DATA_XML_LOCATION = "include/LocationTestData.xml";

    private Location location1;
    private Location location2;
    private Location location3;
    private Location location4;
    private Location location5;

    private static final String SEARCH_QUERY_MATCH_ALL_LOCATIONS = "test location";
    private static final String TAG_NAME = "Test Location Tag";

    private Set<String> restrictedGetMethodNames = new HashSet<String>();

    public LocationSearchAdviserTest() {
        restrictedGetMethodNames.add("getDefaultLocation");
        restrictedGetMethodNames.add("getDefaultLocationFromSting");
        restrictedGetMethodNames.add("getLocationByUuid");
        restrictedGetMethodNames.add("getAllLocations");
        restrictedGetMethodNames.add("getLocations");
        restrictedGetMethodNames.add("getLocationsByTag");
        restrictedGetMethodNames.add("getRootLocations");
    }

    @Override
    protected void setInterceptorAndServices(TestWithAOP testCase) {
        testCase.setInterceptor(new LocationServiceInterceptorAdvice(restrictedGetMethodNames));
        testCase.addService(LocationService.class);
    }

    @Before
    public void setUp() throws Exception {
        locationService = Context.getLocationService();
        executeDataSet(LOCATION_TEST_DATA_XML_LOCATION);

        location1 = locationService.getLocation(1);
        location2 = locationService.getLocation(2);
        location3 = locationService.getLocation(3);
        location4 = locationService.getLocation(4);
        location5 = locationService.getLocation(5);
        assertNotNull(location1);
        assertNotNull(location2);
        assertNotNull(location3);
        assertNotNull(location4);
        assertNotNull(location5);
    }

    @Test
    public void getDefaultLocation_adminShouldGetDefaultLocation() {
        setDefaultLocationGP(location2);
        authenticateAdmin();
        assertAuthenticatedAdmin();

        Location result = locationService.getDefaultLocation();
        assertEquals(location2, result);
    }

    @Test
    public void getDefaultLocation_nonAdminWithLocationPropertyShouldGetDefaultIfHeHasAccessToThatLocation() {
        setDefaultLocationGP(location3);
        User user = authenticateUserWithLocationProperty();
        assertAuthenticatedNormalUser();
        assertEquals(location3.getUuid(), user.getUserProperty(LOCATION_USER_PROPERTY_NAME));

        Location result = locationService.getDefaultLocation();
        assertEquals(location3, result);
    }

    @Test
    public void getDefaultLocation_nonAdminWithLocationPropertyShouldNotGetDefaultLocationHeDoesntHaveAccessToThatLocation() {
        setDefaultLocationGP(location2);
        User user = authenticateUserWithLocationProperty();
        assertAuthenticatedNormalUser();
        assertNotEquals(location2.getUuid(), user.getUserProperty(LOCATION_USER_PROPERTY_NAME));

        Location result = locationService.getDefaultLocation();
        assertNull(result);
    }

    @Test
    public void getDefaultLocation_nonAdminWithoutLocationPropertyShouldGetOnlyDefaultLocationIfItsSessionLocation() {
        setDefaultLocationGP(location2);
        User user = authenticateUserWithoutLocationProperty();
        assertAuthenticatedNormalUser();
        assertEquals("", user.getUserProperty(LOCATION_USER_PROPERTY_NAME));

        setSessionLocation(location2);
        assertEquals(location2, Context.getUserContext().getLocation());

        Location result = locationService.getDefaultLocation();
        assertEquals(location2, result);

    }

    @Test
    public void getDefaultLocation_nonAdminWithoutLocationPropertyShouldNotGetDefaultLocationIfItsNotSessionLocation() {
        setDefaultLocationGP(location2);
        User user = authenticateUserWithoutLocationProperty();
        assertAuthenticatedNormalUser();
        assertEquals("", user.getUserProperty(LOCATION_USER_PROPERTY_NAME));

        setSessionLocation(location3);
        assertEquals(location3, Context.getUserContext().getLocation());

        Location result = locationService.getDefaultLocation();
        assertNull(result);
    }

    @Test
    public void getLocationByUuid_adminShouldGetAllLocations() {
        authenticateAdmin();
        assertAuthenticatedAdmin();

        Location result1 = locationService.getLocationByUuid(location1.getUuid());
        assertEquals(location1, result1);
        Location result2 = locationService.getLocationByUuid(location2.getUuid());
        assertEquals(location2, result2);
        Location result3 = locationService.getLocationByUuid(location3.getUuid());
        assertEquals(location3, result3);
        Location result4 = locationService.getLocationByUuid(location4.getUuid());
        assertEquals(location4, result4);
        Location result5 = locationService.getLocationByUuid(location5.getUuid());
        assertEquals(location5, result5);
    }

    @Test
    public void getLocationByUuid_nonAdminWithLocationPropertyShouldGetOnlyAccessLocation() {
        User user = authenticateUserWithLocationProperty();
        assertAuthenticatedNormalUser();
        assertEquals(location3.getUuid(), user.getUserProperty(LOCATION_USER_PROPERTY_NAME));

        Location result1 = locationService.getLocationByUuid(location1.getUuid());
        assertNull(result1);
        Location result2 = locationService.getLocationByUuid(location2.getUuid());
        assertNull(result2);
        Location result3 = locationService.getLocationByUuid(location3.getUuid());
        assertEquals(location3, result3);
        Location result4 = locationService.getLocationByUuid(location4.getUuid());
        assertNull(result4);
        Location result5 = locationService.getLocationByUuid(location5.getUuid());
        assertNull(result5);
    }

    @Test
    public void getLocationByUuid_nonAdminWithoutLocationPropertyShouldGetOnlySessionLocation() {
        User user = authenticateUserWithoutLocationProperty();
        assertAuthenticatedNormalUser();
        assertEquals("", user.getUserProperty(LOCATION_USER_PROPERTY_NAME));

        setSessionLocation(location2);
        assertEquals(location2, Context.getUserContext().getLocation());

        Location result1 = locationService.getLocationByUuid(location1.getUuid());
        assertNull(result1);
        Location result2 = locationService.getLocationByUuid(location2.getUuid());
        assertEquals(location2, result2);
        Location result3 = locationService.getLocationByUuid(location3.getUuid());
        assertNull(result3);
        Location result4 = locationService.getLocationByUuid(location4.getUuid());
        assertNull(result4);
        Location result5 = locationService.getLocationByUuid(location5.getUuid());
        assertNull(result5);

        setSessionLocation(location4);
        assertEquals(location4, Context.getUserContext().getLocation());

        Location result6 = locationService.getLocationByUuid(location1.getUuid());
        assertNull(result6);
        Location result7 = locationService.getLocationByUuid(location2.getUuid());
        assertNull(result7);
        Location result8 = locationService.getLocationByUuid(location3.getUuid());
        assertNull(result8);
        Location result9 = locationService.getLocationByUuid(location4.getUuid());
        assertEquals(location4, result9);
        Location result10 = locationService.getLocationByUuid(location5.getUuid());
        assertNull(result10);
    }

    @Test
    public void getAllLocations_adminShouldGetAllLocations() {
        authenticateAdmin();
        assertAuthenticatedAdmin();

        //Should get all 5 locations, including retired locations
        List<Location> result1 = locationService.getAllLocations();
        assertThat(result1, hasSize(5));
        assertThat(result1, hasItem(location1));
        assertThat(result1, hasItem(location2));
        assertThat(result1, hasItem(location3));
        assertThat(result1, hasItem(location4));
        assertThat(result1, hasItem(location5));

        //Should get only 3 locations, which are not retired
        List<Location> result2 = locationService.getAllLocations(false);
        assertThat(result2, hasSize(3));
        assertThat(result2, hasItem(location1));
        assertThat(result2, hasItem(location2));
        assertThat(result2, hasItem(location3));
    }

    @Test
    public void getAllLocations_nonAdminWithLocationPropertyShouldGetOnlyAccessLocation() {
        User user = authenticateUserWithLocationProperty();
        assertAuthenticatedNormalUser();
        assertEquals(location3.getUuid(), user.getUserProperty(LOCATION_USER_PROPERTY_NAME));

        List<Location> result = locationService.getAllLocations();
        assertThat(result, hasSize(1));
        assertThat(result, hasItem(location3));
    }

    @Test
    public void getAllLocations_nonAdminWithoutLocationPropertyShouldGetOnlySessionLocation() {
        User user = authenticateUserWithoutLocationProperty();
        assertAuthenticatedNormalUser();
        assertEquals("", user.getUserProperty(LOCATION_USER_PROPERTY_NAME));

        setSessionLocation(location4);
        assertEquals(location4, Context.getUserContext().getLocation());

        List<Location> result1 = locationService.getAllLocations();
        assertThat(result1, hasSize(1));
        assertThat(result1, hasItem(location4));

        setSessionLocation(location2);
        assertEquals(location2, Context.getUserContext().getLocation());

        List<Location> result2 = locationService.getAllLocations();
        assertThat(result2, hasSize(1));
        assertThat(result2, hasItem(location2));
    }

    @Test
    public void getLocations_adminShouldGetAllLocations() {
        authenticateAdmin();
        assertAuthenticatedAdmin();

        //Should get only 3 locations, which are not retired
        List<Location> result1 = locationService.getLocations(SEARCH_QUERY_MATCH_ALL_LOCATIONS);
        assertThat(result1, hasSize(3));
        assertThat(result1, hasItem(location1));
        assertThat(result1, hasItem(location2));
        assertThat(result1, hasItem(location3));

        //Should get all locations, including retired
        List<Location> result2 = locationService.getLocations(SEARCH_QUERY_MATCH_ALL_LOCATIONS, null, null, true, null, null);
        assertThat(result2, hasSize(5));
        assertThat(result2, hasItem(location1));
        assertThat(result2, hasItem(location2));
        assertThat(result2, hasItem(location3));
        assertThat(result2, hasItem(location4));
        assertThat(result2, hasItem(location5));
    }

    @Test
    public void getLocations_nonAdminWithLocationPropertyShouldGetOnlyAccessLocation() {
        User user = authenticateUserWithLocationProperty();
        assertAuthenticatedNormalUser();
        assertEquals(location3.getUuid(), user.getUserProperty(LOCATION_USER_PROPERTY_NAME));

        //access location is not retired so both methods return the same result
        List<Location> result1 = locationService.getLocations(SEARCH_QUERY_MATCH_ALL_LOCATIONS);
        assertThat(result1, hasSize(1));
        assertThat(result1, hasItem(location3));

        List<Location> result2 = locationService.getLocations(SEARCH_QUERY_MATCH_ALL_LOCATIONS, null, null, true, null, null);
        assertThat(result2, hasSize(1));
        assertThat(result2, hasItem(location3));
    }

    @Test
    public void getLocations_nonAdminWithoutLocationPropertyShouldGetOnlySessionLocation() {
        User user = authenticateUserWithoutLocationProperty();
        assertAuthenticatedNormalUser();
        assertEquals("", user.getUserProperty(LOCATION_USER_PROPERTY_NAME));

        //session location is not retired so both methods return the same result
        setSessionLocation(location2);
        assertEquals(location2, Context.getUserContext().getLocation());

        List<Location> result1 = locationService.getLocations(SEARCH_QUERY_MATCH_ALL_LOCATIONS);
        assertThat(result1, hasSize(1));
        assertThat(result1, hasItem(location2));
        List<Location> result2 = locationService.getLocations(SEARCH_QUERY_MATCH_ALL_LOCATIONS, null, null, true, null, null);
        assertThat(result2, hasSize(1));
        assertThat(result2, hasItem(location2));

        //session location is retired so first method should return empty list
        setSessionLocation(location4);
        assertEquals(location4, Context.getUserContext().getLocation());

        List<Location> result3 = locationService.getLocations(SEARCH_QUERY_MATCH_ALL_LOCATIONS);
        assertThat(result3, empty());
        List<Location> result4 = locationService.getLocations(SEARCH_QUERY_MATCH_ALL_LOCATIONS, null, null, true, null, null);
        assertThat(result4, hasSize(1));
        assertThat(result4, hasItem(location4));
    }

    @Test
    public void getLocationsByTag_adminShouldGetAllLocationsWithTag() {
        authenticateAdmin();
        assertAuthenticatedAdmin();
        LocationTag tag = locationService.getLocationTagByName(TAG_NAME);

        List<Location> result = locationService.getLocationsByTag(tag);
        assertThat(result, hasSize(2));
        assertThat(result, hasItem(location1));
        assertThat(result, hasItem(location3));
    }

    @Test
    public void getLocationByTag_nonAdminWithLocationPropertyShouldGetOnlyAccessLocation() {
        User user = authenticateUserWithLocationProperty();
        assertAuthenticatedNormalUser();
        assertEquals(location3.getUuid(), user.getUserProperty(LOCATION_USER_PROPERTY_NAME));
        LocationTag tag = locationService.getLocationTagByName(TAG_NAME);

        List<Location> result = locationService.getLocationsByTag(tag);
        assertThat(result, hasSize(1));
        assertThat(result, hasItem(location3));
    }

    @Test
    public void getLocationByTag_nonAdminWithoutLocationPropertyShouldGetOnlySessionLocation() {
        User user = authenticateUserWithoutLocationProperty();
        assertAuthenticatedNormalUser();
        assertEquals("", user.getUserProperty(LOCATION_USER_PROPERTY_NAME));
        LocationTag tag = locationService.getLocationTagByName(TAG_NAME);

        //session location does have the tag
        setSessionLocation(location1);
        assertEquals(location1, Context.getUserContext().getLocation());

        List<Location> result1 = locationService.getLocationsByTag(tag);
        assertThat(result1, hasSize(1));
        assertThat(result1, hasItem(location1));

        //session location does NOT have the tag
        setSessionLocation(location2);
        assertEquals(location2, Context.getUserContext().getLocation());

        List<Location> result2 = locationService.getLocationsByTag(tag);
        assertThat(result2, empty());
    }

    @Test
    public void getRootLocations_adminShouldGetAllRootLocations() {
        authenticateAdmin();
        assertAuthenticatedAdmin();

        //Should return all 3 root locations, including retired
        List<Location> result1 = locationService.getRootLocations(true);
        assertThat(result1, hasSize(3));
        assertThat(result1, hasItem(location1));
        assertThat(result1, hasItem(location2));
        assertThat(result1, hasItem(location5));

        //Should return only 2 root locations, which are not retired
        List<Location> result2 = locationService.getRootLocations(false);
        assertThat(result2, hasSize(2));
        assertThat(result2, hasItem(location1));
        assertThat(result2, hasItem(location2));
    }

    @Test
    public void getRootLocations_nonAdminWithLocationPropertyShouldNotGetRootLocationIfItsNotAccessLocation() {
        User user = authenticateUserWithLocationProperty();
        assertAuthenticatedNormalUser();
        assertEquals(location3.getUuid(), user.getUserProperty(LOCATION_USER_PROPERTY_NAME));

        List<Location> result = locationService.getRootLocations(true);
        assertThat(result, empty());
    }

    @Test
    public void getRootLocations_nonAdminWithoutLocationPropertyShouldGetOnlySessionLocationIfItsRootLocation() {
        User user = authenticateUserWithoutLocationProperty();
        assertAuthenticatedNormalUser();
        assertEquals("", user.getUserProperty(LOCATION_USER_PROPERTY_NAME));

        setSessionLocation(location2);
        assertEquals(location2, Context.getUserContext().getLocation());

        List<Location> result = locationService.getRootLocations(true);
        assertThat(result, hasSize(1));
        assertThat(result, hasItem(location2));
    }

    @Test
    public void getRootLocations_nonAdminWithoutLocationPropertyShouldNotGetLocationIfSessionLocationItsNotRootLocation() {
        User user = authenticateUserWithoutLocationProperty();
        assertAuthenticatedNormalUser();
        assertEquals("", user.getUserProperty(LOCATION_USER_PROPERTY_NAME));

        setSessionLocation(location3);
        assertEquals(location3, Context.getUserContext().getLocation());

        List<Location> result = locationService.getRootLocations(true);
        assertThat(result, empty());
    }

    private static void assertAuthenticatedAdmin() {
        assertTrue(Context.isAuthenticated());
        User user = Context.getAuthenticatedUser();
        assertNotNull(user);
        assertTrue(user.isSuperUser());
    }

    private static void assertAuthenticatedNormalUser() {
        assertTrue(Context.isAuthenticated());
        User user = Context.getAuthenticatedUser();
        assertNotNull(user);
        assertFalse(user.isSuperUser());
    }

    private static User authenticateAdmin() {
        Context.authenticate("admin", "test");
        return Context.getAuthenticatedUser();
    }

    private static User authenticateUserWithLocationProperty() {
        Context.authenticate("username1", "userServiceTest");
        return Context.getAuthenticatedUser();
    }

    private static User authenticateUserWithoutLocationProperty() {
        Context.authenticate("username2", "userServiceTest");
        return Context.getAuthenticatedUser();
    }

    private static void setDefaultLocationGP(Location location) {
        authenticateAdmin();
        GlobalProperty gp = new GlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_DEFAULT_LOCATION_NAME, location.getName());
        Context.getAdministrationService().saveGlobalProperty(gp);
    }

    private static void setSessionLocation(Location location) {
        Context.getUserContext().setLocation(location);
    }
}
