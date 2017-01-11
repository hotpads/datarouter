package com.hotpads.util.core.date;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;

import org.testng.Assert;
import org.testng.annotations.Test;

public class DatarouterCronExpression{

	private final CronExpression cronExpresssion;

	public DatarouterCronExpression(String cronExpression) throws ParseException{
		this.cronExpresssion = new CronExpression(cronExpression);
	}

	public int countExecutionsBetween(Date start, Date end){
		Date nextProcessDate = cronExpresssion.getTimeAfter(start);
		int count = 0;
		while(nextProcessDate.before(end)){
			count++;
			nextProcessDate = cronExpresssion.getTimeAfter(nextProcessDate);
		}
		return count;
	}

	public CronExpression getCronExpression(){
		return this.cronExpresssion;
	}

	/****************** tests ********************************************/
	public static class DatarouterCronExpressionTests{

		@Test
		public void testCountExecutionsBetween() throws ParseException{
			DatarouterCronExpression cronExpression = new DatarouterCronExpression("0 0 0 ? * *");
			Calendar startCalendar = Calendar.getInstance();
			startCalendar.set(Calendar.HOUR_OF_DAY, 0);
			startCalendar.set(Calendar.MINUTE, 0);
			startCalendar.set(Calendar.SECOND, 0);
			startCalendar.set(Calendar.MILLISECOND, 0);
			Date start = startCalendar.getTime();

			Calendar endCalendar = Calendar.getInstance();
			endCalendar.setTime(start);
			endCalendar.add(Calendar.DATE, 1);
			Date end = endCalendar.getTime();

			Assert.assertEquals(cronExpression.countExecutionsBetween(start, end), 0);

			startCalendar.add(Calendar.SECOND, -1);
			start = startCalendar.getTime();
			Assert.assertEquals(cronExpression.countExecutionsBetween(start, end), 1);

			endCalendar.add(Calendar.SECOND, 1);
			end = endCalendar.getTime();
			Assert.assertEquals(cronExpression.countExecutionsBetween(start, end), 2);

			// back to midnight - 1 second to next midnight
			endCalendar.add(Calendar.SECOND, -1);
			end = endCalendar.getTime();
			cronExpression = new DatarouterCronExpression("0 0 10,20 ? * *");
			Assert.assertEquals(cronExpression.countExecutionsBetween(start, end), 2);

			cronExpression = new DatarouterCronExpression("0 10,20 5,6 ? * *");
			Assert.assertEquals(cronExpression.countExecutionsBetween(start, end), 4);

			cronExpression = new DatarouterCronExpression("0 15 3/3 ? * *");
			Assert.assertEquals(cronExpression.countExecutionsBetween(start, end), 7);

			cronExpression = new DatarouterCronExpression("0 20 9-12 ? * *");
			Assert.assertEquals(cronExpression.countExecutionsBetween(start, end), 4);

			cronExpression = new DatarouterCronExpression("0 30 * ? * *");
			Assert.assertEquals(cronExpression.countExecutionsBetween(start, end), 24);
		}
	}
}
