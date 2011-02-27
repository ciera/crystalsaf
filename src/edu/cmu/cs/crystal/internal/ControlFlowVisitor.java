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
package edu.cmu.cs.crystal.internal;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
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
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
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
import org.eclipse.jdt.core.dom.Name;
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
import org.eclipse.jdt.core.dom.Statement;
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

import edu.cmu.cs.crystal.internal.ControlFlowNode.Direction;

/**
 * An ASTVisitor that traverses the ASTNode structure and connects ControlFlowNodes
 * to form a control flow graph of the ASTNode.
 *   
 * @author David Dickey
 *
 */
public class ControlFlowVisitor extends ASTVisitor {
	protected ControlFlowNode controlFlowNode;
	protected ControlFlowGraph controlFlowGraph;

	public ControlFlowVisitor(ControlFlowNode cfn) {
		if(cfn == null)
			throw new CrystalRuntimeException("Cannot create a ControlFlowVisitor for a null ControlFlowNode");
		controlFlowNode = cfn;
		controlFlowGraph = cfn.getControlFlowGraph();
		if(controlFlowGraph == null)
			throw new CrystalRuntimeException("ControlFlowNode was not part of a ControlFlowGraph");
	}
	
	/**
	 * Carries out the visit to the stored node.
	 */
	public void performVisit() {
		if(controlFlowNode.getASTNode() != null)
			controlFlowNode.getASTNode().accept(this);
		else
			throw new CrystalRuntimeException("Cannot visit a null ASTNode");
	}

	/**
	 * Called before performing a visit
	 */
	public void preVisit(ASTNode node) {
		// TEMP: Print out the visit
		// System.out.println(Utilities.ASTNodeToString(node));
	}
	
	/*
	 * The visit methods follow the following steps
	 * 1) Create Children Visitors
	 * 2) Reroute Control Flow to first Child from Parent
	 * 3) Add Edges between Children
	 * 4) Evaluate Children 
	 */
	
