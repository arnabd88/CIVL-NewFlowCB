package edu.udel.cis.vsl.civl.transform.common;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import edu.udel.cis.vsl.abc.ast.IF.AST;
import edu.udel.cis.vsl.abc.ast.IF.ASTFactory;
import edu.udel.cis.vsl.abc.ast.node.IF.ASTNode;
import edu.udel.cis.vsl.abc.ast.node.IF.ASTNode.NodeKind;
import edu.udel.cis.vsl.abc.ast.node.IF.ExternalDefinitionNode;
import edu.udel.cis.vsl.abc.ast.node.IF.IdentifierNode;
import edu.udel.cis.vsl.abc.ast.node.IF.SequenceNode;
import edu.udel.cis.vsl.abc.ast.node.IF.declaration.FunctionDefinitionNode;
import edu.udel.cis.vsl.abc.ast.node.IF.declaration.VariableDeclarationNode;
import edu.udel.cis.vsl.abc.ast.node.IF.expression.ExpressionNode;
import edu.udel.cis.vsl.abc.ast.node.IF.expression.ExpressionNode.ExpressionKind;
import edu.udel.cis.vsl.abc.ast.node.IF.expression.FunctionCallNode;
import edu.udel.cis.vsl.abc.ast.node.IF.expression.IdentifierExpressionNode;
import edu.udel.cis.vsl.abc.ast.node.IF.expression.OperatorNode.Operator;
import edu.udel.cis.vsl.abc.ast.node.IF.statement.AssumeNode;
import edu.udel.cis.vsl.abc.ast.node.IF.statement.BlockItemNode;
import edu.udel.cis.vsl.abc.ast.node.IF.statement.CompoundStatementNode;
import edu.udel.cis.vsl.abc.ast.node.IF.statement.ExpressionStatementNode;
import edu.udel.cis.vsl.abc.ast.node.IF.statement.ForLoopInitializerNode;
import edu.udel.cis.vsl.abc.ast.node.IF.statement.ForLoopNode;
import edu.udel.cis.vsl.abc.ast.node.IF.statement.StatementNode;
import edu.udel.cis.vsl.abc.ast.node.IF.type.FunctionTypeNode;
import edu.udel.cis.vsl.abc.ast.node.IF.type.TypeNode;
import edu.udel.cis.vsl.abc.ast.type.IF.StandardBasicType.BasicTypeKind;
import edu.udel.cis.vsl.abc.parse.IF.CParser;
import edu.udel.cis.vsl.abc.token.IF.SyntaxException;
import edu.udel.cis.vsl.civl.util.IF.Triple;

//TODO: translate exit(k) to return k;
/**
 * MPI2CIVLTransformer transforms an AST of an MPI program into an AST of an
 * equivalent CIVL-C program. See {@linkplain #transform(AST)}. TODO: copy
 * output files only for the mpi process with rank 0.
 * 
 * @author Manchun Zheng (zmanchun)
 * 
 */
public class MPI2CIVLWorker extends BaseWorker {

	/* ************************** Private Static Fields ********************** */

	// private static String EXIT = "exit";

	/**
	 * The name of the identifier of the MPI_Comm variable in the final CIVL
	 * program.
	 */
	private static String COMM_WORLD = "MPI_COMM_WORLD";

	private static String PTHREAD_POOL = "_pool";

	private static String PTHREAD_ADD_THREAD = "_add_thread";

	private static String PTHREAD_CREATE = "pthread_create";

	private static String PTHREAD_EXIT = "_pthread_exit";

	private static String PTHREAD_IS_TERMINATED = "_isTerminated";

	/**
	 * The name of the identifier of the CMPI_Gcomm variable in the final CIVL
	 * program.
	 */
	private static String GCOMM_WORLD = "GCOMM_WORLD";

	/**
	 * The name of CMPI_Gcomm type in the final CIVL-C program.
	 */
	private static String GCOMM_TYPE = "CMPI_Gcomm";

	/**
	 * The name of MPI_Comm type in both the original program and the final
	 * CIVL-C program.
	 */
	private static String COMM_TYPE = "MPI_Comm";

	/**
	 * The name of the function to create a new CMPI_Gcomm object in the final
	 * CIVL-C program.
	 */
	private static String GCOMM_CREATE = "CMPI_Gcomm_create";

	/**
	 * The name of the function to create a new MPI_Comm object in the final
	 * CIVL-C program.
	 */
	private static String COMM_CREATE = "CMPI_Comm_create";

	/**
	 * The name of the function to free a CMPI_Gcomm object in the final CIVL-C
	 * program.
	 */
	private static String GCOMM_DESTROY = "CMPI_Gcomm_destroy";

