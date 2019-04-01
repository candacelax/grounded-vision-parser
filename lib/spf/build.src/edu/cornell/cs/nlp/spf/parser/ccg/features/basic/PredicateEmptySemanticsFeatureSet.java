package edu.cornell.cs.nlp.spf.parser.ccg.features.basic;

import java.util.Collections;
import java.util.Set;

import edu.cornell.cs.nlp.spf.base.hashvector.IHashVector;
import edu.cornell.cs.nlp.spf.base.hashvector.KeyArgs;
import edu.cornell.cs.nlp.spf.ccg.lexicon.LexicalEntry;
import edu.cornell.cs.nlp.spf.data.IDataItem;
import edu.cornell.cs.nlp.spf.explat.IResourceRepository;
import edu.cornell.cs.nlp.spf.explat.ParameterizedExperiment.Parameters;
import edu.cornell.cs.nlp.spf.explat.resources.IResourceObjectCreator;
import edu.cornell.cs.nlp.spf.explat.resources.usage.ResourceUsage;
import edu.cornell.cs.nlp.spf.mr.lambda.LogicalExpression;
import edu.cornell.cs.nlp.spf.mr.lambda.visitor.GetAllLiterals;
import edu.cornell.cs.nlp.spf.mr.lambda.visitor.GetAllPredicates;
import edu.cornell.cs.nlp.spf.parser.ccg.IParseStep;
import edu.cornell.cs.nlp.spf.parser.ccg.model.lexical.ILexicalFeatureSet;

public class PredicateEmptySemanticsFeatureSet<DI extends IDataItem<?>, MR>
	implements ILexicalFeatureSet<DI, MR> {
	
	
	/**
	 * 
	 * Computes number of steps that derived logical form that do not contribute predicates.
	 * We refer to steps that do not contribute predicates as empty semantics.
	 * 
	 * @author Candace Ross
	 * extension of existing features classes by Yoav Artzi
	 *
	 */
	
	private static final String	FEATURE_TAG			= "PRED";
	private static final double	SCALE				= 10.0;
	// TODO decide on scale
	private static final long serialVersionUID = -9094261966461237776L; // auto-generated

	
	public PredicateEmptySemanticsFeatureSet(){
		return;
	}
	
	@Override
	public Set<KeyArgs> getDefaultFeatures() {
		return Collections.emptySet();
	}
	
	@Override
	public boolean addEntry(LexicalEntry<MR> entry, IHashVector parametersVector) {
		// Nothing to do.
		return false;
	}
	
	@Override
	public void setFeatures(IParseStep<MR> parseStep, IHashVector features, DI dataItem) {
		if (!parseStep.isFullParse()) {
			// Only generate features of the final logical format
			return;
		}
		
		final MR semantics = parseStep.getRoot().getSemantics();
		if (semantics == null) {
			return;
		}
		
		final float numEmptyExpressions = getNumEmptyExpressions((LogicalExpression) semantics);
		
		features.set(FEATURE_TAG, SCALE * numEmptyExpressions);
		
		return;
	}
	
	// TODO
	private float getNumEmptyExpressions(LogicalExpression semantics){
		/*List<String> preds1 = new ArrayList<String>();
		List<String> preds2 = new ArrayList<String>();
		
		for (Literal l : GetAllLiterals.of(semantics)){
			preds1.add(l.getPredicate().toString());
		}
		
		for (LogicalConstant l : GetAllPredicates.of(semantics)){
			preds2.add(l.toString());
		}
		System.out.println(GetAllPredicates.of(semantics).size() / GetAllLiterals.of(semantics).size());
		return preds2.size()/preds1.size();
		*/
		return (float) GetAllPredicates.of(semantics).size() /
					(float) GetAllLiterals.of(semantics).size();
	}
	
	/*
	private int getNumEmptyExpressions(MR semantics){
		// removes parentheses from expression and splits
		//final String[] splitSemantics = Pattern.compile("\\)|\\(").matcher(semantics.toString()
			//	.replaceAll(":", " :")).replaceAll("").split(" ");
		//return getNumEmptyExpressions(splitSemantics);
	}
	
	private int getNumEmptyExpressions(String[] semantics){	
		int numEmptyExpressions = 0;
		for (final String s : semantics){
			//if (!s.matches("lambda|and|\\$[0-9]+|:.+")){
				//System.out.println("S " + s);
				numEmptyExpressions += 1;
			//}
		}
		//System.exit(0);
		return numEmptyExpressions;
	}
	*/
	
	public String type() {
		return "feat.predicate";
	}
	
	public static class Creator<DI extends IDataItem<?>, MR> implements
		IResourceObjectCreator<PredicateEmptySemanticsFeatureSet<DI, MR>> {
		private final String type;
		
		public Creator() {
			this("feat.predicate");
		}
		
		public Creator(String type) {
			this.type = type;
		}
		
		@Override
		public PredicateEmptySemanticsFeatureSet<DI, MR> create(Parameters params, IResourceRepository repo) {
			return new PredicateEmptySemanticsFeatureSet<DI, MR>();
		}
		
		@Override
		public String type() {
			return type;
		}

		@Override
		public ResourceUsage usage() {
			// TODO Auto-generated method stub
			return null;
		}
	}
}
