package edu.cornell.cs.nlp.spf.mr.lambda.visitor;

public class TupleConstantCount {

	private final String constant;
	private final Integer count;
	
	public TupleConstantCount(String constant, Integer count){
		this.constant = constant;
		this.count = count;
	}
	
	public String getConstant(){
		return constant;
	}
	
	public Integer getCount(){
		return count;
	}
}
