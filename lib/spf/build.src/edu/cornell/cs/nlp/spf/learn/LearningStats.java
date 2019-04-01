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
package edu.cornell.cs.nlp.spf.learn;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import edu.cornell.cs.nlp.spf.data.ILabeledDataItem;
import edu.cornell.cs.nlp.utils.counter.Counter;
import edu.cornell.cs.nlp.utils.log.ILogger;
import edu.cornell.cs.nlp.utils.log.LoggerFactory;

import java.util.Set;
import java.util.stream.IntStream;

public class LearningStats<DI extends ILabeledDataItem<?, ?>>{
	public static final ILogger					LOG					= LoggerFactory
			.create(LearningStats.class);
	private static final String					DIGIT_STAT			= "###";
	private final Map<String, List<Counter>>	counters			= new HashMap<>();

	private final Map<String, Mean>				means				= new HashMap<String, Mean>();

	private final int							numSamples;
	final private String[][]					sampleStat;
	final private List<Integer>					sampleStatMaxLength	= new ArrayList<>();

	private final Map<String, String>			statDescription		= new HashMap<>();
	private final Set<String>					validStats;
	
	/** --start of added by Candace --
	 * these values are used to compute precision, recall, f1
	 */
	private final int								numEpochs;
	private final Map<Integer, Integer>				corrects = new HashMap<Integer, Integer>();
	private final Map<Integer, Integer>				incorrects = new HashMap<Integer, Integer>();
	private Map<Integer, Integer>					failures = new HashMap<Integer, Integer>();
	private final Map<String, Integer> 				costs = new HashMap<String, Integer>();
	private final Map<String, Integer> 				numPredicates = new HashMap<String, Integer>();
	private Integer running_sum_costs = 0;
	private Integer running_sum_lengths = 0;
	
	

	private LearningStats(int numSamples, Map<String, String> statLegend, int numEpochs) {
		this.numSamples = numSamples;
		this.validStats = new HashSet<>(statLegend.keySet());
		this.sampleStat = new String[numSamples][];
		this.numEpochs = numEpochs;
		
		// Initialize a map to aggregate sample statistics.
		for (final Entry<String, String> statEntry : statLegend.entrySet()) {
			statDescription.put(statEntry.getKey(),
					"[" + statEntry.getKey() + "] " + statEntry.getValue());
		}
		
		// initialize stats used for computing precision, recall and f1
		for (int i=0; i < numEpochs; i++){
			corrects.put(i, 0);
			incorrects.put(i, 0);
			failures.put(i, 0);
		}
	}
	
	/**
	 * @param epoch
	 * add new correct, incorrect or failure parse
	 */
	public final void addNewCorrectParse(Integer epoch){
		corrects.put(epoch, corrects.get(epoch)+1);
	}
	
	public final void addNewIncorrectParse(Integer epoch){
		incorrects.put(epoch, incorrects.get(epoch)+1);
	}

	public final void addNewFailureParse(Integer epoch){
		failures.put(epoch, failures.get(epoch)+1);
	}
	
	public final double getCorrects(Integer epoch) {
		return corrects.get(epoch);
	}
	
	public double getIncorrects(Integer epoch) {
		return incorrects.get(epoch);
	}
	
	public double getFailures(Integer epoch) {
		return failures.get(epoch);
	}	
	
	/**
	 * @param epochNumber
	 * compute precision, recall, f1
	 */
	public double getPrecision(Integer epoch) {
		final double total = getCorrects(epoch) + getIncorrects(epoch) + getFailures(epoch);
		final double precision =  total - failures.get(epoch) == 0.0 ? 0.0
													: corrects.get(epoch) / (total - failures.get(epoch));
		return precision;
	}
	
	public double getRecall(Integer epoch) {
		final double total = getCorrects(epoch) + getIncorrects(epoch) + getFailures(epoch);
		final double recall = total == 0 ? 0.0 : corrects.get(epoch) / total;
		return recall;
	}	
	
	public double getF1(Integer epoch) {
		final double precision = getPrecision(epoch);
		final double recall = getRecall(epoch);
		
		final double f1 = precision + recall == 0.0 ? 0.0  
				: ( 2 * precision * recall )  / ( precision + recall);
		return f1;
	}
	