	/**
	 * The name of the function to free a MPI_Comm object in the final CIVL-C
	 * program.
	 */
	private static String COMM_DESTROY = "CMPI_Comm_destroy";

	/**
	 * The name used for renaming the main function of the original MPI program.
	 */
	private static String MPI_MAIN = "__main";

	/**
	 * The name of the parameter of a MPI procedure.
	 */
	private static String MPI_RANK = "__rank";

	/**
	 * The name of the function MPI_Init in the original MPI program.
	 */
	private static String MPI_INIT = "MPI_Init";

	/**
	 * The name of the function translating MPI_Init in the final CIVL-C
	 * program.
	 */
	private static String MPI_INIT_NEW = "__MPI_Init";

	/**
	 * The name of the function MPI_Init in the original MPI program.
	 */
	private static String MPI_FINALIZE = "MPI_Finalize";

	/**
	 * The name of the function translating MPI_Init in the final CIVL-C
	 * program.
	 */
	private static String MPI_FINALIZE_NEW = "__MPI_Finalize";

	/**
	 * The name of the variable representing the status of an MPI process, which
	 * is modified by MPI_Init() and MPI_Finalized().
	 */
	private static String MPI_STATUS = "__my_status";

	/**
	 * The name of the MPI procedure in the final CIVL-C program.
	 */
	private static String MPI_PROCESS = "MPI_Process";

	/**
	 * The name of the input variable denoting the number of MPI processes in
	 * the final CIVL-C program.
	 */
	private static String NPROCS = "__NPROCS";

	/**
	 * The name of the input variable denoting the upper bound of the number of
	 * MPI processes in the final CIVL-C program.
	 */
	private static String NPROCS_UPPER_BOUND = "__NPROCS_UPPER_BOUND";

	/**
	 * The name of the input variable denoting the lower bound of the number of
	 * MPI processes in the final CIVL-C program.
	 */
	private static String NPROCS_LOWER_BOUND = "__NPROCS_LOWER_BOUND";

	/**
	 * The name of the variable used to declare the array of references to all
	 * MPI processes in the final CIVL-C program.
	 */
	private static String PROCS = "__procs";

	/**
	 * The name of the function $wait() in the final CIVL-C program.
	 */
	private static String WAIT = "$wait";

	/**
	 * The name of $proc type in the final CIVL-C program.
	 */
	private static String PROC_TYPE = "$proc";

	/* ****************************** Constructor ************************** */
	/**
	 * Creates a new instance of MPITransformer.
	 * 
	 * @param astFactory
	 *            The ASTFactory that will be used to create new nodes.
	 */
	public MPI2CIVLWorker(ASTFactory astFactory) {
		super("MPItoCIVLTransformer", astFactory);
	}

	/* *************************** Private Methods ************************* */

	/**
	 * Creates a bound assumption node in the form of:
	 * <code>$assume lowerBound < variable && variable <= upperBound</code>.
	 * 
	 * @param lowerBound
	 *            The lower bound of the variable.
	 * @param variable
	 *            The variable to be bounded.
	 * @param upperBound
	 *            The upper bound of the variable.
	 * @return The node representing of the assumption on the bound of the
	 *         variable.
	 * @throws SyntaxException
	 */
	private AssumeNode boundAssumption(String lowerBound, String variable,
			String upperBound) throws SyntaxException {
		ExpressionNode variableExpression = this.identifierExpression(variable);
		ExpressionNode upperBoundExpression = this
				.identifierExpression(upperBound);
		ExpressionNode lowerBoundExpression = this
				.identifierExpression(lowerBound);
		ExpressionNode lowerPart, upperPart;

		lowerPart = nodeFactory.newOperatorNode(this.newSource(
				"lower bound of variable " + variable, CParser.EXPR),
				Operator.LT, Arrays.asList(lowerBoundExpression,
						variableExpression));
		variableExpression = variableExpression.copy();
		upperPart = nodeFactory.newOperatorNode(this.newSource(
				"upper bound of variable " + variable, CParser.EXPR),
				Operator.LTE, Arrays.asList(variableExpression,
						upperBoundExpression));
		return nodeFactory.newAssumeNode(this.newSource(
				"assumption on the bound of variable " + variable,
				CParser.ASSUME), nodeFactory.newOperatorNode(
				this.newSource("logical and ", CParser.EXPR), Operator.LAND,
				Arrays.asList(lowerPart, upperPart)));
	}

