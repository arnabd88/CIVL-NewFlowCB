package edu.udel.cis.vsl.civl.transform.common;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;

import edu.udel.cis.vsl.abc.ast.IF.AST;
import edu.udel.cis.vsl.abc.ast.IF.ASTFactory;
import edu.udel.cis.vsl.abc.ast.entity.IF.Variable;
import edu.udel.cis.vsl.abc.ast.node.IF.ASTNode;
import edu.udel.cis.vsl.abc.ast.node.IF.ASTNode.NodeKind;
import edu.udel.cis.vsl.abc.ast.node.IF.ExternalDefinitionNode;
import edu.udel.cis.vsl.abc.ast.node.IF.IdentifierNode;
import edu.udel.cis.vsl.abc.ast.node.IF.PairNode;
import edu.udel.cis.vsl.abc.ast.node.IF.SequenceNode;
import edu.udel.cis.vsl.abc.ast.node.IF.compound.CompoundInitializerNode;
import edu.udel.cis.vsl.abc.ast.node.IF.compound.DesignationNode;
import edu.udel.cis.vsl.abc.ast.node.IF.declaration.InitializerNode;
import edu.udel.cis.vsl.abc.ast.node.IF.declaration.VariableDeclarationNode;
import edu.udel.cis.vsl.abc.ast.node.IF.expression.CompoundLiteralNode;
import edu.udel.cis.vsl.abc.ast.node.IF.expression.ExpressionNode;
import edu.udel.cis.vsl.abc.ast.node.IF.expression.FunctionCallNode;
import edu.udel.cis.vsl.abc.ast.node.IF.expression.IdentifierExpressionNode;
import edu.udel.cis.vsl.abc.ast.node.IF.expression.IntegerConstantNode;
import edu.udel.cis.vsl.abc.ast.node.IF.expression.OperatorNode;
import edu.udel.cis.vsl.abc.ast.node.IF.expression.OperatorNode.Operator;
import edu.udel.cis.vsl.abc.ast.node.IF.label.SwitchLabelNode;
import edu.udel.cis.vsl.abc.ast.node.IF.omp.OmpForNode;
import edu.udel.cis.vsl.abc.ast.node.IF.omp.OmpParallelNode;
import edu.udel.cis.vsl.abc.ast.node.IF.omp.OmpReductionNode;
import edu.udel.cis.vsl.abc.ast.node.IF.omp.OmpSymbolReductionNode;
import edu.udel.cis.vsl.abc.ast.node.IF.omp.OmpSyncNode;
import edu.udel.cis.vsl.abc.ast.node.IF.omp.OmpWorksharingNode;
import edu.udel.cis.vsl.abc.ast.node.IF.statement.BlockItemNode;
import edu.udel.cis.vsl.abc.ast.node.IF.statement.CivlForNode;
import edu.udel.cis.vsl.abc.ast.node.IF.statement.CompoundStatementNode;
import edu.udel.cis.vsl.abc.ast.node.IF.statement.DeclarationListNode;
import edu.udel.cis.vsl.abc.ast.node.IF.statement.ExpressionStatementNode;
import edu.udel.cis.vsl.abc.ast.node.IF.statement.ForLoopInitializerNode;
import edu.udel.cis.vsl.abc.ast.node.IF.statement.ForLoopNode;
import edu.udel.cis.vsl.abc.ast.node.IF.statement.IfNode;
import edu.udel.cis.vsl.abc.ast.node.IF.statement.StatementNode;
import edu.udel.cis.vsl.abc.ast.node.IF.type.ArrayTypeNode;
import edu.udel.cis.vsl.abc.ast.node.IF.type.TypeNode;
import edu.udel.cis.vsl.abc.ast.type.IF.ArrayType;
import edu.udel.cis.vsl.abc.ast.type.IF.PointerType;
import edu.udel.cis.vsl.abc.ast.type.IF.StandardBasicType;
import edu.udel.cis.vsl.abc.ast.type.IF.StandardBasicType.BasicTypeKind;
import edu.udel.cis.vsl.abc.ast.type.IF.Type;
import edu.udel.cis.vsl.abc.parse.IF.CParser;
import edu.udel.cis.vsl.abc.parse.IF.OmpCParser;
import edu.udel.cis.vsl.abc.token.IF.Source;
import edu.udel.cis.vsl.abc.token.IF.SyntaxException;
import edu.udel.cis.vsl.civl.model.IF.CIVLSyntaxException;
import edu.udel.cis.vsl.civl.util.IF.Triple;

public class OpenMP2CIVLWorker extends BaseWorker {

	/* ************************** Private Static Fields ********************** */

	/**
	 * The name of the identifier of the $omp_gteam variable in the final CIVL
	 * program.
	 */
	private static String GTEAM = "gteam";

	/**
	 * The name of the identifier of the $omp_team variable in the final CIVL
	 * program.
	 */
	private static String TEAM = "team";

	/**
	 * The name of $omp_gteam type in the final CIVL-C program.
	 */
	private static String GTEAM_TYPE = "$omp_gteam";

	/**
	 * The name of $omp_team type in the final CIVL-C program.
	 */
	private static String TEAM_TYPE = "$omp_team";

	/**
	 * The name of the function to create a new $omp_gws object in the final
	 * CIVL-C program.
	 */
	private static String GTEAM_CREATE = "$omp_gteam_create";

	/**
	 * The name of the function to create a new $omp_ws object in the final
	 * CIVL-C program.
	 */
	private static String TEAM_CREATE = "$omp_team_create";

	/**
	 * The name of $omp_gshared type in the final CIVL-C program.
	 */
	private static String GSHARED_TYPE = "$omp_gshared";

	/**
	 * The name of $omp_shared type in the final CIVL-C program.
	 */
	private static String SHARED_TYPE = "$omp_shared";

	/**
	 * The name of the function to create a new $omp_gshared object in the final
	 * CIVL-C program.
	 */
	private static String GSHARED_CREATE = "$omp_gshared_create";

	/**
	 * The name of the function to create a new $omp_shared object in the final
	 * CIVL-C program.
	 */
	private static String SHARED_CREATE = "$omp_shared_create";

	/**
	 * The name of the input variable denoting the number of OpenMP threads in
	 * the final CIVL-C program.
	 */
	private static String NTHREADS = "_nthreads";

	/**
	 * The name of the input variable denoting the number of OpenMP threads in
	 * the final CIVL-C program.
	 */
	private static String THREADMAX = "THREAD_MAX";

	/**
	 * The name of the variable denoting the thread number in the CIVL_C
	 * program.
	 */
	private static String TID = "_tid";

	/* **************************** Instance Fields ************************* */

	/**
	 * There are new nodes created by the transformer, other than parsing from
	 * some source file. All new nodes share the same source.
	 */
	private Source source;

	/**
	 * Variable that is increment for naming of temp variables that are created
	 */
	private int tmpCount = 0;
	
	private int tempWriteCount = 0;

	/**
	 * Counter for locating $omp_arrive_loop functions
	 */
	private int ompArriveLoopCounter = 0;

	/**
	 * Counter for locating $omp_arrive_sections functions
	 */
	private int ompArriveSections = 0;

	/**
	 * Counter for locating $omp_arrive_single functions
	 */
	private int ompArriveSingle = 0;

	/**
	 * If the same variable is replaced in the same expression then use the temp
	 * name of the first one
	 */
	private ArrayList<Triple<String, StatementNode, String>> sharedReplaced = new ArrayList<Triple<String, StatementNode, String>>();

	/**
	 * For each critical section encountered, create a new critical variable
	 */
	private ArrayList<String> criticalNames = new ArrayList<String>();

	/* ****************************** Constructor ************************** */
	/**
	 * Creates a new instance of OpenMP2CIVLTransformer.
	 * 
	 * @param astFactory
	 *            The ASTFactory that will be used to create new nodes.
	 */
	public OpenMP2CIVLWorker(ASTFactory astFactory) {
		super("OpenMPtoCIVLTransformer", astFactory);
	}

	/* *************************** Private Methods ************************* */

	/**
	 * Creates the declaration node for the input variable
	 * <code>THREAD_MAX</code>.
	 * 
	 * @param range
	 *            a source object specifying a specific range in the original
	 *            source into which the new declaration is going to be inserted;
	 *            the line numbers of this range will be included in output to
	 *            give an idea of the location into which the generated text was
	 *            inserted. It is probably somewhere near the beginning of the
	 *            file.
	 * 
	 * @return The declaration node of the input variable
	 *         <code>THREAD_MAX</code>.
	 */
	private VariableDeclarationNode threadMaxDeclaration() {
		final String place = "threadMaxDeclaration";
		TypeNode nthreadsType = nodeFactory.newBasicTypeNode(
				newSource(place, CParser.INT), BasicTypeKind.INT);
		IdentifierNode identifierNode = nodeFactory.newIdentifierNode(
				newSource(place, CParser.IDENTIFIER), THREADMAX);

		nthreadsType.setInputQualified(true);
		return nodeFactory.newVariableDeclarationNode(
				newSource(place, CParser.DECLARATION), identifierNode,
				nthreadsType);
	}

	/**
	 * Creates the declaration node for the variable <code>gteam</code> , which
	 * is of <code>$omp_gteam</code> type and has an initializer to call
	 * <code>$omp_gteam_create()</code>. That is:
	 * <code>$omp_gteam gteam = $omp_gteam_create($here, NTHREADS)</code> .
	 * 
	 * @return The declaration node of the variable <code>gteam</code>.
	 */
	private VariableDeclarationNode gteamDeclaration() {
		TypeNode gteamType;
		ExpressionNode gteamCreate;
		final String place = "gteamDeclaration";
		gteamType = nodeFactory.newTypedefNameNode(nodeFactory
				.newIdentifierNode(newSource(place, CParser.TYPE),
						GTEAM_TYPE), null);
		gteamCreate = nodeFactory.newFunctionCallNode(
				newSource(place, CParser.CALL), 
				this.identifierExpression(newSource(place, 
						CParser.IDENTIFIER), GTEAM_CREATE),
						Arrays.asList(nodeFactory.newHereNode(newSource(place, 
								CParser.HERE)),this.identifierExpression(
										newSource(place, CParser.IDENTIFIER), 
										NTHREADS)), null);
		return nodeFactory.newVariableDeclarationNode(newSource(place, 
				CParser.DECLARATION), nodeFactory.newIdentifierNode(newSource(
						place, CParser.IDENTIFIER), GTEAM), gteamType,
						gteamCreate);
	}

	/**
	 * Creates the declaration node for the variable <code>team</code> , which
	 * is of <code>$omp_team</code> type and has an initializer to call
	 * <code>$omp_team_create()</code>. That is:
	 * <code>$omp_team team = $omp_team_create($here, gteam, _tid)</code> .
	 * 
	 * @return The declaration node of the variable <code>team</code>.
	 */
	private VariableDeclarationNode teamDeclaration() {
		TypeNode teamType;
		ExpressionNode teamCreate;
		final String place = "teamDelcaration";

		teamType = nodeFactory.newTypedefNameNode(nodeFactory
				.newIdentifierNode(newSource(place, CParser.TYPE), TEAM_TYPE),
				null);
		teamCreate = nodeFactory
				.newFunctionCallNode(newSource(place, CParser.CALL), this
						.identifierExpression(
								newSource(place, CParser.IDENTIFIER),
								TEAM_CREATE),
						Arrays.asList(nodeFactory.newHereNode(newSource(place,
								CParser.HERE)), this.identifierExpression(
								newSource(place, CParser.IDENTIFIER), GTEAM),
								this.identifierExpression(
										newSource(place, CParser.IDENTIFIER),
										TID)), null);
		return nodeFactory.newVariableDeclarationNode(
				newSource(place, CParser.DECLARATION),
				nodeFactory.newIdentifierNode(
						newSource(place, CParser.IDENTIFIER), TEAM), teamType,
				teamCreate);
	}

