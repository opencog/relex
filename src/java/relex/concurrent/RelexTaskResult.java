package relex.concurrent;

import java.io.Serializable;

import relex.RelexInfo;
import relex.entity.EntityMaintainer;

public class RelexTaskResult implements Comparable<RelexTaskResult>, Serializable {
	
	private static final long serialVersionUID = -3231030217056826602L;
	
	public Integer index;
	public String sentence; 
	public RelexInfo result;
	public EntityMaintainer entityMaintainer;
	
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