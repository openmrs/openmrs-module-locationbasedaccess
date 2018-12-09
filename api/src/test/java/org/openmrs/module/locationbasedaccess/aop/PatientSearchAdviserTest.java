package org.openmrs.module.locationbasedaccess.aop;

import org.junit.Before;
import org.junit.Test;
import org.openmrs.Location;
import org.openmrs.Patient;
import org.openmrs.User;
import org.openmrs.api.LocationService;
import org.openmrs.api.PatientService;
import org.openmrs.api.UserService;
import org.openmrs.api.context.Context;
import org.openmrs.module.locationbasedaccess.aop.common.AOPContextSensitiveTest;
import org.openmrs.module.locationbasedaccess.aop.common.TestWithAOP;
import org.openmrs.module.locationbasedaccess.aop.interceptor.PatientServiceInterceptorAdvice;
import java.util.List;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertEquals;

public class PatientSearchAdviserTest extends AOPContextSensitiveTest {

    private UserService userService;
    private PatientService patientService;
    private LocationService locationService;
    private static final String XML_FILENAME_WITH_PERSON_DATA = "include/PatientTestData.xml";
    private static final String XML_FILENAME_WITH_ADMIN_USER_DATA = "include/AdminUserData.xml";

    /** Configured values in the demo dataset **/
    private static final int DEMO_PERSON_ATTRIBUTE_TYPE_ID = 10;
    private static final int DEMO_LOCATION1_ID = 1;
    private static final int DEMO_LOCATION2_ID = 2;
    private static final int DEMO_USER1_ID = 6001;
    private static final int DEMO_USER2_ID = 6002;

    @Override
    protected void setInterceptorAndServices(TestWithAOP testCase) {
        testCase.setInterceptor(PatientServiceInterceptorAdvice.class);
        testCase.addService(PatientService.class);
    }

    @Before
    public void setUp()  throws Exception {
        userService = Context.getUserService();
        patientService = Context.getPatientService();
        locationService = Context.getLocationService();
        executeDataSet(XML_FILENAME_WITH_ADMIN_USER_DATA);
        executeDataSet(XML_FILENAME_WITH_PERSON_DATA);
    }

    @Test
    public void getPatients_adminShouldGetPatientsFromAllLocations() {
        Context.authenticate("admin", "test");
        User authenticatedUser = Context.getAuthenticatedUser();
        assertNotNull(authenticatedUser);
        assertTrue(authenticatedUser.isSuperUser());

        Location location1 = locationService.getLocation(DEMO_LOCATION1_ID);
        Location location2 = locationService.getLocation(DEMO_LOCATION2_ID);

        List<Patient> patientList = patientService.getPatients("Patient");
        assertEquals(3, patientList.size());
        assertEquals(location1.getUuid(), patientList.get(0).getAttribute(DEMO_PERSON_ATTRIBUTE_TYPE_ID).getValue());
        assertEquals(location2.getUuid(), patientList.get(1).getAttribute(DEMO_PERSON_ATTRIBUTE_TYPE_ID).getValue());
        assertNull(patientList.get(2).getAttribute(DEMO_PERSON_ATTRIBUTE_TYPE_ID));
    }

    @Test
    public void getPatient_adminShouldGetPatientsFromAllLocations() {
        Context.authenticate("admin", "test");
        User authenticatedUser = Context.getAuthenticatedUser();
        assertNotNull(authenticatedUser);
        assertTrue(authenticatedUser.isSuperUser());

        Location location1 = locationService.getLocation(DEMO_LOCATION1_ID);
        Location location2 = locationService.getLocation(DEMO_LOCATION2_ID);

        Patient patient1 = patientService.getPatient(2);
        assertNotNull(patient1);
        assertNull(patient1.getAttribute(DEMO_PERSON_ATTRIBUTE_TYPE_ID));

        Patient patient2 = patientService.getPatient(3);
        assertNotNull(patient2);
        assertEquals(location1.getUuid(), patient2.getAttribute(DEMO_PERSON_ATTRIBUTE_TYPE_ID).getValue());

        Patient patient3 = patientService.getPatient(4);
        assertNotNull(patient3);
        assertEquals(location2.getUuid(), patient3.getAttribute(DEMO_PERSON_ATTRIBUTE_TYPE_ID).getValue());
    }

    @Test
    public void getPatientByUuid_adminShouldGetPatientsFromAllLocations() {
        Context.authenticate("admin", "test");
        User authenticatedUser = Context.getAuthenticatedUser();
        assertNotNull(authenticatedUser);
        assertTrue(authenticatedUser.isSuperUser());

        Location location1 = locationService.getLocation(DEMO_LOCATION1_ID);
        Location location2 = locationService.getLocation(DEMO_LOCATION2_ID);

        Patient patient1 = patientService.getPatientByUuid("5631b434-78aa-102b-91a0-001e378eb67e");
        assertNotNull(patient1);
        assertNull(patient1.getAttribute(DEMO_PERSON_ATTRIBUTE_TYPE_ID));

        Patient patient2 = patientService.getPatientByUuid("5631b434-78aa-102b-91a0-001e378eb68e");
        assertNotNull(patient2);
        assertEquals(location1.getUuid(), patient2.getAttribute(DEMO_PERSON_ATTRIBUTE_TYPE_ID).getValue());

        Patient patient3 = patientService.getPatientByUuid("5631b434-78aa-102b-91a0-001e378eb69e");
        assertNotNull(patient3);
        assertEquals(location2.getUuid(), patient3.getAttribute(DEMO_PERSON_ATTRIBUTE_TYPE_ID).getValue());
    }


