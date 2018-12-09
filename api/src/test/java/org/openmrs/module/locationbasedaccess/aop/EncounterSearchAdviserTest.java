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

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.Cohort;
import org.openmrs.Encounter;
import org.openmrs.Location;
import org.openmrs.Patient;
import org.openmrs.User;
import org.openmrs.Visit;
import org.openmrs.api.EncounterService;
import org.openmrs.api.LocationService;
import org.openmrs.api.UserService;
import org.openmrs.api.PatientService;
import org.openmrs.api.VisitService;
import org.openmrs.api.context.Context;
import org.openmrs.module.locationbasedaccess.aop.common.AOPContextSensitiveTest;
import org.openmrs.module.locationbasedaccess.aop.common.TestWithAOP;
import org.openmrs.module.locationbasedaccess.aop.interceptor.EncounterServiceInterceptorAdvice;
import org.openmrs.parameter.EncounterSearchCriteria;

public class EncounterSearchAdviserTest extends AOPContextSensitiveTest {
    
    private EncounterService encounterService;
    private UserService userService;
    private LocationService locationService;
    private PatientService patientService;
    private VisitService visitService;
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
        testCase.setInterceptor(EncounterServiceInterceptorAdvice.class);
        testCase.addService(EncounterService.class);
    }
    
    @Before
    public void setUp()  throws Exception {
        encounterService = Context.getEncounterService();
        userService = Context.getUserService();
        locationService = Context.getLocationService();
        patientService = Context.getPatientService();
        visitService = Context.getVisitService();
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
                
        Encounter encounter1 = encounterService.getEncounter(1);
        assertNotNull(encounter1);
        assertEquals(location1, encounter1.getLocation());
        
        Encounter encounter2 = encounterService.getEncounter(2);
        assertNotNull(encounter2);
        assertEquals(location2, encounter2.getLocation());
        
        Encounter encounter3 = encounterService.getEncounter(3);
        assertNotNull(encounter3);
        assertEquals(location1, encounter3.getLocation());
        
        Encounter encounter4 = encounterService.getEncounter(4);
        assertNotNull(encounter4);
        assertEquals(location1, encounter4.getLocation());
        
        Encounter encounter5 = encounterService.getEncounter(5);
        assertNotNull(encounter5);
        assertEquals(location2, encounter5.getLocation());
        
        Encounter encounter6 = encounterService.getEncounter(6);
        assertNotNull(encounter6);
        assertEquals(location2, encounter6.getLocation());
        
        Encounter encounter7 = encounterService.getEncounter(7);
        assertNotNull(encounter7);
        assertEquals(location2, encounter7.getLocation());
        
        Encounter encounter8 = encounterService.getEncounter(8);
        assertNotNull(encounter8);
        assertEquals(location2, encounter8.getLocation());
    }
    
    @Test
    public void getEncounter_nonAdminUserWithOutLocationPropertyShouldNotGetEncounterInformation(){
        User normalUser = userService.getUser(DEMO_USER1_ID);
        Context.authenticate(normalUser.getUsername(), "userServiceTest");
        assertFalse(normalUser.isSuperUser());
        assertNotNull(normalUser);
        
        assertEquals(0, normalUser.getUserProperties().size());
        String locationUserProperty = normalUser.getUserProperty("locationUuid");
        assertNotNull(locationUserProperty);
                
        Encounter encounter1 = encounterService.getEncounter(1);
        assertNull(encounter1);
        
        Encounter encounter2 = encounterService.getEncounter(2);
        assertNull(encounter2);
        
        Encounter encounter3 = encounterService.getEncounter(3);
        assertNull(encounter3);
                
        Encounter encounter4 = encounterService.getEncounter(4);
        assertNull(encounter4);
        
        Encounter encounter5 = encounterService.getEncounter(5);
        assertNull(encounter5);
        
        Encounter encounter6 = encounterService.getEncounter(6);
        assertNull(encounter6);
        
        Encounter encounter7 = encounterService.getEncounter(7);
        assertNull(encounter7);
        
        Encounter encounter8 = encounterService.getEncounter(8);
        assertNull(encounter8);
    }
    
    @Test
    public void getEncounter_nonAdminUserWithLocationPropertyShouldGetOnlyAccessibleEncounterInformation(){
        User normalUser = userService.getUser(DEMO_USER2_ID);
        Context.authenticate(normalUser.getUsername(), "userServiceTest");
        assertFalse(normalUser.isSuperUser());

        Location location1 = locationService.getLocation(DEMO_LOCATION1_ID);

        assertEquals(1, normalUser.getUserProperties().size());
        String locationUserProperty = normalUser.getUserProperty("locationUuid");
        assertNotNull(locationUserProperty);
        assertEquals(location1.getUuid(), locationUserProperty);
                       
        Encounter encounter1 = encounterService.getEncounter(1);
        assertNotNull(encounter1);
        assertEquals(location1 , encounter1.getLocation() );
        
        Encounter encounter2 = encounterService.getEncounter(2);
        assertNull(encounter2);
       
        Encounter encounter3 = encounterService.getEncounter(3);
        assertNotNull(encounter3);
        assertEquals(location1 , encounter3.getLocation() );
        
        Encounter encounter4 = encounterService.getEncounter(4);
        assertNotNull(encounter4);
        assertEquals(location1, encounter4.getLocation());
        
        Encounter encounter5 = encounterService.getEncounter(5);
        assertNull(encounter5);
        
        Encounter encounter6 = encounterService.getEncounter(6);
        assertNull(encounter6);
        
        Encounter encounter7 = encounterService.getEncounter(7);
        assertNull(encounter7);
        
        Encounter encounter8 = encounterService.getEncounter(8);
        assertNull(encounter8);
    }
    
    @Test
    public void getEncounterByUuid_adminCanAccessEncounterFromAllLocations() {
        Context.authenticate("admin", "test");
        User authenticatedUser = Context.getAuthenticatedUser();
        assertNotNull(authenticatedUser);
        assertTrue(authenticatedUser.isSuperUser());
        
        Location location1 = locationService.getLocation(DEMO_LOCATION1_ID);
        Location location2 = locationService.getLocation(DEMO_LOCATION2_ID);
        
        Encounter encounter1 = encounterService.getEncounterByUuid("7519d653-393b-4118-9c83-a3715b82d4ac");
        assertNotNull(encounter1);
        assertEquals(location1 , encounter1.getLocation());
        
        Encounter encounter2 = encounterService.getEncounterByUuid("8519d653-393b-4118-9c83-a3715b82d4ac");
        assertNotNull(encounter2);
        assertEquals(location2, encounter2.getLocation());
        
        Encounter encounter3 = encounterService.getEncounterByUuid("6519d653-393b-4118-9c83-a3715b82d4ac");
        assertNotNull(encounter3);
        assertEquals(location1, encounter3.getLocation());
        
        Encounter encounter4 = encounterService.getEncounterByUuid("eec646cb-c847-45a7-98bc-91c8c4f70add");
        assertNotNull(encounter4);
        assertEquals(location1, encounter4.getLocation());
        
        Encounter encounter5 = encounterService.getEncounterByUuid("e403fafb-e5e4-42d0-9d11-4f52e89d148c");
        assertNotNull(encounter5);
        assertEquals(location2, encounter5.getLocation());
        
        Encounter encounter6 = encounterService.getEncounterByUuid("y403fafb-e5e4-42d0-9d11-4f52e89d123r");
        assertNotNull(encounter6);
        assertEquals(location2, encounter6.getLocation());
        
        Encounter encounter7 = encounterService.getEncounterByUuid("9519d653-393b-4118-9c83-a3715b82d4ac");
        assertNotNull(encounter7);
        assertEquals(location2,encounter7.getLocation());
        
        Encounter encounter8 = encounterService.getEncounterByUuid("0619d653-393b-4118-9c83-a3715b82d4ac");
        assertNotNull(encounter8);
        assertEquals(location2, encounter8.getLocation());
    }
    
    @Test
    public void getEncounterByUuid_nonAdminUserWithOutLocationPropertyShouldNotGetEncounterInformation(){
        User normalUser = userService.getUser(DEMO_USER1_ID);
        Context.authenticate(normalUser.getUsername(), "userServiceTest");
        assertFalse(normalUser.isSuperUser());
        assertNotNull(normalUser);
        
        assertEquals(0, normalUser.getUserProperties().size());
        String locationUserProperty = normalUser.getUserProperty("locationUuid");
        assertNotNull(locationUserProperty);
        
        Encounter encounter1 = encounterService.getEncounterByUuid("7519d653-393b-4118-9c83-a3715b82d4ac");
        assertNull(encounter1);
        
        Encounter encounter2 = encounterService.getEncounterByUuid("8519d653-393b-4118-9c83-a3715b82d4ac");
        assertNull(encounter2);
        
        Encounter encounter3 = encounterService.getEncounterByUuid("6519d653-393b-4118-9c83-a3715b82d4ac");
        assertNull(encounter3);
        
        Encounter encounter4 = encounterService.getEncounterByUuid("eec646cb-c847-45a7-98bc-91c8c4f70add");
        assertNull(encounter4);
        
        Encounter encounter5 = encounterService.getEncounterByUuid("e403fafb-e5e4-42d0-9d11-4f52e89d148c");
        assertNull(encounter5);
        
        Encounter encounter6 = encounterService.getEncounterByUuid("y403fafb-e5e4-42d0-9d11-4f52e89d123r");
        assertNull(encounter6);
        
        Encounter encounter7 = encounterService.getEncounterByUuid("9519d653-393b-4118-9c83-a3715b82d4ac");
        assertNull(encounter7);
        
        Encounter encounter8 = encounterService.getEncounterByUuid("0619d653-393b-4118-9c83-a3715b82d4ac");
        assertNull(encounter8);             
    }
    
    @Test
    public void getEncounterByUuid_nonAdminUserWithLocationPropertyShouldGetOnlyAccessibleEncounterInformation(){
        User normalUser = userService.getUser(DEMO_USER2_ID);
        Context.authenticate(normalUser.getUsername(), "userServiceTest");
        assertFalse(normalUser.isSuperUser());

        Location location1 = locationService.getLocation(DEMO_LOCATION1_ID);

        assertEquals(1, normalUser.getUserProperties().size());
        String locationUserProperty = normalUser.getUserProperty("locationUuid");
        assertNotNull(locationUserProperty);
        assertEquals(location1.getUuid(), locationUserProperty);
              
        Encounter encounter1 = encounterService.getEncounterByUuid("7519d653-393b-4118-9c83-a3715b82d4ac");
        assertNotNull(encounter1);
        assertEquals(location1, encounter1.getLocation());
        
        Encounter encounter2 = encounterService.getEncounterByUuid("8519d653-393b-4118-9c83-a3715b82d4ac");
        assertNull(encounter2);
        
        Encounter encounter3 = encounterService.getEncounterByUuid("6519d653-393b-4118-9c83-a3715b82d4ac");
        assertNotNull(encounter3);
        assertEquals(location1, encounter3.getLocation());
        
        Encounter encounter4 = encounterService.getEncounterByUuid("eec646cb-c847-45a7-98bc-91c8c4f70add");
        assertNotNull(encounter4);
        assertEquals(location1, encounter4.getLocation());
        
        Encounter encounter5 = encounterService.getEncounterByUuid("e403fafb-e5e4-42d0-9d11-4f52e89d148c");
        assertNull(encounter5);
        
        Encounter encounter6 = encounterService.getEncounterByUuid("y403fafb-e5e4-42d0-9d11-4f52e89d123r");
        assertNull(encounter6);
        
        Encounter encounter7 = encounterService.getEncounterByUuid("9519d653-393b-4118-9c83-a3715b82d4ac");
        assertNull(encounter7);
        
        Encounter encounter8 = encounterService.getEncounterByUuid("0619d653-393b-4118-9c83-a3715b82d4ac");
        assertNull(encounter8);
    }
    
    @Test
    public void getEncounters_adminCanAccessEncounterFromAllLocations() {
        Context.authenticate("admin", "test");
        User authenticatedUser = Context.getAuthenticatedUser();
        assertNotNull(authenticatedUser);
        assertTrue(authenticatedUser.isSuperUser());
        
        Location location1 = locationService.getLocation(DEMO_LOCATION1_ID);
        Location location2 = locationService.getLocation(DEMO_LOCATION2_ID);
        
        Patient patient1 = patientService.getPatient(2);
        EncounterSearchCriteria esc1 = new EncounterSearchCriteria(patient1,null,null,null,null,null,null,null,null,null,false);
        List<Encounter> encounterList1 = encounterService.getEncounters(esc1);
        assertEquals(5, encounterList1.size());
        Encounter encounter1 = encounterList1.get(0);
        Encounter encounter2 = encounterList1.get(1);
        Encounter encounter3 = encounterList1.get(2);
        Encounter encounter4 = encounterList1.get(3);
        Encounter encounter5 = encounterList1.get(4);
        assertNotNull(encounter1);
        assertEquals(location1, encounter1.getLocation());
        assertNotNull(encounter2);
        assertEquals(location2, encounter2.getLocation());
        assertNotNull(encounter3);
        assertEquals(location2, encounter3.getLocation());
        assertNotNull(encounter4);
        assertEquals(location2, encounter4.getLocation());
        assertNotNull(encounter5);
        assertEquals(location2, encounter5.getLocation());
        
        Patient patient2 = patientService.getPatient(7);
        EncounterSearchCriteria esc2 = new EncounterSearchCriteria(patient2,null,null,null,null,null,null,null,null,null,false);
        List<Encounter> encounterList2 = encounterService.getEncounters(esc2);
        assertEquals(3, encounterList2.size());
        Encounter encounter6 = encounterList2.get(0);
        Encounter encounter7 = encounterList2.get(1);
        Encounter encounter8 = encounterList2.get(2);
        assertNotNull(encounter6);
        assertEquals(location1, encounter6.getLocation());
        assertNotNull(encounter7);
        assertEquals(location1, encounter7.getLocation());
        assertNotNull(encounter8);
        assertEquals(location2, encounter8.getLocation());
    }
    
    @Test
    public void getEncounters_nonAdminUserWithOutLocationPropertyShouldNotGetEncounterInformation(){
        User normalUser = userService.getUser(DEMO_USER1_ID);
        Context.authenticate(normalUser.getUsername(), "userServiceTest");
        assertFalse(normalUser.isSuperUser());
        assertNotNull(normalUser);
        
        assertEquals(0, normalUser.getUserProperties().size());
        String locationUserProperty = normalUser.getUserProperty("locationUuid");
        assertNotNull(locationUserProperty);
        
        Patient patient1 = patientService.getPatient(2);
        EncounterSearchCriteria esc1 = new EncounterSearchCriteria(patient1,null,null,null,null,null,null,null,null,null,false);
        List<Encounter> encounterList1 = encounterService.getEncounters(esc1);
        assertEquals(0 , encounterList1.size());
        
        Patient patient2 = patientService.getPatient(7);
        EncounterSearchCriteria esc2 = new EncounterSearchCriteria(patient2,null,null,null,null,null,null,null,null,null,false);
        List<Encounter> encounterList2 = encounterService.getEncounters(esc2);
        assertEquals(0, encounterList2.size());
    }
    
    @Test
    public void getEncounters_nonAdminUserWithLocationPropertyShouldGetOnlyAccessibleEncounterInformation(){
        User normalUser = userService.getUser(DEMO_USER2_ID);
        Context.authenticate(normalUser.getUsername(), "userServiceTest");
        assertFalse(normalUser.isSuperUser());

        Location location1 = locationService.getLocation(DEMO_LOCATION1_ID);

        assertEquals(1, normalUser.getUserProperties().size());
        String locationUserProperty = normalUser.getUserProperty("locationUuid");
        assertNotNull(locationUserProperty);
        assertEquals(location1.getUuid(), locationUserProperty);
        
        Patient patient1 = patientService.getPatient(2);
        EncounterSearchCriteria esc1 = new EncounterSearchCriteria(patient1,null,null,null,null,null,null,null,null,null,false);
        List<Encounter> encounterList1 = encounterService.getEncounters(esc1);
        assertEquals(1, encounterList1.size());
        Encounter encounter1 = encounterList1.get(0);
        assertNotNull(encounter1);
        assertEquals(location1, encounter1.getLocation());
        
        Patient patient2 = patientService.getPatient(7);
        EncounterSearchCriteria esc2 = new EncounterSearchCriteria(patient2,null,null,null,null,null,null,null,null,null,false);
        List<Encounter> encounterList2 = encounterService.getEncounters(esc2);
        assertEquals(2,encounterList2.size());
        Encounter encounter2 = encounterList2.get(0);
        Encounter encounter3 = encounterList2.get(1);
        assertNotNull(encounter2);
        assertEquals(location1, encounter2.getLocation());
        assertNotNull(encounter3);
        assertEquals(location1, encounter3.getLocation());
    }
    
    @Test
    public void getEncountersByPatientId_adminCanAccessEncounterFromAllLocations() {
        Context.authenticate("admin", "test");
        User authenticatedUser = Context.getAuthenticatedUser();
        assertNotNull(authenticatedUser);
        assertTrue(authenticatedUser.isSuperUser());
        
        Location location1 = locationService.getLocation(DEMO_LOCATION1_ID);
        Location location2 = locationService.getLocation(DEMO_LOCATION2_ID);
        
        List<Encounter> encounterList1 = encounterService.getEncountersByPatientId(2);
        assertEquals(5, encounterList1.size());
        Encounter encounter1 = encounterList1.get(0);
        Encounter encounter2 = encounterList1.get(1);
        Encounter encounter3 = encounterList1.get(2);
        Encounter encounter4 = encounterList1.get(3);
        Encounter encounter5 = encounterList1.get(4);
        assertNotNull(encounter1);
        assertEquals(location2, encounter1.getLocation());
        assertNotNull(encounter2);
        assertEquals(location1, encounter2.getLocation());
        assertNotNull(encounter3);
        assertEquals(location2, encounter3.getLocation());
        assertNotNull(encounter4);
        assertEquals(location2, encounter4.getLocation());
        assertNotNull(encounter5);
        assertEquals(location2, encounter5.getLocation());
        
        List<Encounter> encounterList2 = encounterService.getEncountersByPatientId(7);
        assertEquals(3, encounterList2.size());
        Encounter encounter6 = encounterList2.get(0);
        Encounter encounter7 = encounterList2.get(1);
        Encounter encounter8 = encounterList2.get(2);
        assertNotNull(encounter6);
        assertEquals(location2, encounter6.getLocation());
        assertNotNull(encounter7);
        assertEquals(location1, encounter7.getLocation());
        assertNotNull(encounter8);
        assertEquals(location1, encounter8.getLocation());
    }
            
    @Test
    public void getEncountersByPatientId_nonAdminUserWithOutLocationPropertyShouldNotGetEncounterInformation(){
        User normalUser = userService.getUser(DEMO_USER1_ID);
        Context.authenticate(normalUser.getUsername(), "userServiceTest");
        assertFalse(normalUser.isSuperUser());
        assertNotNull(normalUser);
        
        assertEquals(0, normalUser.getUserProperties().size());
        String locationUserProperty = normalUser.getUserProperty("locationUuid");
        assertNotNull(locationUserProperty);
        
        List<Encounter> encounterList1 = encounterService.getEncountersByPatientId(2);
        assertEquals(0, encounterList1.size());
        
        List<Encounter> encounterList2 = encounterService.getEncountersByPatientId(7);
        assertEquals(0, encounterList2.size());           
    }
            
    @Test
    public void getEncountersByPatientId_nonAdminUserWithLocationPropertyShouldGetOnlyAccessibleEncounterInformation(){
        User normalUser = userService.getUser(DEMO_USER2_ID);
        Context.authenticate(normalUser.getUsername(), "userServiceTest");
        assertFalse(normalUser.isSuperUser());

        Location location1 = locationService.getLocation(DEMO_LOCATION1_ID);

        assertEquals(1, normalUser.getUserProperties().size());
        String locationUserProperty = normalUser.getUserProperty("locationUuid");
        assertNotNull(locationUserProperty);
        assertEquals(location1.getUuid(), locationUserProperty);
        
        List<Encounter> encounterList1 = encounterService.getEncountersByPatientId(2);
        assertEquals(1, encounterList1.size());
        Encounter encounter1 = encounterList1.get(0);
        assertNotNull(encounter1);
        assertEquals(location1, encounter1.getLocation());
        
        List<Encounter> encounterList2 = encounterService.getEncountersByPatientId(7);
        assertEquals(2, encounterList2.size());
        Encounter encounter2 = encounterList2.get(0);
        Encounter encounter3 = encounterList2.get(1);
        assertNotNull(encounter2);
        assertEquals(location1, encounter2.getLocation());
        assertNotNull(encounter3);
        assertEquals(location1, encounter3.getLocation());
    }
    
    @Test
    public void getEncountersByPatient_adminCanAccessEncounterFromAllLocations() {
        Context.authenticate("admin", "test");
        User authenticatedUser = Context.getAuthenticatedUser();
        assertNotNull(authenticatedUser);
        assertTrue(authenticatedUser.isSuperUser());
        
        Location location1 = locationService.getLocation(DEMO_LOCATION1_ID);
        Location location2 = locationService.getLocation(DEMO_LOCATION2_ID);
        
        Patient patient1 = patientService.getPatient(2);
        List<Encounter> encounterList1 = encounterService.getEncountersByPatient(patient1);
        assertEquals(5, encounterList1.size());
        Encounter encounter1 = encounterList1.get(0);
        Encounter encounter2 = encounterList1.get(1);
        Encounter encounter3 = encounterList1.get(2);
        Encounter encounter4 = encounterList1.get(3);
        Encounter encounter5 = encounterList1.get(4);
        assertNotNull(encounter1);
        assertEquals(location1, encounter1.getLocation());
        assertNotNull(encounter2);
        assertEquals(location2, encounter2.getLocation());
        assertNotNull(encounter3);
        assertEquals(location2, encounter3.getLocation());
        assertNotNull(encounter4);
        assertEquals(location2, encounter4.getLocation());
        assertNotNull(encounter5);
        assertEquals(location2, encounter5.getLocation());
        
        Patient patient2 = patientService.getPatient(7);
        List<Encounter> encounterList2 = encounterService.getEncountersByPatient(patient2);
        assertEquals(3, encounterList2.size());
        Encounter encounter6 = encounterList2.get(0);
        Encounter encounter7 = encounterList2.get(1);
        Encounter encounter8 = encounterList2.get(2);
        assertNotNull(encounter6);
        assertEquals(location1, encounter6.getLocation());
        assertNotNull(encounter7);
        assertEquals(location1, encounter7.getLocation());
        assertNotNull(encounter8);
        assertEquals(location2, encounter8.getLocation());
    }
    
    @Test
    public void getEncountersByPatient_nonAdminUserWithOutLocationPropertyShouldNotGetEncounterInformation(){
        User normalUser = userService.getUser(DEMO_USER1_ID);
        Context.authenticate(normalUser.getUsername(), "userServiceTest");
        assertFalse(normalUser.isSuperUser());
        assertNotNull(normalUser);
        
        assertEquals(0, normalUser.getUserProperties().size());
        String locationUserProperty = normalUser.getUserProperty("locationUuid");
        assertNotNull(locationUserProperty);
        
        Patient patient1 = patientService.getPatient(2);
        List<Encounter> encounterList1 = encounterService.getEncountersByPatient(patient1);
        assertEquals(0, encounterList1.size());
        
        Patient patient2 = patientService.getPatient(7);
        List<Encounter> encounterList2 = encounterService.getEncountersByPatient(patient2);
        assertEquals(0, encounterList2.size());           
    }
    
    @Test
    public void getEncountersByPatient_nonAdminUserWithLocationPropertyShouldGetOnlyAccessibleEncounterInformation(){
        User normalUser = userService.getUser(DEMO_USER2_ID);
        Context.authenticate(normalUser.getUsername(), "userServiceTest");
        assertFalse(normalUser.isSuperUser());

        Location location1 = locationService.getLocation(DEMO_LOCATION1_ID);

        assertEquals(1, normalUser.getUserProperties().size());
        String locationUserProperty = normalUser.getUserProperty("locationUuid");
        assertNotNull(locationUserProperty);
        assertEquals(location1.getUuid(), locationUserProperty);
        
        Patient patient1 = patientService.getPatient(2);
        List<Encounter> encounterList1 = encounterService.getEncountersByPatient(patient1);
        assertEquals(1, encounterList1.size());
        Encounter encounter1 = encounterList1.get(0);
        assertNotNull(encounter1);
        assertEquals(location1, encounter1.getLocation());
        
        Patient patient2 = patientService.getPatient(7);
        List<Encounter> encounterList2 = encounterService.getEncountersByPatient(patient2);
        assertEquals(2, encounterList2.size());
        Encounter encounter2 = encounterList2.get(0);
        Encounter encounter3 = encounterList2.get(1);
        assertNotNull(encounter2);
        assertEquals(location1, encounter2.getLocation());
        assertNotNull(encounter3);
        assertEquals(location1, encounter3.getLocation());
    }
    
    @Test
    public void getEncountersByVisit_adminCanAccessEncounterFromAllLocations() {
        Context.authenticate("admin", "test");
        User authenticatedUser = Context.getAuthenticatedUser();
        assertNotNull(authenticatedUser);
        assertTrue(authenticatedUser.isSuperUser());
        
        Location location1 = locationService.getLocation(DEMO_LOCATION1_ID);
        Location location2 = locationService.getLocation(DEMO_LOCATION2_ID);
        
        Visit visit1 = visitService.getVisit(1);
        List<Encounter> encounterList1 = encounterService.getEncountersByVisit(visit1,false);
        assertEquals(2, encounterList1.size());
        Encounter encounter1 = encounterList1.get(0);
        Encounter encounter2 = encounterList1.get(1);
        assertNotNull(encounter1);
        assertEquals(location1, encounter1.getLocation());
        assertNotNull(encounter2);
        assertEquals(location2, encounter2.getLocation());
        
        Visit visit2 = visitService.getVisit(2);
        List<Encounter> encounterList2 = encounterService.getEncountersByVisit(visit2,false);
        assertEquals(1, encounterList2.size());
        Encounter encounter3 = encounterList2.get(0);
        assertNotNull(encounter3);
        assertEquals(location2, encounter3.getLocation());
    }
    
    @Test
    public void getEncountersByVisit_nonAdminUserWithOutLocationPropertyShouldNotGetEncounterInformation(){
        User normalUser = userService.getUser(DEMO_USER1_ID);
        Context.authenticate(normalUser.getUsername(), "userServiceTest");
        assertFalse(normalUser.isSuperUser());
        assertNotNull(normalUser);
        
        assertEquals(0, normalUser.getUserProperties().size());
        String locationUserProperty = normalUser.getUserProperty("locationUuid");
        assertNotNull(locationUserProperty);
        
        Visit visit1 = visitService.getVisit(1);
        List<Encounter> encounterList1 = encounterService.getEncountersByVisit(visit1,false);
        assertEquals(0, encounterList1.size());
        
        Visit visit2 = visitService.getVisit(2);
        List<Encounter> encounterList2 = encounterService.getEncountersByVisit(visit2,false);
        assertEquals(0, encounterList2.size());             
    }
    
    @Test
    public void getEncountersByVisit_nonAdminUserWithLocationPropertyShouldGetOnlyAccessibleEncounterInformation(){
        User normalUser = userService.getUser(DEMO_USER2_ID);
        Context.authenticate(normalUser.getUsername(), "userServiceTest");
        assertFalse(normalUser.isSuperUser());

        Location location1 = locationService.getLocation(DEMO_LOCATION1_ID);

        assertEquals(1, normalUser.getUserProperties().size());
        String locationUserProperty = normalUser.getUserProperty("locationUuid");
        assertNotNull(locationUserProperty);
        assertEquals(location1.getUuid(), locationUserProperty);
        
        Visit visit1 = visitService.getVisit(1);
        List<Encounter> encounterList1 = encounterService.getEncountersByVisit(visit1,false);
        assertEquals(1, encounterList1.size());
        Encounter encounter1 = encounterList1.get(0);
        assertNotNull(encounter1);
        assertEquals(location1, encounter1.getLocation());
        
        Visit visit2 = visitService.getVisit(2);
        List<Encounter> encounterList2 = encounterService.getEncountersByVisit(visit2,false);
        assertEquals(0, encounterList2.size());
    }
    
    @Test
    public void getEncountersNotAssignedToAnyVisit_adminCanAccessEncounterFromAllLocations() {
        Context.authenticate("admin", "test");
        User authenticatedUser = Context.getAuthenticatedUser();
        assertNotNull(authenticatedUser);
        assertTrue(authenticatedUser.isSuperUser());
        
        Location location1 = locationService.getLocation(DEMO_LOCATION1_ID);
        Location location2 = locationService.getLocation(DEMO_LOCATION2_ID);
        
        Patient patient1 = patientService.getPatient(2);
        List<Encounter> encounterList1 = encounterService.getEncountersNotAssignedToAnyVisit(patient1);
        assertEquals(2, encounterList1.size());
        Encounter encounter1 = encounterList1.get(0);
        Encounter encounter2 = encounterList1.get(1);
        assertNotNull(encounter1);
        assertEquals(location2, encounter1.getLocation());
        assertNotNull(encounter2);
        assertEquals(location2, encounter2.getLocation());
        
        Patient patient2 = patientService.getPatient(7);
        List<Encounter> encounterList2 = encounterService.getEncountersNotAssignedToAnyVisit(patient2);
        assertEquals(3, encounterList2.size());
        Encounter encounter3 = encounterList2.get(0);
        Encounter encounter4 = encounterList2.get(1);
        Encounter encounter5 = encounterList2.get(2);
        assertNotNull(encounter3);
        assertEquals(location2 , encounter3.getLocation());
        assertNotNull(encounter4);
        assertEquals(location1, encounter4.getLocation());
        assertNotNull(encounter5);
        assertEquals(location1, encounter5.getLocation());       
    }
    
    @Test
    public void getEncountersNotAssignedToAnyVisit_nonAdminUserWithOutLocationPropertyShouldNotGetEncounterInformation(){
        User normalUser = userService.getUser(DEMO_USER1_ID);
        Context.authenticate(normalUser.getUsername(), "userServiceTest");
        assertFalse(normalUser.isSuperUser());       
        assertNotNull(normalUser);
        
        assertEquals(0, normalUser.getUserProperties().size());
        String locationUserProperty = normalUser.getUserProperty("locationUuid");
        assertNotNull(locationUserProperty);
        
        Patient patient1 = patientService.getPatient(2);
        List<Encounter> encounterList1 = encounterService.getEncountersNotAssignedToAnyVisit(patient1);
        assertEquals(0, encounterList1.size());

        Patient patient2 = patientService.getPatient(7);
        List<Encounter> encounterList2 = encounterService.getEncountersNotAssignedToAnyVisit(patient2);
        assertEquals(0, encounterList2.size());            
    }
    
    @Test
    public void getEncountersNotAssignedToAnyVisit_nonAdminUserWithLocationPropertyShouldGetOnlyAccessibleEncounterInformation(){
        User normalUser = userService.getUser(DEMO_USER2_ID);
        Context.authenticate(normalUser.getUsername(), "userServiceTest");
        assertFalse(normalUser.isSuperUser());

        Location location1 = locationService.getLocation(DEMO_LOCATION1_ID);

        assertEquals(1, normalUser.getUserProperties().size());
        String locationUserProperty = normalUser.getUserProperty("locationUuid");
        assertNotNull(locationUserProperty);
        assertEquals(location1.getUuid(), locationUserProperty);
        
        Patient patient1 = patientService.getPatient(2);
        List<Encounter> encounterList1 = encounterService.getEncountersNotAssignedToAnyVisit(patient1);
        assertEquals(0, encounterList1.size());
        
        Patient patient2 = patientService.getPatient(7);
        List<Encounter> encounterList2 = encounterService.getEncountersNotAssignedToAnyVisit(patient2);
        assertEquals(2, encounterList2.size());
        Encounter encounter1 = encounterList2.get(0);
        Encounter encounter2 = encounterList2.get(1);
        assertNotNull(encounter1);
        assertEquals(location1, encounter1.getLocation());
        assertNotNull(encounter2);
        assertEquals(location1, encounter2.getLocation());
    }
    
    @Test
    public void getEncountersByVisitsAndPatient_adminCanAccessEncounterFromAllLocations() {
        Context.authenticate("admin", "test");
        User authenticatedUser = Context.getAuthenticatedUser();
        assertNotNull(authenticatedUser);
        assertTrue(authenticatedUser.isSuperUser());
        
        Location location1 = locationService.getLocation(DEMO_LOCATION1_ID);
        Location location2 = locationService.getLocation(DEMO_LOCATION2_ID);
        
        Patient patient = patientService.getPatient(2);
        List<Encounter> encounterList = encounterService.getEncountersByVisitsAndPatient(patient,true,null,0,5);
        assertEquals(5, encounterList.size());
        Encounter encounter1 = encounterList.get(0);
        Encounter encounter2 = encounterList.get(1);
        Encounter encounter3 = encounterList.get(2);
        Encounter encounter4 = encounterList.get(3);
        Encounter encounter5 = encounterList.get(4);
        assertNotNull(encounter1);
        assertEquals(location2, encounter1.getLocation());
        assertNotNull(encounter2);
        assertEquals(location2, encounter2.getLocation());
        assertNotNull(encounter3);
        assertEquals(location2, encounter3.getLocation());
        assertNotNull(encounter4);
        assertEquals(location2, encounter4.getLocation());
        assertNotNull(encounter5);
        assertEquals(location1, encounter5.getLocation());
    }
    
    @Test
    public void getEncountersByVisitsAndPatient_nonAdminUserWithOutLocationPropertyShouldNotGetEncounterInformation(){
        User normalUser = userService.getUser(DEMO_USER1_ID);
        Context.authenticate(normalUser.getUsername(), "userServiceTest");
        assertFalse(normalUser.isSuperUser());
        assertNotNull(normalUser);
        
        assertEquals(0, normalUser.getUserProperties().size());
        String locationUserProperty = normalUser.getUserProperty("locationUuid");
        assertNotNull(locationUserProperty);
        
        Patient patient = patientService.getPatient(2);
        List<Encounter> encounterList = encounterService.getEncountersByVisitsAndPatient(patient,true,null,0,5);
        assertEquals(0, encounterList.size());           
    }
    
    @Test
    public void getEncountersByVisitsAndPatient_nonAdminUserWithLocationPropertyShouldGetOnlyAccessibleEncounterInformation(){
        User normalUser = userService.getUser(DEMO_USER2_ID);
        Context.authenticate(normalUser.getUsername(), "userServiceTest");
        assertFalse(normalUser.isSuperUser());

        Location location1 = locationService.getLocation(DEMO_LOCATION1_ID);

        assertEquals(1, normalUser.getUserProperties().size());
        String locationUserProperty = normalUser.getUserProperty("locationUuid");
        assertNotNull(locationUserProperty);
        assertEquals(location1.getUuid(), locationUserProperty );        
        
        Patient patient = patientService.getPatient(2);
        List<Encounter> encounterList = encounterService.getEncountersByVisitsAndPatient(patient,true,null,0,5);
        assertEquals(1, encounterList.size());
        Encounter encounter = encounterList.get(0);
        assertNotNull(encounter);
        assertEquals(location1, encounter.getLocation());
    }
    
    @Test
    public void getAllEncounters_adminCanAccessEncounterFromAllLocations() {
        Context.authenticate("admin", "test");
        User authenticatedUser = Context.getAuthenticatedUser();
        assertNotNull(authenticatedUser);
        assertTrue(authenticatedUser.isSuperUser());
        
        Location location1 = locationService.getLocation(DEMO_LOCATION1_ID);
        Location location2 = locationService.getLocation(DEMO_LOCATION2_ID);
        
        List<Integer> patientIds = Arrays.asList(2,7);
        Cohort cohort = new Cohort(patientIds);
        Map<Integer, List<Encounter>> encounterMap = encounterService.getAllEncounters(cohort);
        assertEquals(5, encounterMap.get(2).size());
        assertEquals(3, encounterMap.get(7).size());
        Encounter encounter1 = encounterMap.get(2).get(0);
        Encounter encounter2 = encounterMap.get(2).get(1);
        Encounter encounter3 = encounterMap.get(2).get(2);
        Encounter encounter4 = encounterMap.get(2).get(3);
        Encounter encounter5 = encounterMap.get(2).get(4);
        Encounter encounter6 = encounterMap.get(7).get(0);
        Encounter encounter7 = encounterMap.get(7).get(1);
        Encounter encounter8 = encounterMap.get(7).get(2);
        assertNotNull(encounter1);
        assertEquals(location2, encounter1.getLocation());
        assertNotNull(encounter2);
        assertEquals(location1, encounter2.getLocation());
        assertNotNull(encounter3);
        assertEquals(location2, encounter3.getLocation());
        assertNotNull(encounter4);
        assertEquals(location2, encounter4.getLocation());
        assertNotNull(encounter5);
        assertEquals(location2, encounter5.getLocation());
        assertNotNull(encounter6);
        assertEquals(location2, encounter6.getLocation());
        assertNotNull(encounter7);
        assertEquals(location1, encounter7.getLocation());
        assertNotNull(encounter8);
        assertEquals(location1, encounter8.getLocation());       
    }
    
    @Test
    public void getAllEncounters_nonAdminUserWithOutLocationPropertyShouldNotGetEncounterInformation(){
        User normalUser = userService.getUser(DEMO_USER1_ID);
        Context.authenticate(normalUser.getUsername(), "userServiceTest");
        assertFalse(normalUser.isSuperUser());
        assertNotNull(normalUser);
        
        assertEquals(0, normalUser.getUserProperties().size());
        String locationUserProperty = normalUser.getUserProperty("locationUuid");
        assertNotNull(locationUserProperty);
        
        List<Integer> patientIds = Arrays.asList(2,7);
        Cohort cohort = new Cohort(patientIds);
        Map<Integer, List<Encounter>> encounterMap = encounterService.getAllEncounters(cohort);
        assertEquals(0, encounterMap.get(2).size());
        assertEquals(0, encounterMap.get(7).size()); 
    }
    
    @Test
    public void getAllEncounters_nonAdminUserWithLocationPropertyShouldGetOnlyAccessibleEncounterInformation(){
        User normalUser = userService.getUser(DEMO_USER2_ID);
        Context.authenticate(normalUser.getUsername(), "userServiceTest");
        assertFalse(normalUser.isSuperUser());

        Location location1 = locationService.getLocation(DEMO_LOCATION1_ID);

        assertEquals(1, normalUser.getUserProperties().size());
        String locationUserProperty = normalUser.getUserProperty("locationUuid");
        assertNotNull(locationUserProperty);
        assertEquals(location1.getUuid(), locationUserProperty);
        
        List<Integer> patientIds = Arrays.asList(2,7);
        Cohort cohort = new Cohort(patientIds);
        Map<Integer, List<Encounter>> encounterMap = encounterService.getAllEncounters(cohort);
        assertEquals(1, encounterMap.get(2).size());
        assertEquals(2, encounterMap.get(7).size());
        Encounter encounter1 = encounterMap.get(2).get(0);
        Encounter encounter2 = encounterMap.get(7).get(0);
        Encounter encounter3 = encounterMap.get(7).get(1);
        assertNotNull(encounter1);
        assertEquals(location1, encounter1.getLocation());
        assertNotNull(encounter2);
        assertEquals(location1, encounter2.getLocation());
        assertNotNull(encounter3);
        assertEquals(location1, encounter3.getLocation()); 
    }
}