	/**
	 * Creates the declaration node for the variable <code>gshared</code>, which
	 * is of <code>$omp_gshared</code> type and has an initializer to call
	 * <code>$omp_gshared_create()</code>. That is: <code>$omp_gshared 
	 * x_gshared = $omp_gshared_create($omp_gteam, void *original)</code> .
	 * 
	 * @return The declaration node of the variable <code>x_gshared</code>.
	 */
	private VariableDeclarationNode gsharedDeclaration(String variable) {
		TypeNode gsharedType;
		ExpressionNode gsharedCreate;
		final String place = variable + "_gsharedDeclaration";

		ExpressionNode addressOf = nodeFactory.newOperatorNode(
				newSource(place, CParser.AMPERSAND),
				Operator.ADDRESSOF,
				Arrays.asList(this.identifierExpression(
						newSource(place, CParser.IDENTIFIER), variable)));

		gsharedType = nodeFactory.newTypedefNameNode(
				nodeFactory.newIdentifierNode(newSource(place, CParser.TYPE),
						GSHARED_TYPE), null);
		gsharedCreate = nodeFactory.newFunctionCallNode(
				newSource(place, CParser.CALL), this.identifierExpression(
						newSource(place, CParser.IDENTIFIER), GSHARED_CREATE),
				Arrays.asList(
						this.identifierExpression(
								newSource(place, CParser.IDENTIFIER), GTEAM),
						addressOf), null);
		return nodeFactory.newVariableDeclarationNode(
				newSource(place, CParser.DECLARATION),
				nodeFactory.newIdentifierNode(
						newSource(place, CParser.IDENTIFIER), variable
								+ "_gshared"), gsharedType, gsharedCreate);
	}

	/**
	 * Creates the declaration node for the variable <code>shared</code>, which
	 * is of <code>$omp_shared</code> type and has an initializer to call
	 * <code>$omp_shared_create()</code>. That is: <code>$omp_shared 
	 * x_shared = $omp_shared_create($omp_team team, $omp_gshared gshared, 
	 * void *local, void *status)</code> .
	 * 
	 * @return The declaration node of the variable <code>x_shared</code>.
	 */
	private VariableDeclarationNode sharedDeclaration(String variable) {
		TypeNode sharedType;
		ExpressionNode sharedCreate;
		final String place = variable + "_sharedDeclaration";

		sharedType = nodeFactory.newTypedefNameNode(
				nodeFactory.newIdentifierNode(newSource(place, CParser.TYPE),
						SHARED_TYPE), null);
		ExpressionNode addressOfLocalVar = nodeFactory.newOperatorNode(
				newSource(place, CParser.AMPERSAND),
				Operator.ADDRESSOF,
				Arrays.asList(this.identifierExpression(
						newSource(place, CParser.IDENTIFIER), variable
								+ "_local")));
		ExpressionNode addressOfStatusVar = nodeFactory.newOperatorNode(
				newSource(place, CParser.AMPERSAND),
				Operator.ADDRESSOF,
				Arrays.asList(this.identifierExpression(
						newSource(place, CParser.IDENTIFIER), variable
								+ "_status")));
		sharedCreate = nodeFactory.newFunctionCallNode(
				newSource(place, CParser.CALL), this.identifierExpression(
						newSource(place, CParser.IDENTIFIER), SHARED_CREATE),
				Arrays.asList(
						this.identifierExpression(
								newSource(place, CParser.IDENTIFIER), TEAM),
						this.identifierExpression(
								newSource(place, CParser.IDENTIFIER), variable
										+ "_gshared"), addressOfLocalVar,
						addressOfStatusVar), null);
		return nodeFactory.newVariableDeclarationNode(
				newSource(place, CParser.DECLARATION),
				nodeFactory.newIdentifierNode(
						newSource(place, CParser.IDENTIFIER), variable
								+ "_shared"), sharedType, sharedCreate);
	}

	/**
	 * Creates the function call node for <code>destroy</code>, which is of void
	 * type. That is: <code>$omp_(g)team_destroy($omp_(g)team (g)team)
	 * </code> . This can be used to destroy gteam or team depending in the type
	 * parameter
	 * 
	 * @return The function call node of <code>$omp_(g)team_destroy</code>.
	 */
	private ExpressionStatementNode destroy(String type, String object) {
		final String place = "destroyCall";
		ExpressionNode function = this.identifierExpression(
				newSource(place, CParser.IDENTIFIER), "$omp_" + type
						+ "_destroy");

		return nodeFactory.newExpressionStatementNode(nodeFactory
				.newFunctionCallNode(
						newSource(place, CParser.CALL),
						function,
						Arrays.asList(this.identifierExpression(
								newSource(place, CParser.IDENTIFIER), object)),
						null));
	}

	/**
	 * Creates the function call node for <code>barrier_and_flush</code>, which
	 * is of void type. That is:
	 * <code>$omp_barrier_and_flush($omp_team team) </code> . This is used as a
	 * barrier and a flush on all shared objects owned by the team
	 * 
	 * @return The function call node of <code>$omp_barrier_and_flush</code>.
	 */
	private ExpressionStatementNode barrierAndFlush(String object) {
		final String place = "barrierAndFlushCall";
		ExpressionNode function = this.identifierExpression(
				newSource(place, CParser.IDENTIFIER), "$omp_barrier_and_flush");

		return nodeFactory.newExpressionStatementNode(nodeFactory
				.newFunctionCallNode(
						newSource(place, CParser.CALL),
						function,
						Arrays.asList(this.identifierExpression(
								newSource(place, CParser.IDENTIFIER), object)),
						null));
	}

	/**
	 * Creates the function call node for <code>write</code>, which is of void
	 * type. That is: <code> $omp_write($omp_shared shared, 
	 * void *ref, void *value)</code> . This is used to write to shared
	 * variables.
	 * 
	 * @return The function call node of <code>$omp_write</code>.
	 */
	private ExpressionStatementNode write(ExpressionNode variable,
			String sharedName) {
		final String place = sharedName + "_sharedWriteCall";
		ExpressionNode function = this.identifierExpression(
				newSource(place, CParser.IDENTIFIER), "$omp_write");
		ExpressionNode addressOfVar = nodeFactory.newOperatorNode(
				newSource(place, CParser.AMPERSAND), Operator.ADDRESSOF,
				Arrays.asList(variable));
		ExpressionNode addressOfTmp = nodeFactory.newOperatorNode(
				newSource(place, CParser.AMPERSAND),
				Operator.ADDRESSOF,
				Arrays.asList(this.identifierExpression(
						newSource(place, CParser.IDENTIFIER),
						"tmpWrite" + String.valueOf(tempWriteCount))));
		return nodeFactory.newExpressionStatementNode(nodeFactory
				.newFunctionCallNode(newSource(place, CParser.CALL), function,
						Arrays.asList(this.identifierExpression(
								newSource(place, CParser.IDENTIFIER),
								sharedName + "_shared"), addressOfVar,
								addressOfTmp), null));
	}

	/**
	 * Creates the function call node for <code>read</code>, which is of void
	 * type. That is: <code> $omp_read($omp_shared shared, 
	 * void *result, void *ref)</code> . This is used to read shared variables.
	 * 
	 * @return The function call node of <code>$omp_read</code>.
	 */
	private ExpressionStatementNode read(ExpressionNode parent,
			String sharedName, String tmpName) {
		final String place = sharedName + "_sharedReadCall";
		ExpressionNode function = this.identifierExpression(
				newSource(place, CParser.IDENTIFIER), "$omp_read");
		ExpressionNode addressOfVar = nodeFactory.newOperatorNode(
				newSource(place, CParser.AMPERSAND), Operator.ADDRESSOF,
				Arrays.asList(parent));
		ExpressionNode addressOfTmp = nodeFactory.newOperatorNode(
				newSource(place, CParser.AMPERSAND),
				Operator.ADDRESSOF,
				Arrays.asList(this.identifierExpression(
						newSource(place, CParser.IDENTIFIER), tmpName)));
		return nodeFactory.newExpressionStatementNode(nodeFactory
				.newFunctionCallNode(newSource(place, CParser.CALL), function,
						Arrays.asList(this.identifierExpression(
								newSource(place, CParser.IDENTIFIER),
								sharedName + "_shared"), addressOfTmp,
								addressOfVar), null));
	}

	/**
	 * Creates the function call node for <code>apply_assoc</code>, which is of
	 * void type. That is: <code> $omp_apply_assoc($omp_shared shared, 
	 *  $operation op, void *local)</code> . This is used to apply associated
	 * operators.
	 * 
	 * @return The function call node of <code>$omp_apply_assoc</code>.
	 */
	private ExpressionStatementNode applyAssoc(String variable, String operation) {
		final String place = variable + "applyAssoc(" + operation + ")";
		ExpressionNode function = this.identifierExpression(
				newSource(place, CParser.IDENTIFIER), "$omp_apply_assoc");
		ExpressionNode addressOfVar = nodeFactory.newOperatorNode(
				newSource(place, CParser.AMPERSAND),
				Operator.ADDRESSOF,
				Arrays.asList(this.identifierExpression(
						newSource(place, CParser.IDENTIFIER), "_" + variable)));
		if (operation.equals("PLUSEQ")) {
			operation = "CIVL_SUM";
		}
		return nodeFactory.newExpressionStatementNode(nodeFactory
				.newFunctionCallNode(newSource(place, CParser.CALL), function,
						Arrays.asList(this.identifierExpression(
								newSource(place, CParser.IDENTIFIER), variable
										+ "_shared"), this
								.identifierExpression(
										newSource(place, CParser.IDENTIFIER),
										operation), addressOfVar), null));
	}

	/* ********************* Methods From TransformerWorker ****************** */

	/**
	 * Transform an AST of a OpenMP program in C into an equivalent AST of
	 * CIVL-C program.<br>
	 * 
	 * @param ast
	 *            The AST of the original OpenMP program in C.
	 * @return An AST of CIVL-C program equivalent to the original OpenMP
	 *         program.
	 * @throws SyntaxException
	 */
	@Override
	public AST transform(AST ast) throws SyntaxException {
		SequenceNode<ExternalDefinitionNode> root = ast.getRootNode();
		AST newAst;
		List<ExternalDefinitionNode> externalList;
		VariableDeclarationNode threadMax;
		SequenceNode<ExternalDefinitionNode> newRootNode;
		List<ExternalDefinitionNode> includedNodes = new ArrayList<>();
		List<VariableDeclarationNode> mainParameters = new ArrayList<>();
		int count;
		Triple<List<ExternalDefinitionNode>, List<ExternalDefinitionNode>, List<VariableDeclarationNode>> result;
		String criticalDeclaration = "criticalDeclarations";
		AST civlcAST = this.parseSystemLibrary("civlc.cvh");
		AST civlcOmpAST = this.parseSystemLibrary("civlc-omp.cvh");
		
		this.source = root.getSource();
		assert this.astFactory == ast.getASTFactory();
		assert this.nodeFactory == astFactory.getNodeFactory();
		ast.release();

		// declaring $input int THREAD_MAX;

		threadMax = this.threadMaxDeclaration();

		// Recursive call to replace all omp code
		replaceOMPPragmas(root, null, null, null, null);

		result = this.program(root);
		includedNodes = result.second;
		mainParameters = result.third;

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
		// Add threadMax declaration
		externalList.add(threadMax);
		for (String name : criticalNames) {
			externalList.add(nodeFactory.newVariableDeclarationNode(
					newSource(criticalDeclaration, CParser.DECLARATION),
					nodeFactory.newIdentifierNode(
							newSource(criticalDeclaration, CParser.IDENTIFIER),
							name), nodeFactory.newBasicTypeNode(
							newSource(criticalDeclaration, CParser.BOOL),
							BasicTypeKind.BOOL), nodeFactory
							.newBooleanConstantNode(
									newSource(criticalDeclaration,
											CParser.FALSE), false)));
		}
		externalList.addAll(result.first);
		newRootNode = nodeFactory.newSequenceNode(null, "TranslationUnit",
				externalList);
		// TODO: the following line is experimental. Check that it works:
		//completeSources(newRootNode);
		newAst = astFactory.newAST(newRootNode, ast.getSourceFiles());
		newAst = this.combineASTs(civlcOmpAST, newAst);
		newAst = this.combineASTs(civlcAST, newAst);
		return newAst;
	}

