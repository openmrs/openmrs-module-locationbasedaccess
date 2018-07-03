package org.openmrs.module.locationbasedaccess.fragment.controller.field;

import org.openmrs.Location;
import org.openmrs.ui.framework.fragment.FragmentModel;
import org.openmrs.api.context.Context;
import javax.servlet.http.HttpSession;
import java.util.List;

public class LocationsFragmentController {

    public void controller(FragmentModel model,
                           HttpSession session) {
        List<Location> activeLocations = Context.getLocationService().getAllLocations();
        model.addAttribute("activeLocations", activeLocations);
    }
}