	/**
	 * Creates the declaration node for the variable <code>MPI_COMM_WORLD</code>
	 * , which is of <code>MPI_Comm</code> type and has an initializer to call
	 * <code>$comm_create()</code>. That is:
	 * <code>MPI_Comm MPI_COMM_WORLD = $comm_create($here, GCOMM_WORLD, _rank)</code>
	 * .
	 * 
	 * @return The declaration node of the variable <code>MPI_COMM_WORLD</code>.
	 */
	private VariableDeclarationNode commDeclaration() {
		TypeNode commType;
		List<ExpressionNode> commCreateArgs;
		ExpressionNode commCreate;

		commType = nodeFactory.newTypedefNameNode(nodeFactory
				.newIdentifierNode(
						this.newSource("$comm type", CParser.IDENTIFIER),
						COMM_TYPE), null);
		commCreateArgs = new ArrayList<>(3);
		commCreateArgs.add(this.hereNode());
		commCreateArgs.add(this.identifierExpression(GCOMM_WORLD));
		commCreateArgs.add(this.identifierExpression(MPI_RANK));
		commCreate = nodeFactory.newFunctionCallNode(
				this.newSource("function call " + COMM_CREATE, CParser.CALL),
				this.identifierExpression(COMM_CREATE), commCreateArgs, null);
		return this.variableDeclaration(COMM_WORLD, commType, commCreate);
	}

	/**
	 * Creates an expression statement node of a function call node for
	 * destroying a communicator, either global or local.
	 * 
	 * @param destroy
	 *            The name of the function call, either
	 *            <code>$gcomm_destroy</code> or <code>$comm_destroy</code>.
	 * @param commName
	 *            The name of the variable of the communicator to be destroy,
	 *            either <code>GCOMM_WORLD</code> or <code>MPI_COMM_WORLD</code>
	 *            .
	 * @return The expression statement node of the function call for destroying
	 *         the specified communicator.
	 */
	private ExpressionStatementNode commDestroy(String destroy, String commName) {
		ExpressionNode function = this.identifierExpression(destroy);

		return nodeFactory.newExpressionStatementNode(nodeFactory
				.newFunctionCallNode(this.newSource("function call " + destroy,
						CParser.CALL), function, Arrays.asList(this
						.identifierExpression(commName)), null));
	}

	/**
	 * Creates the declaration node for the variable <code>GCOMM_WORLD</code> ,
	 * which is of <code>$gcomm</code> type and has an initializer to call
	 * <code>$gcomm_create()</code>. That is:
	 * <code>$gcomm GCOMM_WORLD = $gcomm_create($here, NPROCS)</code> .
	 * 
	 * @return The declaration node of the variable <code>GCOMM_WORLD</code>.
	 */
	private VariableDeclarationNode gcommDeclaration() {
		TypeNode gcommType;
		ExpressionNode gcommCreate;

		gcommType = nodeFactory.newTypedefNameNode(this.identifier(GCOMM_TYPE),
				null);
		gcommCreate = nodeFactory.newFunctionCallNode(
				this.newSource("function call " + GCOMM_CREATE, CParser.CALL),
				this.identifierExpression(GCOMM_CREATE),
				Arrays.asList(this.hereNode(),
						this.identifierExpression(NPROCS)), null);
		return this.variableDeclaration(GCOMM_WORLD, gcommType, gcommCreate);
	}

