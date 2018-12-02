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

import java.util.List;
import java.util.Map;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.Cohort;
import org.openmrs.Encounter;
import org.openmrs.Location;
import org.openmrs.Patient;
import org.openmrs.User;
import org.openmrs.api.EncounterService;
import org.openmrs.api.LocationService;
import org.openmrs.api.UserService;
import org.openmrs.api.PatientService;
import org.openmrs.api.context.Context;
import org.openmrs.module.locationbasedaccess.aop.common.AOPContextSensitiveTest;
import org.openmrs.module.locationbasedaccess.aop.common.TestWithAOP;
import org.openmrs.module.locationbasedaccess.aop.interceptor.UserServiceInterceptorAdvice;

public class EncounterSearchAdviserTest extends AOPContextSensitiveTest {
    
    private EncounterService encounterService;
    private UserService userService;
    private LocationService locationService;
    private PatientService patientService;
    private static final String XML_FILENAME_WITH_ADMIN_USER_DATA = "include/AdminUserData.xml";
    private static final String XML_FILENAME_WITH_PERSON_DATA = "include/PatientTestData.xml";
    
    private static final int DEMO_USER1_ID = 6001;
    private static final int DEMO_USER2_ID = 6002;
    private static final int DEMO_LOCATION1_ID = 1;
    private static final int DEMO_LOCATION2_ID = 2;
    
    public EncounterSearchAdviserTest() {
    }
    
    @Override
    protected void setInterceptorAndServices(TestWithAOP testCase) {
        testCase.setInterceptor(UserServiceInterceptorAdvice.class);
        testCase.addService(UserService.class);
    }
    
    @Before
    public void setUp()  throws Exception {
        encounterService = Context.getEncounterService();
        userService = Context.getUserService();
        locationService = Context.getLocationService();
        patientService = Context.getPatientService();
        executeDataSet(XML_FILENAME_WITH_ADMIN_USER_DATA);
        executeDataSet(XML_FILENAME_WITH_PERSON_DATA);
    }
    
    @Test
    public void getEncounter_adminCanAccessEncounterFromAllLocations() {
        Context.authenticate("admin", "test");
        User authenticatedUser = Context.getAuthenticatedUser();
        assertNotNull(authenticatedUser);
        assertTrue(authenticatedUser.isSuperUser());
        
        Location location1 = locationService.getLocation(DEMO_LOCATION1_ID);
        Location location2 = locationService.getLocation(DEMO_LOCATION2_ID);
        
        Encounter encounter3 = encounterService.getEncounter(3);
        assertNotNull(encounter3);
        assertEquals(encounter3.getLocation(),location1);
        
        Encounter encounter4 = encounterService.getEncounter(4);
        assertNotNull(encounter4);
        assertEquals(encounter4.getLocation(),location1);
        
        Encounter encounter5 = encounterService.getEncounter(5);
        assertNotNull(encounter5);
        assertEquals(encounter5.getLocation(),location2);
        
        Encounter encounter6 = encounterService.getEncounter(6);
        assertNotNull(encounter6);
        assertEquals(encounter6.getLocation(),location2);     
    }
    
    @Test
    public void getEncounter_nonAdminUserWithOutLocationPropertyShouldNotGetEncounterInformation(){
        User normalUser = userService.getUser(DEMO_USER1_ID);
        Context.authenticate(normalUser.getUsername(), "userServiceTest");
        assertFalse(normalUser.isSuperUser());
        
        Encounter encounter3 = encounterService.getEncounter(3);
        assertNotNull(encounter3);
        
        Encounter encounter4 = encounterService.getEncounter(4);
        assertNotNull(encounter4);
        
        Encounter encounter5 = encounterService.getEncounter(5);
        assertNotNull(encounter5);
        
        Encounter encounter6 = encounterService.getEncounter(6);
        assertNotNull(encounter6);
    }
    
    @Test
    public void getEncounter_nonAdminUserWithLocationPropertyShouldGetOnlyAccessibleEncounterInformation(){
        User normalUser = userService.getUser(DEMO_USER2_ID);
        Context.authenticate(normalUser.getUsername(), "userServiceTest");
        assertFalse(normalUser.isSuperUser());

        Location location1 = locationService.getLocation(DEMO_LOCATION1_ID);

        assertEquals(normalUser.getUserProperties().size(), 1);
        String locationUserProperty = normalUser.getUserProperty("locationUuid");
        assertNotNull(locationUserProperty);
        assertEquals(locationUserProperty, location1.getUuid());
        
        Encounter encounter3 = encounterService.getEncounter(3);
        assertNotNull(encounter3);
        
        Encounter encounter4 = encounterService.getEncounter(4);
        assertNotNull(encounter4);
        
        Encounter encounter5 = encounterService.getEncounter(5);
        assertNotNull(encounter5);
        
        Encounter encounter6 = encounterService.getEncounter(6);
        assertNotNull(encounter6);
    }
    
