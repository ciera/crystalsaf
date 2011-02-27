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

import edu.cmu.cs.crystal.flow.AnalysisDirection;
import edu.cmu.cs.crystal.flow.BooleanLabel;
import edu.cmu.cs.crystal.flow.ILabel;
import edu.cmu.cs.crystal.flow.ILatticeOperations;
import edu.cmu.cs.crystal.flow.IResult;
import edu.cmu.cs.crystal.flow.LabeledResult;
import edu.cmu.cs.crystal.flow.LabeledSingleResult;
import edu.cmu.cs.crystal.simple.TupleLatticeElement;
import edu.cmu.cs.crystal.simple.TupleLatticeOperations;
import edu.cmu.cs.crystal.tac.ITACAnalysisContext;
import edu.cmu.cs.crystal.tac.ITACBranchSensitiveTransferFunction;
import edu.cmu.cs.crystal.tac.model.ArrayInitInstruction;
import edu.cmu.cs.crystal.tac.model.BinaryOperation;
import edu.cmu.cs.crystal.tac.model.CastInstruction;
import edu.cmu.cs.crystal.tac.model.ConstructorCallInstruction;
import edu.cmu.cs.crystal.tac.model.CopyInstruction;
import edu.cmu.cs.crystal.tac.model.DotClassInstruction;
import edu.cmu.cs.crystal.tac.model.EnhancedForConditionInstruction;
import edu.cmu.cs.crystal.tac.model.InstanceofInstruction;
import edu.cmu.cs.crystal.tac.model.LoadArrayInstruction;
import edu.cmu.cs.crystal.tac.model.LoadFieldInstruction;
import edu.cmu.cs.crystal.tac.model.LoadLiteralInstruction;
import edu.cmu.cs.crystal.tac.model.MethodCallInstruction;
import edu.cmu.cs.crystal.tac.model.NewArrayInstruction;
import edu.cmu.cs.crystal.tac.model.NewObjectInstruction;
import edu.cmu.cs.crystal.tac.model.ReturnInstruction;
import edu.cmu.cs.crystal.tac.model.SourceVariableDeclaration;
import edu.cmu.cs.crystal.tac.model.SourceVariableReadInstruction;
import edu.cmu.cs.crystal.tac.model.StoreArrayInstruction;
import edu.cmu.cs.crystal.tac.model.StoreFieldInstruction;
import edu.cmu.cs.crystal.tac.model.UnaryOperation;
import edu.cmu.cs.crystal.tac.model.UnaryOperator;
import edu.cmu.cs.crystal.tac.model.Variable;


public class ConstantTransferFunction implements ITACBranchSensitiveTransferFunction<TupleLatticeElement<Variable, BooleanConstantLE>> {
	private final TupleLatticeOperations<Variable, BooleanConstantLE> ops = 
	 new TupleLatticeOperations<Variable, BooleanConstantLE>(
	 new BooleanConstantLatticeOps(), BooleanConstantLE.BOTTOM);

	public ILatticeOperations<TupleLatticeElement<Variable, BooleanConstantLE>> getLatticeOperations() {
		return ops;
	}
	
	public TupleLatticeElement<Variable, BooleanConstantLE> createEntryValue(MethodDeclaration m) {
		return ops.getDefault();
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
		return LabeledSingleResult.createResult(value, labels);
	}

	public IResult<TupleLatticeElement<Variable, BooleanConstantLE>> transfer(
			BinaryOperation binop, List<ILabel> labels,
			TupleLatticeElement<Variable, BooleanConstantLE> value) {
		return handleBooleanLabels(value, labels, binop.getTarget(), binop.getNode());
	}

	public IResult<TupleLatticeElement<Variable, BooleanConstantLE>> transfer(
			CastInstruction instr, List<ILabel> labels,
			TupleLatticeElement<Variable, BooleanConstantLE> value) {
		return LabeledSingleResult.createResult(value, labels);
	}

