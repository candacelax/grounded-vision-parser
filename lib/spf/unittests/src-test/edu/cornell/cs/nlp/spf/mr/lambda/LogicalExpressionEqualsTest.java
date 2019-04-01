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
package edu.cornell.cs.nlp.spf.mr.lambda;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.regex.Matcher;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import edu.cornell.cs.nlp.spf.TestServices;
import edu.cornell.cs.nlp.spf.data.sentence.Sentence;
import edu.cornell.cs.nlp.spf.data.singlesentence.SingleSentence;
import edu.cornell.cs.nlp.spf.mr.lambda.Lambda;
import edu.cornell.cs.nlp.spf.mr.lambda.Literal;
import edu.cornell.cs.nlp.spf.mr.lambda.LogicLanguageServices;
import edu.cornell.cs.nlp.spf.mr.lambda.LogicalConstant;
import edu.cornell.cs.nlp.spf.mr.lambda.LogicalExpression;
import edu.cornell.cs.nlp.spf.mr.lambda.Variable;
import edu.cornell.cs.nlp.spf.mr.lambda.visitor.GetAllLiterals;
import edu.cornell.cs.nlp.spf.mr.lambda.visitor.GetAllPredicates;
import edu.cornell.cs.nlp.spf.mr.lambda.visitor.GetConstCounts;
import edu.cornell.cs.nlp.utils.collections.ArrayUtils;
import edu.cornell.cs.nlp.utils.counter.Counter;
import javafx.util.Pair;

public class LogicalExpressionEqualsTest {

