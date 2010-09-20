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
package edu.cmu.cs.crystal.annotations;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.jdt.core.dom.ArrayInitializer;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.IAnnotationBinding;
import org.eclipse.jdt.core.dom.IExtendedModifier;
import org.eclipse.jdt.core.dom.MemberValuePair;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.NormalAnnotation;
import org.eclipse.jdt.core.dom.SingleMemberAnnotation;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;

import edu.cmu.cs.crystal.AbstractCompilationUnitAnalysis;
import edu.cmu.cs.crystal.internal.CrystalRuntimeException;

/**
 * A pre-analysis that runs in Crystal to gather up all the annotations.
 * 
 * @author ciera
 * @since Crystal 3.4.0
 */
public class AnnotationFinder extends AbstractCompilationUnitAnalysis {
	AnnotationDatabase db;
	
	public AnnotationFinder(AnnotationDatabase database) {
		db = database;
	}
	
	@Override
	public void analyzeCompilationUnit(CompilationUnit d) {
		d.accept(new AnnotationVisitor());
	}
	
	public List<ICrystalAnnotation> getAnnotation(List<IExtendedModifier> modifiers) {
		ICrystalAnnotation crystalAnno;
		List<ICrystalAnnotation> annoList = new ArrayList<ICrystalAnnotation>();
		IAnnotationBinding binding;
		
		for (IExtendedModifier mod : modifiers) {
			if (!mod.isAnnotation())
				continue;
			
			binding = ((Annotation)mod).resolveAnnotationBinding();
			
			if (db.isMulti(binding)) {
				annoList.addAll(getMulti((Annotation)mod));
			}
			else {
				crystalAnno = db.createAnnotation(binding);
				annoList.add(crystalAnno);
			}
		}
		return annoList;
	}

	/**
	 * Get multiple annotations out of an annotation array.
	 * This presumes that baseAnno was marked as a multi. If so, it is
	 * expected to be either a singleMemberAnno, or a normal anno with an
	 * "annos" identifier.
	 * 
	 * @param baseAnno The multi annotation
	 * @return The CrystalAnnotations found inside of it
	 * @throws CrystalRuntimeException if anything went wrong during parsing. This means
	 * that the annotation was not actually a multi annotation and should not
	 * be marked as such.
	 */
	private List<? extends ICrystalAnnotation> getMulti(Annotation baseAnno) {
		CrystalRuntimeException err = new CrystalRuntimeException("Hey! " +
				  "If you have a multi annotation, it better have an array of Annotations!" +
				  " Found " + baseAnno.toString() + " without an array.");
		List<Expression> realAnnos = null;
		List<ICrystalAnnotation> crystalAnnos = new ArrayList<ICrystalAnnotation>();
		
		if (baseAnno.isSingleMemberAnnotation()) {
			SingleMemberAnnotation anno = (SingleMemberAnnotation)baseAnno;
			if (!(anno.getValue() instanceof ArrayInitializer)) {
				realAnnos = Collections.singletonList(anno.getValue());
			}
			else {
				realAnnos = ((ArrayInitializer)anno.getValue()).expressions();
			}	
		}
		else if (baseAnno.isNormalAnnotation()) {
			NormalAnnotation anno = (NormalAnnotation)baseAnno;
			for (MemberValuePair pair : (List<MemberValuePair>)anno.values()) {
				if (pair.getName().getIdentifier().equals("annos") && pair.getValue() instanceof ArrayInitializer) {
					realAnnos = ((ArrayInitializer) pair.getValue()).expressions();
					break;
				}
			}
			if (realAnnos == null)
				throw err;
		}
		else
			throw err;
			
		//ok, now we have the array of annotations
		for (Expression exp : realAnnos ) {
			if (!(exp instanceof Annotation))
				throw err;
			IAnnotationBinding binding = ((Annotation)exp).resolveAnnotationBinding();
			crystalAnnos.add(db.createAnnotation(binding));
		}
		return crystalAnnos;
	}

	private class AnnotationVisitor extends ASTVisitor {

		@Override
		public boolean visit(FieldDeclaration node) {
			List<ICrystalAnnotation> annoList = getAnnotation(node.modifiers());

			for (ICrystalAnnotation anno : annoList)
				db.addAnnotationToField(anno, node);
			return super.visit(node);
		}

		@Override
		public boolean visit(MethodDeclaration node) {
			AnnotationSummary sum;
			String[] paramNames = new String[node.parameters().size()];
			List<ICrystalAnnotation> annoList;
			int ndx = 0;
			
			for (SingleVariableDeclaration param : (List<SingleVariableDeclaration>)node.parameters()) {
				paramNames[ndx] = param.getName().getIdentifier();
				ndx++;
			}
			sum	= new AnnotationSummary(paramNames);
			
			annoList = getAnnotation(node.modifiers());
			for (ICrystalAnnotation anno : annoList)
				sum.addReturn(anno);
			
			ndx = 0;
			for (SingleVariableDeclaration param : (List<SingleVariableDeclaration>)node.parameters()) {
				annoList = getAnnotation(param.modifiers());
				for (ICrystalAnnotation anno : annoList)
					sum.addParameter(anno, ndx);
				ndx++;
			}
			db.addAnnotationToMethod(sum, node);
			return super.visit(node);
		}

		@Override
		public boolean visit(TypeDeclaration node) {
			List<ICrystalAnnotation> annoList = getAnnotation(node.modifiers());

			for (ICrystalAnnotation anno : annoList)
				db.addAnnotationToType(anno, node);
			return super.visit(node);
		}
		
	}
	
}
