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

import org.apache.commons.lang.StringUtils;
import org.openmrs.api.context.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.openmrs.module.BaseModuleActivator;
import org.openmrs.module.ModuleActivator;

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
        String locationUserPropertyName = Context.getAdministrationService().getGlobalProperty(LocationBasedAccessConstants.REF_APP_LOCATION_USER_PROPERTY_NAME);
        if(StringUtils.isBlank(locationUserPropertyName)) {
            Context.getAdministrationService().setGlobalProperty(LocationBasedAccessConstants.REF_APP_LOCATION_USER_PROPERTY_NAME,
                    LocationBasedAccessConstants.LOCATION_USER_PROPERTY_NAME);
            if(log.isDebugEnabled()) {
                log.debug("RefApp Location global property created with value - " + LocationBasedAccessConstants.LOCATION_USER_PROPERTY_NAME);
            }
        }
        else if(StringUtils.isNotBlank(locationUserPropertyName) && !locationUserPropertyName.equals(LocationBasedAccessConstants.LOCATION_USER_PROPERTY_NAME)) {
            log.warn("RefApp Location global property already exist in the system with different value(" + locationUserPropertyName +
                    "). Exiting from creating new global property with the value " + LocationBasedAccessConstants.LOCATION_USER_PROPERTY_NAME);
        }
        log.info("Location Based Access Control Module started");
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
