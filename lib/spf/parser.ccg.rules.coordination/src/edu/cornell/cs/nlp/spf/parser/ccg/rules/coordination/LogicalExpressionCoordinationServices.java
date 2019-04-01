/*******************************************************************************
 * Copyright (C) 2011 - 2015 Yoav Artzi, All rights reserved.
 * <p>
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or any later version.
 * <p>
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * <p>
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 *******************************************************************************/
package edu.cornell.cs.nlp.spf.parser.ccg.rules.coordination;

import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import edu.cornell.cs.nlp.spf.ccg.categories.ICategoryServices;
import edu.cornell.cs.nlp.spf.mr.lambda.Lambda;
import edu.cornell.cs.nlp.spf.mr.lambda.Literal;
import edu.cornell.cs.nlp.spf.mr.lambda.LogicLanguageServices;
import edu.cornell.cs.nlp.spf.mr.lambda.LogicalConstant;
import edu.cornell.cs.nlp.spf.mr.lambda.LogicalExpression;
import edu.cornell.cs.nlp.spf.mr.lambda.Variable;
import edu.cornell.cs.nlp.spf.mr.lambda.printers.LogicalExpressionToLatexString;
import edu.cornell.cs.nlp.spf.mr.lambda.visitor.ApplyAndSimplify;
import edu.cornell.cs.nlp.spf.mr.lambda.visitor.ReplaceFreeVariablesIfPresent;
import edu.cornell.cs.nlp.spf.mr.lambda.visitor.Simplify;
import edu.cornell.cs.nlp.spf.mr.language.type.ComplexType;
import edu.cornell.cs.nlp.spf.mr.language.type.RecursiveComplexType;
import edu.cornell.cs.nlp.spf.mr.language.type.Type;
import edu.cornell.cs.nlp.utils.composites.Triplet;
//import javafx.util.Pair;
import edu.cornell.cs.nlp.utils.composites.Pair;
import jdk.nashorn.internal.runtime.regexp.joni.Option;

