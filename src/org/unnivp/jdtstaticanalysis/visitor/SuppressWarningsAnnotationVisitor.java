package org.unnivp.jdtstaticanalysis.visitor;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.SingleMemberAnnotation;

/**
 * AST Visitor class that enables visit to annotation nodes for checking
 * suppress warning occurrences. Records visits with violations into a list.
 */
public class SuppressWarningsAnnotationVisitor extends ASTVisitor {
	
	private static final String SUPPRESS_WARNING = "SuppressWarnings";

	private List<SingleMemberAnnotation> warningList = new ArrayList<>();

	public SuppressWarningsAnnotationVisitor() {
		super();
	}

	@Override
	public boolean visit(SingleMemberAnnotation annotationNode) {

		if (annotationNode.getTypeName().toString().equals(SUPPRESS_WARNING)) {
			warningList.add(annotationNode);
			return true;
		}
		return false;
	}

	public List<SingleMemberAnnotation> getWarnings() {

		return warningList;
	}
}