	/**
	 * Creates the main function for the final program, which is: <br>
	 * 
	 * <pre>
	 * void main(){
	 *   $proc procs[NPROCS]; 
	 *   for(int i = 0; i < NPROCS; i++)
	 *     procs[i] = $spawn MPI_Process(i);
	 *   for(int i = 0; i < NPROCS; i++)
	 *     $wait(procs[i]);
	 *   $gcomm_destroy(GCOMM_WORLD);
	 * }
	 * </pre>
	 * 
	 * @return The function definition node representing the main function of
	 *         the final program.
	 * @throws SyntaxException
	 */
	private FunctionDefinitionNode mainFunction() throws SyntaxException {
		List<BlockItemNode> items = new LinkedList<BlockItemNode>();
		TypeNode procsType;
		VariableDeclarationNode procsVar;
		ForLoopInitializerNode initializerNode;
		ExpressionNode loopCondition, incrementer, spawnProc, waitProc, leftHandSide;
		StatementNode assign;
		ForLoopNode forLoop;
		CompoundStatementNode mainBody;
		SequenceNode<VariableDeclarationNode> formals;
		FunctionTypeNode mainType;
		FunctionDefinitionNode mainFunction;
		ExpressionStatementNode gcommDestroy = this.commDestroy(GCOMM_DESTROY,
				GCOMM_WORLD);

		// declaring $proc procs[NPROCS];
		procsType = nodeFactory.newArrayTypeNode(this.newSource(
				"type array-of-$proc", CParser.TYPE), nodeFactory
				.newTypedefNameNode(this.identifier(PROC_TYPE), null), this
				.identifierExpression(NPROCS));
		procsVar = this.variableDeclaration(PROCS, procsType);
		items.add(procsVar);
		// first for loop;
		initializerNode = nodeFactory
				.newForLoopInitializerNode(
						newSource("for loop initializer",
								CParser.INIT_DECLARATOR_LIST), Arrays
								.asList(this.variableDeclaration("i",
										this.basicType(BasicTypeKind.INT),
										this.integerConstant(0))));
		loopCondition = nodeFactory.newOperatorNode(
				this.newSource("less-than expression", CParser.EXPR),
				Operator.LT,
				Arrays.asList(this.identifierExpression("i"),
						this.identifierExpression(NPROCS)));
		incrementer = nodeFactory.newOperatorNode(
				this.newSource("post increment", CParser.POST_INCREMENT),
				Operator.POSTINCREMENT,
				Arrays.asList(this.identifierExpression("i")));
		spawnProc = nodeFactory.newSpawnNode(this.newSource("process spawn "
				+ MPI_PROCESS, CParser.SPAWN), nodeFactory.newFunctionCallNode(
				this.newSource("function call " + MPI_PROCESS, CParser.CALL),
				this.identifierExpression(MPI_PROCESS),
				Arrays.asList(this.identifierExpression("i")), null));
		leftHandSide = nodeFactory.newOperatorNode(
				this.newSource("subscript expression", CParser.SUB),
				Operator.SUBSCRIPT,
				Arrays.asList(this.identifierExpression(PROCS),
						this.identifierExpression("i")));
		assign = nodeFactory
				.newExpressionStatementNode(nodeFactory.newOperatorNode(
						this.newSource("assign expression", CParser.ASSIGN),
						Operator.ASSIGN, Arrays.asList(leftHandSide, spawnProc)));
		forLoop = nodeFactory.newForLoopNode(
				this.newSource("for loop", CParser.FOR), initializerNode,
				loopCondition, incrementer, assign, null);
		items.add(forLoop);
		// second for loop;
		initializerNode = nodeFactory.newForLoopInitializerNode(this.newSource(
				"for loop initializer", CParser.INIT_DECLARATOR_LIST), Arrays
				.asList(this.variableDeclaration("i",
						this.basicType(BasicTypeKind.INT),
						this.integerConstant(0))));
		loopCondition = nodeFactory.newOperatorNode(
				this.newSource("less-than expression", CParser.EXPR),
				Operator.LT,
				Arrays.asList(this.identifierExpression("i"),
						this.identifierExpression(NPROCS)));
		incrementer = nodeFactory.newOperatorNode(this.newSource(
				"post increment expression", CParser.POST_INCREMENT),
				Operator.POSTINCREMENT, Arrays.asList(this
						.identifierExpression("i")));
		waitProc = nodeFactory.newFunctionCallNode(this.newSource(
				"function call" + WAIT, CParser.CALL), this
				.identifierExpression(WAIT), Arrays
				.asList((ExpressionNode) nodeFactory.newOperatorNode(
						this.newSource("subscript expression", CParser.SUB),
						Operator.SUBSCRIPT,
						Arrays.asList(this.identifierExpression(PROCS),
								this.identifierExpression("i")))), null);
		forLoop = nodeFactory.newForLoopNode(
				this.newSource("for loop", CParser.FOR), initializerNode,
				loopCondition, incrementer,
				nodeFactory.newExpressionStatementNode(waitProc), null);
		items.add(forLoop);
		// destroying GCOMM_WROLD;
		items.add(gcommDestroy);
		// constructing the function definition node.
		mainBody = nodeFactory.newCompoundStatementNode(
				this.newSource("main body", CParser.COMPOUND_STATEMENT), items);
		formals = nodeFactory.newSequenceNode(this.newSource(
				"formal parameter of the declaration of the main function",
				CParser.DECLARATION_LIST), "FormalParameterDeclarations",
				new ArrayList<VariableDeclarationNode>());
		mainType = nodeFactory.newFunctionTypeNode(
				this.newSource("type of the main function", CParser.TYPE),
				this.voidType(), formals, true);
		mainFunction = nodeFactory.newFunctionDefinitionNode(
				this.newSource("definition of the main function",
						CParser.FUNCTION_DEFINITION), this.identifier("main"),
				mainType, null, mainBody);
		return mainFunction;
	}

