/*
 * Some utilities for loading csv data into a PosgtreSQL database:
 * detect file encoding, CSV format and populate database
 *
 *     Copyright (C) 2016, 2018 J. Férard <https://github.com/jferard>
 *
 * This file is part of pgLoader Utils.
 *
 * pgLoader Utils is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * pgLoader Utils is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.github.jferard.pgloaderutils.csvsniffer;

/**
 * Compute mean and variance of an array of int values.
 */
class StatisticsBasic {
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
