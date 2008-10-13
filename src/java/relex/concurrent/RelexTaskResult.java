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

package relex.concurrent;

import java.io.Serializable;

import relex.Sentence;
import relex.entity.EntityMaintainer;

public class RelexTaskResult implements Comparable<RelexTaskResult>, Serializable {
	
	private static final long serialVersionUID = -3231030217056826602L;
	
	public Integer index;
	public String sentence; 
	public Sentence result;
	public EntityMaintainer entityMaintainer;
	
	public RelexTaskResult(int index, String sentence,
	                       EntityMaintainer entityMaintainer,
	                       Sentence sntc)
	{
		this.index = index;
		this.sentence = sentence;
		this.entityMaintainer = entityMaintainer;
		this.result = sntc;
	}
	
	public int compareTo(RelexTaskResult that) {
		return this.index.compareTo(that.index);
	}
	
	public String toString(){
		return index+": "+sentence+"\n"+result+"\n";
	}
}
