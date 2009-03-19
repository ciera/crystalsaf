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

import java.util.List;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.BooleanLiteral;
import org.eclipse.jdt.core.dom.MethodDeclaration;

import edu.cmu.cs.crystal.BooleanLabel;
import edu.cmu.cs.crystal.ILabel;
import edu.cmu.cs.crystal.flow.AnalysisDirection;
import edu.cmu.cs.crystal.flow.ILatticeOperations;
import edu.cmu.cs.crystal.flow.IResult;
import edu.cmu.cs.crystal.flow.LabeledResult;
import edu.cmu.cs.crystal.flow.LabeledSingleResult;
import edu.cmu.cs.crystal.simple.LatticeElementOps;
import edu.cmu.cs.crystal.simple.TupleLatticeElement;
import edu.cmu.cs.crystal.tac.ArrayInitInstruction;
import edu.cmu.cs.crystal.tac.BinaryOperation;
import edu.cmu.cs.crystal.tac.CastInstruction;
import edu.cmu.cs.crystal.tac.ConstructorCallInstruction;
import edu.cmu.cs.crystal.tac.CopyInstruction;
import edu.cmu.cs.crystal.tac.DotClassInstruction;
import edu.cmu.cs.crystal.tac.EnhancedForConditionInstruction;
import edu.cmu.cs.crystal.tac.ITACAnalysisContext;
import edu.cmu.cs.crystal.tac.ITACBranchSensitiveTransferFunction;
import edu.cmu.cs.crystal.tac.InstanceofInstruction;
import edu.cmu.cs.crystal.tac.LoadArrayInstruction;
import edu.cmu.cs.crystal.tac.LoadFieldInstruction;
import edu.cmu.cs.crystal.tac.LoadLiteralInstruction;
import edu.cmu.cs.crystal.tac.MethodCallInstruction;
import edu.cmu.cs.crystal.tac.NewArrayInstruction;
import edu.cmu.cs.crystal.tac.NewObjectInstruction;
import edu.cmu.cs.crystal.tac.ReturnInstruction;
import edu.cmu.cs.crystal.tac.SourceVariableDeclaration;
import edu.cmu.cs.crystal.tac.SourceVariableRead;
import edu.cmu.cs.crystal.tac.StoreArrayInstruction;
import edu.cmu.cs.crystal.tac.StoreFieldInstruction;
import edu.cmu.cs.crystal.tac.UnaryOperation;
import edu.cmu.cs.crystal.tac.UnaryOperator;
import edu.cmu.cs.crystal.tac.Variable;


public class ConstantTransferFunction implements ITACBranchSensitiveTransferFunction<TupleLatticeElement<Variable, BooleanConstantLE>> {
	private final TupleLatticeElement<Variable, BooleanConstantLE> entry = 
		new TupleLatticeElement<Variable, BooleanConstantLE>(
			BooleanConstantLE.BOTTOM, BooleanConstantLE.UNKNOWN);
	/* (non-Javadoc)
	 * @see edu.cmu.cs.crystal.tac.ITransferFunction#getLattice(com.surelogic.ast.java.operator.IMethodDeclarationNode)
	 */
	public ILatticeOperations<TupleLatticeElement<Variable, BooleanConstantLE>> createLatticeOperations(MethodDeclaration d) {
		return LatticeElementOps.create(entry.bottom());
	}
	
	public TupleLatticeElement<Variable, BooleanConstantLE> createEntryValue(MethodDeclaration m) {
		return entry.copy();
	}

	public AnalysisDirection getAnalysisDirection() {
		return AnalysisDirection.FORWARD_ANALYSIS;
	}

	public void setAnalysisContext(ITACAnalysisContext analysisContext) {
		// not needed
	}

	public IResult<TupleLatticeElement<Variable, BooleanConstantLE>> transfer(
			ArrayInitInstruction instr, List<ILabel> labels,
			TupleLatticeElement<Variable, BooleanConstantLE> value) {
		value.put(instr.getTarget(), BooleanConstantLE.BOTTOM);
		return LabeledSingleResult.createResult(value, labels);
	}

