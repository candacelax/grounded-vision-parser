package edu.mit.cs.infolab.data.groundedsentence.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import edu.cornell.cs.nlp.spf.data.IDataItem;
import edu.cornell.cs.nlp.spf.data.utils.IValidator;
import edu.cornell.cs.nlp.spf.data.utils.LabeledValidator;
import edu.cornell.cs.nlp.spf.explat.IResourceRepository;
import edu.cornell.cs.nlp.spf.explat.ParameterizedExperiment.Parameters;
import edu.cornell.cs.nlp.spf.explat.resources.IResourceObjectCreator;
import edu.cornell.cs.nlp.spf.explat.resources.usage.ResourceUsage;
import edu.cornell.cs.nlp.spf.mr.lambda.LogicalExpression;

/**
 * Uses sentence tracker for validation
 * @author ccross
 *
 */

public class SentenceTrackerValidator<DI extends IDataItem<?>, LABEL> 
	implements IValidator<DI, LABEL> {
	// TODO remove
	final static Random rand = new Random(); 
	static List<String> portNumbers;
	static private Map<Long, String> mapThreadToPort = Collections.synchronizedMap(new HashMap<Long, String>());
	
	private SentenceTrackerValidator(List<String> portNumbers){
		SentenceTrackerValidator.portNumbers = portNumbers;
	}
	
	public static List<String> getPortList(){
		return SentenceTrackerValidator.portNumbers;
	}
	
	public static void addMapping(Long thread, String port){
		mapThreadToPort.put(thread, port);
	}
	
	public static void clearMap(){
		mapThreadToPort.clear();
	}
	
	@Override
	public boolean isValid(DI dataItem, LABEL label) {
		// TODO update paramUpdate so it can all be in thread
		String portNumber;
		if (SentenceTrackerValidator.mapThreadToPort.keySet().contains(Thread.currentThread().getId())){
			portNumber = SentenceTrackerValidator.mapThreadToPort.get(Thread.currentThread().getId());
		} else{
			portNumber = portNumbers.get(rand.nextInt(portNumbers.size()));
		}
		// TODO clean up
		if (portNumber == null){
			portNumber = portNumbers.get(rand.nextInt(portNumbers.size()));
		}
		boolean isValid = cacheLookup(dataItem.getSample().toString(),
										dataItem.getProperties(), label,
										portNumber);
		return isValid;
	}
	
	public final boolean cacheLookup(String sentence, Map<String, String> properties, LABEL hypothesis, String portNumber){
		URL url;
		
		HttpURLConnection con = null;
		try {			
			// format URL
			url = new URL("http://localhost:" + portNumber 
								+ "/check?"
								+ "video=" + URLEncoder.encode(properties.get("video_id").replaceAll("mts", "MTS"), "UTF-8")
								+ "&parse=" + URLEncoder.encode(((LogicalExpression) hypothesis).toString(), "UTF-8")
								+ "&sentence=" + URLEncoder.encode(sentence, "UTF-8"));
			
			// open connection
			con = (HttpURLConnection) url.openConnection();
			con.setRequestMethod("GET");
			con.setRequestProperty("Content-Type", "application/json");
			con.setRequestProperty("accept", "application/json");
			con.setRequestProperty("Connection", "keep-alive");
			
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
	        	con.disconnect();
	        	return true;
	        }
	        con.disconnect();
			return false;
		} catch (IOException e) {
			if (e.toString().matches(".*Server returned HTTP response code: 400 for URL.*")){
				// parse is very random
				con.disconnect();
				return false;
			} else if (e.toString().matches(".*Server returned HTTP response code: 500 for URL.*")){
				// video is likely missing
				//System.out.println("video missing " + properties.get("video_id"));
				con.disconnect();
				return false;
			}else if (e.toString().matches(".*Missing noun constraint.*")){ // TODO is this an actual error message?
				return false;
			}else{
				// TODO
				System.out.println("issue with port? " + portNumber);
				//e.printStackTrace();
				//System.exit(0);
			}
		} catch (ParseException e) {
			// TODO
			System.out.println("error2 " + properties.get("video_id") + " and " + hypothesis.toString());
			//e.printStackTrace();
		} 
		finally {
			if (con != null){
				con.disconnect();
			}
		}
		return false;
	}	
	
	
	public static class Creator<DI extends IDataItem<?>, LABEL>
		implements IResourceObjectCreator<SentenceTrackerValidator<DI, LABEL>> {

	private String	type;
	
	public Creator() {
		this("validator.sentence.tracker");
	}
	
	public Creator(String type) {
		this.type = type;
	}
	
	@Override
	public SentenceTrackerValidator<DI, LABEL> create(Parameters params,
			IResourceRepository repo) {		
		return new SentenceTrackerValidator<DI, LABEL>(Arrays.asList(params.get("ports").split("_")));
	}
	
	@Override
	public String type() {
		return type;
	}
	
	@Override
	public ResourceUsage usage() {
		return new ResourceUsage.Builder(type(), LabeledValidator.class)
				.addParam("port", Integer.class,
						"Port number on this machine for the sentence tracker.")
				.setDescription("Determines whether a hypothesis logical form describes a video.")
				.build();
		}
	}

}
