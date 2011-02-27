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

import edu.cmu.cs.crystal.tac.model.ArrayInitInstruction;
import edu.cmu.cs.crystal.tac.model.BinaryOperation;
import edu.cmu.cs.crystal.tac.model.CastInstruction;
import edu.cmu.cs.crystal.tac.model.ConstructorCallInstruction;
import edu.cmu.cs.crystal.tac.model.CopyInstruction;
import edu.cmu.cs.crystal.tac.model.DotClassInstruction;
import edu.cmu.cs.crystal.tac.model.InstanceofInstruction;
import edu.cmu.cs.crystal.tac.model.LoadArrayInstruction;
import edu.cmu.cs.crystal.tac.model.LoadFieldInstruction;
import edu.cmu.cs.crystal.tac.model.LoadLiteralInstruction;
import edu.cmu.cs.crystal.tac.model.MethodCallInstruction;
import edu.cmu.cs.crystal.tac.model.NewArrayInstruction;
import edu.cmu.cs.crystal.tac.model.NewObjectInstruction;
import edu.cmu.cs.crystal.tac.model.SourceVariableDeclaration;
import edu.cmu.cs.crystal.tac.model.StoreArrayInstruction;
import edu.cmu.cs.crystal.tac.model.StoreFieldInstruction;
import edu.cmu.cs.crystal.tac.model.UnaryOperation;

public class PrintInstructionAnalysis extends SimpleInstructionVisitor {
	
	/**
	 * @param instr
	 */
	public void visit(ArrayInitInstruction instr) {
		System.out.println(instr);
	}

	/**
	 * @param unop
	 */
	public void visit(UnaryOperation unop) {
		System.out.println(unop);
	}

	/**
	 * @param instr
	 */
	public void visit(SourceVariableDeclaration instr) {
		System.out.println(instr);
		System.out.println("id:" + instr.getDeclaredVariable().getBinding().getVariableId());
	}

	/**
	 * @param instr
	 */
	public void visit(StoreFieldInstruction instr) {
		System.out.println(instr);
	}

	/**
	 * @param instr
	 */
	public void visit(StoreArrayInstruction instr) {
		System.out.println(instr);
	}

	/**
	 * @param instr
	 */
	public void visit(NewObjectInstruction instr) {
		System.out.println(instr);
	}

	/**
	 * @param instr
	 */
	public void visit(NewArrayInstruction instr) {
		System.out.println(instr);
	}

	/**
	 * @param instr
	 */
	public void visit(MethodCallInstruction instr) {
		System.out.println(instr);
	}

	/**
	 * @param instr
	 */
	public void visit(LoadFieldInstruction instr) {
		System.out.println(instr);
	}

	/**
	 * @param instr
	 */
	public void visit(LoadArrayInstruction instr) {
		System.out.println(instr);
	}

	/**
	 * @param instr
	 */
	public void visit(LoadLiteralInstruction instr) {
		System.out.println(instr);
	}

	/**
	 * @param instr
	 */
	public void visit(InstanceofInstruction instr) {
		System.out.println(instr);
	}

	/**
	 * @param instr
	 */
	public void visit(CopyInstruction instr) {
		System.out.println(instr);
	}

	/**
	 * @param instr
	 */
	public void visit(ConstructorCallInstruction instr) {
		System.out.println(instr);
	}

	/**
	 * @param instr
	 */
	public void visit(DotClassInstruction instr) {
		System.out.println(instr);
	}

	/**
	 * @param instr
	 */
	public void visit(CastInstruction instr) {
		System.out.println(instr);
	}

	/**
	 * @param binop
	 */
	public void visit(BinaryOperation binop) {
		System.out.println(binop);
	}

}