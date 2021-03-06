/*
 * Copyright 2013 Stephen F. Siegel, University of Delaware
 */
package edu.udel.cis.vsl.sarl.ideal.simplify;

import static edu.udel.cis.vsl.sarl.ideal.simplify.CommonObjects.assumption;
import static edu.udel.cis.vsl.sarl.ideal.simplify.CommonObjects.bigMixedXYTermPoly;
import static edu.udel.cis.vsl.sarl.ideal.simplify.CommonObjects.idealSimplifier;
import static edu.udel.cis.vsl.sarl.ideal.simplify.CommonObjects.idealSimplifierFactory;
import static edu.udel.cis.vsl.sarl.ideal.simplify.CommonObjects.int0;
import static edu.udel.cis.vsl.sarl.ideal.simplify.CommonObjects.int1;
import static edu.udel.cis.vsl.sarl.ideal.simplify.CommonObjects.intNeg1;
import static edu.udel.cis.vsl.sarl.ideal.simplify.CommonObjects.mixedXYTermPoly;
import static edu.udel.cis.vsl.sarl.ideal.simplify.CommonObjects.preUniv;
import static edu.udel.cis.vsl.sarl.ideal.simplify.CommonObjects.rat0;
import static edu.udel.cis.vsl.sarl.ideal.simplify.CommonObjects.x;
import static edu.udel.cis.vsl.sarl.ideal.simplify.CommonObjects.xInt;
import static edu.udel.cis.vsl.sarl.ideal.simplify.CommonObjects.xSqrLess1;
import static edu.udel.cis.vsl.sarl.ideal.simplify.CommonObjects.xSqrP1;
import static edu.udel.cis.vsl.sarl.ideal.simplify.CommonObjects.y;
import static org.junit.Assert.assertEquals;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Testing on IdealSimplifier with expressions that have a variable and its
 * respective terms drop due to being set equal to 0. Aims to confirm that
 * remaining terms, be they constant or variable, agree with expectations.
 * 
 * @author danfried
 *
 */
public class SimplifyEqualsZeroTest {

	/**
	 * Calls the setUp() method in CommonObjects to make use of consolidated
	 * SARL object declarations and initializations for testing of "Simplify"
	 * module.
	 * 
	 * @throws java.lang.Exception
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		CommonObjects.setUp();
		// assumption = preUniv.lessThan(int0, xInt);
		assumption = preUniv.equals(xInt, int0);
		idealSimplifier = idealSimplifierFactory.newSimplifier(assumption);
	}

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
	}

	/**
	 * Testing of IdealSimplifier with expressions when variable is dropped, and
	 * only a constant (integer) remains.
	 */
	@Test
	public void assumptionTest() {
		// out.println(xSqrLess1);
		// out.println(idealSimplifier.simplifyExpression(xSqrLess1));
		// assumption: x=0. Simplify x^2-1 -> -1
		assertEquals(intNeg1, idealSimplifier.simplifyExpression(xSqrLess1));
		assertEquals(int1, idealSimplifier.simplifyExpression(xSqrP1));
		// out.println(idealSimplifier.apply(symbExpr_xpyInt));
		// out.println(idealSimplifier.simplifyExpression(symbExpr_xpyInt));
		// out.println("xx - 1 : " + xSqrLess1.toString());
		// IdealSimplifier.
		// out.println(mixedXYTermPoly);
		// out.println(bigMixedXYTermPoly);
	}

	/**
	 * Testing of IdealSimplifier with expressions of two two variables, when
	 * one is dropped by being set equal to 0.
	 */
	@Test
	public void assumptionOnPolyTest() {
		assumption = preUniv.equals(x, rat0);
		idealSimplifier = idealSimplifierFactory.newSimplifier(assumption);

		// out.println(idealSimplifier.apply(bigMixedXYTermPoly));
		assertEquals(idealSimplifier.apply(bigMixedXYTermPoly).toString(),
				preUniv.multiply(y, preUniv.multiply(y, y)).toString());
		// out.println(idealSimplifier.simplifyExpression(mixedXYTermPoly));
		assertEquals(idealSimplifier.simplifyExpression(mixedXYTermPoly), y);
	}

}
