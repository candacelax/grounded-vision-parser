/*******************************************************************************
 * Copyright (C) 2016 Battushig Myanganbayar, All rights reserved.
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

import edu.cornell.cs.nlp.spf.parser.ccg.model.IDataItemModel;
import edu.cornell.cs.nlp.spf.parser.ccg.model.Model;
import edu.cornell.cs.nlp.spf.data.sentence.Sentence;
import edu.cornell.cs.nlp.spf.mr.lambda.LogicalExpression;
import java.io.File;
import java.io.IOException;
import java.util.Scanner;


/**
 * Main class for GeoQuery experiments.
 * 
 * @author Battushig Myanganbayar
 */
public class VisionParser {

	
	public static void main(String[] args) throws ClassNotFoundException, IOException {
		if (args.length < 1) {
			System.out.println("Missing arguments. Expects a .sp file as a model");
			System.exit(-1);
		}
		
		String modelFile = args[0];
		
		Model<Sentence, LogicalExpression> model = Model.readModel(new File(modelFile));
	
		
		
		Scanner scan = new Scanner(System.in);
		while (true) {	
			// Getting user input sentence 
			if (scan.hasNextLine()) {
				String sentence_raw = scan.nextLine();
				Sentence sentence = new Sentence(sentence_raw);
				IDataItemModel<LogicalExpression> dataItemModel = model
						.createDataItemModel(sentence);
				
				
				System.out.println("================");
			}
			scan.close();
		}
		
	}
}