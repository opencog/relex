/*
 * Copyright 2008 Novamente LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package relex.stats;

import java.lang.Math;

/**
 * This class provides simple historgram (bin count) support.
 *
 * Copyright (C) 2008 Linas Vepstas <linas@linas.org>
 */
public class Histogram implements TruthValue
{
	// strict histograming stuff
	private int nbins;
	private double min_value;
	private double max_value;
	private double rate;
	private double delta;
	private int[] bins;
	int underflow;
	int overflow;

	boolean integer_bins;

	double all_time_high;
	double all_time_low;

	double cnt;
	double sum;
	double sumsq;

	private void init(int _nbins, double low, double high)
	{
		nbins = _nbins;
		min_value = low;
		max_value = high;
		underflow = 0;
		overflow = 0;
		bins = new int[nbins];
		for (int i=0; i<nbins; i++)
		{
			bins[i] = 0;
		}

		integer_bins = false;
		rate = nbins / (max_value - min_value);
		delta = 1.0/rate;

		all_time_high = -1.0e38;
		all_time_low = +1.0e38;
	}

	public Histogram(int _nbins, double low, double high)
	{
		init(_nbins, low, high);
	}

	public Histogram(int binmin, int binmax)
	{
		init(binmax-binmin, (double) binmin, (double) binmax);
		integer_bins = true;
	}

	public void bin(double value)
	{
		int b = (int) Math.floor (rate * (value - min_value));

		if (b < 0)
		{
			underflow ++;
		}
		else if (b >= nbins)
		{
			overflow ++;
		}
		else
		{
			bins[b] ++;
		}

		if (value < all_time_low) all_time_low = value;
		if (value > all_time_high) all_time_high = value;

		cnt ++;
		sum += value;
		sumsq += value*value;
	}

	public double getCount()
	{
		return cnt;
	}

	public double getMean()
	{
		return sum / ((double) cnt);
	}

	public double getStdDev()
	{
		double var = sumsq / ((double) cnt);
		double exp = getMean();
		var -= exp*exp;
		return Math.sqrt(var);
	}

	public double getConfidence()
	{
		return 1.0;
	}

	public double getAllTimeLow()
	{
		return all_time_low;
	}

	public double getAllTimeHigh()
	{
		return all_time_high;
	}

	public int getOverflow()
	{
		return overflow;
	}
	public int getUnderflow()
	{
		return underflow;
	}

	public double getMedian()
	{
		int half = (int) (cnt / 2.0);
		int lt = underflow;
		if (lt >= half) return min_value;

		double midpoint = 0.5;
		if (integer_bins) midpoint = 0.0;
		for (int i = 0; i<nbins; i++)
		{
			lt += bins[i];
			if (lt >= half) return (((double)i) + midpoint) * delta + min_value;
		}
		return max_value;
	}

	public double getMode()
	{
		int pcnt = underflow;
		int peak = -1;
		for (int i = 0; i<nbins; i++)
		{
			if (bins[i] > pcnt)
			{
				peak = i;
				pcnt = bins[i];
			}
		}
		double midpoint = 0.5;
		if (integer_bins) midpoint = 0.0;
		return (((double)peak) + midpoint) * delta + min_value;
	}

}

