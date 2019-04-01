package edu.cornell.cs.nlp.spf.learn;

import edu.cornell.cs.nlp.spf.data.IDataItem;
import edu.cornell.cs.nlp.spf.parser.ccg.model.Model;

/**
 * 
 * For use in weakly supervised learning with grounded examples.
 * Classes that implement this learning use properties for grounding.
 * 
 * @param <SAMPLE>
 *            Inference sample
 * @param <DI>
 *            Learning data item, includes the inference sample, and,
 *            potentially, some supervision.
 * @param <MODEL>
 *            Type of model
 */
public interface ILearnerGrounded<SAMPLE extends IDataItem<?>, DI extends IDataItem<SAMPLE>, MODEL extends Model<SAMPLE, ?>> {
	void train(MODEL model);
}