public class LogicalExpressionCoordinationServices implements
		ICoordinationServices<LogicalExpression> {

	private final LogicalConstant						baseConjunctionConstant;
	private final String								baseConjunctionName;

	private final LogicalConstant						baseDisjunctionConstant;
	private final String								baseDisjunctionName;

	private final ICategoryServices<LogicalExpression>	categoryServices;
	
	// BATTUSHIG: variable for keeping track of conjunction
	private final LogicalConstant                       coordinationProductConstant;
	
	public LogicalExpressionCoordinationServices(
			LogicalConstant baseConjunctionConstant,
			LogicalConstant baseDisjunctionConstant,
			ICategoryServices<LogicalExpression> categoryServices) {
		this.baseConjunctionConstant = baseConjunctionConstant;
		this.baseDisjunctionConstant = baseDisjunctionConstant;
		this.categoryServices = categoryServices;
		this.baseConjunctionName = baseConjunctionConstant.getBaseName();
		this.baseDisjunctionName = baseDisjunctionConstant.getBaseName();
		this.coordinationProductConstant = LogicalConstant.read("rel:c");
		
		
//		this.coordinationProductConstant = new LogicalConstant(COORD_PRODUCT_NAME, );
	}

	private static LogicalConstant createPredicate(LogicalConstant constant,
			int numArgs, Type argType) {
		// Using truth type as the final return type, 't' is a simple
		// placeholder here, it's completely meaningless.
		Type type = LogicLanguageServices.getTypeRepository()
				.getTypeCreateIfNeeded(
						LogicLanguageServices.getTypeRepository()
								.getTruthValueType(), argType);
		for (int i = 1; i < numArgs; ++i) {
			type = LogicLanguageServices.getTypeRepository()
					.getTypeCreateIfNeeded(type, argType);
		}
		return LogicalConstant
				.createDynamic(constant.getBaseName(), type, true);
	}
	
	// BATTUSHIG: ADD
	private static LogicalConstant createPredicateWithArgs(LogicalConstant constant, Type[] argTypes) {
		// Using truth type as the final return type, 't' is a simple
		// placeholder here, it's completely meaningless.
		Type type = LogicLanguageServices.getTypeRepository()
				.getTypeCreateIfNeeded(
						LogicLanguageServices.getTypeRepository()
								.getTruthValueType(), argTypes[argTypes.length - 1]);
		for (int i = argTypes.length - 2; i >= 0; --i) {
			type = LogicLanguageServices.getTypeRepository()
					.getTypeCreateIfNeeded(type, argTypes[i]);
		}
		return LogicalConstant
				.createDynamic(constant.getBaseName(), type, true);
	}

// BATTUSHIG: ORIGINAL VERSION
//	@Override
//	public LogicalExpression applyCoordination(LogicalExpression function1,
//			LogicalExpression coordination) {
//		// Verify the coordination
//		if (!(coordination instanceof Literal)
//				|| !isCoordinator(((Literal) coordination).getPredicate(),
//						((Literal) coordination).numArgs())) {
//			return null;
//		}
//
//		final Literal literal = (Literal) coordination;
//		final Type argType = ((ComplexType) literal.getPredicate().getType())
//				.getDomain();
//
//		// Verify the function is a Lambda operator and the type of the argument
//		// fits the coordination
//		final Lambda functionLambda;
//		if (function1 instanceof Lambda) {
//			functionLambda = (Lambda) function1;
//		} else {
//			return null;
//		}
//		if (!argType.isExtending(functionLambda.getArgument().getType())) {
//			return null;
//		}
//		final Variable outerMostVariable = functionLambda.getArgument();
//
//		// Identify shared arguments (Lambda operators and the variables they
//		// bind), locate the outer-most truth-typed expression
//		LogicalExpression innerBody = functionLambda.getBody();
//		final List<Variable> sharedVariables = new LinkedList<Variable>();
//		while (innerBody instanceof Lambda) {
//			sharedVariables.add(((Lambda) innerBody).getArgument());
//			innerBody = ((Lambda) innerBody).getBody();
//		}
//		if (!innerBody.getType().equals(
//				LogicLanguageServices.getTypeRepository().getTruthValueType())) {
//			// Case the inner body is not truth-typed
//			return null;
//		}
//
		// Create a Lambda expression using only the outer Lambda operator and
		// the found truth-type expression as body
//		final Lambda reducedFunction = (Lambda) ReplaceFreeVariablesIfPresent
//				.of(new Lambda(outerMostVariable, innerBody),
//						literal.getFreeVariables());
//
//		// Apply the recently created expression to all arguments
//		final int len = literal.numArgs();
//		final LogicalExpression[] coordinationItems = new LogicalExpression[len];
//		for (int i = 0; i < len; ++i) {
//			final LogicalExpression coordinate = literal.getArg(i);
//			final LogicalExpression application = ApplyAndSimplify.of(
//					reducedFunction, coordinate);
//			if (application == null) {
//				return null;
//			} else {
//				coordinationItems[i] = application; 
//			}
//		}
//
//		// Create a literal (conjunction or disjunction, depending on the
//		// coordinator) by coordinating all the results of the application
//		final Literal coordinationLiteral;
//		if (isConjunctionCoordinator((LogicalConstant) literal.getPredicate())) {
//			coordinationLiteral = new Literal(
//					LogicLanguageServices.getConjunctionPredicate(),
//					coordinationItems);
//		} else if (isDisjunctionCoordinator((LogicalConstant) literal
//				.getPredicate())) {
//			coordinationLiteral = new Literal(
//					LogicLanguageServices.getDisjunctionPredicate(),
//					coordinationItems);
//		} else {
//			throw new IllegalStateException("invalid coordinator: "
//					+ literal.getPredicate());
//		}
//
//		// Wrap with the shared Lambda operators and return the complete
//		// expression
//		LogicalExpression wrappedCoordination = coordinationLiteral;
//		final ListIterator<Variable> iterator = sharedVariables
//				.listIterator(sharedVariables.size());
//		while (iterator.hasPrevious()) {
//			wrappedCoordination = new Lambda(iterator.previous(),
//					wrappedCoordination);
//		}
//
//		return Simplify.of(wrappedCoordination);
//	}
	
	// CREATES: the logical form: lambda $0:<type(left)> rel ()
	@Override
	public LogicalExpression applyCoordination(LogicalExpression left,
			LogicalExpression right) {
		// System.out.println("COORD: START");
		// Making sure that the right side is conjunction literal 
		if (!(right instanceof Literal)) {
			// System.out.printf("COORD: Right side is not Literal | LEFT: %s RIGHT: %s", left.toString(), right.toString());
			return null; 
		}
		final Literal rightLiteral = (Literal) right;
		
		// System.out.printf("COORD: Left: %s  Right %s \n", left.toString(), rightLiteral.getPredicate().toString());
		if (!(rightLiteral.getPredicate() instanceof LogicalConstant)) {
			// System.out.printf("COORD: Right side's pred is not LogConst |RIGHT PRED: %s", rightLiteral.getPredicate().toString());
			return null;
		} else if (!(((LogicalConstant) rightLiteral.getPredicate()).getBaseName().equals("conj"))) {
			// System.out.printf("COORD: Right side's pred is conj type |RIGHT BASE NAME: %s", ((LogicalConstant) rightLiteral.getPredicate()).getBaseName());
			return null;
		}
		
		// New literal will be produced with one argument and conj<argType>
		final LogicalExpression rightSemantics = rightLiteral.getArg(0);
		// System.out.printf("COORD_SEMANTICS: Left: %s Right: %s", left.toString(), rightSemantics.toString());
		
		// Finding all the common arguments 
		LogicalExpression leftInner = left;
		LogicalExpression rightInner = rightSemantics;
		
		final List<Variable> sharedVariables = new LinkedList<Variable>();
		
		// Used for substituting functional type variables 
		// COPY_VAR, LEFT_VAR, RIGHT_VAR
		final List<Triplet<Variable, Variable, Variable>> functTypeVariables = new LinkedList<Triplet<Variable, Variable, Variable>>();
		
		// Variables that include only include objects
		final List<Variable> leftVariables = new LinkedList<Variable>();
		final List<Variable> rightVariables = new LinkedList<Variable>();
		final List<Variable> bindingVariables = new LinkedList<Variable>();
		
		while (leftInner instanceof Lambda && rightInner instanceof Lambda) {
			final Lambda leftExp = (Lambda) leftInner;
			final Variable leftVar = leftExp.getArgument();
			leftInner = leftExp.getBody();
			
			final Lambda rightExp = (Lambda) rightInner;
			final Variable rightVar = rightExp.getArgument();
			rightInner = rightExp.getBody();
			
			
			// If type signature of the arguments differs, it cannot be coordinated accordingly
			if (!leftVar.getType().equals(rightVar.getType())) {
				// System.out.printf("TYPE_MISMATCH: LEFT: %s RIGHT: %s", leftVar.toString(), rightVar.toString());
				return null;
			}
			
			final Type varType = leftVar.getType();
			final Variable copyVar = new Variable(varType); 
			
			if (varType instanceof ComplexType) {
				functTypeVariables.add(Triplet.of(copyVar, leftVar, rightVar));
			} else {
				// add it to right and left variables
				leftVariables.add(leftVar);
				rightVariables.add(rightVar);
				bindingVariables.add(copyVar);
			}
			
			sharedVariables.add(copyVar);
		}
		
		// System.out.printf("SHARED_VARIABLES:", sharedVariables.toString());
		
		// Either Literal or Logical constant have to be left after levels of lambda removal 
		if (leftInner.getClass() != rightInner.getClass()) {
			return null;
		}
		
		if  (rightInner instanceof Lambda) {
			return null;
		}
		
		final LogicalExpression[] arguments = new LogicalExpression[bindingVariables.size()+2];
		
		// Need to replace functional variables with corresponding variables
		final Pair<LogicalExpression, LogicalExpression> results = replaceVariables(leftInner, rightInner, functTypeVariables);
		arguments[0] = getLambdaWrapped(results.first(), leftVariables);
		arguments[1] = getLambdaWrapped(results.second(), rightVariables);
		
		for (int i=0; i<bindingVariables.size(); ++i) {
			arguments[i+2] = bindingVariables.get(i);
		}
		
		final Type[] argTypes = new Type[bindingVariables.size()+2];
		for (int i=0; i<arguments.length; ++i){
			argTypes[i] = arguments[i].getType();
		}
		
		final LogicalConstant constantPredicate = createPredicateWithArgs(coordinationProductConstant, argTypes);
		
//		LogicalExpression wrappedCoordination = new Literal(constantPredicate, arguments);
//		// System.out.println("APPLYING_COORD: Constant predicate " + constantPredicate.toString());
//		final ListIterator<Variable> iterator = sharedVariables
//				.listIterator(sharedVariables.size());
//		while (iterator.hasPrevious()) {
//			wrappedCoordination = new Lambda(iterator.previous(),
//					wrappedCoordination);
//		}
		
		return getLambdaWrapped(new Literal(constantPredicate, arguments), sharedVariables);
		
	}
	
	private LogicalExpression getLambdaWrapped(LogicalExpression body, List<Variable> vars) {
		final ListIterator<Variable> iterator = vars.listIterator(vars.size());
		LogicalExpression wrappedCoordination = body;
		while (iterator.hasPrevious()) {
			wrappedCoordination = new Lambda(iterator.previous(),
					wrappedCoordination);
		}
		
		return Simplify.of(wrappedCoordination);
	}
	
	private Pair<LogicalExpression, LogicalExpression> replaceVariables(LogicalExpression  leftExp, LogicalExpression  rightExp, 
										List<Triplet<Variable, Variable, Variable>> functTypeVariables) {
		LogicalExpression leftRep = leftExp;
		LogicalExpression rightRep = rightExp;
		
		// TODO
		for (Triplet<Variable, Variable, Variable> triplet : functTypeVariables) {
			//leftRep = leftRep.replaceVar(triplet.second(), triplet.first());
			//rightRep = rightRep.replaceVar(triplet.third(), triplet.first());
		}
		
		return Pair.of(leftRep, rightRep);
	}
	
	
// BATTUSHIG: Original version
//	@Override
//	public LogicalExpression createPartialCoordination(
//			LogicalExpression coordinated, LogicalExpression coordinator) {
//		// Create a binary predicate from coordinator
//		if (!isBaseCoordinator(coordinator)) {
//			return null;
//		}
//
//		// The type of the coordinated element is generalized to create the
//		// coordination predicate
//		final Type argType = LogicLanguageServices.getTypeRepository()
//				.generalizeType(coordinated.getType());
//		final LogicalConstant coordinationPredicate = createPredicate(
//				(LogicalConstant) coordinator, 2, argType);
//
//		// Create a literal using the predicate, with a variable as the
//		// first argument and 'coordinated' as the second, and wrap the literal
//		// with a lambda expression binding the varaible.
//		final LogicalExpression[] arguments = new LogicalExpression[2];
//		final Variable variable = new Variable(argType);
//		arguments[0] = variable;
//		arguments[1] = coordinated;
//		return new Lambda(variable, new Literal(coordinationPredicate,
//				arguments));
//	}
	
	// BATTUSHIG: Created rule to create literal with special type
	@Override
	public LogicalExpression createPartialCoordination(
			LogicalExpression coordinated, LogicalExpression coordinator) {
		// Create a binary predicate from coordinator
		if (!isBaseCoordinator(coordinator)) {
			return null;
		}

		// The type of the coordinated element is generalized to create the
		// coordination predicate
		final Type argType = LogicLanguageServices.getTypeRepository()
				.generalizeType(coordinated.getType());
		
		// Create predicate with form: (conj:<argType> coordinated) 
		final LogicalConstant coordinationPredicate = createPredicate(
				(LogicalConstant) coordinator, 1, argType);

		// Create a literal using the predicate, with a variable as the
		final LogicalExpression[] arguments = new LogicalExpression[1];
		arguments[0] = coordinated;
		return new Literal(coordinationPredicate,
				arguments);
	}

	@Override
	public LogicalExpression createSimpleCoordination(
			LogicalExpression coordinated, LogicalExpression coordinator) {
		// Create a binary predicate from coordinator
		if (!isBaseCoordinator(coordinator)) {
			return null;
		}

		if (LogicLanguageServices.getTypeRepository().getTruthValueType()
				.equals(coordinated.getType())) {
			// Case coordinating truth-typed expressions
			final LogicalExpression coordinationPredicate;
			if (isConjunctionCoordinator((LogicalConstant) coordinator)) {
				coordinationPredicate = LogicLanguageServices
						.getConjunctionPredicate();
			} else if (isDisjunctionCoordinator((LogicalConstant) coordinator)) {
				coordinationPredicate = LogicLanguageServices
						.getDisjunctionPredicate();
			} else {
				throw new IllegalStateException("invalid coordinator: "
						+ coordinator);
			}

			final Variable variable = new Variable(LogicLanguageServices
					.getTypeRepository().getTruthValueType());
			final LogicalExpression[] args = new LogicalExpression[2];
			args[0] = variable;
			args[1] = coordinated;
			return Simplify.of(new Literal(coordinationPredicate, args));
		}

		return null;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final LogicalExpressionCoordinationServices other = (LogicalExpressionCoordinationServices) obj;
		if (baseConjunctionConstant == null) {
			if (other.baseConjunctionConstant != null) {
				return false;
			}
		} else if (!baseConjunctionConstant
				.equals(other.baseConjunctionConstant)) {
			return false;
		}
		if (baseConjunctionName == null) {
			if (other.baseConjunctionName != null) {
				return false;
			}
		} else if (!baseConjunctionName.equals(other.baseConjunctionName)) {
			return false;
		}
		if (baseDisjunctionConstant == null) {
			if (other.baseDisjunctionConstant != null) {
				return false;
			}
		} else if (!baseDisjunctionConstant
				.equals(other.baseDisjunctionConstant)) {
			return false;
		}
		if (baseDisjunctionName == null) {
			if (other.baseDisjunctionName != null) {
				return false;
			}
		} else if (!baseDisjunctionName.equals(other.baseDisjunctionName)) {
			return false;
		}
		if (categoryServices == null) {
			if (other.categoryServices != null) {
				return false;
			}
		} else if (!categoryServices.equals(other.categoryServices)) {
			return false;
		}
		return true;
	}

	@Override
	public LogicalExpression expandCoordination(LogicalExpression coordination) {
		if (coordination instanceof Literal
				&& isCoordinator(((Literal) coordination).getPredicate(),
						((Literal) coordination).numArgs())) {
			final Literal literal = (Literal) coordination;
			final Type argType = ((ComplexType) literal.getPredicate()
					.getType()).getDomain();
			final int len = literal.numArgs();
			final LogicalExpression[] expandedArgs = new LogicalExpression[len + 1];
			// The variable referring to the new argument
			final LogicalExpression variable = new Variable(argType);
			expandedArgs[0] = variable;
			literal.copyArgsIntoArray(expandedArgs, 0, 1, len);
			return new Literal(
					createPredicate((LogicalConstant) literal.getPredicate(),
							len + 1, argType), expandedArgs);
		} else {
			return null;
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime
				* result
				+ (baseConjunctionConstant == null ? 0
						: baseConjunctionConstant.hashCode());
		result = prime
				* result
				+ (baseConjunctionName == null ? 0 : baseConjunctionName
						.hashCode());
		result = prime
				* result
				+ (baseDisjunctionConstant == null ? 0
						: baseDisjunctionConstant.hashCode());
		result = prime
				* result
				+ (baseDisjunctionName == null ? 0 : baseDisjunctionName
						.hashCode());
		result = prime * result
				+ (categoryServices == null ? 0 : categoryServices.hashCode());
		return result;
	}

	private boolean isBaseCoordinator(LogicalExpression predicate) {
		return baseDisjunctionConstant.equals(predicate)
				|| baseConjunctionConstant.equals(predicate);
	}
	
//	// BATTUSHIG: Add
//	private boolean isBaseCoordinatorPredicate(LogicalExpression predicate) {
//		return baseDisjunctionConstant.getName().equals(predicate.get)
//				|| baseConjunctionConstant.getName().equals(type.getName());
//	}

	private boolean isConjunctionCoordinator(LogicalConstant predicate) {
		return predicate.getBaseName().equals(baseConjunctionName);
	}

	private boolean isCoordinator(LogicalExpression predicate, int numArgs) {
		if (predicate instanceof LogicalConstant
				&& (isConjunctionCoordinator((LogicalConstant) predicate) || isDisjunctionCoordinator((LogicalConstant) predicate))
				&& predicate.getType() instanceof ComplexType) {
			final ComplexType predicateType = (ComplexType) predicate.getType();
			Type current = predicateType;
			int count = 0;
			while (current instanceof ComplexType) {
				++count;
				current = ((ComplexType) current).getRange();
			}
			return count == numArgs;
		} else {
			return false;
		}
	}

	private boolean isDisjunctionCoordinator(LogicalConstant predicate) {
		return predicate.getBaseName().equals(baseDisjunctionName);
	}

}
