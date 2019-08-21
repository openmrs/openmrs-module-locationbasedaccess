package org.openmrs.module.locationbasedaccess.web.controller;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.openmrs.GlobalProperty;
import org.openmrs.Patient;
import org.openmrs.PersonAttribute;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.PatientService;
import org.openmrs.api.context.Context;
import org.openmrs.module.webservices.rest.SimpleObject;
import org.openmrs.module.webservices.rest.web.v1_0.controller.RestControllerTestUtils;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.bind.annotation.RequestMethod;

public class LBACOnOffController_Test extends RestControllerTestUtils {

	private final String XML_FILENAME_WITH_GP = "GlobalPropertyData.xml";

	private final String gp1 = "locationbasedaccess.access.patient";

	private final String gp3 = "locationbasedaccess.access.location";

	private final String gp2 = "locationbasedaccess.access.person";

	private AdministrationService administrationService;

	@Before
	public void init() throws Exception {
		administrationService = Context.getAdministrationService();
		executeDataSet(XML_FILENAME_WITH_GP);
	}

	private String getURI() {
		return "lbac/on-off";
	}

	@Test
	public void changeEntityGlobalProperty_shouldChangeGlobalPropertyValue() throws Exception {
		Assert.assertEquals(administrationService.getGlobalProperty(gp1), "true");
		Assert.assertEquals(administrationService.getGlobalProperty(gp2), "false");
		Assert.assertEquals(administrationService.getGlobalProperty(gp3), "false");
		SimpleObject obj = new SimpleObject();
		obj.add(gp1, "true");
		obj.add(gp2, "true");
		String json = new ObjectMapper().writeValueAsString(obj);
		MockHttpServletRequest req = request(RequestMethod.POST, getURI());
		req.setContent(json.getBytes());
		Assert.assertEquals(handle(req).getStatus(), HttpStatus.OK.value());

		String gp1_value = administrationService.getGlobalProperty(gp1);
		String gp2_value = administrationService.getGlobalProperty(gp2);
		String gp3_value = administrationService.getGlobalProperty(gp3);
		Assert.assertEquals(gp1_value, "true");

		Assert.assertEquals(gp2_value, "true");

		Assert.assertEquals(gp3_value, "false");
	}

}
