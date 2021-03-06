/**
 * 
 */
package edu.udel.cis.vsl.civl.model.common;

import java.io.PrintStream;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.Stack;

import edu.udel.cis.vsl.civl.model.IF.CIVLFunction;
import edu.udel.cis.vsl.civl.model.IF.CIVLSource;
import edu.udel.cis.vsl.civl.model.IF.Identifier;
import edu.udel.cis.vsl.civl.model.IF.Model;
import edu.udel.cis.vsl.civl.model.IF.ModelFactory;
import edu.udel.cis.vsl.civl.model.IF.Scope;
import edu.udel.cis.vsl.civl.model.IF.contract.FunctionContract;
import edu.udel.cis.vsl.civl.model.IF.expression.BinaryExpression.BINARY_OPERATOR;
import edu.udel.cis.vsl.civl.model.IF.expression.BooleanLiteralExpression;
import edu.udel.cis.vsl.civl.model.IF.expression.Expression;
import edu.udel.cis.vsl.civl.model.IF.location.Location;
import edu.udel.cis.vsl.civl.model.IF.location.Location.AtomicKind;
import edu.udel.cis.vsl.civl.model.IF.statement.NoopStatement;
import edu.udel.cis.vsl.civl.model.IF.statement.Statement;
import edu.udel.cis.vsl.civl.model.IF.type.CIVLFunctionType;
import edu.udel.cis.vsl.civl.model.IF.type.CIVLType;
import edu.udel.cis.vsl.civl.model.IF.variable.Variable;
import edu.udel.cis.vsl.civl.model.common.type.CommonFunctionType;
import edu.udel.cis.vsl.civl.util.IF.Pair;

/**
 * A function.
 * 
 * @author Timothy K. Zirkel (zirkel)
 * 
 */
public class CommonFunction extends CommonSourceable implements CIVLFunction {

	/* ************************** Instance Fields ************************** */

	private Scope containingScope;

	protected boolean isSystem = false;

	private boolean isAtomic = false;

	private Set<Location> locations;

	private Model model;

	private Identifier name;

	private Scope outerScope;

	private List<Variable> parameters;

	private List<Pair<Expression, Integer>> possibleConsequences = null;

	private CIVLFunctionType functionType;

	private Set<Scope> scopes;

	private Location startLocation;

	private Set<Statement> statements;

	private int fid;

	private FunctionContract contract = null;

	/* **************************** Constructors *************************** */

	/**
	 * A function.
	 * 
	 * @param source
	 *            The CIVL source of the function
	 * @param name
	 *            The name of this function.
	 * @param parameters
	 *            The list of parameters.
	 * @param returnType
	 *            The return type of this function.
	 * @param containingScope
	 *            The scope containing this function.
	 * @param startLocation
	 *            The first location in the function.
	 * @param factory
	 *            The model factory
	 */
	public CommonFunction(CIVLSource source, boolean isAtomic, Identifier name,
			List<Variable> parameters, CIVLType returnType,
			Scope containingScope, int fid, Location startLocation,
			ModelFactory factory) {
		super(source);
		this.name = name;
		this.fid = fid;
		this.parameters = parameters;
		this.isAtomic = isAtomic;
		if (parameters != null) {
			int number = parameters.size();
			CIVLType[] types = new CIVLType[number];

			this.functionType = new CommonFunctionType(returnType, types,
					factory.typeFactory().functionPointerSymbolicType());
			outerScope = factory.scope(source, containingScope,
					new LinkedHashSet<>(parameters), this);
		} else {
			this.functionType = new CommonFunctionType(returnType,
					new CIVLType[0], factory.typeFactory()
							.functionPointerSymbolicType());
			outerScope = factory.scope(source, containingScope,
					new LinkedHashSet<Variable>(), this);
		}
		this.containingScope = containingScope;
		scopes = new HashSet<Scope>();
		scopes.add(outerScope);
		locations = new LinkedHashSet<Location>();
		this.startLocation = startLocation;
		if (startLocation != null) {
			locations.add(this.startLocation);
		}
		statements = new LinkedHashSet<Statement>();
	}

	/************************** Methods from CIVLFunction *************************/

	/**
	 * @param location
	 *            The new location to add.
	 */
	@Override
	public void addLocation(Location location) {
		locations.add(location);
	}

	/**
	 * @param statement
	 *            The new statement to add.
	 */
	@Override
	public void addStatement(Statement statement) {
		statements.add(statement);
	}

	/**
	 * @return The scope containing this function.
	 */
	@Override
	public Scope containingScope() {
		return containingScope;
	}

	@Override
	public boolean isRootFunction() {
		return isSystem;
	}

	/**
	 * @return The set of locations in this function.
	 */
	@Override
	public Set<Location> locations() {
		return locations;
	}

	/**
	 * @return The model to which this function belongs.
	 */
	@Override
	public Model model() {
		return model;
	}

	/**
	 * @return The name of this function.
	 */
	@Override
	public Identifier name() {
		return name;
	}

