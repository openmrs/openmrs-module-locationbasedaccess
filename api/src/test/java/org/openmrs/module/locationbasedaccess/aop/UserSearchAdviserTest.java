/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 * <p>
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */

package org.openmrs.module.locationbasedaccess.aop;

import java.util.ArrayList;
import java.util.List;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import org.junit.Test;
import org.openmrs.Location;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.openmrs.Person;
import org.openmrs.User;
import org.openmrs.Role;
import org.openmrs.api.PersonService;
import org.openmrs.api.UserService;
import org.openmrs.api.LocationService;
import org.openmrs.api.context.Context;
import org.openmrs.module.locationbasedaccess.aop.common.AOPContextSensitiveTest;
import org.openmrs.module.locationbasedaccess.aop.common.TestWithAOP;
import org.openmrs.module.locationbasedaccess.aop.interceptor.UserServiceInterceptorAdvice;

public class UserSearchAdviserTest extends AOPContextSensitiveTest {
    
    private UserService userService;
    private PersonService personService;
    private LocationService locationService;
    private static final String XML_FILENAME_WITH_ADMIN_USER_DATA = "include/AdminUserData.xml";
    private static final String XML_FILENAME_WITH_PERSON_DATA = "include/PatientTestData.xml";
    private static final String DEMO_LOCATION_USER_PROPERTY= "locationUuid";
    
    private static final int DEMO_LOCATION1_ID = 1;
    private static final int DEMO_LOCATION2_ID = 2;
    private static final int DEMO_USER1_ID = 6001;
    private static final int DEMO_USER2_ID = 6002;
    private static final int DEMO_USER3_ID = 6003;
    private static final int DEMO_USER4_ID = 6004;
    private static final int DEMO_PERSON1_ID = 6001;
    private static final int DEMO_PERSON2_ID = 6002;
    private static final int DEMO_PERSON3_ID = 6003;
    private static final int DEMO_PERSON4_ID = 6004;
 
    @Override
    protected void setInterceptorAndServices(TestWithAOP testCase) {
        testCase.setInterceptor(UserServiceInterceptorAdvice.class);
        testCase.addService(UserService.class);
    }
    
    public UserSearchAdviserTest() {
    }

    @Before
    public void setUp()  throws Exception {
        userService = Context.getUserService();
        personService = Context.getPersonService();
        locationService = Context.getLocationService();
        executeDataSet(XML_FILENAME_WITH_ADMIN_USER_DATA);
        executeDataSet(XML_FILENAME_WITH_PERSON_DATA);
    }
        
    @Test
    public void getUsers_adminCanAccessUsersFromAllLocations(){
        Context.authenticate("admin", "test");
        User authenticatedUser = Context.getAuthenticatedUser();
        assertNotNull(authenticatedUser);
        assertTrue(authenticatedUser.isSuperUser());
        
        Location location1 = locationService.getLocation(DEMO_LOCATION1_ID);
        Location location2 = locationService.getLocation(DEMO_LOCATION2_ID);

        List<Role> userRole = new ArrayList<Role>();
        userRole.add(userService.getRole("userRole")); //Adding the role "userRole" to a new List
        List<User> userList = userService.getUsers("User",userRole,true);
        User testingUser1 = userList.get(0);
        User testingUser2 = userList.get(1);
        User testingUser3 = userList.get(2);
        User testingUser4 = userList.get(3);
        assertEquals(4, userList.size());
        assertNotNull(testingUser1);
        assertEquals(location1.getUuid(), testingUser1.getUserProperty(DEMO_LOCATION_USER_PROPERTY));
        assertNotNull(testingUser2);
        assertEquals(location2.getUuid(), testingUser2.getUserProperty(DEMO_LOCATION_USER_PROPERTY));
        assertNotNull(testingUser3);
        assertEquals(location1.getUuid(), testingUser3.getUserProperty(DEMO_LOCATION_USER_PROPERTY));
        assertNotNull(testingUser4);
        assertEquals("", testingUser4.getUserProperty(DEMO_LOCATION_USER_PROPERTY));
    }
    
