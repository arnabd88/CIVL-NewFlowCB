package edu.udel.cis.vsl.civl.kripke.IF;

import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.udel.cis.vsl.civl.model.IF.statement.CallOrSpawnStatement;
import edu.udel.cis.vsl.civl.model.IF.statement.Statement;
import edu.udel.cis.vsl.civl.semantics.IF.Transition;
import edu.udel.cis.vsl.civl.state.IF.State;
import edu.udel.cis.vsl.civl.state.IF.UnsatisfiablePathConditionException;
import edu.udel.cis.vsl.sarl.IF.expr.BooleanExpression;
import edu.udel.cis.vsl.sarl.IF.expr.SymbolicExpression;

/**
 * A Library Enabler provides a method to compute the enabled transitions of
 * system function calls at a certain state for a give process. It also provides
 * a method to compute the ample set of processes at a given state for a given
 * process. A new library is implemented in the package named as
 * "edu.udel.cis.vsl.civl.library." ( {@link CommonLibraryLoader#CLASS_PREFIX})
 * + library name. And the class name of the enabler is: "Lib" + library name +
 * "Enabler". For example, the stdio library enabler is implemented as the class
 * edu.udel.cis.vsl.civl.library.stdio.LibstdioEnabler.
 * 
 * @author Manchun Zheng (zmanchun)
 * 
 */
public interface LibraryEnabler {

	/**
	 * <p>
	 * Computes the ample set process ID's from a system function call at a
	 * given state for a given process.
	 * </p>
	 * 
	 * <p>
	 * Precondition: the call statement is enabled at the given state and it is
	 * one of the outgoing statements of process pid.
	 * </p>
	 * 
	 * <p>
	 * Contract: nothing dependent on the given statement can occur without one
	 * of the transitions of the processes in the returned ample set occurring
	 * first.
	 * </p>
	 * 
	 * @param state
	 *            The current state.
	 * @param pid
	 *            The ID of the process that the system function call belongs
	 *            to.
	 * @param statement
	 *            The system function call statement.
	 * @param reachableMemUnitsMap
	 *            The map of reachable memory units of all active processes.
	 * @return
	 */
	Set<Integer> ampleSet(State state, int pid, CallOrSpawnStatement statement,
			Map<Integer, Map<SymbolicExpression, Boolean>> reachableMemUnitsMap)
			throws UnsatisfiablePathConditionException;

	/**
	 * Computes the enabled transitions of a given function call. This is to
	 * support nondeterministic function calls.
	 * 
	 * @param state
	 *            The current state.
	 * @param call
	 *            The function call statement, upon which the set of enabled
	 *            transitions will be computed.
	 * @param pathCondition
	 *            The current path condition.
	 * @param pid
	 *            The ID of the process that the function call belongs to.
	 * @param assignAtomicLock
	 *            The assignment statement for the atomic lock variable, should
	 *            be null except that the process is going to re-obtain the
	 *            atomic lock variable.
	 * @return The set of enabled transitions.
	 */
	List<Transition> enabledTransitions(State state, CallOrSpawnStatement call,
			BooleanExpression pathCondition, int pid, int processIdentifier,
			Statement assignAtomicLock)
			throws UnsatisfiablePathConditionException;
}
