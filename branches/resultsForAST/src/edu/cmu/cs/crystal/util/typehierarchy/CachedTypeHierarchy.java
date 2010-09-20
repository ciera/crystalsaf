package edu.cmu.cs.crystal.util.typehierarchy;

import java.util.HashMap;
import java.util.HashSet;

import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeHierarchy;
import org.eclipse.jdt.core.JavaModelException;

import edu.cmu.cs.crystal.util.TypeHierarchy;

public class CachedTypeHierarchy implements TypeHierarchy {
	private HashMap<String, TypeNode> types;
	private IJavaProject project;
	
	public CachedTypeHierarchy(IJavaProject project) throws JavaModelException {
		this.project = project;
		types = new HashMap<String, TypeNode>();
		loadNewTree("java.lang.Object");
		defaults();
	}
	
	public boolean existsCommonSubtype(String t1, String t2) {
		return existsCommonSubtype(t1, t2, false, false);
	}
	
	public boolean existsCommonSubtype(String t1, String t2, boolean skipCheck1, boolean skipCheck2) {
		if (t1.equals("java.lang.Object") || t2.equals("java.lang.Object"))
			return true;
		
		int genStart1 = t1.indexOf('<');
		int genStart2 = t2.indexOf('<');
		String type1, gen1, type2, gen2;
		
		if (genStart1 != -1) {
			type1 = t1.substring(0, genStart1);
			gen1 = t1.substring(genStart1 + 1, t1.lastIndexOf('>'));
		}
		else {
			type1 = t1;
			gen1 = "";
		}
		if (genStart2 != -1) {
			type2 = t2.substring(0, genStart2);
			gen2 = t2.substring(genStart2 + 1, t2.lastIndexOf('>'));
		}
		else {
			type2 = t2;
			gen2 = "";
		}
		
		TypeNode node1 = getOrCreateType(type1);
		TypeNode node2 = getOrCreateType(type2);

		if (node1 == null || node2 == null)
			return false;
		
		if (!skipCheck1 && isSubtypeCompatible(type1, type2)) {
			return existsCommonSubtypeGenerics(gen1, gen2);
		}
		
		if (!skipCheck2 && isSubtypeCompatible(type2, type1)) {
			return existsCommonSubtypeGenerics(gen1, gen2);
		}
	
		if (!node1.isCompleted())
			loadNewTree(type1);
		if (!node2.isCompleted())
			loadNewTree(type2);

		HashSet<String> t1Subs = new HashSet<String>();
		HashSet<String> t2Subs = new HashSet<String>();
		
		node1.collectAllSubs(t1Subs);
		node2.collectAllSubs(t2Subs);
		
		for (String sub : t1Subs) {
			if (t2Subs.contains(sub))
				return existsCommonSubtypeGenerics(gen1, gen2);
		}
		return false;
	}

	/**
	 * Get the node out of the type map. If it doesn't exist, then
	 * create it!
	 */
	private TypeNode getOrCreateType(String qualifiedName) {
		TypeNode node = types.get(qualifiedName);
		if (node == null) {
			node = new TypeNode(qualifiedName);
			types.put(qualifiedName, node);
		}
		return node;
	}

	/**
	 * @param gen1 An empty or comma separated list of generics
	 * @param gen2 An empty or comma separated list of generics
	 * @return
	 */
	private boolean existsCommonSubtypeGenerics(String list1, String list2) {
		if (list1.equals("") || list2.equals(""))
			return true;
		
		String[] gens1 = list1.split(",");
		String[] gens2 = list2.split(",");
		
		if (gens1.length != gens2.length)
			return false;
		
		for (int ndx = 0; ndx < gens1.length; ndx++) {
			if (!existsCommonSubtype(gens1[ndx].trim(), gens2[ndx].trim()))
				return false;
		}
		return true;	
	}

	public boolean isSubtypeCompatible(String subType, String superType) {
		if (superType.equals("java.lang.Object"))
			return true;
		
		int genStartSub = subType.indexOf('<');
		int genStartSuper = superType.indexOf('<');
		String subTypeName, genSub, superTypeName, genSuper;
		
		if (genStartSub != -1) {
			subTypeName = subType.substring(0, genStartSub);
			genSub = subType.substring(genStartSub + 1, subType.lastIndexOf('>'));
		}
		else {
			subTypeName = subType;
			genSub = "";
		}
		if (genStartSuper != -1) {
			superTypeName = superType.substring(0, genStartSuper);
			genSuper = superType.substring(genStartSuper + 1, superType.lastIndexOf('>'));
		}
		else {
			superTypeName = superType;
			genSuper = "";
		}

		TypeNode subNode = getOrCreateType(subTypeName);
		TypeNode superNode = getOrCreateType(superTypeName);
		
		if (subNode == null || superNode == null)
			return false;
		
		if (subNode.isCompleted() && superNode.isCompleted())
			return subNode.isSupertype(superNode) && isSubtypeCompatibleGenerics(genSub, genSuper);
		else if (subNode.isSupertype(superNode))
				return isSubtypeCompatibleGenerics(genSub, genSuper);
		else {
			//not necessarily false, but might not be complete. So complete them both and try again!
			if (!subNode.isCompleted())
				loadNewTree(subTypeName);
			if (!superNode.isCompleted())
				loadNewTree(superTypeName);
			return subNode.isSupertype(superNode) && isSubtypeCompatibleGenerics(genSub, genSuper);
			
		}
		
	}
	
