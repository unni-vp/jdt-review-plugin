package org.unnivp.jdtstaticanalysis.markers;

import org.eclipse.ui.views.markers.MarkerField;
import org.eclipse.ui.views.markers.MarkerItem;

/**
 * Custom marker view field for displaying project name.
 * 
 * @author unni-vp
 * 
 */
public class ProjectField extends MarkerField {

	public String getValue(MarkerItem item) {
		return item.getMarker().getResource().getProject().getName();
	}
}
