package org.openmrs.module.locationbasedaccess.fragment.controller.manageAccount;


import org.apache.commons.lang3.StringUtils;
import org.openmrs.Location;
import org.openmrs.User;
import org.openmrs.api.context.Context;
import org.openmrs.module.locationbasedaccess.LocationBasedAccessConstants;
import org.openmrs.ui.framework.fragment.FragmentConfiguration;
import org.openmrs.ui.framework.fragment.FragmentModel;

public class ViewUserLocationFragmentController {

    public void controller(FragmentModel model, FragmentConfiguration config) {
        Object userId = config.getAttribute("userUuid");
        model.addAttribute("selectedUserLocationNames", null);
        if (userId != null) {
            User user = Context.getUserService().getUserByUuid(userId.toString());
            if (user != null) {
                String locationProperty = user.getUserProperty(LocationBasedAccessConstants.LOCATION_USER_PROPERTY_NAME);
                if (StringUtils.isNotBlank(locationProperty)) {
                    String[] locationUuids = locationProperty.split(",");
                    StringBuffer userLocationsList = new StringBuffer();
                    for(String locationUuid : locationUuids){
                        Location userLocation = Context.getLocationService().getLocationByUuid(locationUuid);
                        if (userLocation != null) {
                            userLocationsList.append(userLocation.getName());
                            if(locationUuid != locationUuids[locationUuids.length-1]){
                                userLocationsList.append(", ");
                            }
                        }
                    }
                    model.addAttribute("selectedUserLocationNames", userLocationsList.toString());
                }
            }
        }
    }
}
