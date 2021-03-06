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

import java.util.ArrayList;
import java.util.List;

import edu.cornell.cs.nlp.spf.ccg.categories.ICategoryServices;
import edu.cornell.cs.nlp.spf.explat.IResourceRepository;
import edu.cornell.cs.nlp.spf.explat.ParameterizedExperiment;
import edu.cornell.cs.nlp.spf.explat.ParameterizedExperiment.Parameters;
import edu.cornell.cs.nlp.spf.explat.resources.IResourceObjectCreator;
import edu.cornell.cs.nlp.spf.explat.resources.usage.ResourceUsage;
import edu.cornell.cs.nlp.spf.mr.lambda.LogicalConstant;
import edu.cornell.cs.nlp.spf.mr.lambda.LogicalExpression;
import edu.cornell.cs.nlp.spf.parser.ccg.rules.BinaryRuleSet;
import edu.cornell.cs.nlp.spf.parser.ccg.rules.IBinaryParseRule;

public class CoordinationRule<MR> extends BinaryRuleSet<MR> {
	
	private CoordinationRule(List<IBinaryParseRule<MR>> rules) {
		super(rules);
	}
	
	public static <MR> CoordinationRule<MR> create(
			ICoordinationServices<MR> services) {
		final List<IBinaryParseRule<MR>> rules = new ArrayList<IBinaryParseRule<MR>>(
				3);
		
		rules.add(new C1Rule<MR>(services));
		rules.add(new C2Rule<MR>(services));
		rules.add(new CXRule<MR>(services));
		
		return new CoordinationRule<MR>(rules);
	}
	
	public static class Creator implements IResourceObjectCreator<BinaryRuleSet<LogicalExpression>> {

		private final String type;

		public Creator() {
			this("rule.coordination");
		}

		public Creator(String type) {
			this.type = type;
		}

		@SuppressWarnings("unchecked")
		@Override
		public CoordinationRule<LogicalExpression> create(Parameters params, IResourceRepository repo) {
			final ICategoryServices<LogicalExpression> categoryServices = (ICategoryServices<LogicalExpression>) repo
					.get(ParameterizedExperiment.CATEGORY_SERVICES_RESOURCE);

			final LogicalConstant baseConjunctionConstant = (LogicalConstant) categoryServices.readSemantics("conj:c");

			final LogicalConstant baseDisjunctionConstant = (LogicalConstant) categoryServices.readSemantics("disj:c");

			final LogicalExpressionCoordinationServices services = new LogicalExpressionCoordinationServices(
					baseConjunctionConstant, baseDisjunctionConstant, categoryServices);

			final List<IBinaryParseRule<LogicalExpression>> rules = new ArrayList<IBinaryParseRule<LogicalExpression>>(
					3);

			rules.add(new C1Rule<LogicalExpression>(services));
//			rules.add(new C2Rule<LogicalExpression>(services));
			rules.add(new CXRule<LogicalExpression>(services));

			return new CoordinationRule<LogicalExpression>(rules);
		}

		@Override
		public String type() {
			return type;
		}

		@Override
		public ResourceUsage usage() {
			return ResourceUsage.builder(type, CoordinationRule.class).build();
		}

	}
}
