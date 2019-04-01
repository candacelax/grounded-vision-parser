package edu.mit.cs.infolab.groundedvisionparser;

import edu.cornell.cs.nlp.spf.ccg.lexicon.factored.lambda.FactoredLexicon;
import edu.cornell.cs.nlp.spf.data.collection.CompositeDataCollection;
import edu.cornell.cs.nlp.spf.data.sentence.Sentence;
import edu.cornell.cs.nlp.spf.data.sentence.SentenceLengthFilter;
import edu.cornell.cs.nlp.spf.data.singlesentence.SingleSentence;
import edu.cornell.cs.nlp.spf.data.singlesentence.SingleSentenceCollection;
import edu.cornell.cs.nlp.spf.explat.resources.ResourceCreatorRepository;
import edu.cornell.cs.nlp.spf.genlex.ccg.template.coarse.TemplateCoarseGenlex;
import edu.cornell.cs.nlp.spf.genlex.ccg.unification.UnificationModelInit;
import edu.cornell.cs.nlp.spf.genlex.ccg.unification.split.Splitter;
import edu.cornell.cs.nlp.spf.learn.validation.stocgrad.ValidationStocGradGrounded;
import edu.cornell.cs.nlp.spf.mr.lambda.LogicalExpression;
import edu.cornell.cs.nlp.spf.mr.lambda.ccg.SimpleFullParseFilter;
import edu.cornell.cs.nlp.spf.parser.ccg.cky.multi.MultiCKYParser;
import edu.cornell.cs.nlp.spf.parser.ccg.cky.single.CKYParser;
import edu.cornell.cs.nlp.spf.parser.ccg.factoredlex.features.FactoredLexicalFeatureSet;
import edu.cornell.cs.nlp.spf.parser.ccg.factoredlex.features.scorers.LexemeCooccurrenceScorer;
import edu.cornell.cs.nlp.spf.parser.ccg.features.basic.DynamicWordSkippingFeatures;
import edu.cornell.cs.nlp.spf.parser.ccg.features.basic.LexicalFeaturesInit;
import edu.cornell.cs.nlp.spf.parser.ccg.features.basic.RuleUsageFeatureSet;
import edu.cornell.cs.nlp.spf.parser.ccg.features.basic.PredicateEmptySemanticsFeatureSet;
import edu.cornell.cs.nlp.spf.parser.ccg.features.basic.DuplicatePredicatesFeatureSet;
import edu.cornell.cs.nlp.spf.parser.ccg.features.basic.scorer.ExpLengthLexicalEntryScorer;
import edu.cornell.cs.nlp.spf.parser.ccg.features.basic.scorer.SkippingSensitiveLexicalEntryScorer;
import edu.cornell.cs.nlp.spf.parser.ccg.features.basic.scorer.UniformScorer;
import edu.cornell.cs.nlp.spf.parser.ccg.features.lambda.LogicalExpressionCoordinationFeatureSet;
import edu.cornell.cs.nlp.spf.parser.ccg.model.LexiconModelInit;
import edu.cornell.cs.nlp.spf.parser.ccg.model.Model;
import edu.cornell.cs.nlp.spf.parser.ccg.model.ModelLogger;
import edu.cornell.cs.nlp.spf.parser.ccg.model.WeightInit;
import edu.cornell.cs.nlp.spf.parser.ccg.normalform.eisner.EisnerNormalFormCreator;
import edu.cornell.cs.nlp.spf.parser.ccg.normalform.unaryconstraint.UnaryConstraint;
import edu.cornell.cs.nlp.spf.parser.ccg.rules.coordination.CoordinationRule;
import edu.cornell.cs.nlp.spf.parser.ccg.rules.coordination.simple.CoordinationRuleSimple;
import edu.cornell.cs.nlp.spf.parser.ccg.rules.lambda.PluralExistentialTypeShifting;
import edu.cornell.cs.nlp.spf.parser.ccg.rules.lambda.ThatlessRelative;
import edu.cornell.cs.nlp.spf.parser.ccg.rules.lambda.typeraising.ForwardTypeRaisedComposition;
import edu.cornell.cs.nlp.spf.parser.ccg.rules.lambda.typeshifting.AdjectiveTypeShifting;
import edu.cornell.cs.nlp.spf.parser.ccg.rules.lambda.typeshifting.PrepositionTypeShifting;
import edu.cornell.cs.nlp.spf.parser.ccg.rules.primitivebinary.application.ApplicationCreator;
import edu.cornell.cs.nlp.spf.parser.ccg.rules.primitivebinary.composition.CompositionCreator;
import edu.cornell.cs.nlp.spf.test.Tester;
import edu.mit.cs.infolab.data.groundedsentence.GroundedSentenceCollection;
import edu.mit.cs.infolab.data.groundedsentence.utils.SentenceTrackerValidator;