	/**
	 * 
	 * Constructs the function MPI_Process() from the original MPI program. It
	 * is a wrapper of the original MPI program with some additional features: <br>
	 * 
	 * <pre>
	 * void MPI_Process(){
	 *   $comm MPI_COMM_WORLD = $comm_create(...);
	 *   //SLIGHTLY-MODIFIED ORIGINAL PROGRAM;
	 *   int a, b, ...;
	 *   ... function(){...}
	 *   ...
	 *   ... __main(){...} // renamed main() to __main()
	 *   ....
	 *   //ORIGINAL PROGRAM ENDS HERE;
	 *   __main();
	 *   $comm_destroy(MPI_COMM_WORLD);
	 * }
	 * </pre>
	 * 
	 * @param root
	 *            The root node of the AST of the original MPI program.
	 * @return The function definition node of MPI_Process, the list of AST
	 *         nodes that are parsed from header files and will be moved up to
	 *         the higher scope (i.e., the file scope of the final AST), and the
	 *         list of variable declaration nodes that are the arguments of the
	 *         original main function which will be moved up to the higher scope
	 *         (i.e., the file scope of the final AST) and become $input
	 *         variables of the final AST.
	 */
	private Triple<FunctionDefinitionNode, List<ExternalDefinitionNode>, List<VariableDeclarationNode>> mpiProcess(
			SequenceNode<ExternalDefinitionNode> root) {
		List<ExternalDefinitionNode> includedNodes = new ArrayList<>();
		List<VariableDeclarationNode> vars = new ArrayList<>();
		List<BlockItemNode> items;
		int number;
		ExpressionStatementNode callMain;
		CompoundStatementNode mpiProcessBody;
		SequenceNode<VariableDeclarationNode> formals;
		FunctionTypeNode mpiProcessType;
		FunctionDefinitionNode mpiProcess;
		VariableDeclarationNode commVar = this.commDeclaration();
		ExpressionStatementNode commDestroy = this.commDestroy(COMM_DESTROY,
				COMM_WORLD);

		callMain = nodeFactory.newExpressionStatementNode(nodeFactory
				.newFunctionCallNode(this.newSource(
						"function call " + MPI_MAIN, CParser.CALL), this
						.identifierExpression(MPI_MAIN),
						new ArrayList<ExpressionNode>(), null));
		// build MPI_Process() function:
		items = new LinkedList<>();
		number = root.numChildren();
		items.add(commVar);
		for (int i = 0; i < number; i++) {
			ExternalDefinitionNode child = root.getSequenceChild(i);
			String sourceFile;

			if (child == null)
				continue;
			sourceFile = child.getSource().getFirstToken().getSourceFile()
					.getName();
			root.removeChild(i);
			if (sourceFile.equals("mpi.cvl")) {
				if (child.nodeKind() == NodeKind.VARIABLE_DECLARATION) {
					VariableDeclarationNode variableDeclaration = (VariableDeclarationNode) child;

					if (variableDeclaration.getName().equals(MPI_STATUS))
						// keep variable declaration node of __MPI_Status__
						// __my_status = __UNINIT;
						items.add(variableDeclaration);
					else
						includedNodes.add(child);
				} else
					includedNodes.add(child);
			} else if (sourceFile.endsWith(".cvh")
					|| sourceFile.equals("comm.cvl")
					|| sourceFile.equals("civlmpi.cvl")
					|| sourceFile.equals("mpi.cvl")
					|| sourceFile.equals("civlc.cvl")
					|| sourceFile.equals("concurrency.cvl")
					|| sourceFile.equals("stdio.cvl")
					|| sourceFile.equals("pthread.cvl")
					|| sourceFile.equals("string.cvl"))
				includedNodes.add(child);
			else if (sourceFile.equals("pthread-c.cvl")) {
				if (child.nodeKind() == NodeKind.VARIABLE_DECLARATION) {
					VariableDeclarationNode variableDeclaration = (VariableDeclarationNode) child;
					String varName = variableDeclaration.getName();

					if (varName.equals(PTHREAD_POOL))
						// keep variable declaration nodes for _pool in
						// pthread.cvl
						items.add(variableDeclaration);
					else
						includedNodes.add(child);
				} else if (child.nodeKind() == NodeKind.FUNCTION_DEFINITION) {
					FunctionDefinitionNode functionDef = (FunctionDefinitionNode) child;
					String name = functionDef.getName();

					if (name.equals(PTHREAD_ADD_THREAD)
							|| name.equals(PTHREAD_IS_TERMINATED)
							|| name.equals(PTHREAD_CREATE)
							|| name.equals(PTHREAD_EXIT))
						items.add(functionDef);
					else
						includedNodes.add(child);
				} else
					includedNodes.add(child);
			} else if (sourceFile.endsWith(".h")) {
				if (child.nodeKind() == NodeKind.VARIABLE_DECLARATION) {
					VariableDeclarationNode variableDeclaration = (VariableDeclarationNode) child;

					if (sourceFile.equals("stdio.h"))
						// keep variable declaration nodes from stdio, i.e.,
						// stdout, stdin, etc.
						items.add(variableDeclaration);
					else if (!variableDeclaration.getName().equals(COMM_WORLD))
						// ignore the MPI_COMM_WORLD declaration in mpi.h.
						includedNodes.add(child);
				} else
					includedNodes.add(child);
			} else {
				if (child.nodeKind() == NodeKind.VARIABLE_DECLARATION) {
					VariableDeclarationNode variable = (VariableDeclarationNode) child;

					if (variable.getTypeNode().isInputQualified()
							|| variable.getTypeNode().isOutputQualified()) {
						vars.add(variable);
						continue;
					}
				}
				if (child.nodeKind() == NodeKind.FUNCTION_DEFINITION) {
					FunctionDefinitionNode functionNode = (FunctionDefinitionNode) child;
					IdentifierNode functionName = (IdentifierNode) functionNode
							.child(0);

					if (functionName.name().equals("main"))
						functionName.setName(MPI_MAIN);
				}
				items.add((BlockItemNode) child);
			}
		}
		items.add(callMain);
		items.add(commDestroy);
		mpiProcessBody = nodeFactory.newCompoundStatementNode(root.getSource(),
				items);
		formals = nodeFactory.newSequenceNode(this.newSource(
				"formal parameters of function " + MPI_PROCESS,
				CParser.DECLARATION_LIST), "FormalParameterDeclarations",
				Arrays.asList(this.variableDeclaration(MPI_RANK,
						this.basicType(BasicTypeKind.INT))));
		mpiProcessType = nodeFactory
				.newFunctionTypeNode(this.newSource("type of function "
						+ MPI_PROCESS, CParser.TYPE), this.voidType(), formals,
						true);
		mpiProcess = nodeFactory.newFunctionDefinitionNode(this.newSource(
				"definition of function", CParser.FUNCTION_DEFINITION), this
				.identifier(MPI_PROCESS), mpiProcessType, null, mpiProcessBody);
		return new Triple<>(mpiProcess, includedNodes, vars);
	}

