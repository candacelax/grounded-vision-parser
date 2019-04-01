package edu.cornell.cs.nlp.spf.learn.validation.jointcluster;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.function.Predicate;

import edu.cornell.cs.nlp.spf.base.hashvector.HashVectorFactory;
import edu.cornell.cs.nlp.spf.base.hashvector.IHashVector;
import edu.cornell.cs.nlp.spf.ccg.categories.Category;
import edu.cornell.cs.nlp.spf.ccg.categories.ICategoryServices;
import edu.cornell.cs.nlp.spf.ccg.lexicon.ILexiconImmutable;
import edu.cornell.cs.nlp.spf.data.IDataItem;
import edu.cornell.cs.nlp.spf.data.ILabeledDataItem;
import edu.cornell.cs.nlp.spf.data.collection.IDataCollection;
import edu.cornell.cs.nlp.spf.data.utils.IValidator;
import edu.cornell.cs.nlp.spf.explat.IResourceRepository;
import edu.cornell.cs.nlp.spf.explat.ParameterizedExperiment;
import edu.cornell.cs.nlp.spf.explat.ParameterizedExperiment.Parameters;
import edu.cornell.cs.nlp.spf.explat.resources.IResourceObjectCreator;
import edu.cornell.cs.nlp.spf.explat.resources.usage.ResourceUsage;
import edu.cornell.cs.nlp.spf.genlex.ccg.ILexiconGenerator;
import edu.cornell.cs.nlp.spf.learn.validation.AbstractLearner;
import edu.cornell.cs.nlp.spf.learn.validation.stocgrad.ValidationStocGrad.Builder;
import edu.cornell.cs.nlp.spf.parser.IOutputLogger;
import edu.cornell.cs.nlp.spf.parser.IParserOutput;
import edu.cornell.cs.nlp.spf.parser.ParsingOp;
import edu.cornell.cs.nlp.spf.parser.ccg.model.IDataItemModel;
import edu.cornell.cs.nlp.spf.parser.ccg.model.IModelImmutable;
import edu.cornell.cs.nlp.spf.parser.ccg.model.Model;
import edu.cornell.cs.nlp.spf.parser.filter.IParsingFilterFactory;
import edu.cornell.cs.nlp.spf.parser.filter.StubFilterFactory;
import edu.cornell.cs.nlp.spf.parser.graph.IGraphParser;
import edu.cornell.cs.nlp.spf.parser.graph.IGraphParserOutput;
import edu.cornell.cs.nlp.utils.filter.IFilter;
import edu.cornell.cs.nlp.utils.log.ILogger;
import edu.cornell.cs.nlp.utils.log.LoggerFactory;

