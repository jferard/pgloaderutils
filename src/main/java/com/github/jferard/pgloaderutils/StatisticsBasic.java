package com.github.jferard.pgloaderutils;

public class StatisticsBasic {
	private int[] values;
	private double mean;
	private double variance;
	private int size;
	private boolean hasVariance;
	private boolean hasMean;

	public StatisticsBasic(int[] values) {
		this.values = values;
		this.hasMean = false;
		this.hasVariance = false;
	}

	double getMean() {
		if (!this.hasMean) {
			double sum = 0.0;
			int count = 0;
			for (int value : this.values) {
				sum += value;
				count++;
			}
			this.size = count;
			this.mean = sum / this.size;
			this.hasMean = true;
		}
		return this.mean;
	}

	double getVariance() {
		if (!this.hasVariance) {
			double mean = this.getMean();
			double sum = 0;
			for (int value : this.values) {
				double delta = mean - value;
				sum += delta * delta;
			}
			this.variance = sum / this.size;
			this.hasVariance = true;
		}
		return this.variance;
	}
}