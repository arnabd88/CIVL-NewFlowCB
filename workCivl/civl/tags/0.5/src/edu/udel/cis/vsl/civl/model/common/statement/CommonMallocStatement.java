package edu.udel.cis.vsl.civl.model.common.statement;

import edu.udel.cis.vsl.civl.model.IF.CIVLSource;
import edu.udel.cis.vsl.civl.model.IF.Scope;
import edu.udel.cis.vsl.civl.model.IF.expression.ConditionalExpression;
import edu.udel.cis.vsl.civl.model.IF.expression.Expression;
import edu.udel.cis.vsl.civl.model.IF.expression.LHSExpression;
import edu.udel.cis.vsl.civl.model.IF.expression.VariableExpression;
import edu.udel.cis.vsl.civl.model.IF.location.Location;
import edu.udel.cis.vsl.civl.model.IF.statement.MallocStatement;
import edu.udel.cis.vsl.civl.model.IF.statement.Statement;
import edu.udel.cis.vsl.civl.model.IF.type.CIVLType;
import edu.udel.cis.vsl.sarl.IF.expr.SymbolicExpression;
import edu.udel.cis.vsl.sarl.IF.type.SymbolicArrayType;
import edu.udel.cis.vsl.sarl.IF.type.SymbolicType;

public class CommonMallocStatement extends CommonStatement implements
		MallocStatement {

	private int id;

	private Expression heapPointerExpression;

	private CIVLType staticElementType;

	private SymbolicType dynamicElementType;

	private SymbolicArrayType dynamicObjectType;

	private Expression sizeExpression;

	private SymbolicExpression undefinedObject;

	private LHSExpression lhs;

	public CommonMallocStatement(CIVLSource civlSource, Location source,
			int mallocId, Expression heapPointerExpression,
			CIVLType staticElementType, SymbolicType dynamicElementType,
			SymbolicArrayType dynamicObjectType, Expression sizeExpression,
			SymbolicExpression undefinedObject, LHSExpression lhs) {
		super(civlSource, source);
		this.id = mallocId;
		this.heapPointerExpression = heapPointerExpression;
		this.staticElementType = staticElementType;
		this.dynamicElementType = dynamicElementType;
		this.dynamicObjectType = dynamicObjectType;
		this.sizeExpression = sizeExpression;
		this.undefinedObject = undefinedObject;
		this.lhs = lhs;
	}

	@Override
	public int getMallocId() {
		return id;
	}

	@Override
	public Expression getHeapPointerExpression() {
		return heapPointerExpression;
	}

	@Override
	public CIVLType getStaticElementType() {
		return staticElementType;
	}

	@Override
	public SymbolicType getDynamicElementType() {
		return dynamicElementType;
	}

	@Override
	public SymbolicArrayType getDynamicObjectType() {
		return dynamicObjectType;
	}

	@Override
	public Expression getSizeExpression() {
		return sizeExpression;
	}

	@Override
	public SymbolicExpression getUndefinedObject() {
		return undefinedObject;
	}

	@Override
	public LHSExpression getLHS() {
		return lhs;
	}

	@Override
	public String toString() {
		String result;

		if (lhs != null)
			result = lhs + " = ";
		else
			result = "";
		result += "(" + staticElementType + "*)";
		result += "$malloc(" + heapPointerExpression + ", " + sizeExpression
				+ ")";
		return result;
	}

	@Override
	public void calculateDerefs() {
		boolean lhsDeref = false;
		if (lhs != null) {
			lhs.calculateDerefs();
			lhsDeref = lhs.hasDerefs();
		}
		this.heapPointerExpression.calculateDerefs();
		this.sizeExpression.calculateDerefs();
		this.hasDerefs = lhsDeref || this.heapPointerExpression.hasDerefs()
				|| this.sizeExpression.hasDerefs();
	}

	@Override
	public void purelyLocalAnalysisOfVariables(Scope funcScope) {
		super.purelyLocalAnalysisOfVariables(funcScope);
		if (lhs != null) {
			lhs.purelyLocalAnalysisOfVariables(funcScope);
		}
		this.heapPointerExpression.purelyLocalAnalysisOfVariables(funcScope);
		this.sizeExpression.purelyLocalAnalysisOfVariables(funcScope);
	}

	@Override
	public void purelyLocalAnalysis() {

		this.guard().purelyLocalAnalysis();

		boolean lhsPL = true;
		if (lhs != null) {
			lhs.purelyLocalAnalysis();
			lhsPL = lhs.isPurelyLocal();
		}

		this.heapPointerExpression.purelyLocalAnalysis();
		this.sizeExpression.purelyLocalAnalysis();

		this.purelyLocal = lhsPL && this.guard().isPurelyLocal()
				&& this.heapPointerExpression.isPurelyLocal()
				&& this.sizeExpression.isPurelyLocal();
	}

	@Override
	public void replaceWith(ConditionalExpression oldExpression,
			VariableExpression newExpression) {
		super.replaceWith(oldExpression, newExpression);

		if (sizeExpression == oldExpression) {
			sizeExpression = newExpression;
			return;
		}

		this.sizeExpression.replaceWith(oldExpression, newExpression);
	}

	@Override
	public Statement replaceWith(ConditionalExpression oldExpression,
			Expression newExpression) {
		Expression newGuard = guardReplaceWith(oldExpression, newExpression);
		CommonMallocStatement newStatement = null;

		if (newGuard != null) {
			newStatement = new CommonMallocStatement(this.getSource(),
					this.source(), this.id, this.heapPointerExpression,
					staticElementType, dynamicElementType, dynamicObjectType,
					this.sizeExpression, undefinedObject, lhs);
			newStatement.setGuard(newGuard);
		} else {
			Expression newSizeExpression = sizeExpression.replaceWith(
					oldExpression, newExpression);

			if (newSizeExpression != null) {
				newStatement = new CommonMallocStatement(this.getSource(),
						this.source(), id, this.heapPointerExpression,
						staticElementType, dynamicElementType,
						dynamicObjectType, newSizeExpression, undefinedObject,
						lhs);
				newStatement.setGuard(this.guard());
			}
		}
		return newStatement;
	}

}
