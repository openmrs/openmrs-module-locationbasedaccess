package org.openmrs.module.locationbasedaccess.aop;

import org.hamcrest.Matchers;
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.hamcrest.Matchers.empty;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class LocationSearchAdviserTest extends AOPContextSensitiveTest {

	private LocationService locationService;

	private static final String LOCATION_TEST_DATA_XML_LOCATION = "include/LocationTestData.xml";

	private static final String location1Uuid = "ef93c695-ac43-450a-93f8-4b2b4d50a3c8";
	private static final String location2Uuid = "ef93c695-ac43-450a-93f8-4b2b4d50a3c9";
	private static final String location3Uuid = "ef93c695-ac43-450a-93f8-4b2b4d50a3ca";
	private static final String location4Uuid = "ef93c695-ac43-450a-93f8-4b2b4d50a3cb";
	private static final String location5Uuid = "ef93c695-ac43-450a-93f8-4b2b4d50a3cc";
	private static final String badLocationUuid = "badc0de1-cafe-d00d-0123-456789abcdef";

	private static final String location2Name = "Test Location 2";
	private static final String location3Name = "Test Location 3";

	private static final String username1 = "username1";
	private static final String username2 = "username2";
	private static final String password = "userServiceTest";

	private static final String searchQuery = "test location";
	private static final String tagName = "Test Location Tag";

	private Set<String> restrictedGetMethodNames = new HashSet<String>();
	private Set<String> allLocationUuids = new HashSet<String>();
	private Set<String> unretiredLocationUuids = new HashSet<String>();
	private Set<String> rootLocationUuids = new HashSet<String>();
	private Set<String> locationsWithTagUuids = new HashSet<String>();

	public LocationSearchAdviserTest() {
		restrictedGetMethodNames.add("getDefaultLocation");
		restrictedGetMethodNames.add("getDefaultLocationFromSting");
		restrictedGetMethodNames.add("getLocationByUuid");
		restrictedGetMethodNames.add("getAllLocations");
		restrictedGetMethodNames.add("getLocations");
		restrictedGetMethodNames.add("getLocationsByTag");
		restrictedGetMethodNames.add("getRootLocations");

		allLocationUuids.add(location1Uuid);
		unretiredLocationUuids.add(location1Uuid);
		rootLocationUuids.add(location1Uuid);
		locationsWithTagUuids.add(location1Uuid);

		allLocationUuids.add(location2Uuid);
		unretiredLocationUuids.add(location2Uuid);
		rootLocationUuids.add(location2Uuid);

		allLocationUuids.add(location3Uuid);
		unretiredLocationUuids.add(location3Uuid);
		locationsWithTagUuids.add(location3Uuid);

		allLocationUuids.add(location4Uuid);

		allLocationUuids.add(location5Uuid);
		rootLocationUuids.add(location5Uuid);
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
	}

	@Test
	public void getDefaultLocation_adminShouldGetDefaultLocation() {
		setDefaultLocationGP(location2Name);
		authenticateAdmin();
		assertAuthenticatedAdmin();

		Location result = locationService.getDefaultLocation();
		assertNotNull(result);
		assertEquals(location2Uuid, result.getUuid());
	}

	@Test
	public void getDefaultLocation_nonAdminWithLocationPropertyShouldGetOnlyAccessLocation() {
		setDefaultLocationGP(location3Name);
		authenticateUserWithAccessLocation();
		assertAuthenticatedNormalUser();

		Location result = locationService.getDefaultLocation();
		assertNotNull(result);
		assertEquals(location3Uuid, result.getUuid());
	}

	@Test
	public void getDefaultLocation_nonAdminWithLocationPropertyShouldNotGetDefaultLocationIfItsNotAccessLocation() {
		setDefaultLocationGP(location2Name);
		authenticateUserWithAccessLocation();
		assertAuthenticatedNormalUser();

		Location result = locationService.getDefaultLocation();
		assertNull(result);
	}

	@Test
	public void getDefaultLocation_nonAdminWithoutLocationPropertyShouldGetOnlySessionLocation() {
		setDefaultLocationGP(location2Name);
		authenticateUserWithoutAccessLocation();
		assertAuthenticatedNormalUser();
		setSessionLocationID(2);

		Location result = locationService.getDefaultLocation();
		assertNotNull(result);
		assertEquals(location2Uuid, result.getUuid());
	}

	@Test
	public void getLocationByUuid_adminShouldGetAllLocations() {
		authenticateAdmin();
		assertAuthenticatedAdmin();

		assertGetLocationByUuidReturnsLocation(location1Uuid);
		assertGetLocationByUuidReturnsLocation(location2Uuid);
		assertGetLocationByUuidReturnsLocation(location3Uuid);
		assertGetLocationByUuidReturnsLocation(location4Uuid);
		assertGetLocationByUuidReturnsLocation(location5Uuid);
		assertGetLocationByUuidReturnsNull(badLocationUuid);
	}

	@Test
	public void getLocationByUuid_nonAdminWithLocationPropertyShouldGetOnlyAccessLocation() {
		authenticateUserWithAccessLocation();
		assertAuthenticatedNormalUser();

		assertGetLocationByUuidReturnsNull(location1Uuid);
		assertGetLocationByUuidReturnsNull(location2Uuid);
		assertGetLocationByUuidReturnsLocation(location3Uuid);
		assertGetLocationByUuidReturnsNull(location4Uuid);
		assertGetLocationByUuidReturnsNull(location5Uuid);
		assertGetLocationByUuidReturnsNull(badLocationUuid);
	}

	@Test
	public void getLocationByUuid_nonAdminWithoutLocationPropertyShouldGetOnlySessionLocation() {
		authenticateUserWithoutAccessLocation();
		assertAuthenticatedNormalUser();
		setSessionLocationID(2);

		assertGetLocationByUuidReturnsNull(location1Uuid);
		assertGetLocationByUuidReturnsLocation(location2Uuid);
		assertGetLocationByUuidReturnsNull(location3Uuid);
		assertGetLocationByUuidReturnsNull(location4Uuid);
		assertGetLocationByUuidReturnsNull(location5Uuid);
		assertGetLocationByUuidReturnsNull(badLocationUuid);
	}

	@Test
	public void getAllLocations_adminShouldGetAllLocations() {
		authenticateAdmin();
		assertAuthenticatedAdmin();

		List<String> result1 = getUuidsOfLocations(locationService.getAllLocations());
		assertEquals(5, result1.size());
		assertBothCollectionsAreEqual(allLocationUuids, result1);

		List<String> result2 = getUuidsOfLocations(locationService.getAllLocations(false));
		assertEquals(3, result2.size());
		assertBothCollectionsAreEqual(unretiredLocationUuids, result2);
	}

	@Test
	public void getAllLocations_nonAdminWithLocationPropertyShouldGetOnlyAccessLocation() {
		authenticateUserWithAccessLocation();
		assertAuthenticatedNormalUser();

		List<String> result = getUuidsOfLocations(locationService.getAllLocations());
		assertEquals(1, result.size());
		assertEquals(location3Uuid, result.get(0));
	}

	@Test
	public void getAllLocations_nonAdminWithoutLocationPropertyShouldGetOnlySessionLocation() {
		authenticateUserWithoutAccessLocation();
		assertAuthenticatedNormalUser();
		setSessionLocationID(4);

		List<String> result = getUuidsOfLocations(locationService.getAllLocations());
		assertEquals(1, result.size());
		assertEquals(location4Uuid, result.get(0));
	}

	@Test
	public void getLocations_adminShouldGetAllLocations() {
		authenticateAdmin();
		assertAuthenticatedAdmin();

		List<String> result1 = getUuidsOfLocations(locationService.getLocations(searchQuery));
		assertEquals(3, result1.size());
		assertBothCollectionsAreEqual(unretiredLocationUuids, result1);

		List<String> result2 = getUuidsOfLocations(locationService.getLocations(searchQuery, null, null, true, null, null));
		assertEquals(5, result2.size());
		assertBothCollectionsAreEqual(allLocationUuids, result2);
	}

	@Test
	public void getLocations_nonAdminWithLocationPropertyShouldGetOnlyAccessLocation() {
		authenticateUserWithAccessLocation();
		assertAuthenticatedNormalUser();

		List<String> result1 = getUuidsOfLocations(locationService.getLocations(searchQuery));
		assertEquals(1, result1.size());
		assertEquals(location3Uuid, result1.get(0));

		List<String> result2 = getUuidsOfLocations(locationService.getLocations(searchQuery, null, null, true, null, null));
		assertEquals(1, result2.size());
		assertEquals(location3Uuid, result1.get(0));
	}

	@Test
	public void getLocations_nonAdminWithoutLocationPropertyShouldGetOnlySessionLocation() {
		authenticateUserWithoutAccessLocation();
		assertAuthenticatedNormalUser();
		setSessionLocationID(2);

		List<String> result1 = getUuidsOfLocations(locationService.getLocations(searchQuery));
		assertEquals(1, result1.size());
		assertEquals(location2Uuid, result1.get(0));

		List<String> result2 = getUuidsOfLocations(locationService.getLocations(searchQuery, null, null, true, null, null));
		assertEquals(1, result2.size());
		assertEquals(location2Uuid, result1.get(0));
	}

	@Test
	public void getLocationsByTag_adminShouldGetAllLocationsWithTag() {
		authenticateAdmin();
		assertAuthenticatedAdmin();
		LocationTag tag = locationService.getLocationTagByName(tagName);

		List<String> result = getUuidsOfLocations(locationService.getLocationsByTag(tag));
		assertEquals(2, result.size());
		assertBothCollectionsAreEqual(locationsWithTagUuids, result);
	}

	public void getLocationByTag_nonAdminWithLocationPropertyShouldGetOnlyAccessLocation() {
		authenticateUserWithAccessLocation();
		assertAuthenticatedNormalUser();
		LocationTag tag = locationService.getLocationTagByName(tagName);

		List<String> result = getUuidsOfLocations(locationService.getLocationsByTag(tag));
		assertEquals(1, result.size());
		assertEquals(location3Uuid, result.get(0));
	}

	public void getLocationByTag_nonAdminWithoutLocationPropertyShouldGetOnlySessionLocation() {
		authenticateUserWithoutAccessLocation();
		assertAuthenticatedNormalUser();
		setSessionLocationID(1);
		LocationTag tag = locationService.getLocationTagByName(tagName);

		List<String> result = getUuidsOfLocations(locationService.getLocationsByTag(tag));
		assertThat(result, empty());
	}

	@Test
	public void getRootLocations_adminShouldGetAllRootLocations() {
		authenticateAdmin();
		assertAuthenticatedAdmin();

		List<String> result = getUuidsOfLocations(locationService.getRootLocations(true));
		assertEquals(3, result.size());
		assertBothCollectionsAreEqual(rootLocationUuids, result);
	}

	@Test
	public void getRootLocations_nonAdminWithLocationPropertyShouldNotGetLocationIfItsNotRootLocation() {
		authenticateUserWithAccessLocation();
		assertAuthenticatedNormalUser();

		List<String> result = getUuidsOfLocations(locationService.getRootLocations(true));
		assertThat(result, empty());
	}

	@Test
	public void getRootLocations_nonAdminWithoutLocationPropertyShouldGetOnlySessionLocation() {
		authenticateUserWithoutAccessLocation();
		assertAuthenticatedNormalUser();
		setSessionLocationID(2);

		List<String> result = getUuidsOfLocations(locationService.getRootLocations(true));
		assertEquals(1, result.size());
		assertEquals(location2Uuid, result.get(0));
	}

	@Test
	public void getRootLocations_nonAdminWithoutLocationPropertyShouldNotGetLocationIfSessionLocationIsNotRootLocation() {
		authenticateUserWithoutAccessLocation();
		assertAuthenticatedNormalUser();
		setSessionLocationID(3);

		List<String> result = getUuidsOfLocations(locationService.getRootLocations(true));
		assertThat(result, empty());
	}

	private void assertGetLocationByUuidReturnsLocation(String locationUuid) {
		Location result = locationService.getLocationByUuid(locationUuid);
		assertNotNull(result);
		assertEquals(locationUuid, result.getUuid());
	}

	private void assertGetLocationByUuidReturnsNull(String locationUuid) {
		Location result = locationService.getLocationByUuid(locationUuid);
		assertNull(result);
	}

	private void assertAuthenticatedAdmin() {
		assertTrue(Context.isAuthenticated());
		User user = Context.getAuthenticatedUser();
		assertNotNull(user);
		assertTrue(user.isSuperUser());
	}

	private void assertAuthenticatedNormalUser() {
		assertTrue(Context.isAuthenticated());
		User user = Context.getAuthenticatedUser();
		assertNotNull(user);
		assertFalse(user.isSuperUser());
	}

	private <T> void assertBothCollectionsAreEqual(Collection<T> expected, Collection<T> actual) {
		for (T item : expected) {
			assertThat(actual, Matchers.hasItem(item));
		}
		for (T item : actual) {
			assertThat(expected, Matchers.hasItem(item));
		}
	}

	private void authenticateAdmin() {
		Context.authenticate("admin", "test");
	}

	private void authenticateUserWithAccessLocation() {
		Context.authenticate(username1, password);
	}

	private void authenticateUserWithoutAccessLocation() {
		Context.authenticate(username2, password);
	}

	private void setDefaultLocationGP(String locationName) {
		authenticateAdmin();
		GlobalProperty gp = new GlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_DEFAULT_LOCATION_NAME, locationName);
		Context.getAdministrationService().saveGlobalProperty(gp);
		Context.logout();
	}

	private void setSessionLocationID(Integer locationID) {
		Context.getUserContext().setLocationId(locationID);
	}

	private List<String> getUuidsOfLocations(Iterable<Location> locations) {
		List<String> result = new ArrayList<String>();
		for (Location location : locations) {
			result.add(location.getUuid());
		}
		return result;
	}
}
