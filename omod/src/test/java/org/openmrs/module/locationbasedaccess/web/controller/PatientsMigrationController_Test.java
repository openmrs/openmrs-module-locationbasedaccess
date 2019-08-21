package org.openmrs.module.locationbasedaccess.web.controller;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.openmrs.Patient;
import org.openmrs.PersonAttribute;
import org.openmrs.api.PatientService;
import org.openmrs.api.context.Context;
import org.openmrs.module.webservices.rest.SimpleObject;
import org.openmrs.module.webservices.rest.web.v1_0.controller.RestControllerTestUtils;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.bind.annotation.RequestMethod;

public class PatientsMigrationController_Test extends RestControllerTestUtils {

	private final String XML_FILENAME_WITH_PERSON_DATA = "PatientMigrationData.xml";

	private final String LOCATION_UUID = "ef93c695-ac43-450a-93f8-4b2b4d50a3c9";

	private final String PERSON_ATTRIBUTE_NAME = "LocationAttribute";

	private final int PATIENT1_ID = 1;

	private final int PATIENT2_ID = 2;

	private final int PATIENT3_ID = 3;

	private PatientService patientService;

	@Before
	public void init() throws Exception {
		patientService = Context.getPatientService();
		executeDataSet(XML_FILENAME_WITH_PERSON_DATA);
	}

	private String getURI() {
		return "/lbac/patient-migration";
	}

	@Test
	public void migratePatients_shouldChangePersonLocationAttributeType() throws Exception {

		Patient patient1 = patientService.getPatient(PATIENT1_ID);
		PersonAttribute personAttribute1 = patient1.getAttribute(PERSON_ATTRIBUTE_NAME);
		Assert.assertEquals(personAttribute1.getValue(), LOCATION_UUID);

		Patient patient3 = patientService.getPatient(PATIENT3_ID);
		PersonAttribute personAttribute3 = patient3.getAttribute(PERSON_ATTRIBUTE_NAME);
		Assert.assertNotEquals(personAttribute3.getValue(), LOCATION_UUID);

		Patient patient2 = patientService.getPatient(PATIENT2_ID);
		PersonAttribute personAttribute2 = patient2.getAttribute(PERSON_ATTRIBUTE_NAME);
		Assert.assertNull(personAttribute2);

		String patientList[] = { patient1.getUuid(), patient2.getUuid(), patient3.getUuid() };
		SimpleObject obj = new SimpleObject();
		obj.add("locationUuid", LOCATION_UUID);
		obj.add("patientList", patientList);
		String json = new ObjectMapper().writeValueAsString(obj);
		MockHttpServletRequest req = request(RequestMethod.POST, getURI());
		req.setContent(json.getBytes());
		Assert.assertEquals(handle(req).getStatus(), HttpStatus.OK.value());
		Patient newpatient1 = patientService.getPatient(PATIENT1_ID);
		Patient newpatient2 = patientService.getPatient(PATIENT2_ID);
		Patient newpatient3 = patientService.getPatient(PATIENT3_ID);
		Assert.assertEquals(newpatient1.getAttribute(PERSON_ATTRIBUTE_NAME).getValue(), LOCATION_UUID);

		Assert.assertEquals(newpatient3.getAttribute(PERSON_ATTRIBUTE_NAME).getValue(), LOCATION_UUID);

		Assert.assertEquals(newpatient2.getAttribute(PERSON_ATTRIBUTE_NAME).getValue(), LOCATION_UUID);

	}

}