    @Test
    public void getUsers_nonAdminUserWithOutLocationPropertyShouldNotGetUserInformation(){
        User normalUser = userService.getUser(DEMO_USER1_ID);
        Context.authenticate(normalUser.getUsername(), "userServiceTest");
        assertFalse(normalUser.isSuperUser());

        assertEquals(0, normalUser.getUserProperties().size());

        List<Role> userRole = new ArrayList<Role>();
        userRole.add(userService.getRole("userRole")); //Adding the role "userRole" to a new List
        List<User> userList = userService.getUsers("User",userRole,true);
        assertEquals(1, userList.size());
        User testingUser = userList.get(0);
        assertNotNull(testingUser); 
        assertEquals(normalUser, testingUser); //Users can access themselves
    }
    
    @Test
    public void getUsers_nonAdminUserWithLocationPropertyShouldGetOnlyAccessibleUserInformation(){
        User normalUser = userService.getUser(DEMO_USER2_ID);
        Context.authenticate(normalUser.getUsername(), "userServiceTest");
        assertFalse(normalUser.isSuperUser());

        Location location1 = locationService.getLocation(DEMO_LOCATION1_ID);

        assertEquals(1, normalUser.getUserProperties().size());
        String locationUserProperty = normalUser.getUserProperty("locationUuid");
        assertNotNull(locationUserProperty);
        assertEquals(location1.getUuid(), locationUserProperty);

        List<Role> userRole = new ArrayList<Role>();
        userRole.add(userService.getRole("userRole")); //Adding the role "userRole" to a new List
        List<User> userList = userService.getUsers("User",userRole,true);
        assertEquals(2, userList.size());
        User testingUser1 = userList.get(0);
        User testingUser2 = userList.get(1);
        assertNotNull(testingUser1);
        assertEquals(location1.getUuid(), testingUser1.getUserProperty(DEMO_LOCATION_USER_PROPERTY));
        assertEquals(normalUser, testingUser1); //Users can access themselves
        assertNotNull(testingUser2);
        assertEquals(location1.getUuid(), testingUser2.getUserProperty(DEMO_LOCATION_USER_PROPERTY));
        assertNotEquals(testingUser2,normalUser);
    }
    
    @Test
    public void getAllUsers_adminCanAccessUsersFromAllLocations(){
        Context.authenticate("admin", "test");
        User authenticatedUser = Context.getAuthenticatedUser();
        assertNotNull(authenticatedUser);
        assertTrue(authenticatedUser.isSuperUser());
        
        Location location1 = locationService.getLocation(DEMO_LOCATION1_ID);
        Location location2 = locationService.getLocation(DEMO_LOCATION2_ID);
        
        List<User> userList = userService.getAllUsers();
        assertEquals(8, userList.size());
        User testingUser1 = userList.get(0);
        User testingUser2 = userList.get(1);
        User testingUser3 = userList.get(2);
        User testingUser4 = userList.get(3);
        User testingUser5 = userList.get(4);
        User testingUser6 = userList.get(5);
        User testingUser7 = userList.get(6);
        User testingUser8 = userList.get(7);
        assertNotNull(testingUser1);
        assertEquals("", testingUser1.getUserProperty(DEMO_LOCATION_USER_PROPERTY));        
        assertNotNull(testingUser2);
        assertEquals("", testingUser2.getUserProperty(DEMO_LOCATION_USER_PROPERTY));      
        assertNotNull(testingUser3);
        assertEquals("", testingUser3.getUserProperty(DEMO_LOCATION_USER_PROPERTY));      
        assertNotNull(testingUser4);
        assertEquals("", testingUser4.getUserProperty(DEMO_LOCATION_USER_PROPERTY));        
        assertNotNull(testingUser5);
        assertEquals("", testingUser5.getUserProperty(DEMO_LOCATION_USER_PROPERTY));        
        assertNotNull(testingUser6);
        assertEquals(location1.getUuid(), testingUser6.getUserProperty(DEMO_LOCATION_USER_PROPERTY));
        assertNotNull(testingUser7);
        assertEquals(location2.getUuid(), testingUser7.getUserProperty(DEMO_LOCATION_USER_PROPERTY));
        assertNotNull(testingUser8);
        assertEquals(location1.getUuid(), testingUser8.getUserProperty(DEMO_LOCATION_USER_PROPERTY));
    }
    