	/**
	 * AnnotationTypeDeclaration 
	 * 
	 * This node does not influence control flow.
	 */
	public boolean visit(AnnotationTypeDeclaration node) {
		return false;
	}
	/**
	 * AnnotationTypeMemberDeclaration 
	 * 
	 * This node does not influence control flow.
	 */
	public boolean visit(AnnotationTypeMemberDeclaration node) {
		return false;
	}
	/**
	 * AnonymousClassDeclaration 
	 * 
	 * This node does not influence control flow.
	 */
	public boolean visit(AnonymousClassDeclaration node) {
		return false;
	}
	/**
	 * Example: myArray[0]
	 */
	public boolean visit(ArrayAccess node) {
		if(node.getArray() == null)
			throw new CrystalRuntimeException("null array");
		if(node.getIndex() == null)
				throw new CrystalRuntimeException("null array index");
		ControlFlowNode array = controlFlowNode.newControlFlowNode(node.getArray());
		ControlFlowNode index = controlFlowNode.newControlFlowNode(node.getIndex());

		controlFlowNode.moveEdges(ControlFlowNode.Direction.BACKWARDS, array);

		array.addEdge(ControlFlowNode.Direction.FORWARDS, index);
		index.addEdge(ControlFlowNode.Direction.FORWARDS, controlFlowNode);

		array.evaluate();
		index.evaluate();
		
		return false;
	}
	/**
	 * Example: new int[10]{1,2,3,4,5,6,7,8,9,10};
	 */
	public boolean visit(ArrayCreation node) {
		List dimensions = node.dimensions();
		ArrayInitializer arrayInitializer = node.getInitializer();
		List<ControlFlowNode> cfns = null;

		if(dimensions != null && dimensions.size() > 0) {
			// Handle dimension expressions
			cfns = createCFNListFromASTNodeList(dimensions);
			ControlFlowNode cfn = cfns.get(0);
			controlFlowNode.moveEdges(ControlFlowNode.Direction.BACKWARDS, cfn);
			cfn = cfns.get(cfns.size() - 1);
			cfn.addEdge(ControlFlowNode.Direction.FORWARDS, controlFlowNode);
			evaluate(cfns);
		} else if (arrayInitializer != null) {
			// Handle initializer if there is one
			ControlFlowNode initializer = controlFlowNode.newControlFlowNode(arrayInitializer);
			controlFlowNode.moveEdges(ControlFlowNode.Direction.BACKWARDS, initializer);
			initializer.addEdge(ControlFlowNode.Direction.FORWARDS, controlFlowNode);
			initializer.evaluate();
		}
		return false;
	}
	/**
	 * Example: {1.2f, 2.3f, 3.4f}
	 */
	public boolean visit(ArrayInitializer node) {
		List expressions = node.expressions();
		if(expressions == null || expressions.size() == 0)
			return false;
		// Take the expression list and make more visitors from them.
		List<ControlFlowNode> cfns = createCFNListFromASTNodeList(expressions);
		ControlFlowNode cfn = cfns.get(0);
		controlFlowNode.moveEdges(ControlFlowNode.Direction.BACKWARDS, cfn);
		cfn = cfns.get(cfns.size() - 1);
		cfn.addEdge(ControlFlowNode.Direction.FORWARDS, controlFlowNode);
		
		evaluate(cfns);
		
		return false;
	}
	/**
	 * ArrayType 
	 * 
	 * This node does not influence control flow.
	 */
	public boolean visit(ArrayType node) {
		return false;
	}
	/**
	 * Example: assert (x != 0) : "x is " + x + " which is not 0";
	 */
	public boolean visit(AssertStatement node) {
		ControlFlowNode expression = controlFlowNode.newControlFlowNode(node.getExpression());
		Expression message = node.getMessage();
		
		controlFlowNode.moveEdges(ControlFlowNode.Direction.BACKWARDS, expression);

		if(message != null) {
			ControlFlowNode cfnMessage = controlFlowNode.newControlFlowNode(message);
			expression.addEdge(ControlFlowNode.Direction.FORWARDS, cfnMessage);
			cfnMessage.addEdge(ControlFlowNode.Direction.FORWARDS, controlFlowNode);
			
			expression.evaluate();
			cfnMessage.evaluate();
		} else {
			expression.addEdge(ControlFlowNode.Direction.FORWARDS, controlFlowNode);
			expression.evaluate();
		}
		
		return false;
	}
	/**
	 * Example: x = y
	 */
	public boolean visit(Assignment node) {
		ControlFlowNode leftCFN = controlFlowNode.newControlFlowNode(node.getLeftHandSide());
		ControlFlowNode rightCFN = controlFlowNode.newControlFlowNode(node.getRightHandSide());

		controlFlowNode.moveEdges(ControlFlowNode.Direction.BACKWARDS, leftCFN);

		leftCFN.addEdge(ControlFlowNode.Direction.FORWARDS, rightCFN);
		rightCFN.addEdge(ControlFlowNode.Direction.FORWARDS, controlFlowNode);

		leftCFN.evaluate();
		rightCFN.evaluate();
				
		return false;
	}
	/**
	 * Example: { int x = 1; x++; }   or  {}
	 */
	public boolean visit(Block node) {
		List statements = node.statements();
		
		if(statements == null || statements.size() == 0) {
			// controlFlowNode.remove();
			return false;
		}
		
		List<ControlFlowNode> cfns = createCFNListFromASTNodeList(statements);
		ControlFlowNode cfn = cfns.get(0);
		controlFlowNode.moveEdges(ControlFlowNode.Direction.BACKWARDS, cfn);
		cfn = cfns.get(cfns.size() - 1);
		// Remove the block node from the graph
		cfn.addEdge(ControlFlowNode.Direction.FORWARDS, controlFlowNode);
		//controlFlowNode.moveEdges(ControlFlowNode.Direction.FORWARDS, cfn);
		//controlFlowNode.remove();
		
		evaluate(cfns);
		
		return false;
	}
	/**
	 * BlockComment 
	 * 
	 * This node does not influence control flow.
	 */
	public boolean visit(BlockComment node) {
		return false;
	}
	/**
	 * BooleanLiteral 
	 * 
	 * This node does not influence control flow.
	 */
	public boolean visit(BooleanLiteral node) {
		// Leaf, no action necessary
		return false;
	}
	/**
	 * Example: break;   or   break ToHere;
	 */
	public boolean visit(BreakStatement node) {
		SimpleName simpleName = node.getLabel();
		String label;
		if(simpleName == null)
			label = null;
		else
			label = simpleName.getIdentifier();
		
		controlFlowNode.breaking(label, true);

		return false;
	}
	/**
	 * Example: (Square) myShape
	 */
	public boolean visit(CastExpression node) {
		ControlFlowNode expression = controlFlowNode.newControlFlowNode(node.getExpression());

		controlFlowNode.moveEdges(ControlFlowNode.Direction.BACKWARDS, expression);

		expression.addEdge(ControlFlowNode.Direction.FORWARDS, controlFlowNode);

		expression.evaluate();
		return false;
	}
	/**
	 * Example: catch (RuntimeException re) { System.out.println("RE: " + re); }
	 */
	public boolean visit(CatchClause node) {
		ControlFlowNode exception = controlFlowNode.newControlFlowNode(node.getException());
		ControlFlowNode body = controlFlowNode.newControlFlowNode(node.getBody());

		controlFlowNode.moveEdges(ControlFlowNode.Direction.BACKWARDS, exception);
		exception.addEdge(ControlFlowNode.Direction.FORWARDS, body);
		body.addEdge(ControlFlowNode.Direction.FORWARDS, controlFlowNode);

		exception.evaluate();
		body.evaluate();
		return false;
	}
	/**
	 * CharacterLiteral 
	 * 
	 * This node does not influence control flow.
	 */
	public boolean visit(CharacterLiteral node) {
		// Leaf, no action necessary
		return false;
	}
	/**
	 * Example: new MyClass(5, "hello");
	 */
	public boolean visit(ClassInstanceCreation node) {
		Expression expression = node.getExpression();
		List arguments = node.arguments();
		ControlFlowNode expressioncfn = null, last = null;
		List<ControlFlowNode> cfns = null;
		
		if(expression != null) {
			expressioncfn = controlFlowNode.newControlFlowNode(expression);
			controlFlowNode.moveEdges(ControlFlowNode.Direction.BACKWARDS, expressioncfn);
			last = expressioncfn;
		}

		if(arguments != null && arguments.size() > 0) {
			// Take the argument list and make more CFNs from them.
			cfns = createCFNListFromASTNodeList(arguments);
			if(expression == null)
				controlFlowNode.moveEdges(ControlFlowNode.Direction.BACKWARDS, cfns.get(0));
			else
				expressioncfn.addEdge(ControlFlowNode.Direction.FORWARDS, cfns.get(0));
			last = cfns.get(cfns.size() - 1);
		}
		
		if(last == null)
			return false;
		last.addEdge(ControlFlowNode.Direction.FORWARDS, controlFlowNode);
		
		if(expressioncfn != null)
			expressioncfn.evaluate();
		if(cfns != null)
			evaluate(cfns);
		
		return false;
	}
	/**
	 * CompilationUnit 
	 * 
	 * This node does not influence control flow.
	 */
	public boolean visit(CompilationUnit node) {
		return false;
	}
	/**
	 * Example: (isTrue) ? "yep" : "nope";
	 */
	public boolean visit(ConditionalExpression node) {
		ControlFlowNode conditionCFN = controlFlowNode.newControlFlowNode(node.getExpression());
		ControlFlowNode thenCFN = controlFlowNode.newControlFlowNode(node.getThenExpression());
		ControlFlowNode elseCFN = controlFlowNode.newControlFlowNode(node.getElseExpression());

		controlFlowNode.moveEdges(ControlFlowNode.Direction.BACKWARDS, conditionCFN);

		conditionCFN.addEdge(ControlFlowNode.Direction.FORWARDS, thenCFN);
		conditionCFN.addEdge(ControlFlowNode.Direction.FORWARDS, elseCFN);
		thenCFN.addEdge(ControlFlowNode.Direction.FORWARDS, controlFlowNode);
		elseCFN.addEdge(ControlFlowNode.Direction.FORWARDS, controlFlowNode);

		conditionCFN.evaluate();
		thenCFN.evaluate();
		elseCFN.evaluate();
		
		return false;
	}
	/**
	 * Example: this(1, 23, 2005);
	 */
	public boolean visit(ConstructorInvocation node) {
		List arguments = node.arguments();
		
		if(arguments == null || arguments.size() == 0) {
			return false;
		}
		
		// Handle dimension expressions
		List<ControlFlowNode> cfns = createCFNListFromASTNodeList(arguments);
		ControlFlowNode cfn = cfns.get(0);
		controlFlowNode.moveEdges(ControlFlowNode.Direction.BACKWARDS, cfn);
		cfn = cfns.get(cfns.size() - 1);
		cfn.addEdge(ControlFlowNode.Direction.FORWARDS, controlFlowNode);
		
		evaluate(cfns);
		
		return false;
	}
	/**
	 * Example: continue;   or   continue ToHere;
	 */
	public boolean visit(ContinueStatement node) {
		SimpleName simpleName = node.getLabel();
		String label;
		if(simpleName == null)
			label = null;
		else
			label = simpleName.getIdentifier();
		
		controlFlowNode.continuing(label, true);
		
		return false;
	}
	/**
	 * Example: do { int x = 5; method(x++); } while(x < 10); 
	 */
	public boolean visit(DoStatement node) {
//	    do Statement while ( Expression ) ;
//	       --- 1 ---         --- 2 ----
//	    ------------- 3 -> 1 --------------
//	    ------------- 3 -> exit -----------
	    
		ControlFlowNode exit = controlFlowNode.getNode(ControlFlowNode.Direction.FORWARDS);
		ControlFlowNode expressionCFN = controlFlowNode.newControlFlowNode(node.getExpression());
		ControlFlowNode bodyCFN = controlFlowNode.newControlFlowNode(node.getBody());
		controlFlowNode.moveEdges(ControlFlowNode.Direction.BACKWARDS, bodyCFN);

		bodyCFN.addEdge(ControlFlowNode.Direction.FORWARDS, expressionCFN);
		expressionCFN.addEdge(ControlFlowNode.Direction.FORWARDS, controlFlowNode);
		controlFlowNode.addEdge(ControlFlowNode.Direction.FORWARDS, bodyCFN);
		controlFlowNode.setLoopPaths(expressionCFN, exit);
		
		bodyCFN.evaluate();
		expressionCFN.evaluate();
		
		return false;
	}
	/**
	 * Example:  ;
	 */
	public boolean visit(EmptyStatement node) {
		return false;
	}

