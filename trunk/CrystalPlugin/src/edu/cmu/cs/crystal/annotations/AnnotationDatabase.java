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
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.IAnnotationBinding;
import org.eclipse.jdt.core.dom.IMemberValuePairBinding;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

import edu.cmu.cs.crystal.annotations.MultiAnnotation;
import edu.cmu.cs.crystal.internal.CrystalRuntimeException;

/**
 * This class is a database for annotations. It can store annotations of methods
 * we have analyzed or of ones we have not. Either way, it does not store any
 * AST information, so it returns Crystal objects that represent an annotation.
 * Those who want to use this database must register the type of annotations
 * to scan for.
 * 
 * A ICrystalAnnotation provides all the information that one needs about the
 * annotations. An AnnotationSummary is particularly useful for finding all
 * information relating to the method, including parameters.
 * 
 * And just in case you were wondering...we can not use regular reflection
 * objects here (Class, Annotation, etc.) as the files we are analyzing
 * aren't on the classpath of the system we are running.
 * 
 * @author cchristo
 *
 */
public class AnnotationDatabase {
	
	private static final Logger log = Logger.getLogger(AnnotationDatabase.class.getName());
	
	private Map<String, Class<? extends ICrystalAnnotation>> qualNames;

	private Map<String, AnnotationSummary> methods;
	private Map<String, List<ICrystalAnnotation>> classes;
	private Map<String, List<ICrystalAnnotation>> fields;

	public AnnotationDatabase() {
		qualNames = new HashMap<String, Class<? extends ICrystalAnnotation>>();
		methods = new HashMap<String, AnnotationSummary>();
		classes = new HashMap<String, List<ICrystalAnnotation>>();
		fields = new HashMap<String, List<ICrystalAnnotation>>();
		
	}
	
	public void register(String fullyQualifiedName, Class<? extends ICrystalAnnotation> crystalAnnotationClass) {
		Class<? extends ICrystalAnnotation> annoClass = qualNames.get(fullyQualifiedName);
		
		if (crystalAnnotationClass != null && annoClass != null &&
		  !(crystalAnnotationClass.isAssignableFrom(annoClass))) {
			throw new CrystalRuntimeException("Can not register " + fullyQualifiedName + " for " +
			  crystalAnnotationClass.getCanonicalName() + ", the class " + annoClass.getCanonicalName() +
			  " is already registered.");
		}
		qualNames.put(fullyQualifiedName, crystalAnnotationClass);
	}
	
	public AnnotationSummary getSummaryForMethod(IMethodBinding binding) {
		while(binding != binding.getMethodDeclaration())
			binding = binding.getMethodDeclaration();
		String name = resolveMethodBinding(binding);
		
		AnnotationSummary result = methods.get(name);
		if(result == null) {
			result = createMethodSummary(binding);
			methods.put(name, result);
		}
		return result;
	}
	
	private AnnotationSummary createMethodSummary(IMethodBinding binding) {
		int paramCount = binding.getParameterTypes().length;
		String[] paramNames = new String[paramCount];
		for(int i = 0; i < paramCount; i++) {
			paramNames[i] = "arg" + i; 
		}
		AnnotationSummary result = new AnnotationSummary(paramNames);
		result.addAllReturn(createAnnotations(binding.getAnnotations()));
		try {
			for(int i = 0; i < paramCount; i++) {
				result.addAllParameter(createAnnotations(binding.getParameterAnnotations(i)), i);
			}
		}
		catch(NullPointerException e) {
			if(log.isLoggable(Level.WARNING))
				log.log(Level.WARNING, "Bug in JDT (Eclipse 3.4M5) triggered in " + binding + ".  Not all annotations on parameters might be available.", e);
		}

		return result;
	}