public class ValidationStocGradJointWithCluster<SAMPLE extends IDataItem<SAMPLE>, DI extends ILabeledDataItem<SAMPLE, ?>, MR>
		extends AbstractLearner<SAMPLE, DI, IGraphParserOutput<MR>, MR> {
	public static final ILogger				LOG						= LoggerFactory
			.create(ValidationStocGradJointWithCluster.class);

	private final double					alpha0;

	private final double					c;

	private final IGraphParser<SAMPLE, MR>	parser;

	private int								stocGradientNumUpdates	= 0;

	private final IValidator<DI, MR>		validator;
	
	private final ArrayList<HashMap<DI, String>>		allClusters;
	
	private HashMap<DI, String>				currentCluster;
	
	
	private ValidationStocGradJointWithCluster(int numIterations,
			IDataCollection<DI> trainingData, Map<DI, MR> trainingDataDebug,
			int maxSentenceLength, int lexiconGenerationBeamSize,
			IGraphParser<SAMPLE, MR> parser,
			IOutputLogger<MR> parserOutputLogger, double alpha0, double c,
			IValidator<DI, MR> validator, boolean conflateGenlexAndPrunedParses,
			boolean errorDriven, ICategoryServices<MR> categoryServices,
			ILexiconGenerator<DI, MR, IModelImmutable<SAMPLE, MR>> genlex,
			IFilter<DI> processingFilter, IParsingFilterFactory<DI, MR> parsingFilterFactory,
			String parentDirectory, double trainingNoise,
			boolean runModelBeforeTraining, ArrayList<HashMap<DI, String>> allClusters,
			boolean skipToTest){
		super(numIterations, trainingData, trainingDataDebug,
				lexiconGenerationBeamSize, parserOutputLogger,
				conflateGenlexAndPrunedParses, errorDriven, categoryServices,
				genlex, processingFilter, parsingFilterFactory,
				parentDirectory, trainingNoise, runModelBeforeTraining, skipToTest);
		this.parser = parser;
		this.alpha0 = alpha0;
		this.c = c;
		this.validator = validator;
		this.allClusters = allClusters;
		
		LOG.info(
				"Init ValidationStocGrad: numIterations=%d, trainingData.size()=%d, trainingDataDebug.size()=%d, maxSentenceLength=%d ...",
				numIterations, trainingData.size(), trainingDataDebug.size(),
				maxSentenceLength);
		LOG.info("Init ValidationStocGrad: ... lexiconGenerationBeamSize=%d",
				lexiconGenerationBeamSize);
		LOG.info(
				"Init ValidationStocGrad: ... conflateParses=%s, erroDriven=%s",
				conflateGenlexAndPrunedParses ? "true" : "false",
				errorDriven ? "true" : "false");
		LOG.info("Init ValidationStocGrad: ... c=%f, alpha0=%f", c, alpha0);
		LOG.info("Init ValidationStocGrad: ... parsingFilterFactory=%s",
				parsingFilterFactory);
	}

	@Override
	public void train(Model<SAMPLE, MR> model) {
		stocGradientNumUpdates = 0;
		super.train(model);
	}
	
	// TODO figure out why this was here
	/*
	@Override
	protected void runSingleEpoch(Model<SAMPLE, MR> model, int epochNumber){
		this.currentCluster = allClusters.get(epochNumber);
		super.runSingleEpoch(model, epochNumber);
	}
	*/
	
	@Override
	protected void parameterUpdate(final DI dataItem,
			IGraphParserOutput<MR> realOutput,
			IGraphParserOutput<MR> goodOutput, Model<SAMPLE, MR> model,
			int itemCounter, int epochNumber,
			boolean noisyExample) {

		// Create the update
		final IHashVector update = HashVectorFactory.create();
		
		// Step A: Compute the positive half of the update: conditioned on
		// getting successful validation
		
		final IFilter<Category<MR>> filter = e -> validate(dataItem, e.getSemantics());
		
		final double logConditionedNorm = goodOutput.logNorm(filter);
		if (logConditionedNorm == Double.NEGATIVE_INFINITY) {
			// No positive update, skip the update.
			LOG.info("No positive update");
			return;
		} else {
			// Case have complete valid parses.
			// Check if example is noise for simulation; if so,
			// invert the update (added by Candace)
			final IHashVector expectedFeatures = goodOutput
					.logExpectedFeatures(filter);
			expectedFeatures.add(-logConditionedNorm);
			expectedFeatures.applyFunction(value -> Math.exp(value));
			expectedFeatures.dropNoise();
			expectedFeatures.addTimesInto(noisyExample ? -1.0 : 1.0 , update);

			// Record if the output LF equals the available gold LF (if one is
			// available), otherwise, record using validation signal.
			stats.count("Valid", epochNumber);
			if (realOutput.getBestDerivations().size() == 1
					&& isGoldDebugCorrect(dataItem, realOutput
							.getBestDerivations().get(0).getSemantics())) {
				stats.appendSampleStat(itemCounter, epochNumber,
						GOLD_LF_IS_MAX);
			} else {
				// Record if a valid parse was found.
				stats.appendSampleStat(itemCounter, epochNumber, HAS_VALID_LF);
			}

			LOG.info("Positive update: %s", expectedFeatures);
		}
		
		// Step B: Compute the negative half of the update: expectation under
		// the current model
		
		final double logNorm = realOutput.logNorm();
		if (logNorm == Double.NEGATIVE_INFINITY) {
			LOG.info("No negative update.");
		} else {
			// Case have complete parses.
			// Check if example is noise for simulation; if so,
			// invert the update (added by Candace)
			final IHashVector expectedFeatures = realOutput
					.logExpectedFeatures();
			expectedFeatures.add(-logNorm);
			expectedFeatures.applyFunction(value -> Math.exp(value));
			expectedFeatures.dropNoise();
			expectedFeatures.addTimesInto(noisyExample ? 1.0 : -1.0 , update);
			LOG.info("Negative update: %s", expectedFeatures);
		}

		// Step C: Apply the update

		// Validate the update
		if (!model.isValidWeightVector(update)) {
			throw new IllegalStateException("invalid update: " + update);
		}

		// Scale the update
		final double scale = alpha0 / (1.0 + c * stocGradientNumUpdates);
		update.multiplyBy(scale);
		update.dropNoise();
		stocGradientNumUpdates++;
		LOG.info("Scale: %f", scale);
		if (update.size() == 0) {
			LOG.info("No update");
			return;
		} else {
			LOG.info("Update: %s", update);
		}

		// Check for NaNs and super large updates
		if (update.isBad()) {
			LOG.error("Bad update: %s -- log-norm: %.4f -- features: %s",
					update, logNorm, model.getTheta().printValues(update));
			throw new IllegalStateException("bad update");
		} else {
			if (!update.valuesInRange(-100, 100)) {
				LOG.error("Large update: %s -- log-norm: %.4f -- features: %s",
						update, logNorm, model.getTheta().printValues(update));
			}
			 
			//Do the update
			update.addTimesInto(1, model.getTheta());
			stats.appendSampleStat(itemCounter, epochNumber, TRIGGERED_UPDATE);
		}
	}

	@Override
	protected IGraphParserOutput<MR> parse(DI dataItem,
			IDataItemModel<MR> dataItemModel) {
		return parser.parse(dataItem.getSample(), dataItemModel);
	}

	@Override
	protected IGraphParserOutput<MR> parse(DI dataItem,
			Predicate<ParsingOp<MR>> pruningFilter,
			IDataItemModel<MR> dataItemModel) {
		return parser.parse(dataItem.getSample(), pruningFilter, dataItemModel);
	}

	@Override
	protected IGraphParserOutput<MR> parse(DI dataItem,
			Predicate<ParsingOp<MR>> pruningFilter,
			IDataItemModel<MR> dataItemModel,
			ILexiconImmutable<MR> generatedLexicon, Integer beamSize) {
		return parser.parse(dataItem.getSample(), pruningFilter, dataItemModel,
				false, generatedLexicon, beamSize);
	}

	@Override
	protected boolean validate(DI dataItem, MR hypothesis) {
		final boolean valid = validator.isValid(dataItem, hypothesis);
		return valid;
	}

	public static class Builder<SAMPLE extends IDataItem<SAMPLE>, DI extends ILabeledDataItem<SAMPLE, ?>, MR> {

		/**
		 * Used to define the temperature of parameter updates. temp =
		 * alpha_0/(1+c*num_updates)
		 */
		private double													alpha0							= 1.0;

		/**
		 * Used to define the temperature of parameter updates. temp =
		 * alpha_0/(1+c*num_updates)
		 */
		private double													c								= 0.0001;

		/**
		 * Required for lexicon learning.
		 */
		private ICategoryServices<MR>									categoryServices				= null;

		/**
		 * Recycle the lexical induction parser output as the pruned one for
		 * parameter update.
		 */
		private boolean													conflateGenlexAndPrunedParses	= false;
		private boolean													errorDriven						= false;

		/**
		 * Processing filter, if 'false', skip sample.
		 */
		private IFilter<DI>												filter							= e -> true;

		/**
		 * GENLEX procedure. If 'null' skips lexicon induction.
		 */
		private ILexiconGenerator<DI, MR, IModelImmutable<SAMPLE, MR>>	genlex;

		/**
		 * Beam size to use when doing loss sensitive pruning with generated
		 * lexicon.
		 */
		private int														lexiconGenerationBeamSize		= 20;

		/**
		 * Max sentence length. Sentence longer than this value will be skipped
		 * during training
		 */
		private final int												maxSentenceLength				= Integer.MAX_VALUE;

		/** Number of training iterations */
		private int														numIterations					= 4;

		private final IGraphParser<SAMPLE, MR>							parser;

		private IOutputLogger<MR>										parserOutputLogger				= new IOutputLogger<MR>() {

		/**
		*
		*/
		private static final long serialVersionUID = 6453394425169742726L;

		@Override
		public void log(IParserOutput<MR> output, IDataItemModel<MR> dataItemModel, String tag) {
			// Stub.	
			}
		};

		private IParsingFilterFactory<DI, MR>							parsingFilterFactory			= new StubFilterFactory<DI, MR>();

		/** Training data */
		private final IDataCollection<DI>								trainingData;

		/**
		 * Mapping a subset of training samples into their gold label for debug.
		 */
		private Map<DI, MR>												trainingDataDebug				= new HashMap<DI, MR>();

		private final IValidator<DI, MR>								validator;
		
		/**
		 * -- added by Candace --
		 */
		private final String 											parentDirectory;
		private boolean													partialMatch;
		private double 													trainingNoise;
		private boolean													runModelBeforeTraining;
		protected final Random											rng 							= new Random();
		private boolean													skipToTest;
		
		public Builder(IDataCollection<DI> trainingData,
				IGraphParser<SAMPLE, MR> parser, IValidator<DI, MR> validator,
				String parentDirectory) {
			this.trainingData = trainingData;
			this.parser = parser;
			this.validator = validator;
			this.parentDirectory = parentDirectory;
		}
		
		private ArrayList<HashMap<DI, String>> createAllClusters(IDataCollection<DI> trainingData, int numIterations){
			final ArrayList<HashMap<DI, String>> allClusters = new ArrayList<HashMap<DI, String>>();
			for (int i=0; i < numIterations; i++){
				final HashMap<DI, String> singleCluster = new HashMap<DI, String>();
				for (final DI dataItem : trainingData){
					singleCluster.put(dataItem, rng.nextInt(2) == 1 ? "grounded" : "ungrounded");
				}
				allClusters.add(singleCluster);
			}
			return allClusters;
		}

		public ValidationStocGradJointWithCluster<SAMPLE, DI, MR> build() {
			return new ValidationStocGradJointWithCluster<SAMPLE, DI, MR>(numIterations,
					trainingData, trainingDataDebug, maxSentenceLength,
					lexiconGenerationBeamSize, parser, parserOutputLogger,
					alpha0, c, validator, conflateGenlexAndPrunedParses,
					errorDriven, categoryServices, genlex, filter,
					parsingFilterFactory, parentDirectory, trainingNoise,
					runModelBeforeTraining, createAllClusters(trainingData, numIterations),
					skipToTest);
		}

		public Builder<SAMPLE, DI, MR> setAlpha0(double alpha0) {
			this.alpha0 = alpha0;
			return this;
		}

		public Builder<SAMPLE, DI, MR> setC(double c) {
			this.c = c;
			return this;
		}

		public Builder<SAMPLE, DI, MR> setConflateGenlexAndPrunedParses(
				boolean conflateGenlexAndPrunedParses) {
			this.conflateGenlexAndPrunedParses = conflateGenlexAndPrunedParses;
			return this;
		}

		public Builder<SAMPLE, DI, MR> setErrorDriven(boolean errorDriven) {
			this.errorDriven = errorDriven;
			return this;
		}

		public Builder<SAMPLE, DI, MR> setGenlex(
				ILexiconGenerator<DI, MR, IModelImmutable<SAMPLE, MR>> genlex,
				ICategoryServices<MR> categoryServices) {
			this.genlex = genlex;
			this.categoryServices = categoryServices;
			return this;
		}

		public Builder<SAMPLE, DI, MR> setLexiconGenerationBeamSize(
				int lexiconGenerationBeamSize) {
			this.lexiconGenerationBeamSize = lexiconGenerationBeamSize;
			return this;
		}

		public Builder<SAMPLE, DI, MR> setNumIterations(int numIterations) {
			this.numIterations = numIterations;
			return this;
		}

		public Builder<SAMPLE, DI, MR> setParserOutputLogger(
				IOutputLogger<MR> parserOutputLogger) {
			this.parserOutputLogger = parserOutputLogger;
			return this;
		}

		public Builder<SAMPLE, DI, MR> setParsingFilterFactory(
				IParsingFilterFactory<DI, MR> parsingFilterFactory) {
			this.parsingFilterFactory = parsingFilterFactory;
			return this;
		}

		public Builder<SAMPLE, DI, MR> setProcessingFilter(IFilter<DI> filter) {
			this.filter = filter;
			return this;
		}

		public Builder<SAMPLE, DI, MR> setTrainingDataDebug(
				Map<DI, MR> trainingDataDebug) {
			this.trainingDataDebug = trainingDataDebug;
			return this;
		}
		
		public Builder<SAMPLE, DI, MR> setPartialMatch(boolean partialMatch){
			this.partialMatch = partialMatch;
			return this;
		}
		
		public Builder<SAMPLE, DI, MR> setTrainingNoise(double trainingNoise) {
			assert(trainingNoise > 0 && trainingNoise < 1);
			this.trainingNoise = trainingNoise;
			return this;
		}
		
		public Builder<SAMPLE, DI, MR> setRunModelBeforeTraining(Boolean runModelBeforeTraining) {
			this.runModelBeforeTraining = runModelBeforeTraining;
			return this;
		}
		
		public Builder<SAMPLE, DI, MR> setSkipToTest(Boolean skipToTest) {
			this.skipToTest = skipToTest;
			return this;
		}
		
	}

	public static class Creator<SAMPLE extends IDataItem<SAMPLE>, DI extends ILabeledDataItem<SAMPLE, ?>, MR>
			implements
			IResourceObjectCreator<ValidationStocGradJointWithCluster<SAMPLE, DI, MR>> {

		private final String type;

		public Creator() {
			this("learner.validation.stocgradjointcluster");
		}

		public Creator(String type) {
			this.type = type;
		}

		@SuppressWarnings("unchecked")
		@Override
		public ValidationStocGradJointWithCluster<SAMPLE, DI, MR> create(Parameters params,
				IResourceRepository repo) {
					
			final IDataCollection<DI> trainingData = repo
					.get(params.get("data"));
			
			final Builder<SAMPLE, DI, MR> builder = new ValidationStocGradJointWithCluster.Builder<SAMPLE, DI, MR>(
					trainingData,
					(IGraphParser<SAMPLE, MR>) repo
							.get(ParameterizedExperiment.PARSER_RESOURCE),
					(IValidator<DI, MR>) repo.get(params.get("validator")),
					repo.get("parentDirectory")); // added by Candace
			
			
			if (params.contains("genlex")) {
				builder.setGenlex(
						(ILexiconGenerator<DI, MR, IModelImmutable<SAMPLE, MR>>) repo
								.get(params.get("genlex")),
						(ICategoryServices<MR>) repo.get(
								ParameterizedExperiment.CATEGORY_SERVICES_RESOURCE));
			}

			if (params.contains("parseLogger")) {
				builder.setParserOutputLogger((IOutputLogger<MR>) repo
						.get(params.get("parseLogger")));
			}

			if (params.contains("genlexbeam")) {
				builder.setLexiconGenerationBeamSize(
						Integer.valueOf(params.get("genlexbeam")));
			}

			if (params.contains("iter")) {
				builder.setNumIterations(Integer.valueOf(params.get("iter")));
			}

			if (params.contains("filter")) {
				builder.setProcessingFilter(
						(IFilter<DI>) repo.get(params.get("filter")));
			}

			if (params.contains("errorDriven")) {
				builder.setErrorDriven(
						"true".equals(params.get("errorDriven")));
			}

			if (params.contains("c")) {
				builder.setC(Double.valueOf(params.get("c")));
			}

			if (params.contains("filterFactory")) {
				builder.setParsingFilterFactory(
						(IParsingFilterFactory<DI, MR>) repo
								.get(params.get("filterFactory")));
			}

			if (params.contains("alpha0")) {
				builder.setAlpha0(Double.valueOf(params.get("alpha0")));
			}

			if (params.contains("conflateParses")) {
				builder.setConflateGenlexAndPrunedParses(
						"true".equals(params.get("conflateParses")));
			}
			
			/** -- added by Candace --
			 * determine whether we're accepting partial matches for equality;
			 * amount of noise to be added to training; use only for simulating vision system
			 */
			if (params.contains("partialMatch")){
				builder.setPartialMatch(params.get("partialMatch").equalsIgnoreCase("true"));
			}
			
			if (params.contains("trainingNoise")) {
				builder.setTrainingNoise(
						(Double.parseDouble(params.get("trainingNoise"))));
			}
			
			if (params.contains("runModelBeforeTraining")) {
				builder.setRunModelBeforeTraining(
						Boolean.parseBoolean(params.get("runModelBeforeTraining")));
			}
			
			if (params.contains("skipToTest")) {
				builder.setSkipToTest(
						Boolean.parseBoolean(params.get("skipToTest")));
			}
			
			return builder.build();
		}

		@Override
		public String type() {
			return type;
		}

		@Override
		public ResourceUsage usage() {
			return new ResourceUsage.Builder(type(), ValidationStocGradJointWithCluster.class)
					.setDescription(
							"Validation-based stochastic gradient learner")
					.addParam("data", "id", "Training data")
					.addParam("genlex", "ILexiconGenerator", "GENLEX procedure")
					.addParam("filterFactory", IParsingFilterFactory.class,
							"Factory to create parsing filters (optional).")
					.addParam("conflateParses", "boolean",
							"Recyle lexical induction parsing output as pruned parsing output")
					.addParam("parseLogger", "id",
							"Parse logger for debug detailed logging of parses")
					.addParam("genlexbeam", "int",
							"Beam to use for GENLEX inference (parsing).")
					.addParam("filter", "IFilter", "Processing filter")
					.addParam("iter", "int", "Number of training iterations")
					.addParam("validator", "IValidator", "Validation function")
					.addParam("tester", "ITester",
							"Intermediate tester to use between epochs")
					.addParam("c", "double",
							"Learing rate c parameter, temperature=alpha_0/(1+c*tot_number_of_training_instances)")
					.addParam("alpha0", "double",
							"Learing rate alpha0 parameter, temperature=alpha_0/(1+c*tot_number_of_training_instances)")
					.addParam("errorDriven", "boolean",
							"Error driven lexical generation, if the can generate a valid parse, skip lexical induction")
					.build();
		}

	}
}
