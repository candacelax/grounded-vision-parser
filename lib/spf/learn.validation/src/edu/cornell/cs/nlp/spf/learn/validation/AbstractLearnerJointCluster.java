package edu.cornell.cs.nlp.spf.learn.validation;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import edu.cornell.cs.nlp.spf.ccg.categories.ICategoryServices;
import edu.cornell.cs.nlp.spf.data.IDataItem;
import edu.cornell.cs.nlp.spf.data.ILabeledDataItem;
import edu.cornell.cs.nlp.spf.data.collection.IDataCollection;
import edu.cornell.cs.nlp.spf.genlex.ccg.ILexiconGenerator;
import edu.cornell.cs.nlp.spf.parser.IDerivation;
import edu.cornell.cs.nlp.spf.parser.IOutputLogger;
import edu.cornell.cs.nlp.spf.parser.IParserOutput;
import edu.cornell.cs.nlp.spf.parser.ccg.model.IDataItemModel;
import edu.cornell.cs.nlp.spf.parser.ccg.model.IModelImmutable;
import edu.cornell.cs.nlp.spf.parser.ccg.model.Model;
import edu.cornell.cs.nlp.spf.parser.filter.IParsingFilterFactory;
import edu.cornell.cs.nlp.utils.filter.IFilter;
import edu.cornell.cs.nlp.utils.log.ILogger;
import edu.cornell.cs.nlp.utils.log.LoggerFactory;
import edu.cornell.cs.nlp.utils.system.MemoryReport;

