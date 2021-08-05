package org.unnivp.jdtstaticanalysis.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.SingleMemberAnnotation;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handler Class that returns the count of occurrences of the Suppress Warning
 * annotations current workspace.
 * 
 * @author unni-vp
 *
 */
public class SuppressWarningAnnotationHandler extends AbstractHandler {

	private static final Logger logger = LoggerFactory.getLogger(SuppressWarningAnnotationHandler.class);
	
	private static int suppressWarningCount;

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {

		// Get all projects in the workspace
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IWorkspaceRoot root = workspace.getRoot();
		IProject[] projects = root.getProjects();

		suppressWarningCount = 0;
		// Loop through the projects and packages
		for (IProject project : projects) {
			try {
				IPackageFragment[] packages = JavaCore.create(project).getPackageFragments();
				for (IPackageFragment mypackage : packages) {
					ICompilationUnit[] units = mypackage.getCompilationUnits();
					for (ICompilationUnit unit : units) {
						// Create AST Node for traversal from each Compilation Unit.
						ASTParser parser = ASTParser.newParser(AST.JLS15);
						parser.setKind(ASTParser.K_COMPILATION_UNIT);
						parser.setSource(unit);
						parser.setResolveBindings(true);
						final ASTNode astNodeUnit = parser.createAST(null);
						// Visit the annotation nodes to check for suppress warning occurrences.
						astNodeUnit.accept(new SuppressWarningsAnnotationVisitor());
					}
				}
			} catch (JavaModelException e) {
				logger.error("Exception occured in SuppressWarningAnnotationHandler execute :", e);
			}
		}

		// Populate the results in a new workbench window.
		IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindowChecked(event);
		MessageDialog.openInformation(window.getShell(), "Jdt-static-analysis",
				"Total suppress warning occurrences : " + suppressWarningCount);

		return null;
	}

	
	/**
	 * Static method for updating suppressWarningCount
	 */
	final static void updateWarningCount() {
		suppressWarningCount++;
	}

	
	/**
	 * AST Visitor class that enables visit to annotation nodes for checking
	 * suppress warning occurrences.
	 */
	class SuppressWarningsAnnotationVisitor extends ASTVisitor {

		public SuppressWarningsAnnotationVisitor() {
			super();
		}

		@Override
		public boolean visit(SingleMemberAnnotation annotationNode) {
			if (annotationNode.getTypeName().toString().equals("SuppressWarnings")) {
				updateWarningCount();
			}
			return true;
		}
	}

}
