package org.openmrs.module.locationbasedaccess.aop;

import java.util.Iterator;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.Location;
import org.openmrs.Person;
import org.openmrs.PersonAttribute;
import org.openmrs.PersonAttributeType;
import org.openmrs.User;
import org.openmrs.api.LocationService;
import org.openmrs.api.PersonService;
import org.openmrs.api.UserService;
import org.openmrs.api.context.Context;
import org.openmrs.module.locationbasedaccess.LocationBasedAccessConstants;
import org.openmrs.module.locationbasedaccess.utils.LocationUtils;
import org.openmrs.test.BaseModuleContextSensitiveTest;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class LocationUtilsTest extends BaseModuleContextSensitiveTest {

	private PersonService personService;
	private LocationService locationService;
	private UserService userService;
	private static final String XML_FILENAME_WITH_PERSON_DATA = "include/LocationUtilTestData.xml";

	private static final int DEMO_PERSON3_ID = 3;
	private static final int DEMO_PERSON2_ID = 2;
	private static final int DEMO_PERSON5_ID = 6005;
	private static final int DEMO_PERSON6_ID = 6006;
	private static final int DEMO_LOCATION1_ID = 1;
	private static final int DEMO_LOCATION2_ID = 2;
	private static final int DEMO_USER1_ID = 6001;
	private static final int DEMO_PERSONATTRIBUTE1_ID = 10;

	public LocationUtilsTest() {

	}

	@Before
	public void setUp() throws Exception {
		userService = Context.getUserService();
		personService = Context.getPersonService();
		locationService = Context.getLocationService();
		executeDataSet(XML_FILENAME_WITH_PERSON_DATA);
	}

	@Test
	public void getPersonLocation_shouldReturnPersonLocation() {
		Person person3 = personService.getPerson(DEMO_PERSON3_ID);
		Location location1 = locationService.getLocation(DEMO_LOCATION1_ID);
		PersonAttributeType personAttributeType = personService.getPersonAttributeType(DEMO_PERSONATTRIBUTE1_ID);
		assertNull(person3.getAttribute(personAttributeType));
		PersonAttribute personAttribute = new PersonAttribute(personAttributeType, location1.getUuid());
		person3.addAttribute(personAttribute);
		Location location3 = LocationUtils.getPersonLocation(person3);
		assertEquals(location3, location1);
	}

	@Test
	public void getPersonLocation_shouldReturnNullIfNoLocationAttribute() {
		Person person2 = personService.getPerson(DEMO_PERSON2_ID);
		PersonAttributeType personAttributeType = personService.getPersonAttributeType(DEMO_PERSONATTRIBUTE1_ID);
		assertNull(person2.getAttribute(personAttributeType));
		Location location = LocationUtils.getPersonLocation(person2);
		assertNull(location);
	}

	@Test
	public void doesPersonBelongToGivenLocation_shouldBeTrueIfPersonBelongToSameLocation() {
		Person person2 = personService.getPerson(DEMO_PERSON2_ID);
		PersonAttributeType personAttributeType = personService.getPersonAttributeType(DEMO_PERSONATTRIBUTE1_ID);
		assertNull(person2.getAttribute(personAttributeType));
		Location location1 = locationService.getLocation(DEMO_LOCATION1_ID);
		PersonAttribute personAttribute = new PersonAttribute(personAttributeType, location1.getUuid());
		person2.addAttribute(personAttribute);
		String sessionLocationUuid = location1.getUuid();
		Boolean aBoolean = LocationUtils.doesPersonBelongToGivenLocation(person2, personAttributeType, sessionLocationUuid);
		assertTrue(aBoolean);
	}

	@Test
	public void doesPersonBelongToGivenLocation_shouldBeFalseIfPersonNotBelongToSameLocation() {
		Person person2 = personService.getPerson(DEMO_PERSON2_ID);
		PersonAttributeType personAttributeType = personService.getPersonAttributeType(DEMO_PERSONATTRIBUTE1_ID);
		assertNull(person2.getAttribute(personAttributeType));
		String sessionLocationUuid1 = "ef93c695-ac43-450a-93f8-4b2b4d50a3c9";
		Boolean aBoolean1 = LocationUtils.doesPersonBelongToGivenLocation(person2, personAttributeType,
				sessionLocationUuid1);
		assertFalse(aBoolean1);
		Location location1 = locationService.getLocation(DEMO_LOCATION1_ID);
		Location location2 = locationService.getLocation(DEMO_LOCATION2_ID);
		assertNotEquals(location1.getUuid(), location2.getUuid());
		PersonAttribute personAttribute = new PersonAttribute(personAttributeType, location1.getUuid());
		person2.addAttribute(personAttribute);
		String sessionLocationUuid2 = location2.getUuid();
		Boolean aBoolean2 = LocationUtils.doesPersonBelongToGivenLocation(person2, personAttributeType,
				sessionLocationUuid2);
		assertFalse(aBoolean2);
	}

	@Test
	public void doesUserBelongToGivenLocation_shouldBeTrueIfUserBelongToSameLocation() {
		User user1 = userService.getUser(DEMO_USER1_ID);
		assertEquals(0, user1.getUserProperties().size());
		Location location1 = locationService.getLocation(DEMO_LOCATION1_ID);
		user1.setUserProperty(LocationBasedAccessConstants.LOCATION_USER_PROPERTY_NAME, location1.getUuid());
		String sessionLocationUuid = location1.getUuid();
		Boolean aBoolean = LocationUtils.doesUserBelongToGivenLocation(user1, sessionLocationUuid);
		assertTrue(aBoolean);
	}

	@Test
	public void doesUserBelongToGivenLocation_shouldBeFalseIfUserNotBelongToSameLocation() {
		User user1 = userService.getUser(DEMO_USER1_ID);
		assertEquals(0, user1.getUserProperties().size());
		Location location1 = locationService.getLocation(DEMO_LOCATION1_ID);
		String sessionLocationUuid1 = location1.getUuid();
		Boolean aBoolean1 = LocationUtils.doesUserBelongToGivenLocation(user1, sessionLocationUuid1);
		assertFalse(aBoolean1);
		Location location2 = locationService.getLocation(DEMO_LOCATION2_ID);
		assertNotEquals(location1.getUuid(), location2.getUuid());
		user1.setUserProperty(LocationBasedAccessConstants.LOCATION_USER_PROPERTY_NAME, location1.getUuid());
		String sessionLocationUuid2 = location2.getUuid();
		Boolean aBoolean2 = LocationUtils.doesUserBelongToGivenLocation(user1, sessionLocationUuid2);
		assertFalse(aBoolean2);
	}

	@Test
	public void doesUsersForPersonBelongToGivenLocation_shouldBeTrueIfAnyUserOfPersonBelongToGivenLocation() {
		Person person5 = personService.getPerson(DEMO_PERSON5_ID);
		List<User> users = userService.getUsersByPerson(person5, false);
		assertNotNull(users);
		User user0 = users.get(0);
		String sessionLocationUuid = user0.getUserProperty(LocationBasedAccessConstants.LOCATION_USER_PROPERTY_NAME);
		Boolean aBoolean1 = LocationUtils.doesUsersForPersonBelongToGivenLocation(person5, sessionLocationUuid);
		assertTrue(aBoolean1);
	}

	@Test
	public void doesUsersForPersonBelongToGivenLocation_shouldBeFalseIfNoUserOfPersonBelongToGivenLocation() {
		Person person6 = personService.getPerson(DEMO_PERSON6_ID);
		List<User> userList = userService.getUsersByPerson(person6, false);
		assertNotNull(userList);
		Location location1 = locationService.getLocation(DEMO_LOCATION1_ID);
		for (Iterator<User> iterator = userList.iterator(); iterator.hasNext(); ) {
			User user = iterator.next();
			assertNotEquals(user.getUserProperty(LocationBasedAccessConstants.LOCATION_USER_PROPERTY_NAME),
					location1.getUuid());
		}
		Boolean aBoolean1 = LocationUtils.doesUsersForPersonBelongToGivenLocation(person6, location1.getUuid());
		assertFalse(aBoolean1);
	}

	@Test
	public void getUserAccessibleLocationUuid_shouldReturnAccessibleLocationUuid() {
		User user1 = userService.getUser(DEMO_USER1_ID);
		assertEquals(0, user1.getUserProperties().size());
		Location location1 = locationService.getLocation(DEMO_LOCATION1_ID);
		user1.setUserProperty(LocationBasedAccessConstants.LOCATION_USER_PROPERTY_NAME, location1.getUuid());
		String userLocationUuid = location1.getUuid();
		String userAccessibleLocationUuid = LocationUtils.getUserAccessibleLocationUuid(user1);
		assertEquals(userLocationUuid, userAccessibleLocationUuid);
	}

	@Test
	public void getUserAccessibleLocationUuid_shouldReturnSessionLocationUuidIfNoUserProperty() {
		User user1 = userService.getUser(DEMO_USER1_ID);
		assertEquals(0, user1.getUserProperties().size());
		Location location1 = locationService.getLocation(DEMO_LOCATION1_ID);
		Context.getUserContext().setLocationId(location1.getLocationId());
		String sessionLocationUuid = location1.getUuid();
		String userAccessibleLocationUuid = LocationUtils.getUserAccessibleLocationUuid(user1);
		assertEquals(sessionLocationUuid, userAccessibleLocationUuid);
	}

	@Test
	public void getUserAccessibleLocationUuid_shouldReturnEmptyStringIfNoSessionLocationUuidAndNoUserProperty() {
		User user1 = userService.getUser(DEMO_USER1_ID);
		assertEquals(0, user1.getUserProperties().size());
		Integer sessionLocationId = Context.getUserContext().getLocationId();
		assertNull(sessionLocationId);
		String userAccessibleLocationUuid = LocationUtils.getUserAccessibleLocationUuid(user1);
		assertEquals("", userAccessibleLocationUuid);
	}

}
