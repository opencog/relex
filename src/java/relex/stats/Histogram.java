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
	private int nbins;
	private double min_value;
	private double max_value;
	private double rate;
	private double delta;
	private int[] bins;
	int underflow;
	int overflow;

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

		rate = nbins / (max_value - min_value);
		delta = 1.0 / rate;
	}

	public Histogram(int _nbins, double low, double high)
	{
		init(_nbins, low, high);
	}

	public Histogram(int binmin, int binmax)
	{
		init(binmax-binmin, (double) binmin, (double) binmax);
	}

	public void bin(double value)
	{
		int b = (int) Math.floor (rate * (value - min_value));

		if (b < 0)
		{
			underflow ++;
			return;
		}
		if (b >= nbins)
		{
			overflow ++;
			return;
		}
		bins[b] ++;
	}

	public double getCount()
	{
		int cnt = 0;
		for (int i=0; i<nbins; i++)
		{
			cnt += bins[i];
		}
		return cnt;
	}

	public double getMean()
	{
		int cnt = 0;
		double avg = 0.0;
		for (int i=0; i<nbins; i++)
		{
			avg += delta * (i + 0.5) * bins[i];
			cnt += bins[i];
		}
		return (avg / ((double) cnt)) + min_value;
	}

	public double getConfidence()
	{
		return 1.0;
	}

	public double getLowestBin()
	{
		if (underflow != 0) return min_value;
		for (int i=0; i<nbins; i++)
		{
			if (bins[i] != 0)
			{
				return ((double) i) * delta + min_value;
			}
		}
		return max_value;
	}

	public double getHighestBin()
	{
		if (overflow != 0) return max_value;
		for (int i=nbins-1; i>=0; i--)
		{
			if (bins[i] != 0)
			{
				return ((double) i+1) * delta + min_value;
			}
		}
		return min_value;
	}

	public int getOverflow()
	{
		return overflow;
	}
	public int getUnderflow()
	{
		return underflow;
	}
}

