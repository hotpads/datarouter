package io.datarouter.util.iterable.scanner;

import org.testng.Assert;
import org.testng.annotations.Test;

import io.datarouter.util.iterable.scanner.Scanner;

public class ScannerTests{

	@Test
	public void testAdvanceBy(){
		Scanner<Integer> scanner = new TestScanner();
		Assert.assertTrue(scanner.advanceBy(10));
		Assert.assertEquals(scanner.getCurrent().intValue(), 10);
		Assert.assertFalse(scanner.advanceBy(1));
	}

	private static class TestScanner implements Scanner<Integer>{

		private Integer current = 0;

		@Override
		public Integer getCurrent(){
			return current;
		}

		@Override
		public boolean advance(){
			if(current == 10){
				return false;
			}
			current++;
			return true;
		}

	}

}