    @Test
    public void getAllUsers_nonAdminUserWithOutLocationPropertyShouldNotGetUserInformation(){
        User normalUser = userService.getUser(DEMO_USER1_ID);
        Context.authenticate(normalUser.getUsername(), "userServiceTest");
        assertFalse(normalUser.isSuperUser());

        assertEquals(0, normalUser.getUserProperties().size());
        
        List<User> userList = userService.getAllUsers();
        assertEquals(1, userList.size());
        User testingUser = userList.get(0);
        assertNotNull(testingUser); 
        assertEquals("", testingUser.getUserProperty(DEMO_LOCATION_USER_PROPERTY));
        assertEquals(normalUser, testingUser); //Users can access themselves
        
    }
    
    @Test
    public void getAllUsers_nonAdminUserWithLocationPropertyShouldGetOnlyAccessibleUserInformation(){
        User normalUser = userService.getUser(DEMO_USER2_ID);
        Context.authenticate(normalUser.getUsername(), "userServiceTest");
        assertFalse(normalUser.isSuperUser());

        Location location1 = locationService.getLocation(DEMO_LOCATION1_ID);

        assertEquals(1, normalUser.getUserProperties().size());
        String locationUserProperty = normalUser.getUserProperty("locationUuid");
        assertNotNull(locationUserProperty);
        assertEquals(location1.getUuid(), locationUserProperty);
        
        List<User> userList = userService.getAllUsers();
        assertEquals(2, userList.size());
        User testingUser1 = userList.get(0);
        User testingUser2 = userList.get(1);
        assertNotNull(testingUser1);
        assertEquals(location1.getUuid(), testingUser1.getUserProperty(DEMO_LOCATION_USER_PROPERTY));
        assertEquals(normalUser, testingUser1);
        assertNotNull(testingUser2);
        assertEquals(location1.getUuid(), testingUser2.getUserProperty(DEMO_LOCATION_USER_PROPERTY));
        assertNotEquals(testingUser2,normalUser);
    }
    
    @Test
    public void getUser_adminCanAccessUsersFromAllLocations(){
        Context.authenticate("admin", "test");
        User authenticatedUser = Context.getAuthenticatedUser();
        assertNotNull(authenticatedUser);
        assertTrue(authenticatedUser.isSuperUser());
        
        Location location1 = locationService.getLocation(DEMO_LOCATION1_ID);
        Location location2 = locationService.getLocation(DEMO_LOCATION2_ID);
         
        User user1 = userService.getUser(0);
        assertNotNull(user1);
        assertEquals("", user1.getUserProperty(DEMO_LOCATION_USER_PROPERTY));    
        
        User user2 = userService.getUser(1);
        assertNotNull(user2);
        assertEquals("", user2.getUserProperty(DEMO_LOCATION_USER_PROPERTY));  
        
        User user3 = userService.getUser(DEMO_USER1_ID);
        assertNotNull(user3);
        assertEquals("", user3.getUserProperty(DEMO_LOCATION_USER_PROPERTY));
       
        User user4 = userService.getUser(DEMO_USER2_ID);
        assertNotNull(user4);
        assertEquals(location1.getUuid(), user4.getUserProperty(DEMO_LOCATION_USER_PROPERTY));
        
        User user5 = userService.getUser(DEMO_USER3_ID);
        assertNotNull(user5);
        assertEquals(location2.getUuid(), user5.getUserProperty(DEMO_LOCATION_USER_PROPERTY));
        
        User user6 = userService.getUser(DEMO_USER4_ID);
        assertNotNull(user6);
        assertEquals(location1.getUuid(), user6.getUserProperty(DEMO_LOCATION_USER_PROPERTY));
    }
    
