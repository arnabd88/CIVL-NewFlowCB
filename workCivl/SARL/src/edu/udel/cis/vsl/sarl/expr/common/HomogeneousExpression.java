/*******************************************************************************
 * Copyright (c) 2013 Stephen F. Siegel, University of Delaware.
 * 
 * This file is part of SARL.
 * 
 * SARL is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 * 
 * SARL is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with SARL. If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package edu.udel.cis.vsl.sarl.expr.common;

import java.util.Arrays;

import edu.udel.cis.vsl.sarl.IF.expr.NumericExpression;
import edu.udel.cis.vsl.sarl.IF.expr.SymbolicExpression;
import edu.udel.cis.vsl.sarl.IF.object.BooleanObject;
import edu.udel.cis.vsl.sarl.IF.object.SymbolicObject;
import edu.udel.cis.vsl.sarl.IF.object.SymbolicSequence;
import edu.udel.cis.vsl.sarl.IF.type.SymbolicType;
import edu.udel.cis.vsl.sarl.IF.type.SymbolicType.SymbolicTypeKind;
import edu.udel.cis.vsl.sarl.object.IF.ObjectFactory;
import edu.udel.cis.vsl.sarl.object.common.CommonSymbolicObject;
import edu.udel.cis.vsl.sarl.util.ArrayIterable;

/**
 * Implementation of {@link SymbolicExpression} in which every argument belongs
 * to some type <code>T</code> which extends {@link SymbolicObject}.
 */
