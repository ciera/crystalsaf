package edu.cmu.cs.crystal.analysis.print;

import java.io.PrintWriter;

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

/** This visitor prints the nodes of an AST as an indented tree.
 * It is a helper class for PrintNodesAnalysis
 * 
 * @author aldrich
 * @since Crystal 3.4.1
 */
public class PrintNodesVisitor extends ASTVisitor {	
	public PrintNodesVisitor(PrintWriter writer) {
		out = writer;
	}
	
	int level = 0;
	PrintWriter out;
	
	private void output(String string) {
		for (int i = 0; i < level; ++i)
			out.print("  ");
		out.println(string);		
	}

	@Override
	public void postVisit(ASTNode node) {
		level--;
		super.postVisit(node);
	}

	@Override
	public void preVisit(ASTNode node) {
		level++;
		super.preVisit(node);
	}

	@Override
	public boolean visit(AnnotationTypeDeclaration node) {
		output("AnnotationTypeDeclaration");
		return super.visit(node);
	}

	@Override
	public boolean visit(AnnotationTypeMemberDeclaration node) {
		output("AnnotationTypeMemberDeclaration");
		return super.visit(node);
	}

	@Override
	public boolean visit(AnonymousClassDeclaration node) {
		output("AnonymousClassDeclaration");
		return super.visit(node);
	}

	@Override
	public boolean visit(ArrayAccess node) {
		output("ArrayAccess");
		return super.visit(node);
	}

	@Override
	public boolean visit(ArrayCreation node) {
		output("ArrayCreation");
		return super.visit(node);
	}

	@Override
	public boolean visit(ArrayInitializer node) {
		output("ArrayInitializer");
		return super.visit(node);
	}

	@Override
	public boolean visit(ArrayType node) {
		output("ArrayType");
		return super.visit(node);
	}

	@Override
	public boolean visit(AssertStatement node) {
		output("AssertStatement");
		return super.visit(node);
	}

	@Override
	public boolean visit(Assignment node) {
		output("Assignment");
		return super.visit(node);
	}

	@Override
	public boolean visit(Block node) {
		output("Block");
		return super.visit(node);
	}

	@Override
	public boolean visit(BlockComment node) {
		output("BlockComment");
		return super.visit(node);
	}

	@Override
	public boolean visit(BooleanLiteral node) {
		output("BooleanLiteral");
		return super.visit(node);
	}

	@Override
	public boolean visit(BreakStatement node) {
		output("BreakStatement");
		return super.visit(node);
	}

	@Override
	public boolean visit(CastExpression node) {
		output("CastExpression");
		return super.visit(node);
	}

	@Override
	public boolean visit(CatchClause node) {
		output("CatchClause");
		return super.visit(node);
	}

	@Override
	public boolean visit(CharacterLiteral node) {
		output("CharacterLiteral");
		return super.visit(node);
	}

	@Override
	public boolean visit(ClassInstanceCreation node) {
		output("ClassInstanceCreation");
		return super.visit(node);
	}

	@Override
	public boolean visit(CompilationUnit node) {
		output("CompilationUnit");
		return super.visit(node);
	}

	@Override
	public boolean visit(ConditionalExpression node) {
		output("ConditionalExpression");
		return super.visit(node);
	}

	@Override
	public boolean visit(ConstructorInvocation node) {
		output("ConstructorInvocation");
		return super.visit(node);
	}

	@Override
	public boolean visit(ContinueStatement node) {
		output("ContinueStatement");
		return super.visit(node);
	}

	@Override
	public boolean visit(DoStatement node) {
		output("DoStatement");
		return super.visit(node);
	}

	@Override
	public boolean visit(EmptyStatement node) {
		output("EmptyStatement");
		return super.visit(node);
	}

	@Override
	public boolean visit(EnhancedForStatement node) {
		output("EnhancedForStatement");
		return super.visit(node);
	}

	@Override
	public boolean visit(EnumConstantDeclaration node) {
		output("EnumConstantDeclaration");
		return super.visit(node);
	}

	@Override
	public boolean visit(EnumDeclaration node) {
		output("EnumDeclaration");
		return super.visit(node);
	}

	@Override
	public boolean visit(ExpressionStatement node) {
		output("ExpressionStatement");
		return super.visit(node);
	}

	@Override
	public boolean visit(FieldAccess node) {
		output("FieldAccess");
		return super.visit(node);
	}

	@Override
	public boolean visit(FieldDeclaration node) {
		output("FieldDeclaration");
		return super.visit(node);
	}

	@Override
	public boolean visit(ForStatement node) {
		output("ForStatement");
		return super.visit(node);
	}

	@Override
	public boolean visit(IfStatement node) {
		output("IfStatement");
		return super.visit(node);
	}

	@Override
	public boolean visit(ImportDeclaration node) {
		output("ImportDeclaration");
		return super.visit(node);
	}

	@Override
	public boolean visit(InfixExpression node) {
		output("InfixExpression");
		return super.visit(node);
	}

	@Override
	public boolean visit(Initializer node) {
		output("Initializer");
		return super.visit(node);
	}

	@Override
	public boolean visit(InstanceofExpression node) {
		output("InstanceofExpression");
		return super.visit(node);
	}

	@Override
	public boolean visit(Javadoc node) {
		output("Javadoc");
		return super.visit(node);
	}

	@Override
	public boolean visit(LabeledStatement node) {
		output("LabeledStatement");
		return super.visit(node);
	}

	@Override
	public boolean visit(LineComment node) {
		output("LineComment");
		return super.visit(node);
	}

