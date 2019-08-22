package org.openmrs.module.locationbasedaccess.web.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.openmrs.module.Module;
import org.openmrs.module.ModuleFactory;
import org.openmrs.module.webservices.rest.SimpleObject;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.v1_0.controller.BaseRestController;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

@Controller
@RequestMapping(value = "/rest/" + RestConstants.VERSION_1 + "/lbac/module-dependency")
public class ModuleDependencyController extends BaseRestController {

	/**
	 * @return modules required for LBAC module installation
	 */
	@RequestMapping(method = RequestMethod.GET)
	@ResponseBody
	@ResponseStatus(value = HttpStatus.OK)
	public Map<String, Map<String,String>> getRequiredModules() {
		Module lbacModule = ModuleFactory.getModuleById("locationbasedaccess");
		Map<String, Map<String, String>> obj = new HashMap<String, Map<String, String>>();
		List<String> modules = lbacModule.getRequiredModules();
		for (String modulePackage : modules) {
			Module module = ModuleFactory.getModuleByPackage(modulePackage);
			Map<String,String> innerObject = new HashMap<String,String>();
			innerObject.put("requiredVerison",lbacModule.getRequiredModuleVersion(modulePackage));
			if (module.isStarted()) {
				innerObject.put("status" , "Installed");
			} else {
				innerObject.put("status" , "NotInstalled");
			}
			obj.put(module.getName(),innerObject);
		}
		return obj;
	}
}
