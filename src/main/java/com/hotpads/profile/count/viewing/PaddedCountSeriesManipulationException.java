package com.hotpads.profile.count.viewing;

public class PaddedCountSeriesManipulationException extends Exception{

	private static final long serialVersionUID = 1L;

	public PaddedCountSeriesManipulationException(PaddedCountSeriesTypeException type){
		switch(type){
		case INTERVAL:
			System.out.println("The interval of the count series does not corssponds to the paddedCountSeries interval");
			break;
		case WRONG_START_TIME:
			System.out.println("One of your count does not have a startime corresponding to the period");
			break;

		default:
			break;
		}
	}

	public enum PaddedCountSeriesTypeException{
		INTERVAL, WRONG_START_TIME;
	}
}
