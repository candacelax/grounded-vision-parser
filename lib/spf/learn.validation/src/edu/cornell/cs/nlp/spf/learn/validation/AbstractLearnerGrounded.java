package edu.cornell.cs.nlp.spf.learn.validation;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.function.Predicate;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;

import edu.cornell.cs.nlp.spf.ccg.categories.ICategoryServices;
import edu.cornell.cs.nlp.spf.ccg.lexicon.ILexiconImmutable;
import edu.cornell.cs.nlp.spf.ccg.lexicon.LexicalEntry;
import edu.cornell.cs.nlp.spf.data.IDataItem;
import edu.cornell.cs.nlp.spf.data.collection.IDataCollection;
import edu.mit.cs.infolab.data.groundedsentence.utils.SentenceTrackerValidator;
import javafx.util.Pair;
import edu.cornell.cs.nlp.spf.genlex.ccg.ILexiconGenerator;
import edu.cornell.cs.nlp.spf.genlex.ccg.LexiconGenerationServices;
import edu.cornell.cs.nlp.spf.learn.ILearnerGrounded;
import edu.cornell.cs.nlp.spf.learn.LearningStatsGrounded;
import edu.cornell.cs.nlp.spf.learn.validation.perceptron.ValidationPerceptron;
import edu.cornell.cs.nlp.spf.learn.validation.stocgrad.ValidationStocGrad;
import edu.cornell.cs.nlp.spf.parser.IDerivation;
import edu.cornell.cs.nlp.spf.parser.IOutputLogger;
import edu.cornell.cs.nlp.spf.parser.IParserOutput;
import edu.cornell.cs.nlp.spf.parser.ParsingOp;
import edu.cornell.cs.nlp.spf.parser.ccg.IWeightedParseStep;
import edu.cornell.cs.nlp.spf.parser.ccg.model.IDataItemModel;
import edu.cornell.cs.nlp.spf.parser.ccg.model.IModelImmutable;
import edu.cornell.cs.nlp.spf.parser.ccg.model.Model;
import edu.cornell.cs.nlp.spf.parser.filter.IParsingFilterFactory;
import edu.cornell.cs.nlp.utils.filter.IFilter;
import edu.cornell.cs.nlp.utils.log.ILogger;
import edu.cornell.cs.nlp.utils.log.LoggerFactory;
import edu.cornell.cs.nlp.utils.system.MemoryReport;

/**
 * Validation-based learner. See Artzi and Zettlemoyer 2013 for detailed
 * description. While the algorithm in the paper is situated, this one is not.
 * For a situated version see the package edu.uw.cs.lil.tiny.learn.situated.
 * <p>
 * The learner is insensitive to the syntactic category generated by the
 * inference procedure -- only the semantic portion is being validated. However,
 * parsers can be constrained to output only specific syntactic categories, see
 * the parser builders.
 * </p>
 * <p>
 * Parameter update step inspired by: Natasha Singh-Miller and Michael Collins.
 * 2007. Trigger-based Language Modeling using a Loss-sensitive Perceptron
 * Algorithm. In proceedings of ICASSP 2007.
 * </p>
 *
 * @author Yoav Artzi
 * @see ValidationPerceptron
 * @see ValidationStocGrad
 */
