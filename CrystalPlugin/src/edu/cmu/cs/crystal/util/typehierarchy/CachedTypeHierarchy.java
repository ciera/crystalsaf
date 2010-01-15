package edu.cmu.cs.crystal.util.typehierarchy;

import java.util.HashMap;
import java.util.HashSet;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeHierarchy;
import org.eclipse.jdt.core.JavaModelException;

import edu.cmu.cs.crystal.util.TypeHierarchy;

public class CachedTypeHierarchy implements TypeHierarchy {
	private HashMap<String, TypeNode> types;
	private IProgressMonitor monitor;
	private IJavaProject project;
	
	public CachedTypeHierarchy(IJavaProject project, IProgressMonitor monitor) throws JavaModelException {
		this.monitor = monitor;
		this.project = project;
		types = new HashMap<String, TypeNode>();
		loadInitialTree();
		defaults();
	}
	
	public boolean existsCommonSubtype(String t1, String t2) {
		return existsCommonSubtype(t1, t2, false, false);
	}
	
	public boolean existsCommonSubtype(String t1, String t2, boolean skipCheck1, boolean skipCheck2) {
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
		
		TypeNode node1 = types.get(type1);
		if (node1 == null) {
			loadNewTree(type1);
			node1 = types.get(type1);
		}

		TypeNode node2 = types.get(type2);
		if (node2 == null) {
			loadNewTree(type2);
			node2 = types.get(type2);
		}

		if (node1 == null || node2 == null)
			return false;
		
		if (!skipCheck1 && isSubtypeCompatible(type1, type2)) {
			return existsCommonSubtypeGenerics(gen1, gen2);
		}
		
		if (!skipCheck2 && isSubtypeCompatible(type2, type1)) {
			return existsCommonSubtypeGenerics(gen1, gen2);
		}
	
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

		TypeNode subNode = types.get(subTypeName);
		
		if (subNode == null) {
			loadNewTree(subTypeName);
			subNode = types.get(subTypeName);
		}

		TypeNode superNode = types.get(superTypeName);

		if (superNode == null) {
			loadNewTree(superTypeName);
			superNode = types.get(superTypeName);
		}
		
		if (subNode == null || superNode == null)
			return false;
		return subNode.isSupertype(superNode) && isSubtypeCompatibleGenerics(genSub, genSuper);
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

	
	private void loadInitialTree() {
		try {
			IType baseType = project.findType("java.lang.Object");
			
			if (baseType != null) {
				ITypeHierarchy hierarchy = baseType.newTypeHierarchy(monitor);
				
				for (IType root : hierarchy.getRootClasses())
					buildHierarchy(root, hierarchy);
				for (IType root : hierarchy.getRootInterfaces())
					buildHierarchy(root, hierarchy);
				
				Runtime r = Runtime.getRuntime();
				r.gc();
			}
		} catch (JavaModelException e) {
			//can't really do anything...
			e.printStackTrace();
		}
	}
	
	/**
	 * Build down the type hierarchy. Will return fast if this type already exists in our hierarchy.
	 * @param type The type to look down from
	 * @param hierarchy the hierarchy we are looking in
	 * @return A node for this type which exists in types.
	 * @throws JavaModelException 
	 */
	private TypeNode buildHierarchy(IType type, ITypeHierarchy hierarchy) throws JavaModelException {
		String qName = type.getFullyQualifiedName('.');
		TypeNode node = types.get(qName);
		
		if (node == null) {
			node = new TypeNode(qName);
			
			//always go down
			for (IType sub : hierarchy.getSubtypes(type)) {
				TypeNode subNode = buildHierarchy(sub, hierarchy);
				subNode.addSupertype(node);
				node.addSubtype(subNode);
			}
			//once were in types, we're guaranteed to have all the children
			//that are currently known
			types.put(qName, node);
		}
		return node;
	}
	
	private void loadNewTree(String qName) {
		try {
			IType baseType = project.findType(qName);
			
			if (baseType != null) {
				ITypeHierarchy hierarchy = baseType.newTypeHierarchy(monitor);
				
				//classes were already taken care of
				//so just do interfaces
				for (IType root : hierarchy.getRootInterfaces())
					addInHierarchy(root, hierarchy);
				Runtime r = Runtime.getRuntime();
				r.gc();

			}
		} catch (JavaModelException e) {
			//can't really do anything...
			e.printStackTrace();
		}
	}
	
	private void addInHierarchy(IType type, ITypeHierarchy hierarchy) throws JavaModelException {
		String qName = type.getFullyQualifiedName('.');
		TypeNode node = types.get(qName);
		boolean firstCreate = false;
		
		if (node == null) {
			node = new TypeNode(qName);
			//store first, and then have children build connections on the way back up
			types.put(qName, node);
			firstCreate = true;
		}
		
		//Since this was called, we can presume that at least some children need to be created.
		//Go get those children, and recurse.
			
		for (IType sub : hierarchy.getSubtypes(type)) {
			addInHierarchy(sub, hierarchy);
		}

		//if this node is being created for the first time, it needs to create the upward
		//connections.
		if (firstCreate) {
			for (IType inter : hierarchy.getSupertypes(type)) {
				if (inter.isClass()) //classes are already take care of
					continue;
				TypeNode interNode = types.get(inter.getFullyQualifiedName('.'));
				//if we don't visit it now, we'll get it on the next time we come back...
				if (interNode != null) {
					node.addSupertype(interNode);
					interNode.addSubtype(node);
				}
			}
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
