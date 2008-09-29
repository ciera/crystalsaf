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

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;

import edu.cmu.cs.crystal.Crystal;
import edu.cmu.cs.crystal.ILabel;
import edu.cmu.cs.crystal.flow.AnalysisDirection;
import edu.cmu.cs.crystal.flow.IBranchSensitiveTransferFunction;
import edu.cmu.cs.crystal.flow.IFlowAnalysisDefinition;
import edu.cmu.cs.crystal.flow.IResult;
import edu.cmu.cs.crystal.flow.ITACFlowAnalysis;
import edu.cmu.cs.crystal.flow.ITransferFunction;
import edu.cmu.cs.crystal.flow.LabeledSingleResult;
import edu.cmu.cs.crystal.flow.Lattice;
import edu.cmu.cs.crystal.flow.LatticeElement;
import edu.cmu.cs.crystal.flow.MotherFlowAnalysis;
import edu.cmu.cs.crystal.flow.SingleResult;
import edu.cmu.cs.crystal.tac.eclipse.CompilationUnitTACs;
import edu.cmu.cs.crystal.tac.eclipse.EclipseInstructionSequence;
import edu.cmu.cs.crystal.tac.eclipse.EclipseTAC;

/**
 * This class implements flow analyses over 3-address code instructions
 * ({@link TACInstruction}).  To define a specific analysis, implement 
 * {@link ITACTransferFunction} for conventional or 
 * {@link ITACBranchSensitiveTransferFunction} for branch-sensitive flow analyses.
 * 
 * @author Kevin Bierhoff
 *
 * @param <LE>	The LatticeElement subclass that represents the analysis knowledge
 */