	public boolean visit(EnhancedForStatement node) {
//	    for ( FormalParameter : Expression ) Statement
//                              ---- 1 ---   --- 3 ---
//		------------------- 2 -> 3 -------------------
//		------------------- 2 -> exit ----------------

		// TODO: Add formal parameter, so a continue; makes more sense
		Expression expression = node.getExpression();
		Statement body = node.getBody();
		ControlFlowNode expressionCFN = null;
		ControlFlowNode bodyCFN = null;
		ControlFlowNode exit = controlFlowNode.getNode(ControlFlowNode.Direction.FORWARDS);
		
		if(expression == null)
			throw new CrystalRuntimeException("enchancedForStatement did not have an expression");
		if(body == null)
			throw new CrystalRuntimeException("No body defined for an EnhancedForStatment");
			
		expressionCFN = controlFlowNode.newControlFlowNode(expression);
		controlFlowNode.moveEdges(ControlFlowNode.Direction.BACKWARDS, expressionCFN);
		expressionCFN.addEdge(ControlFlowNode.Direction.FORWARDS, controlFlowNode);
		
		bodyCFN = controlFlowNode.newControlFlowNode(body);
		controlFlowNode.addEdge(ControlFlowNode.Direction.FORWARDS, bodyCFN);
		bodyCFN.addEdge(ControlFlowNode.Direction.FORWARDS, controlFlowNode);
		controlFlowNode.setLoopPaths(bodyCFN, exit);
		
		expressionCFN.evaluate();
		bodyCFN.evaluate();
		
		return false;
	}
	/**
	 * Example: enum MyEnum { CONST_1, CONST_2, CONST_2 }
	 */
	public boolean visit(EnumConstantDeclaration node) {
		List arguments = node.arguments();
		
		if(arguments == null || arguments.size() == 0) {
			return false;
		}
		
		// Handle dimension expressions
		List<ControlFlowNode> cfns = createCFNListFromASTNodeList(arguments);
		ControlFlowNode cfn = cfns.get(0);
		controlFlowNode.moveEdges(ControlFlowNode.Direction.BACKWARDS, cfn);
		cfn = cfns.get(cfns.size() - 1);
		cfn.addEdge(ControlFlowNode.Direction.FORWARDS, controlFlowNode);
		
		evaluate(cfns);
		
		return false;
	}
	/**
	 * Example: enum MyEnum { CONST_1, CONST_2 }
	 */
	public boolean visit(EnumDeclaration node) {
		return false;
	}
	/**
	 * Example: x = 5;
	 */
	public boolean visit(ExpressionStatement node) {
		// Candidate for Removal
		ControlFlowNode expressionCFN = controlFlowNode.newControlFlowNode(node.getExpression());
		controlFlowNode.moveEdges(ControlFlowNode.Direction.BACKWARDS, expressionCFN);
		expressionCFN.addEdge(ControlFlowNode.Direction.FORWARDS, controlFlowNode);
		expressionCFN.evaluate();
		return false;
	}
	/**
	 * Example: x = this.myField;
	 */
	public boolean visit(FieldAccess node) {
		Expression expression = node.getExpression();
		
		if(expression == null)
			return false;

		ControlFlowNode expressioncfn = controlFlowNode.newControlFlowNode(expression);
		controlFlowNode.moveEdges(ControlFlowNode.Direction.BACKWARDS, expressioncfn);

		expressioncfn.addEdge(ControlFlowNode.Direction.FORWARDS, controlFlowNode);

		expressioncfn.evaluate();
		return false;
	}
	/**
	 * Example: protected int myField = 18;
	 */
	public boolean visit(FieldDeclaration node) {
		List fragments = node.fragments();
		if(fragments == null || fragments.size() == 0)
			return false;
		List<ControlFlowNode> cfns = createCFNListFromASTNodeList(fragments);
		ControlFlowNode cfn = cfns.get(0);
		controlFlowNode.moveEdges(ControlFlowNode.Direction.BACKWARDS, cfn);
		cfn = cfns.get(cfns.size() - 1);
		cfn.addEdge(ControlFlowNode.Direction.FORWARDS, controlFlowNode);
		evaluate(cfns);
		
		return false;
	}

