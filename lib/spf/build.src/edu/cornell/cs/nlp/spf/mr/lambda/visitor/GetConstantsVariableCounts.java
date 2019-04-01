package edu.cornell.cs.nlp.spf.mr.lambda.visitor;

import java.util.LinkedList;
import java.util.List;

import edu.cornell.cs.nlp.spf.mr.lambda.Lambda;
import edu.cornell.cs.nlp.spf.mr.lambda.Literal;
import edu.cornell.cs.nlp.spf.mr.lambda.LogicalConstant;
import edu.cornell.cs.nlp.spf.mr.lambda.LogicalExpression;
import edu.cornell.cs.nlp.spf.mr.lambda.Variable;

/**
 * Creates a map of all strings and variable counts in an expression.
 * 
 * Similar to GetAllStrings by Yoav Artzi, this list will contain duplicates.
 *
 * @author Candace Ross
 */

public class GetConstantsVariableCounts implements ILogicalExpressionVisitor {
	private final List<TupleConstantCount>	constantStringsCounts	= new LinkedList<TupleConstantCount>();

	private GetConstantsVariableCounts() {
		// Usage only through static 'of' method.
	}

	public static List<TupleConstantCount> of(LogicalExpression exp) {
		final GetConstantsVariableCounts visitor = new GetConstantsVariableCounts();
		visitor.visit(exp);
		return visitor.getConstantStringsCounts();
	}

	public List<TupleConstantCount> getConstantStringsCounts() {
		return constantStringsCounts;
	}

	@Override
	public void visit(Lambda lambda) {
		lambda.getBody().accept(this);
	}

	@Override
	public void visit(Literal literal) {
		// only add predicate if it's not 'and'
		String predicateWithoutType = literal.getPredicate().toString()
				.replaceAll(":\\<*[a-z|\\**,|\\>|\\<]+\\>*", "").replaceAll("\\(", "").replaceAll("\\)", "");
		
		if (!predicateWithoutType.equals("and")){
			constantStringsCounts.add(new TupleConstantCount(predicateWithoutType,
																CountVariables.of(literal, false)));
		}
		
		// continue visitor
		literal.getPredicate().accept(this);
		final int len = literal.numArgs();
		for (int i = 0; i < len; ++i) {
			literal.getArg(i).accept(this);
		}
	}

	@Override
	public void visit(LogicalConstant logicalConstant) {
		// nothing to do
	}

	@Override
	public void visit(LogicalExpression logicalExpression) {
		logicalExpression.accept(this);
	}

	@Override
	public void visit(Variable variable) {
		// Nothing to do
	}
}
