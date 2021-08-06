package org.unnivp.jdtstaticanalysis.job;

import static org.unnivp.jdtstaticanalysis.constants.ReviewConstants.*;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.SingleMemberAnnotation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.unnivp.jdtstaticanalysis.constants.ReviewConstants;
import org.unnivp.jdtstaticanalysis.visitor.SuppressWarningsAnnotationVisitor;

/**
 * Async job to review entire workspace for custom violations.
 * 
 * @author unni-vp
 *
 */
public class JDTReviewJob extends Job {

	private static final Logger logger = LoggerFactory.getLogger(JDTReviewJob.class);

	public JDTReviewJob(String name) {
		super(name);
	}

	@Override
	protected IStatus run(IProgressMonitor arg0) {

		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		for (IProject project : workspace.getRoot().getProjects()) {

			logger.info("JDT Project Review requested for: " + project);
			if (!project.isAccessible()) {
				continue;
			}
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
						SuppressWarningsAnnotationVisitor suppressWarningVisitor = new SuppressWarningsAnnotationVisitor();
						astNodeUnit.accept(suppressWarningVisitor);
						
						// Clear previous markers.
						deleteExistingMarkers(unit);

						// Check and add markers for review violation.
						markSuppressWarningViolations(unit, astNodeUnit, suppressWarningVisitor);
					}
				}
			} catch (JavaModelException e) {
				logger.error("Exception occured in JDTReviewJob execute :", e);
			}
		}

		return Status.OK_STATUS;
	}

	private void deleteExistingMarkers(ICompilationUnit unit) {
		try {
			for (IMarker marker : unit.getResource().findMarkers(ReviewConstants.JDT_REVIEW_MARKER, true, 1)) {
				if (((String) marker.getAttribute(IMarker.MESSAGE)).startsWith("JDT Review violation")) {
					marker.delete();
				}
			}
		} catch (CoreException e) {
			logger.error("Exception occured in JDTReviewJob marker deletion :", e);
		}
	}

	private void markSuppressWarningViolations(ICompilationUnit unit, ASTNode astNodeUnit,
			SuppressWarningsAnnotationVisitor suppressWarningVisitor) {

		if (suppressWarningVisitor.getWarnings() != null) {

			logger.info("Suppress Warning violations found in " + unit.getElementName() + " : "
					+ suppressWarningVisitor.getWarnings().size());

			for (SingleMemberAnnotation warningAnnotation : suppressWarningVisitor.getWarnings()) {

				try {
					int lineNumber = ((CompilationUnit) astNodeUnit).getLineNumber(warningAnnotation.getStartPosition());

					IMarker marker = unit.getResource().createMarker(JDT_REVIEW_MARKER);
					marker.setAttribute(IMarker.MESSAGE, "JDT Review violation for : "
							+ warningAnnotation.getTypeName() + " - " + warningAnnotation.getValue().toString());
					marker.setAttribute(IMarker.LINE_NUMBER, lineNumber);

				} catch (CoreException e) {
					logger.error("Exception occured in JDTReviewJob marker addition :", e);
				}
			}
		}
	}

}
