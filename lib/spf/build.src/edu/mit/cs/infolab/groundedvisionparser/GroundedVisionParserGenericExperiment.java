package edu.mit.cs.infolab.groundedvisionparser;

import java.io.File;
import java.io.IOException;

public class GroundedVisionParserGenericExperiment {
	
	public static void main(String[] args) throws ClassNotFoundException {
		run(args[0]);
	}
	
	public static void run(String filename) throws ClassNotFoundException {
		try {
			new GroundedVisionParserExp(new File(filename)).start();
		} catch (final IOException e) {
			e.printStackTrace();
		}
	}
	
}
