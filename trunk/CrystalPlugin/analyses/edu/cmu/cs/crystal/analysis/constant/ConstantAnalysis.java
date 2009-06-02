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
package edu.cmu.cs.crystal.analysis.constant;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.AssertStatement;
import org.eclipse.jdt.core.dom.DoStatement;
import org.eclipse.jdt.core.dom.EnhancedForStatement;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.WhileStatement;

import edu.cmu.cs.crystal.AbstractCrystalMethodAnalysis;
import edu.cmu.cs.crystal.flow.BooleanLabel;
import edu.cmu.cs.crystal.flow.IResult;
import edu.cmu.cs.crystal.flow.ITACFlowAnalysis;
import edu.cmu.cs.crystal.internal.CrystalRuntimeException;
import edu.cmu.cs.crystal.simple.TupleLatticeElement;
import edu.cmu.cs.crystal.tac.ITACBranchSensitiveTransferFunction;
import edu.cmu.cs.crystal.tac.TACFlowAnalysis;
import edu.cmu.cs.crystal.tac.TACInstruction;
import edu.cmu.cs.crystal.tac.Variable;


public class ConstantAnalysis extends AbstractCrystalMethodAnalysis {

	private ITACFlowAnalysis<TupleLatticeElement<Variable, BooleanConstantLE>> fa;

	public ConstantAnalysis() {
		super();
	}
	
	public boolean hasPreciseValueAfter(Variable var, ASTNode node, boolean after) {
		BooleanConstantLE value;	
		if (after)
			value = fa.getResultsAfter(node).get(var);
		else
			value = fa.getResultsBefore(node).get(var);
		return value.equals(BooleanConstantLE.TRUE) || value.equals(BooleanConstantLE.FALSE);
	}
	
	public boolean getValue(Variable var, ASTNode node, boolean after) {
		BooleanConstantLE value;	
		if (after)
			value = fa.getResultsAfter(node).get(var);
		else
			value = fa.getResultsBefore(node).get(var);
		
		if (value.equals(BooleanConstantLE.TRUE))
			return true;
		else if (value.equals(BooleanConstantLE.FALSE))
			return false;
		else
			throw new CrystalRuntimeException("Can not get the constant value as it is not precise.");
	}

	@Override
	public void analyzeMethod(MethodDeclaration d) {
		ITACBranchSensitiveTransferFunction<TupleLatticeElement<Variable, BooleanConstantLE>> tf = new ConstantTransferFunction();
		fa = new TACFlowAnalysis<TupleLatticeElement<Variable, BooleanConstantLE>>(tf,
				this.analysisInput.getComUnitTACs().unwrap());
	
		// must call getResultsAfter at least once on this method,
		// or the analysis won't be run on this method
		TupleLatticeElement<Variable, BooleanConstantLE> finalLattice = fa.getResultsAfter(d);
		printLattice(finalLattice);
		d.accept(new DeadBranchChecker());
	}

	private void printLattice(TupleLatticeElement<Variable, BooleanConstantLE> lattice) {
		for (Variable var : lattice.getKeySet()) {
			BooleanConstantLE bool = lattice.get(var);
			if (bool != BooleanConstantLE.BOTTOM)
			reporter.debugOut().println(var.getSourceString() + ":" + bool.toString());
		}		
	}


	public TupleLatticeElement<Variable, BooleanConstantLE> getResultsBefore(
			TACInstruction instr) {
		return fa.getResultsBefore(instr.getNode());
	}

	public TupleLatticeElement<Variable, BooleanConstantLE> getResultsAfter(
			TACInstruction instr) {
		return fa.getResultsAfter(instr);
	}
	
	/**
	 * @author ciera
	 * @since Crystal 3.4.0
	 */
	public class DeadBranchChecker extends ASTVisitor {

		@Override
		public void endVisit(AssertStatement node) {
			IResult<TupleLatticeElement<Variable, BooleanConstantLE>> lattices = fa.getLabeledResultsAfter(node.getExpression());
			
			Variable tempVar = fa.getVariable(node.getExpression());
			TupleLatticeElement<Variable, BooleanConstantLE> tLattice = lattices.get(BooleanLabel.getBooleanLabel(true));
			TupleLatticeElement<Variable, BooleanConstantLE> fLattice = lattices.get(BooleanLabel.getBooleanLabel(false));
			
			if (tLattice.get(tempVar) == BooleanConstantLE.FALSE)
				reporter.reportUserProblem("The assert will never be popped.", node, getName());
			else if (fLattice.get(tempVar) == BooleanConstantLE.TRUE)
				reporter.reportUserProblem("The assert will always be popped.", node, getName());
		}

