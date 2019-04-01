package edu.mit.cs.infolab.groundedvisionparser;

import edu.cornell.cs.nlp.utils.log.ILogger;
import edu.cornell.cs.nlp.utils.log.LoggerFactory;

/**
 * Main class for Vision Parser experiments with weak supervision.
 * 
 * @author Candace Ross
 */

public class GroundedVisionParserMain {
	public static final ILogger	LOG	= LoggerFactory.create(GroundedVisionParserMain.class);
	
	public static void main(String[] args) throws ClassNotFoundException {
		if (args.length < 1) {
			LOG.error("Missing arguments. Expects a .exp file as argument.");
			System.exit(-1);
		}
		GroundedVisionParserGenericExperiment.main(args);
	}
}

