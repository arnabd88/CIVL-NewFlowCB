package edu.udel.cis.vsl.civl.model.common.expression;

import java.util.Set;

import edu.udel.cis.vsl.civl.model.IF.CIVLSource;
import edu.udel.cis.vsl.civl.model.IF.Scope;
import edu.udel.cis.vsl.civl.model.IF.expression.ProcnullExpression;
import edu.udel.cis.vsl.civl.model.IF.variable.Variable;

public class CommonProcnullExpression extends CommonExpression implements
		ProcnullExpression {

	public CommonProcnullExpression(CIVLSource source) {
		super(source);
	}

	@Override
	public String toString() {
		return "$proc_null";
	}

	@Override
	public ExpressionKind expressionKind() {
		return ExpressionKind.PROC_NULL;
	}

	@Override
	public Set<Variable> variableAddressedOf(Scope scope) {
		return null;
	}

	@Override
	public Set<Variable> variableAddressedOf() {
		return null;
	}

}
