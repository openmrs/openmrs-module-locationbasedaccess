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

import org.aopalliance.aop.Advice;
import org.springframework.aop.Advisor;
import java.lang.reflect.Method;

/**
 * It will used to create the abstract methods for using AOP Services inside the classes
 */
public interface AOPService extends Advisor {

    boolean matches(Method method, Class targetClass);

    @Override
    Advice getAdvice();

}