	public List<ICrystalAnnotation> getAnnosForType(ITypeBinding type) {
		while(type != type.getTypeDeclaration())
			type = type.getTypeDeclaration();
		if(type.isPrimitive())
			return Collections.emptyList();
		if(type.isArray()) {
			log.warning("Annotations for array type requested: " + type.getName());
		}
		
		String name = type.getQualifiedName();
		
		List<ICrystalAnnotation> result = classes.get(name);
		if(result == null) {
			result = createAnnotations(type.getAnnotations());
			classes.put(name, result);
		}
		return result;
	}
		

	public List<ICrystalAnnotation> getAnnosForField(IVariableBinding binding) {
		while(binding != binding.getVariableDeclaration())
			binding = binding.getVariableDeclaration();
		String name = resolveVariableBinding(binding);
		
		List<ICrystalAnnotation> result = fields.get(name);
		if(result == null) {
			result = createAnnotations(binding.getAnnotations());
			fields.put(name, result);
		}
		return result;
	}
	
	protected List<ICrystalAnnotation> createAnnotations(IAnnotationBinding[] bindings) {
		List<ICrystalAnnotation> result = new ArrayList<ICrystalAnnotation>(bindings.length);
		for(IAnnotationBinding anno : bindings) {
			result.addAll(createAnnotations(anno));
		}
		return Collections.unmodifiableList(result);
	}
	
	protected List<ICrystalAnnotation> createAnnotations(IAnnotationBinding binding) {
		if(isMulti(binding)) {
			for(IMemberValuePairBinding pair : binding.getAllMemberValuePairs()) {
				Object value;
				if("value".equals(pair.getName()))
					value = pair.getValue();
				else if("annos".equals(pair.getName()))
					value = pair.getValue();
				else {
					log.warning("Ignore extra attribute in multi-annotation " + binding.getName() + ": " + pair.toString());
					continue;
				}
				if(value instanceof Object[]) {
					Object[] array = (Object[]) value;
					List<ICrystalAnnotation> result = new ArrayList<ICrystalAnnotation>(array.length);
					for(Object o : array) {
						result.add(createAnnotation((IAnnotationBinding) o));
					}
					return Collections.unmodifiableList(result);
				}
				else {
					// Eclipse doesn't desugar single-element arrays with omitted braces as arrays
					// https://bugs.eclipse.org/bugs/show_bug.cgi?id=223225
					return Collections.singletonList(createAnnotation((IAnnotationBinding) value));
				}
			}
			log.warning("Couldn't find annotation array in: " + binding);
		}
		return Collections.singletonList(createAnnotation(binding));
	}
	
	protected ICrystalAnnotation createAnnotation(IAnnotationBinding binding) {
		String	qualName;
		ICrystalAnnotation crystalAnno;

		qualName = binding.getAnnotationType().getQualifiedName();
		crystalAnno = createCrystalAnnotation(qualName);
		crystalAnno.setName(qualName);
		
		for (IMemberValuePairBinding pair : binding.getAllMemberValuePairs()) {
			crystalAnno.setObject(pair.getName(), getAnnotationValue(pair.getValue(), pair.getMethodBinding().getReturnType().isArray()));
		}
		return crystalAnno;
	}

	/**
	 * Checks whether this annotation is marked as a multi annotation, as
	 * described by MultiAnnotation
	 * @param annoBinding
	 * @return
	 */
	public boolean isMulti(IAnnotationBinding annoBinding) {
		ITypeBinding binding = annoBinding.getAnnotationType();
		
		for (IAnnotationBinding meta : binding.getAnnotations()) {
			if (meta.getAnnotationType().getQualifiedName().equals(MultiAnnotation.class.getName()))
				return true;
		}
		return false;
	}

	/**
	 * @param value
	 * @return
	 */
	private Object getAnnotationValue(Object rawValue, boolean forceArray) {
		if(rawValue instanceof Object[]) {
			Object[] array = (Object[]) rawValue;
			Object[] result = new Object[array.length];
			for(int i = 0; i < array.length; i++)
				result[i] = getAnnotationValue(array[i], false);
			return result;
		}
		if(rawValue instanceof IAnnotationBinding) {
			rawValue = createAnnotation((IAnnotationBinding) rawValue);
		}
		if(forceArray) {
			// this is a workaround for an Eclipse "bug" (#223225)
			// Eclipse doesn't desugar single-element arrays with omitted braces as arrays
			// https://bugs.eclipse.org/bugs/show_bug.cgi?id=223225
			return new Object[] { rawValue };
		}
		// other values are literals
		return rawValue;
	}

