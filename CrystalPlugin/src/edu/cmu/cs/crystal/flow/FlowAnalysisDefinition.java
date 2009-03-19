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
package edu.cmu.cs.crystal.flow;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.AnnotationTypeDeclaration;
import org.eclipse.jdt.core.dom.AnnotationTypeMemberDeclaration;
import org.eclipse.jdt.core.dom.AnonymousClassDeclaration;
import org.eclipse.jdt.core.dom.ArrayAccess;
import org.eclipse.jdt.core.dom.ArrayCreation;
import org.eclipse.jdt.core.dom.ArrayInitializer;
import org.eclipse.jdt.core.dom.ArrayType;
import org.eclipse.jdt.core.dom.AssertStatement;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.BlockComment;
import org.eclipse.jdt.core.dom.BooleanLiteral;
import org.eclipse.jdt.core.dom.BreakStatement;
import org.eclipse.jdt.core.dom.CastExpression;
import org.eclipse.jdt.core.dom.CatchClause;
import org.eclipse.jdt.core.dom.CharacterLiteral;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ConditionalExpression;
import org.eclipse.jdt.core.dom.ConstructorInvocation;
import org.eclipse.jdt.core.dom.ContinueStatement;
import org.eclipse.jdt.core.dom.DoStatement;
import org.eclipse.jdt.core.dom.EmptyStatement;
import org.eclipse.jdt.core.dom.EnhancedForStatement;
import org.eclipse.jdt.core.dom.EnumConstantDeclaration;
import org.eclipse.jdt.core.dom.EnumDeclaration;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.Initializer;
import org.eclipse.jdt.core.dom.InstanceofExpression;
import org.eclipse.jdt.core.dom.Javadoc;
import org.eclipse.jdt.core.dom.LabeledStatement;
import org.eclipse.jdt.core.dom.LineComment;
import org.eclipse.jdt.core.dom.MarkerAnnotation;
import org.eclipse.jdt.core.dom.MemberRef;
import org.eclipse.jdt.core.dom.MemberValuePair;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.MethodRef;
import org.eclipse.jdt.core.dom.MethodRefParameter;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.NormalAnnotation;
import org.eclipse.jdt.core.dom.NullLiteral;
import org.eclipse.jdt.core.dom.NumberLiteral;
import org.eclipse.jdt.core.dom.PackageDeclaration;
import org.eclipse.jdt.core.dom.ParameterizedType;
import org.eclipse.jdt.core.dom.ParenthesizedExpression;
import org.eclipse.jdt.core.dom.PostfixExpression;
import org.eclipse.jdt.core.dom.PrefixExpression;
import org.eclipse.jdt.core.dom.PrimitiveType;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.QualifiedType;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.SingleMemberAnnotation;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jdt.core.dom.SuperConstructorInvocation;
import org.eclipse.jdt.core.dom.SuperFieldAccess;
import org.eclipse.jdt.core.dom.SuperMethodInvocation;
import org.eclipse.jdt.core.dom.SwitchCase;
import org.eclipse.jdt.core.dom.SwitchStatement;
import org.eclipse.jdt.core.dom.SynchronizedStatement;
import org.eclipse.jdt.core.dom.TagElement;
import org.eclipse.jdt.core.dom.TextElement;
import org.eclipse.jdt.core.dom.ThisExpression;
import org.eclipse.jdt.core.dom.ThrowStatement;
import org.eclipse.jdt.core.dom.TryStatement;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclarationStatement;
import org.eclipse.jdt.core.dom.TypeLiteral;
import org.eclipse.jdt.core.dom.TypeParameter;
import org.eclipse.jdt.core.dom.VariableDeclarationExpression;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.core.dom.WhileStatement;
import org.eclipse.jdt.core.dom.WildcardType;

import edu.cmu.cs.crystal.internal.CrystalRuntimeException;

/**
 * Defines the transfer functions that manipulate the analysis knowledge through
 * lattices.  Override the transfer functions of interest with the logic appropriate
 * to your analysis.
 * 
 * @author David Dickey
 * @author Jonathan Aldrich
 *
 * @param <LE>	the LatticeElement subclass that represents the analysis knowledge
 */