	public boolean visit(ForStatement node) {
//	    for ( [ ForInit ]; [ Expression ] ; [ ForUpdate ] ) Statement
//		       ---- 1 ----   ----- 2 ---    --- 5 -> 2 --   --- 4 ---
//		-------------------------- 3 -> 4 --------------------------
//		-------------------------- 3 -> exit -----------------------

		List initializers = node.initializers();
		Expression expression = node.getExpression();
		List updaters = node.updaters();
		Statement body = node.getBody();
		List<ControlFlowNode> initializercfns = null, updatercfns = null;
		ControlFlowNode expressioncfn = null, bodycfn = null, tempcfn;
		ControlFlowNode exit = controlFlowNode.getNode(ControlFlowNode.Direction.FORWARDS);
		ControlFlowNode first = controlFlowNode;
		
		// Initialize all CFNs AND setup CFG edges
		if(initializers != null && initializers.size() > 0) {
			initializercfns = createCFNListFromASTNodeList(initializers);
			first = initializercfns.get(0);
			controlFlowNode.moveEdges(ControlFlowNode.Direction.BACKWARDS, first);
		}
		if(expression != null) {
			expressioncfn = controlFlowNode.newControlFlowNode(expression);
			if(initializercfns == null) {
				controlFlowNode.moveEdges(ControlFlowNode.Direction.BACKWARDS, expressioncfn);
				first = expressioncfn;
			} else {
				tempcfn = initializercfns.get(initializercfns.size() - 1);
				tempcfn.addEdge(ControlFlowNode.Direction.FORWARDS, expressioncfn);
			}
			expressioncfn.addEdge(ControlFlowNode.Direction.FORWARDS, controlFlowNode);
		} else if(initializercfns != null) {
			tempcfn = initializercfns.get(initializercfns.size() - 1);
			tempcfn.addEdge(ControlFlowNode.Direction.FORWARDS, controlFlowNode);
		}
		if(updaters != null && updaters.size() > 0) {
			updatercfns = createCFNListFromASTNodeList(updaters);
			tempcfn = updatercfns.get(updatercfns.size() - 1);
			if(expressioncfn == null)
				tempcfn.addEdge(ControlFlowNode.Direction.FORWARDS, controlFlowNode);
			else
				tempcfn.addEdge(ControlFlowNode.Direction.FORWARDS, expressioncfn);
		}
		if(body == null)
			throw new CrystalRuntimeException("for statement with no body");
		
		bodycfn = controlFlowNode.newControlFlowNode(body);
		controlFlowNode.addEdge(ControlFlowNode.Direction.FORWARDS, bodycfn);
		if(updatercfns != null)
			bodycfn.addEdge(ControlFlowNode.Direction.FORWARDS, updatercfns.get(0));
		else if (expressioncfn != null)
			bodycfn.addEdge(ControlFlowNode.Direction.FORWARDS, expressioncfn);
		else
			bodycfn.addEdge(ControlFlowNode.Direction.FORWARDS, controlFlowNode);
		
		if(updatercfns != null)
			controlFlowNode.setLoopPaths(updatercfns.get(0), exit);
		else if(expressioncfn != null) 
			controlFlowNode.setLoopPaths(expressioncfn, exit);
		else
			controlFlowNode.setLoopPaths(bodycfn, exit);

		// Do visits
		
		if(initializercfns != null)
			evaluate(initializercfns);	
		if(expressioncfn != null)
			expressioncfn.evaluate();
		if(updatercfns != null)
				evaluate(updatercfns);
		if(bodycfn != null)
			bodycfn.evaluate();
		
		
		return false;
	}
	/**
	 * Example: if(bool) x = 5; else x = 7;
	 */
	public boolean visit(IfStatement node) {
		Expression expression = node.getExpression();
		Statement thenStatement = node.getThenStatement();
		Statement elseStatement = node.getElseStatement();

		ControlFlowNode conditionCFN = controlFlowNode.newControlFlowNode(expression);
		// conditionCFN.copyLabelsFrom(controlFlowNode);
		ControlFlowNode thenCFN = controlFlowNode.newControlFlowNode(thenStatement);
		ControlFlowNode elseCFN = null;
		controlFlowNode.moveEdges(ControlFlowNode.Direction.BACKWARDS, conditionCFN);
		// THEN
		conditionCFN.addEdge(ControlFlowNode.Direction.FORWARDS, thenCFN);
		thenCFN.addEdge(ControlFlowNode.Direction.FORWARDS, controlFlowNode);
	
		// ELSE
		if(elseStatement != null) {
			elseCFN = controlFlowNode.newControlFlowNode(elseStatement);
			conditionCFN.addEdge(ControlFlowNode.Direction.FORWARDS, elseCFN);
			elseCFN.addEdge(ControlFlowNode.Direction.FORWARDS, controlFlowNode);
		} else {
			conditionCFN.addEdge(ControlFlowNode.Direction.FORWARDS, controlFlowNode);
		}

		// Perform visits
		conditionCFN.evaluate();

		thenCFN.evaluate();
		if(elseCFN != null)
			elseCFN.evaluate();
		return false;
	}
	/**
	 * ImportDeclaration
	 * 
	 * This node does not influence control flow.
	 */
	public boolean visit(ImportDeclaration node) {
		return false;
	}
	/**
	 * Example: "Hello " + "World " + "How " + "Are " + "You?"
	 *          left + right + extOp1 + extOp2 + extOp3
	 */
	public boolean visit(InfixExpression node) {
		List<ControlFlowNode> cfns = null;
		ControlFlowNode leftCFN = controlFlowNode.newControlFlowNode(node.getLeftOperand());
		ControlFlowNode rightCFN = controlFlowNode.newControlFlowNode(node.getRightOperand());
		// Connect the back edges to the first operand (ie. left)
		controlFlowNode.moveEdges(ControlFlowNode.Direction.BACKWARDS, leftCFN);
		leftCFN.addEdge(ControlFlowNode.Direction.FORWARDS, rightCFN);
		
		// Add all extended operands
		if(node.hasExtendedOperands()) {
			cfns = createCFNListFromASTNodeList(node.extendedOperands());
			ControlFlowNode cfn = cfns.get(0);
			rightCFN.addEdge(ControlFlowNode.Direction.FORWARDS, cfn);
			cfn = cfns.get(cfns.size() - 1);
			cfn.addEdge(ControlFlowNode.Direction.FORWARDS, controlFlowNode);
		} else 
			rightCFN.addEdge(ControlFlowNode.Direction.FORWARDS, controlFlowNode);
		
		leftCFN.evaluate();
		rightCFN.evaluate();
		if(cfns != null)
			evaluate(cfns);

		return false;
	}