	public ICrystalAnnotation createCrystalAnnotation(String qualifiedAnnotationName) {
		Class<? extends ICrystalAnnotation> annoClass = qualNames.get(qualifiedAnnotationName);
		
		if (annoClass == null)
			return new CrystalAnnotation();
		else {
			try {
				return annoClass.newInstance();
			} 
			catch (InstantiationException e) {
				log.log(Level.WARNING, "Error instantiating custom annotation parser.  Using default representation.", e);
				return new CrystalAnnotation();
			} 
			catch (IllegalAccessException e) {
				log.log(Level.WARNING, "Error accessing custom annotation parser.  Using default representation.", e);
				return new CrystalAnnotation();
			}
		}
	}
	
	public void addAnnotationToField(ICrystalAnnotation anno, FieldDeclaration field) {
		IVariableBinding binding;
		String name;
		List<ICrystalAnnotation> annoList;
		
		if (field.fragments().isEmpty())
			return;
		
		binding = ((VariableDeclarationFragment)field.fragments().get(0)).resolveBinding();
		name = resolveVariableBinding(binding);
		annoList = fields.get(name);
		if (annoList == null) {
			annoList = new ArrayList<ICrystalAnnotation>();
			fields.put(name, annoList);	
		}
		annoList.add(anno);
	}
	
	public void addAnnotationToMethod(AnnotationSummary anno, MethodDeclaration method) {
		IMethodBinding binding = method.resolveBinding();
		String name = resolveMethodBinding(binding);
		
		AnnotationSummary existing = methods.get(name);
		if (existing == null)
			methods.put(name, anno);
		else
			existing.add(anno);
	}
	
	
	public void addAnnotationToType(ICrystalAnnotation anno, TypeDeclaration type) {
		ITypeBinding binding = type.resolveBinding();
		String name = binding.getQualifiedName();
		List<ICrystalAnnotation> annoList;
		
		annoList = classes.get(name);
		if (annoList == null) {
			annoList = new ArrayList<ICrystalAnnotation>();
			classes.put(name, annoList);	
		}
		annoList.add(anno);
	}
	
	public static <A extends ICrystalAnnotation> List<A> filter(List<ICrystalAnnotation> list, Class<A> type) {
		List<A> result = new LinkedList<A>();
		for(ICrystalAnnotation anno : list) {
			if(type.isAssignableFrom(anno.getClass()))
				result.add((A) anno);
		}
		return result;
	}
	
	static protected ICrystalAnnotation findAnnotation(String name, List<ICrystalAnnotation> list) {
		ICrystalAnnotation result = null;
		for (ICrystalAnnotation anno : list) {
			if (anno.getName().equals(name)) {
				assert result == null;
				result = anno;
			}
		}
		return result;
	}

	private String resolveMethodBinding(IMethodBinding binding) {
		return getFunctionName(binding) + getTypes(binding);
	}
	
	private String resolveVariableBinding(IVariableBinding binding) {
		String qualName;
		
		if (binding.getDeclaringMethod() != null)
			qualName = resolveMethodBinding(binding.getDeclaringMethod());
		else
			qualName = binding.getDeclaringClass().getQualifiedName();
		qualName += "." + binding.getVariableId();
		
		return qualName;
	}
	

	private static String getFunctionName(IMethodBinding bind) {
		return bind.getDeclaringClass().getQualifiedName() + "." + bind.getName();
	}
	
	private static String getTypes(IMethodBinding bind) {
		ITypeBinding[] types = bind.getParameterTypes();
		String typesStr = "";
		
		for (int ndx = 0; ndx < types.length; ndx++)
			typesStr += types[ndx].getName();
		
		return typesStr;
	}
}
