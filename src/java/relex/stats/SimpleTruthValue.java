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
 * Perhaps someday, it should be refectored to resemble OpenCog
 * more.
 *
 * Copyright (C) 2008 Linas Vepstas <linas@linas.org> 
 */
public class SimpleTruthValue implements TruthValue
{
	private int count;
	private double sum;
	private double sum_squared;

	public SimpleTruthValue()
	{
		count = 0;
		sum = 0.0;
		sum_squared = 0.0;
	}

	public double getMean()
	{
		if (count <= 0) return 0; 
		return sum/count;
	}

	public double getCount()
	{
		return count;
	}

	public double getConfidence()
	{
		if (count <= 0) return 0.0;

		// XXX FIXME
		// A total hack, right now -- the invese RMS value.
		double rms = (sum_squared - sum*sum/count) / count;
		return 1.0/rms;
	}
}

