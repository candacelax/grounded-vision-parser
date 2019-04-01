package edu.cornell.cs.nlp.spf.mr.lambda;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import edu.cornell.cs.nlp.spf.mr.lambda.visitor.GetConstantsVariableCounts;
import edu.cornell.cs.nlp.spf.mr.lambda.visitor.TupleConstantCount;
import javafx.util.Pair;

public class ExtractExpressions {
	
	public static final List<List<String>> permute(List<String> input) {
    	//https://stackoverflow.com/questions/10503392/java-code-for-permutations-of-a-list-of-numbers		
        List<List<String>> output = new ArrayList<List<String>>();
        if (input.isEmpty()) {
            output.add(new ArrayList<String>());
            return output;
        }
        List<String> list = new ArrayList<String>(input);
        final String head = list.get(0);
        final List<String> rest = list.subList(1, list.size());
        for (List<String> permutations : permute(rest)) {
            List<List<String>> subLists = new ArrayList<List<String>>();
            for (int i = 0; i <= permutations.size(); i++) {
                List<String> subList = new ArrayList<String>();
                subList.addAll(permutations);
                subList.add(i, head);
                subLists.add(subList);
            }
            output.addAll(subLists);
        }
        return output;
    }
	
	/**
	 * --- added by Candace ---
	 * 
	 * Gets list of binders, predicates, and list of literal expressions
	 * Note: this removes types!
	 * 
	 * @param readLogicalExpression
	 * @return
	 */
	public static final Pair<List<String>, List<String>> extract(LogicalExpression readLogicalExpression){
		// ---------------- get predicates, binders -------------------- //
		List<TupleConstantCount> predicates = GetConstantsVariableCounts.of(readLogicalExpression);
		
		// TODO use types for replacement
		List<String> orderedBinders = Arrays.stream(readLogicalExpression.toString().split(" "))
										.filter(x -> x.matches("\\$[0-9]\\)*")).map(x -> x.replaceAll("\\)*", ""))
												.collect(Collectors.toList());
		
		// ------------- create literal expressions as string from binders and predicates -------------- //
		List<String> literalExpressions = new LinkedList<String>();
		for (TupleConstantCount tuple : predicates){
			List<String> thisBinders = orderedBinders.subList(0, tuple.getCount());
			orderedBinders = orderedBinders.subList(tuple.getCount(), orderedBinders.size());
			literalExpressions.add(tuple.getConstant() + " " + String.join(" ", thisBinders));
		}
		
		Collections.sort(literalExpressions);

		return new Pair<List<String>, List<String>>(orderedBinders, literalExpressions);
	}
	
	
	/**
	 * -- added by Candace --
	 * @param binders
	 * @param expressions
	 * @param permutedBinders
	 * @return permuted expressions of logical form
	 */
   public static final List<List<String>> renameExpressions(List<String> binders, List<String> expressions, List<List<String>> permutedBinders){
	List<List<String>> renamedExpressions = new ArrayList<List<String>>();
		
	for (final List<String> permutation : permutedBinders){
		List<String> currentRenamedPermutation = new ArrayList<String>();
		currentRenamedPermutation.addAll(expressions);
	
		// store changes for renaming expressions
		HashMap<String, String> originalToTemp = new HashMap<String, String>();
		HashMap<String, String> tempToFinal = new HashMap<String, String>();
		
		
		// temporary replacement
		for (int i=0; i < permutation.size(); i++){
			// store binders/permutations to be swapped
			final String currentBinder = binders.get(i);
			final String currentPermutation = permutation.get(i);
			originalToTemp.put(String.format("@%d@", i), currentBinder);
			tempToFinal.put(String.format("@%d@", i), currentPermutation);
			
			
			for (int j=0; j < currentRenamedPermutation.size(); j ++){
				final String currentRenamedExpression = currentRenamedPermutation.get(j);
				final Matcher matcher = Pattern.compile(String.format(".*\\%s.*", currentBinder)).matcher(currentRenamedExpression);
				if (matcher.matches()){
					final String cr = currentRenamedExpression.replaceAll( String.format("\\%s", currentBinder),
											String.format("@%d@", i));
					currentRenamedPermutation.set(j, cr);
				}
			}
		}
		
		// final replacement
		for (int i=0; i < permutation.size(); i++){				
			for (int j=0; j < currentRenamedPermutation.size(); j ++){
				final String currentRenamedExpression = currentRenamedPermutation.get(j);
				
				final Matcher matcher = Pattern.compile(String.format(".*%s.*", Integer.toString(i))).matcher(currentRenamedExpression);				
				
				if (matcher.matches()){
					final String cr = currentRenamedExpression.replaceAll( String.format("@%d@", i),
										String.format("\\%s", tempToFinal.get(String.format("@%d@", i))));
					currentRenamedPermutation.set(j, cr);						
				}
			}
		}
		Collections.sort(currentRenamedPermutation);
		renamedExpressions.add(currentRenamedPermutation);
		}
		return renamedExpressions;
	}
   
