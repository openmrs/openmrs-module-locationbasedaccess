package org.openmrs.module.locationbasedaccess.fragment.controller.manageAccount;

import org.apache.commons.lang3.StringUtils;
import org.openmrs.Location;
import org.openmrs.Person;
import org.openmrs.User;
import org.openmrs.api.context.Context;
import org.openmrs.module.appui.UiSessionContext;
import org.openmrs.module.locationbasedaccess.LocationBasedAccessConstants;
import org.openmrs.module.locationbasedaccess.utils.LocationUtils;
import org.openmrs.ui.framework.fragment.FragmentModel;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpSession;
import java.util.List;

public class AddLocationFragmentController {

    public void controller(FragmentModel model,
                           HttpSession session,
                           @RequestParam(value = "userId", required = false) User user,
                           @RequestParam(value = "personId", required = false) Person person) {
        List<Location> activeLocations = Context.getLocationService().getAllLocations();
        model.addAttribute("activeLocations", activeLocations);

        model.addAttribute("selectedLocationUuid", null);
        Object sessionLocationId = session.getAttribute(UiSessionContext.LOCATION_SESSION_ATTRIBUTE);
        if(sessionLocationId != null && StringUtils.isNotBlank(sessionLocationId.toString())) {
            Location sessionLocation = Context.getLocationService().getLocation(Integer.parseInt(sessionLocationId.toString()));
            model.addAttribute("selectedLocationUuid", sessionLocation.getUuid());
        }

        if (user != null) {
            Object userProperty = user.getUserProperty(LocationBasedAccessConstants.LOCATION_USER_PROPERTY_NAME);
            if (userProperty != null) {
                model.addAttribute("selectedLocationUuid", userProperty.toString());
            }
        }
        else if (person != null) {
            Location location = LocationUtils.getPersonLocation(person);
            if(location != null) {
                model.addAttribute("selectedLocationUuid", location.getUuid());
            }
        }
    }
}
