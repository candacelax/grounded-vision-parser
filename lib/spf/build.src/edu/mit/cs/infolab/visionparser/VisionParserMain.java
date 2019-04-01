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
package edu.mit.cs.infolab.visionparser;

import edu.cornell.cs.nlp.utils.log.ILogger;
import edu.cornell.cs.nlp.utils.log.LoggerFactory;

/**
 * Main class for Vision Parser experiments.
 * 
 * @author Yoav Artzi
 */

public class VisionParserMain {
	public static final ILogger	LOG	= LoggerFactory.create(VisionParserMain.class);
	
	public static void main(String[] args) throws ClassNotFoundException {
		if (args.length < 1) {
			LOG.error("Missing arguments. Expects a .exp file as argument.");
			System.exit(-1);
		}
		VisionParserGenericExperiment.main(args);
	}
}

