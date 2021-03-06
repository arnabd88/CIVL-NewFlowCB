package edu.udel.cis.vsl.civl.library.common;

import edu.udel.cis.vsl.civl.dynamic.IF.SymbolicUtility;
import edu.udel.cis.vsl.civl.semantics.IF.SymbolicAnalyzer;
import edu.udel.cis.vsl.sarl.IF.SymbolicUniverse;
import edu.udel.cis.vsl.sarl.IF.expr.BooleanExpression;
import edu.udel.cis.vsl.sarl.IF.expr.NumericExpression;
import edu.udel.cis.vsl.sarl.IF.object.IntObject;

/**
 * The LibraryComponent class provides the common data and operations of library
 * evaluator, enabler, and executor.
 * 
 * @author Manchun Zheng (zmanchun)
 * 
 */
public abstract class LibraryComponent {

	// The order of these operations should be consistent with civlc.cvh
	// file.
	protected enum CIVLOperation {
		CIVL_NO_OP, // no operation
		CIVL_MAX, // maxinum
		CIVL_MIN, // minimun
		CIVL_SUM, // sum
		CIVL_PROD, // product
		CIVL_LAND, // logical and
		CIVL_BAND, // bit-wise and
		CIVL_LOR, // logical or
		CIVL_BOR, // bit-wise or
		CIVL_LXOR, // logical exclusive or
		CIVL_BXOR, // bit-wise exclusive or
		CIVL_MINLOC, // min value and location
		CIVL_MAXLOC, // max value and location
		CIVL_REPLACE // replace ? TODO: Find definition for this operation
	}

	/**
	 * The symbolic expression of one.
	 */
	protected NumericExpression one;

	/**
	 * The symbolic object of integer one.
	 */
	protected IntObject oneObject;

	/**
	 * The symbolic expression of three.
	 */
	protected NumericExpression three;

	/**
	 * The symbolic object of integer three.
	 */
	protected IntObject threeObject;

	/**
	 * The symbolic expression of two.
	 */
	protected NumericExpression two;

	/**
	 * The symbolic object of integer two.
	 */
	protected IntObject twoObject;

	/**
	 * The symbolic expression of zero.
	 */
	protected NumericExpression zero;

	/**
	 * The symbolic object of integer zero.
	 */
	protected IntObject zeroObject;

	/**
	 * The symbolic universe for symbolic computations.
	 */
	protected SymbolicUniverse universe;

	/**
	 * The symbolic universe for symbolic computations.
	 */
	protected SymbolicUtility symbolicUtil;

	/**
	 * The symbolic analyzer for operations on symbolic expressions and states.
	 */
	protected SymbolicAnalyzer symbolicAnalyzer;

	protected String name;

	protected BooleanExpression trueValue;
	protected BooleanExpression falseValue;

	/**
	 * Creates a new instance of a library.
	 * 
	 * @param universe
	 *            The symbolic universe to be used.
	 * @param symbolicUtil
	 *            The symbolic utility to be used.
	 * @param symbolicAnalyzer
	 *            The symbolic analyzer to be used.
	 */
	protected LibraryComponent(String name, SymbolicUniverse universe,
			SymbolicUtility symbolicUtil, SymbolicAnalyzer symbolicAnalyzer) {
		this.name = name;
		this.universe = universe;
		this.zero = universe.zeroInt();
		this.one = universe.oneInt();
		this.two = universe.integer(2);
		this.three = universe.integer(3);
		this.zeroObject = universe.intObject(0);
		this.oneObject = universe.intObject(1);
		this.twoObject = universe.intObject(2);
		this.threeObject = universe.intObject(3);
		this.symbolicUtil = symbolicUtil;
		this.symbolicAnalyzer = symbolicAnalyzer;
		this.trueValue = universe.trueExpression();
		this.falseValue = universe.falseExpression();
	}

	/**
	 * Returns the name associated to this library executor or enabler, for
	 * example, "stdio".
	 * 
	 * @return
	 */
	public String name() {
		return this.name;
	}
}
