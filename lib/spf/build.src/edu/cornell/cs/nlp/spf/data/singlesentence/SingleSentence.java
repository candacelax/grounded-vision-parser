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
package edu.cornell.cs.nlp.spf.data.singlesentence;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.cornell.cs.nlp.spf.base.properties.Properties;
import edu.cornell.cs.nlp.spf.data.ILabeledDataItem;
import edu.cornell.cs.nlp.spf.data.sentence.Sentence;
import edu.cornell.cs.nlp.spf.mr.lambda.ExtractExpressions;
import edu.cornell.cs.nlp.spf.mr.lambda.LogicalExpression;
import javafx.util.Pair;

/**
 * Represents a single sentence and its logical form for supervised learning.
 *
 * @author Yoav Artzi
 */
public class SingleSentence implements
		ILabeledDataItem<Sentence, LogicalExpression> {
	private static final long			serialVersionUID	= 5434665811874050978L;
	private final Map<String, String>	properties;

	private final LogicalExpression		semantics;
	private final Sentence				sentence;
	
	/**
	 * -- added by Candace --
	 * used to compare predicted and gold logical forms
	 */
	private List<String> 				binders = new ArrayList<String>();
	private List<List<String>> 			permutedBinders = new ArrayList<List<String>>();
	private List<String> 				literalExpressions = new ArrayList<String>();
	private List<List<String>> 			renamedExpressions = new ArrayList<List<String>>();
	
	private final Map<String, Boolean> parseCache = new HashMap<String, Boolean>();

	public SingleSentence(Sentence sentence, LogicalExpression semantics) {
		this(sentence, semantics, new HashMap<String, String>());
	}

	public SingleSentence(Sentence sentence, LogicalExpression semantics,
			Map<String, String> properties) {
		this.sentence = sentence;
		this.semantics = semantics;
		this.properties = Collections.unmodifiableMap(properties);		
		Pair<List<String>, List<String>> extracted = ExtractExpressions.extract(semantics);
		binders = extracted.getKey();
		literalExpressions = extracted.getValue();
		permutedBinders = ExtractExpressions.permute(binders);
		renamedExpressions = ExtractExpressions.renameExpressions(binders, literalExpressions, permutedBinders);
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
		final SingleSentence other = (SingleSentence) obj;
		if (properties == null) {
			if (other.properties != null) {
				return false;
			}
		} else if (!properties.equals(other.properties)) {
			return false;
		}
		if (semantics == null) {
			if (other.semantics != null) {
				return false;
			}
		} else if (!semantics.equals(other.semantics)) {
			return false;
		}
		if (sentence == null) {
			if (other.sentence != null) {
				return false;
			}
		} else if (!sentence.equals(other.sentence)) {
			return false;
		}
		return true;
	}

	@Override
	public LogicalExpression getLabel() {
		return semantics;
	}

	public Map<String, String> getProperties() {
		return properties;
	}

	@Override
	public Sentence getSample() {
		return sentence;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ (properties == null ? 0 : properties.hashCode());
		result = prime * result
				+ (semantics == null ? 0 : semantics.hashCode());
		result = prime * result + (sentence == null ? 0 : sentence.hashCode());
		return result;
	}

	@Override
	public boolean isCorrect(LogicalExpression label) {
		return label.equals(semantics);
	}
	
	
	/** -- added by Candace --
	 * returns binders (e.g. $0 $1) for expression
	 */
   @Override
	public List<String> getBinders(){
		return binders;
	}
	
	/** -- added by Candace --
	 * returns all permutations of binders (e.g. {($0 $1) ($1 $0)} ) for expression
	 */
	@Override
	public List<List<String>> getPermutedBinders(){
		return permutedBinders;
	}
	
	// TODO
	/** -- added by Candace --
	 * 
	 */
	@Override
	public List<String> getLiteralExpressions(){
		return literalExpressions;
	}
	
	/** -- added by Candace --
	 * 
	 */
	@Override
	public List<List<String>> getRenamedExpressions(){
		return renamedExpressions;
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder(sentence.toString())
				.append('\n');
		if (!properties.isEmpty()) {
			sb.append(Properties.toString(properties)).append('\n');
		}
		sb.append(semantics).toString();
		return sb.toString();
	}

	
	// TODO
	@Override
	public void addToCache(String semantics, Boolean isValid){
		parseCache.put(semantics, isValid);
	}
	
	// TODO
	@Override
	public boolean existsInCache(String semantics) {
		return parseCache.containsKey(semantics);
	}

	// TODO
	@Override
	public boolean validInCache(String parse) {
		if (existsInCache(parse)){
			return parseCache.get(parse);
		}
		else {
			throw new IllegalArgumentException("This parse does not exist in the cache!");
		}
	}

}