	@SuppressWarnings("unchecked")
	private void replaceOMPPragmas(ASTNode node,
			SequenceNode<IdentifierExpressionNode> privateIDs,
			SequenceNode<IdentifierExpressionNode> sharedIDs,
			SequenceNode<IdentifierExpressionNode> reductionIDs,
			SequenceNode<IdentifierExpressionNode> firstPrivateIDs)
			throws SyntaxException {
		if (node instanceof OmpParallelNode) {
			List<BlockItemNode> items;
			CompoundStatementNode pragmaBody;
			VariableDeclarationNode gteamVar;
			SequenceNode<IdentifierExpressionNode> sharedList;
			SequenceNode<IdentifierExpressionNode> privateList;
			SequenceNode<IdentifierExpressionNode> firstPrivateList;
			SequenceNode<IdentifierExpressionNode> reductionList = null;
			SequenceNode<OmpReductionNode> ompReductionNode;
			Iterable<ASTNode> children;
			String parallelNode = "parallelPragma";
			items = new LinkedList<>();
			children = node.children();

			// int _nthreads = 1+$choose_int(THREAD_MAX);
			String nthreadDeclaration = "_nthreadsDeclaration";
			VariableDeclarationNode nthreads;
			ExpressionNode add;
			ExpressionNode numThreads = ((OmpParallelNode) node).numThreads();

			removeNodeFromParent(numThreads);
			
			if(numThreads == null){
				numThreads = this.identifierExpression(
						newSource(nthreadDeclaration, 
								CParser.IDENTIFIER),
								THREADMAX);
			} 
			
			
			add = nodeFactory.newOperatorNode(newSource(nthreadDeclaration,
					CParser.PLUS), Operator.PLUS, Arrays
					.asList(nodeFactory.newIntegerConstantNode(
							newSource(nthreadDeclaration, 
									CParser.INTEGER_CONSTANT), "1"),
							nodeFactory.newFunctionCallNode(newSource(
									nthreadDeclaration, CParser.CHOOSE),
							this.identifierExpression(newSource(nthreadDeclaration,
									CParser.IDENTIFIER),"$choose_int"), 
									Arrays.asList(numThreads), null)));
			
			nthreads = nodeFactory.newVariableDeclarationNode(
					newSource(parallelNode, CParser.DECLARATION), nodeFactory
							.newIdentifierNode(
									newSource(nthreadDeclaration,
											CParser.IDENTIFIER), "_nthreads"),
					nodeFactory.newBasicTypeNode(
							newSource(nthreadDeclaration, CParser.INT),
							BasicTypeKind.INT), add);
			items.add(nthreads);

			// $range thread_range = 0 .. nthreads-1;
			String threadRangeDeclaration = "thread_rangeDeclaration";
			VariableDeclarationNode threadRange;
			threadRange = nodeFactory.newVariableDeclarationNode(newSource(
					parallelNode, CParser.DECLARATION), nodeFactory
					.newIdentifierNode(newSource(threadRangeDeclaration, 
							CParser.IDENTIFIER), "thread_range"),
					nodeFactory.newRangeTypeNode(newSource(
							threadRangeDeclaration, CParser.RANGE)),
					nodeFactory.newRegularRangeNode(newSource(
							threadRangeDeclaration, CParser.RANGE), nodeFactory
							.newIntegerConstantNode(newSource(
									threadRangeDeclaration, 
									CParser.INTEGER_CONSTANT), "0"), nodeFactory
							.newOperatorNode(newSource(threadRangeDeclaration, 
									CParser.SUB), Operator.MINUS, Arrays
									.asList(this.identifierExpression(newSource(
											threadRangeDeclaration, 
											CParser.IDENTIFIER), "_nthreads"),
											nodeFactory.newIntegerConstantNode(
													newSource(threadRangeDeclaration, 
															CParser.INTEGER_CONSTANT), "1")))));
			items.add(threadRange);

			// $domain(1) dom = ($domain){thread_range};
			String domainDeclaration = "domainDeclaration";
			List<PairNode<DesignationNode, InitializerNode>> initList = new ArrayList<PairNode<DesignationNode, InitializerNode>>();
			initList.add(nodeFactory.newPairNode(
					newSource(domainDeclaration, CParser.STRUCT),
					(DesignationNode) null,
					(InitializerNode) this.identifierExpression(
							newSource(domainDeclaration, CParser.IDENTIFIER),
							"thread_range")));
			CompoundInitializerNode temp = nodeFactory
					.newCompoundInitializerNode(
							newSource(domainDeclaration,
									CParser.INITIALIZER_LIST), initList);
			CompoundLiteralNode cln = nodeFactory.newCompoundLiteralNode(
					newSource(domainDeclaration, CParser.COMPOUND_LITERAL),
					nodeFactory.newDomainTypeNode(newSource(domainDeclaration,
							CParser.DOMAIN)), temp);

			VariableDeclarationNode loopDomain;
			loopDomain = nodeFactory.newVariableDeclarationNode(
					newSource(parallelNode, CParser.DECLARATION), nodeFactory
							.newIdentifierNode(
									newSource(domainDeclaration,
											CParser.IDENTIFIER), "dom"),
					nodeFactory.newDomainTypeNode(
							newSource(domainDeclaration, CParser.DOMAIN),
							nodeFactory.newIntegerConstantNode(
									newSource(domainDeclaration,
											CParser.INTEGER_CONSTANT), "1")),
					cln);
			items.add(loopDomain);

			// Declaring $omp_gteam gteam = $omp_gteam_create($here, nthreads);
			gteamVar = this.gteamDeclaration();
			items.add(gteamVar);

			// Get list of shared, private, and firstPrivate variables from the
			// OmpParallelNode
			sharedList = ((OmpParallelNode) node).sharedList();
			removeNodeFromParent(sharedList);
			privateList = ((OmpParallelNode) node).privateList();
			removeNodeFromParent(privateList);
			firstPrivateList = ((OmpParallelNode) node)
					.firstprivateList();
			removeNodeFromParent(firstPrivateList);

			// Get list of reduction variables from the OmpParallelNode
			OmpSymbolReductionNode reductionNode = null;
			ompReductionNode = ((OmpParallelNode) node).reductionList();
			removeNodeFromParent(ompReductionNode);
			if (ompReductionNode != null) {
				reductionNode = (OmpSymbolReductionNode) ompReductionNode
						.child(0);
				reductionList = (SequenceNode<IdentifierExpressionNode>) reductionNode
						.child(0);
			}

			// Declaring $omp_gshared x_gshared = $omp_gshared_create(gteam, &x)
			// for each shared variable "x"
			if (sharedList != null) {
				for (ASTNode child : sharedList.children()) {
					VariableDeclarationNode gsharedVar;
					IdentifierNode c = ((IdentifierExpressionNode) child)
							.getIdentifier();

					gsharedVar = this.gsharedDeclaration(c.name());
					items.add(gsharedVar);
				}
			}
			CivlForNode cfn;

			// Create the ForLoopInitializerNode for the CIVLForNode
			String parFor = "parFor";
			ForLoopInitializerNode initializerNode;
			initializerNode = nodeFactory.newForLoopInitializerNode(
					newSource(parFor, CParser.INITIALIZER_LIST),
					Arrays.asList(nodeFactory.newVariableDeclarationNode(
							newSource(parFor, CParser.DECLARATION),
							nodeFactory.newIdentifierNode(
									newSource(parFor, CParser.IDENTIFIER),
									"_tid"), nodeFactory.newBasicTypeNode(
									newSource(parFor, CParser.INT),
									BasicTypeKind.INT))));

			List<BlockItemNode> parForItems = new LinkedList<>();

			// $omp_team team = $omp_team_create($here, gteam, _tid);
			parForItems.add(teamDeclaration());

			// Create local and status variables for each shared variable
			if (sharedList != null) {
				for (ASTNode child : sharedList.children()) {
					VariableDeclarationNode localSharedVar;
					VariableDeclarationNode statusSharedVar;
					
					IdentifierNode c = (IdentifierNode) child.child(0);
					String localDeclaration = "LocalSharedVarDeclaration";
					String statusDeclaration = "StatusSharedVarDeclaration";
					TypeNode privateType = ((VariableDeclarationNode) ((Variable) c
							.getEntity()).getFirstDeclaration()).getTypeNode()
							.copy();
					IdentifierNode localSharedIdentifer = nodeFactory
							.newIdentifierNode(
									newSource(c.name() + localDeclaration,
											CParser.IDENTIFIER), c.name()
											+ "_local");
					IdentifierNode statusSharedIdentifer = nodeFactory
							.newIdentifierNode(
									newSource(c.name() + statusDeclaration,
											CParser.IDENTIFIER), c.name()
											+ "_status");

					localSharedVar = nodeFactory.newVariableDeclarationNode(
							newSource(localDeclaration, CParser.DECLARATION),
							localSharedIdentifer, privateType);

					TypeNode statusType = privateType.copy();
					TypeNode baseStatusType = statusType;
					while (statusType instanceof ArrayTypeNode
							&& statusType.child(0) instanceof ArrayTypeNode) {
						statusType = (TypeNode) statusType.child(0);
					}
					if (statusType instanceof ArrayTypeNode) {
						statusType.setChild(0, nodeFactory.newBasicTypeNode(
								newSource(statusDeclaration, CParser.INT),
								BasicTypeKind.INT));
						statusSharedVar = nodeFactory
								.newVariableDeclarationNode(
										newSource(statusDeclaration,
												CParser.DECLARATION),
										statusSharedIdentifer, baseStatusType);
					} else {
						statusSharedVar = nodeFactory
								.newVariableDeclarationNode(
										newSource(statusDeclaration,
												CParser.DECLARATION),
										statusSharedIdentifer,
										nodeFactory.newBasicTypeNode(
												newSource(statusDeclaration,
														CParser.INT),
												BasicTypeKind.INT));
					}

					parForItems.add(localSharedVar);
					parForItems.add(statusSharedVar);

				}
			}

			// Declare $omp_shared x_shared = $omp_shared_create(team,
			// x_gshared, void *local, void, *status)
			// for each shared variable "x"
			if (sharedList != null) {
				for (ASTNode child : sharedList.children()) {
					VariableDeclarationNode sharedVar;
					IdentifierNode c = ((IdentifierExpressionNode) child)
							.getIdentifier();

					sharedVar = this.sharedDeclaration(c.name());
					parForItems.add(sharedVar);
				}
			}

			// Add firstprivate variable declarations
			if (firstPrivateList != null) {
				for (ASTNode child : firstPrivateList.children()) {
					VariableDeclarationNode firstPrivate = addPrivateVariable(
							(IdentifierExpressionNode) child, "first");
					parForItems.add(firstPrivate);
				}
			}

			// Add reduction variable declarations
			if (reductionList != null) {
				for (ASTNode child : reductionList.children()) {
					VariableDeclarationNode localPrivate = addPrivateVariable(
							(IdentifierExpressionNode) child, "reduction");
					parForItems.add(localPrivate);
				}
			}

			// Declare local copies of the private variables
			if (privateList != null) {
				for (ASTNode child : privateList.children()) {
					VariableDeclarationNode localPrivate = addPrivateVariable(
							(IdentifierExpressionNode) child, "regular");
					parForItems.add(localPrivate);
				}
			}

			// Add all children to the $par_for body
			int i = 0;
			for (ASTNode child : children) {
				node.removeChild(i);
				parForItems.add((BlockItemNode) child);
				i++;
			}

			// $omp_barrier_and_flush(team);
			parForItems.add(barrierAndFlush(TEAM));

			// $omp_shared_destroy(x_shared);
			// for each shared variable "x"
			if (sharedList != null) {
				for (ASTNode child : sharedList.children()) {
					IdentifierNode c = ((IdentifierExpressionNode) child)
							.getIdentifier();

					parForItems.add(destroy("shared", c.name() + "_shared"));
				}
			}

			// $omp_team_destroy(team);
			parForItems.add(destroy(TEAM, TEAM));

			StatementNode parForBody;
			parForBody = nodeFactory.newCompoundStatementNode(
					newSource(parFor, CParser.COMPOUND_STATEMENT), parForItems);

			// Create $par_for loop
			cfn = nodeFactory.newCivlForNode(
					newSource(parallelNode, CParser.CIVLFOR),
					true,
					(DeclarationListNode) initializerNode,
					this.identifierExpression(
							newSource(parFor, CParser.IDENTIFIER), "dom"),
					parForBody, null);

			// Add $par_for to the body
			items.add(cfn);

			// $omp_shared_destroy(x_gshared);
			// for each shared variable "x"
			if (sharedList != null) {
				for (ASTNode child : sharedList.children()) {
					IdentifierNode c = ((IdentifierExpressionNode) child)
							.getIdentifier();

					items.add(destroy("gshared", c.name() + "_gshared"));
				}
			}

			// $omp_gteam_destroy(gteam);
			items.add(destroy("gteam", "gteam"));

			// Create the CompoundStatementNode of that replaces the
			// OmpParallelNode
			pragmaBody = nodeFactory.newCompoundStatementNode(
					newSource(parallelNode, OmpCParser.PARALLEL), items);

			// Insert the CompoundStatementNode where the OmpParallelNode used
			// to be
			int index = node.childIndex();
			ASTNode parent = node.parent();
			parent.setChild(index, pragmaBody);
			children = pragmaBody.children();

			// Visit all the child to check for omp code
			for (ASTNode child : children) {
				replaceOMPPragmas(child, privateList, sharedList,
						reductionList, firstPrivateList);
			}
		} else if (node instanceof OmpForNode) {
			ForLoopInitializerNode initializerNode;
			SequenceNode<OmpReductionNode> ompReductionNode;
			SequenceNode<IdentifierExpressionNode> reductionList = null;
			List<BlockItemNode> items;
			List<BlockItemNode> forItems;
			CompoundStatementNode pragmaBody;
			Iterable<ASTNode> children = node.children();
			items = new LinkedList<>();
			forItems = new LinkedList<>();
			VariableDeclarationNode loopDomain;
			int collapseLevel = ((OmpForNode) node).collapse();
			ASTNode body = null;
			ArrayList<Triple<ASTNode, ASTNode, ASTNode>> ranges = new ArrayList<>();
			ArrayList<IdentifierNode> loopVariables = new ArrayList<IdentifierNode>();
			SequenceNode<IdentifierExpressionNode> firstPrivateList;
			ForLoopNode currentLoop = null;
			ForLoopInitializerNode initializer;
			OmpSymbolReductionNode reductionNode = null;
			String forNode = "forPragma";

			// Get the list of reduction variables in the OmpForNode
			ompReductionNode = ((OmpForNode) node).reductionList();
			removeNodeFromParent(ompReductionNode);
			if (ompReductionNode != null) {
				reductionNode = (OmpSymbolReductionNode) ompReductionNode
						.child(0);
				reductionList = (SequenceNode<IdentifierExpressionNode>) reductionNode
						.child(0);
			}

			// Get the lost of first privates varaibles in the OmpForNode
			firstPrivateList = ((OmpForNode) node).firstprivateList();
			removeNodeFromParent(firstPrivateList);
			
			for (int i = 0; i < collapseLevel; i++) {
				Triple<ASTNode, ASTNode, ASTNode> lo_hi_step;

				if (i == 0) {
					currentLoop = (ForLoopNode) node.child(7);
					body = currentLoop.child(1);
				} else {
					currentLoop = (ForLoopNode) currentLoop.getBody();
					body = currentLoop.child(1);
				}
				initializer = currentLoop.getInitializer();
				lo_hi_step = canonicalForLoopBounds(currentLoop);
				ranges.add(new Triple<ASTNode, ASTNode, ASTNode>(
						lo_hi_step.first, lo_hi_step.second, lo_hi_step.third));
				// Initializer node only will be either IdentifierExpressionNode
				// or DeclarationNode.
				// The first child of the first child of both of them is
				// identifier node.
				loopVariables.add((IdentifierNode) initializer.child(0)
						.child(0));
			}
			// For each of the ranges create a range variable
			int rangeNumber = 0;
			for (Triple<ASTNode, ASTNode, ASTNode> triple : ranges) {
				VariableDeclarationNode threadRange;
				String range = "rangeDeclaration";
				threadRange = nodeFactory.newVariableDeclarationNode(
						newSource(forNode, CParser.DECLARATION),
						nodeFactory.newIdentifierNode(
								newSource(range, CParser.IDENTIFIER), "r"
										+ Integer.toString(rangeNumber + 1)),
						nodeFactory.newRangeTypeNode(newSource(range,
								CParser.RANGE)), nodeFactory
								.newRegularRangeNode(
										newSource(range, CParser.RANGE),
										(ExpressionNode) triple.first.copy(),
										(ExpressionNode) triple.second.copy(),
										(ExpressionNode) triple.third.copy()));
				items.add(threadRange);
				rangeNumber++;
			}

			// Create the right hand side of the domain variable declaration
			List<PairNode<DesignationNode, InitializerNode>> initList = new ArrayList<PairNode<DesignationNode, InitializerNode>>();
			
			String domainDeclaration = "domainDeclaration";
			
			for (int k = 1; k < rangeNumber + 1; k++) {
				initList.add(nodeFactory.newPairNode(
						newSource(domainDeclaration, CParser.STRUCT),
						(DesignationNode) null, (InitializerNode) this
								.identifierExpression(
										newSource(domainDeclaration,
												CParser.IDENTIFIER), "r"
												+ Integer.toString(k))));
			}
			
			CompoundInitializerNode temp = nodeFactory
					.newCompoundInitializerNode(
							newSource(domainDeclaration,
									CParser.INITIALIZER_LIST), initList);
			CompoundLiteralNode cln = nodeFactory.newCompoundLiteralNode(
					newSource(domainDeclaration, CParser.COMPOUND_LITERAL),
					nodeFactory.newDomainTypeNode(newSource(domainDeclaration,
							CParser.DOMAIN)), temp);

			// If there is only 1 child then make that the children
			// Else get the list from the OmpForNode body
			if (body instanceof CompoundStatementNode) {
				children = body.children();
			} else {
				ArrayList<ASTNode> tempList = new ArrayList<ASTNode>();
				tempList.add(body);
				Iterable<ASTNode> tempIterable = tempList;
				children = (Iterable<ASTNode>) tempIterable;
			}

			// Create loop_domain variable
			loopDomain = nodeFactory.newVariableDeclarationNode(
					newSource(forNode, CParser.DECLARATION),
					nodeFactory.newIdentifierNode(
							newSource(domainDeclaration, CParser.IDENTIFIER),
							"loop_domain"), nodeFactory.newDomainTypeNode(
							newSource(domainDeclaration, CParser.DOMAIN),
							nodeFactory.newIntegerConstantNode(
									newSource(domainDeclaration,
											CParser.INTEGER_CONSTANT), "1")),
					cln);
			items.add(loopDomain);

			// ($domain(1))$omp_arrive_loop(team, int, loop_domain, strategy)
			String myItersPlace = "myItersDeclaration";
			ExpressionNode ompArriveLoop = nodeFactory.newCastNode(newSource(myItersPlace, CParser.CAST),
					nodeFactory.newDomainTypeNode(
							newSource(myItersPlace, CParser.DOMAIN),
							nodeFactory.newIntegerConstantNode(newSource(myItersPlace, CParser.INTEGER_CONSTANT),
									Integer.toString(collapseLevel))),
					nodeFactory.newFunctionCallNode(newSource(myItersPlace, CParser.CALL), this
							.identifierExpression(newSource(myItersPlace, CParser.IDENTIFIER), "$omp_arrive_loop"),
							Arrays.asList(this.identifierExpression(newSource(myItersPlace, CParser.IDENTIFIER),
									TEAM), nodeFactory.newIntegerConstantNode(
									newSource(myItersPlace, CParser.INTEGER_CONSTANT), this.ompArriveLoopCounter + ""),
									nodeFactory.newCastNode(newSource(myItersPlace, CParser.CAST), nodeFactory
											.newDomainTypeNode(newSource(myItersPlace, CParser.DOMAIN)), this
											.identifierExpression(newSource(myItersPlace, CParser.IDENTIFIER),
													"loop_domain")),
									nodeFactory.newIntegerConstantNode(newSource(myItersPlace, CParser.INTEGER_CONSTANT),
											"0")), null));
			this.ompArriveLoopCounter++;
			// Get correct level "n" for domain(n)
			IntegerConstantNode domainLevel = nodeFactory.newIntegerConstantNode(newSource(myItersPlace, CParser.INTEGER_CONSTANT),
						String.valueOf(collapseLevel));


			// Add $domain(1) my_iters = ($domain(1))$omp_arrive_loop(team,
			// loop_domain);
			VariableDeclarationNode myIters;
			myIters = nodeFactory.newVariableDeclarationNode(
					newSource(forNode, CParser.DECLARATION),
					nodeFactory.newIdentifierNode(
							newSource(myItersPlace, CParser.IDENTIFIER),
							"my_iters"), nodeFactory.newDomainTypeNode(
							newSource(myItersPlace, CParser.DOMAIN),
							domainLevel), ompArriveLoop);
			items.add(myIters);

			// Add firstprivate variable declarations
			if (firstPrivateList != null) {
				for (ASTNode child : firstPrivateList.children()) {
					VariableDeclarationNode firstPrivate = addPrivateVariable(
							(IdentifierExpressionNode) child, "first");
					items.add(firstPrivate);
				}
				firstPrivateIDs = firstPrivateList;
			}

			// Add reduction variable declarations
			if (reductionList != null) {
				for (ASTNode child : reductionList.children()) {
					VariableDeclarationNode localPrivate = addPrivateVariable(
							(IdentifierExpressionNode) child, "reduction");
					items.add(localPrivate);
				}
				reductionIDs = reductionList;
			}
			
			String civlFor = "civlFor";
			CivlForNode cfn;

			List<VariableDeclarationNode> declarations = new ArrayList<VariableDeclarationNode>();
			for (IdentifierNode var : loopVariables) {
				declarations.add(nodeFactory.newVariableDeclarationNode(
						newSource(civlFor, CParser.DECLARATION), var.copy(),
						nodeFactory.newBasicTypeNode(
								newSource(civlFor, CParser.INT),
								BasicTypeKind.INT)));
			}

			initializerNode = nodeFactory.newForLoopInitializerNode(
					newSource(civlFor, CParser.INITIALIZER_LIST), declarations);

			// Get the body of the OmpForNode and go down for loop children if
			// there is a collapse clause
			for (int i = 0; i < collapseLevel; i++) {
				if (i == 0) {
					currentLoop = (ForLoopNode) node.child(7);
				} else {
					currentLoop = (ForLoopNode) currentLoop.getBody();
				}
			}

			// Remove children from original for body and add to new for loop
			int i = 0;
			for (ASTNode child : children) {
				if (body instanceof CompoundStatementNode) {
					body.removeChild(i);
				} else {
					ASTNode bodyParent = body.parent();
					int bodyIndex = body.childIndex();
					bodyParent.removeChild(bodyIndex);
				}

				forItems.add((BlockItemNode) child);
				i++;
			}

			StatementNode forBody;
			forBody = nodeFactory.newCompoundStatementNode(
					newSource(civlFor, CParser.COMPOUND_STATEMENT), forItems);

			// Create $for node with the original for body as its body
			cfn = nodeFactory.newCivlForNode(
					newSource(forNode, CParser.CIVLFOR), false,
					(DeclarationListNode) initializerNode,
					nodeFactory.newIdentifierExpressionNode(
							newSource(civlFor, CParser.INITIALIZER_LIST),
							nodeFactory.newIdentifierNode(
									newSource(civlFor, CParser.IDENTIFIER),
									"my_iters")), forBody, null);
			items.add(cfn);

			// Apply association operator to all reduction variables
			if (reductionList != null) {
				for (ASTNode child : reductionList.children()) {
					String name = ((IdentifierNode) child.child(0)).name();
					String operator = reductionNode.operator().name();
					items.add(applyAssoc(name, operator));
				}
			}

			// $barrier_and_flush(team);
			if (!((OmpForNode) node).nowait()) {
				items.add(barrierAndFlush(TEAM));
			}

			// Insert the CompoundStatementNode where the OmpForNode used to be
			pragmaBody = nodeFactory.newCompoundStatementNode(
					newSource(forNode, CParser.COMPOUND_STATEMENT), items);
			children = pragmaBody.children();
			int index = node.childIndex();
			ASTNode parent = node.parent();
			parent.setChild(index, pragmaBody);

			// Visit all of the children to check for omp code
			for (ASTNode child : children) {
				replaceOMPPragmas(child, privateIDs, sharedIDs, reductionIDs,
						firstPrivateIDs);
			}

		} else if (node instanceof OmpSyncNode) {
			String syncKind = ((OmpSyncNode) node).ompSyncNodeKind().toString();
			CompoundStatementNode body;
			LinkedList<BlockItemNode> items = new LinkedList<>();
			// Check the synckind
			if (syncKind.equals("MASTER")) {
				String syncNode = "masterNode";
				// Replace omp master with a check if _tid==0 and insert the omp
				// master body into that
				List<ExpressionNode> operands = new ArrayList<ExpressionNode>();
				operands.add(nodeFactory.newIdentifierExpressionNode(
						newSource(syncNode, CParser.IDENTIFIER),
						nodeFactory
						.newIdentifierNode(
								newSource(syncNode, CParser.IDENTIFIER),
								"_tid")));
				operands.add(nodeFactory.newIntegerConstantNode(
						newSource(syncNode, CParser.INTEGER_CONSTANT), "0"));
				int i = 0;
				for (ASTNode child : node.children()) {
					node.removeChild(i);
					items.add((BlockItemNode) child);
					i++;
				}
				body = nodeFactory.newCompoundStatementNode(
						newSource(syncNode, CParser.COMPOUND_STATEMENT), items);
				IfNode ifStatement = nodeFactory.newIfNode(
						newSource(syncNode, CParser.IF), nodeFactory
						.newOperatorNode(
								newSource(syncNode, CParser.EQUALS),
								Operator.EQUALS, operands), body);
				// Replace omp master with the new if statement
				int index = node.childIndex();
				ASTNode parent = node.parent();
				parent.setChild(index, ifStatement);
			} else if (syncKind.equals("BARRIER")) {
				// Replace omp barrier with $omp_barrier_and_flush
				ExpressionStatementNode barrierAndFlush = barrierAndFlush(TEAM);
				int index = node.childIndex();
				ASTNode parent = node.parent();
				parent.setChild(index, barrierAndFlush);
			} else if (syncKind.equals("CRITICAL")) {
				// For an omp critical, check if there is a name for the
				// critical
				// If there is a name, get the name. Else, give it noname
				String syncNode = "criticalNode";
				IdentifierNode criticalIDName = ((OmpSyncNode) node)
						.criticalName();
				removeNodeFromParent(criticalIDName);
				String criticalName = null;
				if (criticalIDName != null) {
					criticalName = "_critical_" + criticalIDName.name();
				} else {
					criticalName = "_critical_noname";
				}

				// Create expression and body of when node
				ExpressionNode notCritical = nodeFactory.newOperatorNode(
						newSource(syncNode, CParser.NOT), Operator.NOT, Arrays
								.asList(this
										.identifierExpression(
												newSource(syncNode,
														CParser.IDENTIFIER),
												criticalName)));
				ExpressionStatementNode criticalTrue = nodeFactory
						.newExpressionStatementNode(nodeFactory.newOperatorNode(
								newSource(syncNode, CParser.ASSIGN),
								Operator.ASSIGN, Arrays.asList(this
										.identifierExpression(
												newSource(syncNode,
														CParser.IDENTIFIER),
												criticalName), nodeFactory
										.newBooleanConstantNode(
												newSource(syncNode,
														CParser.TRUE), true))));

				// Create and add when node
				// $when (!_critical_a) _critical_a=$true;
				items.add(nodeFactory.newWhenNode(
						newSource(syncNode, CParser.WHEN), notCritical,
						criticalTrue));
				// Put critical section body in the when body.
				int i = 0;
				for (ASTNode child : node.children()) {
					node.removeChild(i);
					items.add((BlockItemNode) child);
					i++;
				}
				// Make the critical variable false now
				ExpressionStatementNode criticalFalse = nodeFactory
						.newExpressionStatementNode(nodeFactory.newOperatorNode(
								newSource(syncNode, CParser.ASSIGN),
								Operator.ASSIGN, Arrays.asList(this
										.identifierExpression(
												newSource(syncNode,
														CParser.IDENTIFIER),
												criticalName), nodeFactory
										.newBooleanConstantNode(
												newSource(syncNode,
														CParser.FALSE), false))));
				items.add(criticalFalse);

				// Add body to the CompoundstatementNode and replace the omp
				// critical node with the CompoundStatementNode
				body = nodeFactory.newCompoundStatementNode(
						newSource(syncNode, CParser.COMPOUND_STATEMENT), items);
				criticalNames.add(criticalName);
				int index = node.childIndex();
				ASTNode parent = node.parent();
				parent.setChild(index, body);
			}
		} else if (node instanceof OmpWorksharingNode) {
			Iterable<ASTNode> children = node.children();
			String workshareKind = ((OmpWorksharingNode) node)
					.ompWorkshareNodeKind().toString();
			CompoundStatementNode body;
			LinkedList<BlockItemNode> items = new LinkedList<>();
			if (workshareKind.equals("SECTIONS")) {
				String sectionsPlace = "sections";
				privateIDs = ((OmpWorksharingNode) node).privateList();
				removeNodeFromParent(privateIDs);
				firstPrivateIDs = ((OmpWorksharingNode) node)
						.firstprivateList();
				removeNodeFromParent(firstPrivateIDs);
				int numberSections = 0;
				CompoundStatementNode pragmaBody = (CompoundStatementNode) node
						.child(7);
				ArrayList<LinkedList<BlockItemNode>> sectionsChildren = new ArrayList<LinkedList<BlockItemNode>>();
				for (ASTNode child : pragmaBody.children()) {
					if (child instanceof OmpWorksharingNode) {
						if (((OmpWorksharingNode) child)
								.ompWorkshareNodeKind().toString()
								.equals("SECTION")) {
							LinkedList<BlockItemNode> sectionItems = new LinkedList<>();
							int i = 0;
							for (ASTNode sectionChild : child.children()) {
								child.removeChild(i);
								sectionItems.add((BlockItemNode) sectionChild);
								i++;
							}
							sectionsChildren.add(sectionItems);
							numberSections++;
						}
					}
				}

				ExpressionNode ompArriveSections = nodeFactory.newFunctionCallNode(
								newSource(sectionsPlace, CParser.CALL),
								this.identifierExpression(newSource(sectionsPlace, CParser.IDENTIFIER),
										"$omp_arrive_sections"),
								Arrays.asList(this.identifierExpression(newSource(sectionsPlace, CParser.IDENTIFIER),
										TEAM), nodeFactory
										.newIntegerConstantNode(newSource(sectionsPlace, CParser.INTEGER_CONSTANT),
												this.ompArriveSections + ""),
										this.identifierExpression(newSource(sectionsPlace, CParser.IDENTIFIER),
												String.valueOf(numberSections))),
								null);
				this.ompArriveSections++;
				String mySecDeclaration = "my_secsDeclaration";
				VariableDeclarationNode my_secs;
				my_secs = nodeFactory.newVariableDeclarationNode(newSource(
						sectionsPlace, CParser.DECLARATION), nodeFactory
						.newIdentifierNode(newSource(mySecDeclaration, 
								CParser.IDENTIFIER), "my_secs"),
								nodeFactory.newDomainTypeNode(newSource(
										mySecDeclaration, CParser.DOMAIN),
										nodeFactory.newIntegerConstantNode(
												newSource(mySecDeclaration, 
														CParser.INTEGER_CONSTANT), 
														"1")), ompArriveSections);
				items.add(my_secs);

				// Declare local copies of the private variables
				if (privateIDs != null) {
					for (ASTNode child : privateIDs.children()) {
						VariableDeclarationNode localPrivate = addPrivateVariable(
								(IdentifierExpressionNode) child, "regular");
						items.add(localPrivate);
					}
				}

				// Add firstprivate variable declarations
				if (firstPrivateIDs != null) {
					for (ASTNode child : firstPrivateIDs.children()) {
						VariableDeclarationNode firstPrivate = addPrivateVariable(
								(IdentifierExpressionNode) child, "first");
						items.add(firstPrivate);
					}
				}

				CivlForNode cfn;
				String civlFor = "civlFor";
				List<BlockItemNode> forItems = new LinkedList<>();

				// for loop;
				ForLoopInitializerNode initializerNode = nodeFactory
						.newForLoopInitializerNode(newSource(civlFor, 
								CParser.INITIALIZER_LIST), Arrays
								.asList(nodeFactory.newVariableDeclarationNode(
										newSource(civlFor, CParser.DECLARATION), 
										nodeFactory.newIdentifierNode(newSource(
												civlFor, CParser.IDENTIFIER), "i"), nodeFactory
												.newBasicTypeNode(newSource(civlFor, CParser.INT),
														BasicTypeKind.INT))));

				StatementNode forBody;
				StatementNode switchBody;
				List<BlockItemNode> switchItems = new LinkedList<>();
				int caseNumber = 0;
				String caseItem = "caseItem";
				for (LinkedList<BlockItemNode> tempChildren : sectionsChildren) {
					StatementNode caseBody;
					List<BlockItemNode> caseItems = tempChildren;
					caseItems.add(nodeFactory.newBreakNode(newSource(caseItem, CParser.BREAK)));
					caseBody = nodeFactory.newCompoundStatementNode(newSource(caseItem, CParser.COMPOUND_STATEMENT),
							caseItems);
					SwitchLabelNode labelDecl = nodeFactory
							.newCaseLabelDeclarationNode(newSource(caseItem, CParser.CASE), nodeFactory
									.newIntegerConstantNode(newSource(caseItem, CParser.INTEGER_CONSTANT),
											String.valueOf(caseNumber)),
									caseBody);
					switchItems.add(nodeFactory.newLabeledStatementNode(newSource(caseItem, CParser.CASE_LABELED_STATEMENT),
							labelDecl, caseBody));
				}
				switchBody = nodeFactory.newCompoundStatementNode(newSource(civlFor, CParser.COMPOUND_STATEMENT),
						switchItems);
				forItems.add(nodeFactory.newSwitchNode(newSource(civlFor, CParser.SWITCH),
						this.identifierExpression(newSource(civlFor, CParser.IDENTIFIER), "i"), switchBody));
				forBody = nodeFactory
						.newCompoundStatementNode(newSource(civlFor, CParser.COMPOUND_STATEMENT), forItems);

				cfn = nodeFactory.newCivlForNode(newSource(sectionsPlace, CParser.CIVLFOR), true,
						(DeclarationListNode) initializerNode, nodeFactory
								.newIdentifierExpressionNode(newSource(civlFor, CParser.IDENTIFIER),
										nodeFactory.newIdentifierNode(newSource(civlFor, CParser.IDENTIFIER),
												"my_secs")), forBody, null);
				items.add(cfn);

				if (!((OmpWorksharingNode) node).nowait()) {
					items.add(barrierAndFlush(TEAM));
				}

				CompoundStatementNode sectionBody = nodeFactory
						.newCompoundStatementNode(newSource(sectionsPlace, CParser.COMPOUND_STATEMENT), items);

				int index = node.childIndex();
				ASTNode parent = node.parent();
				parent.setChild(index, sectionBody);

				for (ASTNode child : children) {
					replaceOMPPragmas(child, privateIDs, sharedIDs,
							reductionIDs, firstPrivateIDs);
				}

			}
			if (workshareKind.equals("SINGLE")) {
				String singlePlace = "single";
				ExpressionNode arriveSingle = nodeFactory
						.newFunctionCallNode(source, this.identifierExpression(
								source, "$omp_arrive_single"), Arrays.asList(
								this.identifierExpression(source, TEAM),
								nodeFactory.newIntegerConstantNode(source,
										this.ompArriveSingle + "")), null);
				this.ompArriveSingle++;
				items.add(nodeFactory.newVariableDeclarationNode(
						newSource(singlePlace, CParser.DECLARATION),
						nodeFactory.newIdentifierNode(
								newSource(singlePlace, CParser.IDENTIFIER),
								"owner"), nodeFactory.newBasicTypeNode(
								newSource(singlePlace, CParser.INT),
								BasicTypeKind.INT), arriveSingle));

				List<ExpressionNode> operands = new ArrayList<ExpressionNode>();
				operands.add(this.identifierExpression(
						newSource(singlePlace, CParser.IDENTIFIER), "owner"));
				operands.add(this.identifierExpression(
						newSource(singlePlace, CParser.IDENTIFIER), "_tid"));
				int i = 0;
				CompoundStatementNode ifBody;
				LinkedList<BlockItemNode> ifItems = new LinkedList<>();
				for (ASTNode child : node.children()) {
					node.removeChild(i);
					ifItems.add((BlockItemNode) child);
					i++;
				}
				ifBody = nodeFactory.newCompoundStatementNode(
						newSource(singlePlace, CParser.COMPOUND_STATEMENT),
						ifItems);

				IfNode ifStatement = nodeFactory.newIfNode(
						newSource(singlePlace, CParser.IF), nodeFactory
								.newOperatorNode(
										newSource(singlePlace, CParser.EQUALS),
										Operator.EQUALS, operands), ifBody);
				items.add(ifStatement);
				items.add(barrierAndFlush(TEAM));
				body = nodeFactory.newCompoundStatementNode(
						newSource(singlePlace, CParser.COMPOUND_STATEMENT),
						items);

				int index = node.childIndex();
				ASTNode parent = node.parent();
				parent.setChild(index, body);

				for (ASTNode child : children) {
					replaceOMPPragmas(child, privateIDs, sharedIDs,
							reductionIDs, firstPrivateIDs);
				}

			}
		} else if (node instanceof FunctionCallNode) {
			ASTNode parent = node.parent();
			boolean nestedFunctionCall = false;
			boolean replaced = replaceOmpFunction((FunctionCallNode) node);
			//boolean replaced = false;
			if (!replaced) {
				while (!(parent instanceof ExpressionStatementNode)) {
					if (parent instanceof FunctionCallNode) {
						nestedFunctionCall = true;
					}
					if (parent.parent() != null) {
						parent = parent.parent();
					} else {
						break;
					}
				}
				if (nestedFunctionCall) {
					Type currentType = ((FunctionCallNode) node).getType();
					BasicTypeKind baseTypeKind = ((StandardBasicType) currentType)
							.getBasicTypeKind();
					CompoundStatementNode body;
					LinkedList<BlockItemNode> items = new LinkedList<>();
					String tempFuncCall = "tempFuncCall";
					ASTNode nodeDirectParent = node.parent();
					int index = node.childIndex();
					nodeDirectParent.setChild(index,
							this.identifierExpression(newSource(tempFuncCall, CParser.IDENTIFIER), "tempFuncCall"));
					VariableDeclarationNode tempNode = nodeFactory
							.newVariableDeclarationNode(newSource(tempFuncCall, CParser.DECLARATION), nodeFactory
									.newIdentifierNode(newSource(tempFuncCall, CParser.IDENTIFIER), "tempFuncCall"),
									nodeFactory.newBasicTypeNode(newSource(tempFuncCall, CParser.INT),
											baseTypeKind),
									(InitializerNode) node);
					items.add(tempNode);
					nodeDirectParent = parent.parent();
					index = parent.childIndex();
					nodeDirectParent.removeChild(index);
					items.add((BlockItemNode) parent);

					body = nodeFactory.newCompoundStatementNode(newSource(tempFuncCall, CParser.COMPOUND_STATEMENT), items);
					nodeDirectParent.setChild(index, body);

				}

				for (ASTNode child : node.children()) {
					replaceOMPPragmas(child, privateIDs, sharedIDs,
							reductionIDs, firstPrivateIDs);
				}
			}
		} else if (node instanceof IdentifierNode) {
			if (privateIDs != null) {
				for (ASTNode child : privateIDs.children()) {
					IdentifierNode c = ((IdentifierExpressionNode) child)
							.getIdentifier();
					if (c.name().equals(((IdentifierNode) node).name())) {
						((IdentifierNode) node).setName(
								((IdentifierNode) node).name()+"$omp_private");
					}
				}
			}
			if (firstPrivateIDs != null) {
				for (ASTNode child : privateIDs.children()) {
					IdentifierNode c = ((IdentifierExpressionNode) child)
							.getIdentifier();
					if (c.name().equals(((IdentifierNode) node).name())) {
						((IdentifierNode) node).setName(
								((IdentifierNode) node).name()+"$omp_private");
					}
				}
			}
			if (reductionIDs != null) {
				for (ASTNode child : reductionIDs.children()) {
					IdentifierNode c = ((IdentifierExpressionNode) child)
							.getIdentifier();
					if (c.name().equals(((IdentifierNode) node).name())) {
						((IdentifierNode) node).setName(
								((IdentifierNode) node).name()+"$omp_private");
					}
				}
			}
			if (sharedIDs != null
					&& ((IdentifierNode) node).getEntity() != null) {
				for (ASTNode child : sharedIDs.children()) {
					IdentifierNode c = ((IdentifierExpressionNode) child)
							.getIdentifier();
					if (c.name().equals(((IdentifierNode) node).name())) {
						ASTNode parent = getParentOfID((IdentifierNode) node);
						boolean sameName = false;
						//TODO ADD name +"_local"
						for (Triple<String, StatementNode, String> tempName : sharedReplaced) {
							if (tempName.first.equals(((IdentifierNode) node)
									.name())) {
								if (tempName.second.equals(parent)) {
									sameName = true;
									((IdentifierNode) node)
											.setName(tempName.third);
								}
							}
						}
						if (!sameName) {
							sharedRead((IdentifierNode) node,
									(StatementNode) parent, privateIDs,
									sharedIDs, reductionIDs, firstPrivateIDs);
						}

					}
				}
			}

		} else if (node instanceof OperatorNode
				&& sharedIDs != null
				&& isAssignmentOperator(((OperatorNode) node).getOperator()
						.toString())) {
			ArrayList<OperatorNode> assignedToVars = new ArrayList<OperatorNode>();
			assignedToVars.add((OperatorNode) node);
			ASTNode rhs = node.child(1);
			do {
				if (rhs instanceof OperatorNode
						&& isAssignmentOperator(((OperatorNode) node.child(1))
								.getOperator().toString())) {
					assignedToVars.add((OperatorNode) rhs);
					rhs = rhs.child(1);
				} else {
					rhs = null;
				}
			} while (rhs != null);

			ExpressionStatementNode temp = containsSharedWrite(assignedToVars,
					privateIDs, sharedIDs, reductionIDs, firstPrivateIDs,
					((OperatorNode) node).getOperator());
			if (temp == null) {
				replaceOMPPragmas(node.child(0), privateIDs, sharedIDs,
						reductionIDs, firstPrivateIDs);
				replaceOMPPragmas(node.child(1), privateIDs, sharedIDs,
						reductionIDs, firstPrivateIDs);
			} else {

				OperatorNode initializer = (OperatorNode) temp.getExpression();
				while (initializer.child(1) instanceof OperatorNode
						&& isAssignmentOperator(initializer.getOperator()
								.toString())) {
					initializer = (OperatorNode) initializer.child(1);
				}
				replaceOMPPragmas(initializer, privateIDs, sharedIDs,
						reductionIDs, firstPrivateIDs);

			}

		} else if (node != null) {
			Iterable<ASTNode> children = node.children();
			for (ASTNode child : children) {
				replaceOMPPragmas(child, privateIDs, sharedIDs, reductionIDs,
						firstPrivateIDs);
			}
		}

	}

