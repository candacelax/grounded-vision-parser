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
package edu.cornell.cs.nlp.spf.parser.ccg.cky.sloppy;

import edu.cornell.cs.nlp.spf.ccg.categories.Category;
import edu.cornell.cs.nlp.spf.ccg.categories.ICategoryServices;
import edu.cornell.cs.nlp.spf.parser.ccg.cky.CKYBinaryParsingRule;
import edu.cornell.cs.nlp.spf.parser.ccg.cky.chart.Cell;
import edu.cornell.cs.nlp.spf.parser.ccg.rules.ParseRuleResult;
import edu.cornell.cs.nlp.spf.parser.ccg.rules.SentenceSpan;
import edu.cornell.cs.nlp.spf.parser.ccg.rules.RuleName.Direction;

/**
 * This is used to skip words tagged with an empty category:
 * <p>
 * <li>
 * <ul>
 * EMP X => X
 * </ul>
 * </li>
 * </p>
 *
 * @author Yoav Artzi
 */
public class BackwardSkippingRule<MR> extends CKYBinaryParsingRule<MR> {

	private static final long	serialVersionUID	= 6410547264427030992L;

	private final boolean		aggressive;

	private final Category<MR>	emptyCategory;

	public BackwardSkippingRule(ICategoryServices<MR> categoryServices,
			boolean aggressive) {
		super(new SkippingRule<MR>(Direction.BACKWARD, categoryServices));
		this.aggressive = aggressive;
		this.emptyCategory = categoryServices.getEmptyCategory();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!super.equals(obj)) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		@SuppressWarnings("rawtypes")
		final BackwardSkippingRule other = (BackwardSkippingRule) obj;
		if (aggressive != other.aggressive) {
			return false;
		}
		if (emptyCategory == null) {
			if (other.emptyCategory != null) {
				return false;
			}
		} else if (!emptyCategory.equals(other.emptyCategory)) {
			return false;
		}
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + (aggressive ? 1231 : 1237);
		result = prime * result
				+ (emptyCategory == null ? 0 : emptyCategory.hashCode());
		return result;
	}

	@Override
	protected ParseRuleResult<MR> apply(Cell<MR> left, Cell<MR> right,
			SentenceSpan span) {
		// Apply backward skipping only when the left span starts at 0, and, if
		// using aggressive generation of skipping entries, require that the
		// right cell's category is not empty.
		if (left.getStart() == 0
				&& (!aggressive || !right.getCategory().equals(emptyCategory))) {
			return super.apply(left, right, span);
		} else {
			return null;
		}
	}
}