	/**
	 * @return The outermost local scope in this function.
	 */
	@Override
	public Scope outerScope() {
		return outerScope;
	}

	/**
	 * @return The list of parameters.
	 */
	@Override
	public List<Variable> parameters() {
		return parameters;
	}

	@Override
	public void print(String prefix, PrintStream out, boolean isDebug) {
		Iterator<Variable> iter;

		out.println(prefix + "function " + name);
		out.println(prefix + "| formal parameters");
		iter = parameters.iterator();
		while (iter.hasNext()) {
			out.println(prefix + "| | " + iter.next().name());
		}
		// print contracts
		if (contract != null) {
			contract.print(prefix + "| ", out, isDebug);
		}
		// print scopes
		outerScope.print(prefix + "| ", out, isDebug);
		// print locations
		if (!isRootFunction()) {
			out.println(prefix + "| locations (start=" + startLocation.id()
					+ ")");
			for (Location location : this.locations) {
				location.print(prefix + "| | ", out, isDebug);
			}
		}
		out.flush();
	}

	@Override
	public void purelyLocalAnalysis() {
		Scope funcScope = this.outerScope;

		for (Location loc : this.locations) {
			Iterable<Statement> stmts = loc.outgoing();

			for (Statement s : stmts) {
				s.purelyLocalAnalysisOfVariables(funcScope);
				// TODO functions that are never spawned are to be executed in
				// the same process as the caller
			}
		}
	}

	/**
	 * @return The return type of this function.
	 */
	@Override
	public CIVLType returnType() {
		return this.functionType.returnType();
	}

	/**
	 * @return The set of scopes in this function.
	 */
	@Override
	public Set<Scope> scopes() {
		return scopes;
	}

	/**
	 * @param containingScope
	 *            The scope containing this function.
	 */
	@Override
	public void setContainingScope(Scope containingScope) {
		this.containingScope = containingScope;
	}

	/**
	 * @param locations
	 *            The set of locations in this function.
	 */
	@Override
	public void setLocations(Set<Location> locations) {
		this.locations = locations;
	}

	/**
	 * @param model
	 *            The Model to which this function belongs.
	 */
	@Override
	public void setModel(Model model) {
		this.model = model;
	}

	/**
	 * @param name
	 *            The name of this function.
	 */
	@Override
	public void setName(Identifier name) {
		this.name = name;
	}

	/**
	 * @param outerScope
	 *            The outermost local scope of this function.
	 */
	@Override
	public void setOuterScope(Scope outerScope) {
		this.outerScope = outerScope;
	}

	/**
	 * @param parameters
	 *            The list of parameters.
	 */
	@Override
	public void setParameters(List<Variable> parameters) {
		this.parameters = parameters;
	}

	// /**
	// * @param returnType
	// * The return type of this function.
	// */
	// @Override
	// public void setReturnType(CIVLType returnType) {
	// this.returnType = returnType;
	// }

	/**
	 * @param scopes
	 *            The set of scopes in this function.
	 */
	@Override
	public void setScopes(Set<Scope> scopes) {
		this.scopes = scopes;
	}

	/**
	 * @param startLocation
	 *            The first location in this function.
	 */
	@Override
	public void setStartLocation(Location startLocation) {
		this.startLocation = startLocation;
		if (!locations.contains(startLocation)) {
			locations.add(startLocation);
		}
	}

	/**
	 * @param statements
	 *            The set of statements in this function.
	 */
	@Override
	public void setStatements(Set<Statement> statements) {
		this.statements = statements;
	}

	@Override
	public void simplify() {
		int count = locations.size();
		Location[] oldLocations = new Location[count];
		Set<Location> newLocations;
		// The index of locations that can be removed
		Set<Integer> toRemove = new LinkedHashSet<Integer>();
		Iterator<Integer> removeIter;

		this.locations.toArray(oldLocations);
		for (int i = 0; i < count; i++) {
			Location loc = oldLocations[i];

			if (loc.atomicKind() != AtomicKind.NONE)
				continue;
			// loc has exactly one outgoing statement
			if (loc.getNumOutgoing() == 1) {
				Statement s = loc.getSoleOutgoing();

				// The only statement of loc is a no-op statement
				if (s instanceof NoopStatement) {
					NoopStatement noop = (NoopStatement) s;

					if (noop.isRemovable())
						toRemove.add(i);
					// The noop is temporary or associates to a variable
					// declaration
					if (noop.isTemporary() || noop.isVariableDeclaration()) {
						Expression guard = s.guard();

						// The guard of the no-op is true
						if (guard instanceof BooleanLiteralExpression) {
							if (((BooleanLiteralExpression) guard).value()) {

								// Record the index of loc so that it can be
								// removed later
								toRemove.add(i);
								if (this.startLocation == loc) {
									this.startLocation = loc.getSoleOutgoing()
											.target();
									startLocation.setAsStart(true);
								}
							}
						}
					}
				}
			}
		}
		removeIter = toRemove.iterator();
		while (removeIter.hasNext()) {
			// the location will be removed
			Location removal = oldLocations[removeIter.next()];
			Location removalSoleTarget;
			Set<Statement> tmpIncomes = new LinkedHashSet<>();

			assert removal.getNumOutgoing() == 1;
			removalSoleTarget = removal.getSoleOutgoing().target();
			removal.getSoleOutgoing().setTarget(null);
			for (Statement income : removal.incoming())
				tmpIncomes.add(income);
			// setTarget will affect removal's incomes, so it needs a temporary
			// collection "tmpIncomes"
			for (Statement income : tmpIncomes)
				income.setTarget(removalSoleTarget);
		}
		newLocations = new LinkedHashSet<Location>();
		for (int k = 0; k < count; k++) {
			if (toRemove.contains(k))
				continue;
			newLocations.add(oldLocations[k]);
		}
		this.locations = newLocations;
	}

