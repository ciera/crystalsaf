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

import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import edu.cmu.cs.crystal.Crystal;
import edu.cmu.cs.crystal.ICrystalAnalysis;
import edu.cmu.cs.crystal.annotations.ICrystalAnnotation;

/**
 * Provided Crystal plugin functionality
 * 
 * @author David Dickey
 * @author Jonathan Aldrich
 * 
 */
public abstract class AbstractCrystalPlugin extends AbstractUIPlugin {
	
	private static final Logger log = Logger.getLogger(AbstractCrystalPlugin.class.getName());
	
	/**
	 * This method is called upon plug-in activation.  Used to initialize
	 * the plugin for first time use.  Invokes setupCrystalAnalyses,
	 * which is overridden by CrystalPlugin.java to register any
	 * necessary analyses with the framework.
	 */
	static private Crystal crystal;
	
	static public Crystal getCrystalInstance() {
		synchronized(AbstractCrystalPlugin.class) {
			return crystal;
		}
	}
	
	public void start(BundleContext context) throws Exception {
		super.start(context);
		synchronized(AbstractCrystalPlugin.class) {
			if(crystal == null)
				crystal = new Crystal();
		}
		setupCrystalAnalyses(crystal);
		
		// analysis extensions
		for(IConfigurationElement config : 
			Platform.getExtensionRegistry().getConfigurationElementsFor("edu.cmu.cs.crystal.CrystalAnalysis")) {
			if("analysis".equals(config.getName()) == false) {
				if(log.isLoggable(Level.WARNING))
					log.warning("Unknown CrystalAnalysis configuration element: " + config.getName());
				continue;
			}
			try {
				ICrystalAnalysis analysis = (ICrystalAnalysis) config.createExecutableExtension("class");
				if(log.isLoggable(Level.CONFIG))
					log.config("Registering analysis extension: " + analysis.getName());
				crystal.registerAnalysis(analysis);
			}
			catch(CoreException e) {
				log.log(Level.SEVERE, "Problem with configured analysis: " + config.getValue(), e);
			}
		}
		
		// annotation extensions
		for(IConfigurationElement config : 
			Platform.getExtensionRegistry().getConfigurationElementsFor("edu.cmu.cs.crystal.CrystalAnnotation")) {
			if("customAnnotation".equals(config.getName()) == false) {
				if(log.isLoggable(Level.WARNING))
					log.warning("Unknown CrystalAnnotation configuration element: " + config.getName());
				continue;
			}
			try {
				Class<? extends ICrystalAnnotation> annoClass;
				try {
					annoClass = 
						(Class<? extends ICrystalAnnotation>) Class.forName(config.getAttribute("parserClass"));
				}
				catch(ClassNotFoundException x) {
					if(log.isLoggable(Level.WARNING))
						log.warning("Having classloader problems.  Try to add to your MANIFEST.MF: " +
								"\"Eclipse-RegisterBuddy: edu.cmu.cs.crystal\"");
					// can only directly load annotation class if defining plugin considers Crystal a "buddy"
					// See: http://www.ibm.com/developerworks/library/os-ecl-osgi/index.html#buddyoptions
					// but somehow on my eclipse the configuration is always able to create an instance
					// so we will try this and get the Class object from the returned instance
					annoClass = ((ICrystalAnnotation) config.createExecutableExtension("parserClass")).getClass();
					if(log.isLoggable(Level.WARNING))
						log.warning("Recovered from problem loading class: " + config.getAttribute("parserClass"));
				}
				if(config.getChildren("sourceAnnotation").length == 0) {
					if(log.isLoggable(Level.WARNING))
						log.warning("No @Annotation classes associated with parser: " + annoClass);
					continue;
				}
				for(IConfigurationElement anno : config.getChildren("sourceAnnotation")) {
					if(log.isLoggable(Level.CONFIG))
						log.config("Registering annotation: " + anno.getAttribute("annotationClass"));
					crystal.registerAnnotation(anno.getAttribute("annotationClass"), annoClass);
				}
			}
			catch(Throwable e) {
				log.log(Level.SEVERE, "Problem with configured annotation parser: " + config.getValue(), e);
			}
		}
	}
	
	public abstract void setupCrystalAnalyses(Crystal crystal);
}