	/**
	 * Declare a variable of a basic type with a specific name.
	 * 
	 * @param type
	 *            The kind of basic type.
	 * @param name
	 *            The name of the variable.
	 * @return The variable declaration node.
	 */
	private VariableDeclarationNode basicTypeVariableDeclaration(
			BasicTypeKind type, String name) {
		TypeNode typeNode = this.basicType(type);

		return this.variableDeclaration(name, typeNode);
	}

	/**
	 * Creates the declaration node for the input variable <code>NPROCS</code>.
	 * 
	 * @return The declaration node of the input variable <code>NPROCS</code>.
	 */
	private VariableDeclarationNode nprocsDeclaration() {
		TypeNode nprocsType = this.basicType(BasicTypeKind.INT);

		nprocsType.setInputQualified(true);
		return this.variableDeclaration(NPROCS, nprocsType);
	}

	/**
	 * Scans all children nodes to do preprocessing. Currently, only one kind of
	 * processing is performed, i.e., translating all <code>MPI_Init(...)</code>
	 * function call into <code>__MPI_Init()</code>.
	 * 
	 * @param node
	 *            The AST node to be checked and all its children will be
	 *            scanned.
	 * @throws SyntaxException
	 */
	private void preprocessASTNode(ASTNode node) throws SyntaxException {
		int numChildren = node.numChildren();

		for (int i = 0; i < numChildren; i++) {
			ASTNode child = node.child(i);

			if (child != null)
				this.preprocessASTNode(node.child(i));
		}
		if (node instanceof FunctionCallNode) {
			this.preprocessFunctionCall((FunctionCallNode) node);
		}

	}