    @Test
    public void getUser_nonAdminUserWithOutLocationPropertyShouldNotGetUserInformation(){
        User normalUser = userService.getUser(DEMO_USER1_ID);
        Context.authenticate(normalUser.getUsername(), "userServiceTest");
        assertFalse(normalUser.isSuperUser());

        assertEquals(0, normalUser.getUserProperties().size());
        
        User user1 = userService.getUser(0);
        assertNull(user1);   
        
        User user2 = userService.getUser(1);
        assertNull(user2);
        
        User user3 = userService.getUser(DEMO_USER1_ID); 
        assertNotNull(user3);
        assertEquals("", user3.getUserProperty(DEMO_LOCATION_USER_PROPERTY));
        assertEquals(normalUser, user3); //Users can access themselves

        User user4 = userService.getUser(DEMO_USER2_ID);
        assertNull(user4);
        
        User user5 = userService.getUser(DEMO_USER3_ID);
        assertNull(user5);
        
        User user6 = userService.getUser(DEMO_USER4_ID);
        assertNull(user6);
    }
    
    @Test
    public void getUser_nonAdminUserWithLocationPropertyShouldGetOnlyAccessibleUserInformation(){
        User normalUser = userService.getUser(DEMO_USER2_ID);
        Context.authenticate(normalUser.getUsername(), "userServiceTest");
        assertFalse(normalUser.isSuperUser());

        Location location1 = locationService.getLocation(DEMO_LOCATION1_ID);

        assertEquals(1, normalUser.getUserProperties().size());
        String locationUserProperty = normalUser.getUserProperty("locationUuid");
        assertNotNull(locationUserProperty);
        assertEquals(location1.getUuid(), locationUserProperty);
        
        User user1 = userService.getUser(0);
        assertNull(user1);   
        
        User user2 = userService.getUser(1);
        assertNull(user2);
        
        User user3 = userService.getUser(DEMO_USER1_ID);
        assertNull(user3);
        
        User user4 = userService.getUser(DEMO_USER2_ID);
        assertNotNull(user4);
        assertEquals(location1.getUuid(), user4.getUserProperty(DEMO_LOCATION_USER_PROPERTY));
        assertEquals(normalUser, user4); //Users can access themselves
        
        User user5 = userService.getUser(DEMO_USER3_ID);
        assertNull(user5);
        
        User user6 = userService.getUser(DEMO_USER4_ID);
        assertNotNull(user6);
        assertEquals(location1.getUuid(), user6.getUserProperty(DEMO_LOCATION_USER_PROPERTY));
        assertNotEquals(normalUser, user6);
    }
    
    @Test
    public void getUserByUuid_adminCanAccessUsersFromAllLocations(){
        Context.authenticate("admin", "test");
        User authenticatedUser = Context.getAuthenticatedUser();
        assertNotNull(authenticatedUser);
        assertTrue(authenticatedUser.isSuperUser());
        
        Location location1 = locationService.getLocation(DEMO_LOCATION1_ID);
        Location location2 = locationService.getLocation(DEMO_LOCATION2_ID);
        
        User user1 = userService.getUserByUuid("0bc18050-e132-11de-babe-001e378eb67f");
        assertNotNull(user1);
        assertEquals("", user1.getUserProperty(DEMO_LOCATION_USER_PROPERTY));
        
        User user2 = userService.getUserByUuid("0bc28050-e132-11de-babe-001e378eb67f");
        assertNotNull(user2);
        assertEquals(location1.getUuid(), user2.getUserProperty(DEMO_LOCATION_USER_PROPERTY));
        
        User user3 = userService.getUserByUuid("0bc38050-e132-11de-babe-001e378eb67f");
        assertNotNull(user3);
        assertEquals(location2.getUuid(), user3.getUserProperty(DEMO_LOCATION_USER_PROPERTY));
        
        User user4 = userService.getUserByUuid("0bc48050-e132-11de-babe-001e378eb67f");
        assertNotNull(user4);
        assertEquals(location1.getUuid(), user4.getUserProperty(DEMO_LOCATION_USER_PROPERTY));
    }
    
