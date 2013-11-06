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

/**
 * This class provides a simple OpenCog-like TruthValue object.
 * It is similar to, but not the same as, OpenCog TV objects.
 *
 * Copyright (C) 2008 Linas Vepstas <linas@linas.org>
 */
public class SimpleTruthValue implements TruthValue
{
	private double count;
	private int offset;
	private double mean;
	private static final double PLUS_EPSILON = 1.0 + 1.0E-6;

	public SimpleTruthValue()
	{
		count = 0.0;
		mean = 0.0;
		offset = 10;
	}

	public SimpleTruthValue(double m, double conf)
	{
		mean = m;
		offset = 10;
		setConfidence(conf);
	}

	public double getMean()
	{
		return mean;
	}

	public void setMean(double m)
	{
		mean = m;
	}

	public void setMean(double m, double conf)
	{
		mean = m;
		setConfidence(conf);
	}

	public double getCount()
	{
		return count;
	}

	public double getConfidence()
	{
		return PLUS_EPSILON * count/(count+offset);
	}

	public void setConfidence(double cnf)
	{
		// The goal of EPSILON is to allow cnf=1.0
		// without causing a divide-by-zero.
		if (1.0 < cnf) cnf = 1.0;
		count =  offset*cnf / (PLUS_EPSILON - cnf);
	}

	public void setCount(int cnt)
	{
		count = cnt;
	}

	public void setOffset(int off)
	{
		offset = off;
	}

	public String toString()
	{
		Double m = mean;
		String ms = m.toString();
		if (6 < ms.length()) ms = ms.substring(0,6);

		Double c = getConfidence();
		String cs = c.toString();
		if (6 < cs.length()) cs = cs.substring(0,6);

		String str = "(" + ms + ", " + cs + ")";
		return str;
	}
}

