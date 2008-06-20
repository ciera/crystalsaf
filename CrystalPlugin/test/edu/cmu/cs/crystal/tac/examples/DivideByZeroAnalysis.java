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
package edu.cmu.cs.crystal.tac.examples;

import java.util.HashMap;

import org.eclipse.jdt.core.dom.MethodDeclaration;

import edu.cmu.cs.crystal.AbstractCrystalMethodAnalysis;
import edu.cmu.cs.crystal.flow.Lattice;
import edu.cmu.cs.crystal.flow.TupleLatticeElement;
import edu.cmu.cs.crystal.tac.AbstractingTransferFunction;
import edu.cmu.cs.crystal.tac.AssignmentInstruction;
import edu.cmu.cs.crystal.tac.BinaryOperation;
import edu.cmu.cs.crystal.tac.BinaryOperator;
import edu.cmu.cs.crystal.tac.CopyInstruction;
import edu.cmu.cs.crystal.tac.LoadLiteralInstruction;
import edu.cmu.cs.crystal.tac.TACFlowAnalysis;
import edu.cmu.cs.crystal.tac.TACInstruction;
import edu.cmu.cs.crystal.tac.Variable;

/**
 * An analysis that checks for possible divide-by-zero exceptions.
 * Note how the getResultsAfter() method of the FlowAnalysis is used
 * to actually run the analysis, even though we kind of ignore its
 * return value. Note also that we defined the actual transfer methods
 * as a private inner class so that when we call 
 * <code>Crystal.getInstance().reportUserProblem()</code> we have the
 * actual analysis instance to pass to it.
 * 
 * 
 * @author Nels Beckman
 *
 */
public class DivideByZeroAnalysis extends AbstractCrystalMethodAnalysis {

	protected TACFlowAnalysis<TupleLatticeElement<Variable, DivideByZeroLatticeElement>> fa;

	@Override
	public void analyzeMethod(MethodDeclaration d) {
		// create a transfer function object and pass it to a new FlowAnalysis
		DBZTransferMethods tf = new DBZTransferMethods();
		fa = new TACFlowAnalysis<TupleLatticeElement<Variable, DivideByZeroLatticeElement>>(
				crystal, tf);
		
		// must call getResultsAfter at least once on this method,
		// or the analysis won't be run on this method
		fa.getResultsAfter(d);

		// report any problems found
		for(TACInstruction i : tf.getProblems().keySet())
			report(i, tf.getProblems().get(i));
	}

	/**
	 * Reports problems to the user
	 * @param instr Instruction with problem
	 * @param le Offending lattice value
	 */
	private void report(TACInstruction instr, DivideByZeroLatticeElement le) {
		// we look up the AST node associated with the instruction
		// so that the Crystal framework knows where the error is
		if(le.equals(DivideByZeroLatticeElement.ZERO))
			crystal.reportUserProblem("ERROR: Definite divide by zero: " + instr, instr.getNode(), this);
		if(le.equals(DivideByZeroLatticeElement.MAYBEZERO))
			crystal.reportUserProblem("Warning: Possible divide by zero: " + instr, instr.getNode(), this);
	}
	
	private class DBZTransferMethods extends AbstractingTransferFunction<TupleLatticeElement<Variable, DivideByZeroLatticeElement>> {

		/** We keep a HashMap of the errors generated for each instruction.
		 * It's important to add errors incrementally to this map as they
		 * are discovered and report them all at the end, instead of
		 * reporting them as they come up.  That's because the flow functions
		 * could be called multiple times for each instruction, and we want
		 * only one error per instruction.
		 */
		private HashMap<TACInstruction, DivideByZeroLatticeElement> problems;
		
		public DBZTransferMethods() {
			problems = new HashMap<TACInstruction, DivideByZeroLatticeElement>();
		}
		
		/**
		 * Returns the problems.
		 * @return The problems.
		 */
		public HashMap<TACInstruction, DivideByZeroLatticeElement> getProblems() {
			return problems;
		}
		
		/**
		 * Retrieves the entry lattice for a method.
		 * 
		 * In this case, we assume that all values are possibly zero at entry
		 * to the function.
		 * 
		 * @param d		the method to get the entry lattice for
		 * @return		the entry lattice for the specified method
		 */
		public Lattice<TupleLatticeElement<Variable, DivideByZeroLatticeElement>> getLattice(MethodDeclaration d) {
			TupleLatticeElement<Variable, DivideByZeroLatticeElement> entry = new TupleLatticeElement<Variable, DivideByZeroLatticeElement>(
					DivideByZeroLatticeElement.bottom, DivideByZeroLatticeElement.MAYBEZERO);
			return new Lattice<TupleLatticeElement<Variable, DivideByZeroLatticeElement>>(entry, entry
					.bottom());
		}

		@Override
		public TupleLatticeElement<Variable, DivideByZeroLatticeElement> transfer(BinaryOperation binop, 
				TupleLatticeElement<Variable, DivideByZeroLatticeElement> value) {

			// if we are visiting a divide or modulus operation
			if( binop.getOperator().equals(BinaryOperator.ARIT_DIVIDE) ||
					binop.getOperator().equals(BinaryOperator.ARIT_MODULO)) {

				// get the lattice elemnt for the second operand (the divisor)
				DivideByZeroLatticeElement cur_val = value.get(binop.getOperand2());
				
				// note an error or warning if the divisor is definitely or possibly zero
				if( cur_val.equals(DivideByZeroLatticeElement.ZERO) ) {
					problems.put(binop, DivideByZeroLatticeElement.ZERO);
				}
				else if( cur_val.equals(DivideByZeroLatticeElement.MAYBEZERO) ) {
					problems.put(binop, DivideByZeroLatticeElement.MAYBEZERO);
				}
			}

			return super.transfer(binop, value);
		}


		/** A copy instruction copies the lattice value for the source
		 * variable to the target variable.
		 */
		@Override
		public TupleLatticeElement<Variable, DivideByZeroLatticeElement> transfer(CopyInstruction instr, TupleLatticeElement<Variable, DivideByZeroLatticeElement> value) {
			value.put(instr.getTarget(), value.get(instr.getOperand()));
			return value;
		}

		/** Assignment instructions (other than those explicitly defined
		 * with other flow functions) set the result to maybe zero.
		 */
		@Override
		public TupleLatticeElement<Variable, DivideByZeroLatticeElement> transfer(AssignmentInstruction instr, TupleLatticeElement<Variable, DivideByZeroLatticeElement> value) {
			value.put(instr.getTarget(), DivideByZeroLatticeElement.MAYBEZERO);
			return value;
		}

		/** If we assign a literal integer expression to a variable, we use the
		 * value of that expression to determine the analysis lattice value for
		 * that variable.
		 */ 
		@Override
		public TupleLatticeElement<Variable, DivideByZeroLatticeElement> transfer(LoadLiteralInstruction instr, TupleLatticeElement<Variable, DivideByZeroLatticeElement> value) {
			if( instr.isNumber() ) {
				try {
					if( Integer.parseInt((String) instr.getLiteral()) == 0 )
						value.put(instr.getTarget(), DivideByZeroLatticeElement.ZERO);
					else
						value.put(instr.getTarget(), DivideByZeroLatticeElement.NONZERO);
				}
				catch(NumberFormatException e) {
					// ignore
				}
				return value;
			} else {
				// if it's not an integer literal, handle as usual
				return super.transfer(instr, value);
			}
		}
	}
	
}