    @Test
    public void getEncounterByUuid_adminCanAccessEncounterFromAllLocations() {
        Context.authenticate("admin", "test");
        User authenticatedUser = Context.getAuthenticatedUser();
        assertNotNull(authenticatedUser);
        assertTrue(authenticatedUser.isSuperUser());
        
        Location location1 = locationService.getLocation(DEMO_LOCATION1_ID);
        Location location2 = locationService.getLocation(DEMO_LOCATION2_ID);
        
        Encounter encounter1 = encounterService.getEncounterByUuid("6519d653-393b-4118-9c83-a3715b82d4ac");
        assertNotNull(encounter1);
        assertEquals(encounter1.getLocation(),location1);
        
        Encounter encounter2 = encounterService.getEncounterByUuid("eec646cb-c847-45a7-98bc-91c8c4f70add");
        assertNotNull(encounter2);
        assertEquals(encounter2.getLocation(),location1);
        
        Encounter encounter3 = encounterService.getEncounterByUuid("e403fafb-e5e4-42d0-9d11-4f52e89d148c");
        assertNotNull(encounter3);
        assertEquals(encounter3.getLocation(),location2);
        
        Encounter encounter4 = encounterService.getEncounterByUuid("y403fafb-e5e4-42d0-9d11-4f52e89d123r");
        assertNotNull(encounter4);
        assertEquals(encounter4.getLocation(),location2);
    }
    
    @Test
    public void getEncounterByUuid_nonAdminUserWithOutLocationPropertyShouldNotGetEncounterInformation(){
        User normalUser = userService.getUser(DEMO_USER1_ID);
        Context.authenticate(normalUser.getUsername(), "userServiceTest");
        assertFalse(normalUser.isSuperUser());
              
    }
    
    @Test
    public void getEncounterByUuid_nonAdminUserWithLocationPropertyShouldGetOnlyAccessibleEncounterInformation(){
        User normalUser = userService.getUser(DEMO_USER2_ID);
        Context.authenticate(normalUser.getUsername(), "userServiceTest");
        assertFalse(normalUser.isSuperUser());

        Location location1 = locationService.getLocation(DEMO_LOCATION1_ID);

        assertEquals(normalUser.getUserProperties().size(), 1);
        String locationUserProperty = normalUser.getUserProperty("locationUuid");
        assertNotNull(locationUserProperty);
        assertEquals(locationUserProperty, location1.getUuid());
    }
    
    @Test
    public void getEncounters_adminCanAccessEncounterFromAllLocations() {
        Context.authenticate("admin", "test");
        User authenticatedUser = Context.getAuthenticatedUser();
        assertNotNull(authenticatedUser);
        assertTrue(authenticatedUser.isSuperUser());
        
        Location location1 = locationService.getLocation(DEMO_LOCATION1_ID);
        Location location2 = locationService.getLocation(DEMO_LOCATION2_ID);
        
        //EncounterSearchCriteria esc = new EncounterSearchCriteria("Patient",location1);
        
    }
    
    @Test
    public void getEncounters_nonAdminUserWithOutLocationPropertyShouldNotGetEncounterInformation(){
        User normalUser = userService.getUser(DEMO_USER1_ID);
        Context.authenticate(normalUser.getUsername(), "userServiceTest");
        assertFalse(normalUser.isSuperUser());
              
    }
    
    @Test
    public void getEncounters_nonAdminUserWithLocationPropertyShouldGetOnlyAccessibleEncounterInformation(){
        User normalUser = userService.getUser(DEMO_USER2_ID);
        Context.authenticate(normalUser.getUsername(), "userServiceTest");
        assertFalse(normalUser.isSuperUser());

        Location location1 = locationService.getLocation(DEMO_LOCATION1_ID);

        assertEquals(normalUser.getUserProperties().size(), 1);
        String locationUserProperty = normalUser.getUserProperty("locationUuid");
        assertNotNull(locationUserProperty);
        assertEquals(locationUserProperty, location1.getUuid());
    }
    