	private boolean replaceOmpFunction(FunctionCallNode node) throws SyntaxException {
		ExpressionNode function = node.getFunction();
		String functionName = null;
		if (function instanceof IdentifierExpressionNode) {
			functionName = ((IdentifierExpressionNode) function)
					.getIdentifier().name();
		}
		ASTNode replacementNode = null;
		String place;
		switch (functionName) {
		case "omp_get_num_threads":
			place = "omp_get_num_threads->_nthreads";
			replacementNode = this.identifierExpression(newSource(place, CParser.IDENTIFIER), NTHREADS);
			break;
		case "omp_get_thread_num":
			place = "omp_get_thread_num->_tid";
			replacementNode = this.identifierExpression(newSource(place, CParser.IDENTIFIER), TID);
			break;
		case "omp_get_wtime":
			place = "omp_get_wtime->time(NULL)";
			
			ExpressionNode nullNode = nodeFactory.newCastNode(newSource(place, CParser.CAST), 
					nodeFactory.newPointerTypeNode(newSource(place, CParser.POINTER), 
							nodeFactory.newVoidTypeNode(newSource(place, CParser.VOID))), 
							nodeFactory.newIntegerConstantNode(newSource(place, CParser.INTEGER_CONSTANT), "0"));
			
			replacementNode = nodeFactory.newFunctionCallNode(newSource(place, CParser.CALL), 
					this.identifierExpression(newSource(place, 
							CParser.IDENTIFIER), "time"),
							Arrays.asList(nullNode), null);
			break;
		}
		
			
			

		if (replacementNode != null) {
			ASTNode parent = node.parent();
			int index = node.childIndex();
			parent.setChild(index, replacementNode);
			return true;
		}

		return false;
	}