    @Test
    public void getUserByUuid_nonAdminUserWithOutLocationPropertyShouldNotGetUserInformation(){
        User normalUser = userService.getUser(DEMO_USER1_ID);
        Context.authenticate(normalUser.getUsername(), "userServiceTest");
        assertFalse(normalUser.isSuperUser());

        assertEquals(0, normalUser.getUserProperties().size());
        
        User user1 = userService.getUserByUuid("0bc18050-e132-11de-babe-001e378eb67f"); 
        assertNotNull(user1);
        assertEquals("", user1.getUserProperty(DEMO_LOCATION_USER_PROPERTY)); 
        assertEquals(normalUser, user1); //Users can access themselves
        
        User user2 = userService.getUserByUuid("0bc28050-e132-11de-babe-001e378eb67f");
        assertNull(user2);
        
        User user3 = userService.getUserByUuid("0bc38050-e132-11de-babe-001e378eb67f");
        assertNull(user3);
        
        User user4 = userService.getUserByUuid("0bc48050-e132-11de-babe-001e378eb67f");
        assertNull(user4);
    }
    
    @Test
    public void getUserByUuid_nonAdminUserWithLocationPropertyShouldGetOnlyAccessibleUserInformation(){
        User normalUser = userService.getUser(DEMO_USER2_ID);
        Context.authenticate(normalUser.getUsername(), "userServiceTest");
        assertFalse(normalUser.isSuperUser());

        Location location1 = locationService.getLocation(DEMO_LOCATION1_ID);

        assertEquals(1, normalUser.getUserProperties().size());
        String locationUserProperty = normalUser.getUserProperty("locationUuid");
        assertNotNull(locationUserProperty);
        assertEquals(location1.getUuid(), locationUserProperty);
        
        User user1 = userService.getUserByUuid("0bc18050-e132-11de-babe-001e378eb67f");
        assertNull(user1);
        
        User user2 = userService.getUserByUuid("0bc28050-e132-11de-babe-001e378eb67f");
        assertNotNull(user2);
        assertEquals(location1.getUuid(), user2.getUserProperty(DEMO_LOCATION_USER_PROPERTY));
        assertEquals(normalUser, user2); //Users can access themselves
        
        User user3 = userService.getUserByUuid("0bc38050-e132-11de-babe-001e378eb67f");
        assertNull(user3);
        
        User user4 = userService.getUserByUuid("0bc48050-e132-11de-babe-001e378eb67f");
        assertNotNull(user4);
        assertEquals(location1.getUuid(), user4.getUserProperty(DEMO_LOCATION_USER_PROPERTY));
        assertNotEquals(user4,normalUser);
    }
    
    @Test
    public void getUserByUsername_adminCanAccessUsersFromAllLocations(){
        Context.authenticate("admin", "test");
        User authenticatedUser = Context.getAuthenticatedUser();
        assertNotNull(authenticatedUser);
        assertTrue(authenticatedUser.isSuperUser());
        
        Location location1 = locationService.getLocation(DEMO_LOCATION1_ID);
        Location location2 = locationService.getLocation(DEMO_LOCATION2_ID);
        
        User user1 = userService.getUserByUsername("username1");
        assertNotNull(user1);
        assertEquals("", user1.getUserProperty(DEMO_LOCATION_USER_PROPERTY));
        
        User user2 = userService.getUserByUsername("username2");
        assertNotNull(user2);
        assertEquals(location1.getUuid(), user2.getUserProperty(DEMO_LOCATION_USER_PROPERTY));
        
        User user3 = userService.getUserByUsername("username3");
        assertNotNull(user3);
        assertEquals(location2.getUuid(), user3.getUserProperty(DEMO_LOCATION_USER_PROPERTY));
        
        User user4 = userService.getUserByUsername("username4");
        assertNotNull(user4);
        assertEquals(location1.getUuid(), user4.getUserProperty(DEMO_LOCATION_USER_PROPERTY));
    }
    
