/**
 * 
 */
package edu.udel.cis.vsl.civl.model.common.location;

import java.io.PrintStream;
import java.util.ArrayList;

import edu.udel.cis.vsl.civl.err.CIVLInternalException;
import edu.udel.cis.vsl.civl.model.IF.CIVLFunction;
import edu.udel.cis.vsl.civl.model.IF.CIVLSource;
import edu.udel.cis.vsl.civl.model.IF.Scope;
import edu.udel.cis.vsl.civl.model.IF.location.Location;
import edu.udel.cis.vsl.civl.model.IF.statement.Statement;
import edu.udel.cis.vsl.civl.model.IF.statement.WaitStatement;
import edu.udel.cis.vsl.civl.model.common.CommonSourceable;

/**
 * The parent of all locations.
 * 
 * @author Timothy K. Zirkel (zirkel)
 * 
 */
public class CommonLocation extends CommonSourceable implements Location {

	private int id;
	private Scope scope;
	private ArrayList<Statement> incoming = new ArrayList<>();
	private ArrayList<Statement> outgoing = new ArrayList<>();
	private CIVLFunction function;
	private boolean purelyLocal = false;

	/**
	 * The parent of all locations.
	 * 
	 * @param scope
	 *            The scope containing this location.
	 * @param id
	 *            The unique id of this location.
	 */
	public CommonLocation(CIVLSource source, Scope scope, int id) {
		super(source);
		this.scope = scope;
		this.id = id;
		this.function = scope.function();
	}

	/**
	 * @return The unique ID number of this location.
	 */
	public int id() {
		return id;
	}

	/**
	 * @return The scope of this location.
	 */
	public Scope scope() {
		return scope;
	}

	/**
	 * @return The function containing this location.
	 */
	public CIVLFunction function() {
		return function;
	}

	/**
	 * @return The set of incoming statements.
	 */
	public Iterable<Statement> incoming() {
		return incoming;
	}

	/**
	 * @return The set of outgoing statements.
	 */
	public Iterable<Statement> outgoing() {
		return outgoing;
	}

	/**
	 * Set the unique ID number of this location.
	 * 
	 * @param id
	 *            The unique ID number of this location.
	 */
	public void setId(int id) {
		this.id = id;
	}

	/**
	 * @param scope
	 *            The scope of this location.
	 */
	public void setScope(Scope scope) {
		this.scope = scope;
		this.function = scope.function();
	}

	/**
	 * Print this location and all outgoing transitions.
	 * 
	 * @param prefix
	 *            The prefix string for all lines of this printout.
	 * @param out
	 *            The PrintStream to use for printing this location.
	 */
	public void print(String prefix, PrintStream out) {
		String targetLocation = null;
		String guardString = "(true)";
		String gotoString;

		if (this.purelyLocal) {
			out.println(prefix + "location " + id() + " (scope: " + scope.id()
					+ ") #");
		} else
			out.println(prefix + "location " + id() + " (scope: " + scope.id()
					+ ")");
		for (Statement statement : outgoing) {
			if (statement.target() != null) {
				targetLocation = "" + statement.target().id();
			}
			if (statement.guard() != null) {
				guardString = "(" + statement.guard() + ")";
			}
			if (statement.isPurelyLocal()) {
				gotoString = prefix + "| " + "when " + guardString + " "
						+ statement + " @ "
						+ statement.getSource().getLocation() + " ; #";
			} else
				gotoString = prefix + "| " + "when " + guardString + " "
						+ statement + " @ "
						+ statement.getSource().getLocation() + " ;";
			if (targetLocation != null) {
				gotoString += " goto location " + targetLocation;
			}
			out.println(gotoString);
		}
	}

	@Override
	public String toString() {
		return "Location " + id;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + id;
		result = prime * result + ((scope == null) ? 0 : scope.hashCode());
		return result;
	}

	@Override
	public void addIncoming(Statement statement) {
		incoming.add(statement);
	}

	@Override
	public void addOutgoing(Statement statement) {
		outgoing.add(statement);
	}

	@Override
	public void removeOutgoing(Statement statement) {
		outgoing.remove(statement);
	}

	@Override
	public void removeIncoming(Statement statement) {
		incoming.remove(statement);
	}

	@Override
	public boolean equals(Object that) {
		if (that instanceof CommonLocation) {
			return (((CommonLocation) that).id() == id);
		}
		return false;
	}

	@Override
	public Statement getSoleOutgoing() {
		int size = outgoing.size();

		if (size >= 1) {
			Statement result = outgoing.iterator().next();

			if (size > 1) {
				throw new CIVLInternalException(
						"Expected 1 outgoing transition but saw " + size,
						result.getSource());
			}
			return result;
		}
		throw new CIVLInternalException(
				"Expected 1 outgoing transition but saw 0 at " + this
						+ " in function " + function, this.getSource());
	}

	@Override
	public int getNumOutgoing() {
		return outgoing.size();
	}

	@Override
	public int getNumIncoming() {
		return incoming.size();
	}

	@Override
	public Statement getOutgoing(int i) {
		return outgoing.get(i);
	}

	@Override
	public Statement getIncoming(int i) {
		return incoming.get(i);
	}

	@Override
	public boolean isPurelyLocal() {
		return this.purelyLocal;
	}

	@Override
	public void purelyLocalAnalysis() {
//		if(incoming.size() > 1)
//			this.purelyLocal = false;
		//else 
		if(outgoing.size() != 1)

			this.purelyLocal = false;
		else {
			for (Statement s : outgoing) {
				if (s instanceof WaitStatement)
					this.purelyLocal = false;
				else {
					this.purelyLocal = s.isPurelyLocal();
					return;
				}
			}
		}
	}

}
