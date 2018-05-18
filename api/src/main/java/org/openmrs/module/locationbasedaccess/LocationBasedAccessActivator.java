/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */
package org.openmrs.module.locationbasedaccess;


import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;

import org.openmrs.PersonAttributeType;
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

        if (Context.getPersonService().getPersonAttributeTypeByName("accessLocation") == null) {
            PersonAttributeType personAttributeType = new PersonAttributeType();
            personAttributeType.setName("accessLocation");
            personAttributeType.setFormat("java.lang.String");
            Context.getPersonService().savePersonAttributeType(personAttributeType);
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