	/**
	 * 
	 * Translates an <code>MPI_Init(...)</code> function call into
	 * <code>__MPI_Init(MPI_COMM_WORLD)</code>.
	 * 
	 * 
	 * @param functionCall
	 */
	private void preprocessFunctionCall(FunctionCallNode functionCall) {
		if (functionCall.getFunction().expressionKind() == ExpressionKind.IDENTIFIER_EXPRESSION) {
			IdentifierExpressionNode functionExpression = (IdentifierExpressionNode) functionCall
					.getFunction();
			String functionName = functionExpression.getIdentifier().name();

			if (functionName.equals(MPI_INIT)) {
				ExpressionNode addressOf = nodeFactory.newOperatorNode(
						this.newSource("address-of expression", CParser.EXPR),
						Operator.ADDRESSOF,
						Arrays.asList(this.identifierExpression(COMM_WORLD)));

				functionExpression.getIdentifier().setName(MPI_INIT_NEW);
				functionCall.setArguments(nodeFactory.newSequenceNode(
						this.newSource("actual parameter list of "
								+ MPI_INIT_NEW, CParser.ARGUMENT_LIST),
						"ActualParameterList", Arrays.asList(addressOf)));
			} else if (functionName.equals(MPI_FINALIZE)) {
				ExpressionNode addressOf = nodeFactory.newOperatorNode(
						this.newSource("address-of expression", CParser.EXPR),
						Operator.ADDRESSOF,
						Arrays.asList(this.identifierExpression(COMM_WORLD)));

				functionExpression.getIdentifier().setName(MPI_FINALIZE_NEW);
				functionCall.setArguments(nodeFactory.newSequenceNode(
						this.newSource("actual parameter list of "
								+ MPI_FINALIZE_NEW, CParser.ARGUMENT_LIST),
						"ActualParameterList", Arrays.asList(addressOf)));
			}
			// else if (functionName.equals(EXIT)) {
			// int myIndex = functionCall.parent().childIndex();
			// ExpressionNode value = functionCall.getArgument(0).copy();
			// ReturnNode returnNode = nodeFactory
			// .newReturnNode(source, value);
			// ASTNode parent = functionCall.parent().parent();
			//
			// parent.setChild(myIndex, returnNode);
			// }
		}
	}

	/**
	 * Creates the assumption node for NPROCS.
	 * 
	 * @return the assumption node of NPROCS, null if the input variable list
	 *         already contains NPROCS.
	 * @throws SyntaxException
	 */
	private AssumeNode nprocsAssumption() throws SyntaxException {
		// if (this.inputVariableNames.contains(NPROCS))
		// return null;
		// if (this.inputVariableNames.contains(NPROCS_LOWER_BOUND))
		return this.boundAssumption(NPROCS_LOWER_BOUND, NPROCS,
				NPROCS_UPPER_BOUND);
		// return upperBoundAssumption(NPROCS, NPROCS_UPPER_BOUND);
	}

	/* ********************* Methods From BaseTransformer ****************** */

