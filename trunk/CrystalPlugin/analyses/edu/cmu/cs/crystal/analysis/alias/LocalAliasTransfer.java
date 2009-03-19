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
package edu.cmu.cs.crystal.analysis.alias;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;

import edu.cmu.cs.crystal.ILabel;
import edu.cmu.cs.crystal.analysis.metrics.LoopCountingAnalysis;
import edu.cmu.cs.crystal.flow.ILatticeOperations;
import edu.cmu.cs.crystal.flow.IResult;
import edu.cmu.cs.crystal.flow.LabeledSingleResult;
import edu.cmu.cs.crystal.simple.LatticeElementOps;
import edu.cmu.cs.crystal.simple.TupleLatticeElement;
import edu.cmu.cs.crystal.tac.AbstractTACBranchSensitiveTransferFunction;
import edu.cmu.cs.crystal.tac.ArrayInitInstruction;
import edu.cmu.cs.crystal.tac.AssignmentInstruction;
import edu.cmu.cs.crystal.tac.BinaryOperation;
import edu.cmu.cs.crystal.tac.CastInstruction;
import edu.cmu.cs.crystal.tac.CopyInstruction;
import edu.cmu.cs.crystal.tac.DotClassInstruction;
import edu.cmu.cs.crystal.tac.ITACBranchSensitiveTransferFunction;
import edu.cmu.cs.crystal.tac.InstanceofInstruction;
import edu.cmu.cs.crystal.tac.LoadArrayInstruction;
import edu.cmu.cs.crystal.tac.LoadFieldInstruction;
import edu.cmu.cs.crystal.tac.LoadLiteralInstruction;
import edu.cmu.cs.crystal.tac.MethodCallInstruction;
import edu.cmu.cs.crystal.tac.NewArrayInstruction;
import edu.cmu.cs.crystal.tac.NewObjectInstruction;
import edu.cmu.cs.crystal.tac.SourceVariableDeclaration;
import edu.cmu.cs.crystal.tac.TACInstruction;
import edu.cmu.cs.crystal.tac.UnaryOperation;
import edu.cmu.cs.crystal.tac.Variable;

/**
 * @author Ciera Christopher
 * @author Kevin Bierhoff (refactorings from {@link MayAliasTransferFunction})
 *
 */