    @Test
    public void getEncountersByPatientId_adminCanAccessEncounterFromAllLocations() {
        Context.authenticate("admin", "test");
        User authenticatedUser = Context.getAuthenticatedUser();
        assertNotNull(authenticatedUser);
        assertTrue(authenticatedUser.isSuperUser());
        
        Location location1 = locationService.getLocation(DEMO_LOCATION1_ID);
        Location location2 = locationService.getLocation(DEMO_LOCATION2_ID);
        
        List<Encounter> encounterList1 = encounterService.getEncountersByPatientId(7);
        Encounter encounter1 = encounterList1.get(0);
        Encounter encounter2 = encounterList1.get(1);
        Encounter encounter3 = encounterList1.get(2);
        assertNotNull(encounter1);
        assertEquals(encounter1.getLocation(),location2);
        assertNotNull(encounter2);
        assertEquals(encounter2.getLocation(),location1);
        assertNotNull(encounter3);
        assertEquals(encounter3.getLocation(),location1);
        
        List<Encounter> encounterList2 = encounterService.getEncountersByPatientId(2);
        Encounter encounter4 = encounterList2.get(0);
        assertNotNull(encounter4);
        assertEquals(encounter4.getLocation(),location2);
    }
            
    @Test
    public void getEncountersByPatientId_nonAdminUserWithOutLocationPropertyShouldNotGetEncounterInformation(){
        User normalUser = userService.getUser(DEMO_USER1_ID);
        Context.authenticate(normalUser.getUsername(), "userServiceTest");
        assertFalse(normalUser.isSuperUser());
              
    }
            
    @Test
    public void getEncountersByPatientId_nonAdminUserWithLocationPropertyShouldGetOnlyAccessibleEncounterInformation(){
        User normalUser = userService.getUser(DEMO_USER2_ID);
        Context.authenticate(normalUser.getUsername(), "userServiceTest");
        assertFalse(normalUser.isSuperUser());

        Location location1 = locationService.getLocation(DEMO_LOCATION1_ID);

        assertEquals(normalUser.getUserProperties().size(), 1);
        String locationUserProperty = normalUser.getUserProperty("locationUuid");
        assertNotNull(locationUserProperty);
        assertEquals(locationUserProperty, location1.getUuid());
    }
    
    @Test
    public void getEncountersByPatient_adminCanAccessEncounterFromAllLocations() {
        Context.authenticate("admin", "test");
        User authenticatedUser = Context.getAuthenticatedUser();
        assertNotNull(authenticatedUser);
        assertTrue(authenticatedUser.isSuperUser());
        
        Location location2 = locationService.getLocation(DEMO_LOCATION2_ID);
        
        Patient patient = patientService.getPatientByUuid("5631b434-78aa-102b-91a0-001e378eb67e");
        List<Encounter> encounterList = encounterService.getEncountersByPatient(patient);
        Encounter encounter = encounterList.get(0);
        assertNotNull(encounter);
        assertEquals(encounter.getLocation(),location2);
    }
    
    @Test
    public void getEncountersByPatient_nonAdminUserWithOutLocationPropertyShouldNotGetEncounterInformation(){
        User normalUser = userService.getUser(DEMO_USER1_ID);
        Context.authenticate(normalUser.getUsername(), "userServiceTest");
        assertFalse(normalUser.isSuperUser());
              
    }
    
    @Test
    public void getEncountersByPatient_nonAdminUserWithLocationPropertyShouldGetOnlyAccessibleEncounterInformation(){
        User normalUser = userService.getUser(DEMO_USER2_ID);
        Context.authenticate(normalUser.getUsername(), "userServiceTest");
        assertFalse(normalUser.isSuperUser());

        Location location1 = locationService.getLocation(DEMO_LOCATION1_ID);

        assertEquals(normalUser.getUserProperties().size(), 1);
        String locationUserProperty = normalUser.getUserProperty("locationUuid");
        assertNotNull(locationUserProperty);
        assertEquals(locationUserProperty, location1.getUuid());
    }
    
    @Test
    public void getEncountersByVisit_adminCanAccessEncounterFromAllLocations() {
        Context.authenticate("admin", "test");
        User authenticatedUser = Context.getAuthenticatedUser();
        assertNotNull(authenticatedUser);
        assertTrue(authenticatedUser.isSuperUser());
        
        Location location2 = locationService.getLocation(DEMO_LOCATION2_ID);
    }
    
    @Test
    public void getEncountersByVisit_nonAdminUserWithOutLocationPropertyShouldNotGetEncounterInformation(){
        User normalUser = userService.getUser(DEMO_USER1_ID);
        Context.authenticate(normalUser.getUsername(), "userServiceTest");
        assertFalse(normalUser.isSuperUser());
              
    }
    
    @Test
    public void getEncountersByVisit_nonAdminUserWithLocationPropertyShouldGetOnlyAccessibleEncounterInformation(){
        User normalUser = userService.getUser(DEMO_USER2_ID);
        Context.authenticate(normalUser.getUsername(), "userServiceTest");
        assertFalse(normalUser.isSuperUser());

        Location location1 = locationService.getLocation(DEMO_LOCATION1_ID);

        assertEquals(normalUser.getUserProperties().size(), 1);
        String locationUserProperty = normalUser.getUserProperty("locationUuid");
        assertNotNull(locationUserProperty);
        assertEquals(locationUserProperty, location1.getUuid());
    }
    
