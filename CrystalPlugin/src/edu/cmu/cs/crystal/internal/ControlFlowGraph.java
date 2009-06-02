/**
 * Copyright (c) 2006, 2007, 2008 Marwan Abi-Antoun, Jonathan Aldrich, Nels E. Beckman,
 * Kevin Bierhoff, David Dickey, Ciera Jaspan, Thomas LaToza, Gabriel Zenarosa, and others.
 *
 * This file is part of Crystal.
 *
 * Crystal is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Crystal is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Crystal.  If not, see <http://www.gnu.org/licenses/>.
 */
package edu.cmu.cs.crystal.internal;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.core.dom.ASTNode;

public class ControlFlowGraph {
	/**
	 * stored mappings between ASTNodes and ControlFlowNodes
	 */
	protected static Map<ASTNode, ControlFlowNode> controlFlowNodes = null;
	/**
	 * Adds the mapping from ASTNode to ControlFlowNode for later lookup
	 * @param astNode	the ASTNode
	 * @param cfn	the ControlFlowNode
	 */
	public static void addControlFlowNode(ASTNode astNode, ControlFlowNode cfn) {
		if(controlFlowNodes == null)
			controlFlowNodes = new HashMap<ASTNode, ControlFlowNode>();
		controlFlowNodes.put(astNode, cfn);
	}
	/**
	 * Removes a mapping from the ASTNode to ControlFlowNode map.  It is
	 * important to remove mappings that get removed from the CFG.
	 * @param astNode	the node to remove
	 */
	public static void removeControlFlowNode(ASTNode astNode) {
		if(controlFlowNodes == null || astNode == null)
			return;
		controlFlowNodes.remove(astNode);
	}
	/**
	 * Retrieves the ControlFlowNode that is associated with the ASTNode.
	 * If the ASTNode does not have a direct CFN mapping, then find a
	 * parent node that does.  Also responsible for caching.
	 * 
	 * @param inNode	the ASTNode
	 * @return	the ControlFlowNode, or null if a CFG was never created.
	 */
	public static ControlFlowNode getControlFlowNode(ASTNode inNode) {
		if(controlFlowNodes == null)
			return null;
		if(inNode == null)
			throw new CrystalRuntimeException("null ASTNode argument");
		ASTNode node = inNode;
		ControlFlowNode cfn;
		// Find the CFN associated with this ASTNode
		while(node != null) {
			if(controlFlowNodes.containsKey(node)) {
				cfn = controlFlowNodes.get(node);
				// Cache the discovered knowledge
				if(node != inNode)
					addControlFlowNode(node, cfn);
				return cfn;
			}
			node = node.getParent();
		}
		return null;
	}

	/**
	 * the start dummy node in the CFG
	 */
	protected ControlFlowNode startNode = null;
	/**
	 * the end dummy node in the CFG
	 */
	protected ControlFlowNode endNode = null;
	
	/**
	 * Constructor.  Initializes an empty CFG.
	 */
	public ControlFlowGraph(ASTNode node) {
		if(node == null)
			throw new CrystalRuntimeException("null node");
		// Create the ControlFlowNodes
		startNode = new ControlFlowNode(this, null);
		endNode = new ControlFlowNode(this, null);
		ControlFlowNode cfn = startNode.newControlFlowNode(node);
		// Connect them (start -> node -> end)
		startNode.addEdge(ControlFlowNode.Direction.FORWARDS, cfn);
		cfn.addEdge(ControlFlowNode.Direction.FORWARDS, endNode);
		// Have the CFN evaluate itself further 
		cfn.evaluate();
	}
	/**
	 * Retrieves the start dummy node for the graph
	 * @return	the starting node
	 */
	public ControlFlowNode getStartNode() {
		return startNode;
	}
	/**
	 * Retrieves the end dummy node for the graph
	 * @return	the end node
	 */
	public ControlFlowNode getEndNode() {
		return endNode;
	}
	
	/**
	 * Creates a short textual representation of the CFG.
	 * (Currently only returns "A CFG")
	 * 
	 * @return	a string representing this CFG
	 */
	public String toString() {
		// TODO: Make this more meaningful
		return "A CFG";
	}
	/**
	 * Generates a set of all nodes in the CFG
	 * @return	the set of all nodes in the CFG
	 */
	public Set<ControlFlowNode> getNodeSet() {
		Set<ControlFlowNode> set = new HashSet<ControlFlowNode>();
		if(startNode != null)
			buildNodeList(startNode, set);
		return set;
	}
	/**
	 * A recursive helper method to getNodeSet().  This method
	 * performs the CFG traversal to generate the set. 
	 * @param cfn	the node to add & recursively visit children
	 * @param set	the set to add all nodes into
	 */
	private void buildNodeList(ControlFlowNode cfn, Set<ControlFlowNode> set) {
		Iterator<ControlFlowNode> i = cfn.getIterator(ControlFlowNode.Direction.FORWARDS);
		ControlFlowNode next;
		for(;i.hasNext();) {
			next = i.next();
			if(set.contains(next))
				continue;
			else {
				set.add(next);
				buildNodeList(next, set);
			}
		}
	}	
}