public abstract class AbstractLearnerGrounded<SAMPLE extends IDataItem<?>, DI extends IDataItem<SAMPLE>, PO extends IParserOutput<MR>, MR>
	implements ILearnerGrounded<SAMPLE, DI, Model<SAMPLE, MR>> {
	public static final ILogger												LOG					= LoggerFactory
			.create(AbstractLearnerGrounded.class);

	protected static final String											GOLD_LF_IS_MAX		= "G";
	protected static final String											HAS_VALID_LF		= "V";
	protected static final String											TRIGGERED_UPDATE	= "U";

	private final ICategoryServices<MR>										categoryServices;

	/**
	 * Number of training epochs.
	 */
	protected final int														epochs;
	
	/**
	 * The learner is error driven, meaning: if it can parse a sentence, it will
	 * skip lexical induction.
	 */
	protected final boolean													errorDriven;

	/**
	 * GENLEX procedure. If 'null', skip lexicon learning.
	 */
	protected final ILexiconGenerator<DI, MR, IModelImmutable<SAMPLE, MR>>	genlex;

	/**
	 * Parser beam size for lexical generation.
	 */
	private final Integer													lexiconGenerationBeamSize;

	protected final IParsingFilterFactory<DI, MR>								parsingFilterFactory;

	protected final IFilter<DI>												processingFilter;

	/**
	 * Training data.
	 */
	protected final IDataCollection<DI>										trainingData;

	/**
	 * Parser output logger.
	 */
	protected final IOutputLogger<MR>										parserOutputLogger;

	/**
	 * Learning statistics.
	 */
	protected final LearningStatsGrounded<DI>											stats;

	
	/** -------------------- added by Candace ----------------------------
	 * adding global parameters for equality metric & saving stats
	 */
	protected final String 														parentDirectory;
	/** -------------------- added by Candace ----------------------------
	 * whether to use partial match for logical form to simulate vision system
	 */
	protected final boolean														runModelBeforeTraining;
	protected final boolean														skipToTest;
	
	/** 
	 * 
	 * @param numIterations
	 * @param trainingData
	 * @param trainingDataDebug
	 * @param lexiconGenerationBeamSize
	 * @param parserOutputLogger
	 * @param errorDriven
	 * @param categoryServices
	 * @param genlex
	 * @param processingFilter
	 * @param parsingFilterFactory
	 * @param parentDirectory
	 * @param runModelBeforeTraining
	 */
	
	protected AbstractLearnerGrounded(int numIterations, IDataCollection<DI> trainingData,
			int lexiconGenerationBeamSize, IOutputLogger<MR> parserOutputLogger,
			boolean errorDriven, ICategoryServices<MR> categoryServices,
			ILexiconGenerator<DI, MR, IModelImmutable<SAMPLE, MR>> genlex,
			IFilter<DI> processingFilter, IParsingFilterFactory<DI, MR> parsingFilterFactory,
			String parentDirectory, boolean runModelBeforeTraining, boolean skipToTest) {
		this.epochs = numIterations;
		this.trainingData = trainingData;
		this.lexiconGenerationBeamSize = lexiconGenerationBeamSize;
		this.parserOutputLogger = parserOutputLogger;
		this.errorDriven = errorDriven;
		this.categoryServices = categoryServices;
		this.genlex = genlex;
		//TODO remove
		this.processingFilter = processingFilter;
		this.parsingFilterFactory = parsingFilterFactory;
		this.stats = new LearningStatsGrounded.Builder<DI>(trainingData.size(), epochs)
				.addStat(HAS_VALID_LF, "Has a valid parse")
				.addStat(TRIGGERED_UPDATE, "Sample triggered update")
				.addStat(GOLD_LF_IS_MAX,
						"The best-scoring LF equals the provided GOLD debug LF")
				.setNumberStat("Number of new lexical entries added").build();
		/** -------------------- added by Candace ---------------------------- **/
		this.parentDirectory = parentDirectory;
		this.runModelBeforeTraining = runModelBeforeTraining;
		this.skipToTest = skipToTest;
	}
	
	public class ThrowableValidator implements Callable<Pair<Boolean, IDerivation<MR>>> {

		final DI dataItem;
		final IDerivation<MR> parse;
		final IDataItemModel<MR> dataItemModel;
		final boolean shouldLogParse;
		final String trackerPort;
		
		
		public ThrowableValidator(DI dataItem, IDerivation<MR> parse, String trackerPort){
			this.dataItem = dataItem;
			this.parse = parse;
			this.trackerPort = trackerPort;
			this.dataItemModel = null;
			this.shouldLogParse = false;
		}
		
		public ThrowableValidator(DI dataItem, IDerivation<MR> parse, String trackerPort, IDataItemModel<MR> dataItemModel){
			this.dataItem = dataItem;
			this.parse = parse;
			this.trackerPort = trackerPort;
			this.dataItemModel = dataItemModel;
			this.shouldLogParse = true;
		}
		
		protected Pair<Boolean, IDerivation<MR>> callableValidator() throws MalformedURLException, IOException{
			// TODO rename parse in argument list
			SentenceTrackerValidator.addMapping(Long.valueOf(Thread.currentThread().getId()), trackerPort);
			
			boolean isValid;
			if (dataItem.existsInCache(parse.getSemantics().toString())){
				isValid = dataItem.validInCache(parse.getSemantics().toString());
			} else {
				isValid = validate(dataItem, parse.getSemantics());
				dataItem.addToCache(parse.getSemantics().toString(), isValid);
			}
			
			if (shouldLogParse){
				logParse(dataItem, parse,
						isValid, true,
						dataItemModel);
			}
			return new Pair<Boolean, IDerivation<MR>>(isValid, parse);
		}
		
		public final IDerivation<MR> getParse(){
			return parse;
		}
		
		@Override
		public Pair<Boolean, IDerivation<MR>> call() throws Exception {
			return callableValidator();
		}
		
	}
	
	@Override
	public void train(Model<SAMPLE, MR> model) {		
		if (runModelBeforeTraining){
			runInitialModel(model);
		}
		
		// Init GENLEX.
		LOG.info("Initializing GENLEX ...\n");
		genlex.init(model);
		
		// Epochs
		if (model.getSeedBenchmark() || skipToTest){
			System.out.println("not training");
			return;
		}
		
		for (int epochNumber = 0; epochNumber < epochs; ++epochNumber) {
			runSingleEpoch(model, epochNumber);
		}

		// Write stats
		recordFinalStats(epochs);
		writeFinalLexiconToFile(model);
	}
	
	protected void runSingleEpoch(Model<SAMPLE, MR> model, int epochNumber){
		LOG.info("=========================");
		LOG.info("Training epoch %d", epochNumber);
		LOG.info("=========================");
		System.out.println("=========================" + 
							"Training epoch " + epochNumber + 
							"=========================");
		
		
		int itemCounter = -1;
		for (final DI dataItem : trainingData) {
			
			// print update of where we are in training data
			if ( (itemCounter+1) % 25 == 0 ){ // adding 1 because we start from -1
				System.out.println("on example " + (itemCounter+1) + " of " + trainingData.size());
			}			
			
			// Process a single training sample

			// Record start time
			final long startTime = System.currentTimeMillis();

			// Log sample header
			LOG.info("%d : ================== [%d]", ++itemCounter,
					epochNumber);
			LOG.info("Sample type: %s\n%s",
					dataItem.getClass().getSimpleName(),
					dataItem.getProperties().toString());
			LOG.info("%s", dataItem);
			
			// Skip sample, if over the length limit
			if (!processingFilter.test(dataItem)) {
				LOG.info("Skipped training sample, due to processing filter");
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
				
				final List<? extends IDerivation<MR>> modelParses = parserOutput.getAllDerivations();				

				LOG.info("Model parsing time: %.4fsec", parserOutput.getParsingTime() / 1000.0);
				LOG.info("Output is %s", parserOutput.isExact() ? "exact" : "approximate");
				LOG.info("Created %d model parses for training sample:", modelParses.size());
				
				// validate parses in parallel
				// TODO decide on type of executor
				int portCounter = 0;
				ExecutorService executor = Executors.newFixedThreadPool(Runtime
																		.getRuntime().availableProcessors());
				for (final IDerivation<MR> parse : modelParses) {
					executor.execute(new FutureTask<Pair<Boolean, IDerivation<MR>>>(new ThrowableValidator(dataItem, parse,
																											SentenceTrackerValidator.getPortList().get(portCounter),
																											dataItemModel)));
					portCounter++;
					if (portCounter >= SentenceTrackerValidator.getPortList().size()){
						portCounter = 0;
					}
				}
				
				// reclaim resources
				executor.shutdown();
				SentenceTrackerValidator.clearMap();
				
				// Create a list of all valid parses
				final List<? extends IDerivation<MR>> validParses = getValidParses(parserOutput, dataItem);
				
				
				// If has a valid parse, call parameter update procedure and continue
				if (!validParses.isEmpty() && errorDriven) {
					parameterUpdate(dataItem, parserOutput, parserOutput,
							model, itemCounter, epochNumber);
					stats.addNewCorrectParse(epochNumber);
					continue;
				}
				
				// Add stats for either invalid parses or failures 
				if (validParses.isEmpty() && modelParses.size() > 0){
					stats.addNewIncorrectParse(epochNumber);
				} else {
					stats.addNewFailureParse(epochNumber);
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
				// TODO remove
				boolean conflateGenlexAndPrunedParses = false;
				if (conflateGenlexAndPrunedParses && generationParserOutput != null) {
						parameterUpdate(dataItem, parserOutput, generationParserOutput, model, itemCounter, epochNumber);
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
					
					parameterUpdate(dataItem, parserOutput, prunedParserOutput, model, itemCounter, epochNumber);
				}

				
				LOG.info("current avg precision: "+ stats.getAveragePrecision()
						+ " current avg recall: "+ stats.getAverageRecall()
				 		+ " current avg f1: "+ stats.getAverageF1());
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
		
		try{
			Model.write(model, new File(String.format("%s/logs/intermediate_model.sp", this.parentDirectory)));
		} catch (final IOException e){
			// TODO
			e.printStackTrace();
		}
		
		// Compute epoch stats
		System.out.println("precision: " + stats.getPrecision(epochNumber) +
							" recall " + stats.getRecall(epochNumber) +  
							" f1 " + stats.getF1(epochNumber));
		LOG.info("precision: " + stats.getPrecision(epochNumber) +
				" recall " + stats.getRecall(epochNumber) +  
				" f1 " + stats.getF1(epochNumber));
	}
	
	protected final void recordFinalStats(int epochs){
		stats.writeAveragesToFile(String.format("%s/logs/trends_train.txt", this.parentDirectory),
				trainingData.size());
		
		final BufferedWriter bw;
		try{
			bw = new BufferedWriter(new FileWriter (String.format("%s/logs/final_accuracy_train.txt", this.parentDirectory)));
			bw.write(String.format("Initial training precision: %f\n", stats.getPrecision(0)));
			bw.write(String.format("Initial training recall: %f\n", stats.getRecall(0)));
			bw.write(String.format("Initial training F1: %f\n", stats.getF1(0)));
			
			bw.write(String.format("Final training precision: %f\n", stats.getPrecision(epochs-1)));
			bw.write(String.format("Final training recall: %f\n", stats.getRecall(epochs-1)));
			bw.write(String.format("Final training F1: %f\n", stats.getF1(epochs-1)));
			
			bw.close();
		} catch (final IOException e){
			e.printStackTrace();
		}
	}
	
	protected final void writeFinalLexiconToFile(Model<SAMPLE, MR> model){		
		BufferedWriter bw = null;
		
		try {
			bw = new BufferedWriter(new FileWriter(String.format("%s/logs/final_lexicon.txt", this.parentDirectory)));
			bw.write("total number of entries in seed: " 		+ model.getSeedLexicon().getNumEntries() + "\n"
					+ "total number of entries in final: " 		+ model.getLexicon().getNumEntries() + "\n"
					+ "total number of learned entries: " 		+ model.getNumLearnedEntries() + "\n"
					+ "total number of empty entries: " 		+ model.getLexicon().getNumEmptySemanticsEntries() + "\n");
			// TODO fix this below
			//bw.write("average number of entries per token: " 	+ model.getLexicon().getAvgNumEntriesPerToken() + "\n");

			for(LexicalEntry<MR> entry : model.getLexicon().toCollection()){
				bw.write(entry + "\n");
			}
		} catch (IOException e){
			e.printStackTrace();
		} finally{
			try{
				bw.close();
			} catch (IOException ex){
				ex.printStackTrace();
			}
		}
	}

	protected List<? extends IDerivation<MR>> getValidParses(PO parserOutput,
			final DI dataItem) {
		final List<? extends IDerivation<MR>> parses = new LinkedList<IDerivation<MR>>(
				parserOutput.getAllDerivations());
		
		// TODO decide on type of executor
		ExecutorService executor = Executors.newFixedThreadPool(10);
		List<FutureTask<Pair<Boolean, IDerivation<MR>>>> tasks = new ArrayList<FutureTask<Pair<Boolean, IDerivation<MR>>>>();
		
		int portCounter = 0;
		for (IDerivation<MR> p : parses){
			FutureTask<Pair<Boolean, IDerivation<MR>>> current 
							= new FutureTask<Pair<Boolean, IDerivation<MR>>>(new ThrowableValidator(dataItem, p,
														SentenceTrackerValidator.getPortList().get(portCounter)));
			tasks.add(current);
			executor.execute(current);
			
			portCounter++;
			if (portCounter >= SentenceTrackerValidator.getPortList().size()){
				portCounter = 0;
			}
		}
		
		// reclaim resources
		executor.shutdown();
		SentenceTrackerValidator.clearMap();
		
		List<IDerivation<MR>> validParses = new ArrayList<IDerivation<MR>>();
		
		for (FutureTask<Pair<Boolean, IDerivation<MR>>> t : tasks){
			try{
				if (t.get().getKey()){
					validParses.add(t.get().getValue());
				}
			} catch(ExecutionException | InterruptedException e){
				continue;
			}
		}
		return validParses;
	}
	
	protected PO lexicalInduction(final DI dataItem, int dataItemNumber,
			IDataItemModel<MR> dataItemModel, Model<SAMPLE, MR> model,
			int epochNumber) {
		// Generate lexical entries
		
		final ILexiconImmutable<MR> generatedLexicon = genlex.generate(dataItem,
				model, categoryServices);
		
		LOG.info("Generated lexicon size = %d", generatedLexicon.size());
		
		if (generatedLexicon.size() > 0) {
			// Case generated lexical entries

			// Create pruning filter, if the data item fits
			final Predicate<ParsingOp<MR>> pruningFilter = parsingFilterFactory 
					.create(dataItem);
			
			// Parse with generated lexicon
			final PO parserOutput = parse(dataItem, pruningFilter,
					dataItemModel, generatedLexicon, lexiconGenerationBeamSize);
			
			// Log lexical generation parsing time
			stats.mean("genlex parse", parserOutput.getParsingTime() / 1000.0,
					"sec");
			LOG.info("Lexicon induction parsing time: %.4fsec",
					parserOutput.getParsingTime() / 1000.0);
			LOG.info("Output is %s",
					parserOutput.isExact() ? "exact" : "approximate");

			// Log generation parser output
			parserOutputLogger.log(parserOutput, dataItemModel, String
					.format("train-%d-%d-genlex", epochNumber, dataItemNumber));

			LOG.info("Created %d lexicon generation parses for training sample",
					parserOutput.getAllDerivations().size());
			
			// TODO
			if (parserOutput.getAllDerivations().size() > 2){
				int counter = 0;
				for (IDerivation<MR> parse :parserOutput.getAllDerivations()){
					LOG.info("Generated parse: " + parse);
					
					counter +=1;
					if (counter > 2){
						break;
					}
				}
			}
			
			// Get valid lexical generation parses
			final List<? extends IDerivation<MR>> validParses = getValidParses(
					parserOutput, dataItem);
			LOG.info("Removed %d invalid parses",
					parserOutput.getAllDerivations().size()
							- validParses.size());
			
			// Collect max scoring valid generation parses
			final List<IDerivation<MR>> bestGenerationParses = new LinkedList<IDerivation<MR>>();
			double currentMaxModelScore = -Double.MAX_VALUE;
			
			for (final IDerivation<MR> parse : validParses) {
				if (parse.getScore() > currentMaxModelScore) {
					currentMaxModelScore = parse.getScore();
					bestGenerationParses.clear();
					bestGenerationParses.add(parse);
				} else if (parse.getScore() == currentMaxModelScore) {
					bestGenerationParses.add(parse);
				}
			}
			
			LOG.info("%d valid best parses for lexical generation:",
					bestGenerationParses.size());
			for (final IDerivation<MR> parse : bestGenerationParses) {
				logParse(dataItem, parse, true, true, dataItemModel);
			}

			// Update the model's lexicon with generated lexical
			// entries from the max scoring valid generation parses
			int newLexicalEntries = 0;
			for (final IDerivation<MR> parse : bestGenerationParses) {
				for (final LexicalEntry<MR> entry : parse
						.getMaxLexicalEntries()) {
					if (genlex.isGenerated(entry)) {
						if (model.addLexEntry(
								LexiconGenerationServices.unmark(entry))) {
							++newLexicalEntries;
							LOG.info("Added LexicalEntry to model: %s [%s]",
									entry, model.getTheta().printValues(
											model.computeFeatures(entry)));
						}
						// Lexical generators might link related lexical
						// entries, so if we add the original one, we
						// should also add all its linked ones
						for (final LexicalEntry<MR> linkedEntry : entry
								.getLinkedEntries()) {
							if (model.addLexEntry(LexiconGenerationServices
									.unmark(linkedEntry))) {
								++newLexicalEntries;
								LOG.info(
										"Added (linked) LexicalEntry to model: %s [%s]",
										linkedEntry,
										model.getTheta().printValues(model
												.computeFeatures(linkedEntry)));
							}
						}
					}
				}
			}
			// Record statistics
			if (newLexicalEntries > 0) {
				stats.appendSampleStat(dataItemNumber, epochNumber,
						newLexicalEntries);
			}

			return parserOutput;
		} else {
			// Skip lexical induction
			LOG.info("Skipped GENLEX step. No generated lexical items.");
			return null;
		}
	}

	protected void logParse(DI dataItem, IDerivation<MR> parse, Boolean valid,
			boolean verbose, IDataItemModel<MR> dataItemModel) {
		logParse(dataItem, parse, valid, verbose, null, dataItemModel);
	}

	protected void logParse(DI dataItem, IDerivation<MR> parse, Boolean valid,
			boolean verbose, String tag, IDataItemModel<MR> dataItemModel) {
		LOG.info("%s[%.2f%s] %s",
				tag == null ? "" : tag + " ", parse.getScore(),
						valid == null ? "" : valid ? ", V" : ", X", parse);
		if (verbose) {
			for (final IWeightedParseStep<MR> step : parse.getMaxSteps()) {
				LOG.info("\t%s",
						step.toString(false, false, dataItemModel.getTheta()));
			}
		}
	}
	
	
	/**
	 * runs parser on all data once without making any parameter updates;
	 * this lets us see how the model does only on the seed lexicon
	 */
	protected void runInitialModel(Model<SAMPLE, MR> model){
		System.out.println("========================="
							+ "Running initial model..." + ""
							+ "=========================");
		
		int itemCounter = 0;
		int numCorrects = 0;
		int numFailures = 0;
		for (final DI dataItem : trainingData) {
			// Data item model
			final IDataItemModel<MR> dataItemModel = model.createDataItemModel(dataItem.getSample());
			
			// Parse with current model and record some statistics
			final PO parserOutput = parse(dataItem, dataItemModel);
			parserOutputLogger.log(parserOutput, dataItemModel, String
							.format("train-%d-%d", 0, itemCounter));
	
			final List<? extends IDerivation<MR>> modelParses = parserOutput.getAllDerivations();
	
			LOG.info("Model parsing time: %.4fsec",
					parserOutput.getParsingTime() / 1000.0);
			LOG.info("Output is %s",
					parserOutput.isExact() ? "exact" : "approximate");
			LOG.info("Created %d model parses for training sample:",
					modelParses.size());
			for (final IDerivation<MR> parse : modelParses) {
				try {
					logParse(dataItem, parse,
							validate(dataItem, parse.getSemantics()), false,
							dataItemModel);
				} catch (IOException e) {
					System.out.println("Error with sentence tracker..");
					e.printStackTrace();
				}
			}
			
			// Create a list of all valid parses
			final List<? extends IDerivation<MR>> validParses = getValidParses(parserOutput, dataItem);
			
			numCorrects += validParses.size() > 0 ? 1 : 0; // at least one valid parse
			numFailures += modelParses.size() == 0 ? 1 : 0; // no parses found
		}
		
		// precision = # correct / total # parsed
		final double precision = trainingData.size() - numFailures == 0.0 ? 0.0
														: (double) numCorrects / (trainingData.size() - numFailures);
		// recall = # correct / total # examples
		final double recall = trainingData.size() == 0 ? 0.0 : (double) numCorrects / trainingData.size();
		final double f1 = precision + recall == 0.0 ? 0.0
												: ( 2 * precision * recall ) / (precision + recall);
		
		System.out.println("initial stats:"
							+ "\n\tprecision: " + precision
							+ "\n\trecall: " + recall 
							+ "\n\tf1: " + f1);
	}
	
	

	/**
	 * Parameter update method.
	 */
	protected abstract void parameterUpdate(DI dataItem, PO realOutput,
			PO goodOutput, Model<SAMPLE, MR> model,
			//List<? extends IDerivation<MR>> validParses,
			int itemCounter, int epochNumber);

	/**
	 * Unconstrained parsing method.
	 */
	protected abstract PO parse(DI dataItem, IDataItemModel<MR> dataItemModel);

	/**
	 * Constrained parsing method.
	 */
	protected abstract PO parse(DI dataItem,
			Predicate<ParsingOp<MR>> pruningFilter,
			IDataItemModel<MR> dataItemModel);

	/**
	 * Constrained parsing method for lexical generation.
	 */
	protected abstract PO parse(DI dataItem,
			Predicate<ParsingOp<MR>> pruningFilter,
			IDataItemModel<MR> dataItemModel,
			ILexiconImmutable<MR> generatedLexicon, Integer beamSize);

	/**
	 * Validation method.
	 */
	abstract protected boolean validate(DI dataItem, MR hypothesis) throws MalformedURLException, IOException;
}