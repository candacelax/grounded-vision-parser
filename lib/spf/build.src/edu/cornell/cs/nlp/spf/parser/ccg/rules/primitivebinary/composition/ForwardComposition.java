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
package edu.cornell.cs.nlp.spf.parser.ccg.rules.primitivebinary.composition;

import edu.cornell.cs.nlp.spf.ccg.categories.Category;
import edu.cornell.cs.nlp.spf.ccg.categories.ICategoryServices;
import edu.cornell.cs.nlp.spf.explat.IResourceRepository;
import edu.cornell.cs.nlp.spf.explat.ParameterizedExperiment;
import edu.cornell.cs.nlp.spf.explat.ParameterizedExperiment.Parameters;
import edu.cornell.cs.nlp.spf.explat.resources.IResourceObjectCreator;
import edu.cornell.cs.nlp.spf.explat.resources.usage.ResourceUsage;
import edu.cornell.cs.nlp.spf.parser.ccg.rules.ParseRuleResult;
import edu.cornell.cs.nlp.spf.parser.ccg.rules.SentenceSpan;
import edu.cornell.cs.nlp.spf.parser.ccg.rules.RuleName.Direction;

/**
 * A rule for logical composition. Forward composition rule:
 * <ul>
 * <li>X/Y Y/Z => X/Z</li>
 * </ul>
 *
 * @author Yoav Artzi
 */
public class ForwardComposition<MR> extends AbstractComposition<MR> {

	private static final long	serialVersionUID	= -120889057244747295L;

	public ForwardComposition(ICategoryServices<MR> categoryServices,
			int order, boolean cross) {
		super(RULE_LABEL, Direction.FORWARD, order, categoryServices, cross);
	}

	@Override
	public ParseRuleResult<MR> apply(Category<MR> left, Category<MR> right,
			SentenceSpan span) {
		return doComposition(left, right, false);
	}

	public static class Creator<MR> implements
			IResourceObjectCreator<ForwardComposition<MR>> {

		private String	type;

		public Creator() {
			this("rule.composition.forward");
		}

		public Creator(String type) {
			this.type = type;
		}

		@SuppressWarnings("unchecked")
		@Override
		public ForwardComposition<MR> create(Parameters params,
				IResourceRepository repo) {
			return new ForwardComposition<MR>(
					(ICategoryServices<MR>) repo
							.get(ParameterizedExperiment.CATEGORY_SERVICES_RESOURCE),
					params.getAsInteger("order", 1), params.getAsBoolean(
							"cross", false));
		}

		@Override
		public String type() {
			return type;
		}

		@Override
		public ResourceUsage usage() {
			return new ResourceUsage.Builder(type, ForwardComposition.class)
					.addParam("cross", Boolean.class,
							"Crossing composition (default: false)")
					.addParam("order", Integer.class,
							"Composition order (for English, around 3 should be the max, default: 1)")
					.build();
		}

	}

}