	public LogicalExpressionEqualsTest() {
		TestServices.init();
	}
	
	
	@ Test
	public void checkingOurMetric(){
		String a = "(lambda $0:e (person:<a,t> $0) (person:<a,t> $0)";
		final SingleSentence s1 = new SingleSentence(new Sentence("The person put down the red apple on the table"),
													LogicalExpression.read("(lambda $0:e (lambda $1:e (lambda $2:e (and:<t*,t> (person:<e,t> $0)"
															+ "(put_down:<e,<e,t>> $0 $1) (red:<e,t> $1)"
															+ "(red:<e,t> $1) (apple:<e,t> $1)"
															+ "(on:<e,<e,t>> $1 $2) (table:<e,t> $2)))))"));
		
		
		
		final LogicalExpression e2 = LogicalExpression
				.read("(lambda $0:e (lambda $1:e (lambda $2:e (lambda $3:<e,<e,t>> (and:<t*,t> (table:<e,t> $0) (red:<e,t> $1) "
						+ "(apple:<e,t> $1) (apple:<e,t> $1) (person:<e,t> $2) (put_down:<e,<e,t>> $2 $1) (on:<e,<e,t>> $1 $0) ($3 $0 $1))))))");
		
		
		System.out.println(GetConstCounts.of(e2).keySet());
		
		List<Integer> b = new ArrayList<Integer>();
		b.add(1);
		b.add(2);
		b.add(1);
		b.add(5);

		List<Integer> c = b.stream().filter(p -> p > 1).collect(Collectors.toList());
		for (Integer i : c){
			System.out.println("c " + i);
		}
		System.exit(0);
	
		
		final String lit = GetAllLiterals.of(e2).get(0).toString();
		int n = 0;
		Map<LogicalConstant, Counter> counts  = GetConstCounts.of(e2);
		for (LogicalConstant key : counts.keySet()){
			if (counts.get(key).value() == 1){
				continue;
			}
			
			List<String> substrings = new ArrayList<String>();
			final Matcher m = Pattern.compile("\\("+ key.toString() + " [(\\$[0-9])(\\$[0-9]:[(a-z)(\\<.*\\>)](\\$[0-9] )]+\\)").matcher(lit);
			while (m.find()){
				if (substrings.contains(lit.substring(m.start(), m.end()))){
					n++;
				} else{
					substrings.add(lit.substring(m.start(), m.end()));
				}
			}
		}
		System.out.println("n " + n);
		System.exit(0);
	
		
		//final String lit = GetAllLiterals.of(e2).get(0).toString();
		final List<Matcher> matchers =
				GetAllPredicates.of(e2).stream()
							.map(p ->Pattern.compile("\\("+ p.toString() + " [(\\$[0-9])(\\$[0-9]:[(a-z)(\\<.*\\>)](\\$[0-9] )]+\\)")
										.matcher(lit)).collect(Collectors.toList());
		
		int numDuplicates = 0;
		for (final Matcher m : matchers){
			List<String> substrings = new ArrayList<String>();
			while (m.find()){
				if (substrings.contains(lit.substring(m.start(), m.end()))){
					numDuplicates++;
				} else{
					substrings.add(lit.substring(m.start(), m.end()));
				}
			}
		}
		System.out.println("num " + numDuplicates);
		System.exit(0);
		
		
		final LogicalExpression e3 = LogicalExpression.read("(lambda $0:<e,<e,t>> (lambda $1:e (lambda $2:e ($0 $1 $2))))");
		System.out.println(GetAllLiterals.of(e2).size() + " "  + GetAllPredicates.of(e2));
		List<String> preds1 = new ArrayList<String>();
		List<String> preds2 = new ArrayList<String>();
		
		for (Literal l : GetAllLiterals.of(e2)){
			preds1.add(l.getPredicate().toString());
			System.out.println("l " + l.getPredicate());
		}
		
		for (LogicalConstant l : GetAllPredicates.of(e2)){
			preds2.add(l.toString());
			System.out.println("p " + l);
		}
		
		System.out.println(preds1.size()-preds2.size());
		
		System.out.println("diff " + (float) GetAllPredicates.of(e2).size() / (float) GetAllLiterals.of(e2).size());
		
		System.exit(0);
		//final SingleSentence s2 = new SingleSentence(new Sentence("count the states which have elevations lower than what alabama has"),
			//				LogicalExpression.read("(count:<<e,t>,i> (lambda $0:e (and:<t*,t> (state:<s,t> $0) (exists:<<e,t>,t> (lambda $1:e (and:<t*,t> (place:<p,t> $1) (loc:<lo,<lo,t>> $1 $0) (<:<i,<i,t>> (elevation:<lo,i> $1) (elevation:<lo,i> (argmin:<<e,t>,<<e,i>,e>> (lambda $2:e (and:<t*,t> (place:<p,t> $2) (loc:<lo,<lo,t>> $2 alabama:s))) (lambda $3:e (elevation:<lo,i> $3)))))))))))"));
		
		Pair<List<String>, List<String>> extracted = ExtractExpressions.extract(e3);
		List<String> expressions = extracted.getValue();
		for (String s : expressions){
			System.out.println("s " + s);
		}
		System.exit(0);
		
		
	}
	
	@Ignore
	public void emptySemantics(){
		final LogicalExpression e1 = LogicalExpression
				.read("(lambda $0:<e,t> (lambda $1:e (lambda $2:e (and:<t*,t> ($0 $1) ($0 $2)))))");
		final LogicalExpression e2 = LogicalExpression
				.read("(lambda $0:e (lambda $1:e (lambda $2:e (and:<t*,t> (table:<e,t> $2) (red:<e,t> $0) (apple:<e,t> $0) (person:<e,t> $1) (put_down:<e,<e,t>> $1 $0) (on:<e,<e,t>> $0 $2)))))");
		
	
		String[] split_exp = Pattern.compile("\\)|\\(").matcher(e2.toString()
							.replaceAll(":", " :")).replaceAll("").split(" ");

		for (String s : split_exp){
			if (!s.matches("lambda|and|\\$[0-9]+|:.+")){
				System.out.println("1 " + s);
			} else{
				System.out.println("2 " + s);
			}
		}
		System.out.println("true");
	}
	
	@Ignore
	public void test() {
		final LogicalExpression e1 = LogicalExpression
				.read("(lambda $0:e (boo:<e,t> $0))");
		final LogicalExpression e2 = LogicalExpression
				.read("(lambda $0:e (boo:<e,t> $0))");
		Assert.assertTrue(e1.equals(e2));
	}