    @Test
    public void getUserByUsername_nonAdminUserWithOutLocationPropertyShouldNotGetUserInformation(){
        User normalUser = userService.getUser(DEMO_USER1_ID);
        Context.authenticate(normalUser.getUsername(), "userServiceTest");
        assertFalse(normalUser.isSuperUser());

        assertEquals(0, normalUser.getUserProperties().size());
        
        User user1 = userService.getUserByUsername("username1"); 
        assertNotNull(user1);
        assertEquals("", user1.getUserProperty(DEMO_LOCATION_USER_PROPERTY));
        assertEquals(normalUser, user1); //Users can access themselves
        
        User user2 = userService.getUserByUsername("username2");
        assertNull(user2);
        
        User user3 = userService.getUserByUsername("username3");
        assertNull(user3);
        
        User user4 = userService.getUserByUsername("username4");
        assertNull(user4);
    }
    
    @Test
    public void getUserByUsername_nonAdminUserWithLocationPropertyShouldGetOnlyAccessibleUserInformation(){
        User normalUser = userService.getUser(DEMO_USER2_ID);
        Context.authenticate(normalUser.getUsername(), "userServiceTest");
        assertFalse(normalUser.isSuperUser());

        Location location1 = locationService.getLocation(DEMO_LOCATION1_ID);

        assertEquals(1, normalUser.getUserProperties().size());
        String locationUserProperty = normalUser.getUserProperty("locationUuid");
        assertNotNull(locationUserProperty);
        assertEquals(location1.getUuid(), locationUserProperty);
        
        User user1 = userService.getUserByUsername("username1");
        assertNull(user1);
        
        User user2 = userService.getUserByUsername("username2");
        assertNotNull(user2);
        assertEquals(location1.getUuid(), user2.getUserProperty(DEMO_LOCATION_USER_PROPERTY));
        assertEquals(normalUser, user2); //Users can access themselves
        
        User user3 = userService.getUserByUsername("username3");
        assertNull(user3);
        
        User user4 = userService.getUserByUsername("username4");
        assertNotNull(user4);
        assertEquals(location1.getUuid(), user4.getUserProperty(DEMO_LOCATION_USER_PROPERTY));
        assertNotEquals(user4,normalUser);
    }
    
    @Test
    public void getUserByName_adminCanAccessUsersFromAllLocations(){
        Context.authenticate("admin", "test");
        User authenticatedUser = Context.getAuthenticatedUser();
        assertNotNull(authenticatedUser);
        assertTrue(authenticatedUser.isSuperUser());
        
        Location location1 = locationService.getLocation(DEMO_LOCATION1_ID);
        Location location2 = locationService.getLocation(DEMO_LOCATION2_ID);
        
        List<User> userList = userService.getUsersByName("User","accessLocation",false);
        assertEquals(4, userList.size());
        User testingUser1 = userList.get(0);
        User testingUser2 = userList.get(1);
        User testingUser3 = userList.get(2);
        User testingUser4 = userList.get(3);
        assertNotNull(testingUser1);
        assertEquals("", testingUser1.getUserProperty(DEMO_LOCATION_USER_PROPERTY));
        assertNotNull(testingUser2);
        assertEquals(location1.getUuid(), testingUser2.getUserProperty(DEMO_LOCATION_USER_PROPERTY));
        assertNotNull(testingUser3);
        assertEquals(location2.getUuid(), testingUser3.getUserProperty(DEMO_LOCATION_USER_PROPERTY));
        assertNotNull(testingUser4);
        assertEquals(location1.getUuid(), testingUser4.getUserProperty(DEMO_LOCATION_USER_PROPERTY));
    }
    
