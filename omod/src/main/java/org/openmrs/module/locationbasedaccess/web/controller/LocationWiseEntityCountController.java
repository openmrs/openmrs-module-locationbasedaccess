package org.openmrs.module.locationbasedaccess.web.controller;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.openmrs.Cohort;
import org.openmrs.Encounter;
import org.openmrs.Location;
import org.openmrs.Patient;
import org.openmrs.PersonAttribute;
import org.openmrs.PersonAttributeType;
import org.openmrs.User;
import org.openmrs.api.context.Context;
import org.openmrs.module.locationbasedaccess.LocationBasedAccessConstants;
import org.openmrs.module.locationbasedaccess.utils.LocationUtils;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.v1_0.controller.BaseRestController;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

@Controller
@RequestMapping(value = "/rest/" + RestConstants.VERSION_1 + "/lbac")
public class LocationWiseEntityCountController extends BaseRestController {

	@RequestMapping(value = "/locationwise-patients-count", method = RequestMethod.GET)
	@ResponseBody
	@ResponseStatus(value = HttpStatus.OK)
	public Map<String, Object> getPatientLocationWiseCount() {

		List<Patient> patientList = Context.getPatientService().getAllPatients();
		Map<String, Integer> locationPatientMap = new HashMap<String, Integer>();
		Map<String, String> locationNamesMap = getLocationUuidNameMap(locationPatientMap);
		String locationAttributeUuid = Context.getAdministrationService().getGlobalProperty(
				LocationBasedAccessConstants.LOCATION_ATTRIBUTE_GLOBAL_PROPERTY_NAME);
		final PersonAttributeType personAttributeType = Context.getPersonService().getPersonAttributeTypeByUuid(
				locationAttributeUuid);
		for (Patient patient : patientList) {
			PersonAttribute personAttribute = patient.getPerson().getAttribute(personAttributeType);
			if (personAttribute != null && locationNamesMap.get(personAttribute.getValue()) != null) {
				Integer patientsCount = locationPatientMap.get(locationNamesMap.get(personAttribute.getValue()));
				patientsCount++;
				locationPatientMap.put(locationNamesMap.get(personAttribute.getValue()), patientsCount);
			}
		}
		Map<String, Object> obj = new HashMap<String, Object>();
		obj.put("results", locationPatientMap);
		return obj;
	}

	@RequestMapping(value = "/locationwise-users-count", method = RequestMethod.GET)
	@ResponseBody
	@ResponseStatus(value = HttpStatus.OK)
	public Map<String, Object> getUsersLocationWiseCount() {

		Map<String, Integer> locationUsersMap = new HashMap<String, Integer>();
		Map<String, String> locationNamesMap = getLocationUuidNameMap(locationUsersMap);
		List<User> usersList = Context.getUserService().getAllUsers();
		for (User user : usersList) {
			List<String> userLocations = LocationUtils.getUserAccessibleLocationUuids(user);
			for (String userLocation : userLocations) {
				if (StringUtils.isNotBlank(userLocation) && locationNamesMap.get(userLocation) != null) {
					Integer userCount = locationUsersMap.get(locationNamesMap.get(userLocation));
					userCount++;
					locationUsersMap.put(locationNamesMap.get(userLocation), userCount);
				}
			}
		}
		Map<String, Object> obj = new HashMap<String, Object>();
		obj.put("results", locationUsersMap);
		return obj;
	}

	@RequestMapping(value = "/locationwise-encounters-count", method = RequestMethod.GET)
	@ResponseBody
	@ResponseStatus(value = HttpStatus.OK)
	public Map<String, Object> getEncountersLocationWiseCount() {
		List<Patient> patientList = Context.getPatientService().getAllPatients();
		Map<String, Integer> locationEncounterMap = new HashMap<String, Integer>();
		Map<String, String> locationNamesMap = getLocationUuidNameMap(locationEncounterMap);
		Map<Integer, List<Encounter>> encounterMap = Context.getEncounterService().getAllEncounters(new Cohort(patientList));
		Iterator<Map.Entry<Integer, List<Encounter>>> mapIterator = encounterMap.entrySet().iterator();
		while (mapIterator.hasNext()) {
			Map.Entry<Integer, List<Encounter>> entry = mapIterator.next();
			List<Encounter> encounterList = entry.getValue();
			for (Encounter encounter : encounterList) {
				if (encounter.getLocation() != null) {
					String locationUuid = encounter.getLocation().getUuid();
					Integer userCount = locationEncounterMap.get(locationNamesMap.get(locationUuid));
					userCount++;
					locationEncounterMap.put(locationNamesMap.get(locationUuid), userCount);
				}
			}
		}
		Map<String, Object> obj = new HashMap<String, Object>();
		obj.put("results", locationEncounterMap);
		return obj;
	}

	private Map<String, String> getLocationUuidNameMap(Map<String, Integer> locationEntityMap) {
		List<Location> locationList = Context.getLocationService().getAllLocations();
		Map<String, String> locationNamesMap = new HashMap<String, String>();
		for (Location location : locationList) {
			locationNamesMap.put(location.getUuid(), location.getName());
			locationEntityMap.put(location.getName(), 0);
		}
		return locationNamesMap;
	}
}
