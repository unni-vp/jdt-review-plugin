package org.unnivp.jdtstaticanalysis.markers;

import org.eclipse.ui.views.markers.MarkerSupportView;

public class JDTReviewMarker extends MarkerSupportView {
	
    public JDTReviewMarker() {
    	
        super("org.unnivp.jdtstaticanalysis.markers.JDTReviewMarkerGenerator");
    }
}