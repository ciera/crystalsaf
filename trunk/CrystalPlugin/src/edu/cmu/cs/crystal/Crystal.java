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
package edu.cmu.cs.crystal;

import java.io.PrintWriter;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.IBinding;

import edu.cmu.cs.crystal.annotations.AnnotationDatabase;
import edu.cmu.cs.crystal.annotations.AnnotationFinder;
import edu.cmu.cs.crystal.annotations.CrystalAnnotation;
import edu.cmu.cs.crystal.annotations.ICrystalAnnotation;
import edu.cmu.cs.crystal.internal.UserConsoleView;
import edu.cmu.cs.crystal.internal.WorkspaceUtilities;

/**
 * Provides the ability to run the analyses.
 * Provides output mechanisms for both the Static Analysis developer
 * and the Static Analysis user.
 * 
 * Also maintains several useful data structures.  They can be accessed through several "get*"
 * methods.
 * 
 * @author David Dickey
 * @author Jonathan Aldrich
 * @author cchristo
 */
public class Crystal {
	
	/**
	 * This is the name of the regression-testing logger.
	 * The intention of the regression-testing logger is to produce a output file
	 * that can be compared with some reference file to make sure analysis results
	 * are stable.
	 * 
	 * If you want to include regular output into regression tests, you should
	 * log it into a {@link java.util.Logger} object for the name defined here.
	 * (Such a logger can be acquired using
	 * <code>java.util.Logger.getLogger(Crystal.REGRESSION_LOGGER)</code>).
	 * By default, a message for each compilation unit being analyzed and
	 * for each user problem reported will be included in the regression-testing log.
	 * Note that messages of levels {@link java.util.Level#WARNING} and 
	 * {@link java.util.Level#SEVERE} should also be included in the regression-testing log.
	 * 
	 * @see #reportUserProblem(String, ASTNode, ICrystalAnalysis)
	 * @see #regression
	 */
	public static final String REGRESSION_LOGGER = "edu.cmu.cs.crystal.regression";
	
	private static final Logger logger = Logger.getLogger(Crystal.class.getName());
	
	private static Logger regressionLogger = Logger.getLogger(REGRESSION_LOGGER);
	
	// TODO: Make these data structures are immutable (ie unchangable)
	/**
	 * the list of analyses to perfrom
	 */
	private LinkedList<ICrystalAnalysis> analyses;

	/**
	 * A map from a unique String from an IBinding to an ASTNode.
	 * The ASTNode is the node that initially declared the object
	 * in the binding. It will only contain the bindings for the
	 * compilation unit currently being analyzed.
	 */
	private Map<String, ASTNode> bindings;

	/**
	 * The annotation database will be populated before
	 * any analyses are run. It will populate from all compilation
	 * units that the run is going on. It will be reset at each analysis
	 * run and will NOT store the data from the last run.
	 */
	private AnnotationDatabase annoDB;

	/**
	 * Permanent registry for annotation parsers, populated at plugin initialization time.
	 */
	private Map<String, Class<? extends ICrystalAnnotation>> annotationRegistry =
		new HashMap<String, Class<? extends ICrystalAnnotation>>(); 
	
	public Crystal() {
		analyses = new LinkedList<ICrystalAnalysis>();
	}
	
	/**
	 * Returns a PrintWriter that can be used to output text to 
	 * a console the user can see.
	 * 
	 * This text will goto the "UserConsole", a console
	 * in the child-eclipse window that the user must enable through
	 * the "Window" menu. If this console is not open, then it will
	 * print to standard out.
	 */
	public PrintWriter userOut() {
		UserConsoleView consoleView = UserConsoleView.getInstance();
		if (consoleView == null) {
			// TODO: Automatically enable the user console or make this a pop-up on the child eclipse
			return new PrintWriter(System.out, true);
		}
		return consoleView.getPrintWriter();
	}
	
	/**
	 * Returns a PrintWriter that can be used to output text to 
	 * a console the static-analysis developer can see.
	 * 
	 * Currently this text will goto the parent-eclipse's standard
	 * console.
	 */
	public PrintWriter debugOut()  {
		return new PrintWriter(System.out, true);
	}
	