    @Test
    public void getPatients_nonAdminUserWithOutLocationPropertyShouldNotGetPatientInformation() {
        User normalUser = userService.getUser(DEMO_USER1_ID);
        Context.authenticate(normalUser.getUsername(), "userServiceTest");
        assertFalse(normalUser.isSuperUser());

        assertEquals(0, normalUser.getUserProperties().size());

        List<Patient> patientList = patientService.getPatients("Patient");
        assertEquals(0, patientList.size());
    }

    @Test
    public void getPatient_nonAdminUserWithOutLocationPropertyShouldNotGetPatientInformation() {
        User normalUser = userService.getUser(DEMO_USER1_ID);
        Context.authenticate(normalUser.getUsername(), "userServiceTest");
        assertFalse(normalUser.isSuperUser());

        assertEquals(0, normalUser.getUserProperties().size());

        Patient patient1 = patientService.getPatient(2);
        assertNull(patient1);
        Patient patient2 = patientService.getPatient(3);
        assertNull(patient2);
        Patient patient3 = patientService.getPatient(4);
        assertNull(patient3);
    }

    @Test
    public void getPatientByUuid_nonAdminUserWithOutLocationPropertyShouldNotGetPatientInformation() {
        User normalUser = userService.getUser(DEMO_USER1_ID);
        Context.authenticate(normalUser.getUsername(), "userServiceTest");
        assertFalse(normalUser.isSuperUser());

        assertEquals(0, normalUser.getUserProperties().size());

        Patient patientA = patientService.getPatientByUuid("5631b434-78aa-102b-91a0-001e378eb67e");
        assertNull(patientA);
        Patient patientB = patientService.getPatientByUuid("5631b434-78aa-102b-91a0-001e378eb68e");
        assertNull(patientB);
        Patient patientC = patientService.getPatientByUuid("5631b434-78aa-102b-91a0-001e378eb69e");
        assertNull(patientC);
    }

    @Test
    public void getPatients_nonAdminUserWithLocationPropertyShouldGetOnlyAccessiblePatientInformation() {
        User normalUser = userService.getUser(DEMO_USER2_ID);
        Context.authenticate(normalUser.getUsername(), "userServiceTest");
        assertFalse(normalUser.isSuperUser());

        Location location1 = locationService.getLocation(DEMO_LOCATION1_ID);

        assertEquals(1, normalUser.getUserProperties().size());
        String locationUserProperty = normalUser.getUserProperty("locationUuid");
        assertNotNull(locationUserProperty);
        assertEquals(location1.getUuid(), locationUserProperty);

        List<Patient> patientList = patientService.getPatients("Patient");
        assertEquals(1, patientList.size());

        Patient patient = patientList.get(0);
        assertNotNull(patient.getAttribute(DEMO_PERSON_ATTRIBUTE_TYPE_ID));
        assertEquals(location1.getUuid(), patient.getAttribute(DEMO_PERSON_ATTRIBUTE_TYPE_ID).getValue());
    }


    @Test
    public void getPatient_nonAdminUserWithLocationPropertyShouldGetOnlyAccessiblePatientInformation() {
        User normalUser = userService.getUser(DEMO_USER2_ID);
        Context.authenticate(normalUser.getUsername(), "userServiceTest");
        assertFalse(normalUser.isSuperUser());

        Location location1 = locationService.getLocation(DEMO_LOCATION1_ID);

        assertEquals(1, normalUser.getUserProperties().size());
        String locationUserProperty = normalUser.getUserProperty("locationUuid");
        assertNotNull(locationUserProperty);
        assertEquals(location1.getUuid(), locationUserProperty);

        Patient patient1 = patientService.getPatient(2);
        assertNull(patient1);

        Patient patient2 = patientService.getPatient(3);
        assertNotNull(patient2);
        assertEquals(location1.getUuid(), patient2.getAttribute(DEMO_PERSON_ATTRIBUTE_TYPE_ID).getValue());

        Patient patient3 = patientService.getPatient(4);
        assertNull(patient3);
    }

    @Test
    public void getPatientByUuid_nonAdminUserWithLocationPropertyShouldGetOnlyAccessiblePatientInformation() {
        User normalUser = userService.getUser(DEMO_USER2_ID);
        Context.authenticate(normalUser.getUsername(), "userServiceTest");
        assertFalse(normalUser.isSuperUser());

        Location location1 = locationService.getLocation(DEMO_LOCATION1_ID);

        assertEquals(1, normalUser.getUserProperties().size());
        String locationUserProperty = normalUser.getUserProperty("locationUuid");
        assertNotNull(locationUserProperty);
        assertEquals(location1.getUuid(), locationUserProperty);

        Patient patient1 = patientService.getPatientByUuid("5631b434-78aa-102b-91a0-001e378eb67e");
        assertNull(patient1);

        Patient patient2 = patientService.getPatientByUuid("5631b434-78aa-102b-91a0-001e378eb68e");
        assertNotNull(patient2);
        assertEquals(location1.getUuid(), patient2.getAttribute(DEMO_PERSON_ATTRIBUTE_TYPE_ID).getValue());

        Patient patient3 = patientService.getPatientByUuid("5631b434-78aa-102b-91a0-001e378eb69e");
        assertNull(patient3);
    }

}
