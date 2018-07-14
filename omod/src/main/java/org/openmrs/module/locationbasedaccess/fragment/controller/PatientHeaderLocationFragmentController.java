package org.openmrs.module.locationbasedaccess.fragment.controller;

import org.apache.commons.lang3.StringUtils;
import org.openmrs.Location;
import org.openmrs.Patient;
import org.openmrs.PersonAttribute;
import org.openmrs.PersonAttributeType;
import org.openmrs.module.locationbasedaccess.LocationBasedAccessConstants;
import org.openmrs.ui.framework.fragment.FragmentModel;
import org.openmrs.api.context.Context;
import org.springframework.web.bind.annotation.RequestParam;

public class PatientHeaderLocationFragmentController {

    public void controller(FragmentModel model,
                           @RequestParam(value = "patientId", required = false) Patient patient) {
        model.addAttribute("patientLocation", null);
        if (patient != null && Context.getAuthenticatedUser().isSuperUser()) {
            String locationAttributeUuid = Context.getAdministrationService().getGlobalProperty(LocationBasedAccessConstants.LOCATION_ATTRIBUTE_GLOBAL_PROPERTY_NAME);
            if (StringUtils.isNotBlank(locationAttributeUuid)) {
                final PersonAttributeType personAttributeType = Context.getPersonService().getPersonAttributeTypeByUuid(locationAttributeUuid);
                PersonAttribute personAttribute = patient.getAttribute(personAttributeType);
                if (personAttribute != null) {
                    Location patientLocation = Context.getLocationService().getLocationByUuid(personAttribute.getValue());
                    model.addAttribute("patientLocation", patientLocation.getName());
                }
            }
        }
    }
}
