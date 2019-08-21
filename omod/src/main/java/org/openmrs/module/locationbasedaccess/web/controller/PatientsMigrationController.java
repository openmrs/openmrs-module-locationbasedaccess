package org.openmrs.module.locationbasedaccess.web.controller;

import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.openmrs.Location;
import org.openmrs.Patient;
import org.openmrs.Person;
import org.openmrs.PersonAttribute;
import org.openmrs.PersonAttributeType;
import org.openmrs.api.APIException;
import org.openmrs.api.PatientService;
import org.openmrs.api.PersonService;
import org.openmrs.api.context.Context;
import org.openmrs.module.locationbasedaccess.LocationBasedAccessConstants;
import org.openmrs.module.webservices.rest.SimpleObject;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.RestUtil;
import org.openmrs.module.webservices.rest.web.v1_0.controller.BaseRestController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

@Controller
@RequestMapping(value = "/rest/" + RestConstants.VERSION_1 + "/lbac/patient-migration")
public class PatientsMigrationController extends BaseRestController {

	/**
	 * @param body json with list of patientsUUIDs and locationUuid to migrate to.
	 * post request will migrate the patients to the location
	 */
	@RequestMapping(method = RequestMethod.POST)
	@ResponseBody
	public SimpleObject migratePatientLocation(@RequestBody Map<String, Object> body) throws APIException {
		PatientService patientService = Context.getPatientService();
		List<String> patientList = (List<String>) body.get("patientList");
		String locationUuid = (String) body.get("locationUuid");
		Location location = Context.getLocationService().getLocationByUuid(locationUuid);
		LinkedHashMap map = new LinkedHashMap();
		if (location == null) {
			map.put("message", "Location Uuid " + locationUuid + " is Not valid");
		} else {
			String locationAttributeUuid = Context.getAdministrationService().getGlobalProperty(
					LocationBasedAccessConstants.LOCATION_ATTRIBUTE_GLOBAL_PROPERTY_NAME);
			if (StringUtils.isNotBlank(locationAttributeUuid)) {
				final PersonAttributeType personAttributeType = Context.getPersonService().getPersonAttributeTypeByUuid(
						locationAttributeUuid);
				PersonAttribute personAttribute = new PersonAttribute(personAttributeType, locationUuid);
				for (Iterator<String> iterator = patientList.iterator(); iterator.hasNext(); ) {
					String patientUuid = iterator.next();
					Patient patient = patientService.getPatientByUuid(patientUuid);
					patient.addAttribute(personAttribute);
					patientService.savePatient(patient);
				}
				map.put("message", HttpStatus.OK);
			} else {
				map.put("message", "Global Property " + LocationBasedAccessConstants.LOCATION_ATTRIBUTE_GLOBAL_PROPERTY_NAME
						+ "not Found");
			}
		}
		return (new SimpleObject()).add("status", map);
	}

	@ExceptionHandler(NullPointerException.class)
	@ResponseBody
	public SimpleObject handleNotFound(NullPointerException exception, HttpServletRequest request,
			HttpServletResponse response) {
		response.setStatus(HttpServletResponse.SC_NOT_FOUND);
		return RestUtil.wrapErrorResponse(exception, "Patient not found");
	}

	@ExceptionHandler(ClassCastException.class)
	@ResponseBody
	public SimpleObject handleNotFound(ClassCastException exception, HttpServletRequest request,
			HttpServletResponse response) {
		response.setStatus(HttpServletResponse.SC_NOT_FOUND);
		return RestUtil.wrapErrorResponse(exception, "Patient List has to be List");
	}

}