public class TACFlowAnalysis<LE extends LatticeElement<LE>> 
extends MotherFlowAnalysis<LE> implements ITACFlowAnalysis<LE>, ITACAnalysisContext {
	
	private AbstractTACAnalysisDriver<LE, ?> driver;
	
	/**
	 * Creates a branch insensitive flow analysis object.
	 * @param transferFunction
	 */
	public TACFlowAnalysis(ITACTransferFunction<LE> transferFunction, CompilationUnitTACs eclipseTAC) {
		super();
		this.driver = new BranchInsensitiveTACAnalysisDriver<LE>(transferFunction, eclipseTAC);
		// TODO use the driver as the analysis context
		transferFunction.setAnalysisContext(this);
	}

	/**
	 * Creates a branch sensitive flow analysis object.
	 * @param transferFunction
	 */
	public TACFlowAnalysis(ITACBranchSensitiveTransferFunction<LE> transferFunction,
			CompilationUnitTACs eclipseTAC) {
		super();
		this.driver = new BranchSensitiveTACAnalysisDriver<LE>(transferFunction, eclipseTAC);
		// TODO use the driver as the analysis context
		transferFunction.setAnalysisContext(this);
	}

	/**
	 * @param crystal
	 * @param transferFunction
	 * @deprecated Use constructor that only takes transfer function.
	 */
	@Deprecated
	public TACFlowAnalysis(Crystal crystal, ITACTransferFunction<LE> transferFunction,
			CompilationUnitTACs eclipseTAC) {
		super(crystal);
		this.driver = new BranchInsensitiveTACAnalysisDriver<LE>(transferFunction, eclipseTAC);
		// TODO use the driver as the analysis context
		transferFunction.setAnalysisContext(this);
	}

	/**
	 * @param crystal
	 * @param transferFunction
	 * @deprecated Use constructor that only takes transfer function.
	 */
	@Deprecated
	public TACFlowAnalysis(Crystal crystal, ITACBranchSensitiveTransferFunction<LE> transferFunction,
			CompilationUnitTACs eclipseTAC) {
		super(crystal);
		this.driver = new BranchSensitiveTACAnalysisDriver<LE>(transferFunction, eclipseTAC);
		// TODO use the driver as the analysis context
		transferFunction.setAnalysisContext(this);
	}

	public LE getResultsAfter(TACInstruction instr) {
		ASTNode node = instr.getNode();
		TACInstruction rootInstr = this.driver.tac.instruction(node);
		if(rootInstr == instr) {
			return getResultsAfter(node);
		}
		else if(rootInstr instanceof EclipseInstructionSequence) {
			EclipseInstructionSequence seq = (EclipseInstructionSequence) rootInstr;
			LE incoming = this.driver.tf.getAnalysisDirection() == AnalysisDirection.BACKWARD_ANALYSIS ?
					getResultsOrNullAfter(node) : getResultsOrNullBefore(node);
			if(incoming == null) 
				// no result available -> return bottom
				return getResultsAfter(node);
			else
				// derive result for needed instruction in sequence
				return mergeLabeledResult(this.driver.deriveResult(seq, incoming, instr, true), node);
		}
		else
			throw new UnsupportedOperationException("Can't determine results for instruction: " + instr);
	}

	public LE getResultsBefore(TACInstruction instr) {
		ASTNode node = instr.getNode();
		TACInstruction rootInstr = this.driver.tac.instruction(node);
		if(rootInstr == instr) {
			return getResultsBefore(node);
		}
		else if(rootInstr instanceof EclipseInstructionSequence) {
			EclipseInstructionSequence seq = (EclipseInstructionSequence) rootInstr;
			LE incoming = this.driver.tf.getAnalysisDirection() == AnalysisDirection.BACKWARD_ANALYSIS ?
					getResultsOrNullAfter(node) : getResultsOrNullBefore(node);
			if(incoming == null) 
				// no result available -> return bottom
				return getResultsBefore(node);
			else
				// derive result for needed instruction in sequence
				return mergeLabeledResult(this.driver.deriveResult(seq, incoming, instr, false), node);
		}
		else
			throw new UnsupportedOperationException("Can't determine results for instruction: " + instr);
	}

	public IResult<LE> getLabeledResultsAfter(TACInstruction instr) {
		ASTNode node = instr.getNode();
		TACInstruction rootInstr = this.driver.tac.instruction(node);
		if(rootInstr == instr) {
			return getLabeledResultsAfter(node);
		}
		else if(rootInstr instanceof EclipseInstructionSequence) {
			EclipseInstructionSequence seq = (EclipseInstructionSequence) rootInstr;
			LE incoming = this.driver.tf.getAnalysisDirection() == AnalysisDirection.BACKWARD_ANALYSIS ?
					getResultsOrNullAfter(node) : getResultsOrNullBefore(node);
			if(incoming == null) 
				// no result available -> return bottom
				return getLabeledResultsAfter(node);
			else
				// derive result for needed instruction in sequence
				return this.driver.deriveResult(seq, incoming, instr, true);
		}
		else
			throw new UnsupportedOperationException("Can't determine results for instruction: " + instr);
	}

	public IResult<LE> getLabeledResultsBefore(TACInstruction instr) {
		ASTNode node = instr.getNode();
		TACInstruction rootInstr = this.driver.tac.instruction(node);
		if(rootInstr == instr) {
			return getLabeledResultsBefore(node);
		}
		else if(rootInstr instanceof EclipseInstructionSequence) {
			EclipseInstructionSequence seq = (EclipseInstructionSequence) rootInstr;
			LE incoming = this.driver.tf.getAnalysisDirection() == AnalysisDirection.BACKWARD_ANALYSIS ?
					getResultsOrNullAfter(node) : getResultsOrNullBefore(node);
			if(incoming == null) 
				// no result available -> return bottom
				return getLabeledResultsBefore(node);
			else
				// derive result for needed instruction in sequence
				return this.driver.deriveResult(seq, incoming, instr, false);
		}
		else
			throw new UnsupportedOperationException("Can't determine results for instruction: " + instr);
	}

	public ASTNode getNode(Variable x, TACInstruction instruction) {
		if(x instanceof TempVariable) {
			return ((TempVariable) x).getNode();
		}
		// TODO return better node for source, type, and keyword variables
		return instruction.getNode();
	}

	/**
	 * Returns the TAC variable for a given ASTNode <i>after
	 * previously analyzing the method surrounding the given node.</i>
	 * It is the caller's responsibility to make sure to call this
	 * method only when analysis results for the surrounding method
	 * are available.
	 * @param node AST node in the previously analyzed method.
	 * @return The TAC variable for a given ASTNode.
	 */
	public Variable getVariable(ASTNode node) {
		if(hasResults(node) || findSurroundingMethod(node) == getCurrentMethod())
			return driver.tac.variable(node);
		throw new IllegalArgumentException("Not currently analyzing method surrounding node: " + node);
	}
	
	/**
	 * Returns the <b>this</b> variable for a given method <i>after
	 * previously analyzing that method.</i>
	 * It is the caller's responsibility to make sure to call this
	 * method only when analysis results for the given method
	 * are available.
	 * @param methodDecl The method for which <b>this</b> is requested.
	 * @return The <b>this</b> variable for the given method. 
	 */
	public ThisVariable getThisVariable(MethodDeclaration methodDecl) {
		if(methodDecl == null || methodDecl != getCurrentMethod())
			throw new IllegalArgumentException("Not currently analyzing method: " + methodDecl);
		return driver.tac.thisVariable();
	}
	
	public ThisVariable getThisVariable() {
		return driver.tac.thisVariable();
	}
	
	public SuperVariable getSuperVariable() {
		return driver.tac.superVariable(null);
	}
	
	/**
	 * Returns the variable for a given parameter or local <i>after
	 * previously analyzing the method declaring the parameter or local.</i>
	 * It is the caller's responsibility to make sure to call this
	 * method only when analysis results for the declaring method 
	 * are available.
	 * @param varBinding Binding of a local or parameter.
	 * @return the variable for the given parameter or local.
	 */
	public SourceVariable getSourceVariable(IVariableBinding varBinding) {
		if(varBinding.getDeclaringMethod() == null)
			throw new IllegalArgumentException("Not a local or parameter: " + varBinding);
		if(varBinding.getDeclaringMethod().equals(getCurrentMethod().resolveBinding()) == false)
			throw new IllegalArgumentException("Not currently analyzing method declaring variable: " + varBinding);
		return driver.tac.sourceVariable(varBinding);
	}
	
	public MethodDeclaration getAnalyzedMethod() {
		return getCurrentMethod();
	}

	/**
	 * Returns the implicit <b>this</b> variable for accessing a
	 * given method or field  <i>after previously analyzing the method
	 * surrounding the access.</i>
	 * It is the caller's responsibility to make sure to call this
	 * method only when analysis results for the method surrounding the
	 * access are available.
	 * @param accessedElement
	 * @return
	 */
	public ThisVariable getImplicitThisVariable(IBinding accessedElement) {
		// TODO make sure this is only called for accesses from currently analyzed method
		return driver.tac.implicitThisVariable(accessedElement);
	}
	
	@Override
	protected IFlowAnalysisDefinition<LE> createTransferFunction(MethodDeclaration method) {
		driver.switchToMethod(method);
		return driver;
	}

	protected abstract static class 
	AbstractTACAnalysisDriver<LE extends LatticeElement<LE>, TF extends IFlowAnalysisDefinition<LE>> 
	implements IFlowAnalysisDefinition<LE> {

		protected TF tf;
		protected EclipseTAC tac;
		protected final CompilationUnitTACs compUnitTacs;
		
		public AbstractTACAnalysisDriver(TF tf, CompilationUnitTACs compUnitTacs) {
			super();
			this.tf = tf;
			this.compUnitTacs = compUnitTacs;
		}
		
		/**
		 * @param methodDecl
		 */
		public void switchToMethod(MethodDeclaration methodDecl) {
			this.tac = this.compUnitTacs.getMethodTAC(methodDecl);
		}
		
		public AnalysisDirection getAnalysisDirection() {
			return tf.getAnalysisDirection();
		}
		
		public Lattice<LE> getLattice(MethodDeclaration methodDeclaration) {
			return tf.getLattice(methodDeclaration);
		}
		
		public abstract IResult<LE> deriveResult(EclipseInstructionSequence seq, LE incoming, TACInstruction targetInstruction, boolean afterResult);
	}
	
	protected static class BranchInsensitiveTACAnalysisDriver<LE extends LatticeElement<LE>> 
	extends AbstractTACAnalysisDriver<LE, ITACTransferFunction<LE>> 
	implements ITransferFunction<LE> {
		
		public BranchInsensitiveTACAnalysisDriver(ITACTransferFunction<LE> tf,
				CompilationUnitTACs compUnitTacs) {
			super(tf, compUnitTacs);
		}
		
		public LE transfer(ASTNode astNode, LE incoming) {
			LE result;
			TACInstruction instr = tac.instruction(astNode);
			if(instr == null)
				result = incoming;
			else
				result = instr.transfer(tf, incoming);
			return result;
		}
		
		public IResult<LE> deriveResult(EclipseInstructionSequence seq, LE incoming, TACInstruction targetInstruction, boolean afterResult) {
			return SingleResult.createSingleResult(seq.deriveResult(tf, targetInstruction, incoming, afterResult));
		}
	}		
	protected class BranchSensitiveTACAnalysisDriver<LE extends LatticeElement<LE>> 
	extends AbstractTACAnalysisDriver<LE, ITACBranchSensitiveTransferFunction<LE>> 
	implements IBranchSensitiveTransferFunction<LE> {
		
		public BranchSensitiveTACAnalysisDriver(ITACBranchSensitiveTransferFunction<LE> tf,
				CompilationUnitTACs compUnitTacs) {
			super(tf, compUnitTacs);
		}

		public IResult<LE> transfer(ASTNode astNode, List<ILabel> labels, LE value) {
			TACInstruction instr = tac.instruction(astNode);
			if(instr == null)
				return new LabeledSingleResult<LE>(value, labels);
			else
				return instr.transfer(tf, labels, value);
		}
		
		public IResult<LE> deriveResult(EclipseInstructionSequence seq, LE incoming, 
				TACInstruction targetInstruction, boolean afterResult) {
			Set<ILabel> labels = tf.getAnalysisDirection() == AnalysisDirection.BACKWARD_ANALYSIS ?
					getLabeledResultsBefore(targetInstruction.getNode()).keySet() :
						getLabeledResultsAfter(targetInstruction.getNode()).keySet();
			return seq.deriveResult(tf, new ArrayList<ILabel>(labels), targetInstruction, incoming, afterResult);
		}
	}

}
