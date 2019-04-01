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
package edu.cornell.cs.nlp.spf.parser.ccg.rules.coordination.simple;

import edu.cornell.cs.nlp.spf.ccg.categories.Category;
import edu.cornell.cs.nlp.spf.ccg.categories.syntax.ComplexSyntax;
import edu.cornell.cs.nlp.spf.ccg.categories.syntax.Slash;
import edu.cornell.cs.nlp.spf.ccg.categories.syntax.Syntax;
import edu.cornell.cs.nlp.spf.parser.ccg.rules.IBinaryParseRule;
import edu.cornell.cs.nlp.spf.parser.ccg.rules.ParseRuleResult;
import edu.cornell.cs.nlp.spf.parser.ccg.rules.RuleName;
import edu.cornell.cs.nlp.spf.parser.ccg.rules.SentenceSpan;
import edu.cornell.cs.nlp.spf.parser.ccg.rules.coordination.ICoordinationServices;
import edu.cornell.cs.nlp.spf.parser.ccg.rules.RuleName.Direction;

class C1RuleSimple<MR> implements IBinaryParseRule<MR> {

	private static final RuleName			RULE_NAME			= RuleName
																		.create("c1Simple",
																				Direction.FORWARD);
	private static final long				serialVersionUID	= -3172784040138354513L;
	private final ICoordinationServices<MR>	services;

	public C1RuleSimple(ICoordinationServices<MR> services) {
		this.services = services;
	}

	@Override
	public ParseRuleResult<MR> apply(Category<MR> left, Category<MR> right,
			SentenceSpan span) {
		if (left.getSyntax().equals(Syntax.C) && left.getSemantics() != null
				&& right.getSemantics() != null) {
			// Simple coordination is the case of an argument of type 't' that
			// is coordinated through simple conjuncton/disjunction with others
			final MR simpleCoordination = services.createSimpleCoordination(
					right.getSemantics(), left.getSemantics());

			if (simpleCoordination == null) {
				// Case simple coordination failed, try the more complex case
				final MR semantics = services.createPartialCoordination(
						right.getSemantics(), left.getSemantics());
				if (semantics != null) {
					return new ParseRuleResult<MR>(RULE_NAME, Category.create(
							new ComplexSyntax(new ComplexSyntax(left
									.getSyntax(), right.getSyntax(),
									Slash.VERTICAL), right.getSyntax(),
									Slash.BACKWARD), semantics));
				}
			} else {
				return new ParseRuleResult<MR>(RULE_NAME, Category.create(
						new ComplexSyntax(right.getSyntax(), right.getSyntax(),
								Slash.BACKWARD), simpleCoordination));
			}
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
		@SuppressWarnings("rawtypes")
		final C1RuleSimple other = (C1RuleSimple) obj;
		if (services == null) {
			if (other.services != null) {
				return false;
			}
		} else if (!services.equals(other.services)) {
			return false;
		}
		return true;
	}

	@Override
	public RuleName getName() {
		return RULE_NAME;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ (RULE_NAME == null ? 0 : RULE_NAME.hashCode());
		result = prime * result + (services == null ? 0 : services.hashCode());
		return result;
	}

}