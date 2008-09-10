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
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.IBinding;

import edu.cmu.cs.crystal.annotations.AnnotationDatabase;
import edu.cmu.cs.crystal.annotations.AnnotationFinder;
import edu.cmu.cs.crystal.annotations.CrystalAnnotation;
import edu.cmu.cs.crystal.annotations.ICrystalAnnotation;
import edu.cmu.cs.crystal.internal.ICrystalJob;
import edu.cmu.cs.crystal.internal.ISingleCrystalJob;
import edu.cmu.cs.crystal.internal.Option;
import edu.cmu.cs.crystal.internal.UserConsoleView;
import edu.cmu.cs.crystal.internal.Utilities;
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
	
	/**
	 * Currently unused default marker type for Crystal.
	 * @see IMarker
	 */
	public static final String MARKER_DEFAULT = "edu.cmu.cs.crystal.crystalproblem";
	
	/**
	 * Currently unused marker attribute for markers of type {@link #MARKER_DEFAULT}.
	 */
	public static final String MARKER_ATTR_ANALYSIS = "analysis";
	
	private static final Logger logger = Logger.getLogger(Crystal.class.getName());
	
	private static Logger regressionLogger = Logger.getLogger(REGRESSION_LOGGER);
	
	// TODO: Make these data structures are immutable (ie unchangable)
	/**
	 * the list of analyses to perfrom
	 */
	private LinkedList<ICrystalAnalysis> analyses;
	
	/**
	 * The names of the analyses that are enabled.
	 */
	private Set<String> enabledAnalyses;
	
	/**
	 * Permanent registry for annotation parsers, populated at plugin initialization time.
	 */
	private Map<String, Class<? extends ICrystalAnnotation>> annotationRegistry =
		new HashMap<String, Class<? extends ICrystalAnnotation>>(); 
	
	public Crystal() {
		analyses = new LinkedList<ICrystalAnalysis>();
		enabledAnalyses = new HashSet<String>();
	}
				
	/**
	 * Registers an analysis with the framework.  All analyses must be
	 * registered in order for them to be invoked.
	 * 
	 * @param	analysis	the analysis to be used
	 */
	public void registerAnalysis(ICrystalAnalysis analysis) {
		analyses.add(analysis);
		enabledAnalyses.add(analysis.getName());
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
	 * @deprecated Use {@link #runAnalyses(List<ICompilationUnit>,IProgressMonitor)} instead
	 */
	@Deprecated
	public void runAnalyses(List<ICompilationUnit> reanalyzeList) {
		runAnalyses(reanalyzeList, null);
	}

	/**
	 * Runs all of the analyses on the compilation units passed in.
	 * Will clear the console before starting.
	 * Will clear ALL the markers for each compilation unit before starting.
	 * This will run all the analyses on a single compilation unit at a time.
	 * After finishing a compilation unit, we may not hold onto any ASTNodes
	 * @param reanalyzeList The compilation units to analyze
	 * @param monitor the progress monitor used to report progress and request cancellation, 
	 * or <code>null</code> if none.  Monitor must not be initialized with {@link IProgressMonitor#beginTask(String, int)}.
	 */
	private void runAnalyses(List<ICompilationUnit> reanalyzeList, IProgressMonitor monitor) {
		if (analyses == null || analyses.isEmpty()) {
			logger.warning("Crystal::runAnalyses() No analyses registered");
			return;
		}
		Utilities.nyi();
	}
	
	public void runAnalyses(IRunCrystalCommand command, IProgressMonitor monitor) {
		runCrystalJob(createJobFromCommand(command, monitor));
	}
	
	/**
	 * Run the crystal job. At the moment, this consists of calling the {@code run}
	 * method on the job parameter, but reserves the right to run many jobs in
	 * parallel.
	 */
	private void runCrystalJob(ICrystalJob job) {
		job.runJobs();
	}
	
	/**
	 * Given a command to run some analyses on some compilation units, 
	 * creates a job to run all of those analyses. This method does many
	 * of the things that runAnalysisOnMultiUnit and runAnalysisOnSingleUnit
	 * used to do, but now those activities are packaged up as ISingleCrystalJobs
	 * and in an ICrystalJob.
	 */
	private ICrystalJob createJobFromCommand(final IRunCrystalCommand command, 
			                                 final IProgressMonitor monitor) {
		final int num_jobs = command.analyses().size() * command.compilationUnits().size();
		final List<ISingleCrystalJob> jobs = new ArrayList<ISingleCrystalJob>(num_jobs); 
		final Set<ICrystalAnalysis> used_analyses = new HashSet<ICrystalAnalysis>();
		
		for( final ICompilationUnit cu : command.compilationUnits() ) {
			for( String analysis_name : command.analyses() ) {
				final Option<ICrystalAnalysis> analysis_ = findAnalysisWithName(analysis_name);
				if( analysis_.isSome() ) {
					// Just to keep track of the total set of analyses that will be used
					used_analyses.add(analysis_.unwrap());
					
					jobs.add(new ISingleCrystalJob(){
						public ICrystalAnalysis analysis() { return analysis_.unwrap(); }
						public ICompilationUnit compilationUnit() { return cu; }
						public void run(final AnnotationDatabase annoDB) {
							if( monitor != null && monitor.isCanceled() )
								return;
							
							ICompilationUnit compUnit = compilationUnit(); 
							if(compUnit == null) {
								if(logger.isLoggable(Level.WARNING))
									logger.warning("AbstractCompilationUnitAnalysis: null CompilationUnit");
							}
							else {
								IAnalysisInput input = new IAnalysisInput() {
									public AnnotationDatabase getAnnoDB() {	return annoDB; }
								};
								
								// Run the analysis
								analysis().runAnalysis(command.reporter(),
										               input,
										               compUnit,
										               (CompilationUnit)WorkspaceUtilities.getASTNodeFromCompilationUnit(compUnit));
								if(monitor != null) {
									// increment monitor
									monitor.worked(1);
								}
							}						
						}});
				}
			}
		}
		
		return new ICrystalJob(){
			public List<ISingleCrystalJob> analysisJobs() {
				return Collections.unmodifiableList(jobs);
			}
			public void runJobs() {
				if(monitor != null) {
					String task = "Running " +num_jobs + " total analyses.";
					monitor.beginTask(task, num_jobs);
				}
				
				AnnotationDatabase annoDB = new AnnotationDatabase();
				AnnotationFinder finder = new AnnotationFinder(annoDB);
				
				// register annotation parsers from registry
				for(Map.Entry<String, Class<? extends ICrystalAnnotation>> entry : annotationRegistry.entrySet()) {
					annoDB.register(entry.getKey(), entry.getValue());
				}
				
				//register any special classes for the annotation database
				// TODO remove getAnnotationClasses() from ICrystalAnalysis
				for( ICrystalAnalysis crystalAnalysis : used_analyses ) {
					Map<String, Class<? extends CrystalAnnotation>> map = crystalAnalysis.getAnnotationClasses();
					if (map == null)
						continue;	
					for (Map.Entry<String, Class<? extends CrystalAnnotation>> entry : map.entrySet())
						annoDB.register(entry.getKey(), entry.getValue());
				}
								
				//run the annotation finder on everything
				if(monitor != null)
					monitor.subTask("Scanning annotations of analyzed compilation units");
				if(logger.isLoggable(Level.INFO))
					logger.info("Scanning annotations of analyzed compilation units");
				for( ICompilationUnit compUnit : command.compilationUnits() ) {
					if (compUnit == null)
						continue;
					ASTNode node = WorkspaceUtilities.getASTNodeFromCompilationUnit(compUnit);
					if(monitor != null && monitor.isCanceled())
						// cancel here in case cancellation can produce null or incomplete ASTs
						return;
					if (!(node instanceof CompilationUnit))
						continue;
					
					// Clear any markers that may be onscreen...
					command.reporter().clearMarkersForCompUnit(compUnit);
					
					IAnalysisInput input = new IAnalysisInput() {
						AnnotationDatabase annoDB = new AnnotationDatabase();
						public AnnotationDatabase getAnnoDB() {	return annoDB; }
					};
					
					// Run annotation finder
					finder.runAnalysis(command.reporter(),
							           input,
							           compUnit, 
							           (CompilationUnit)node);
				}
				
				// Now, run every single job
				for( ISingleCrystalJob job : analysisJobs() ) {
					job.run(annoDB);
				}
		}};
	}

	/**
	 * @param analysis_name
	 * @return
	 */
	private Option<ICrystalAnalysis> findAnalysisWithName(String analysis_name) {
		for( ICrystalAnalysis analysis : this.getAnalyses() ) {
			if( analysis.getName().equals(analysis_name) )
				return Option.some(analysis);
		}
		return Option.none();
	}
	
	/**
	 * Gets the root ASTNode for a compilation unit, with bindings on.
	 * @param compUnit
	 * @return the root ASTNode for a compilation unit, with bindings on.
	 * @deprecated Use {@link WorkspaceUtilities#getASTNodeFromCompilationUnit(ICompilationUnit)} instead
	 */
	private static ASTNode getASTNodeFromCompilationUnit(ICompilationUnit compUnit) {
		return WorkspaceUtilities.getASTNodeFromCompilationUnit(compUnit);
	}

	public void registerAnnotation(String annotationName,
			Class<? extends ICrystalAnnotation> annoClass) {
		annotationRegistry.put(annotationName, annoClass);
	}
	
}