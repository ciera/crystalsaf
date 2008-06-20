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

import java.io.PrintWriter;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.SimpleName;

import edu.cmu.cs.crystal.AbstractCrystalMethodAnalysis;
import edu.cmu.cs.crystal.flow.FlowAnalysis;
import edu.cmu.cs.crystal.internal.Utilities;


/**
 * Live Variables Analysis Example
 *  
 * @author David Dickey
 *
 */
public class LVAnalysis extends AbstractCrystalMethodAnalysis {

	public String getName() {
		return "LVAnalysis";
	}

	public void analyzeMethod(MethodDeclaration md) {
		PrintWriter output = crystal.userOut();
		
		output.println(getName() + " analyzeMethod() [" + md.getName() + "]");
		
		// Initialize the FlowAnalysis objects
		LVFlowAnalysisDefinition lvfad = new LVFlowAnalysisDefinition(crystal);
		FlowAnalysis<LVLatticeElement> fa = new FlowAnalysis<LVLatticeElement>(crystal, lvfad);

		output.println("\nResults of Live Variables Analysis");
		LVResultsVisitor visitor = new LVResultsVisitor(fa);
		md.accept(visitor);
	}
	
	class LVResultsVisitor extends ASTVisitor {
		protected FlowAnalysis<LVLatticeElement> flowAnalysis;
		protected PrintWriter output = crystal.userOut();

		LVResultsVisitor(FlowAnalysis<LVLatticeElement> fa) {
			flowAnalysis = fa;
		}
		
		public void preVisit(ASTNode node) {
			// Show results of nodes that we are interested in.
			switch(node.getNodeType()) {
			case ASTNode.SIMPLE_NAME:
				if(((SimpleName)node).resolveBinding().getKind() != IBinding.VARIABLE)
					return;
			case ASTNode.VARIABLE_DECLARATION_FRAGMENT:
			case ASTNode.ASSIGNMENT:
			case ASTNode.INFIX_EXPRESSION:
				output.println("Node: " + Utilities.ASTNodeToString(node));
				LVLatticeElement lvgrb = flowAnalysis.getResultsBefore(node);
				LVLatticeElement lvgra = flowAnalysis.getResultsAfter(node);
				output.println("\tgetResultsBefore: " + lvgrb);
				output.println("\t getResultsAfter: " + lvgra);
			}
		}
		public void postVisit(ASTNode node) {
		}
	}
}