public class GroundedVisionParserResourceRepo extends ResourceCreatorRepository {
	public GroundedVisionParserResourceRepo() {
		// Parser creators
		registerResourceCreator(new ApplicationCreator<LogicalExpression>());
		registerResourceCreator(new CompositionCreator<LogicalExpression>());
		registerResourceCreator(new PrepositionTypeShifting.Creator());
		registerResourceCreator(new AdjectiveTypeShifting.Creator());
		registerResourceCreator(new ForwardTypeRaisedComposition.Creator());
		registerResourceCreator(new ThatlessRelative.Creator());
		registerResourceCreator(new PluralExistentialTypeShifting.Creator());
		registerResourceCreator(new CoordinationRule.Creator());
		registerResourceCreator(new CoordinationRuleSimple.Creator());
		registerResourceCreator(
				new MultiCKYParser.Creator<Sentence, LogicalExpression>());
		registerResourceCreator(
				new CKYParser.Creator<Sentence, LogicalExpression>());
		registerResourceCreator(new SimpleFullParseFilter.Creator());
		registerResourceCreator(
				new ExpLengthLexicalEntryScorer.Creator<LogicalExpression>());
		registerResourceCreator(
				new LexicalFeaturesInit.Creator<Sentence, LogicalExpression>());
		registerResourceCreator(
				new Model.Creator<Sentence, LogicalExpression>());
		registerResourceCreator(new ModelLogger.Creator());
		registerResourceCreator(new UniformScorer.Creator<LogicalExpression>());
		registerResourceCreator(
				new FactoredLexicalFeatureSet.Creator<Sentence>());
		registerResourceCreator(
				new SkippingSensitiveLexicalEntryScorer.Creator<LogicalExpression>());
		registerResourceCreator(
				new LogicalExpressionCoordinationFeatureSet.Creator<Sentence>());
		registerResourceCreator(new FactoredLexicon.Creator());
		registerResourceCreator(
				new TemplateCoarseGenlex.Creator<Sentence>());
		// for training data
		registerResourceCreator(new GroundedSentenceCollection.Creator());
		// for test data
		registerResourceCreator(new SingleSentenceCollection.Creator());
		registerResourceCreator(
				new CompositeDataCollection.Creator<SingleSentence>());
		// TODO make perceptron weak
		//registerResourceCreator(
			//	new ValidationPerceptron.Creator<Sentence, SingleSentence, LogicalExpression>());
		// TODO remove this creator from validation.inc; it's not being used
		registerResourceCreator(
				new SentenceTrackerValidator.Creator<Sentence, LogicalExpression>());
		registerResourceCreator(
				new ValidationStocGradGrounded.Creator<Sentence, Sentence, LogicalExpression>());
		// TODO fix this
		registerResourceCreator(
				new Tester.Creator<Sentence, LogicalExpression, SingleSentence>());
		registerResourceCreator(
				new LexiconModelInit.Creator<Sentence, LogicalExpression>());
		registerResourceCreator(new Splitter.Creator());
		registerResourceCreator(new UnificationModelInit.Creator());
		registerResourceCreator(new LexemeCooccurrenceScorer.Creator());
		registerResourceCreator(
				new SentenceLengthFilter.Creator<Sentence>());
		registerResourceCreator(new EisnerNormalFormCreator());
		registerResourceCreator(new UnaryConstraint.Creator());
		registerResourceCreator(
				new RuleUsageFeatureSet.Creator<Sentence, LogicalExpression>());
		registerResourceCreator(new DynamicWordSkippingFeatures.Creator<>());
		registerResourceCreator(new WeightInit.Creator<>());
		// added by Candace
		registerResourceCreator(
				new PredicateEmptySemanticsFeatureSet.Creator<>());
		registerResourceCreator(
				new DuplicatePredicatesFeatureSet.Creator<>());
	}
}