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
package edu.cmu.cs.crystal.analysis.live;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Name;

import edu.cmu.cs.crystal.AbstractCrystalMethodAnalysis;
import edu.cmu.cs.crystal.IAnalysisReporter.SEVERITY;
import edu.cmu.cs.crystal.simple.TupleLatticeElement;
import edu.cmu.cs.crystal.tac.ITACTransferFunction;
import edu.cmu.cs.crystal.tac.TACFlowAnalysis;
import edu.cmu.cs.crystal.tac.model.Variable;
import edu.cmu.cs.crystal.util.Utilities;

public class LiveVariableAnalysis extends AbstractCrystalMethodAnalysis
{

	public static LiveVariableAnalysis Instance;
	
	private TACFlowAnalysis<TupleLatticeElement<Variable, LiveVariableLE>> fa;

	
	public LiveVariableAnalysis() {
		Instance = this;
	}
	
	
	public boolean isLiveBefore(Variable var, ASTNode node) 
	{
		// If the node is located in a field initializer (no surrounding method), no results exist for it.  In this case,
		// we default to live.
		if (Utilities.getMethodDeclaration(node) == null)
			return true;
		else
			return fa.getResultsBefore(node).get(var) == LiveVariableLE.LIVE;
	}

	@Override
	public void analyzeMethod(MethodDeclaration d) {
		ITACTransferFunction<TupleLatticeElement<Variable, LiveVariableLE>> tf = new LiveVariableTransferFunction();
		fa = new TACFlowAnalysis<TupleLatticeElement<Variable, LiveVariableLE>>(tf,
				this.analysisInput.getComUnitTACs().unwrap());
	
		// must call getResultsAfter at least once on this method, or the analysis won't be run on this method			
		TupleLatticeElement<Variable, LiveVariableLE> finalLattice = fa.getResultsBefore(d);
		d.accept(new LiveVariableVisitor());
	}

	public void printLattice(TupleLatticeElement<Variable, LiveVariableLE> lattice) {
		for (Variable var : lattice.getKeySet()) {
			LiveVariableLE live = lattice.get(var);
			if (live == LiveVariableLE.LIVE)
				reporter.debugOut().println(var.getSourceString() + ":" + " LIVE  ");		
			else
				reporter.debugOut().println(var.getSourceString() + ":" + " DEAD  ");
		}
		reporter.debugOut().println("\n\n");
	}
	
	/**
	 * @author ciera
	 * @since Crystal 3.4.0
	 */
	public class LiveVariableVisitor extends ASTVisitor {

		@Override
		public void endVisit(Assignment node) {
			IVariableBinding binding = null;
			Expression left = node.getLeftHandSide();
			TupleLatticeElement<Variable, LiveVariableLE> lattice = fa.getResultsAfter(node);
			
			if (left instanceof Name && ((Name)left).resolveBinding() instanceof IVariableBinding)
				binding = (IVariableBinding) ((Name)left).resolveBinding();
			else if (left instanceof FieldAccess)
				binding = ((FieldAccess)left).resolveFieldBinding();
			
			if (binding == null)
				return;
			
			if (binding.isField() || binding.isParameter())
				return;
			
			if (lattice.get(fa.getSourceVariable(binding)) == LiveVariableLE.DEAD)
				reporter.reportUserProblem("The variable " + node.getLeftHandSide() + " is dead and is no longer used.", node, getName(), SEVERITY.WARNING);
		}
	}
}