	private boolean isAssignmentOperator(String operator) {
		if (operator.equals("ASSIGN")) {
			return true;
		} else if (operator.equals("PLUSEQ")) {
			return true;
		}
		return false;
	}

	private VariableDeclarationNode addPrivateVariable(
			IdentifierExpressionNode node, String privateKind)
			throws SyntaxException {
		VariableDeclarationNode privateVariable;
		
		IdentifierNode c = node.getIdentifier();
		String place = c.name() + "$omp_privateDeclaration";
		TypeNode privateType = ((VariableDeclarationNode) ((Variable) c
				.getEntity()).getFirstDeclaration()).getTypeNode().copy();
		IdentifierNode privateIdentifer = nodeFactory.newIdentifierNode(newSource(place, CParser.IDENTIFIER),
				c.name() + "$omp_private");
		if (privateKind.equals("first")) {
			privateVariable = nodeFactory.newVariableDeclarationNode(newSource(place, CParser.DECLARATION),
					privateIdentifer, privateType,
					this.identifierExpression(source, c.name()));
		} else if (privateKind.equals("reduction")) {
			privateVariable = nodeFactory.newVariableDeclarationNode(newSource(place, CParser.DECLARATION),
					privateIdentifer, privateType,
					nodeFactory.newIntegerConstantNode(source, "0"));
		} else {
			privateVariable = nodeFactory.newVariableDeclarationNode(newSource(place, CParser.DECLARATION),
					privateIdentifer, privateType);
		}
		return privateVariable;
	}