public abstract class AbstractLearnerJointCluster<SAMPLE extends IDataItem<?>, DI extends ILabeledDataItem<SAMPLE, ?>, PO extends IParserOutput<MR>, MR>
		//implements ILearner<SAMPLE, DI, Model<SAMPLE, MR>>
		extends AbstractLearner<SAMPLE, DI, PO, MR>{

	public static final ILogger												LOG					= LoggerFactory
			.create(AbstractLearner.class);
	
	/**-------------------- added by Candace ----------------------------
	 * used for clustering sentences as either grounded or ungrounded
	 */
	protected final List<HashMap<Integer, Boolean>>							clusters;
	protected final boolean													skipToTest;
	
	
	protected AbstractLearnerJointCluster(int numIterations, IDataCollection<DI> trainingData,
			Map<DI, MR> trainingDataDebug, int lexiconGenerationBeamSize, IOutputLogger<MR> parserOutputLogger,
			boolean conflateGenlexAndPrunedParses, boolean errorDriven, ICategoryServices<MR> categoryServices,
			ILexiconGenerator<DI, MR, IModelImmutable<SAMPLE, MR>> genlex, IFilter<DI> processingFilter,
			IParsingFilterFactory<DI, MR> parsingFilterFactory, String parentDirectory,
			double trainingNoise, boolean partialMatch, boolean runModelBeforeTraining,
			List<HashMap<Integer, Boolean>> clusters, boolean skipToTest) {
		super(numIterations, trainingData, trainingDataDebug, lexiconGenerationBeamSize, parserOutputLogger,
				conflateGenlexAndPrunedParses, errorDriven, categoryServices, genlex, processingFilter, parsingFilterFactory,
				parentDirectory, trainingNoise, runModelBeforeTraining, skipToTest);
		this.clusters = clusters;
		this.skipToTest = skipToTest;
	}
	
	
	@Override
	public void train(Model<SAMPLE, MR> model) {
		System.out.println("in abstract 2");
		super.train(model);
		
		// Init GENLEX.
		LOG.info("Initializing GENLEX ...");
		genlex.init(model);
		
		// Epochs
		for (int epochNumber = 0; epochNumber < epochs; ++epochNumber) {
			// Training epoch, iterate over all training samples
			LOG.info("=========================");
			LOG.info("Training epoch %d joint", epochNumber);
			LOG.info("=========================");
			System.out.println("=========================");
			System.out.println("Training epoch " + epochNumber);
			System.out.println("=========================");
			
			int itemCounter = -1;
			final HashSet<Integer> noisyExamples = generateNoisyExamples();
			
			for (final DI dataItem : trainingData) {
				// print update of where we are in training data
				if ( (itemCounter+1) % 50 == 0 ){ // adding 1 because we start from -1
					System.out.println("on example " + (itemCounter+1) + " of " + trainingData.size());
				}
				
				
				// Process a single training sample

				// Record start time
				final long startTime = System.currentTimeMillis();

				// Log sample header
				LOG.info("%d : ================== [%d]", ++itemCounter,
						epochNumber);
				LOG.info("Sample type: %s",
						dataItem.getClass().getSimpleName());
				LOG.info("%s", dataItem);

				// Skip sample, if over the length limit
				if (!processingFilter.test(dataItem)) {
					LOG.info(
							"Skipped training sample, due to processing filter");
					continue;
				}

				stats.count("Processed", epochNumber);

				try {
					// Data item model
					final IDataItemModel<MR> dataItemModel = model.createDataItemModel(dataItem.getSample());
					
					// ///////////////////////////
					// Step I: Parse with current model. If we get a valid
					// parse, update parameters.
					// ///////////////////////////

					// Parse with current model and record some statistics
					final PO parserOutput = parse(dataItem, dataItemModel);
					stats.mean("Model parse",
							parserOutput.getParsingTime() / 1000.0, "sec");
					parserOutputLogger.log(parserOutput, dataItemModel, String
							.format("train-%d-%d", epochNumber, itemCounter));

					final List<? extends IDerivation<MR>> modelParses = parserOutput
							.getAllDerivations();
					

					LOG.info("Model parsing time: %.4fsec", parserOutput.getParsingTime() / 1000.0);
					LOG.info("Output is %s", parserOutput.isExact() ? "exact" : "approximate");
					LOG.info("Created %d model parses for training sample:", modelParses.size());
					for (final IDerivation<MR> parse : modelParses) {
						try {
							logParse(dataItem, parse,
									validate(dataItem, parse.getSemantics()), true,
									dataItemModel);
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
					
					// Create a list of all valid parses
					final List<? extends IDerivation<MR>> validParses = getValidParses(parserOutput, dataItem);
					
					/*System.out.println("model: " + modelParses.size() + " valid: " + validParses.size());
					
					for (final IDerivation<MR> parse : modelParses){
						System.out.println("model " + parse
									+ "\n\t" + dataItem.getLabel());
						
					}
					for (final IDerivation<MR> parse : validParses){
						System.out.println("valid " + parse);
					}
					*/
					
					if (validParses.size() > 0){ // at least one valid parse
						if (noisyExamples.contains(itemCounter)){ // example is noise so skip parameter update
							if (rng.nextInt(trainingData.size()) == 0){
								stats.addNewIncorrectParse(epochNumber);
							} else {
								stats.addNewFailureParse(epochNumber);
							}
						} else { // example is not noise, so do parameter update
							stats.addNewCorrectParse(epochNumber);
							if (errorDriven){
								
								// call parameter update procedure and continue
								parameterUpdate(dataItem, parserOutput, parserOutput,
										model, itemCounter, epochNumber, false);
								continue;
							}
						}
					} else if (modelParses.size() > 0){
						if (noisyExamples.contains(itemCounter)){ // example is noise, so pretend parses are valid and do parameter update
							stats.addNewCorrectParse(epochNumber);
							
							if (errorDriven){
								// call parameter update procedure and continue
								parameterUpdate(dataItem, parserOutput, parserOutput,
										model, itemCounter, epochNumber, true);
								continue;
							}
						} else { // example is not noise so do parameter update
							stats.addNewIncorrectParse(epochNumber);
						}
					} else { // no parses found
						if (noisyExamples.contains(itemCounter)){ // example is noise, so pretend parse exists and update
							stats.addNewCorrectParse(epochNumber);
							
							if (errorDriven){
								// call parameter update procedure and continue
								parameterUpdate(dataItem, parserOutput, parserOutput,
										model, itemCounter, epochNumber, true);
								continue;
							}
						}else { // example is not noise
							stats.addNewFailureParse(epochNumber);
						}
						
					}

					// ///////////////////////////
					// Step II: Generate new lexical entries, prune and update
					// the model. Keep the parser output for Step III.
					// ///////////////////////////

					if (genlex == null) {
						// Skip the example if not doing lexicon learning
						continue;
					}

					final PO generationParserOutput = lexicalInduction(dataItem,
							itemCounter, dataItemModel, model, epochNumber);	

					// ///////////////////////////
					// Step III: Update parameters
					// ///////////////////////////
					
					if (conflateGenlexAndPrunedParses && generationParserOutput != null) {
							parameterUpdate(dataItem, parserOutput,
									generationParserOutput, model, itemCounter,
									epochNumber, noisyExamples.contains(itemCounter));
					} else {
						final PO prunedParserOutput = parse(dataItem,
								parsingFilterFactory.create(dataItem),
								dataItemModel);
						
						LOG.info("Conditioned parsing time: %.4fsec",
								prunedParserOutput.getParsingTime() / 1000.0);
						parserOutputLogger.log(prunedParserOutput,
								dataItemModel,
								String.format("train-%d-%d-conditioned",
										epochNumber, itemCounter));
						
						parameterUpdate(dataItem, parserOutput,
								prunedParserOutput, model, itemCounter,
								epochNumber, noisyExamples.contains(itemCounter));
					}

					
					LOG.info("current avg precision: "+ stats.getAveragePrecision());
					LOG.info("current avg recall: "+ stats.getAverageRecall());
					LOG.info("current avg f1: "+ stats.getAverageF1());
					
				} finally {
					// Record statistics.
					stats.mean("Sample processing",
							(System.currentTimeMillis() - startTime) / 1000.0,
							"sec");
					LOG.info("Total sample handling time: %.4fsec",
							(System.currentTimeMillis() - startTime) / 1000.0);
				}
			}
			
			// Output epoch statistics
			LOG.info("System memory: %s", MemoryReport.generate());
			LOG.info("Epoch stats:");
			LOG.info(stats);
			
			/* 
			 * -------------------- added by Candace ----------------------------
			 * compute stats for epoch
			 */
			stats.getPrecision(epochNumber);
			stats.getRecall(epochNumber);
			stats.getF1(epochNumber);
		}

		// -------------------- added by Candace ----------------------------//
		System.out.println("stats " + stats.getFailures(0));
		recordFinalStats(epochs);
		writeFinalLexiconToFile(model);
	}
}