    @Test
    public void getEncountersNotAssignedToAnyVisit_adminCanAccessEncounterFromAllLocations() {
        Context.authenticate("admin", "test");
        User authenticatedUser = Context.getAuthenticatedUser();
        assertNotNull(authenticatedUser);
        assertTrue(authenticatedUser.isSuperUser());
        
        Location location2 = locationService.getLocation(DEMO_LOCATION2_ID);
    }
    
    @Test
    public void getEncountersNotAssignedToAnyVisit_nonAdminUserWithOutLocationPropertyShouldNotGetEncounterInformation(){
        User normalUser = userService.getUser(DEMO_USER1_ID);
        Context.authenticate(normalUser.getUsername(), "userServiceTest");
        assertFalse(normalUser.isSuperUser());
              
    }
    
    @Test
    public void getEncountersNotAssignedToAnyVisit_nonAdminUserWithLocationPropertyShouldGetOnlyAccessibleEncounterInformation(){
        User normalUser = userService.getUser(DEMO_USER2_ID);
        Context.authenticate(normalUser.getUsername(), "userServiceTest");
        assertFalse(normalUser.isSuperUser());

        Location location1 = locationService.getLocation(DEMO_LOCATION1_ID);

        assertEquals(normalUser.getUserProperties().size(), 1);
        String locationUserProperty = normalUser.getUserProperty("locationUuid");
        assertNotNull(locationUserProperty);
        assertEquals(locationUserProperty, location1.getUuid());
    }
    
    @Test
    public void getEncountersByVisitsAndPatient_adminCanAccessEncounterFromAllLocations() {
        Context.authenticate("admin", "test");
        User authenticatedUser = Context.getAuthenticatedUser();
        assertNotNull(authenticatedUser);
        assertTrue(authenticatedUser.isSuperUser());
        
        Location location2 = locationService.getLocation(DEMO_LOCATION2_ID);
    }
    
    @Test
    public void getEncountersByVisitsAndPatient_nonAdminUserWithOutLocationPropertyShouldNotGetEncounterInformation(){
        User normalUser = userService.getUser(DEMO_USER1_ID);
        Context.authenticate(normalUser.getUsername(), "userServiceTest");
        assertFalse(normalUser.isSuperUser());
              
    }
    
    @Test
    public void getEncountersByVisitsAndPatient_nonAdminUserWithLocationPropertyShouldGetOnlyAccessibleEncounterInformation(){
        User normalUser = userService.getUser(DEMO_USER2_ID);
        Context.authenticate(normalUser.getUsername(), "userServiceTest");
        assertFalse(normalUser.isSuperUser());

        Location location1 = locationService.getLocation(DEMO_LOCATION1_ID);

        assertEquals(normalUser.getUserProperties().size(), 1);
        String locationUserProperty = normalUser.getUserProperty("locationUuid");
        assertNotNull(locationUserProperty);
        assertEquals(locationUserProperty, location1.getUuid());
    }
    
    @Test
    public void getAllEncounters_adminCanAccessEncounterFromAllLocations() {
        Context.authenticate("admin", "test");
        User authenticatedUser = Context.getAuthenticatedUser();
        assertNotNull(authenticatedUser);
        assertTrue(authenticatedUser.isSuperUser());
        
        Location location2 = locationService.getLocation(DEMO_LOCATION2_ID);
        
        Cohort cohort = new Cohort();
        Map<Integer, List<Encounter>> encounterList = encounterService.getAllEncounters(cohort);
    }
    
    @Test
    public void getAllEncounters_nonAdminUserWithOutLocationPropertyShouldNotGetEncounterInformation(){
        User normalUser = userService.getUser(DEMO_USER1_ID);
        Context.authenticate(normalUser.getUsername(), "userServiceTest");
        assertFalse(normalUser.isSuperUser());
    }
    
    @Test
    public void getAllEncounters_nonAdminUserWithLocationPropertyShouldGetOnlyAccessibleEncounterInformation(){
        User normalUser = userService.getUser(DEMO_USER2_ID);
        Context.authenticate(normalUser.getUsername(), "userServiceTest");
        assertFalse(normalUser.isSuperUser());

        Location location1 = locationService.getLocation(DEMO_LOCATION1_ID);

        assertEquals(normalUser.getUserProperties().size(), 1);
        String locationUserProperty = normalUser.getUserProperty("locationUuid");
        assertNotNull(locationUserProperty);
        assertEquals(locationUserProperty, location1.getUuid());
    }
}