public abstract class FlowAnalysisDefinition<LE> 
		implements ITransferFunction<LE> {
	
	
	public FlowAnalysisDefinition() {
		
	}

	/**
	 * Informs the FlowAnalysis which direction to perform the analysis.
	 * Default is a Forward analysis.
	 * <p>
	 * Use AnalysisDirection enumeration
	 * 
	 * @return	the direction of the analysis
	 */
	public AnalysisDirection getAnalysisDirection() {
		return AnalysisDirection.FORWARD_ANALYSIS;
	}
	
	/**
	 * Retrieves the entry lattice for a method.
	 * 
	 * @param d		the method to get the entry lattice for
	 * @return		the entry lattice for the specified method
	 */
	public abstract ILatticeOperations<LE> createLatticeOperations(MethodDeclaration d);
	
	    
	/*
	 * transfer functions, one for each kind of ASTNode
	 * Default passes back the LatticeElement
	 * 
	 * MEANT TO BE OVERRIDDEN IF OF INTEREST
	 */
	
	public LE transfer(AnnotationTypeDeclaration node, LE value) {
		return value;
	}

	public LE transfer(AnnotationTypeMemberDeclaration node, LE value) {
		return value;
	}

	public LE transfer(AnonymousClassDeclaration node, LE value) {
		return value;
	}

	public LE transfer(ArrayAccess node, LE value) {
		return value;
	}

	public LE transfer(ArrayCreation node, LE value) {
		return value;
	}

	public LE transfer(ArrayInitializer node, LE value) {
		return value;
	}

	public LE transfer(ArrayType node, LE value) {
		return value;
	}
	public LE transfer(AssertStatement node, LE value) {
		return value;
	}

	public LE transfer(Assignment node, LE value) {
		return value;
	}

	public LE transfer(Block node, LE value) {
		return value;
	}

	public LE transfer(BlockComment node, LE value) {
		return value;
	}

	public LE transfer(BooleanLiteral node, LE value) {
		return value;
	}

	public LE transfer(BreakStatement node, LE value) {
		return value;
	}

	public LE transfer(CastExpression node, LE value) {
		return value;
	}

	public LE transfer(CatchClause node, LE value) {
		return value;
	}

	public LE transfer(CharacterLiteral node, LE value) {
		return value;
	}

	public LE transfer(ClassInstanceCreation node, LE value) {
		return value;
	}

	public LE transfer(CompilationUnit node, LE value) {
		return value;
	}

	public LE transfer(ConditionalExpression node, LE value) {
		return value;
	}

	public LE transfer(ConstructorInvocation node, LE value) {
		return value;
	}

	public LE transfer(ContinueStatement node, LE value) {
		return value;
	}

	public LE transfer(DoStatement node, LE value) {
		return value;
	}

	public LE transfer(EmptyStatement node, LE value) {
		return value;
	}

	public LE transfer(EnhancedForStatement node, LE value) {
		return value;
	}

	public LE transfer(EnumConstantDeclaration node, LE value) {
		return value;
	}

	public LE transfer(EnumDeclaration node, LE value) {
		return value;
	}

	public LE transfer(ExpressionStatement node, LE value) {
		return value;
	}

	public LE transfer(FieldAccess node, LE value) {
		return value;
	}

	public LE transfer(FieldDeclaration node, LE value) {
		return value;
	}

	public LE transfer(ForStatement node, LE value) {
		return value;
	}

	public LE transfer(IfStatement node, LE value) {
		return value;
	}

	public LE transfer(ImportDeclaration node, LE value) {
		return value;
	}

	public LE transfer(InfixExpression node, LE value) {
		return value;
	}

	public LE transfer(Initializer node, LE value) {
		return value;
	}

	public LE transfer(InstanceofExpression node, LE value) {
		return value;
	}

	public LE transfer(Javadoc node, LE value) {
		return value;
	}

	public LE transfer(LabeledStatement node, LE value) {
		return value;
	}

	public LE transfer(LineComment node, LE value) {
		return value;
	}

	public LE transfer(MarkerAnnotation node, LE value) {
		return value;
	}

	public LE transfer(MemberRef node, LE value) {
		return value;
	}

	public LE transfer(MemberValuePair node, LE value) {
		return value;
	}

	public LE transfer(MethodDeclaration node, LE value) {
		return value;
	}

	public LE transfer(MethodInvocation node, LE value) {
		return value;
	}

	public LE transfer(MethodRef node, LE value) {
		return value;
	}

	public LE transfer(MethodRefParameter node, LE value) {
		return value;
	}

	public LE transfer(Modifier node, LE value) {
		return value;
	}

	public LE transfer(NormalAnnotation node, LE value) {
		return value;
	}

	public LE transfer(NullLiteral node, LE value) {
		return value;
	}

	public LE transfer(NumberLiteral node, LE value) {
		return value;
	}

	public LE transfer(PackageDeclaration node, LE value) {
		return value;
	}

	public LE transfer(ParameterizedType node, LE value) {
		return value;
	}

	public LE transfer(ParenthesizedExpression node, LE value) {
		return value;
	}

	public LE transfer(PostfixExpression node, LE value) {
		return value;
	}

	public LE transfer(PrefixExpression node, LE value) {
		return value;
	}

	public LE transfer(PrimitiveType node, LE value) {
		return value;
	}

	public LE transfer(QualifiedName node, LE value) {
		return value;
	}

	public LE transfer(QualifiedType node, LE value) {
		return value;
	}

	public LE transfer(ReturnStatement node, LE value) {
		return value;
	}

	public LE transfer(SimpleName node, LE value) {
		return value;
	}

	public LE transfer(SimpleType node, LE value) {
		return value;
	}

	public LE transfer(SingleMemberAnnotation node, LE value) {
		return value;
	}

	public LE transfer(SingleVariableDeclaration node, LE value) {
		return value;
	}

	public LE transfer(StringLiteral node, LE value) {
		return value;
	}

	public LE transfer(SuperConstructorInvocation node, LE value) {
		return value;
	}

	public LE transfer(SuperFieldAccess node, LE value) {
		return value;
	}

	public LE transfer(SuperMethodInvocation node, LE value) {
		return value;
	}

	public LE transfer(SwitchCase node, LE value) {
		return value;
	}

	public LE transfer(SwitchStatement node, LE value) {
		return value;
	}

	public LE transfer(SynchronizedStatement node, LE value) {
		return value;
	}

	public LE transfer(TagElement node, LE value) {
		return value;
	}

	public LE transfer(TextElement node, LE value) {
		return value;
	}

	public LE transfer(ThisExpression node, LE value) {
		return value;
	}

	public LE transfer(ThrowStatement node, LE value) {
		return value;
	}

	public LE transfer(TryStatement node, LE value) {
		return value;
	}

	public LE transfer(TypeDeclaration node, LE value) {
		return value;
	}

	public LE transfer(TypeDeclarationStatement node, LE value) {
		return value;
	}

	public LE transfer(TypeLiteral node, LE value) {
		return value;
	}

	public LE transfer(TypeParameter node, LE value) {
		return value;
	}

	public LE transfer(VariableDeclarationExpression node, LE value) {
		return value;
	}

	public LE transfer(VariableDeclarationFragment node, LE value) {
		return value;
	}

	public LE transfer(VariableDeclarationStatement node, LE value) {
		return value;
	}

	public LE transfer(WhileStatement node, LE value) {
		return value;
	}

	public LE transfer(WildcardType node, LE value) {
		return value;
	}

	/**
	 * This method calls the correct transfer function from a generic
	 * ASTNode.
	 * 
	 * TODO: Make this process cleaner
	 * 
	 * @param node	the ASTNode that the flow is transfering through
	 * @param value	the lattice before the transfer
	 * @return	the lattice after the transfer
	 */
	public LE transfer(ASTNode node, LE value) {
		if(node == null) {
			throw new CrystalRuntimeException("null ASTNode");
		} else if (node instanceof AnnotationTypeDeclaration) {
			return transfer((AnnotationTypeDeclaration) node, value);
		} else if (node instanceof AnnotationTypeMemberDeclaration) {
			return transfer((AnnotationTypeMemberDeclaration) node, value);
		} else if(node instanceof AnonymousClassDeclaration) {
			return transfer((AnonymousClassDeclaration) node, value);
		} else if(node instanceof ArrayAccess) {
			return transfer((ArrayAccess) node, value);
		} else if(node instanceof ArrayCreation) {
			return transfer((ArrayCreation) node, value);
		} else if(node instanceof ArrayInitializer) {
			return transfer((ArrayInitializer) node, value);
		} else if(node instanceof ArrayType) {
			return transfer((ArrayType) node, value);
		} else if(node instanceof AssertStatement) {
			return transfer((AssertStatement) node, value);
		} else if(node instanceof Assignment) {
			return transfer((Assignment) node, value);
		} else if(node instanceof Block) {
			return transfer((Block) node, value);
		} else if(node instanceof BlockComment) {
			return transfer((BlockComment) node, value);
		} else if(node instanceof BooleanLiteral) {
			return transfer((BooleanLiteral) node, value);
		} else if(node instanceof BreakStatement) {
			return transfer((BreakStatement) node, value);
		} else if(node instanceof CastExpression) {
			return transfer((CastExpression) node, value);
		} else if(node instanceof CatchClause) {
			return transfer((CatchClause) node, value);
		} else if(node instanceof CharacterLiteral) {
			return transfer((CharacterLiteral) node, value);
		} else if(node instanceof ClassInstanceCreation) {
			return transfer((ClassInstanceCreation) node, value);
		} else if(node instanceof CompilationUnit) {
			return transfer((CompilationUnit) node, value);
		} else if(node instanceof ConditionalExpression) {
			return transfer((ConditionalExpression) node, value);
		} else if(node instanceof ConstructorInvocation) {
			return transfer((ConstructorInvocation) node, value);
		} else if(node instanceof ContinueStatement) {
			return transfer((ContinueStatement) node, value);
		} else if(node instanceof DoStatement) {
			return transfer((DoStatement) node, value);
		} else if(node instanceof EmptyStatement) {
			return transfer((EmptyStatement) node, value);
		} else if(node instanceof EnhancedForStatement) {
			return transfer((EnhancedForStatement) node, value);
		} else if(node instanceof EnumConstantDeclaration) {
			return transfer((EnumConstantDeclaration) node, value);
		} else if(node instanceof EnumDeclaration) {
			return transfer((EnumDeclaration) node, value);
		} else if(node instanceof ExpressionStatement) {
			return transfer((ExpressionStatement) node, value);
		} else if(node instanceof FieldAccess) {
			return transfer((FieldAccess) node, value);
		} else if(node instanceof FieldDeclaration) {
			return transfer((FieldDeclaration) node, value);
		} else if(node instanceof ForStatement) {
			return transfer((ForStatement) node, value);
		} else if(node instanceof IfStatement) {
			return transfer((IfStatement) node, value);
		} else if(node instanceof ImportDeclaration) {
			return transfer((ImportDeclaration) node, value);
		} else if(node instanceof InfixExpression) {
			return transfer((InfixExpression) node, value);
		} else if(node instanceof Initializer) {
			return transfer((Initializer) node, value);
		} else if(node instanceof InstanceofExpression) {
			return transfer((InstanceofExpression) node, value);
		} else if(node instanceof Javadoc) {
			return transfer((Javadoc) node, value);
		} else if(node instanceof LabeledStatement) {
			return transfer((LabeledStatement) node, value);
		} else if(node instanceof LineComment) {
			return transfer((LineComment) node, value);
		} else if(node instanceof MarkerAnnotation) {
			return transfer((MarkerAnnotation) node, value);
		} else if(node instanceof MemberRef) {
			return transfer((MemberRef) node, value);
		} else if(node instanceof MemberValuePair) {
			return transfer((MemberValuePair) node, value);
		} else if(node instanceof MethodDeclaration) {
			return transfer((MethodDeclaration) node, value);
		} else if(node instanceof MethodInvocation) {
			return transfer((MethodInvocation) node, value);
		} else if(node instanceof MethodRef) {
			return transfer((MethodRef) node, value);
		} else if(node instanceof MethodRefParameter) {
			return transfer((MethodRefParameter) node, value);
		} else if(node instanceof Modifier) {
			return transfer((Modifier) node, value);
		} else if(node instanceof NormalAnnotation) {
			return transfer((NormalAnnotation) node, value);
		} else if(node instanceof NullLiteral) {
			return transfer((NullLiteral) node, value);
		} else if(node instanceof NumberLiteral) {
			return transfer((NumberLiteral) node, value);
		} else if(node instanceof PackageDeclaration) {
			return transfer((PackageDeclaration) node, value);
		} else if(node instanceof ParameterizedType) {
			return transfer((ParameterizedType) node, value);
		} else if(node instanceof ParenthesizedExpression) {
			return transfer((ParenthesizedExpression) node, value);
		} else if(node instanceof PostfixExpression) {
			return transfer((PostfixExpression) node, value);
		} else if(node instanceof PrefixExpression) {
			return transfer((PrefixExpression) node, value);
		} else if(node instanceof PrimitiveType) {
			return transfer((PrimitiveType) node, value);
		} else if(node instanceof QualifiedName) {
			return transfer((QualifiedName) node, value);
		} else if(node instanceof QualifiedType) {
			return transfer((QualifiedType) node, value);
		} else if(node instanceof ReturnStatement) {
			return transfer((ReturnStatement) node, value);
		} else if(node instanceof SimpleName) {
			return transfer((SimpleName) node, value);
		} else if(node instanceof SimpleType) {
			return transfer((SimpleType) node, value);
		} else if(node instanceof SingleMemberAnnotation) {
			return transfer((SingleMemberAnnotation) node, value);
		} else if(node instanceof SingleVariableDeclaration) {
			return transfer((SingleVariableDeclaration) node, value);
		} else if(node instanceof StringLiteral) {
			return transfer((StringLiteral) node, value);
		} else if(node instanceof SuperConstructorInvocation) {
			return transfer((SuperConstructorInvocation) node, value);
		} else if(node instanceof SuperFieldAccess) {
			return transfer((SuperFieldAccess) node, value);
		} else if(node instanceof SuperMethodInvocation) {
			return transfer((SuperMethodInvocation) node, value);
		} else if(node instanceof SwitchCase) {
			return transfer((SwitchCase) node, value);
		} else if(node instanceof SwitchStatement) {
			return transfer((SwitchStatement) node, value);
		} else if(node instanceof SynchronizedStatement) {
			return transfer((SynchronizedStatement) node, value);
		} else if(node instanceof TagElement) {
			return transfer((TagElement) node, value);
		} else if(node instanceof TextElement) {
			return transfer((TextElement) node, value);
		} else if(node instanceof ThisExpression) {
			return transfer((ThisExpression) node, value);
		} else if(node instanceof ThrowStatement) {
			return transfer((ThrowStatement) node, value);
		} else if(node instanceof TryStatement) {
			return transfer((TryStatement) node, value);
		} else if(node instanceof TypeDeclaration) {
			return transfer((TypeDeclaration) node, value);
		} else if(node instanceof TypeDeclarationStatement) {
			return transfer((TypeDeclarationStatement) node, value);
		} else if(node instanceof TypeLiteral) {
			return transfer((TypeLiteral) node, value);
		} else if(node instanceof TypeParameter) {
			return transfer((TypeParameter) node, value);
		} else if(node instanceof VariableDeclarationExpression) {
			return transfer((VariableDeclarationExpression) node, value);
		} else if(node instanceof VariableDeclarationFragment) {
			return transfer((VariableDeclarationFragment) node, value);
		} else if(node instanceof VariableDeclarationStatement) {
			return transfer((VariableDeclarationStatement) node, value);
		} else if(node instanceof WhileStatement) {
			return transfer((WhileStatement) node, value);
		} else if(node instanceof WildcardType) {
			return transfer((WildcardType) node, value);
		} else {
			throw new CrystalRuntimeException("Unknown ASTNode type [" + node.getClass().getSimpleName() + "]");
		}
	}

}
