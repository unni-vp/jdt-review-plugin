package org.unnivp.jdtstaticanalysis.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 * Handler Class that returns the count of total classes and methods in the
 * current workspace.
 * 
 * @author unni-vp
 *
 */
public class SourceCodeCountHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {

		// Get all projects in the workspace
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IWorkspaceRoot root = workspace.getRoot();
		IProject[] projects = root.getProjects();

		int compliationUnitCount = 0;
		int methodCount = 0;
		
		// Loop through the projects and packages
		for (IProject project : projects) {
			try {
				IPackageFragment[] packages = JavaCore.create(project).getPackageFragments();
				for (IPackageFragment mypackage : packages) {
					// Get each source code file and count the methods in the file.
					ICompilationUnit[] units = mypackage.getCompilationUnits();
					compliationUnitCount += units.length;
					for (ICompilationUnit unit : units) {
						if (unit.getTypes() != null && unit.getTypes().length > 0) {
							IMethod[] methods = unit.getTypes()[0].getMethods();
							methodCount += methods.length;
						}
					}
				}
			} catch (JavaModelException e) {
				System.err.println("Exception occured in SourceCodeCountHandler execute :" + e.getMessage());
			}
		}

		// Populate the results in a new workbench window.
		IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindowChecked(event);
		MessageDialog.openInformation(window.getShell(), "JDT Review", "Total file count for workspace is : "
				+ compliationUnitCount + "\nTotal method count for workspace is : " + methodCount);

		return null;
	}

}