	@Test
	public void test2() {
		final LogicalExpression e1 = LogicalExpression.read("(boo:<e,t> $0:e)");
		final LogicalExpression e2 = LogicalExpression.read("(boo:<e,t> $0:e)");
		Assert.assertFalse(e1.equals(e2));
	}

	@Test
	public void test3() {
		final LogicalExpression e1 = LogicalExpression
				.read("(lambda $0:e (boo:<e,t> $0))");
		final LogicalExpression e2 = LogicalExpression
				.read("(lambda $1:e (boo:<e,t> $0:e))");
		Assert.assertFalse(e1.equals(e2));
	}

	@Test
	public void test4() {
		final LogicalExpression e1 = LogicalExpression
				.read("(lambda $0:e (and:<t*,t> (boo:<e,t> $0) (foo:<e,t> $1:e)))");
		final LogicalExpression e2 = LogicalExpression
				.read("(lambda $0:e (and:<t*,t> (boo:<e,t> $0) (foo:<e,t> $0)))");
		Assert.assertFalse(e1.equals(e2));
	}

	@Test
	public void test5() {
		final LogicalExpression e1 = LogicalExpression
				.read("(lambda $0:e (and:<t*,t> (boo:<e,t> $0) (foo:<e,t> $0)))");
		final LogicalExpression e2 = LogicalExpression
				.read("(lambda $0:e (and:<t*,t> (boo:<e,t> $0) (foo:<e,t> $1:e)))");
		Assert.assertFalse(e1.equals(e2));
	}

	@Test
	public void test6() {
		final LogicalExpression e1 = LogicalExpression
				.read("(lambda $0:e (and:<t*,t> (boo:<e,t> $1:e) (foo:<e,t> $1)))");
		final LogicalExpression e2 = LogicalExpression
				.read("(lambda $0:e (and:<t*,t> (boo:<e,t> $1:e) (foo:<e,t> $0)))");
		Assert.assertFalse(e1.equals(e2));
	}

	@Test
	public void test7() {
		final Variable variable = new Variable(LogicLanguageServices
				.getTypeRepository().getEntityType());
		final LogicalExpression e1 = new Lambda(variable, new Literal(
				LogicalConstant.read("boo:<e,<<e,t>,t>>"), ArrayUtils.create(
						variable,
						new Lambda(variable,
								new Literal(LogicalConstant.read("goo:<e,t>"),
										ArrayUtils.create(variable))))));
		final LogicalExpression e2 = LogicalExpression
				.read("(lambda $0:e (boo:<e,<<e,t>,t>> $0 (lambda $1:e (goo:<e,t> $1))))");
		Assert.assertEquals(e2, e1);
	}

	@Test
	public void test8() {
		final LogicalExpression e1 = LogicalExpression
				.read("(lambda $0:e (exists:<<e,t>,t> (lambda $1:e (and:<t*,t> (place:<p,t> $1) (exists:<<e,t>,t> (lambda $2:e (and:<t*,t> (state:<s,t> $2) (loc:<lo,<lo,t>> $2 $2)))) (equals:<e,<e,t>> $0 $1)))))");
		final LogicalExpression e2 = LogicalExpression
				.read("(lambda $0:e (exists:<<e,t>,t> (lambda $1:e (and:<t*,t> (place:<p,t> $1) (exists:<<e,t>,t> (lambda $2:e (and:<t*,t> (state:<s,t> $2) (loc:<lo,<lo,t>> $2 $1)))) (equals:<e,<e,t>> $0 $1)))))");
		Assert.assertFalse(e1.equals(e2));
	}

	@Test
	public void test9() {
		final Variable variable = new Variable(LogicLanguageServices
				.getTypeRepository().getEntityType());
		final LogicalExpression e1 = new Lambda(variable, new Lambda(variable,
				new Literal(LogicalConstant.read("pred:<e,<e,t>>"),
						ArrayUtils.create(variable, variable))));
		final LogicalExpression e2 = LogicalExpression
				.read("(lambda $0:e (lambda $1:e (pred:<e,<e,t>> $0 $1)))");
		Assert.assertNotEquals(e2, e1);
		Assert.assertNotEquals(e1, e2);
	}

}