public class HomogeneousExpression<T extends SymbolicObject>
		extends CommonSymbolicObject implements SymbolicExpression {

	/** Turn this one to print debugging information */
	public static final boolean debug = false;

	/** The operator of this expression. */
	protected SymbolicOperator operator;

	/** The type of this expression */
	protected SymbolicType type;

	/** The arguments of this expression */
	protected T[] arguments;

	// Constructors...

	/**
	 * Constructs a new instance from the given operator, type, and array of
	 * arguments. This is the primary constructor; the others go through this
	 * one. The given parameters are used directly to become the new fields of
	 * the new expressions; nothing is cloned.
	 * 
	 * @param operator
	 *            the operator for the new expression; must be non-null
	 * @param type
	 *            the type of the new expression; can only be <code>null</code>
	 *            if the operator is {@link SymbolicOperator#NULL}
	 * @param arguments
	 *            the arguments for the new expression; must be non-null, but
	 *            can have length 0
	 */
	protected HomogeneousExpression(SymbolicOperator operator,
			SymbolicType type, T[] arguments) {
		assert operator != null;
		assert operator == SymbolicOperator.NULL || type != null;
		assert arguments != null;
		this.operator = operator;
		this.type = type;
		this.arguments = arguments;
	}

	@Override
	public SymbolicObjectKind symbolicObjectKind() {
		return SymbolicObjectKind.EXPRESSION;
	}

	/**
	 * Returns the type of this symbolic expression.
	 */
	public SymbolicType type() {
		return type;
	}

	/**
	 * Returns the arguments of this symbolic expression.
	 */
	public T[] arguments() {
		return arguments;
	}

	@Override
	public Iterable<T> getArguments() {
		return new ArrayIterable<T>(arguments);
	}

	/**
	 * Know that o has argumentKind SYMBOLIC_EXPRESSION and is not == to this.
	 */
	@Override
	protected boolean intrinsicEquals(SymbolicObject o) {
		HomogeneousExpression<?> that = (HomogeneousExpression<?>) o;

		return operator == that.operator()
				&& ((type == null && that.type == null)
						|| type.equals(that.type))
				&& Arrays.equals(arguments, that.arguments);
	}

	/**
	 * Returns the type HashCode if not Null and all the Expressions arguments'
	 * Hashcodes
	 */
	@Override
	protected int computeHashCode() {
		int numArgs = this.numArguments();
		int result = operator.hashCode();

		if (type != null)
			result ^= type.hashCode();
		for (int i = 0; i < numArgs; i++)
			result ^= this.argument(i).hashCode();
		return result;
	}

	/**
	 * Returns an individual argument within the SymbolicExpression
	 */
	@Override
	public T argument(int index) {
		return arguments[index];
	}

	/**
	 * Returns the operator
	 */
	@Override
	public SymbolicOperator operator() {
		return operator;
	}

	/**
	 * Returns the number of arguments within the SymbolicExpression
	 */
	@Override
	public int numArguments() {
		return arguments.length;
	}

	/**
	 * String Representation of an array of SymbolicObjects -call to
	 * toStringBufferLong() for individual string representations
	 * 
	 * @param objects
	 * @param buffer
	 *            string buffer to which computed result should be appended
	 */
	private StringBuffer toStringBufferLong(SymbolicObject[] objects) {
		StringBuffer buffer = new StringBuffer("{");
		boolean first = true;

		for (SymbolicObject object : objects) {
			if (first)
				first = false;
			else
				buffer.append(",");
			if (object == null)
				buffer.append("null");
			else
				buffer.append(object.toStringBufferLong());
		}
		buffer.append("}");
		return buffer;
	}

	/**
	 * String representation of a singular SymbolicExpression
	 */
	@Override
	public StringBuffer toStringBufferLong() {
		StringBuffer buffer = new StringBuffer(getClass().getSimpleName());

		buffer.append("[");
		buffer.append(operator.toString());
		buffer.append("; ");
		buffer.append(type != null ? type.toString() : "no type");
		buffer.append("; ");
		buffer.append(toStringBufferLong(arguments));
		buffer.append("]");
		return buffer;
	}

	/**
	 * accumulates the operator opString to every operand in the following
	 * format opString = " " + opString + " ";
	 * 
	 * @param buffer
	 *            string buffer to which computed result should be appended
	 * @param opString
	 *            the string representation of the operator, e.g. "+"
	 * @param operands
	 *            collection of Symbolic Objects
	 * @param atomizeArgs
	 *            should each argument be atomized (surrounded by parens if
	 */
	private void accumulate(StringBuffer buffer, String opString,
			SymbolicSequence<?> operands, boolean atomizeArgs) {
		boolean first = true;

		for (SymbolicExpression arg : operands) {
			if (first)
				first = false;
			else
				buffer.append(opString);
			buffer.append(arg.toStringBuffer(atomizeArgs));
		}
	}

	/**
	 * Computes string representation of a binary operator expression
	 * 
	 * @param buffer
	 *            string buffer to which computed result should be appended
	 * @param opString
	 *            the string representation of the operator, e.g. "+"
	 * @param arg0
	 *            object to be represented
	 * @param arg1
	 *            object to be represented
	 * @param atomizeArgs
	 *            should each argument be atomized (surrounded by parens if
	 *            necessary)?
	 */
	private void processBinary(StringBuffer buffer, String opString,
			SymbolicObject arg0, SymbolicObject arg1, boolean atomizeArgs) {
		buffer.append(arg0.toStringBuffer(atomizeArgs));
		buffer.append(opString);
		buffer.append(arg1.toStringBuffer(atomizeArgs));
	}

	private void processSum(StringBuffer buffer, boolean atomizeResult) {
		int n = arguments.length;

		assert n > 0;
		if (n == 1) {
			buffer.append(arguments[0].toStringBuffer(atomizeResult));
		} else {
			buffer.append(arguments[0].toStringBuffer(false));

			for (int i = 1; i < n; i++) {
				StringBuffer argString = arguments[i].toStringBuffer(false);

				if ("-".equals(argString.substring(0, 1))) {
					argString.delete(0, 1);
					buffer.append(" - ");
				} else {
					buffer.append(" + ");
				}
				buffer.append(argString);
			}
			if (atomizeResult) {
				buffer.insert(0, '(');
				buffer.append(')');
			}
		}
	}

	private void processProduct(StringBuffer buffer, boolean atomizeResult) {
		int n = arguments.length;

		assert n > 0;
		if (n == 1) {
			buffer.append(arguments[0].toStringBuffer(atomizeResult));
		} else {
			for (int i = 0; i < n; i++) {
				T arg = arguments[i];
				boolean atomizeArg = !(arg instanceof SymbolicExpression
						&& ((SymbolicExpression) arg)
								.operator() == SymbolicOperator.MULTIPLY);
				StringBuffer argString = arg.toStringBuffer(atomizeArg);

				if (i > 0)
					buffer.append("*");
				buffer.append(argString);
			}
			if (atomizeResult) {
				buffer.insert(0, '(');
				buffer.append(')');
			}
		}
	}

	private void processAnd(StringBuffer buffer, boolean atomizeResult) {
		int n = arguments.length;

		assert n > 0;
		if (n == 1) {
			buffer.append(arguments[0].toStringBuffer(atomizeResult));
		} else {
			for (int i = 0; i < n; i++) {
				T arg = arguments[i];
				boolean atomizeArg = arg instanceof SymbolicExpression
						&& ((SymbolicExpression) arg)
								.operator() == SymbolicOperator.OR;
				StringBuffer argString = arg.toStringBuffer(atomizeArg);

				if (i > 0)
					buffer.append(" && ");
				buffer.append(argString);
			}
			if (atomizeResult) {
				buffer.insert(0, '(');
				buffer.append(')');
			}
		}
	}

	private void processOr(StringBuffer buffer, boolean atomizeResult) {
		int n = arguments.length;

		assert n > 0;
		if (n == 1) {
			buffer.append(arguments[0].toStringBuffer(atomizeResult));
		} else {
			for (int i = 0; i < n; i++) {
				T arg = arguments[i];
				boolean atomizeArg = arg instanceof SymbolicExpression
						&& ((SymbolicExpression) arg)
								.operator() == SymbolicOperator.AND;
				StringBuffer argString = arg.toStringBuffer(atomizeArg);

				if (i > 0)
					buffer.append(" || ");
				buffer.append(argString);
			}
			if (atomizeResult) {
				buffer.insert(0, '(');
				buffer.append(')');
			}
		}
	}

	@Override
	public StringBuffer toStringBuffer(boolean atomize) {
		if (debug)
			return toStringBufferLong();
		return toStringBuffer1(atomize);
	}

	/**
	 * Returns a string representation of this object as a StringBuffer. Use
	 * this instead of "toString()" for performance reasons if you are going to
	 * be building up big strings.
	 * 
	 * @param atomize
	 *            if true, place parentheses around the string if necessary in
	 *            order to include this as a term in a larger expression
	 * @return result StringBuffer
	 */
	public StringBuffer toStringBuffer1(boolean atomize) {
		StringBuffer result = new StringBuffer();

		switch (operator) {
		case ADD:
			processSum(result, atomize);
			return result;
		case AND:
			processAnd(result, atomize);
			return result;
		case APPLY: {
			result.append(arguments[0].toStringBuffer(true));
			result.append("(");
			accumulate(result, ",", (SymbolicSequence<?>) arguments[1], false);
			result.append(")");
			return result;
		}
		case ARRAY: {
			result.append("[");
			for (int i = 0; i < arguments.length; i++) {
				if (i > 0)
					result.append(",");
				result.append(arguments[i].toStringBuffer(false));
			}
			result.append("]");
			return result;
		}
		case ARRAY_LAMBDA:
			result.append("(");
			result.append(type.toStringBuffer(false));
			result.append(")<");
			result.append(arguments[0].toStringBuffer(false));
			result.append(">");
			return result;
		case ARRAY_READ:
			result.append(arguments[0].toStringBuffer(true));
			result.append("[");
			result.append(arguments[1].toStringBuffer(false));
			result.append("]");
			return result;
		case ARRAY_WRITE:
			result.append(arguments[0].toStringBuffer(true));
			result.append("[");
			result.append(arguments[1].toStringBuffer(false));
			result.append(":=");
			result.append(arguments[2].toStringBuffer(false));
			result.append("]");
			return result;
		case CAST:
			result.append('(');
			result.append(type.toStringBuffer(false));
			result.append(')');
			result.append(arguments[0].toStringBuffer(true));
			return result;
		case CONCRETE: {
			SymbolicTypeKind tk = type.typeKind();

			if (tk == SymbolicTypeKind.CHAR) {
				result.append("'");
				result.append(arguments[0].toStringBuffer(false));
				result.append("'");
			} else {
				if (!type.isNumeric() && !type.isBoolean()) {
					if (tk == SymbolicTypeKind.TUPLE)
						result.append(type.toStringBuffer(false));
					else {
						result.append('(');
						result.append(type.toStringBuffer(false));
						result.append(')');
					}
				}
				result.append(arguments[0].toStringBuffer(atomize));
				if (type.isHerbrand())
					result.append('h');
			}
			return result;
		}
		case COND:
			result.append(arguments[0].toStringBuffer(true));
			result.append(" ? ");
			result.append(arguments[1].toStringBuffer(true));
			result.append(" : ");
			result.append(arguments[1].toStringBuffer(true));
			if (atomize)
				atomize(result);
			return result;
		case DENSE_ARRAY_WRITE: {
			int count = 0;
			boolean first = true;

			result.append(arguments[0].toStringBuffer(true));
			result.append("[");
			for (SymbolicExpression value : (SymbolicSequence<?>) arguments[1]) {
				if (!value.isNull()) {
					if (first)
						first = false;
					else
						result.append(", ");
					result.append(count + ":=");
					result.append(value.toStringBuffer(false));
				}
				count++;
			}
			result.append("]");
			return result;
		}
		case DENSE_TUPLE_WRITE: {
			int count = 0;
			boolean first = true;

			result.append(arguments[0].toStringBuffer(true));
			result.append("<");
			for (SymbolicExpression value : (SymbolicSequence<?>) arguments[1]) {
				if (!value.isNull()) {
					if (first)
						first = false;
					else
						result.append(", ");
					result.append(count + ":=");
					result.append(value.toStringBuffer(false));
				}
				count++;
			}
			result.append(">");
			return result;
		}
		case DIVIDE:
			result.append(arguments[0].toStringBuffer(true));
			result.append("/");
			result.append(arguments[1].toStringBuffer(true));
			if (atomize)
				atomize(result);
			return result;
		case EQUALS:
			result.append(arguments[0].toStringBuffer(false));
			result.append(" == ");
			result.append(arguments[1].toStringBuffer(false));
			if (atomize)
				atomize(result);
			return result;
		case EXISTS:
			result.append("exists ");
			result.append(arguments[0].toStringBuffer(false));
			result.append(" : ");
			result.append(((SymbolicExpression) arguments[0]).type()
					.toStringBuffer(false));
			result.append(" . ");
			result.append(arguments[1].toStringBuffer(true));
			if (atomize)
				atomize(result);
			return result;
		case FORALL:
			result.append("forall ");
			result.append(arguments[0].toStringBuffer(false));
			result.append(" : ");
			result.append(((SymbolicExpression) arguments[0]).type()
					.toStringBuffer(false));
			result.append(" . ");
			result.append(arguments[1].toStringBuffer(true));
			if (atomize)
				atomize(result);
			return result;
		case INT_DIVIDE: {
			result.append(arguments[0].toStringBuffer(true));
			// result.append("\u00F7");
			result.append(" div ");
			result.append(arguments[1].toStringBuffer(true));
			if (atomize)
				atomize(result);
			return result;
		}
		case LAMBDA:
			result.append("lambda ");
			result.append(arguments[0].toStringBuffer(false));
			result.append(" : ");
			result.append(((SymbolicExpression) arguments[0]).type()
					.toStringBuffer(false));
			result.append(" . ");
			result.append(arguments[1].toStringBuffer(true));
			if (atomize)
				atomize(result);
			return result;
		case LENGTH:
			result.append("length(");
			result.append(arguments[0].toStringBuffer(false));
			result.append(")");
			return result;
		case LESS_THAN:
			result.append(arguments[0].toStringBuffer(false));
			result.append(" < ");
			result.append(arguments[1].toStringBuffer(false));
			if (atomize)
				atomize(result);
			return result;
		case LESS_THAN_EQUALS:
			result.append(arguments[0].toStringBuffer(false));
			result.append(" <= ");
			result.append(arguments[1].toStringBuffer(false));
			if (atomize)
				atomize(result);
			return result;
		case MODULO:
			result.append(arguments[0].toStringBuffer(true));
			result.append("%");
			result.append(arguments[1].toStringBuffer(true));
			if (atomize)
				atomize(result);
			return result;
		case MULTIPLY:
			processProduct(result, atomize);
			return result;
		case NEGATIVE:
			result.append("-");
			result.append(arguments[0].toStringBuffer(true));
			if (atomize)
				atomize(result);
			return result;
		case NEQ:
			result.append(arguments[0].toStringBuffer(false));
			result.append(" != ");
			result.append(arguments[1].toStringBuffer(false));
			if (atomize)
				atomize(result);
			return result;
		case NOT:
			result.append("!");
			result.append(arguments[0].toStringBuffer(true));
			if (atomize)
				atomize(result);
			return result;
		case NULL:
			result.append("NULL");
			return result;
		case OR:
			processOr(result, atomize);
			return result;
		case POWER:
			result.append(arguments[0].toStringBuffer(true));
			result.append("^");
			result.append(arguments[1].toStringBuffer(true));
			if (atomize)
				atomize(result);
			return result;
		case TUPLE:
			result.append("<");
			for (int i = 0; i < arguments.length; i++) {
				if (i > 0)
					result.append(",");
				result.append(arguments[i].toStringBuffer(false));
			}
			result.append(">");
			return result;
		case SUBTRACT:
			processBinary(result, " - ", arguments[0], arguments[1], true);
			if (atomize)
				atomize(result);
			return result;
		case SYMBOLIC_CONSTANT:
			result.append(arguments[0].toStringBuffer(false));
			return result;
		case TUPLE_READ:
			result.append(arguments[0].toStringBuffer(true));
			result.append(".");
			result.append(arguments[1].toStringBuffer(false));
			if (atomize)
				atomize(result);
			return result;
		case TUPLE_WRITE:
			result.append(arguments[0].toStringBuffer(true));
			result.append("[.");
			result.append(arguments[1].toStringBuffer(false));
			result.append(":=");
			result.append(arguments[2].toStringBuffer(false));
			result.append("]");
			return result;
		case UNION_EXTRACT:
			result.append("extract(");
			result.append(arguments[0].toStringBuffer(false));
			result.append(",");
			result.append(arguments[1].toStringBuffer(false));
			result.append(")");
			return result;
		case UNION_INJECT:
			result.append("inject(");
			result.append(arguments[0].toStringBuffer(false));
			result.append(",");
			result.append(arguments[1].toStringBuffer(false));
			result.append(")");
			return result;
		case UNION_TEST:
			result.append("test(");
			result.append(arguments[0].toStringBuffer(false));
			result.append(",");
			result.append(arguments[1].toStringBuffer(false));
			result.append(")");
			return result;
		default:
			return toStringBufferLong();
		}
	}

	@Override
	public String atomString() {
		return toStringBuffer(true).toString();
	}

	@Override
	public void canonizeChildren(ObjectFactory factory) {
		int numArgs = arguments.length;

		if (type != null && !type.isCanonic())
			type = factory.canonic(type);
		for (int i = 0; i < numArgs; i++) {
			SymbolicObject arg = arguments[i];

			if (!arg.isCanonic()) {
				@SuppressWarnings("unchecked")
				T canonicArg = (T) factory.canonic(arg);

				arguments[i] = canonicArg;
			}
		}
	}

	@Override
	public boolean isNull() {
		return operator == SymbolicOperator.NULL;
	}

	@Override
	public boolean isFalse() {
		return operator == SymbolicOperator.CONCRETE
				&& arguments[0] instanceof BooleanObject
				&& !((BooleanObject) arguments[0]).getBoolean();
	}

	@Override
	public boolean isTrue() {
		return operator == SymbolicOperator.CONCRETE
				&& arguments[0] instanceof BooleanObject
				&& ((BooleanObject) arguments[0]).getBoolean();
	}

	/**
	 * Returns false, since this will be overridden in NumericExpression.
	 */
	@Override
	public boolean isZero() {
		return false;
	}

	/**
	 * Returns false, since this will be overridden in NumericExpression.
	 */
	@Override
	public boolean isOne() {
		return false;
	}

	@Override
	public boolean isNumeric() {
		return this instanceof NumericExpression;
	}
}
