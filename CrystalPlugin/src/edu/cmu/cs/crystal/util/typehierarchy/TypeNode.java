package edu.cmu.cs.crystal.util.typehierarchy;

import java.util.HashSet;
import java.util.Set;

public class TypeNode {
	private Set<TypeNode> subTypes;
	private Set<TypeNode> superTypes;
	private String qualifiedName;
	private boolean isCompleted;
	
	public TypeNode(String name) {
		qualifiedName = name;
		superTypes = new HashSet<TypeNode>();
		subTypes = new HashSet<TypeNode>();
		isCompleted = false;
	}
	
	/**
	 * 
	 * @return True if this type knows all of its subtypes, false otherwise.
	 */
	public boolean isCompleteDown() {return isCompleted;}
	
	/**
	 * To be called when this type knows all of its subtypes.
	 */
	public void completedDown() {isCompleted = true;}
	
	public boolean isDirectSupertype(TypeNode superNode) {
		return superTypes.contains(superNode);
	}

	public boolean isSupertype(TypeNode superNode) {
		if (this == superNode)
			return true;
		for (TypeNode directSuper : superTypes) {
			if (directSuper.isSupertype(superNode))
				return true;
		}
		return false;
	}

	public void addSubtype(TypeNode sub) {
		subTypes.add(sub);
	}

	public void addSupertype(TypeNode superNode) {
		superTypes.add(superNode);
	}

	/**
	 * Recursively adds all the subtypes of this node to the subs set
	 * @param subs An out parameter, must be initialized
	 */
	public void collectAllSubs(HashSet<String> subs) {
		subs.add(qualifiedName);
		for (TypeNode subNode : subTypes)
			subNode.collectAllSubs(subs);
	}
	
	public String toString() {return qualifiedName;}

}