	public boolean visit(Initializer node) {
		return false;
	}
	/**
	 * Example: myVar instanceof MyClass
	 */
	public boolean visit(InstanceofExpression node) {
		Expression expression = node.getLeftOperand();
		if(expression == null)
			return false;

		ControlFlowNode expressioncfn = controlFlowNode.newControlFlowNode(expression);
		controlFlowNode.moveEdges(ControlFlowNode.Direction.BACKWARDS, expressioncfn);
		expressioncfn.addEdge(ControlFlowNode.Direction.FORWARDS, controlFlowNode);
		expressioncfn.evaluate();
		return false;
	}
	/**
	 * Javadoc
	 * 
	 * This node does not influence control flow.
	 */
	public boolean visit(Javadoc node) {
		return false;
	}
	/**
	 * Example: MyLabel: { int x = 5; }
	 */
	public boolean visit(LabeledStatement node) {
		SimpleName label = node.getLabel();
		if(label == null)
			throw new CrystalRuntimeException("labeled statement had no label");
		ControlFlowNode body = controlFlowNode.newControlFlowNode(node.getBody());
		if(body == null)
			throw new CrystalRuntimeException("labeled statement had no body");

		controlFlowNode.moveEdges(ControlFlowNode.Direction.BACKWARDS, body);
		body.addEdge(ControlFlowNode.Direction.FORWARDS, controlFlowNode);
		// body.copyLabelsFrom(controlFlowNode);
		// body.addLabel(label.getIdentifier());
		body.evaluate();
		
		return false;
	}
	/**
	 * This node does not influence control flow.
	 */
	public boolean visit(LineComment node) {
		return false;
	}
	/**
	 * This node does not influence control flow.
	 */
	public boolean visit(MarkerAnnotation node) {
		return false;
	}
	/**
	 * This node does not influence control flow.
	 */
	public boolean visit(MemberRef node) {
		return false;
	}
	/**
	 * This node does not influence control flow.
	 */
	public boolean visit(MemberValuePair node) {
		return false;
	}
	/**
	 * Example: public void myMethod(int x) { return x * x; }
	 */
	public boolean visit(MethodDeclaration node) {
		// If method has no body, then nothing to traverse.
		if(node.getBody() == null)
			return false;
		
		ControlFlowNode blockCFN = controlFlowNode.newControlFlowNode(node.getBody());
		controlFlowNode.insertNode(ControlFlowNode.Direction.BACKWARDS, blockCFN);
		blockCFN.evaluate();
		return false;
	}
	/**
	 * Example: var.getRef.myMethod(1, "two", true);
	 */
	public boolean visit(MethodInvocation node) {		
		Expression expression = node.getExpression();
		List arguments = node.arguments();
		ControlFlowNode expressioncfn = null, last = null;
		List<ControlFlowNode> cfns = null;
		
		if(expression != null) {
			expressioncfn = controlFlowNode.newControlFlowNode(expression);
			controlFlowNode.moveEdges(ControlFlowNode.Direction.BACKWARDS, expressioncfn);
			last = expressioncfn;
		}

		if(arguments != null && arguments.size() > 0) {
			// Take the argument list and make more CFNs from them.
			cfns = createCFNListFromASTNodeList(arguments);
			if(expression == null)
				controlFlowNode.moveEdges(ControlFlowNode.Direction.BACKWARDS, cfns.get(0));
			else
				expressioncfn.addEdge(ControlFlowNode.Direction.FORWARDS, cfns.get(0));
			last = cfns.get(cfns.size() - 1);
		}
		
		if(last == null)
			return false;
		last.addEdge(ControlFlowNode.Direction.FORWARDS, controlFlowNode);
		
		if(expressioncfn != null)
			expressioncfn.evaluate();
		if(cfns != null)
			evaluate(cfns);
		
		return false;
	}
	/**
	 * This node does not influence control flow.
	 */
	public boolean visit(MethodRef node) {
		return false;
	}
	/**
	 * This node does not influence control flow.
	 */
	public boolean visit(MethodRefParameter node) {
		return false;
	}
	/**
	 * This node does not influence control flow.
	 */
	public boolean visit(Modifier node) {
		// Leaf, no action necessary
		return false;
	}
	/**
	 * This node does not influence control flow.
	 */
	public boolean visit(NormalAnnotation node) {
		return false;
	}
	/**
	 * This node does not influence control flow.
	 */
	public boolean visit(NullLiteral node) {
		// Leaf, no action necessary
		return false;
	}
	/**
	 * This node does not influence control flow.
	 */
	public boolean visit(NumberLiteral node) {
		// Leaf, no action necessary
		return false;
	}
	/**
	 * This node does not influence control flow.
	 */
	public boolean visit(PackageDeclaration node) {
		return false;
	}
	/**
	 * This node does not influence control flow.
	 */
	public boolean visit(ParameterizedType node) {
		return false;
	}
	/**
	 * Example: (5 + 3)
	 */
	public boolean visit(ParenthesizedExpression node) {
		// Candidate for Removal
		ControlFlowNode expressionCFN = controlFlowNode.newControlFlowNode(node.getExpression());
		controlFlowNode.moveEdges(ControlFlowNode.Direction.BACKWARDS, expressionCFN);
		expressionCFN.addEdge(ControlFlowNode.Direction.FORWARDS, controlFlowNode);
		expressionCFN.evaluate();
		
		return false;
	}
	/**
	 * Example: x++;
	 */
	public boolean visit(PostfixExpression node) {
		ControlFlowNode operandCFN = controlFlowNode.newControlFlowNode(node.getOperand());
		controlFlowNode.moveEdges(ControlFlowNode.Direction.BACKWARDS, operandCFN);
		operandCFN.addEdge(ControlFlowNode.Direction.FORWARDS, controlFlowNode);
		operandCFN.evaluate();
		return false;
	}
	/**
	 * Example: ++x;
	 */
	public boolean visit(PrefixExpression node) {
		ControlFlowNode operandCFN = controlFlowNode.newControlFlowNode(node.getOperand());
		controlFlowNode.moveEdges(ControlFlowNode.Direction.BACKWARDS, operandCFN);
		operandCFN.addEdge(ControlFlowNode.Direction.FORWARDS, controlFlowNode);
		operandCFN.evaluate();
		return false;
	}
	/**
	 * This node does not influence control flow.
	 */
	public boolean visit(PrimitiveType node) {
		// Leaf, no action necessary
		return false;
	}
	/**
	 * Example: (2 in 1)  java.lang.System.out.println("Hello");
	 */
	public boolean visit(QualifiedName node) {
		SimpleName name = node.getName();
		Name qualifier = node.getQualifier();
		IBinding nameBinding = name.resolveBinding();
		// If this is a Field access, then add children to CFG
		if(nameBinding.getKind() == IBinding.VARIABLE) {
			IVariableBinding variableBinding = (IVariableBinding) nameBinding;
			if(variableBinding.isField()) {
				ControlFlowNode nameCFN = controlFlowNode.newControlFlowNode(name);
				ControlFlowNode qualifierCFN = controlFlowNode.newControlFlowNode(qualifier);
				controlFlowNode.moveEdges(ControlFlowNode.Direction.BACKWARDS, qualifierCFN);
				qualifierCFN.addEdge(ControlFlowNode.Direction.FORWARDS, nameCFN);
				nameCFN.addEdge(ControlFlowNode.Direction.FORWARDS, controlFlowNode);
				qualifierCFN.evaluate();
				nameCFN.evaluate();
			}
		}
		// If it is NOT a field access, then do not add children to CFG
		return false;
	}
	/**
	 * This node does not influence control flow.
	 */
	public boolean visit(QualifiedType node) {
		// Leaf in CFG, has children in AST
		return false;
	}
	/**
	 * Example: return "goodbye";
	 */
	public boolean visit(ReturnStatement node) {
		
		controlFlowNode.returning();
		
		Expression expression = node.getExpression();
		if(expression != null) {
			ControlFlowNode expressionCFN = controlFlowNode.newControlFlowNode(expression);
			controlFlowNode.moveEdges(ControlFlowNode.Direction.BACKWARDS, expressionCFN);
			expressionCFN.addEdge(ControlFlowNode.Direction.FORWARDS, controlFlowNode);
			expressionCFN.evaluate();
		}

		return false;
	}
	/**
	 * This node does not influence control flow.
	 */
	public boolean visit(SimpleName node) {
		// Leaf, no action necessary
		return false;
	}
	/**
	 * This node does not influence control flow.
	 */
	public boolean visit(SimpleType node) {
		return false;
	}
	/**
	 * This node does not influence control flow.
	 */
	public boolean visit(SingleMemberAnnotation node) {
		return false;
	}

