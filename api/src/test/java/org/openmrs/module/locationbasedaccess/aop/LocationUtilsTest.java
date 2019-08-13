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
	public void getUserAccessibleLocationUuid_shouldReturnAccessibleLocationUuid() {
		User user1 = userService.getUser(DEMO_USER1_ID);
		assertEquals(0, user1.getUserProperties().size());
		Location location1 = locationService.getLocation(DEMO_LOCATION1_ID);
		Location location2 = locationService.getLocation(DEMO_LOCATION2_ID);
		String locations = location1.getUuid()+","+location2.getUuid();
		user1.setUserProperty(LocationBasedAccessConstants.LOCATION_USER_PROPERTY_NAME, locations);
		String userLocationUuid = location1.getUuid();
		List<String> userAccessibleLocationUuids = LocationUtils.getUserAccessibleLocationUuids(user1);
		assertEquals(2, userAccessibleLocationUuids.size());
		assertEquals(location1.getUuid(), userAccessibleLocationUuids.get(0));
		assertEquals(location2.getUuid(), userAccessibleLocationUuids.get(1));
	}

	@Test
	public void getUserAccessibleLocationUuid_shouldReturnSessionLocationUuidIfNoUserProperty() {
		User user1 = userService.getUser(DEMO_USER1_ID);
		assertEquals(0, user1.getUserProperties().size());
		Location location1 = locationService.getLocation(DEMO_LOCATION1_ID);
		Context.getUserContext().setLocationId(location1.getLocationId());
		List<String> userAccessibleLocationUuids = LocationUtils.getUserAccessibleLocationUuids(user1);
		assertEquals(1, userAccessibleLocationUuids.size());
		assertEquals(userAccessibleLocationUuids.get(0), location1.getUuid());
	}

	@Test
	public void getUserAccessibleLocationUuid_shouldReturnEmptyListIfNoSessionLocationUuidAndNoUserProperty() {
		User user1 = userService.getUser(DEMO_USER1_ID);
		assertEquals(0, user1.getUserProperties().size());
		Integer sessionLocationId = Context.getUserContext().getLocationId();
		assertNull(sessionLocationId);
		List<String> userAccessibleLocationUuids = LocationUtils.getUserAccessibleLocationUuids(user1);
		assertEquals(1,userAccessibleLocationUuids.size());
		assertEquals("",userAccessibleLocationUuids.get(0));
	}

}
