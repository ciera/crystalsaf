# Pre-reqs #
This page assumes you already know how to [make a CrystalAnalysis](GettingStarted.md)


# Create the annotations #
First, you'll need to create some annotations so that the code you're analyzing can use them. We suggest the following steps, though you may do this however you want:
  * Create a new source folder, so this code is not confused with your analysis code. Call it "annotations".
  * Make the annotation types in here.
  * Right click on the source folder "annotations" and select "Export..."
  * Drill into Java -> JAR file. Click "Next".
  * Select an export destination on your machine, somewhere you can find it later. Click "Finish".
  * Use the Run menu to open the child Eclipse as though you want to run your analysis.
  * Right click on the project you want to use the annotations and select "Import..."
  * Find your jar file and click "Ok".
  * Right click on the jar file and click "Add to Build Path..."

Now your test code can use the annotations. Edit it as you wish.

# Query for annotations #
In your visitor and transfer function, you'd probably now like to use those annotations. Use `ICrystalAnalysis.getInput().getAnnoDB()` to get an `AnnotationDatabase`.

Now you can query this to find annotations by passing in the appropriate type bindings for a method, type, or field. The most common thing to do is get annotations on method parameters and return values; this information is kept by the type `AnnotationSummary`. A single annotation is represented as an `ICrystalAnnotation` and can be queried for particular values.

Notice that when asking for an annotation name from `AnnotationSummary`, you must provide the fully qualified name!


# Optional: Using the multi-annotation #
Sometimes, you want to allow clients to write more than one annotation on a target, for example:

```
@MyAnno("foo")
@MyAnno("bar")
public String method() {}
```

Unfortunately, Java doesn't allow this right now, so you have to do something like this:

```
@MyAnnos({
  @MyAnno("foo"),
  @MyAnno("bar")
})
public String method() {}
```

How do you get Crystal to recognize the MyAnno within a MyAnnos? Simply annotate @MyAnnos with @MultiAnnotation, as shown below. Crystal will automatically dive into a multi annotation, and you can treat it as though it was just two annotations on a method.

```
import edu.cmu.cs.crystal.annotations.MultiAnnotation;

@MultiAnnotation
public @interface MyAnnos {
   MyAnno[] value();
}
```

# Optional: Register an annotation parser #
By default, Crystal will read in all annotations and just store the data within a `CrystalAnnotation`. You can retrieve each parameter as an `Object`. However, if you have interesting data that you want to parse or check separately, you may want to make your own parser. This is extremely helpful for annotations which use Strings for richer information, such as:

```
@Invariant("foo == bar and forall x in xs . x == blah")
```

  * Open the plugin.xml file
  * Open the "Extensions" tab
  * Click "Add..."
  * Find the extension point "edu.cmu.cs.crystal.CrystalAnnotation" and click Finish
  * You'll now have something called "(customAnnotation)" in the left window, and if you drill into it, something like "(sourceAnnotation)". Click "(customAnnotation)".
  * In the right panel, set the parserClass to be the name of your new parser, which must implement `ICrystalAnnotation`.
  * Now click on the "(sourceAnnotation)".
  * Set the annotationClass to be the actual annotation.
  * Set parseFromMeta to false; this is an experimental feature to parse any annotation which is itself annotated by the annotationClass.