public class LocalAliasTransfer extends
		AbstractTACBranchSensitiveTransferFunction<TupleLatticeElement<Variable, AliasLE>> implements
		ITACBranchSensitiveTransferFunction<TupleLatticeElement<Variable, AliasLE>> {
	
	private final LoopCountingAnalysis loopCounter;
	private Map<Variable, ObjectLabel> labelContext;
	private final TupleLatticeElement<Variable, AliasLE> empty = 
		new TupleLatticeElement<Variable, AliasLE>(
			AliasLE.bottom(), AliasLE.bottom());
	
	public LocalAliasTransfer() {
		loopCounter = new LoopCountingAnalysis();
		labelContext = new HashMap<Variable, ObjectLabel>();
	}

	/* (non-Javadoc)
	 * @see edu.cmu.cs.crystal.flow.IFlowAnalysisDefinition#getLattice(org.eclipse.jdt.core.dom.MethodDeclaration)
	 */
	public ILatticeOperations<TupleLatticeElement<Variable, AliasLE>> createLatticeOperations(MethodDeclaration methodDeclaration) {
		return LatticeElementOps.create(empty.bottom());
	}
	
	public TupleLatticeElement<Variable, AliasLE> createEntryValue(MethodDeclaration m) {
		labelContext = new HashMap<Variable, ObjectLabel>();

		TupleLatticeElement<Variable, AliasLE> result = empty.copy();
		Variable thisVar = getAnalysisContext().getThisVariable();
		ObjectLabel thisLabel = getThisLabel(thisVar);
		result.put(thisVar, AliasLE.create(thisLabel));
		// TODO what about qualified this's and super's?
		return result;
	}

	private ObjectLabel getThisLabel(final Variable thisVar) {
		// special label for "this"
		return new ObjectLabel() {
			public ITypeBinding getType() {
				return thisVar.resolveType();
			}
			public boolean isSummary() {
				return false;
			}
			@Override
			public String toString() {
				return thisVar.getSourceString();
			}
		};
	}

	private ObjectLabel getLabel(Variable associatedVar, ITypeBinding binding, TACInstruction declaringInstr) {
		//if we already produced an initial label for this variable, use it
		// TODO use special labels for literals and in particular null
		if (labelContext.get(associatedVar) != null) {
			return labelContext.get(associatedVar);
		}
		else {
			boolean isInLoop = loopCounter.isInLoop(declaringInstr.getNode());
			ObjectLabel label = new DefaultObjectLabel(binding, isInLoop);
			labelContext.put(associatedVar, label);
			return label;
		}
	}
	
	private TupleLatticeElement<Variable, AliasLE> 
	putSingletonLabel(AssignmentInstruction instr, TupleLatticeElement<Variable, AliasLE> value) {
		ObjectLabel label = getLabel(instr.getTarget(), instr.getTarget().resolveType(), instr);
		AliasLE aliases = AliasLE.create(label);
		value.put(instr.getTarget(), aliases);
		return value;
	}
	
	public IResult<TupleLatticeElement<Variable, AliasLE>> transfer(
			ArrayInitInstruction instr, List<ILabel> labels,
			TupleLatticeElement<Variable, AliasLE> value) {
		return LabeledSingleResult.createResult(putSingletonLabel(instr, value), labels);
	}

	public IResult<TupleLatticeElement<Variable, AliasLE>> transfer(
			BinaryOperation binop, List<ILabel> labels,
			TupleLatticeElement<Variable, AliasLE> value) {
		return LabeledSingleResult.createResult(putSingletonLabel(binop, value), labels);
	}

	public IResult<TupleLatticeElement<Variable, AliasLE>> transfer(
			CastInstruction instr, List<ILabel> labels,
			TupleLatticeElement<Variable, AliasLE> value) {
		// TODO maybe one could use the type cased to to drop aliases of incompatible types?
		value.put(instr.getTarget(), value.get(instr.getOperand()).copy());
		return LabeledSingleResult.createResult(value, labels);
	}

	public IResult<TupleLatticeElement<Variable, AliasLE>> transfer(
			DotClassInstruction instr, List<ILabel> labels,
			TupleLatticeElement<Variable, AliasLE> value) {
		//TODO: consider how to handle literals
		return LabeledSingleResult.createResult(putSingletonLabel(instr, value), labels);
	}

	public IResult<TupleLatticeElement<Variable, AliasLE>> transfer(
			CopyInstruction instr, List<ILabel> labels,
			TupleLatticeElement<Variable, AliasLE> value) {
		//if value does not contain instr, then what? will it do the put automatically?
		value.put(instr.getTarget(), value.get(instr.getOperand()).copy());
		return LabeledSingleResult.createResult(value, labels);
	}

	public IResult<TupleLatticeElement<Variable, AliasLE>> transfer(
			InstanceofInstruction instr, List<ILabel> labels,
			TupleLatticeElement<Variable, AliasLE> value) {
		// result of instanceof is a fresh (boolean) label
		return LabeledSingleResult.createResult(putSingletonLabel(instr, value), labels);
	}

	public IResult<TupleLatticeElement<Variable, AliasLE>> transfer(
			LoadLiteralInstruction instr, List<ILabel> labels,
			TupleLatticeElement<Variable, AliasLE> value) {
		//TODO: consider how to handle literals
		return LabeledSingleResult.createResult(putSingletonLabel(instr, value), labels);
	}

	public IResult<TupleLatticeElement<Variable, AliasLE>> transfer(
			LoadArrayInstruction instr, List<ILabel> labels,
			TupleLatticeElement<Variable, AliasLE> value) {
		return LabeledSingleResult.createResult(putSingletonLabel(instr, value), labels);
	}

	public IResult<TupleLatticeElement<Variable, AliasLE>> transfer(
			LoadFieldInstruction instr, List<ILabel> labels,
			TupleLatticeElement<Variable, AliasLE> value) {
		return LabeledSingleResult.createResult(putSingletonLabel(instr, value), labels);
	}

	public IResult<TupleLatticeElement<Variable, AliasLE>> transfer(
			MethodCallInstruction instr, List<ILabel> labels,
			TupleLatticeElement<Variable, AliasLE> value) {
		return LabeledSingleResult.createResult(putSingletonLabel(instr, value), labels);
	}

	public IResult<TupleLatticeElement<Variable, AliasLE>> transfer(
			NewArrayInstruction instr, List<ILabel> labels,
			TupleLatticeElement<Variable, AliasLE> value) {
		return LabeledSingleResult.createResult(putSingletonLabel(instr, value), labels);
	}

	public IResult<TupleLatticeElement<Variable, AliasLE>> transfer(
			NewObjectInstruction instr, List<ILabel> labels,
			TupleLatticeElement<Variable, AliasLE> value) {
		return LabeledSingleResult.createResult(putSingletonLabel(instr, value), labels);
	}

	public IResult<TupleLatticeElement<Variable, AliasLE>> transfer(
			UnaryOperation unop, List<ILabel> labels,
			TupleLatticeElement<Variable, AliasLE> value) {
		return LabeledSingleResult.createResult(putSingletonLabel(unop, value), labels);
	}

	public IResult<TupleLatticeElement<Variable, AliasLE>> transfer(
			final SourceVariableDeclaration instr, List<ILabel> labels,
			TupleLatticeElement<Variable, AliasLE> value) {
		if (instr.isFormalParameter()) {
			ObjectLabel label = new ObjectLabel() {
				public ITypeBinding getType() {
					return instr.resolveBinding().getType();
				}
				public boolean isSummary() {
					return false;
				}
				@Override public String toString() {
					return "param." + instr.getDeclaredVariable().getSourceString();
				}
			};
			AliasLE aliases = AliasLE.create(label);
			value.put(instr.getDeclaredVariable(), aliases);
		}
		return LabeledSingleResult.createResult(value, labels);
	}
}