	public boolean visit(SingleVariableDeclaration node) {
		Expression initializer = node.getInitializer();
		if(initializer == null)
			return false;
		ControlFlowNode initializercfn = controlFlowNode.newControlFlowNode(initializer);
		controlFlowNode.moveEdges(ControlFlowNode.Direction.BACKWARDS, initializercfn);
		initializercfn.addEdge(ControlFlowNode.Direction.FORWARDS, controlFlowNode);
		initializercfn.evaluate();
		return false;
	}
	/**
	 * This node does not influence control flow.
	 */
	public boolean visit(StringLiteral node) {
		// Leaf, no action necessary
		return false;
	}
	/**
	 * Example: super(arg1);
	 */
	public boolean visit(SuperConstructorInvocation node) {
		Expression expression = node.getExpression();
		List arguments = node.arguments();
		ControlFlowNode expressioncfn = null, last = null;
		List<ControlFlowNode> cfns = null;
		
		if(expression != null) {
			expressioncfn = controlFlowNode.newControlFlowNode(expression);
			controlFlowNode.moveEdges(ControlFlowNode.Direction.BACKWARDS, expressioncfn);
			last = expressioncfn;
		}

		if(arguments != null && arguments.size() > 0) {
			// Take the argument list and make more CFNs from them.
			cfns = createCFNListFromASTNodeList(arguments);
			if(expression == null)
				controlFlowNode.moveEdges(ControlFlowNode.Direction.BACKWARDS, cfns.get(0));
			else
				expressioncfn.addEdge(ControlFlowNode.Direction.FORWARDS, cfns.get(0));
			last = cfns.get(cfns.size() - 1);
		}
		
		if(last == null)
			return false;
		last.addEdge(ControlFlowNode.Direction.FORWARDS, controlFlowNode);
		
		if(expressioncfn != null)
			expressioncfn.evaluate();
		if(cfns != null)
			evaluate(cfns);
		
		return false;
	}

