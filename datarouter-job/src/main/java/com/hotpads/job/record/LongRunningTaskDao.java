package com.hotpads.job.record;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.hotpads.datarouter.node.op.combo.IndexedSortedMapStorage;
import com.hotpads.job.trigger.Job;
import com.hotpads.util.core.collections.Range;

@Singleton
public class LongRunningTaskDao{
	private final IndexedSortedMapStorage<LongRunningTaskKey,LongRunningTask> node;

	@Inject
	public LongRunningTaskDao(LongRunningTaskNodeProvider nodeProvider){
		this.node = nodeProvider.get();
	}

	//gets previous job runs (from fromInstant to job's current task)
	public List<LongRunningTask> getPreviousRunsFromInstant(Job job, Instant fromInstant){
		LongRunningTaskKey currentKey = job.getLongRunningTaskTracker().getTask().getKey();
		Range<LongRunningTaskKey> range = new Range<>(new LongRunningTaskKey(currentKey.getJobClass(), Date.from(
				fromInstant)), true, currentKey, false);
		List<LongRunningTask> runs = node.stream(range, null).collect(Collectors.toList());
		return runs;
	}

	//find the oldest run AFTER/SINCE the most recent SUCCESS
	public static LongRunningTask getOldestRunSinceLastSuccess(List<LongRunningTask> runs){
		LongRunningTask oldestRunSinceLastSuccess = null;
		for(int i = runs.size() - 1; i >= 0; i--){
			if(JobExecutionStatus.SUCCESS == runs.get(i).getJobExecutionStatus()){
				break;
			}
			oldestRunSinceLastSuccess = runs.get(i);
		}
		return oldestRunSinceLastSuccess;
	}

	public static LongRunningTask getLastSuccess(List<LongRunningTask> runs){
		for(int i = runs.size() - 1; i >= 0; i--){
			if(JobExecutionStatus.SUCCESS == runs.get(i).getJobExecutionStatus()){
				return runs.get(i);
			}
		}
		return null;
	}

	public static class LongRunningTaskDaoUnitTests{
		@Test
		public void testGetOldestRunSinceLastSuccess(){
			List<LongRunningTask> tasks = new ArrayList<>();
			//null when empty
			Assert.assertNull(LongRunningTaskDao.getOldestRunSinceLastSuccess(tasks));

			LongRunningTask first = new LongRunningTask();
			tasks.add(first);
			first.setJobExecutionStatus(JobExecutionStatus.SUCCESS);
			//only one item, which is SUCCESS, so return null
			Assert.assertNull(LongRunningTaskDao.getOldestRunSinceLastSuccess(tasks));
			first.setJobExecutionStatus(JobExecutionStatus.INTERRUPTED);
			//one item, not SUCCESS, so return it
			Assert.assertEquals(LongRunningTaskDao.getOldestRunSinceLastSuccess(tasks), first);

			LongRunningTask second = new LongRunningTask();
			tasks.add(second);
			second.setJobExecutionStatus(JobExecutionStatus.SUCCESS);
			//most recent item is SUCCESS, so null
			Assert.assertNull(LongRunningTaskDao.getOldestRunSinceLastSuccess(tasks));
			second.setJobExecutionStatus(JobExecutionStatus.RUNNING);
			//all items are not SUCCESS, so return oldest one
			Assert.assertEquals(LongRunningTaskDao.getOldestRunSinceLastSuccess(tasks), first);
			first.setJobExecutionStatus(JobExecutionStatus.SUCCESS);
			//only one item since last SUCCESS, so return it
			Assert.assertEquals(LongRunningTaskDao.getOldestRunSinceLastSuccess(tasks), second);
		}
	}
}
