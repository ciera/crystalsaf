package edu.cmu.cs.crystal.analysis.npe.branch;

import java.util.List;

import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;

import edu.cmu.cs.crystal.annotations.AnnotationDatabase;
import edu.cmu.cs.crystal.annotations.AnnotationSummary;
import edu.cmu.cs.crystal.flow.BooleanLabel;
import edu.cmu.cs.crystal.flow.ILabel;
import edu.cmu.cs.crystal.flow.ILatticeOperations;
import edu.cmu.cs.crystal.flow.IResult;
import edu.cmu.cs.crystal.flow.LabeledResult;
import edu.cmu.cs.crystal.flow.LabeledSingleResult;
import edu.cmu.cs.crystal.simple.TupleLatticeElement;
import edu.cmu.cs.crystal.simple.TupleLatticeOperations;
import edu.cmu.cs.crystal.tac.AbstractTACBranchSensitiveTransferFunction;
import edu.cmu.cs.crystal.tac.model.ArrayInitInstruction;
import edu.cmu.cs.crystal.tac.model.BinaryOperation;
import edu.cmu.cs.crystal.tac.model.BinaryOperator;
import edu.cmu.cs.crystal.tac.model.CopyInstruction;
import edu.cmu.cs.crystal.tac.model.LoadLiteralInstruction;
import edu.cmu.cs.crystal.tac.model.MethodCallInstruction;
import edu.cmu.cs.crystal.tac.model.NewArrayInstruction;
import edu.cmu.cs.crystal.tac.model.NewObjectInstruction;
import edu.cmu.cs.crystal.tac.model.Variable;

/**
 * These transfer functions are identical to the ones for the annotated NPE analysis except
 * 1) Its now branching, so the transfer function signatures are slightly different.
 * 2) we transfer on BinaryOperation and put different values down the two branches.
 * 
 * @author ciera
 *
 */
public class NPEBranchingTransferFunction extends AbstractTACBranchSensitiveTransferFunction<TupleLatticeElement<Variable, NullLatticeElement>> {
	/**
	 * The operations for this lattice. We want to have a tuple lattice from variables to null lattice elements, so we
	 * give it an instance of NullLatticeOperations. We also want the default value to be maybe null.
	 */
	TupleLatticeOperations<Variable, NullLatticeElement> ops =
		new TupleLatticeOperations<Variable, NullLatticeElement>(new NullLatticeOperations(), NullLatticeElement.MAYBE_NULL);
	private AnnotationDatabase annoDB;
	
	public NPEBranchingTransferFunction(AnnotationDatabase annoDB) {
		this.annoDB = annoDB;
	}

	/**
	 * The operations will create a default lattice which will map all variables to maybe null (since that was our default).
	 * 
	 * Of course, "this" should never be null.
	 */
	public TupleLatticeElement<Variable, NullLatticeElement> createEntryValue(
			MethodDeclaration method) {
		TupleLatticeElement<Variable, NullLatticeElement> def = ops.getDefault();
		def.put(getAnalysisContext().getThisVariable(), NullLatticeElement.NOT_NULL);
		
		AnnotationSummary summary = annoDB.getSummaryForMethod(method.resolveBinding());
		
		for (int ndx = 0; ndx < method.parameters().size(); ndx++) {
			SingleVariableDeclaration decl = (SingleVariableDeclaration) method.parameters().get(ndx);
			Variable paramVar = getAnalysisContext().getSourceVariable(decl.resolveBinding());
			
			if (summary.getParameter(ndx, BranchingNPEAnalysis.NON_NULL_ANNO) != null) //is this parameter annotated with @Nonnull?
				def.put(paramVar, NullLatticeElement.NOT_NULL);
		}
		
		return def;
	}

	/**
	 * Just return our lattice ops.
	 */
	public ILatticeOperations<TupleLatticeElement<Variable, NullLatticeElement>> getLatticeOperations() {
		return ops;
	}

	@Override
	public IResult<TupleLatticeElement<Variable, NullLatticeElement>> transfer(
			ArrayInitInstruction instr, List<ILabel> labels,
			TupleLatticeElement<Variable, NullLatticeElement> value) {
		value.put(instr.getTarget(), NullLatticeElement.NOT_NULL);
		return LabeledSingleResult.createResult(value, labels);
	}

