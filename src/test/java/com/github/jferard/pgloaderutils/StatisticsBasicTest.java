package com.github.jferard.pgloaderutils;

import org.junit.Assert;
import org.junit.Test;

public class StatisticsBasicTest {

	@Test
	public final void test() {
		StatisticsBasic statisticsBasic = new StatisticsBasic(new int[] {1,2,3});
		Assert.assertEquals(2.0, statisticsBasic.getMean(), 1e-10);
		Assert.assertEquals(2.0/3.0, statisticsBasic.getVariance(), 1e-10);
	}

	/**
	 * @see http://www.vitutor.com/statistics/descriptive/variance_problems.html
	 */
	@Test
	public final void test2() {
		StatisticsBasic statisticsBasic = new StatisticsBasic(new int[] {12, 6, 7, 3, 15, 10, 18, 5});
		Assert.assertEquals(9.5, statisticsBasic.getMean(), 1e-10);
		Assert.assertEquals(23.75, statisticsBasic.getVariance(), 1e-10);
		
		statisticsBasic = new StatisticsBasic(new int[] {2, 3, 6, 8, 11});
		Assert.assertEquals(6, statisticsBasic.getMean(), 1e-10);
		Assert.assertEquals(10.8, statisticsBasic.getVariance(), 1e-10);
		
		statisticsBasic = new StatisticsBasic(new int[] {3, 5, 2, 7, 6, 4, 9});
		Assert.assertEquals(5.143, statisticsBasic.getMean(), 1e-3);
		Assert.assertEquals(4.979, statisticsBasic.getVariance(), 1e-3);
		
		statisticsBasic = new StatisticsBasic(new int[] {3, 5, 2, 7, 6, 4, 9, 1});
		Assert.assertEquals(4.625, statisticsBasic.getMean(), 1e-3);
		Assert.assertEquals(6.234, statisticsBasic.getVariance(), 1e-3);
	}

}
