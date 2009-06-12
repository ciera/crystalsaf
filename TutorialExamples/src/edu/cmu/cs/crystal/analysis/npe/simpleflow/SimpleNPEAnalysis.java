/**
 * Copyright (c) 2006-2009 Marwan Abi-Antoun, Jonathan Aldrich, Nels E. Beckman,    
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
package edu.cmu.cs.crystal.analysis.npe.simpleflow;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.ArrayAccess;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.QualifiedName;

import edu.cmu.cs.crystal.AbstractCrystalMethodAnalysis;
import edu.cmu.cs.crystal.IAnalysisReporter.SEVERITY;
import edu.cmu.cs.crystal.simple.SimpleTACFlowAnalysis;
import edu.cmu.cs.crystal.simple.TupleLatticeElement;
import edu.cmu.cs.crystal.tac.model.Variable;

/**
 * A simple flow analysis. This analysis still walks the tree, but it
 * first checks the flow analysis results to see if it can guarantee that
 * the value is or is not null.
 * 
 * @author ciera
 */
public class SimpleNPEAnalysis extends AbstractCrystalMethodAnalysis {
	SimpleTACFlowAnalysis<TupleLatticeElement<Variable, NullLatticeElement>> flowAnalysis;

	@Override
	public String getName() {
		return "Simple NPE Flow";
	}

	@Override
	public void analyzeMethod(MethodDeclaration d) {
		NPESimpleTransferFunction tf = new NPESimpleTransferFunction();
		flowAnalysis = new SimpleTACFlowAnalysis<TupleLatticeElement<Variable, NullLatticeElement>>(tf, getInput());
		
		d.accept(new NPEFlowVisitor());
	}

	/**
	 * The visitor for the analysis.
	 * @author ciera
	 */
	public class NPEFlowVisitor extends ASTVisitor {

		private void checkVariable(TupleLatticeElement<Variable, NullLatticeElement> tuple, Expression nodeToCheck) {
			Variable varToCheck = flowAnalysis.getVariable(nodeToCheck);
			NullLatticeElement element = tuple.get(varToCheck);
			
			if (element == NullLatticeElement.MAYBE_NULL)
				getReporter().reportUserProblem("The expression " + nodeToCheck + " may be null.", nodeToCheck, getName(), SEVERITY.WARNING);		
			else if (element == NullLatticeElement.NULL)
				getReporter().reportUserProblem("The expression " + nodeToCheck + "is null.", nodeToCheck, getName(), SEVERITY.ERROR);		

		}

		@Override
		public void endVisit(ArrayAccess node) {
			TupleLatticeElement<Variable, NullLatticeElement> beforeTuple = flowAnalysis.getResultsBefore(node);
			
			checkVariable(beforeTuple, node.getArray());
		}


		@Override
		public void endVisit(FieldAccess node) {
			TupleLatticeElement<Variable, NullLatticeElement> beforeTuple = flowAnalysis.getResultsBefore(node);
			if (node.getExpression() != null)
				checkVariable(beforeTuple, node.getExpression());
		}

		@Override
		public void endVisit(MethodInvocation node) {
			TupleLatticeElement<Variable, NullLatticeElement> beforeTuple = flowAnalysis.getResultsBefore(node);
			if (node.getExpression() != null)
			checkVariable(beforeTuple, node.getExpression());
		}

		@Override
		public void endVisit(QualifiedName node) {
			//Due to an ambiguity within the parser, a qualified name may actually be a FieldAccess.
			//To check for this, see what the binding is.
			if (node.resolveBinding() instanceof IVariableBinding) {
				//now we know it's field access.
				TupleLatticeElement<Variable, NullLatticeElement> beforeTuple = flowAnalysis.getResultsBefore(node);
				
				checkVariable(beforeTuple, node.getQualifier());
			}
		}
	}

}
