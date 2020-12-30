/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */

package org.openmrs.module.locationbasedaccess;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.openmrs.Location;
import org.openmrs.Privilege;
import org.openmrs.api.UserService;
import org.openmrs.api.context.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.openmrs.module.BaseModuleActivator;
import org.openmrs.module.ModuleActivator;
import org.openmrs.module.locationbasedaccess.utils.LocationUtils;

public class LocationBasedAccessActivator extends BaseModuleActivator {


    protected Logger log = LoggerFactory.getLogger(getClass());

    /**
     * @see ModuleActivator#willRefreshContext()
     */
    public void willRefreshContext() {
        log.info("Refreshing Location Based Access Control Module");
    }

    /**
     * @see ModuleActivator#contextRefreshed()
     */
    public void contextRefreshed() {
        log.info("Location Based Access Control Module refreshed");
    }

    /**
     * @see ModuleActivator#willStart()
     */
    public void willStart() {
        log.info("Starting Location Based Access Control Module");
    }

    /**
     * @see ModuleActivator#started()
     */
    public void started() {
		LocationUtils.createPersonAttributeTypeForLocation();
	    generatePrivilegesForLocations();
	    createGlobalPropertyForLocationAttribute(LocationBasedAccessConstants.LOCATION_ATTRIBUTE_GLOBAL_PROPERTY_NAME,
			    LocationBasedAccessConstants.PERSONATTRIBUTETYPE_UUID);
	    createGlobalPropertyForLocationAttribute(LocationBasedAccessConstants.REF_APP_LOCATION_USER_PROPERTY_NAME,
			    LocationBasedAccessConstants.LOCATION_USER_PROPERTY_NAME);
	    createGlobalPropertyForEntityRestrictions();
	    log.info("Location Based Access Control Module started");
    }

	/**
	 * Used to create global property for every entity whether to have Location Based
	 * restrictions on it or not.global property with value true means to have the restriction
	 */
	private void createGlobalPropertyForEntityRestrictions() {
    	createGlobalPropertyForLocationAttribute(LocationBasedAccessConstants.ENCOUNTER_RESTRICTION_GLOBAL_PROPERTY_NAME,
			    LocationBasedAccessConstants.LOCATION_BASED_RESTRICTION_VALUE_TRUE);
		createGlobalPropertyForLocationAttribute(LocationBasedAccessConstants.PERSON_RESTRICTION_GLOBAL_PROPERTY_NAME,
				LocationBasedAccessConstants.LOCATION_BASED_RESTRICTION_VALUE_TRUE);
		createGlobalPropertyForLocationAttribute(LocationBasedAccessConstants.PATIENT_RESTRICTION_GLOBAL_PROPERTY_NAME,
				LocationBasedAccessConstants.LOCATION_BASED_RESTRICTION_VALUE_TRUE);
		createGlobalPropertyForLocationAttribute(LocationBasedAccessConstants.LOCATION_RESTRICTION_GLOBAL_PROPERTY_NAME,
				LocationBasedAccessConstants.LOCATION_BASED_RESTRICTION_VALUE_TRUE);
		createGlobalPropertyForLocationAttribute(LocationBasedAccessConstants.USER_RESTRICTION_GLOBAL_PROPERTY_NAME,
				LocationBasedAccessConstants.LOCATION_BASED_RESTRICTION_VALUE_TRUE);
	}

	private void generatePrivilegesForLocations() {
		UserService userService = Context.getUserService();
		List<Location> openMrsLocations = Context.getLocationService().getAllLocations(false);
		for (Location location : openMrsLocations) {
			Privilege privilege = new Privilege("LocationAccess " + location.getName(),
					"Able to view Entity with Location " + location.getName());
			if (userService.getPrivilege(privilege.getPrivilege()) == null) {
				userService.savePrivilege(privilege);
			}
		}
	}

	private void createGlobalPropertyForLocationAttribute(String key, String value) {
		String locationUserPropertyName = Context.getAdministrationService().getGlobalProperty(key);
		if (StringUtils.isBlank(locationUserPropertyName)) {
			Context.getAdministrationService().setGlobalProperty(key, value);
			if (log.isDebugEnabled()) {
				log.debug("RefApp Location global property created with value - "
						+ value);
			}
		} else if (StringUtils.isNotBlank(locationUserPropertyName) && !locationUserPropertyName.equals(value)) {
			log.warn("RefApp Location global property already exist in the system with different value("
					+ locationUserPropertyName +
					"). Exiting from creating new global property with the value " + value);
		}
	}

	/**
     * @see ModuleActivator#willStop()
     */
    public void willStop() {
        log.info("Stopping Location Based Access Control Module");
    }

    /**
     * @see ModuleActivator#stopped()
     */
    public void stopped() {
        log.info("Location Based Access Control Module stopped");
    }
}
