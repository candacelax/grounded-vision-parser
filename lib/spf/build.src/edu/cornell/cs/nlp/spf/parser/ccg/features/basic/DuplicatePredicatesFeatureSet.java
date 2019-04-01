package edu.cornell.cs.nlp.spf.parser.ccg.features.basic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import edu.cornell.cs.nlp.spf.base.hashvector.IHashVector;
import edu.cornell.cs.nlp.spf.base.hashvector.KeyArgs;
import edu.cornell.cs.nlp.spf.ccg.lexicon.LexicalEntry;
import edu.cornell.cs.nlp.spf.data.IDataItem;
import edu.cornell.cs.nlp.spf.explat.IResourceRepository;
import edu.cornell.cs.nlp.spf.explat.ParameterizedExperiment.Parameters;
import edu.cornell.cs.nlp.spf.explat.resources.IResourceObjectCreator;
import edu.cornell.cs.nlp.spf.explat.resources.usage.ResourceUsage;
import edu.cornell.cs.nlp.spf.mr.lambda.LogicalConstant;
import edu.cornell.cs.nlp.spf.mr.lambda.LogicalExpression;
import edu.cornell.cs.nlp.spf.mr.lambda.visitor.GetAllLiterals;
import edu.cornell.cs.nlp.spf.mr.lambda.visitor.GetConstCounts;
import edu.cornell.cs.nlp.spf.parser.ccg.IParseStep;
import edu.cornell.cs.nlp.spf.parser.ccg.model.lexical.ILexicalFeatureSet;
import edu.cornell.cs.nlp.utils.counter.Counter;

public class DuplicatePredicatesFeatureSet<DI extends IDataItem<?>, MR>
	implements ILexicalFeatureSet<DI, MR> {

	/**
	 * 
	 * 
	 * @author Candace Ross
	 * extension of existing features classes by Yoav Artzi
	 *
	 */
	
	private static final String	FEATURE_TAG			= "DUP";
	// TODO decide on scale
	private static final double	SCALE				= 2.0;
	private static final long serialVersionUID = -406487446979772430L;
	

	
	public DuplicatePredicatesFeatureSet(){
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
		
		if (!(semantics instanceof LogicalExpression)){
			return;
		}
		
		final int numDuplicates = getNumDuplicateExpressions((LogicalExpression) semantics);
		features.set(FEATURE_TAG, SCALE * (numDuplicates + 1));
		
		return;
	}
	
	private int getNumDuplicateExpressions(LogicalExpression semantics){
		int numDuplicates = 0;
		final String lit = GetAllLiterals.of(semantics).get(0).toString();
		final Map<LogicalConstant, Counter> earlyCounts  = GetConstCounts.of(semantics);
		final Map<LogicalConstant, Counter> filteredCounts = 
					earlyCounts.keySet().stream().filter(k -> earlyCounts.get(k).value() > 1).collect(Collectors.toMap(k -> k, k -> earlyCounts.get(k)));
		
		
		for (final LogicalConstant key : filteredCounts.keySet()){
			if (filteredCounts.get(key).value() == 1){
				continue;
			}
			
			List<String> substrings = new ArrayList<String>();
			final Matcher m = Pattern.compile("\\("+ key.toString() + " [(\\$[0-9])(\\$[0-9]:[(a-z)(\\<.*\\>)](\\$[0-9] )]+\\)").matcher(lit);
			while (m.find()){
				if (substrings.contains(lit.substring(m.start(), m.end()))){
					numDuplicates++;
				} else{
					substrings.add(lit.substring(m.start(), m.end()));
				}
			}
		}
		return numDuplicates;
		
		//return ExtractExpressions.getNumDuplicates(semantics);
	}
	
	public String type() {
		return "feat.duplicate";
	}
	
	public static class Creator<DI extends IDataItem<?>, MR> implements
		IResourceObjectCreator<DuplicatePredicatesFeatureSet<DI, MR>> {
		private final String type;
		
		public Creator() {
			this("feat.duplicate");
		}
		
		public Creator(String type) {
			this.type = type;
		}
		
		@Override
		public DuplicatePredicatesFeatureSet<DI, MR> create(Parameters params, IResourceRepository repo) {
			return new DuplicatePredicatesFeatureSet<DI, MR>();
		}
		
		@Override
		public String type() {
			return type;
		}

		public ResourceUsage usage() {
			return new ResourceUsage.Builder(type(), DuplicatePredicatesFeatureSet.class)
					.setDescription(
							"Computes the number of duplicate literal expressions in logical form.")
					.build();
		}
	}
}