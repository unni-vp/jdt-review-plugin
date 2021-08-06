package org.unnivp.jdtstaticanalysis.markers;

import static org.unnivp.jdtstaticanalysis.constants.ReviewConstants.MARKER_VIEW_GENERATOR;

import org.eclipse.ui.views.markers.MarkerSupportView;

/**
 * Marker view for showing JDT review results.
 * 
 * @author unnivp
 *
 */
public class JDTReviewMarkerView extends MarkerSupportView {

	public JDTReviewMarkerView() {

		super(MARKER_VIEW_GENERATOR);
	}
}