	/**
	 * @return The first location in this function.
	 */
	@Override
	public Location startLocation() {
		return startLocation;
	}

	/**
	 * @return The set of statements in this function.
	 */
	@Override
	public Set<Statement> statements() {
		return statements;
	}

	/* ************************ Methods from Object ************************ */

	@Override
	public String toString() {
		String result = name.name() + "(";

		for (int i = 0; i < parameters.size(); i++) {
			if (i != 0) {
				result += ",";
			}
			result += parameters.get(i);
		}
		result += ")";
		return result;
	}

	@Override
	public Set<Variable> variableAddressedOf(Scope scope) {
		Set<Variable> result = new HashSet<>();

		for (Statement statement : this.statements) {
			Set<Variable> subResult = statement.variableAddressedOf(scope);

			if (subResult != null)
				result.addAll(subResult);
		}
		return result;
	}

	@Override
	public Set<Variable> variableAddressedOf() {
		Set<Variable> result = new HashSet<>();

		for (Statement statement : this.statements) {
			// System.out.println(statement + " " +
			// statement.summaryOfSource());
			Set<Variable> subResult = statement.variableAddressedOf();

			if (subResult != null)
				result.addAll(subResult);
		}
		return result;
	}

	@Override
	public CIVLFunctionType functionType() {
		return this.functionType;
	}

	@Override
	public boolean isSystemFunction() {
		return false;
	}

	@Override
	public boolean isAbstractFunction() {
		return false;
	}

	@Override
	public boolean isNormalFunction() {
		return true;
	}

	@Override
	public void setReturnType(CIVLType returnType) {
		this.functionType.setReturnType(returnType);
	}

	@Override
	public void setParameterTypes(CIVLType[] types) {
		this.functionType.setParameterTypes(types);
	}

	@Override
	public int fid() {
		return this.fid;
	}

	@Override
	public StringBuffer unreachedCode() {
		StringBuffer result = new StringBuffer("");

		for (Location location : locations) {
			for (Statement stmt : location.outgoing()) {
				if (!(stmt instanceof NoopStatement) && !stmt.reachable()) {
					CIVLSource source = stmt.getSource();

					result.append(source.getContent());
					result.append("\n");
				}
			}
		}
		return result;
	}

	@Override
	public FunctionContract functionContract() {
		return this.contract;
	}

	@Override
	public void setFunctionContract(FunctionContract contract) {
		this.contract = contract;
	}

	@Override
	public void addPossibleValidConsequence(
			Pair<Expression, Integer> validConsequences) {
		if (possibleConsequences == null)
			possibleConsequences = new LinkedList<>();
		possibleConsequences.add(validConsequences);
	}

	@Override
	public List<Pair<Expression, Integer>> getPossibleValidConsequences() {
		if (possibleConsequences == null)
			possibleConsequences = new LinkedList<>();
		return possibleConsequences;
	}

	@Override
	public boolean isContracted() {
		return contract != null
				&& (contract.hasRequirementsOrEnsurances() || contract
						.numMPICollectiveBehaviors() > 0);
	}

	@Override
	public boolean isAtomicFunction() {
		return this.isAtomic;
	}

	@SuppressWarnings("unused")
	@Override
	public void computePathconditionOfLocations(ModelFactory factory) {
		Expression context = null;
		Stack<Location> working = new Stack<>();
		Set<Integer> visited = new HashSet<>();
		Location current;

		working.add(startLocation);
		startLocation.setPathcondition(factory.trueExpression(startLocation
				.getSource()));
		while (!working.isEmpty()) {
			int numIncoming;

			current = working.pop();
			visited.add(current.id());
			for (Statement outgoing : current.outgoing()) {
				Expression guard = outgoing.guard();
				Location target = outgoing.target();
				CIVLSource source = target.getSource();
				int tid = target.id();

				target.setPathcondition(factory.binaryExpression(source,
						BINARY_OPERATOR.OR, target.pathCondition(), guard));
				// if(visited.contains(target).)
			}
		}

	}
}