	private ASTNode getParentOfID(IdentifierNode node) {
		ASTNode parent = node.parent();
		while (!(parent instanceof StatementNode)) {
			parent = parent.parent();
		}
		return parent;
	}
	
	private boolean checkIfParentIsFunction(IdentifierNode node){
		boolean parentFunction = false;
		ASTNode parent = node.parent();
		
		while(!(parent instanceof StatementNode)){
			if(parent instanceof FunctionCallNode){
				parentFunction=true;
			}
			parent = parent.parent();
		}
		
		return parentFunction;
	}
	
	private void removeNodeFromParent(ASTNode node){
		if(node != null){
			int index = node.childIndex();
			ASTNode parent = node.parent();
			parent.removeChild(index);
		}
		
	}

	private CompoundStatementNode sharedRead(IdentifierNode node,
			StatementNode parentStatement,
			SequenceNode<IdentifierExpressionNode> privateIDs,
			SequenceNode<IdentifierExpressionNode> sharedIDs,
			SequenceNode<IdentifierExpressionNode> reductionIDs,
			SequenceNode<IdentifierExpressionNode> firstPrivateIDs)
			throws SyntaxException, NoSuchElementException {
		List<BlockItemNode> items = new LinkedList<>();
		CompoundStatementNode bodyRead;

		VariableDeclarationNode temp;
		Type currentType = ((Variable) node.getEntity()).getType();
		int nodesDeep = 0;
		boolean pointer = false;
		if(currentType instanceof PointerType){
			currentType = ((PointerType) currentType).referencedType();
			nodesDeep++;
			pointer = true;
		}
		while (currentType instanceof ArrayType) {
			currentType = ((ArrayType) currentType).getElementType();
			nodesDeep++;
		}
		String place = node.name() + "SharedRead";
		BasicTypeKind baseTypeKind = ((StandardBasicType) currentType)
				.getBasicTypeKind();
		IdentifierNode tempID = nodeFactory.newIdentifierNode(newSource(place, CParser.IDENTIFIER), "tmpRead"
				+ String.valueOf(tmpCount));
		TypeNode declarationTypeNode = null;
		
		boolean parentIsFunctionCall = false;
		if(pointer){
			parentIsFunctionCall = checkIfParentIsFunction(node);
		}
		
		if(parentIsFunctionCall){
			declarationTypeNode = nodeFactory.newPointerTypeNode(source, nodeFactory.newBasicTypeNode(newSource(place, CParser.TYPE), baseTypeKind));
		} else {
			declarationTypeNode = nodeFactory.newBasicTypeNode(newSource(place, CParser.TYPE), baseTypeKind);
		}
		
		temp = nodeFactory.newVariableDeclarationNode(newSource(place, CParser.DECLARATION), tempID,
				declarationTypeNode);

		items.add(temp);
		ASTNode parentOfID = null;
		String nodeName = node.name();
		node.setName(node.name() + "_local");
		int tempCount = tmpCount;
		tmpCount++;
		if (nodesDeep == 0) {
			parentOfID = node.parent();
			items.add(read((ExpressionNode) parentOfID.copy(), nodeName, "tmpRead"
					+ String.valueOf(tempCount)));
		} else {
			parentOfID = node.parent();
			while (nodesDeep > 0) {
				parentOfID = parentOfID.parent();
				nodesDeep--;
			}

			if (parentOfID instanceof OperatorNode) {
				checkArrayIndices((OperatorNode) parentOfID, privateIDs,
						sharedIDs, reductionIDs, firstPrivateIDs);
			}
			items.add(read((ExpressionNode) parentOfID.copy(), nodeName, "tmpRead"
					+ String.valueOf(tempCount)));
		}

		String origName = node.name();
		ASTNode tempParent = parentOfID.parent();
		int nodeIndex = parentOfID.childIndex();
		tempParent.setChild(
				nodeIndex,
				this.identifierExpression(newSource(place, CParser.IDENTIFIER),
						"tmpRead" + String.valueOf(tempCount)));

		Triple<String, StatementNode, String> tempTriple = new Triple<>(
				origName, parentStatement, "tmpRead" + String.valueOf(tempCount));
		sharedReplaced.add(tempTriple);
		//tmpCount++;
		int index = parentStatement.childIndex();
		ASTNode parent = parentStatement.parent();
		parent.removeChild(index);
		items.add(parentStatement);

		bodyRead = nodeFactory.newCompoundStatementNode(newSource(place, CParser.COMPOUND_STATEMENT), items);
		parent.setChild(index, bodyRead);
		return bodyRead;
	}