	public IResult<TupleLatticeElement<Variable, BooleanConstantLE>> transfer(
			BinaryOperation binop, List<ILabel> labels,
			TupleLatticeElement<Variable, BooleanConstantLE> value) {
		value.put(binop.getTarget(), BooleanConstantLE.BOTTOM);
		return LabeledSingleResult.createResult(value, labels);
	}

	public IResult<TupleLatticeElement<Variable, BooleanConstantLE>> transfer(
			CastInstruction instr, List<ILabel> labels,
			TupleLatticeElement<Variable, BooleanConstantLE> value) {
		value.put(instr.getTarget(), BooleanConstantLE.BOTTOM);
		return LabeledSingleResult.createResult(value, labels);
	}

	public IResult<TupleLatticeElement<Variable, BooleanConstantLE>> transfer(
			DotClassInstruction instr, List<ILabel> labels,
			TupleLatticeElement<Variable, BooleanConstantLE> value) {
		value.put(instr.getTarget(), BooleanConstantLE.BOTTOM);
		return LabeledSingleResult.createResult(value, labels);
	}

	public IResult<TupleLatticeElement<Variable, BooleanConstantLE>> transfer(
			ConstructorCallInstruction instr, List<ILabel> labels,
			TupleLatticeElement<Variable, BooleanConstantLE> value) {
		return LabeledSingleResult.createResult(value, labels);
	}

	public IResult<TupleLatticeElement<Variable, BooleanConstantLE>> transfer(
			CopyInstruction instr, List<ILabel> labels,
			TupleLatticeElement<Variable, BooleanConstantLE> value) {
		value.put(instr.getTarget(), value.get(instr.getOperand()));
		return LabeledSingleResult.createResult(value, labels);
	}

	public IResult<TupleLatticeElement<Variable, BooleanConstantLE>> transfer(
			InstanceofInstruction instr, List<ILabel> labels,
			TupleLatticeElement<Variable, BooleanConstantLE> value) {
		value.put(instr.getTarget(), BooleanConstantLE.BOTTOM);
		return LabeledSingleResult.createResult(value, labels);
	}

	public IResult<TupleLatticeElement<Variable, BooleanConstantLE>> transfer(
			LoadLiteralInstruction instr, List<ILabel> labels,
			TupleLatticeElement<Variable, BooleanConstantLE> value) {
		if (instr.getNode() instanceof BooleanLiteral) {
			BooleanLiteral boolNode = (BooleanLiteral)instr.getNode();
			if (boolNode.booleanValue())
				value.put(instr.getTarget(), BooleanConstantLE.TRUE);
			else
				value.put(instr.getTarget(), BooleanConstantLE.FALSE);
		}
		else {
			value.put(instr.getTarget(), BooleanConstantLE.BOTTOM);
		}
		return LabeledSingleResult.createResult(value, labels);
	}

	public IResult<TupleLatticeElement<Variable, BooleanConstantLE>> transfer(
			LoadArrayInstruction instr, List<ILabel> labels,
			TupleLatticeElement<Variable, BooleanConstantLE> value) {
		return handleBooleanLabels(value, labels, instr.getTarget(), instr.getNode());
	}

	public IResult<TupleLatticeElement<Variable, BooleanConstantLE>> transfer(
			LoadFieldInstruction instr, List<ILabel> labels,
			TupleLatticeElement<Variable, BooleanConstantLE> value) {
		return handleBooleanLabels(value, labels, instr.getTarget(), instr.getNode());
	}

	public IResult<TupleLatticeElement<Variable, BooleanConstantLE>> transfer(
			MethodCallInstruction instr, List<ILabel> labels,
			TupleLatticeElement<Variable, BooleanConstantLE> value) {
		return handleBooleanLabels(value, labels, instr.getTarget(), instr.getNode());
	}
	
