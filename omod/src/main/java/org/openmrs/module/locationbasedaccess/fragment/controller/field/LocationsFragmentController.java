package org.openmrs.module.locationbasedaccess.fragment.controller.field;

import org.apache.commons.lang3.StringUtils;
import org.openmrs.Location;
import org.openmrs.Patient;
import org.openmrs.PersonAttribute;
import org.openmrs.PersonAttributeType;
import org.openmrs.module.appui.UiSessionContext;
import org.openmrs.module.locationbasedaccess.LocationBasedAccessConstants;
import org.openmrs.ui.framework.fragment.FragmentModel;
import org.openmrs.api.context.Context;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpSession;
import java.util.List;

public class LocationsFragmentController {

    public void controller(FragmentModel model,
                           HttpSession session,
                           @RequestParam(value = "patientId", required = false) Patient patient) {
        List<Location> activeLocations = Context.getLocationService().getAllLocations();
        model.addAttribute("activeLocations", activeLocations);

        model.addAttribute("selectedLocationUuid", null);
        Object sessionLocationId = session.getAttribute(UiSessionContext.LOCATION_SESSION_ATTRIBUTE);
        if(sessionLocationId!=null && StringUtils.isNotBlank(sessionLocationId.toString())) {
            Location sessionLocation = Context.getLocationService().getLocation(Integer.parseInt(sessionLocationId.toString()));
            model.addAttribute("selectedLocationUuid", sessionLocation.getUuid());
        }
        if (patient != null) {
            String locationAttributeUuid = Context.getAdministrationService().getGlobalProperty(LocationBasedAccessConstants.LOCATION_ATTRIBUTE_GLOBAL_PROPERTY_NAME);
            if (StringUtils.isNotBlank(locationAttributeUuid)) {
                final PersonAttributeType personAttributeType = Context.getPersonService().getPersonAttributeTypeByUuid(locationAttributeUuid);
                PersonAttribute personAttribute = patient.getAttribute(personAttributeType);
                if (personAttribute != null) {
                    Location patientLocation = Context.getLocationService().getLocationByUuid(personAttribute.getValue());
                    model.addAttribute("selectedLocationUuid", patientLocation.getUuid());
                }
            }
        }
    }
}