	/**
	 * get average precision, recall and f1 across all epoch
	 * @return
	 */
	
	public double getAveragePrecision() {
		return IntStream.range(0, numEpochs-1).mapToDouble(epoch -> getPrecision(epoch)).sum() / numEpochs;
	}
	
	public double getAverageRecall() {
		return IntStream.range(0, numEpochs-1).mapToDouble(epoch -> getRecall(epoch)).sum() / numEpochs;
	}
	
	public double getAverageF1() {
		return IntStream.range(0, numEpochs-1).mapToDouble(epoch -> getF1(epoch)).sum() / numEpochs;
	}
	
	
	/* -------------------- added by Candace ----------------------------
	 * writing averages for comparing parameters
	 */
	public final void writeAveragesToFile(String filename, Integer size_training_data){
		try {
			final BufferedWriter bw = new BufferedWriter(new FileWriter(filename));
			bw.write(String.format("total size training set: %s\n", size_training_data));
			bw.write(String.format("avg cost over avg length: %s\n", ((double) running_sum_costs / running_sum_lengths)));
			bw.write(String.format("num annotated sentences: %s\n",
									(corrects.get(0) + incorrects.get(0) + failures.get(0))) );
			bw.write("sum cost over length" + " " + costs.size() + "\n");
			
			for (final String key : costs.keySet()){
				bw.write( String.format("%s\n%s %s %s\n",
										key,
										(double) costs.get(key) / numPredicates.get(key),
										costs.get(key),
										numPredicates.get(key)) );
			}
			bw.close();
		} catch (final IOException e){
			e.printStackTrace();
		}
	}

	public void appendSampleStat(int itemNumber, int iterationNumber, int value) {
		extendSampleList(itemNumber, iterationNumber);
		verifyStat(DIGIT_STAT);
		if (sampleStat[itemNumber][iterationNumber] == null) {
			sampleStat[itemNumber][iterationNumber] = "";
		}
		sampleStat[itemNumber][iterationNumber] += String.valueOf(value);
		updateSampleStatMaxLength(itemNumber, iterationNumber);

		// Aggregate counts. Only aggregate when the description is
		// pre-recorded.
		if (statDescription.containsKey(DIGIT_STAT)) {
			count(statDescription.get(DIGIT_STAT), value, iterationNumber);
		}
	}

	public void appendSampleStat(int itemNumber, int iterationNumber,
			String stat) {
		extendSampleList(itemNumber, iterationNumber);
		verifyStat(stat);
		if (sampleStat[itemNumber][iterationNumber] == null) {
			sampleStat[itemNumber][iterationNumber] = "";
		}
		sampleStat[itemNumber][iterationNumber] += stat;
		updateSampleStatMaxLength(itemNumber, iterationNumber);

		// Aggregate counts. Only aggregate when the description is
		// pre-recorded.
		if (statDescription.containsKey(stat)) {
			count(statDescription.get(stat), iterationNumber);
		}
	}

	public void count(String label, int iterationNumber) {
		verifyCounterExist(label, iterationNumber);
		counters.get(label).get(iterationNumber).inc();
	}

	public void count(String label, int value, int iterationNumber) {
		verifyCounterExist(label, iterationNumber);
		counters.get(label).get(iterationNumber).inc(value);
	}

	public double getMean(String label) {
		return means.containsKey(label) ? means.get(label).mean : 0.0;
	}

	public void mean(String label, double value, String unit) {
		final Mean aggregate = means.get(label);
		if (aggregate == null) {
			means.put(label, new Mean(unit, value));
		} else {
			assert unit.equals(aggregate.unit);
			aggregate.add(value);
		}
	}

	public void setSampleStat(int itemNumber, int iterationNumber,
			String stat) {
		extendSampleList(itemNumber, iterationNumber);
		verifyStat(stat);
		sampleStat[itemNumber][iterationNumber] = stat;
		updateSampleStatMaxLength(itemNumber, iterationNumber);

		// Aggregate counts. Only aggregate when the description is
		// pre-recorded.
		if (statDescription.containsKey(stat)) {
			count(statDescription.get(stat), iterationNumber);
		}
	}

