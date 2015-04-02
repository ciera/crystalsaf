# Crystal 3.5 #

We released Crystal 3.5 in May 2011.  You can install it from Crystal's eclipse update site (see Installation), browse the [Javadocs](http://crystalsaf.googlecode.com/svn/trunk/CrystalPlugin/doc/index.html) or go to the source.

## Transition from Crystal 3.4 ##
**Using Crystal 3.5 is likely transparent** if your analysis class extending one of Crystal's abstract base classes.

If you are implementing ICrystalAnalysis directly (or you are overriding the "runAnalysis" method of an abstract base class) you will have to change the type of one of the 3rd method parameter, "compUnit", from ICompilationUnit to ITypeRoot.  ITypeRoot is the common base class of ICompilationUnit and IClassFile, hence this chance enables running Crystal on binaries (see below).


# Release Notes #

Crystal 3.5 comes with a few improvements and API changes:
  * You can run Crystal on .class files with source attachment (requires a small interface change that should be transparent to most users).
  * Installing the [Plaid Annotations](http://code.google.com/p/plaidannotations) Eclipse plugin is no longer required (but needed for Crystal's @MultiAnnotation and analysis test features).
  * You can hit F6 to run Crystal on the .java file in the current Eclipse editor.