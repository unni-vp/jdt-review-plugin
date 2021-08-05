package org.unnivp.jdtstaticanalysis.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.handlers.HandlerUtil;
import org.unnivp.jdtstaticanalysis.job.JDTReviewJob;

/**
 * Handler Class that returns the count of total classes and methods in the
 * current workspace.
 * 
 * @author unni-vp
 *
 */
public class JDTReviewHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {

		new JDTReviewJob("JDT Review in progress").schedule();
		
		// Notify the user of review start on a new workbench window.
		IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindowChecked(event);
		MessageDialog.openInformation(window.getShell(), "JDT Review", "Review in Progress. Results will be displayed in the 'JDT Review Results' view.");
		
		// Open the JDT Review Results' view.
		try {
			window.getActivePage().showView("JDTReviewMarkerView");
		} catch (PartInitException e) {
		}
		
		return null;
	}

}
