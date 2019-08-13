package org.openmrs.module.locationbasedaccess.fragment.controller.manageAccount;

import org.apache.commons.lang3.StringUtils;
import org.openmrs.Location;
import org.openmrs.Person;
import org.openmrs.User;
import org.openmrs.api.context.Context;
import org.openmrs.module.appframework.service.AppFrameworkService;
import org.openmrs.module.appui.UiSessionContext;
import org.openmrs.module.locationbasedaccess.LocationBasedAccessConstants;
import org.openmrs.module.locationbasedaccess.utils.LocationUtils;
import org.openmrs.ui.framework.annotation.SpringBean;
import org.openmrs.ui.framework.fragment.FragmentConfiguration;
import org.openmrs.ui.framework.fragment.FragmentModel;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AddLocationFragmentController {

    public void controller(FragmentModel model,
                           HttpSession session,
                           @SpringBean("appFrameworkService") AppFrameworkService appFrameworkService,
                           FragmentConfiguration config,
                           @RequestParam(value = "userId", required = false) User user,
                           @RequestParam(value = "personId", required = false) Person person) {
        List<Location> activeLocations = appFrameworkService.getLoginLocations();
        model.addAttribute("activeLocations", activeLocations);

        model.addAttribute("selectedLocationUuids", null);
        model.addAttribute("userLocationProperty", null);
        model.addAttribute("disabledLocations", null);
        Object sessionLocationId = session.getAttribute(UiSessionContext.LOCATION_SESSION_ATTRIBUTE);
        if(sessionLocationId != null && StringUtils.isNotBlank(sessionLocationId.toString())) {
            Location sessionLocation = Context.getLocationService().getLocation(Integer.parseInt(sessionLocationId.toString()));
            List<String> locationUuids = new ArrayList<String>();
            locationUuids.add(sessionLocation.getUuid());
            model.addAttribute("selectedLocationUuids", locationUuids);
            model.addAttribute("userLocationProperty", sessionLocation.getUuid());
        }
        if (user != null) {
            Object userProperty = user.getUserProperty(LocationBasedAccessConstants.LOCATION_USER_PROPERTY_NAME);
            if (userProperty != null) {
                List<String> locationUuids = Arrays.asList(userProperty.toString().split(","));
                model.addAttribute("selectedLocationUuids", locationUuids);
                seperateEditableLocations(model, locationUuids);
            }
        }
        else if (person != null) {
            Location location = LocationUtils.getPersonLocation(person);
            if(location != null) {
                List<String> locationUuids = new ArrayList<String>();
                locationUuids.add(location.getUuid());
                //this implementation have to be changed when multiple locations added to person
                model.addAttribute("selectedLocationUuids", locationUuids);
            }
        }

        Object userId = config.getAttribute("userUuid");
        if (userId != null) {
            User thisUser = Context.getUserService().getUserByUuid(userId.toString());
            if (thisUser != null) {
                String locationProperty = thisUser.getUserProperty(LocationBasedAccessConstants.LOCATION_USER_PROPERTY_NAME);
                if (StringUtils.isNotBlank(locationProperty)) {
                    List<String> locationUuids = Arrays.asList(locationProperty.split(","));
                    model.addAttribute("selectedLocationUuids", locationUuids);
                    seperateEditableLocations(model, locationUuids);
                }
            }
        }
    }

    public void seperateEditableLocations(FragmentModel model, List<String> locationUuids) {
        StringBuffer disabledLocations = new StringBuffer();
        StringBuffer editableLocations = new StringBuffer();
        for (String locationUuid : locationUuids) {
            if (Context.getLocationService().getLocationByUuid(locationUuid) == null) {
                disabledLocations.append(locationUuid + ",");
            } else {
                editableLocations.append(locationUuid + ",");
            }
        }
        model.addAttribute("disabledLocations", disabledLocations.toString());
        model.addAttribute("userLocationProperty", editableLocations.toString());
    }

}
