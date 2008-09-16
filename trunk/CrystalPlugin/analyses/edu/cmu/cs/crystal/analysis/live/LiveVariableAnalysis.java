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
import org.eclipse.jdt.core.dom.MethodDeclaration;

import edu.cmu.cs.crystal.AbstractCrystalMethodAnalysis;
import edu.cmu.cs.crystal.flow.TupleLatticeElement;
import edu.cmu.cs.crystal.internal.Utilities;
import edu.cmu.cs.crystal.tac.ITACTransferFunction;
import edu.cmu.cs.crystal.tac.TACFlowAnalysis;
import edu.cmu.cs.crystal.tac.Variable;

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
}
