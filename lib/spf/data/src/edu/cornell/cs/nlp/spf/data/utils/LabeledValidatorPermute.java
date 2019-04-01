package edu.cornell.cs.nlp.spf.data.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import edu.cornell.cs.nlp.spf.data.ILabeledDataItem;
import edu.cornell.cs.nlp.spf.explat.IResourceRepository;
import edu.cornell.cs.nlp.spf.explat.ParameterizedExperiment.Parameters;
import edu.cornell.cs.nlp.spf.explat.resources.IResourceObjectCreator;
import edu.cornell.cs.nlp.spf.explat.resources.usage.ResourceUsage;
import edu.cornell.cs.nlp.spf.mr.lambda.ExtractExpressions;
import edu.cornell.cs.nlp.spf.mr.lambda.LogicalExpression;
import javafx.util.Pair;

/**
 * Simple validator for labeled data items. Compares the hypothesis label to the
 * gold label.
 * 
 * @param <DI>
 *            Labeled data item to use for validation.
 * @param <LABEL>
 *            Type of label.
 */
public class LabeledValidatorPermute<DI extends ILabeledDataItem<?, LABEL>, LABEL>
		implements IValidator<DI, LABEL> {
	
	private boolean useCache;
	private  int 	portNumber;
	
	private LabeledValidatorPermute(int portNumber){
		this.useCache = portNumber == -1 ? false : true;
		this.portNumber = portNumber;
	}
	
	@Override
	public boolean isValid(DI dataItem, LABEL label) {
		return computeCost(dataItem, label, this.portNumber).equals(0);
	}
	
	
	private final int dist(List<String> predictedBinders, List<String> predictedExpressions,
			List<String> goldBinders, List<String> goldExpressions){
		if (goldExpressions.containsAll(predictedExpressions)){
			return 0;
		} else if (goldExpressions.containsAll(predictedExpressions) && predictedExpressions.containsAll(goldExpressions)){
			return 0;
		}
		return Integer.MAX_VALUE;
	}

	public final boolean cacheLookup(String sentence, Map<String, String> properties, LABEL hypothesis, int portNumber){				
		URL url;
		try {
			// format URL
			url = new URL("http://baffin.csail.mit.edu:" + portNumber + "/check?"
					+ "video=" + URLEncoder.encode(properties.get("video_id"), "UTF-8")
					+ "&parse=" + URLEncoder.encode(((LogicalExpression) hypothesis).toString(), "UTF-8")
					+ "&sentence=" + URLEncoder.encode(sentence, "UTF-8"));
	
			// open connection
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			con.setRequestMethod("GET");
			con.setRequestProperty("Content-Type", "application/json");
			con.setRequestProperty("accept", "application/json");
			
			// get response
			BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
			String inputLine;
			StringBuffer response = new StringBuffer();

			while ((inputLine = in.readLine()) != null) {
				response.append(inputLine);
			}
			in.close();
			
			JSONObject jsonObject = (JSONObject) new JSONParser().parse(response.toString());			
	        if (jsonObject.get("found").toString().equals("true") && jsonObject.get("answer").toString().equals("true")){
	        	return true;
	        }
			return false;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
			System.out.println("error1 " + properties.get("video_id") + " and " + hypothesis.toString() + " " + e.getMessage());
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
			System.out.println("error2 " + properties.get("video_id") + " and " + hypothesis.toString());
		} 
		return false;
	}

	public final Integer computeCost(DI gold, LABEL hypothesis, int portNumber){
		if (useCache && cacheLookup(gold.getSample().toString(), gold.getProperties(), hypothesis, portNumber)){
			return 0;
		}
		
		Pair<List<String>, List<String>> extracted = ExtractExpressions.extract((LogicalExpression) hypothesis);
		List<String> hypothesisBinders = extracted.getKey();
		List<String> hypothesisLiteralExpressions = extracted.getValue();
		
		// ----------- run comparison ---------//
		// return immediately if hypothesis expression is any of permutations
		if (gold.getPermutedBinders().contains(hypothesis)){
			return 0;
		}


		int minCost = Integer.MAX_VALUE;
		for (int p=0; p < gold.getPermutedBinders().size(); p++){
			//System.out.println("iterating");
			Integer cost = dist(hypothesisBinders,
					hypothesisLiteralExpressions,
					gold.getPermutedBinders().get(p),
					gold.getRenamedExpressions().get(p));
			if (cost.equals(0)) {
				return 0;
			}else{
				minCost = Integer.min(minCost, cost);
			}
		}
		return minCost;
	}
	
	
	public static class Creator<DI extends ILabeledDataItem<?, LABEL>, LABEL>
			implements IResourceObjectCreator<LabeledValidatorPermute<DI, LABEL>> {
		
		private String	type;
		
		public Creator() {
			this("validator.labeled.permute");
		}
		
		public Creator(String type) {
			this.type = type;
		}
		
		@Override
		public LabeledValidatorPermute<DI, LABEL> create(Parameters params,
				IResourceRepository repo) {
			
			final int portNumber = params.contains("cache") ? params.getAsInteger("cache") : -1;
			return new LabeledValidatorPermute<DI, LABEL>(portNumber);
		}
		
		@Override
		public String type() {
			return type;
		}
		
		@Override
		public ResourceUsage usage() {
			return new ResourceUsage.Builder(type(), LabeledValidator.class)
					.build();
		}
		
	}
	
}