	/**
	 * Transform an AST of a pure MPI program in C into an equivalent AST of
	 * CIVL-C program.<br>
	 * Given an MPI program:<br>
	 * 
	 * <pre>
	 * #include &lt;mpi.h>
	 * ...
	 * #include &lt;stdio.h>
	 * int a, b, ...;
	 * ... function(){
	 *   ...
	 * }
	 * ...
	 * int main(){
	 *   ....
	 * }
	 * </pre>
	 * 
	 * It is translated to the following program:<br>
	 * 
	 * <pre>
	 * #include &lt;mpi.h> // all included files are moved above to the new file scope.
	 * ...
	 * #include &lt;stdio.h>
	 * $input int argc;//optional, only necessary when the original main function has arguments.
	 * $input char** argv;//optional, only necessary when the original main function has arguments.
	 * $input int NPROCS;
	 * $gcomm GCOMM_WORLD = $gcomm_create($here, NPROCS);
	 * 
	 * void MPI_Process(int _rank){
	 *   ...
	 * }
	 * void main(){
	 *   $proc procs[NPROCS];
	 *   for(int i = 0; i < NPROCS; i++)
	 *     procs[i] = $spawn MPI_Process(i);
	 *   for(int i = 0; i < NPROCS; i++)
	 *     $wait(procs[i]);
	 *   $gcomm_destroy(GCOMM_WORLD);
	 * }
	 * </pre>
	 * 
	 * Whereas MPI_Process() is a wrapper of the original MPI program with some
	 * special handling:<br>
	 * 
	 * <pre>
	 * void MPI_Process(){
	 *   $comm MPI_COMM_WORLD = $comm_create(...);
	 *   //SLIGHTLY-MODIFIED ORIGINAL PROGRAM;
	 *   int a, b, ...;
	 *   ... function(){...}
	 *   ...
	 *   ... __main(){...} // renamed main() to __main()
	 *   ....
	 *   //ORIGINAL PROGRAM ENDS HERE;
	 *   __main();
	 *   $comm_destroy(MPI_COMM_WORLD);
	 * }
	 * </pre>
	 * 
	 * @param ast
	 *            The AST of the original MPI program in C.
	 * @return An AST of CIVL-C program equivalent to the original MPI program.
	 * @throws SyntaxException
	 */
	@Override
	public AST transform(AST ast) throws SyntaxException {
		SequenceNode<ExternalDefinitionNode> root = ast.getRootNode();
		AST newAst;
		FunctionDefinitionNode mpiProcess, mainFunction;
		VariableDeclarationNode gcommWorld;
		List<ExternalDefinitionNode> externalList;
		VariableDeclarationNode nprocsVar;
		VariableDeclarationNode nprocsUpperBoundVar = null, nprocsLowerBoundVar = null;
		SequenceNode<ExternalDefinitionNode> newRootNode;
		List<ExternalDefinitionNode> includedNodes = new ArrayList<>();
		List<VariableDeclarationNode> mainParameters = new ArrayList<>();
		int count;
		AssumeNode nprocsAssumption = null;
		Triple<FunctionDefinitionNode, List<ExternalDefinitionNode>, List<VariableDeclarationNode>> result;

		assert this.astFactory == ast.getASTFactory();
		assert this.nodeFactory == astFactory.getNodeFactory();
		ast.release();
		// change MPI_Init(...) to _MPI_Init();
		preprocessASTNode(root);
		// declaring $input int NPROCS;
		nprocsVar = this.nprocsDeclaration();
		// declaring $input int NPROCS_UPPER_BOUND;
		// if (!this.inputVariableNames.contains(NPROCS)
		// && this.inputVariableNames.contains(NPROCS_UPPER_BOUND)) {
		nprocsUpperBoundVar = this.basicTypeVariableDeclaration(
				BasicTypeKind.INT, NPROCS_UPPER_BOUND);
		nprocsUpperBoundVar.getTypeNode().setInputQualified(true);
		// }
		// if (!this.inputVariableNames.contains(NPROCS)
		// && this.inputVariableNames.contains(NPROCS_LOWER_BOUND)) {
		// declaring $input int NPROCS_LOWER_BOUND;
		nprocsLowerBoundVar = this.basicTypeVariableDeclaration(
				BasicTypeKind.INT, NPROCS_LOWER_BOUND);
		nprocsLowerBoundVar.getTypeNode().setInputQualified(true);
		// }
		// if (!this.inputVariableNames.contains(NPROCS)
		// && !this.inputVariableNames.contains(NPROCS_UPPER_BOUND)) {
		// throw new SyntaxException(
		// "Please specify the number of processes (e.g., -input__NPROCS=5)"
		// +
		// "or the upper bound of number of processes (e.g. -input__NPROCS_UPPER_BOUND=6)",
		// source);// TODO improve messages with pragma.
		// }
		// assuming NPROCS_LOWER_BOUND < NPROCS && NPROCS <= NPROCS_UPPER_BOUND
		nprocsAssumption = this.nprocsAssumption();
		// declaring $gcomm GCOMM_WORLD = $gcomm_create($here, NPROCS);
		gcommWorld = this.gcommDeclaration();
		result = this.mpiProcess(root);
		mpiProcess = result.first;
		includedNodes = result.second;
		mainParameters = result.third;
		// defining the main function;
		mainFunction = mainFunction();
		// the translated program is:
		// input variables;
		// gcomm
		// MPI_Process() definition;
		// main function.
		externalList = new LinkedList<>();
		count = includedNodes.size();
		// adding nodes from header files.
		for (int i = 0; i < count; i++) {
			externalList.add(includedNodes.get(i));
		}
		count = mainParameters.size();
		// adding nodes from the arguments of the original main function.
		for (int i = 0; i < count; i++) {
			externalList.add(mainParameters.get(i));
		}
		externalList.add(nprocsVar);
		if (nprocsLowerBoundVar != null)
			externalList.add(nprocsLowerBoundVar);
		if (nprocsUpperBoundVar != null)
			externalList.add(nprocsUpperBoundVar);
		if (nprocsAssumption != null)
			externalList.add(nprocsAssumption);
		externalList.add(gcommWorld);
		externalList.add(mpiProcess);
		externalList.add(mainFunction);
		newRootNode = nodeFactory.newSequenceNode(null, "TranslationUnit",
				externalList);
		newAst = astFactory.newAST(newRootNode, ast.getSourceFiles());
		return newAst;
	}

}
