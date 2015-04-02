# Eclipse update site #

The easiest way to install Crystal is to update your existing Eclipse installation.  Use the following update site and follow the instructions.

http://crystalsaf.googlecode.com/svn/trunk/EclipseUpdate/

You may want to install the **optional** Plaid Annotations plugin, available at the following update site:

http://plaidannotations.googlecode.com/svn/trunk/PlaidAnnotationsUpdateSite/

## Installing Crystal 3.5 ##

We recommend using Crystal 3.5 with Eclipse 3.5.

  1. Select Help -> Install New Software... from the main menu.
  1. Use the "Add..." button to add the Crystal update site location above (if not already present).  The name of the site doesn't matter but you can use "Crystal Update Site"
  1. You may have to **uncheck** "Group items by category" to see Crystal
  1. Check "Crystal" from the list of plugins
  1. Click the "Next >" button and follow the instructions.

Eclipse will automatically download and install Crystal.

## Installing Plaid Annotations ##
You will need Plaid annotations to take advantage of Crystal's @MultiAnnotation and built-in analysis test features.  You also need Plaid Annotations to run [Plural](http://code.google.com/p/pluralism).

  1. Select Help -> Install New Software... from the main menu.
  1. Use the "Add..." button to add the Plaid Annotations update site above (if not already present).  The name of the site doesn't matter but you can use "Plaid Annotations Update Site"
  1. You may have to uncheck "Group items by category" to see the Plaid Annotations
  1. Check "Plaid Annotations" from the list of plugins
  1. Click the "Next >" button and follow the instructions, accept the license, etc.

## Crystal 3.4 ##
Earlier versions of Crystal **require** Plaid Annotations, so go through its installation before installing a Crystal 3.4 version.

## Macs and Eclipse 3.5 ##
If you are using a Mac, make sure you get the right version of Eclipse. If you intend to run Java 1.6, you will need the 64-bit version of Eclipse.

# Source installation #

You can use Subversion to check out the CrystalPlugin module from this repository.  CrystalPlugin is an Eclipse plug-in project that you should add to your Eclipse workspace.  (If you are checking out through Eclipse it will offer to add the project automatically to your workspace.)  Create a "Run Configuration" for an "Eclipse Application" and make sure "edu.cmu.cs.crystal" is among the loaded Plug-ins.  Running the application will open a second Eclipse window, called the _child eclipse_, in which Crystal is loaded.  When using Crystal sources (which include the annotations), you may still need the annotation Jar from the "Downloads" area to build code in the child eclipse.

# Troubleshooting #
  * Note that if you intend to build Crystal from source, you must have certain Eclipse plug-ins such as [PDE](http://www.eclipse.org/pde/) and [JDT](http://www.eclipse.org/jdt/). These plugins do not come with every Eclipse package. In particular, they do not come with "Eclipse for Java Developers." We recommend using either "Eclipse Classic" or "Eclipse for RCP/Plug-in Developers."
  * If you are using a Mac, and received an error about 64-bit JVM and SWT when trying to run Crystal, you are using the wrong version of Eclipse with the wrong version of Java. If you want to use Java 1.6 (recommended), you will need to install the 64-bit version of Eclipse. If your system does not support either 64-bit or Java 1.6, you must use Java 1.5 and the 32-bit version of Eclipse. Crystal supports both Java 5 and Java 6.