    @Test
    public void getUserByName_nonAdminUserWithOutLocationPropertyShouldNotGetUserInformation(){
        User normalUser = userService.getUser(DEMO_USER1_ID);
        Context.authenticate(normalUser.getUsername(), "userServiceTest");
        assertFalse(normalUser.isSuperUser());

        assertEquals(0, normalUser.getUserProperties().size());
        
        List<User> userList = userService.getUsersByName("User","accessLocation",false);
        assertEquals(1, userList.size());
        User testingUser = userList.get(0);
        assertNotNull(testingUser); 
        assertEquals("", testingUser.getUserProperty(DEMO_LOCATION_USER_PROPERTY));
        assertEquals(normalUser, testingUser); //Users can access themselves
    }
    
    @Test
    public void getUserByName_nonAdminUserWithLocationPropertyShouldGetOnlyAccessibleUserInformation(){
        User normalUser = userService.getUser(DEMO_USER2_ID);
        Context.authenticate(normalUser.getUsername(), "userServiceTest");
        assertFalse(normalUser.isSuperUser());

        Location location1 = locationService.getLocation(DEMO_LOCATION1_ID);

        assertEquals(1, normalUser.getUserProperties().size());
        String locationUserProperty = normalUser.getUserProperty("locationUuid");
        assertNotNull(locationUserProperty);
        assertEquals(location1.getUuid(), locationUserProperty);
        
        List<User> userList = userService.getUsersByName("User","accessLocation",false);
        assertEquals(2, userList.size());
        User testingUser1 = userList.get(0);
        User testingUser2 = userList.get(1);
        assertNotNull(testingUser1);
        assertEquals(location1.getUuid(), testingUser1.getUserProperty(DEMO_LOCATION_USER_PROPERTY));
        assertEquals(normalUser, testingUser1); //Users can access themselves
        assertNotNull(testingUser2);
        assertEquals(location1.getUuid(), testingUser2.getUserProperty(DEMO_LOCATION_USER_PROPERTY));
        assertNotEquals(testingUser2,normalUser);
    }
    
    @Test
    public void getUserByPerson_adminCanAccessUsersFromAllLocations(){
        Context.authenticate("admin", "test");
        User authenticatedUser = Context.getAuthenticatedUser();
        assertNotNull(authenticatedUser);
        assertTrue(authenticatedUser.isSuperUser());
        
        Location location1 = locationService.getLocation(DEMO_LOCATION1_ID);
        Location location2 = locationService.getLocation(DEMO_LOCATION2_ID);
        
        Person person1 = personService.getPerson(DEMO_PERSON1_ID);
        List<User> userList1 = userService.getUsersByPerson(person1,false);
        assertEquals(1, userList1.size());
        User testingUser1 = userList1.get(0);
        assertNotNull(testingUser1);
        assertEquals("", testingUser1.getUserProperty(DEMO_LOCATION_USER_PROPERTY));
        
        Person person2 = personService.getPerson(DEMO_PERSON2_ID);
        List<User> userList2 = userService.getUsersByPerson(person2,false);
        assertEquals(1, userList2.size());
        User testingUser2 = userList2.get(0);
        assertNotNull(testingUser2);
        assertEquals(location1.getUuid(), testingUser2.getUserProperty(DEMO_LOCATION_USER_PROPERTY));
        
        Person person3 = personService.getPerson(DEMO_PERSON3_ID);
        List<User> userList3 = userService.getUsersByPerson(person3,false);
        assertEquals(1, userList3.size());
        User testingUser3 = userList3.get(0);
        assertNotNull(testingUser3);
        assertEquals(location2.getUuid(), testingUser3.getUserProperty(DEMO_LOCATION_USER_PROPERTY));
        
        Person person4 = personService.getPerson(DEMO_PERSON4_ID);
        List<User> userList4 = userService.getUsersByPerson(person4,false);
        assertEquals(1, userList4.size());
        User testingUser4 = userList4.get(0);
        assertNotNull(testingUser4);
        assertEquals(location1.getUuid(), testingUser4.getUserProperty(DEMO_LOCATION_USER_PROPERTY));
    }
    