	private ExpressionStatementNode containsSharedWrite(
			ArrayList<OperatorNode> assignedToVars,
			SequenceNode<IdentifierExpressionNode> privateIDs,
			SequenceNode<IdentifierExpressionNode> sharedIDs,
			SequenceNode<IdentifierExpressionNode> reductionIDs,
			SequenceNode<IdentifierExpressionNode> firstPrivateIDs,
			Operator opType) throws SyntaxException, NoSuchElementException {

		ArrayList<IdentifierNode> nodeID = new ArrayList<IdentifierNode>();
		for (OperatorNode op : assignedToVars) {
			ASTNode nodeChild0 = op.child(0);
			if (nodeChild0 instanceof IdentifierExpressionNode) {
				nodeID.add(((IdentifierExpressionNode) nodeChild0)
						.getIdentifier());
			} else if (op.child(0) instanceof OperatorNode) {
				while (nodeChild0 instanceof OperatorNode) {
					nodeChild0 = nodeChild0.child(0);
				}
				nodeID.add(((IdentifierExpressionNode) nodeChild0)
						.getIdentifier());
			}
		}
		List<BlockItemNode> items = new LinkedList<>();
		int nodesDeep = 0;
		int count = 0;
		int j = assignedToVars.size();
		ArrayList<VariableDeclarationNode> tempVarsAdded = new ArrayList<VariableDeclarationNode>();
		VariableDeclarationNode temp = null;
		ExpressionNode initializer = (ExpressionNode) assignedToVars.get(j - 1)
				.removeChild(1);
		for (IdentifierNode currentNodeID : nodeID) {
			for (ASTNode child : sharedIDs.children()) {
				IdentifierNode c = ((IdentifierExpressionNode) child)
						.getIdentifier();
				if (c.name().equals(currentNodeID.name())) {
					Type currentType = ((Variable) currentNodeID.getEntity())
							.getType();
					nodesDeep = 0;
					if(currentType instanceof PointerType){
						currentType = ((PointerType) currentType).referencedType();
						nodesDeep++;
					}
					while (currentType instanceof ArrayType) {
						currentType = ((ArrayType) currentType)
								.getElementType();
						nodesDeep++;
					}
					String place = currentNodeID.name() + "sharedWrite";
					BasicTypeKind baseTypeKind = ((StandardBasicType) currentType)
							.getBasicTypeKind();
					IdentifierNode tempID = nodeFactory.newIdentifierNode(
							newSource(place, CParser.IDENTIFIER), "tmpWrite" + String.valueOf(tempWriteCount + count));

					temp = nodeFactory.newVariableDeclarationNode(newSource(place, CParser.DECLARATION),
							tempID,
							nodeFactory.newBasicTypeNode(newSource(place, CParser.TYPE), baseTypeKind));
					tempVarsAdded.add(temp);
					items.add(temp);
					count++;
				}
			}
		}
		ExpressionStatementNode assignmentOfTemp = null;
		if (!items.isEmpty()) {
			count = 0;
			OperatorNode currentOp = null;
			for (int i = nodeID.size(); i > 0; i--) {
				String place = nodeID.get(i-1).name() + "sharedWrite";
				if (i == nodeID.size()) {
					if (opType.equals(Operator.ASSIGN)) {
						currentOp = nodeFactory.newOperatorNode(newSource(place, CParser.ASSIGN),
								Operator.ASSIGN, Arrays.asList(
										this.identifierExpression(
												newSource(place, CParser.IDENTIFIER),
												"tmpWrite"
														+ String.valueOf(tempWriteCount
																+ count)),
																initializer));
					} else {
						currentOp = nodeFactory
								.newOperatorNode(
										newSource(place, CParser.OPERATOR),
										opType,
										Arrays.asList(
												this.identifierExpression(
														newSource(place, CParser.IDENTIFIER),
														"tmpWrite"
																+ String.valueOf(tempWriteCount
																		+ count)),
												initializer));
						ASTNode parentOfID = null;
						IdentifierNode idNode = (IdentifierNode) nodeID.get(0);
						String nodeName = idNode.name();
						idNode.setName(idNode.name() + "_local");
						int k = nodesDeep;
						if (k == 0) {
							parentOfID = idNode.parent();
							items.add(read((ExpressionNode) parentOfID.copy(),
									nodeName, "tmpRead" + String.valueOf(tmpCount)));
						} else {
							parentOfID = idNode.parent();
							while (k > 0) {
								parentOfID = parentOfID.parent();
								k--;
							}

							if (parentOfID instanceof OperatorNode) {
								checkArrayIndices((OperatorNode) parentOfID,
										privateIDs, sharedIDs, reductionIDs,
										firstPrivateIDs);
							}
							items.add(read((ExpressionNode) parentOfID.copy(),
									nodeName, "tmpWrite" + String.valueOf(tempWriteCount)));
							idNode.setName(nodeName);
						}
					}

				} else {
					currentOp = nodeFactory
							.newOperatorNode(newSource(place, CParser.ASSIGN), Operator.ASSIGN, Arrays
									.asList(this.identifierExpression(
											newSource(place, CParser.IDENTIFIER),
											"tmpWrite"
													+ String.valueOf(tempWriteCount
															+ count)),
											currentOp));
				}
				count++;
			}
			assignmentOfTemp = nodeFactory
					.newExpressionStatementNode(currentOp);
			items.add(assignmentOfTemp);
		}

		for (IdentifierNode currentNodeID : nodeID) {
			for (ASTNode child : sharedIDs.children()) {
				IdentifierNode c = ((IdentifierExpressionNode) child)
						.getIdentifier();
				if (c.name().equals(currentNodeID.name())
						|| c.name().equals(currentNodeID.name() + "_local")) {

					ASTNode parentOfID = null;
					String nodeName = currentNodeID.name();
					if (!currentNodeID.name().equals(c.name() + "_local")) {
						currentNodeID.setName(currentNodeID.name() + "_local");
					}
					if (nodesDeep == 0) {
						parentOfID = currentNodeID.parent();
						items.add(write((ExpressionNode) parentOfID.copy(),
								nodeName));
					} else {
						parentOfID = currentNodeID.parent();
						while (nodesDeep > 0) {
							parentOfID = parentOfID.parent();
							nodesDeep--;
						}

						if (parentOfID instanceof OperatorNode) {
							checkArrayIndices((OperatorNode) parentOfID,
									privateIDs, sharedIDs, reductionIDs,
									firstPrivateIDs);
						}
						items.add(write((ExpressionNode) parentOfID.copy(),
								nodeName));
					}
					tempWriteCount++;

				}
			}
		}
		if (!items.isEmpty()) {
			CompoundStatementNode bodyWrite;
			String place = "sharedWrite";
			bodyWrite = nodeFactory.newCompoundStatementNode(newSource(place, CParser.COMPOUND_STATEMENT), items);
			ASTNode expNode = assignedToVars.get(0).parent();
			int index = expNode.childIndex();
			ASTNode parent = expNode.parent();
			parent.setChild(index, bodyWrite);
		}
		if (items.isEmpty() && initializer != null) {
			assignedToVars.get(j - 1).setChild(1, initializer);
		}
		return assignmentOfTemp;
	}