	/**
	 * Reports a problem in the problems window the user can see.
	 * Provides relevant problem information like "Description", "Resource", 
	 * "Resource Folder" and "Line Location" whenever possible.
	 * 
	 * @param	problemDescription	the text describing the problem
	 * @param	node				the {@link #ASTNode} where the problem occurred
	 * @param	analysis			the analysis where the problem occurred
	 */
	public void reportUserProblem(String problemDescription, ASTNode node, ICrystalAnalysis analysis) {
		if(node == null)
			throw new NullPointerException("null ASTNode argument in reportUserProblem");
		if(analysis == null)
			throw new NullPointerException("null analysis argument in reportUserProblem");
		if(logger.isLoggable(Level.FINE))
			logger.fine("Reporting problem to user: " + problemDescription + "; node: " + node);
		if(regressionLogger.isLoggable(Level.INFO)) {
			regressionLogger.info(problemDescription);
			regressionLogger.info(node.toString());
		}
		
		IResource resource;
		ASTNode root = node.getRoot();

		// Identify the closest resource to the ASTNode,
		// otherwise fall back to using the high-level workspace root.
		if(root.getNodeType() == ASTNode.COMPILATION_UNIT) {
			CompilationUnit cu = (CompilationUnit) root;
			IJavaElement je = cu.getJavaElement();
			resource = je.getResource();
		} 
		else {
			// Use the high-level Workspace
			resource = ResourcesPlugin.getWorkspace().getRoot();
		}
		
		// Create the marker
		//TODO: create markers according to the type of the analysis
		try {
			IMarker marker = resource.createMarker(IMarker.PROBLEM);
			marker.setAttribute(IMarker.CHAR_START, node.getStartPosition());
			marker.setAttribute(IMarker.CHAR_END, node.getStartPosition() + node.getLength());
			marker.setAttribute(IMarker.MESSAGE, "[" + analysis.getName() + "]: " + problemDescription);
			marker.setAttribute(IMarker.PRIORITY, IMarker.PRIORITY_NORMAL);
			marker.setAttribute(IMarker.SEVERITY, IMarker.SEVERITY_INFO);
			CompilationUnit cu = (CompilationUnit) node.getRoot();
			int line = cu.getLineNumber(node.getStartPosition());
			if(line >= 0) // -1 and -2 indicate error conditions
				marker.setAttribute(IMarker.LINE_NUMBER, line);
		} 
		catch (CoreException ce) {
			logger.log(Level.SEVERE, "CoreException when creating marker", ce);
		}
	}
	
	/**
	 * Registers an analysis with the framework.  All analyses must be
	 * registered in order for them to be invoked.
	 * 
	 * @param	analysis	the analysis to be used
	 */
	public void registerAnalysis(ICrystalAnalysis analysis) {
		analyses.add(analysis);
	}
	
	/**
	 * Retrieves the declaring ASTNode of the binding.
	 * 
	 * The first time this method is called, the mapping between
	 * bindings and nodes is created.  The creation time will
	 * depend on the size of the workspace.  Subsequent calls 
	 * will simply look up the values from a mapping.
	 * 
	 * @param binding	the binding from which you want the declaration
	 * @return	the declaration node
	 */
	public ASTNode getASTNodeFromBinding(IBinding binding) {
		throw new UnsupportedOperationException("Retrieving AST nodes for bindings not supported");
//		if (bindings == null)
//			throw new CrystalRuntimeException("Crystal::getASTNodeFromBinding: An error occured while creating the binding -> declarations mapping");
//		return bindings.get(binding.getKey());
	}
	
	/**
	 * 
	 * @return the annotation database for the compilation unit currently being analyzed.
	 * Null if no compilation unit is being analyzed
	 */
	public AnnotationDatabase getAnnotationDatabase() {
		return annoDB;
	}
	
	public List<ICrystalAnalysis> getAnalyses() {
		return Collections.unmodifiableList(analyses);
	}
	
	/**
	 * Runs all of the analyses on the compilation units passed in.
	 * Will clear the console before starting.
	 * Will clear ALL the markers for each compilation unit before starting.
	 * This will run all the analyses on a single compilation unit at a time.
	 * After finishing a compilation unit, we may not hold onto any ASTNodes
	 * @param reanalyzeList The compilation units to analyze
	 */
	public void runAnalyses(List<ICompilationUnit> reanalyzeList) {
		PrintWriter output = debugOut();
		PrintWriter user = userOut();

		// Clear User Output Console
		UserConsoleView console = UserConsoleView.getInstance();
		if (console != null)
			console.clearConsole();
		runAnalysesOnMultiUnit(reanalyzeList, output, user);
	}


	/**
	 * Runs all of the analyses on all of the compilation units in the workspace.
	 * Will clear the console before starting.
	 * Will clear ALL the markers for each compilation unit before starting.
	 * This will run all the analyses on a single compilation unit at a time.
	 * After finishing a compilation unit, we may not hold onto any ASTNodes
	 */
	public void runAnalyses() {
		PrintWriter output = debugOut();
		PrintWriter user = userOut();
		
		if (analyses == null || analyses.isEmpty()) {
			logger.warning("Crystal::runAnalyses() No analyses registered");
			return;
		}
		
		// Clear User Output Console
		UserConsoleView console = UserConsoleView.getInstance();
		if (console != null)
			console.clearConsole();

		runAnalysesOnMultiUnit(WorkspaceUtilities.scanForCompilationUnits(), output, user);
	}