   public static final List<List<String>> renameExpressionsNoDuplicates(List<String> binders, List<String> expressions, List<List<String>> permutedBinders){
	List<List<String>> renamedExpressions = new ArrayList<List<String>>();
		
	for (final List<String> permutation : permutedBinders){
		List<String> noDuplicates = new ArrayList<>();
		for (String e : expressions){
			if (!noDuplicates.contains(e)){
				noDuplicates.add(e);
			}
		}
		List<String> currentRenamedPermutation = new ArrayList<String>();
		currentRenamedPermutation.addAll(noDuplicates);
	
		// store changes for renaming expressions
		HashMap<String, String> originalToTemp = new HashMap<String, String>();
		HashMap<String, String> tempToFinal = new HashMap<String, String>();
		
		// temporary replacement
		for (int i=0; i < permutation.size(); i++){
			// store binders/permutations to be swapped
			final String currentBinder = binders.get(i);
			final String currentPermutation = permutation.get(i);
			originalToTemp.put(String.format("@%d@", i), currentBinder);
			tempToFinal.put(String.format("@%d@", i), currentPermutation);
			
			
			for (int j=0; j < currentRenamedPermutation.size(); j ++){
				final String currentRenamedExpression = currentRenamedPermutation.get(j);
				final Matcher matcher = Pattern.compile(String.format(".*\\%s.*", currentBinder)).matcher(currentRenamedExpression);
				if (matcher.matches()){
					final String cr = currentRenamedExpression.replaceAll( String.format("\\%s", currentBinder),
											String.format("@%d@", i));
					currentRenamedPermutation.set(j, cr);
				}
			}
		}
		
		// final replacement
		for (int i=0; i < permutation.size(); i++){				
			for (int j=0; j < currentRenamedPermutation.size(); j ++){
				final String currentRenamedExpression = currentRenamedPermutation.get(j);
				
				final Matcher matcher = Pattern.compile(String.format(".*%s.*", Integer.toString(i))).matcher(currentRenamedExpression);				
				
				if (matcher.matches()){
					final String cr = currentRenamedExpression.replaceAll( String.format("@%d@", i),
										String.format("\\%s", tempToFinal.get(String.format("@%d@", i))));
					currentRenamedPermutation.set(j, cr);						
				}
			}
		}
		Collections.sort(currentRenamedPermutation);
		renamedExpressions.add(currentRenamedPermutation);
		}
		return renamedExpressions;
	}
   
   
   public static final int getNumDuplicates(LogicalExpression logExp){
	   List<String> originalExpressions = extract(logExp).getValue();
	   List<String> newExpressions = new ArrayList<String>();
	   
	   int counter = 1; // nonzero so we don't get NaN later
	   for (String s : originalExpressions){
		   if (!newExpressions.contains(s)){
			   newExpressions.add(s);
		   } else{
			   counter++;
		   }
	   }
	   return counter;
		//final List<String> permutation : permutedBinders){
			/*int numDuplicates = 0;
			List<String> noDuplicates = new ArrayList<>();
			for (String e : expressions){
				if (!noDuplicates.contains(e)){
					noDuplicates.add(e);
				} else{
					numDuplicates++;
				}
			}
			return numDuplicates;*/
   }
}