	@Override
	public boolean visit(MarkerAnnotation node) {
		output("MarkerAnnotation");
		return super.visit(node);
	}

	@Override
	public boolean visit(MemberRef node) {
		output("MemberRef");
		return super.visit(node);
	}

	@Override
	public boolean visit(MemberValuePair node) {
		output("MemberValuePair");
		return super.visit(node);
	}

	@Override
	public boolean visit(MethodDeclaration node) {
		output("MethodDeclaration");
		return super.visit(node);
	}

	@Override
	public boolean visit(MethodInvocation node) {
		output("MethodInvocation");
		return super.visit(node);
	}

	@Override
	public boolean visit(MethodRef node) {
		output("MethodRef");
		return super.visit(node);
	}

	@Override
	public boolean visit(MethodRefParameter node) {
		output("MethodRefParameter");
		return super.visit(node);
	}

	@Override
	public boolean visit(Modifier node) {
		output("Modifier");
		return super.visit(node);
	}

	@Override
	public boolean visit(NormalAnnotation node) {
		output("NormalAnnotation");
		return super.visit(node);
	}

	@Override
	public boolean visit(NullLiteral node) {
		output("NullLiteral");
		return super.visit(node);
	}

	@Override
	public boolean visit(NumberLiteral node) {
		output("NumberLiteral");
		return super.visit(node);
	}

	@Override
	public boolean visit(PackageDeclaration node) {
		output("PackageDeclaration");
		return super.visit(node);
	}

	@Override
	public boolean visit(ParameterizedType node) {
		output("ParameterizedType");
		return super.visit(node);
	}

	@Override
	public boolean visit(ParenthesizedExpression node) {
		output("ParenthesizedExpression");
		return super.visit(node);
	}

	@Override
	public boolean visit(PostfixExpression node) {
		output("PostfixExpression");
		return super.visit(node);
	}

	@Override
	public boolean visit(PrefixExpression node) {
		output("PrefixExpression");
		return super.visit(node);
	}

	@Override
	public boolean visit(PrimitiveType node) {
		output("PrimitiveType " + node);
		return super.visit(node);
	}

	@Override
	public boolean visit(QualifiedName node) {
		output("QualifiedName");
		return super.visit(node);
	}

	@Override
	public boolean visit(QualifiedType node) {
		output("QualifiedType");
		return super.visit(node);
	}

	@Override
	public boolean visit(ReturnStatement node) {
		output("ReturnStatement");
		return super.visit(node);
	}

	@Override
	public boolean visit(SimpleName node) {
		output("SimpleName " + node.getFullyQualifiedName());
		return super.visit(node);
	}

	@Override
	public boolean visit(SimpleType node) {
		output("SimpleType");
		return super.visit(node);
	}

	@Override
	public boolean visit(SingleMemberAnnotation node) {
		output("SingleMemberAnnotation");
		return super.visit(node);
	}

	@Override
	public boolean visit(SingleVariableDeclaration node) {
		output("SingleVariableDeclaration");
		return super.visit(node);
	}

	@Override
	public boolean visit(StringLiteral node) {
		output("StringLiteral");
		return super.visit(node);
	}

	@Override
	public boolean visit(SuperConstructorInvocation node) {
		output("SuperConstructorInvocation");
		return super.visit(node);
	}

	@Override
	public boolean visit(SuperFieldAccess node) {
		output("SuperFieldAccess");
		return super.visit(node);
	}

	@Override
	public boolean visit(SuperMethodInvocation node) {
		output("SuperMethodInvocation");
		return super.visit(node);
	}

	@Override
	public boolean visit(SwitchCase node) {
		output("SwitchCase");
		return super.visit(node);
	}

	@Override
	public boolean visit(SwitchStatement node) {
		output("SwitchStatement");
		return super.visit(node);
	}

	@Override
	public boolean visit(SynchronizedStatement node) {
		output("SynchronizedStatement");
		return super.visit(node);
	}

	@Override
	public boolean visit(TagElement node) {
		output("TagElement");
		return super.visit(node);
	}

	@Override
	public boolean visit(TextElement node) {
		output("TextElement");
		return super.visit(node);
	}

	@Override
	public boolean visit(ThisExpression node) {
		output("ThisExpression");
		return super.visit(node);
	}

	@Override
	public boolean visit(ThrowStatement node) {
		output("ThrowStatement");
		return super.visit(node);
	}

	@Override
	public boolean visit(TryStatement node) {
		output("TryStatement");
		return super.visit(node);
	}

	@Override
	public boolean visit(TypeDeclaration node) {
		output("TypeDeclaration");
		return super.visit(node);
	}

	@Override
	public boolean visit(TypeDeclarationStatement node) {
		output("TypeDeclarationStatement");
		return super.visit(node);
	}

	@Override
	public boolean visit(TypeLiteral node) {
		output("TypeLiteral");
		return super.visit(node);
	}

	@Override
	public boolean visit(TypeParameter node) {
		output("TypeParameter");
		return super.visit(node);
	}

	@Override
	public boolean visit(VariableDeclarationExpression node) {
		output("VariableDeclarationExpression");
		return super.visit(node);
	}

	@Override
	public boolean visit(VariableDeclarationFragment node) {
		output("VariableDeclarationFragment");
		return super.visit(node);
	}

	@Override
	public boolean visit(VariableDeclarationStatement node) {
		output("VariableDeclarationStatement");
		return super.visit(node);
	}

	@Override
	public boolean visit(WhileStatement node) {
		output("WhileStatement");
		return super.visit(node);
	}

	@Override
	public boolean visit(WildcardType node) {
		output("WildcardType");
		return super.visit(node);
	}
};
