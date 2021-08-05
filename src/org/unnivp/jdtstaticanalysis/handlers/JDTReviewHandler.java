package org.unnivp.jdtstaticanalysis.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
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
		return null;
	}

}