	private void checkArrayIndices(OperatorNode op,
			SequenceNode<IdentifierExpressionNode> privateIDs,
			SequenceNode<IdentifierExpressionNode> sharedIDs,
			SequenceNode<IdentifierExpressionNode> reductionIDs,
			SequenceNode<IdentifierExpressionNode> firstPrivateIDs)
			throws SyntaxException, NoSuchElementException {
		replaceOMPPragmas(op.child(1), privateIDs, sharedIDs, reductionIDs,
				firstPrivateIDs);
		ASTNode lhs = op.child(0);
		while (lhs instanceof OperatorNode) {
			replaceOMPPragmas(lhs.child(1), privateIDs, sharedIDs,
					reductionIDs, firstPrivateIDs);
			lhs = lhs.child(0);
		}
	}

	/**
	 * 
	 * Adds appropriate headers to the program
	 * 
	 * 
	 * @param root
	 *            The root node of the AST of the original OpenMP program.
	 * @return The function definition node of OpenMP, the list of AST nodes
	 *         that are parsed from header files and will be moved up to the
	 *         higher scope (i.e., the file scope of the final AST), and the
	 *         list of variable declaration nodes.
	 */
	private Triple<List<ExternalDefinitionNode>, List<ExternalDefinitionNode>, List<VariableDeclarationNode>> program(
			SequenceNode<ExternalDefinitionNode> root) {
		List<ExternalDefinitionNode> includedNodes = new ArrayList<>();
		List<VariableDeclarationNode> vars = new ArrayList<>();
		List<ExternalDefinitionNode> items;
		int number;
		items = new LinkedList<>();
		number = root.numChildren();
		for (int i = 0; i < number; i++) {
			ExternalDefinitionNode child = root.getSequenceChild(i);
			String sourceFile;

			if (child == null)
				continue;
			sourceFile = child.getSource().getFirstToken().getSourceFile()
					.getName();
			root.removeChild(i);
			if (sourceFile.equals("omp.cvl")) {
				includedNodes.add(child);
			} else if (sourceFile.endsWith(".cvh")
					|| sourceFile.equals("comm.cvl")
					|| sourceFile.equals("civlmpi.cvl")
					|| sourceFile.equals("omp.cvl")
					|| sourceFile.equals("civlc.cvl")
					|| sourceFile.equals("concurrency.cvl")
					|| sourceFile.equals("stdio.cvl")
					|| sourceFile.equals("pthread.cvl")
					|| sourceFile.equals("string.cvl"))
				includedNodes.add(child);
			else if (sourceFile.endsWith(".h")) {
				if (child.nodeKind() == NodeKind.VARIABLE_DECLARATION) {
					VariableDeclarationNode variableDeclaration = (VariableDeclarationNode) child;
					if (sourceFile.equals("stdio.h"))
						// keep variable declaration nodes from stdio, i.e.,
						// stdout, stdin, etc.
						items.add(variableDeclaration);
					else
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
				items.add(child);
			}
		}
		return new Triple<>(items, includedNodes, vars);
	}

	/**
	 * Extracting bounds and step for ranges from a Canonical ForLoopNode
	 * 
	 * @param forloopNode
	 *            The AST Node represents a canonical for loop. (Pre-condition:
	 *            The for loop should be more strictly a canonical for loop.)
	 * @return A triple of lower bound , higher bound and step
	 * @throws SyntaxException
	 */
	private Triple<ASTNode, ASTNode, ASTNode> canonicalForLoopBounds(
			ForLoopNode forloopNode) throws SyntaxException {
		IdentifierNode loopVar;
		ForLoopInitializerNode initializer;
		IdentifierExpressionNode identExpr;
		ExpressionNode conditioner;
		ExpressionNode incrementor;
		OperatorNode incNode;
		ASTNode lo = null;
		ASTNode hi = null;
		ASTNode step = null;
		ASTNode one = nodeFactory.newIntegerConstantNode((Source) null, "1");
		ASTNode zero = nodeFactory.newIntegerConstantNode((Source) null, "0");
		ASTNode newHi;
		Operator incOp, stepSign;
		boolean isNegativeIncrement = false;

		initializer = forloopNode.getInitializer();
		conditioner = forloopNode.getCondition();
		incrementor = forloopNode.getIncrementer();
		// Initializer can either be a OperatorNode or a DeclarationListNode:
		// for(i = 0;...) or for(int i = 0; ...)
		if (initializer instanceof OperatorNode) {
			// OperatorNode means it's an assignment statement e.g. i = 0;
			// So the initial value is child(1).
			lo = initializer.child(1);
			// Obtain the identifierNode of the loop variable which will be used
			// to distinguish between a loop variable and a regular variable in
			// termination and incrementor.
			loopVar = ((IdentifierExpressionNode) initializer.child(0))
					.getIdentifier();
		} else if (initializer instanceof DeclarationListNode) {
			VariableDeclarationNode varDecNode = (VariableDeclarationNode) initializer
					.child(0);

			// The initializer has a similar form as "int i = 0"
			lo = varDecNode.getInitializer();
			loopVar = (IdentifierNode) varDecNode.getIdentifier();
		} else {
			throw new CIVLSyntaxException("OpenMP for loop initializer");
		}
		// The bound value in conditioner can either be in the left hand side or
		// right hand side of a comparison operator(GT, LT, GTE, LTE).
		if (conditioner instanceof OperatorNode) {
			if (conditioner.child(0) instanceof IdentifierExpressionNode) {
				identExpr = (IdentifierExpressionNode) conditioner.child(0);

				if (identExpr.getIdentifier().getEntity()
						.equals(loopVar.getEntity()))
					hi = conditioner.child(1);
			}
			if (hi == null
					&& conditioner.child(1) instanceof IdentifierExpressionNode) {
				identExpr = (IdentifierExpressionNode) conditioner.child(1);

				if (identExpr.getIdentifier().getEntity()
						.equals(loopVar.getEntity()))
					hi = conditioner.child(0);
			}
		}
		if (hi == null)
			throw new CIVLSyntaxException("OpenMP for loop conditioner");
		// Incrementor has three basic forms. e.g. i++ (--), i+=1, i = i + 1,
		// what in common is all of them are operatorNodes.
		assert incrementor instanceof OperatorNode;
		incNode = (OperatorNode) incrementor;
		incOp = incNode.getOperator();
		if (incNode.getOperator() == Operator.ASSIGN) {
			assert incrementor.child(1) instanceof OperatorNode : "OpenMP for loop incrementor";
			incNode = (OperatorNode) incrementor.child(1);
			incOp = incNode.getOperator();
			if (incNode.child(0) instanceof IdentifierExpressionNode) {
				if (((IdentifierExpressionNode) incNode.child(0))
						.getIdentifier().getEntity()
						.equals(loopVar.getEntity()))
					step = incNode.child(1);
			} else
				step = incNode.child(0);

		} else {
			if (incNode.numChildren() == 1)
				step = nodeFactory.newIntegerConstantNode(
						incrementor.getSource(), "1");
			else if (incNode.numChildren() == 2)
				step = incNode.child(1);
		}
		// If the incrementor increase the loop variable or decrease it.
		if (incOp == Operator.POSTDECREMENT || incOp == Operator.MINUSEQ
				|| incOp == Operator.MINUS) {
			step = nodeFactory.newOperatorNode(incrementor.getSource(),
					Operator.MINUS, Arrays.asList((ExpressionNode) zero.copy(),
							(ExpressionNode) step.copy()));
			isNegativeIncrement = true;
		}
		newHi = nodeFactory.newOperatorNode(
				source,
				Operator.PLUS,
				Arrays.asList((ExpressionNode) lo.copy(),
						(ExpressionNode) hi.copy()));
		// If the termination bound is open, change the bound by one. (Since
		// canonical only accept integer loop variable, it's safe)
		if (((OperatorNode) conditioner).getOperator() == Operator.LT
				|| ((OperatorNode) conditioner).getOperator() == Operator.GT) {
			if (isNegativeIncrement) {
				stepSign = Operator.PLUS;
			} else
				stepSign = Operator.MINUS;
			newHi = nodeFactory.newOperatorNode(source, stepSign, Arrays
					.asList((ExpressionNode) hi.copy(),
							(ExpressionNode) one.copy()));
		} else
			newHi = hi.copy();
		// Since $range will always put the larger value at the right hand side
		// of the "..", so if the operator of the incrementor is -- -= or -, we
		// have to switch the high bound and the lower bound.
		if (isNegativeIncrement) {
			ASTNode temp = newHi;

			newHi = lo;
			lo = temp;
		}
		return new Triple<>(lo, newHi, step);
	}
}
