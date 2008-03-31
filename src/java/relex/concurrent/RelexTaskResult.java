package relex.concurrent;

import relex.RelexInfo;
import relex.entity.EntityMaintainer;

public class RelexTaskResult implements Comparable<RelexTaskResult> {
	Integer index;
	String sentence; 
	RelexInfo result;
	EntityMaintainer entityMaintainer;
	
	public RelexTaskResult(int index, String sentence, EntityMaintainer entityMaintainer, RelexInfo ri){
		this.index = index;
		this.sentence = sentence;
		this.entityMaintainer = entityMaintainer;
		this.result = ri;
	}
	
	public int compareTo(RelexTaskResult that) {
		return this.index.compareTo(that.index);
	}
	
	public String toString(){
		return index+": "+sentence+"\n"+result+"\n";
	}
}