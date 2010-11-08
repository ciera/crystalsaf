package edu.cmu.cs.crystal.util.typehierarchy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

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

		if (t1.equals("void") || t2.equals("void"))
			return false;

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
		
		List<List<Integer>> bracketPairs1 = bracketIndices(list1);
		List<List<Integer>> bracketPairs2 = bracketIndices(list2);
		
		List<String> gen1 = commaSplit(list1, bracketPairs1);
		List<String> gen2 = commaSplit(list2, bracketPairs2);
				
		if (gen1.size() != gen2.size())
			return false;
		
		for (int ndx = 0; ndx < gen1.size(); ndx++) {
			if (!existsCommonSubtype(gen1.get(ndx).trim(), gen2.get(ndx).trim()))
				return false;
		}
		return true;
	}

	public boolean isSubtypeCompatible(String subType, String superType) {
		if (superType.equals("java.lang.Object"))
			return true;
		
		if (subType.equals("void") || superType.equals("void"))
			return false;
		
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

		List<List<Integer>> bracketPairsSub = bracketIndices(listSub);
		List<List<Integer>> bracketPairsSuper = bracketIndices(listSuper);
		
		List<String> genSub = commaSplit(listSub, bracketPairsSub);
		List<String> genSuper = commaSplit(listSuper, bracketPairsSuper);
				
		if (genSub.size() != genSuper.size())
			return false;
		
		for (int ndx = 0; ndx < genSub.size(); ndx++) {
			if (!isSubtypeCompatible(genSub.get(ndx).trim(), genSuper.get(ndx).trim()))
				return false;
		}
		return true;	
	}

	private List<List<Integer>> bracketIndices(String s) {
		List<List<Integer>> pairsOfIndices = new ArrayList<List<Integer>>();
		Stack<Integer> stack = new Stack<Integer>(); // stack of open brackets
		int current = 0;
		while (current < s.length()) {
			int openIndex = s.indexOf('<', current);
			int closeIndex = s.indexOf('>', current);
			if (openIndex > 0 && closeIndex > 0) { // both found
				if (openIndex < closeIndex) { // open comes first
					stack.push(openIndex);
					current = openIndex+1;
				} else { // close comes first
					List<Integer> pair = new LinkedList<Integer>();
					pair.add(stack.pop()); // open
					pair.add(closeIndex); // close
					pairsOfIndices.add(pair);
					current = closeIndex+1;
				}
			} else if (closeIndex > 0) { // only close found
				List<Integer> pair = new LinkedList<Integer>();
				pair.add(stack.pop()); // open
				pair.add(closeIndex); // close
				pairsOfIndices.add(pair);
				current = closeIndex+1;
			} else if (openIndex > 0) { // only open found --> something went wrong
				throw new RuntimeException("Error while parsing generics: "+s);
			} else { // no (more) brackets found
				assert(stack.isEmpty());
				break;
			}
		}
		return pairsOfIndices;
	}
	
	/**
	 * Split at commas, but exclude commas in the given ranges.
	 * @return
	 */
	private List<String> commaSplit(String s, List<List<Integer>> excludedRanges) {
		List<Integer> commas = new ArrayList<Integer>();
		int current = 0;
		while (current < s.length()) {
		 	int index = s.indexOf(',', current);
		 	if (index < 0) break;
		 	boolean excluded = false;
		 	for (List<Integer> pair : excludedRanges) {
		 		if (index >= pair.get(0) & index <= pair.get(1)) {
		 			excluded = true;
		 			break;
		 		}
			}
		 	if (!excluded) commas.add(index);
		 	current = index+1;
		}
		
		List<String> splitted = new ArrayList<String>();
		current = 0;
		for (Integer commaIndex : commas) {
			splitted.add(s.substring(current, commaIndex));
			current = commaIndex+1;
		}
		splitted.add(s.substring(current, s.length()));
		
		return splitted;
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
				ITypeHierarchy hierarchy = baseType.newTypeHierarchy(project, null);		
				addInHierarchy(baseType, hierarchy);
				
				for (IType iType : hierarchy.getRootInterfaces())
					addInHierarchy(iType, hierarchy);
				
				
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
		
		if (node.isCompleted())
			return;
		
		//Recurse on children
		for (IType sub : hierarchy.getSubtypes(type)) {
			String subName = sub.getFullyQualifiedName('.');
			TypeNode subNode = getOrCreateType(subName);
			
			if (!subNode.isDirectSupertype(node)) { //missing the connection
				node.addSubtype(subNode);
				subNode.addSupertype(node);
				addInHierarchy(sub, hierarchy);
			}
			//and if we already have the connection, we also have them all the way down.
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
