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
package edu.cornell.cs.nlp.spf.test.stats;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.cornell.cs.nlp.spf.data.ILabeledDataItem;
import edu.cornell.cs.nlp.spf.explat.IResourceRepository;
import edu.cornell.cs.nlp.spf.explat.ParameterizedExperiment.Parameters;
import edu.cornell.cs.nlp.spf.explat.resources.IResourceObjectCreator;
import edu.cornell.cs.nlp.spf.explat.resources.usage.ResourceUsage;
import edu.cornell.cs.nlp.spf.mr.lambda.LogicalConstant;
import edu.cornell.cs.nlp.spf.mr.lambda.LogicalExpression;
import edu.cornell.cs.nlp.spf.mr.lambda.visitor.GetPredicateCounts;
import edu.cornell.cs.nlp.utils.counter.Counter;
import edu.cornell.cs.nlp.utils.log.ILogger;
import edu.cornell.cs.nlp.utils.log.LoggerFactory;

/**
 * Testing statistics for the exact match metric.
 *
 * @author Yoav Artzi
 * @param <SAMPLE>
 *            Testing sample.
 * @param <LABEL>
 *            Provided label.
 */
public class ExactMatchTestingStatistics<SAMPLE, LABEL, DI extends ILabeledDataItem<SAMPLE, LABEL>>
		extends AbstractTestingStatistics<SAMPLE, LABEL, DI> {
	public static final ILogger	LOG					= LoggerFactory
															.create(ExactMatchTestingStatistics.class);

	private static final String	DEFAULT_METRIC_NAME	= "EXACT";
	
	/** -- added by Candace --
	 * stats for comparing across parameters
	 */
	private final Map<String, Integer> costs = new HashMap<String, Integer>();
	private final Map<String, Integer> numPredicates = new HashMap<String, Integer>();
	private Integer running_sum_costs = 0;
	private Integer running_sum_lengths = 0;
	

	public ExactMatchTestingStatistics() {
		this(null);
	}

	public ExactMatchTestingStatistics(String prefix) {
		this(prefix, DEFAULT_METRIC_NAME);
	}

	public ExactMatchTestingStatistics(String prefix, String metricName) {
		this(prefix, metricName, new SimpleStats<SAMPLE>(DEFAULT_METRIC_NAME));
	}

	public ExactMatchTestingStatistics(String prefix, String metricName, IStatistics<SAMPLE> stats) {
		super(prefix, metricName, stats);
	}

	@Override
	public void recordNoParse(DI dataItem) {
		LOG.info("%s stats -- recording no parse", getMetricName());
		stats.recordFailure(dataItem.getSample());
	}
	
	@Override
	public void recordNoParsePartialMatch(DI dataItem) {
		LOG.info("%s stats -- recording no parse for partial match", getMetricName());
		stats.recordFailurePartial(dataItem.getSample()); // TODO make own method
	}

	@Override
	public void recordNoParseWithSkipping(DI dataItem) {
		LOG.info("%s stats -- recording no parse with skipping",
				getMetricName());
		stats.recordSloppyFailure(dataItem.getSample());
	}

	@Override
	public void recordParse(DI dataItem, LABEL candidate, boolean isCorrect) {
		if (isCorrect) {
			LOG.info("%s stats -- recording correct parse: %s",
					getMetricName(), candidate);
			stats.recordCorrect(dataItem.getSample());
		}else {
			LOG.info("%s stats -- recording wrong parse: %s", getMetricName(),
					candidate);
			stats.recordIncorrect(dataItem.getSample());
		}
	}
	
	@Override
	public void recordParsePartialMatch(DI dataItem, LABEL candidate, boolean isCorrect) {
		if (isCorrect) {
			LOG.info("%s stats -- recording correct parse with partial match: %s",
					getMetricName(), candidate);
			stats.recordCorrectPartial(dataItem.getSample());
		}else {
			LOG.info("%s stats -- recording wrong parse with partial match: %s", getMetricName(),
					candidate);
			stats.recordIncorrectPartial(dataItem.getSample());
		}
	}

	@Override
	public void recordParses(DI dataItem, List<LABEL> candidates) {
		recordNoParse(dataItem);
	}
	
	@Override
	public void recordParsesPartialMatch(DI dataItem, List<LABEL> candidates) {
		recordNoParsePartialMatch(dataItem);
	}

	@Override
	public void recordParsesWithSkipping(DI dataItem, List<LABEL> labels) {
		recordNoParseWithSkipping(dataItem);
	}

	@Override
	public void recordParseWithSkipping(DI dataItem, LABEL candidate) {
		if (dataItem.getLabel().equals(candidate)) {
			LOG.info("%s stats -- recording correct parse with skipping: %s",
					getMetricName(), candidate);
			stats.recordSloppyCorrect(dataItem.getSample());
		} else if (dataItem.getRenamedExpressions().contains(candidate)){
			LOG.info("%s stats -- recording correct parse with skipping: %s",
					getMetricName(), candidate);
			stats.recordSloppyCorrect(dataItem.getSample()) ;
		}else {
			LOG.info("%s stats -- recording wrong parse with skipping: %s",
					getMetricName(), candidate);
			stats.recordSloppyIncorrect(dataItem.getSample());
		}
	}
	
	/** -- added by Candace --
	 * gets averages for comparing this run to others w/ different parameters
	 */
	public void recordAverages(DI dataItem, Integer cost) {
		// get number of predicates
		costs.put(dataItem.getLabel().toString(), cost);
		
		Map<LogicalConstant, Counter> map = GetPredicateCounts.of((LogicalExpression) dataItem.getLabel());
		Integer total = 0;
		for (Counter c : map.values()){
			total = total + c.value();
		}
		numPredicates.put(dataItem.getLabel().toString(), total);		
		
		// increment sum/costs and sum/predicate lengths
		running_sum_costs = running_sum_costs + cost;
		running_sum_lengths = running_sum_lengths + total;
	}
	
	/** -- added by Candace --
	 * gets averages for comparing this run to others w/ different parameters
	 */
	public void writeAveragesToFile(String filename, Integer size_test_data){
		
		BufferedWriter bw = null;
		try {
			bw = new BufferedWriter(new FileWriter(filename));
			
			//bw.write(String.format("avg/cost over avg/length: %s\n\n", ((double) running_sum_costs / running_sum_lengths)));
			bw.write(String.format("total_size_training_set: %s\n", size_test_data));
			bw.write(String.format("avg_cost_over_avg_length: %s\n", ((double) running_sum_costs / running_sum_lengths)));
			bw.write(String.format("num_annotated_sentences: " + (int) (stats.getCorrects() + stats.getFailures() + stats.getIncorrects())) + "\n");
			
			bw.write("sum_cost_over_length\n");
			for (String key : costs.keySet()){
				bw.write( String.format("%s\n%s %s %s\n",
						key,
						(double) costs.get(key) / numPredicates.get(key),
						costs.get(key),
						numPredicates.get(key)) );
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
	
	public void writeFinalAccuracy(String filename){
		BufferedWriter bw = null;
		try{
			bw = new BufferedWriter(new FileWriter(filename));
			bw.write("Final test precision: " + stats.precision() + "\n");
			bw.write("Final test recall: " + stats.recall() + "\n");
			bw.write("Final test F1: " + stats.f1() + "\n");
			
			bw.write("Final partial test precision: " + stats.partialPrecision() + "\n");
			bw.write("Final partial test recall: " + stats.partialRecall() + "\n");
			bw.write("Final partial test F1: " + stats.partialF1() + "\n");
			
			bw.write("Final sloppy test precision: " + stats.sloppyPrecision() + "\n");
			bw.write("Final sloppy test recall: " + stats.sloppyRecall() + "\n");
			bw.write("Final sloppy test F1: " + stats.sloppyF1() + "\n");
			
			System.out.println("Final test precision: " + stats.precision());
			System.out.println("Final test recall: " + stats.recall());
			System.out.println("Final test F1: " + stats.f1() + "\n");
			
			System.out.println("Final partial test precision: " + stats.partialPrecision());
			System.out.println("Final partial test recall: " + stats.partialRecall());
			System.out.println("Final partial test F1: " + stats.partialF1() + "\n");
			
			System.out.println("Final sloppy test precision: " + stats.sloppyPrecision());
			System.out.println("Final sloppy test recall: " + stats.sloppyRecall());
			System.out.println("Final sloppy test F1: " + stats.sloppyF1());
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

	public static class Creator<SAMPLE, LABEL, DI extends ILabeledDataItem<SAMPLE, LABEL>>
			implements
			IResourceObjectCreator<ExactMatchTestingStatistics<SAMPLE, LABEL, DI>> {

		private String	type;

		public Creator() {
			this("test.stats.exact");
		}

		public Creator(String type) {
			this.type = type;
		}

		@Override
		public ExactMatchTestingStatistics<SAMPLE, LABEL, DI> create(
				Parameters params, IResourceRepository repo) {
			
			
			return new ExactMatchTestingStatistics<SAMPLE, LABEL, DI>(
					params.get("prefix"), params.get("name",
							DEFAULT_METRIC_NAME));
		}

		@Override
		public String type() {
			return type;
		}

		@Override
		public ResourceUsage usage() {
			return ResourceUsage
					.builder(type, ExactMatchTestingStatistics.class)
					.addParam("prefix", String.class,
							"Prefix string used to identify this metric")
					.addParam(
							"name",
							String.class,
							"Metric name (default: " + DEFAULT_METRIC_NAME
									+ ")").build();
		}

	}

}
