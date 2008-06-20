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
package edu.cmu.cs.crystal.flow.examples;

import org.eclipse.jdt.core.dom.*;

import edu.cmu.cs.crystal.Crystal;
import edu.cmu.cs.crystal.flow.AnalysisDirection;
import edu.cmu.cs.crystal.flow.FlowAnalysisDefinition;
import edu.cmu.cs.crystal.flow.Lattice;

/**
 * Live Variable Flow Analysis Definition Example
 * 
 * @author David Dickey
 *
 */
public class LVFlowAnalysisDefinition extends FlowAnalysisDefinition<LVLatticeElement> {
	
	public LVFlowAnalysisDefinition(Crystal crystal) {
		super(crystal);
	}

	public String getName() {
		return "LVFlowAnalysis";
	}
	
	public AnalysisDirection setAnalysisDirection() {
		return AnalysisDirection.BACKWARD_ANALYSIS;
	}

	public Lattice<LVLatticeElement> getLattice(MethodDeclaration d) {
		return new Lattice<LVLatticeElement>(new LVLatticeElement(), new LVLatticeElement());
	}
	
	/**
	 * SimpleName transfer function.
	 * 
	 * Add all SimpleNames that are variables and NOT being assigned to right now.
	 * 
	 * @param node	the node
	 * @param value	the lattice representing the knowledge before the node
	 * @return	the lattice representing the knowledge after the node 
	 */
	public LVLatticeElement transfer(SimpleName node, LVLatticeElement value) {
		if(node.resolveBinding().getKind() != IBinding.VARIABLE)
			return value;
		if(node.isDeclaration())
			return value;
		ASTNode parent = node.getParent();
		if(parent != null) {
			if(parent.getNodeType() == ASTNode.SINGLE_VARIABLE_DECLARATION) {
				return value.removeVariable(node.resolveBinding());
			} else if (parent.getNodeType() == ASTNode.ASSIGNMENT
					|| parent.getNodeType() == ASTNode.VARIABLE_DECLARATION_FRAGMENT) {
				// We handle Assignments and VDF in their respective transfer() functions
				return value;
			}
		}
		return value.addVariable(node.resolveBinding());
	}
	
	/**
	 * VariableDeclarationFragment transfer function.
	 * 
	 * Remove the liveness from the Variable being declared.  Also
	 * add all other variables that are being referenced.
	 * 
	 * @param node	the node
	 * @param value	the lattice representing the knowledge before the node
	 * @return	the lattice representing the knowledge after the node 
	 */
	public LVLatticeElement transfer(VariableDeclarationFragment node, LVLatticeElement value) {
		SimpleName name = node.getName();
		// Remove binding for variable being declared
		value.removeVariable(name.resolveBinding());
		// Add bindings for all variables used in the initializer
		SimpleNameFinderVisitor snfv = new SimpleNameFinderVisitor(value);
		Expression expression = node.getInitializer();
		if(expression != null)
			expression.accept(snfv);
		return value;
	}
	
	/**
	 * Assignment transfer function.
	 * 
	 * Remove the liveness from the Variable being declared.  Also
	 * add all other variables that are being referenced.
	 * 
	 * @param node	the node
	 * @param value	the lattice representing the knowledge before the node
	 * @return	the lattice representing the knowledge after the node 
	 */
	public LVLatticeElement transfer(Assignment node, LVLatticeElement value) {	
		Expression lhs = node.getLeftHandSide();
		Expression rhs = node.getRightHandSide();
		// If it is a SimpleName on the LHS then remove binding
		if(lhs.getNodeType() == ASTNode.SIMPLE_NAME) {
			SimpleName name = (SimpleName)lhs;
			value.removeVariable(name.resolveBinding());
		} else {
			// Add bindings for all variables used in the lhs expression
			SimpleNameFinderVisitor snfv = new SimpleNameFinderVisitor(value);
			if(lhs != null)
				lhs.accept(snfv);
		}
		// Add bindings for all variables used in the initializer
		SimpleNameFinderVisitor snfv = new SimpleNameFinderVisitor(value);
		if(rhs != null)
			rhs.accept(snfv);		
		return value;
	}
	
	/**
	 * Go through and add all SimpleNames that are variables to the Lattice
	 * @author David Dickey
	 *
	 */
	class SimpleNameFinderVisitor extends ASTVisitor {
		LVLatticeElement lattice;
		public SimpleNameFinderVisitor(LVLatticeElement le) {
			lattice = le;
		}
		public boolean visit(SimpleName node) {
			if(node.resolveBinding().getKind() != IBinding.VARIABLE)
				return false;
			lattice.addVariable(node.resolveBinding());
			return false;
		}
	}
}