		@Override
		public void endVisit(DoStatement node) {
			IResult<TupleLatticeElement<Variable, BooleanConstantLE>> lattices = fa.getLabeledResultsAfter(node.getExpression());
			
			Variable tempVar = fa.getVariable(node.getExpression());
			TupleLatticeElement<Variable, BooleanConstantLE> tLattice = lattices.get(BooleanLabel.getBooleanLabel(true));
			TupleLatticeElement<Variable, BooleanConstantLE> fLattice = lattices.get(BooleanLabel.getBooleanLabel(false));
			
			if (tLattice.get(tempVar) == BooleanConstantLE.FALSE)
				reporter.reportUserProblem("The loop will only be run once.", node, getName());
			else if (fLattice.get(tempVar) == BooleanConstantLE.TRUE)
				reporter.reportUserProblem("The loop is infinite.", node, getName());
		}

		@Override
		public void endVisit(EnhancedForStatement node) {
			IResult<TupleLatticeElement<Variable, BooleanConstantLE>> lattices = fa.getLabeledResultsAfter(node.getExpression());
			
			Variable tempVar = fa.getVariable(node.getExpression());
			TupleLatticeElement<Variable, BooleanConstantLE> tLattice = lattices.get(BooleanLabel.getBooleanLabel(true));
			TupleLatticeElement<Variable, BooleanConstantLE> fLattice = lattices.get(BooleanLabel.getBooleanLabel(false));
			
			if (tLattice.get(tempVar) == BooleanConstantLE.FALSE)
				reporter.reportUserProblem("The loop will never be entered.", node, getName());
			else if (fLattice.get(tempVar) == BooleanConstantLE.TRUE)
				reporter.reportUserProblem("The loop is infinite.", node, getName());
		}

		@Override
		public void endVisit(ForStatement node) {
			IResult<TupleLatticeElement<Variable, BooleanConstantLE>> lattices = fa.getLabeledResultsAfter(node.getExpression());
			
			Variable tempVar = fa.getVariable(node.getExpression());
			TupleLatticeElement<Variable, BooleanConstantLE> tLattice = lattices.get(BooleanLabel.getBooleanLabel(true));
			TupleLatticeElement<Variable, BooleanConstantLE> fLattice = lattices.get(BooleanLabel.getBooleanLabel(false));
			
			if (tLattice.get(tempVar) == BooleanConstantLE.FALSE)
				reporter.reportUserProblem("The loop will never be entered.", node, getName());
			else if (fLattice.get(tempVar) == BooleanConstantLE.TRUE)
				reporter.reportUserProblem("The loop is infinite.", node, getName());
		}
		
		@Override
		public void endVisit(IfStatement node) {
			IResult<TupleLatticeElement<Variable, BooleanConstantLE>> lattices = fa.getLabeledResultsAfter(node.getExpression());
			
			Variable tempVar = fa.getVariable(node.getExpression());
			TupleLatticeElement<Variable, BooleanConstantLE> tLattice = lattices.get(BooleanLabel.getBooleanLabel(true));
			TupleLatticeElement<Variable, BooleanConstantLE> fLattice = lattices.get(BooleanLabel.getBooleanLabel(false));
			
			if (tLattice.get(tempVar) == BooleanConstantLE.FALSE)
				reporter.reportUserProblem("The else branch will always be taken.", node, getName());
			else if (fLattice.get(tempVar) == BooleanConstantLE.TRUE)
				reporter.reportUserProblem("The then branch will always be taken.", node, getName());
		}

		@Override
		public void endVisit(WhileStatement node) {
			IResult<TupleLatticeElement<Variable, BooleanConstantLE>> lattices = fa.getLabeledResultsAfter(node.getExpression());
			
			Variable tempVar = fa.getVariable(node.getExpression());
			TupleLatticeElement<Variable, BooleanConstantLE> tLattice = lattices.get(BooleanLabel.getBooleanLabel(true));
			TupleLatticeElement<Variable, BooleanConstantLE> fLattice = lattices.get(BooleanLabel.getBooleanLabel(false));
			
			if (tLattice.get(tempVar) == BooleanConstantLE.FALSE)
				reporter.reportUserProblem("The loop will never be entered.", node, getName());
			else if (fLattice.get(tempVar) == BooleanConstantLE.TRUE)
				reporter.reportUserProblem("The loop is infinite.", node, getName());
		}

	}

}
