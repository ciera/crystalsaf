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
package edu.cmu.cs.crystal.tac;

import java.util.List;

import edu.cmu.cs.crystal.ILabel;
import edu.cmu.cs.crystal.flow.IFlowAnalysisDefinition;
import edu.cmu.cs.crystal.flow.IResult;
import edu.cmu.cs.crystal.flow.LatticeElement;

/**
 * Interface for defining branch-sensitive 
 * flow analysis transfer functions based on 3-address code instructions.
 * Implement this interface directly or use a pre-defined abstract base classes
 * for it.  
 * 
 * To create a flow analysis, pass an instance of this interface to
 * {@link BranchInsensitiveTACAnalysis}.
 * 
 * @author Kevin Bierhoff
 *
 * @param <LE> LatticeElement subclass that represents the analysis knowledge.
 * 
 * @see TACInstruction
 * @see ITACTransferFunction
 */
public interface ITACBranchSensitiveTransferFunction<LE extends LatticeElement<LE>> extends IFlowAnalysisDefinition<LE> {
	
	/**
	 * This method is used to pass a variable query interface to the
	 * transfer function.  Transfer functions can, but do not
	 * have to, store the passed object in one of their fields for future use.
	 * The provided object can be used to find {@link Variable} objects for AST nodes.  
	 * @param context Interface to query for variables given AST nodes.
	 */
	public void setAnalysisContext(ITACAnalysisContext context);
	
	public IResult<LE> transfer(ArrayInitInstruction instr, List<ILabel> labels, LE value);

	public IResult<LE> transfer(BinaryOperation binop, List<ILabel> labels, LE value);

	public IResult<LE> transfer(CastInstruction instr, List<ILabel> labels, LE value);

	public IResult<LE> transfer(DotClassInstruction instr, List<ILabel> labels, LE value);

	public IResult<LE> transfer(ConstructorCallInstruction instr, List<ILabel> labels, LE value);

	public IResult<LE> transfer(CopyInstruction instr, List<ILabel> labels, LE value);

	public IResult<LE> transfer(EnhancedForConditionInstruction instr, List<ILabel> labels, LE value);

	public IResult<LE> transfer(InstanceofInstruction instr, List<ILabel> labels, LE value);

	public IResult<LE> transfer(LoadLiteralInstruction instr, List<ILabel> labels, LE value);

	public IResult<LE> transfer(LoadArrayInstruction instr, List<ILabel> labels, LE value);

	public IResult<LE> transfer(LoadFieldInstruction instr, List<ILabel> labels, LE value);

	public IResult<LE> transfer(MethodCallInstruction instr, List<ILabel> labels, LE value);

	public IResult<LE> transfer(NewArrayInstruction instr, List<ILabel> labels, LE value);

	public IResult<LE> transfer(NewObjectInstruction instr, List<ILabel> labels, LE value);

	public IResult<LE> transfer(ReturnInstruction instr, List<ILabel> labels, LE value);

	public IResult<LE> transfer(StoreArrayInstruction instr, List<ILabel> labels, LE value);

	public IResult<LE> transfer(StoreFieldInstruction instr, List<ILabel> labels, LE value);

	public IResult<LE> transfer(SourceVariableDeclaration instr, List<ILabel> labels, LE value);

	public IResult<LE> transfer(SourceVariableRead instr, List<ILabel> labels, LE value);

	public IResult<LE> transfer(UnaryOperation unop, List<ILabel> labels, LE value);
}