	public boolean visit(SuperFieldAccess node) {
		return false;
	}

	public boolean visit(SuperMethodInvocation node) {
		List arguments = node.arguments();
		if(arguments == null || arguments.size() == 0)
			return false;
		List<ControlFlowNode> cfns = createCFNListFromASTNodeList(arguments);
		ControlFlowNode cfn = cfns.get(0);
		controlFlowNode.moveEdges(ControlFlowNode.Direction.BACKWARDS, cfn);
		cfn = cfns.get(cfns.size() - 1);
		cfn.addEdge(ControlFlowNode.Direction.FORWARDS, controlFlowNode);
		evaluate(cfns);
		return false;
	}

	public boolean visit(SwitchCase node) {
		Expression expression = node.getExpression();
		if(expression == null)
			return false;
		
		ControlFlowNode expressioncfn = controlFlowNode.newControlFlowNode(expression);
		controlFlowNode.moveEdges(ControlFlowNode.Direction.BACKWARDS, expressioncfn);
		expressioncfn.addEdge(ControlFlowNode.Direction.FORWARDS, controlFlowNode);
		expressioncfn.evaluate();
		return false;
	}

	public boolean visit(SwitchStatement node) {
		Expression expression = node.getExpression();
		List statements = node.statements();
		if(expression == null)
			throw new CrystalRuntimeException("Switch statement without an expression?");
		if(statements == null || statements.size() == 0)
			throw new CrystalRuntimeException("Switch statements without any statements?");

		ControlFlowNode expressionCFN = controlFlowNode.newControlFlowNode(expression);

		controlFlowNode.moveEdges(ControlFlowNode.Direction.BACKWARDS, expressionCFN);
		
		Iterator i = statements.iterator();
		List<ControlFlowNode> statementCFNs = new LinkedList<ControlFlowNode>();
		ControlFlowNode cfn = null, previous = null;
		Statement astNode;
		boolean haveSeenDefault = false;
		while(i.hasNext()) {
			astNode = (Statement) i.next();
			cfn = controlFlowNode.newControlFlowNode(astNode);
			statementCFNs.add(cfn);
			if(previous != null)
				previous.addEdge(Direction.FORWARDS, cfn);
			if(astNode.getNodeType() == ASTNode.SWITCH_CASE) {
				expressionCFN.addEdge(Direction.FORWARDS, cfn);
				SwitchCase sc = (SwitchCase) astNode;
				if(sc.isDefault()) {
					if(haveSeenDefault)
						throw new CrystalRuntimeException("cannot have more than one default in a switch");
					haveSeenDefault = true;
				}
			}
			previous = cfn;
		}
		if(cfn == null)
			throw new CrystalRuntimeException("no statements in switch");
		// if we never saw a default, then add an edge to the switch statement
		if(!haveSeenDefault)
			expressionCFN.addEdge(Direction.FORWARDS, controlFlowNode);
		cfn.addEdge(ControlFlowNode.Direction.FORWARDS, controlFlowNode);
	
		expressionCFN.evaluate();
		if(statementCFNs != null)
			evaluate(statementCFNs);

		return false;
	}
	/**
	 * Example: synchronized (myVar) { myMethod(); }
	 */
	public boolean visit(SynchronizedStatement node) {
		ControlFlowNode expression = controlFlowNode.newControlFlowNode(node.getExpression());
		ControlFlowNode body = controlFlowNode.newControlFlowNode(node.getBody());

		controlFlowNode.moveEdges(ControlFlowNode.Direction.BACKWARDS, expression);

		expression.addEdge(ControlFlowNode.Direction.FORWARDS, controlFlowNode);
		controlFlowNode.addEdge(ControlFlowNode.Direction.FORWARDS, body);

		expression.evaluate();
		body.evaluate();
		
		return false;
	}
	/**
	 * This node does not influence control flow.
	 */
	public boolean visit(TagElement node) {
		return false;
	}
	/**
	 * This node does not influence control flow.
	 */
	public boolean visit(TextElement node) {
		return false;
	}
	/**
	 * This node does not influence control flow.
	 */
	public boolean visit(ThisExpression node) {
		return false;
	}
	/**
	 * 
	 * The throw statement will either be caught locally or
	 * be thrown up the stack (ie leave the method).
	 */
	public boolean visit(ThrowStatement node) {
		return false;
	}
	/**
	 * Example: try { int x; }
	 */
	public boolean visit(TryStatement node) {
		Block body = node.getBody();
		Block finalBody = node.getFinally();
		List catches = node.catchClauses();

		ControlFlowNode cfnBody = controlFlowNode.newControlFlowNode(body);
		ControlFlowNode cfnFinalBody = null;

		controlFlowNode.moveEdges(ControlFlowNode.Direction.BACKWARDS, cfnBody);
		
		List<ControlFlowNode> cfns = new ArrayList<ControlFlowNode>();
		ControlFlowNode cfn = cfnBody, prev = cfnBody;
		Iterator i = catches.iterator();
		CatchClause cc;
		for(;i.hasNext();) {
			cc = (CatchClause) i.next();
			cfn = controlFlowNode.newControlFlowNode(cc);
			prev.addEdge(ControlFlowNode.Direction.FORWARDS, cfn);
			prev = cfn;
			cfns.add(cfn);
		}
		
		if(finalBody != null) {
			cfnFinalBody = controlFlowNode.newControlFlowNode(finalBody);
			cfn.addEdge(ControlFlowNode.Direction.FORWARDS, cfnFinalBody);
			cfnFinalBody.addEdge(ControlFlowNode.Direction.FORWARDS, controlFlowNode);
		} else {
			cfn.addEdge(ControlFlowNode.Direction.FORWARDS, controlFlowNode);
		}
		
		// Peform Visits
		cfnBody.evaluate();
		Iterator<ControlFlowNode> j = cfns.iterator();
		for(;j.hasNext();)
			j.next().evaluate();
		if(finalBody != null)
			cfnFinalBody.evaluate();
		
		return false;
	}
	/**
	 * This node does not influence control flow.
	 */
	public boolean visit(TypeDeclaration node) {
		return false;
	}
	/**
	 * This node does not influence control flow.
	 */
	public boolean visit(TypeDeclarationStatement node) {
		return false;
	}
	/**
	 * This node does not influence control flow.
	 */
	public boolean visit(TypeLiteral node) {
		// Leaf, no action necessary
		return false;
	}
	/**
	 * This node does not influence control flow.
	 */
	public boolean visit(TypeParameter node) {
		return false;
	}