	@Override
	public String toString() {
		final StringBuilder ret = new StringBuilder();

		final Iterator<Entry<String, Mean>> aggregateIterator = means.entrySet()
				.iterator();
		while (aggregateIterator.hasNext()) {
			final Entry<String, Mean> aggregateEntry = aggregateIterator.next();
			ret.append(String.format("Perfromend %d %s, mean: %.4f%s",
					aggregateEntry.getValue().count, aggregateEntry.getKey(),
					aggregateEntry.getValue().mean,
					aggregateEntry.getValue().unit));

			if (aggregateIterator.hasNext()) {
				ret.append("\n");
			}
		}

		for (final Entry<String, List<Counter>> counterArrayEntry : counters
				.entrySet()) {
			ret.append("\n");
			ret.append(counterArrayEntry.getKey()).append(": ");
			final int len = counterArrayEntry.getValue().size();
			for (int i = 0; i < len; ++i) {
				ret.append(counterArrayEntry.getValue().get(i).value());
				if (i + 1 < len) {
					ret.append(", ");
				}
			}
		}

		ret.append("\n");
		ret.append(
				String.format("Sample statistics (total: %d)\n", numSamples));
		final String sampleNumberFormat = "%1$-"
				+ String.valueOf(numSamples).length() + "s";
		for (int itemCounter = 0; itemCounter < numSamples; ++itemCounter) {
			ret.append(String.format(sampleNumberFormat,
					String.valueOf(itemCounter))).append(" :: ");
			if (sampleStat[itemCounter] != null) {
				for (int i = 0; i < sampleStat[itemCounter].length; ++i) {
					ret.append(String.format(
							"%1$-" + sampleStatMaxLength.get(i) + "s ",
							sampleStat[itemCounter][i] == null ? "-"
									: sampleStat[itemCounter][i]));
				}
			}
			ret.append('\n');
		}

		return ret.toString();
	}

	private void extendSampleList(int itemNumber, int iterationNumber) {
		if (sampleStat[itemNumber] == null) {
			sampleStat[itemNumber] = new String[iterationNumber + 1];
		} else if (sampleStat[itemNumber].length <= iterationNumber) {
			sampleStat[itemNumber] = Arrays.copyOf(sampleStat[itemNumber],
					iterationNumber + 1);
		}
	}

	private void updateSampleStatMaxLength(int itemNumber,
			int iterationNumber) {
		while (sampleStatMaxLength.size() <= iterationNumber) {
			sampleStatMaxLength.add(0);
		}
		if (sampleStat[itemNumber][iterationNumber]
				.length() > sampleStatMaxLength.get(iterationNumber)) {
			sampleStatMaxLength.set(iterationNumber,
					sampleStat[itemNumber][iterationNumber].length());
		}
	}

	private void verifyCounterExist(String label, int iterationNumber) {
		if (!counters.containsKey(label)) {
			counters.put(label, new ArrayList<>());
		}
		final List<Counter> list = counters.get(label);
		while (list.size() <= iterationNumber) {
			list.add(new Counter(0));
		}
	}

	private void verifyStat(String stat) {
		if (!validStats.contains(stat)) {
			LOG.error("Unknown stat: %s", stat);
		}
	}

	public static class Builder<DI extends ILabeledDataItem<?, ?>> {

		private final int					numSamples;
		private final Map<String, String>	statLegend	= new HashMap<String, String>();
		private final int					numEpochs;

		public Builder(int numSamples, int numEpochs) {
			this.numSamples = numSamples;
			this.numEpochs = numEpochs;
		}

		public Builder<DI> addStat(String stat, String description) {
			statLegend.put(stat, description);
			return this;
		}

		public LearningStats<DI> build() {
			return new LearningStats<DI>(numSamples, statLegend, numEpochs);
		}

		public Builder<DI> setNumberStat(String description) {
			statLegend.put(DIGIT_STAT, description);
			return this;
		}
	}

	private class Mean {
		int				count;
		double			mean;
		final String	unit;

		public Mean(String unit, double value) {
			this.unit = unit;
			this.mean = value;
			this.count = 1;
		}

		public void add(double value) {
			mean = (mean * count + value) / ++count;
		}
	}

}