	/**
	 * Runs the analyses on many compilation units. This method will first load up the annotation
	 * database for these compilation units, and it will clear the markers for each unit.
	 * @param units CompilationUnits to analyze
	 * @param output Debug output
	 * @param user User output
	 */
	private void runAnalysesOnMultiUnit(
			List<ICompilationUnit> units, PrintWriter output,
			PrintWriter user) {
		if(units == null || units.isEmpty())
			return;
		
		annoDB = new AnnotationDatabase();
		AnnotationFinder finder = new AnnotationFinder(annoDB);
		
		// register annotation parsers from registry
		for(Map.Entry<String, Class<? extends ICrystalAnnotation>> entry : annotationRegistry.entrySet()) {
			annoDB.register(entry.getKey(), entry.getValue());
		}
		
		//register any special classes for the annotation database
		// TODO remove getAnnotationClasses() from ICrystalAnalysis
		for (ICrystalAnalysis crystalAnalysis : analyses) {
			Map<String, Class<? extends CrystalAnnotation>> map = crystalAnalysis.getAnnotationClasses();
			if (map == null)
				continue;	
			for (Map.Entry<String, Class<? extends CrystalAnnotation>> entry : map.entrySet())
				annoDB.register(entry.getKey(), entry.getValue());
		}
		
		//run the annotation finder on everything
		if(logger.isLoggable(Level.INFO))
			logger.info("Scanning annotations of analyzed compilation units");
		for(ICompilationUnit compUnit : units) {
			if (compUnit == null)
				continue;
			ASTNode node = getASTNodeFromCompilationUnit(compUnit);
			if (!(node instanceof CompilationUnit))
				continue;
			bindings = WorkspaceUtilities.scanForBindings(compUnit, node);
			finder.runAnalysis(this, compUnit, (CompilationUnit)node);
			bindings = null;
		}
		
		for(ICompilationUnit compUnit : units) {
			try {
				if(compUnit == null) {
					if(logger.isLoggable(Level.WARNING))
						logger.warning("AbstractCompilationUnitAnalysis: null CompilationUnit");
					continue;
				}
				// TODO Only delete Crystal-generated markers
				compUnit.getResource().deleteMarkers(IMarker.PROBLEM, true, IResource.DEPTH_INFINITE);

			// Retrieve the path of this compilation unit, and output it
				IResource resource = compUnit.getCorrespondingResource();
				if(resource != null) {
					IPath path = resource.getLocation();
					if(path != null) {
						if(logger.isLoggable(Level.INFO)) 
							logger.info("Running Crystal on " + path.toPortableString());
						if(regressionLogger.isLoggable(Level.INFO))
							regressionLogger.info("Running Crystal on " + path.toFile().getName());
					}
				}
			} 
			catch (JavaModelException e) {
				logger.log(Level.WARNING, 
						"AbstractCompilationUnitAnalysis: Unable to retrieve path of CompilationUnit" + compUnit.getElementName(),
						e);
			} 
			catch (CoreException e) {
				logger.log(Level.WARNING, 
						"Could not remove markers from CompilationUnit" + compUnit.getElementName(),
						e);
			}
			
			runAnalysesOnSingleUnit(compUnit, output, user);
		}
		annoDB = null;
	}
	
	/**
	 * Runs all analyses on the compilation unit.
	 * @param compUnit
	 * @param output The debug output
	 * @param user The output to the user
	 */
	private void runAnalysesOnSingleUnit(ICompilationUnit compUnit, PrintWriter output, PrintWriter user) {
		// Obtain the AST for this CompilationUnit and analyze it
		ASTNode node = getASTNodeFromCompilationUnit(compUnit);
		
		if (!(node instanceof CompilationUnit)) {
			if(logger.isLoggable(Level.WARNING))
				logger.warning("Root node is not a CompilationUnit " + compUnit.getElementName());
			return;
		}
		
		bindings = WorkspaceUtilities.scanForBindings(compUnit, node);
		boolean logInfo = logger.isLoggable(Level.INFO);
		for (ICrystalAnalysis crystalAnalysis : analyses) {
			String analysisName = null;
			try {
				analysisName = crystalAnalysis.getName();
				if(logInfo) logger.info("Begin [" + analysisName + "] Analysis");
				crystalAnalysis.runAnalysis(this, compUnit, (CompilationUnit)node);
				if(logInfo) logger.info("End [" + analysisName + "] Analysis");
			} 
			catch (Throwable e) {
				logger.log(Level.SEVERE, 
						"Exception during analysis" + (analysisName != null ? " [ " + analysisName + " ] " : ""), 
						e);
//				String message;
//				
//				if (e instanceof CrystalRuntimeException)
//					message = "\n*** INTERNAL CRYSTAL EXCEPTION ***";
//				else if (e instanceof StudentRuntimeException)
//					message = "\n*** ANALYSIS EXCEPTION ***";
//				else
//					message = "\n*** EXCEPTION ***";
//				if (analysisName != null)
//					message += " [ " + analysisName + " analysis ]";
//				user.println(message);
//				e.printStackTrace(user);
			}
		}
		bindings = null;
	}
	
	/**
	 * Gets the root ASTNode for a compilation unit, with bindings on
	 * @param compUnit
	 * @return
	 */
	private ASTNode getASTNodeFromCompilationUnit(ICompilationUnit compUnit) {
	 	ASTParser parser = ASTParser.newParser(AST.JLS3);
 		parser.setResolveBindings(true);
 		parser.setSource(compUnit);
 		return parser.createAST(null);
	}

	public void registerAnnotation(String annotationName,
			Class<? extends ICrystalAnnotation> annoClass) {
		annotationRegistry.put(annotationName, annoClass);
	}
	
}