	public IResult<TupleLatticeElement<Variable, BooleanConstantLE>> transfer(
			DotClassInstruction instr, List<ILabel> labels,
			TupleLatticeElement<Variable, BooleanConstantLE> value) {
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
		
		BooleanConstantLE rValue = value.get(instr.getOperand());
		
		if (labels.contains(BooleanLabel.getBooleanLabel(true)) && labels.contains(BooleanLabel.getBooleanLabel(false)))
		{
			if (rValue == BooleanConstantLE.TRUE || rValue == BooleanConstantLE.FALSE) {
				TupleLatticeElement<Variable, BooleanConstantLE> tVal, fVal;
				LabeledResult<TupleLatticeElement<Variable, BooleanConstantLE>> result = LabeledResult.createResult(value);
				boolean isTrue = rValue == BooleanConstantLE.TRUE;
				
				tVal = ops.copy(value);
				fVal = ops.copy(value);
				
				tVal.put(instr.getTarget(), isTrue ? rValue : BooleanConstantLE.BOTTOM);
				fVal.put(instr.getTarget(), !isTrue ? rValue : BooleanConstantLE.BOTTOM);
				value.put(instr.getTarget(), rValue);
				
				result.put(BooleanLabel.getBooleanLabel(true), tVal);
				result.put(BooleanLabel.getBooleanLabel(false), fVal);
				return result;				
				
			}
			else {
				value.put(instr.getTarget(), rValue);
				return LabeledSingleResult.createResult(value, labels);				
			}
		}
		else {
			value.put(instr.getTarget(), rValue);
			return LabeledSingleResult.createResult(value, labels);
		}
	}

	public IResult<TupleLatticeElement<Variable, BooleanConstantLE>> transfer(
			InstanceofInstruction instr, List<ILabel> labels,
			TupleLatticeElement<Variable, BooleanConstantLE> value) {
		return handleBooleanLabels(value, labels, instr.getTarget(), instr.getNode());
	}

	public IResult<TupleLatticeElement<Variable, BooleanConstantLE>> transfer(
			LoadLiteralInstruction instr, List<ILabel> labels,
			TupleLatticeElement<Variable, BooleanConstantLE> value) {
		if (instr.getNode() instanceof BooleanLiteral) {
			BooleanLiteral boolNode = (BooleanLiteral)instr.getNode();
			
			if (labels.contains(BooleanLabel.getBooleanLabel(true)) && labels.contains(BooleanLabel.getBooleanLabel(false)))
			{
				TupleLatticeElement<Variable, BooleanConstantLE> tVal, fVal;
				LabeledResult<TupleLatticeElement<Variable, BooleanConstantLE>> result = LabeledResult.createResult(value);
				
				tVal = ops.copy(value);
				fVal = ops.copy(value);
				
				tVal.put(instr.getTarget(), boolNode.booleanValue() ? BooleanConstantLE.TRUE : BooleanConstantLE.BOTTOM);
				fVal.put(instr.getTarget(), !boolNode.booleanValue() ? BooleanConstantLE.FALSE : BooleanConstantLE.BOTTOM);
				value.put(instr.getTarget(), boolNode.booleanValue() ? BooleanConstantLE.TRUE : BooleanConstantLE.FALSE);
				
				result.put(BooleanLabel.getBooleanLabel(true), tVal);
				result.put(BooleanLabel.getBooleanLabel(false), fVal);
				return result;				
			}
			else
				value.put(instr.getTarget(), boolNode.booleanValue() ? BooleanConstantLE.TRUE : BooleanConstantLE.FALSE);
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
	
	private IResult<TupleLatticeElement<Variable, BooleanConstantLE>> handleBooleanLabels(TupleLatticeElement<Variable, BooleanConstantLE> value,
			List<ILabel> labels, Variable var, ASTNode node) {
		if (labels.contains(BooleanLabel.getBooleanLabel(true)) && labels.contains(BooleanLabel.getBooleanLabel(false)))
		{
			TupleLatticeElement<Variable, BooleanConstantLE> tVal, fVal;
			LabeledResult<TupleLatticeElement<Variable, BooleanConstantLE>> result = LabeledResult.createResult(value);
			
			tVal = ops.copy(value);
			fVal = ops.copy(value);
			
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
		return LabeledSingleResult.createResult(value, labels);
	}

	public IResult<TupleLatticeElement<Variable, BooleanConstantLE>> transfer(
			NewObjectInstruction instr, List<ILabel> labels,
			TupleLatticeElement<Variable, BooleanConstantLE> value) {
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
		if (instr.getDeclaredVariable().resolveType().getName().equals("boolean"))
			value.put(instr.getDeclaredVariable(), BooleanConstantLE.UNKNOWN);
		return LabeledSingleResult.createResult(value, labels);
	}

	public IResult<TupleLatticeElement<Variable, BooleanConstantLE>> transfer(
			SourceVariableReadInstruction instr, List<ILabel> labels,
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
