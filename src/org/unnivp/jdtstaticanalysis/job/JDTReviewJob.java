package org.unnivp.jdtstaticanalysis.job;

import java.util.ArrayList;
import java.util.List;

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
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.SingleMemberAnnotation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

						SuppressWarningsAnnotationVisitor suppressWarningVisitor = new SuppressWarningsAnnotationVisitor();
						// Visit the annotation nodes to check for suppress warning occurrences.
						astNodeUnit.accept(suppressWarningVisitor);

						if (suppressWarningVisitor.getWarnings() != null) {
							logger.info(" Suppress Warning violations found in " + unit.getElementName() + " : "
									+ suppressWarningVisitor.getWarnings().size());
							for (SingleMemberAnnotation warningAnnotation : suppressWarningVisitor.getWarnings()) {

								try {
									IMarker marker = unit.getResource().createMarker(IMarker.PROBLEM);
									marker.setAttribute(IMarker.MESSAGE,
											"Suppress Warning annotation found in " + unit.getElementName());
									marker.setAttribute(IMarker.LOCATION, warningAnnotation.getStartPosition());
								} catch (CoreException e) {
									logger.error("Exception occured in JDTReviewJob marker addition :", e);
								}
							}
						}
					}
				}
			} catch (JavaModelException e) {
				logger.error("Exception occured in JDTReviewJob execute :", e);
			}

		}

		return Status.OK_STATUS;
	}

	/**
	 * AST Visitor class that enables visit to annotation nodes for checking
	 * suppress warning occurrences.
	 */
	class SuppressWarningsAnnotationVisitor extends ASTVisitor {

		private List<SingleMemberAnnotation> warningList = new ArrayList<>();

		public SuppressWarningsAnnotationVisitor() {
			super();
		}

		@Override
		public boolean visit(SingleMemberAnnotation annotationNode) {

			if (annotationNode.getTypeName().toString().equals("SuppressWarnings")) {
				warningList.add(annotationNode);
				return true;
			}
			return false;
		}

		public List<SingleMemberAnnotation> getWarnings() {

			return warningList;
		}
	}

}
