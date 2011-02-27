package edu.cmu.cs.crystal.util;

import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import edu.cmu.cs.crystal.util.typehierarchy.CachedTypeHierarchy;

public class CachedTypeHierarchyTester {
	static private TypeHierarchy hierarchy;

	@BeforeClass
	static public void setup() throws JavaModelException {
		IJavaProject project;

		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		Assert.assertNotNull(workspace);
		IWorkspaceRoot root = workspace.getRoot();
		Assert.assertNotNull(root);
		project = (IJavaProject) JavaCore.create(root.getProject("FusionTests"));
		Assert.assertNotNull(project);
		
		hierarchy =  new CachedTypeHierarchy(project);
	}

	@Test
	public void testEquality() {
		String sub = "edu.cmu.cs.fusion.test.typehierarchy.A";
		String sup = "edu.cmu.cs.fusion.test.typehierarchy.A";
		Assert.assertTrue(hierarchy.isSubtypeCompatible(sub, sup));
	}

	@Test
	public void testDirectClassCorrect() {
		String sub = "edu.cmu.cs.fusion.test.typehierarchy.A";
		String base = "java.lang.Object";
		Assert.assertTrue(hierarchy.isSubtypeCompatible(sub, base));
	}

	@Test
	public void testDirectClassOpposite() {
		String sub = "java.lang.Object";
		String base = "edu.cmu.cs.fusion.test.typehierarchy.A";
		Assert.assertTrue(!hierarchy.isSubtypeCompatible(sub, base));
	}

	@Test
	public void testDirectClassNoRelation() {
		String sub = "edu.cmu.cs.fusion.test.typehierarchy.H";
		String base = "edu.cmu.cs.fusion.test.typehierarchy.B";
		Assert.assertTrue(!hierarchy.isSubtypeCompatible(sub, base));
	}
	
	@Test
	public void testIndirectClassCorrect() {
		String sub = "edu.cmu.cs.fusion.test.typehierarchy.C";
		String base = "java.lang.Object";
		Assert.assertTrue(hierarchy.isSubtypeCompatible(sub, base));
	}

	@Test
	public void testIndirectClassOpposite() {
		String sub = "java.lang.Object";
		String base = "edu.cmu.cs.fusion.test.typehierarchy.C";
		Assert.assertTrue(!hierarchy.isSubtypeCompatible(sub, base));
	}
	
	@Test
	public void testDirectInterfaceClassNoRelation() {
		String sub = "edu.cmu.cs.fusion.test.typehierarchy.B";
		String base = "edu.cmu.cs.fusion.test.typehierarchy.D";
		Assert.assertTrue(!hierarchy.isSubtypeCompatible(sub, base));
	}

	@Test
	public void testDirectInterfaceClassCorrect() {
		String sub = "edu.cmu.cs.fusion.test.typehierarchy.C";
		String base = "edu.cmu.cs.fusion.test.typehierarchy.D";
		Assert.assertTrue(hierarchy.isSubtypeCompatible(sub, base));
	}

	@Test
	public void testDirectInterfaceClassOpposite() {
		String sub = "edu.cmu.cs.fusion.test.typehierarchy.D";
		String base = "edu.cmu.cs.fusion.test.typehierarchy.C";
		Assert.assertTrue(!hierarchy.isSubtypeCompatible(sub, base));
	}
	
	@Test
	public void testDirectInterfaceNoRelation() {
		String sub = "edu.cmu.cs.fusion.test.typehierarchy.F";
		String base = "edu.cmu.cs.fusion.test.typehierarchy.D";
		Assert.assertTrue(!hierarchy.isSubtypeCompatible(sub, base));
	}

	@Test
	public void testDirectInterfaceCorrect() {
		String sub = "edu.cmu.cs.fusion.test.typehierarchy.G";
		String base = "edu.cmu.cs.fusion.test.typehierarchy.D";
		Assert.assertTrue(hierarchy.isSubtypeCompatible(sub, base));
	}

	@Test
	public void testDirectInterfaceOpposite() {
		String sub = "edu.cmu.cs.fusion.test.typehierarchy.D";
		String base = "edu.cmu.cs.fusion.test.typehierarchy.G";
		Assert.assertTrue(!hierarchy.isSubtypeCompatible(sub, base));
	}

	@Test
	public void testInDirectInterfaceClassCorrect() {
		String sub = "edu.cmu.cs.fusion.test.typehierarchy.C";
		String base = "edu.cmu.cs.fusion.test.typehierarchy.E";
		Assert.assertTrue(hierarchy.isSubtypeCompatible(sub, base));
	}


	
	
	
	@Test
	public void testInterfaceSeparateTree() {
		String sub = "edu.cmu.cs.fusion.test.typehierarchy.E";
		String base = "edu.cmu.cs.fusion.test.typehierarchy.K";
		Assert.assertTrue(!hierarchy.isSubtypeCompatible(sub, base));
	}

	@Test
	public void testDirectInterfaceSeparateTreeCorrect() {
		String sub = "edu.cmu.cs.fusion.test.typehierarchy.J";
		String base = "edu.cmu.cs.fusion.test.typehierarchy.I";
		Assert.assertTrue(hierarchy.isSubtypeCompatible(sub, base));
	}

	@Test
	public void testDirectInterfaceSeparateTreeOpposite() {
		String sub = "edu.cmu.cs.fusion.test.typehierarchy.I";
		String base = "edu.cmu.cs.fusion.test.typehierarchy.J";
		Assert.assertTrue(!hierarchy.isSubtypeCompatible(sub, base));
	}

	@Test
	public void testDirectInterfaceSeparateTreeNoRelation() {
		String sub = "edu.cmu.cs.fusion.test.typehierarchy.J";
		String base = "edu.cmu.cs.fusion.test.typehierarchy.K";
		Assert.assertTrue(!hierarchy.isSubtypeCompatible(sub, base));
	}

	@Test
	public void testIndirectInterfaceSeparateTreeCorrect() {
		String sub = "edu.cmu.cs.fusion.test.typehierarchy.M";
		String base = "edu.cmu.cs.fusion.test.typehierarchy.I";
		Assert.assertTrue(hierarchy.isSubtypeCompatible(sub, base));
	}

	@Test
	public void testInDirectInterfaceSeparateTreeOpposite() {
		String sub = "edu.cmu.cs.fusion.test.typehierarchy.I";
		String base = "edu.cmu.cs.fusion.test.typehierarchy.M";
		Assert.assertTrue(!hierarchy.isSubtypeCompatible(sub, base));
	}

	@Test
	public void testBug19() {
		String sub = "java.lang.Object";
		String base = "java.util.List";
		Assert.assertTrue(hierarchy.existsCommonSubtype(sub, base, true, false));
	}
}
