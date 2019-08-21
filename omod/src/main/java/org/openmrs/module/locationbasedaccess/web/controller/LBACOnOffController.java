package org.openmrs.module.locationbasedaccess.web.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.openmrs.api.context.Context;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.v1_0.controller.BaseRestController;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

@Controller
@RequestMapping(value = "/rest/" + RestConstants.VERSION_1 + "/lbac/on-off")
public class LBACOnOffController extends BaseRestController {

	@RequestMapping(method = RequestMethod.POST)
	@ResponseBody
	@ResponseStatus(value = HttpStatus.OK)
	public void changeEntityGlobalProperty(@RequestBody Map<String, String> body) {
		List<String> entities = getLocationEntites();
		for (String entity : entities) {
			String entityGlobalPropertyValue = body.get(entity);
			if (!StringUtils.isBlank(entityGlobalPropertyValue)) {
				Context.getAdministrationService().setGlobalProperty(entity, entityGlobalPropertyValue);
			}
		}
	}

	private List<String> getLocationEntites() {
		List<String> entities = new ArrayList<String>();
		entities.add("locationbasedaccess.access.patient");
		entities.add("locationbasedaccess.access.location");
		entities.add("locationbasedaccess.access.person");
		entities.add("locationbasedaccess.access.user");
		entities.add("locationbasedaccess.access.encounter");
		return entities;
	}

}