	/**
	 * @param gen1 An empty or comma separated list of generics
	 * @param gen2 An empty or comma separated list of generics
	 * @return
	 */
	private boolean isSubtypeCompatibleGenerics(String listSub, String listSuper) {
		if (listSuper.equals(""))
			return true;
		if (listSub.equals(""))
			return false;

		String[] genSub = listSub.split(",");
		String[] genSuper = listSuper.split(",");	
		
		if (genSub.length != genSuper.length)
			return false;
		
		for (int ndx = 0; ndx < genSub.length; ndx++) {
			if (!isSubtypeCompatible(genSub[ndx].trim(), genSuper[ndx].trim()))
				return false;
		}
		return true;	
	}

	
	/**
	 * 
	 * @param qName
	 * @param doClasses
	 * @return the new typeNode for this type, fully completed
	 */
	private void loadNewTree(String qName) {
		try {
			IType baseType = project.findType(qName);
			
			if (baseType != null) {
				ITypeHierarchy hierarchy = baseType.newTypeHierarchy(null);		
				addInHierarchy(baseType, hierarchy);
				
				//Yeah...that just wasted a bunch of resources. Clean up now...
				Runtime r = Runtime.getRuntime();
				r.gc();

				//This node is complete!
				TypeNode node = types.get(qName);
				node.completed();
			}
		} catch (JavaModelException e) {
			//can't really do anything...
			e.printStackTrace();
		}
	}
	
	private void addInHierarchy(IType type, ITypeHierarchy hierarchy) throws JavaModelException {
		String qName = type.getFullyQualifiedName('.');
		TypeNode node = getOrCreateType(qName);
		
		//Recurse on children
		for (IType sub : hierarchy.getSubtypes(type)) {
			String subName = sub.getFullyQualifiedName('.');
			TypeNode subNode = getOrCreateType(subName);
			
			if (!subNode.isSupertype(node)) { //missing the connection
				node.addSubtype(subNode);
				subNode.addSupertype(node);
				addInHierarchy(sub, hierarchy);
			}
			//and if we already have the connection, we also have them all the way down.
		}

		//Recurse on parents
		for (IType sup : hierarchy.getAllSupertypes(type)) {
			String supName = sup.getFullyQualifiedName('.');
			TypeNode supNode = getOrCreateType(supName);
			
			if (!node.isSupertype(supNode)) { //missing the connection
				supNode.addSubtype(node);
				node.addSupertype(supNode);
				addInHierarchy(sup, hierarchy);
			}
			//and if we already have the connection, we also have them all the way up.
		}
	}


	private void defaults() {
		TypeNode intNode = new TypeNode("int");
		TypeNode shortNode = new TypeNode("short");
		TypeNode longNode = new TypeNode("long");
		TypeNode charNode = new TypeNode("char");
		TypeNode boolNode = new TypeNode("boolean");
		TypeNode doubleNode = new TypeNode("double");
		TypeNode floatNode = new TypeNode("float");
		TypeNode voidNode = new TypeNode("void");
		
		types.put("void", voidNode);
		types.put("char", charNode);
		types.put("short", shortNode);
		types.put("int", intNode);
		types.put("long", longNode);
		types.put("boolean", boolNode);
		types.put("float", floatNode);
		types.put("double", doubleNode);
		
		shortNode.addSubtype(charNode);
		charNode.addSupertype(shortNode);
		
		intNode.addSubtype(shortNode);
		intNode.addSubtype(charNode);
		charNode.addSupertype(intNode);
		shortNode.addSupertype(intNode);
		
		longNode.addSubtype(shortNode);
		longNode.addSubtype(intNode);
		longNode.addSubtype(charNode);
		charNode.addSupertype(longNode);
		shortNode.addSupertype(longNode);
		intNode.addSupertype(longNode);
		
		doubleNode.addSubtype(floatNode);
		floatNode.addSupertype(doubleNode);

		floatNode.addSubtype(longNode);
		longNode.addSupertype(floatNode);
	}
}