	private IResult handleBooleanLabels(TupleLatticeElement<Variable, BooleanConstantLE> value,
			List<ILabel> labels, Variable var, ASTNode node) {
		if (labels.contains(BooleanLabel.getBooleanLabel(true)) && labels.contains(BooleanLabel.getBooleanLabel(false)))
		{
			TupleLatticeElement<Variable, BooleanConstantLE> tVal, fVal;
			LabeledResult result = new LabeledResult(value);
			
			tVal = value.copy();
			fVal = value.copy();
			
			tVal.put(var, BooleanConstantLE.TRUE);
			fVal.put(var, BooleanConstantLE.FALSE);
			value.put(var, BooleanConstantLE.UNKNOWN);
			
			result.put(BooleanLabel.getBooleanLabel(true), tVal);
			result.put(BooleanLabel.getBooleanLabel(false), fVal);
			return result;
		}
		else if (var.resolveType().getName().equals("boolean"))
			value.put(var, BooleanConstantLE.UNKNOWN);
		else
			value.put(var, BooleanConstantLE.BOTTOM);
		return LabeledSingleResult.createResult(value, labels);
	}

	public IResult<TupleLatticeElement<Variable, BooleanConstantLE>> transfer(
			NewArrayInstruction instr, List<ILabel> labels,
			TupleLatticeElement<Variable, BooleanConstantLE> value) {
		value.put(instr.getTarget(), BooleanConstantLE.BOTTOM);
		return LabeledSingleResult.createResult(value, labels);
	}

	public IResult<TupleLatticeElement<Variable, BooleanConstantLE>> transfer(
			NewObjectInstruction instr, List<ILabel> labels,
			TupleLatticeElement<Variable, BooleanConstantLE> value) {
		value.put(instr.getTarget(), BooleanConstantLE.BOTTOM);
		return LabeledSingleResult.createResult(value, labels);
	}

	public IResult<TupleLatticeElement<Variable, BooleanConstantLE>> transfer(
			StoreArrayInstruction instr, List<ILabel> labels,
			TupleLatticeElement<Variable, BooleanConstantLE> value) {
		return LabeledSingleResult.createResult(value, labels);
	}

	public IResult<TupleLatticeElement<Variable, BooleanConstantLE>> transfer(
			StoreFieldInstruction instr, List<ILabel> labels,
			TupleLatticeElement<Variable, BooleanConstantLE> value) {
		return LabeledSingleResult.createResult(value, labels);
	}

	public IResult<TupleLatticeElement<Variable, BooleanConstantLE>> transfer(
			UnaryOperation unop, List<ILabel> labels,
			TupleLatticeElement<Variable, BooleanConstantLE> value) {
		BooleanConstantLE targetLattice;
		
		if ( unop.getOperator().equals(UnaryOperator.BOOL_NOT)) {
			if (value.get(unop.getOperand()) == BooleanConstantLE.FALSE)
				targetLattice = BooleanConstantLE.TRUE;
			else if (value.get(unop.getOperand()) == BooleanConstantLE.TRUE)
				targetLattice = BooleanConstantLE.FALSE;
			else
				targetLattice = BooleanConstantLE.UNKNOWN;		
		}
		else
			targetLattice = BooleanConstantLE.BOTTOM;
			
		value.put(unop.getTarget(), targetLattice);
		return LabeledSingleResult.createResult(value, labels);
	}

	public IResult<TupleLatticeElement<Variable, BooleanConstantLE>> transfer(
			SourceVariableDeclaration instr, List<ILabel> labels,
			TupleLatticeElement<Variable, BooleanConstantLE> value) {
		return LabeledSingleResult.createResult(value, labels);
	}

	public IResult<TupleLatticeElement<Variable, BooleanConstantLE>> transfer(
			SourceVariableRead instr, List<ILabel> labels,
			TupleLatticeElement<Variable, BooleanConstantLE> value) {
		return LabeledSingleResult.createResult(value, labels);
	}

	public IResult<TupleLatticeElement<Variable, BooleanConstantLE>> transfer(
			EnhancedForConditionInstruction instr, List<ILabel> labels,
			TupleLatticeElement<Variable, BooleanConstantLE> value) {
		return LabeledSingleResult.createResult(value, labels);
	}

	public IResult<TupleLatticeElement<Variable, BooleanConstantLE>> transfer(
			ReturnInstruction instr, List<ILabel> labels,
			TupleLatticeElement<Variable, BooleanConstantLE> value) {
		return LabeledSingleResult.createResult(value, labels);
	}

}