	public boolean visit(VariableDeclarationExpression node) {
		List fragments = node.fragments();
		if(fragments == null || fragments.size() == 0)
			return false;
		List<ControlFlowNode>cfns = createCFNListFromASTNodeList(fragments);
		ControlFlowNode cfn = cfns.get(0);
		controlFlowNode.moveEdges(ControlFlowNode.Direction.BACKWARDS, cfn);
		cfn = cfns.get(cfns.size() - 1);
		cfn.addEdge(ControlFlowNode.Direction.FORWARDS, controlFlowNode);
		evaluate(cfns);
		
		return false;
	}

	/**
	 * Example: int a[] = {5, 4}, x = 5, y;
	 * VariableDeclarationFragments: (a[] = {5, 4}), (x = 5), & (y)  
	 */
	public boolean visit(VariableDeclarationFragment node) {
		// Similar to Assignment
		ControlFlowNode nameCFN = controlFlowNode.newControlFlowNode(node.getName());

		// Empty Initializer
		if(node.getInitializer() == null) {
			controlFlowNode.moveEdges(ControlFlowNode.Direction.BACKWARDS, nameCFN);
			nameCFN.addEdge(ControlFlowNode.Direction.FORWARDS, controlFlowNode);
			nameCFN.evaluate();
		} else {
			ControlFlowNode initializerCFN = controlFlowNode.newControlFlowNode(node.getInitializer());

			controlFlowNode.moveEdges(ControlFlowNode.Direction.BACKWARDS, initializerCFN);

			initializerCFN.addEdge(ControlFlowNode.Direction.FORWARDS, nameCFN);
			nameCFN.addEdge(ControlFlowNode.Direction.FORWARDS, controlFlowNode);

			initializerCFN.evaluate();
			nameCFN.evaluate();
		}
		
		return false;
	}

	public boolean visit(VariableDeclarationStatement node) {
		List fragments = node.fragments();
		if(fragments == null || fragments.size() == 0)
			return false;

		List<ControlFlowNode> cfns = createCFNListFromASTNodeList(fragments);
		ControlFlowNode cfn = cfns.get(0);
		controlFlowNode.moveEdges(ControlFlowNode.Direction.BACKWARDS, cfn);
		cfn = cfns.get(cfns.size() - 1);
		cfn.addEdge(ControlFlowNode.Direction.FORWARDS, controlFlowNode);
		
		evaluate(cfns);
		
		return false;
	}
	/*
	 * Example: while (x < 10) { x++; run(x); };
	 */
	public boolean visit(WhileStatement node) {
//	    while ( Expression ) Statement
//             ----- 1 ---  -- 3 -> 1 --
//      -------------- 2 -> 3 ----------
// 		-------------- 2 -> exit -------

 
		ControlFlowNode exit = controlFlowNode.getNode(ControlFlowNode.Direction.FORWARDS);
		ControlFlowNode expressionCFN = controlFlowNode.newControlFlowNode(node.getExpression());
		// expressionCFN.copyLabelsFrom(controlFlowNode);
		ControlFlowNode bodyCFN = controlFlowNode.newControlFlowNode(node.getBody());

		controlFlowNode.moveEdges(ControlFlowNode.Direction.BACKWARDS, expressionCFN);

		expressionCFN.addEdge(ControlFlowNode.Direction.FORWARDS, controlFlowNode);
		controlFlowNode.addEdge(ControlFlowNode.Direction.FORWARDS, bodyCFN);
		bodyCFN.addEdge(ControlFlowNode.Direction.FORWARDS, expressionCFN);
		
		controlFlowNode.setLoopPaths(expressionCFN, exit);
		
		expressionCFN.evaluate();
		bodyCFN.evaluate();
		
		return false;
	}
	/**
	 * This node does not influence control flow.
	 */
	public boolean visit(WildcardType node) {
		return false;
	}
	
	
	/*
	 * Helper Methods
	 * 
	 */
	
	
	/**
	 * Takes a list of ASTNodes and creates ControlFlowNodes for each and connects them.
	 * The first element will not have any back edges and the last element will not
	 * have any forward edges.
	 */
	protected List<ControlFlowNode> createCFNListFromASTNodeList(List nodes) {
		if(nodes == null)
			return null;
		List<ControlFlowNode> cfns = new ArrayList<ControlFlowNode>();
		ControlFlowNode current, previous = null;
		Iterator i = nodes.iterator();
		ASTNode node;
		for(;i.hasNext();) {
			node = (ASTNode) i.next();
			current = controlFlowNode.newControlFlowNode(node);
			cfns.add(current);
			if(previous != null)
				previous.addEdge(ControlFlowNode.Direction.FORWARDS, current);
			previous = current;
		}
		return cfns;
	}
	
	protected void evaluate(List<ControlFlowNode> list) {
		if(list == null)
			return;
		Iterator<ControlFlowNode> i = list.iterator();
		ControlFlowNode cfn;
		for(;i.hasNext();) {
			cfn = i.next();
			cfn.evaluate();
		}
	}
}