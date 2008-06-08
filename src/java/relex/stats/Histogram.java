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
	private int[] bins;
	int underflow;
	int overflow;

	public Histogram(int _nbins, double low, double high)
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
		double delta = 1.0 / rate;
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
}