    @Test
    public void getUserByPerson_nonAdminUserWithOutLocationPropertyShouldNotGetUserInformation(){
        User normalUser = userService.getUser(DEMO_USER1_ID);
        Context.authenticate(normalUser.getUsername(), "userServiceTest");
        assertFalse(normalUser.isSuperUser());

        assertEquals(0, normalUser.getUserProperties().size());
        
        Person person1 = personService.getPerson(DEMO_PERSON1_ID);
        List<User> userList1 = userService.getUsersByPerson(person1,false);
        assertEquals(1, userList1.size());
        User testingUser = userList1.get(0);
        assertNotNull(testingUser); 
        assertEquals("", testingUser.getUserProperty(DEMO_LOCATION_USER_PROPERTY));
        assertEquals(normalUser, testingUser); //Users can access themselves
        
        Person person2 = personService.getPerson(DEMO_PERSON2_ID);
        List<User> userList2 = userService.getUsersByPerson(person2,false);
        assertEquals(0, userList2.size());
        
        Person person3 = personService.getPerson(DEMO_PERSON3_ID);
        List<User> userList3 = userService.getUsersByPerson(person3,false);
        assertEquals(0, userList3.size());
        
        Person person4 = personService.getPerson(DEMO_PERSON4_ID);
        List<User> userList4 = userService.getUsersByPerson(person4,false);
        assertEquals(0, userList4.size());
    }
    
    @Test
    public void getUserByPerson_nonAdminUserWithLocationPropertyShouldGetOnlyAccessibleUserInformation(){
        User normalUser = userService.getUser(DEMO_USER2_ID);
        Context.authenticate(normalUser.getUsername(), "userServiceTest");
        assertFalse(normalUser.isSuperUser());

        Location location1 = locationService.getLocation(DEMO_LOCATION1_ID);

        assertEquals(1, normalUser.getUserProperties().size());
        String locationUserProperty = normalUser.getUserProperty("locationUuid");
        assertNotNull(locationUserProperty);
        assertEquals(location1.getUuid(), locationUserProperty);
        
        Person person1 = personService.getPerson(DEMO_PERSON1_ID);
        List<User> userList1 = userService.getUsersByPerson(person1,false);
        assertEquals(0, userList1.size());
        
        Person person2 = personService.getPerson(DEMO_PERSON2_ID);
        List<User> userList2 = userService.getUsersByPerson(person2,false);
        assertEquals(1, userList2.size());
        User testingUser2 = userList2.get(0);
        assertNotNull(testingUser2);
        assertEquals(location1.getUuid(), testingUser2.getUserProperty(DEMO_LOCATION_USER_PROPERTY));
        assertEquals(normalUser, testingUser2); //Users can access themselves
        
        Person person3 = personService.getPerson(DEMO_PERSON3_ID);
        List<User> userList3 = userService.getUsersByPerson(person3,false);
        assertEquals(0,userList3.size());

        Person person4 = personService.getPerson(DEMO_PERSON4_ID);
        List<User> userList4 = userService.getUsersByPerson(person4,false);
        assertEquals(1, userList4.size());
        User testingUser4 = userList4.get(0);
        assertNotNull(testingUser4);
        assertEquals(location1.getUuid(), testingUser4.getUserProperty(DEMO_LOCATION_USER_PROPERTY));
        assertNotEquals(testingUser4,normalUser);
    }
}