	@Override
	public IResult<TupleLatticeElement<Variable, NullLatticeElement>> transfer(
			BinaryOperation binop, List<ILabel> labels,
			TupleLatticeElement<Variable, NullLatticeElement> value) {
		
		//first, check for == or !=
		if (binop.getOperator() != BinaryOperator.REL_EQ &&  binop.getOperator() != BinaryOperator.REL_NEQ)
			return LabeledSingleResult.createResult(value, labels);

		//are they even different anyway?
		NullLatticeElement leftValue = value.get(binop.getOperand1());
		NullLatticeElement rightValue = value.get(binop.getOperand2());
		if (rightValue == leftValue)
			return LabeledSingleResult.createResult(value, labels);
		
		//now, check labels
		if (labels.contains(BooleanLabel.getBooleanLabel(true)) && labels.contains(BooleanLabel.getBooleanLabel(false))) {
			LabeledResult<TupleLatticeElement<Variable, NullLatticeElement>> result =
				LabeledResult.createResult(value);
			
			TupleLatticeElement<Variable, NullLatticeElement> tVal = ops.copy(value);
			TupleLatticeElement<Variable, NullLatticeElement> fVal = ops.copy(value);
			
			if (rightValue == NullLatticeElement.NULL || leftValue == NullLatticeElement.NULL) {
				Variable opToChange = leftValue == NullLatticeElement.NULL ? binop.getOperand2() : binop.getOperand1();
				if (binop.getOperator() == BinaryOperator.REL_EQ) {
					tVal.put(opToChange, NullLatticeElement.NULL);
					fVal.put(opToChange, NullLatticeElement.NOT_NULL);
				}
				else {
					fVal.put(opToChange, NullLatticeElement.NULL);
					tVal.put(opToChange, NullLatticeElement.NOT_NULL);
				}
			}
			else if (rightValue == NullLatticeElement.NOT_NULL || leftValue == NullLatticeElement.NOT_NULL) {
				Variable opToChange = leftValue == NullLatticeElement.NOT_NULL ? binop.getOperand2() : binop.getOperand1();
				if (binop.getOperator() == BinaryOperator.REL_EQ) {
					tVal.put(opToChange, NullLatticeElement.NOT_NULL);
				}
				else {
					fVal.put(opToChange, NullLatticeElement.NOT_NULL);
				}
			}
			
			result.put(BooleanLabel.getBooleanLabel(true), tVal);
			result.put(BooleanLabel.getBooleanLabel(false), fVal);
			return result;
		}
		else
			return LabeledSingleResult.createResult(value, labels);
	}


	@Override
	public IResult<TupleLatticeElement<Variable, NullLatticeElement>> transfer(
			CopyInstruction instr, List<ILabel> labels,
			TupleLatticeElement<Variable, NullLatticeElement> value) {
		value.put(instr.getTarget(), value.get(instr.getOperand()));
		return LabeledSingleResult.createResult(value, labels);
	}

	@Override
	public IResult<TupleLatticeElement<Variable, NullLatticeElement>> transfer(
			LoadLiteralInstruction instr, List<ILabel> labels,
			TupleLatticeElement<Variable, NullLatticeElement> value) {
		if (instr.isNull())
			value.put(instr.getTarget(), NullLatticeElement.NULL);
		else
			value.put(instr.getTarget(), NullLatticeElement.NOT_NULL);
		return LabeledSingleResult.createResult(value, labels);
	}

	@Override
	public IResult<TupleLatticeElement<Variable, NullLatticeElement>> transfer(
			MethodCallInstruction instr, List<ILabel> labels,
			TupleLatticeElement<Variable, NullLatticeElement> value) {
		AnnotationSummary summary = annoDB.getSummaryForMethod(instr.resolveBinding());
		
		for (int ndx = 0; ndx < instr.getArgOperands().size(); ndx++) {
			Variable paramVar = instr.getArgOperands().get(ndx);
			
			if (summary.getParameter(ndx, BranchingNPEAnalysis.NON_NULL_ANNO) != null) //is this parameter annotated with @Nonnull?
				value.put(paramVar, NullLatticeElement.NOT_NULL);
		}
		
		if (summary.getReturn(BranchingNPEAnalysis.NON_NULL_ANNO) != null) 
			value.put(instr.getTarget(), NullLatticeElement.NOT_NULL);
		
		//clearly, the receiver is not null if this method call does actually occur ;)
		value.put(instr.getReceiverOperand(), NullLatticeElement.NOT_NULL);

		return LabeledSingleResult.createResult(value, labels);
	}

	@Override
	public IResult<TupleLatticeElement<Variable, NullLatticeElement>> transfer(
			NewArrayInstruction instr, List<ILabel> labels,
			TupleLatticeElement<Variable, NullLatticeElement> value) {
		value.put(instr.getTarget(), NullLatticeElement.NOT_NULL);
		return LabeledSingleResult.createResult(value, labels);
	}

	@Override
	public IResult<TupleLatticeElement<Variable, NullLatticeElement>> transfer(
			NewObjectInstruction instr, List<ILabel> labels,
			TupleLatticeElement<Variable, NullLatticeElement> value) {
		value.put(instr.getTarget(), NullLatticeElement.NOT_NULL);
		return LabeledSingleResult.createResult(value, labels);
	}
}
