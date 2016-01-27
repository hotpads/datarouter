package com.hotpads.datarouter.batch.math;

public class PolynomialRegressionOptimumFinderPoint{

	private Integer abscissa;
	private Integer ordinate;

	public PolynomialRegressionOptimumFinderPoint(Integer abscissa, Integer ordinate){
		this.abscissa = abscissa;
		this.ordinate = ordinate;
	}

	public Integer getAbscissa(){
		return abscissa;
	}

	public Integer getOrdinate(){
		return ordinate;
	}

}
