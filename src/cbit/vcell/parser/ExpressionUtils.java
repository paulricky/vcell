package cbit.vcell.parser;

import java.util.Random;
/**
 * Insert the type's description here.
 * Creation date: (12/27/2002 1:37:29 PM)
 * @author: Jim Schaff
 */
public class ExpressionUtils {
/**
 * Insert the method's description here.
 * Creation date: (12/22/2002 3:41:17 PM)
 * @return int
 * @param node cbit.vcell.parser.SimpleNode
 */
private static SimpleNode createNode(java.util.Random random, boolean bIsConstraint) {
	final int AddNode = 0;
	final int AndNode = 1;
	final int FloatNode = 2;
	final int FuncNode = 3;
	final int IDNode = 4;
	final int InvertNode = 5;
	final int MinusNode = 6;
	final int MultNode = 7;
	final int NotNode = 8;
	final int OrNode = 9;
	final int PowerNode = 10;
	final int RelationalNode = 11;

	final double nodeProbabilityNormal[] = {
		0.5,	// ADD
		0.25,	// AND
		0.5,	// FLOAT
		10.0,	// FUNCTION
		0.2,	// ID
		1.0,	// INVERT
		1.0,	// MINUS
		2.0,	// MULT
		0.3,	// NOT
		0.3,	// OR
		1,		// POWER
		0.3,	// RELATIONAL
	};
	final double nodeProbabilityConstraint[] = {
		2.0,	// ADD
		0.0,	// AND
		2.0,	// FLOAT
		1.0,	// FUNCTION
		1.0,	// ID
		1.0,	// INVERT
		1.0,	// MINUS
		2.0,	// MULT
		0.0,	// NOT
		0.0,	// OR
		1,		// POWER
		0.0,	// RELATIONAL
	};
		
	final int[] functionIDs = {
		ASTFuncNode.ABS,
		ASTFuncNode.ACOS,
		ASTFuncNode.ASIN,
		ASTFuncNode.ATAN,
		ASTFuncNode.ATAN2,
		ASTFuncNode.COS,
		ASTFuncNode.EXP,
		ASTFuncNode.LOG,
		ASTFuncNode.MAX,
		ASTFuncNode.MIN,
		ASTFuncNode.POW,
		ASTFuncNode.SIN,
		ASTFuncNode.SQRT,
		ASTFuncNode.TAN,
		ASTFuncNode.CEIL,
		ASTFuncNode.FLOOR,
		ASTFuncNode.CSC,
		ASTFuncNode.COT,
		ASTFuncNode.SEC,
		ASTFuncNode.ACSC,
		ASTFuncNode.ACOT,
		ASTFuncNode.ASEC,
		ASTFuncNode.SINH,
		ASTFuncNode.COSH,
		ASTFuncNode.TANH,
		ASTFuncNode.CSCH,
		ASTFuncNode.COTH,
		ASTFuncNode.SECH,
		ASTFuncNode.ASINH,
		ASTFuncNode.ACOSH,
		ASTFuncNode.ATANH,
		ASTFuncNode.ACSCH,
		ASTFuncNode.ACOTH,
		ASTFuncNode.ASECH,
		ASTFuncNode.FACTORIAL
	};
	double nodeProbability[] = null;
	if (bIsConstraint){
		nodeProbability = nodeProbabilityConstraint;
	}else{
		nodeProbability = nodeProbabilityNormal;
	}
	double totalProb = 0;
	for (int i = 0; i < nodeProbability.length; i++){
		totalProb += nodeProbability[i];
	}
	double f = random.nextDouble()*totalProb;
	int nodeChoice = -1;
	double cumulativeProb = 0;
	for (int i = 0; i < nodeProbability.length; i++){
		cumulativeProb += nodeProbability[i];
		if (f < cumulativeProb){
			nodeChoice = i;
			break;
		}
	}
	switch (nodeChoice){
		case AddNode: {
			return new ASTAddNode();
		}
		case AndNode: {
			return new ASTAndNode();
		}
		case FloatNode: {
			if (random.nextFloat() > 0.1){
				return new ASTFloatNode(random.nextDouble());
			}else{
				return new ASTFloatNode(0.0);
			}
		}
		case FuncNode: {
			ASTFuncNode fn = new ASTFuncNode();
			double ftype = random.nextDouble();
			int index = (int)Math.min(functionIDs.length-1,Math.floor(functionIDs.length*ftype));
			fn.setFunction(functionIDs[index]);
			return fn;
		}
		case IDNode: {
			ASTIdNode idNode = new ASTIdNode();
			idNode.name = "id_"+((int)Math.abs(random.nextInt())%10);
			return idNode;
		}
		case InvertNode: {
			return new ASTInvertTermNode();
		}
		case MinusNode: {
			return new ASTMinusTermNode();
		}
		case MultNode: {
			return new ASTMultNode();
		}
		case NotNode: {
			return new ASTNotNode();
		}
		case OrNode: {
			return new ASTOrNode();
		}
		case PowerNode: {
			return new ASTPowerNode();
		}
		case RelationalNode: {
			ASTRelationalNode relNode = new ASTRelationalNode();
			double rtype = random.nextDouble();
			final String ops[] = {
				">",
				"<",
				"<=",
				">=",
				"==",
				"!="
			};
			relNode.setOperationFromToken(ops[(int)Math.floor(rtype*6)]);
			return relNode;
		}
		default: {
			throw new RuntimeException("f = "+f+", couldn't create a node");
		}
	}
}
/**
 * Insert the method's description here.
 * Creation date: (12/22/2002 4:23:34 PM)
 * @return cbit.vcell.parser.SimpleNode
 */
private static SimpleNode createTerminalNode(java.util.Random random, boolean bIsConstraint) {
	double choice = random.nextDouble();
	double percentIdentifiers = (bIsConstraint)?(0.8):(0.5);

	if (choice < percentIdentifiers){
		ASTIdNode idNode = new ASTIdNode();
		idNode.name = "id_"+((int)Math.abs(random.nextInt())%10);
		return idNode;
	}else if (choice > 0.95){
		return new ASTFloatNode(0.0);
	}else{
		return new ASTFloatNode(random.nextDouble());
	}
}
/**
 * Insert the method's description here.
 * Creation date: (10/17/2002 12:42:18 AM)
 * @return boolean
 * @param exp1 cbit.vcell.parser.Expression
 * @param exp2 cbit.vcell.parser.Expression
 */
public static boolean derivativeFunctionallyEquivalent(Expression exp, String diffSymbol, Expression diff, double relativeTolerance, double absoluteTolerance) {
	try {
		String symbolsExp[] = exp.getSymbols();
		String symbolsDiff[] = diff.getSymbols();
		boolean bHasDiffSymbol = false;
		for (int i = 0; symbolsExp!=null && i < symbolsExp.length; i++){
			if (symbolsExp[i].equals(diffSymbol)){
				bHasDiffSymbol = true;
			}
		}
		if (!bHasDiffSymbol){
			//
			// if expression doesn't reference 'diffSymbol', then derivative must be 0.
			//
			if (diff.isZero()){
				return true;
			}else{
				return false;
			}
		}
		
		String symbols[] = null;
		if (symbolsDiff==null){
			symbols = symbolsExp;
		}else{
			//
			// make combined list (without duplicates)
			//
			java.util.HashSet hashSet = new java.util.HashSet();
			for (int i = 0; i < symbolsExp.length; i++){
				hashSet.add(symbolsExp[i]);
			}
			for (int i = 0; i < symbolsDiff.length; i++){
				hashSet.add(symbolsDiff[i]);
			}
			symbols = (String[])hashSet.toArray(new String[hashSet.size()]);
		}

		cbit.vcell.parser.SymbolTable symbolTable = new SimpleSymbolTable(symbols);
		exp.bindExpression(symbolTable);
		diff.bindExpression(symbolTable);
		int diffSymbolIndex = exp.getSymbolBinding(diffSymbol).getIndex();
		double values[] = new double[symbols.length];
		//
		// go through 20 different sets of random inputs to test for equivalence
		// ignore cases of Exceptions during evaluations (out of domain of imbedded functions), keep trying until 20 successful evaluations
		//
		Random rand = new Random();
		final int MAX_TRIES = 1000;
		final int REQUIRED_NUM_EVALUATIONS = 20;
		int numEvaluations = 0;
		Exception savedException = null;
		for (int i = 0; i < MAX_TRIES && numEvaluations < REQUIRED_NUM_EVALUATIONS; i++){
			for (int j = 0; j < values.length; j++){
				values[j] = i*rand.nextGaussian();
			}
			try {
				//
				// evalutate "exact" differential
				//
				double resultDiff = diff.evaluateVector(values);

				//
				// central difference approximation from expression w.r.t "P" (diffSymbol)
				// choose "delta" about 100 times greater than error.
				//
				double nominalSymbolValue = values[diffSymbolIndex];
				double deltaP = Math.max(Math.abs(nominalSymbolValue*10*relativeTolerance),absoluteTolerance*10);
				double lowSymbolValue = nominalSymbolValue - deltaP;
				double highSymbolValue = nominalSymbolValue + deltaP;
				values[diffSymbolIndex] = lowSymbolValue;
				double result_low = exp.evaluateVector(values);
				values[diffSymbolIndex] = highSymbolValue;
				double result_high = exp.evaluateVector(values);
				if (Double.isInfinite(resultDiff) || Double.isNaN(resultDiff) || Math.abs(resultDiff)>1e10){
					throw new RuntimeException("diff = '"+diff+"' evaluates to "+resultDiff);
				}
				if (Double.isInfinite(result_low) || Double.isNaN(result_low)){
					throw new RuntimeException("low("+exp+") = "+result_low);
				}
				if (Double.isInfinite(result_high) || Double.isNaN(result_high)){
					throw new RuntimeException("high("+exp+") = "+result_high);
				}
				double resultCentralDifference = (result_high-result_low)/(2*deltaP);
				double scale = Math.abs(resultDiff)+Math.abs(resultCentralDifference);
				double absdiff = Math.abs(resultDiff-resultCentralDifference);
				if (scale > absoluteTolerance){ // if scale < absoluteTolerance, they are both close enough to zero.
					if (absdiff > relativeTolerance*10*scale){
						System.out.println("ExpressionUtils.derivativeFunctionallyEquivalent() 'exact' = "+resultDiff+", approx = "+resultCentralDifference+", absDiff = "+absdiff+", f_low = "+result_low+", f_high = "+result_high+", "+diffSymbol+" = "+nominalSymbolValue+" +/- "+deltaP);
						return false;
					}
				}
				numEvaluations++;
			}catch (Exception e){
				savedException = e;
			}
		}
		if (numEvaluations < REQUIRED_NUM_EVALUATIONS){
			savedException.printStackTrace(System.out);
			throw new RuntimeException("too many failed evaluations ("+numEvaluations+" of "+REQUIRED_NUM_EVALUATIONS+") ("+savedException.getMessage()+")");
		}
		return true;
	}catch (cbit.vcell.parser.ExpressionException e){
		e.printStackTrace(System.out);
		return false;
	}
}
/**
 * Insert the method's description here.
 * Creation date: (10/17/2002 12:42:18 AM)
 * @return boolean
 * @param exp1 cbit.vcell.parser.Expression
 * @param exp2 cbit.vcell.parser.Expression
 */
public static boolean functionallyEquivalent(Expression exp1, Expression exp2) {
	boolean verifySameSymbols = true;
	return functionallyEquivalent(exp1,exp2,verifySameSymbols);
}
/**
 * Insert the method's description here.
 * Creation date: (10/17/2002 12:42:18 AM)
 * @return boolean
 * @param exp1 cbit.vcell.parser.Expression
 * @param exp2 cbit.vcell.parser.Expression
 */
public static boolean functionallyEquivalent(Expression exp1, Expression exp2, boolean bVerifySameSymbols) {
	double defaultAbsoluteTolerance = 1e-12;
	double defaultRelativeTolerance = 1e-10;
	return functionallyEquivalent(exp1,exp2,bVerifySameSymbols,defaultRelativeTolerance,defaultAbsoluteTolerance);
}
/**
 * Insert the method's description here.
 * Creation date: (10/17/2002 12:42:18 AM)
 * @return boolean
 * @param exp1 cbit.vcell.parser.Expression
 * @param exp2 cbit.vcell.parser.Expression
 */
public static boolean functionallyEquivalent(Expression exp1, Expression exp2, boolean verifySameSymbols, double relativeTolerance, double absoluteTolerance) {
	try {
		String symbols1[] = exp1.getSymbols();
		String symbols2[] = exp2.getSymbols();
		if (symbols1==null && symbols2==null){
			//
			// compare solutions
			//
			try {
				double result1 = exp1.evaluateConstant();
				double result2 = exp2.evaluateConstant();
				if (Double.isInfinite(result1) || Double.isNaN(result1)){
					throw new RuntimeException("exp1 = '"+exp1+"' evaluates to "+result1);
				}
				if (Double.isInfinite(result2) || Double.isNaN(result2)){
					throw new RuntimeException("exp1 = '"+exp2+"' evaluates to "+result2);
				}
				double scale = Math.abs(result1)+Math.abs(result2);
				double absdiff = Math.abs(result1-result2);
				if (scale > absoluteTolerance){
					if (absdiff > relativeTolerance*scale){
						System.out.println("EXPRESSIONS DIFFERENT: no symbols, delta eval: "+(result2-result1));
						return false;
					}
					else {
						return true;
					}
				} else {
					return true;
				}
			}catch (Throwable e){
				throw new RuntimeException("unexpected exception ("+e.getMessage()+") while evaluating functional equivalence");
			}
		}
		String symbols[] = null;
		if (verifySameSymbols){
			if (symbols1==null || symbols2==null){
				System.out.println("EXPRESSIONS DIFFERENT: symbols null");
				return false;
			}
			if (symbols1.length!=symbols2.length){
				System.out.println("EXPRESSIONS DIFFERENT: symbols different number");
				return false;
			}
			//
			// make sure every symbol in symbol1 is in symbol2
			//
			for (int i = 0; i < symbols1.length; i++){
				boolean bFound = false;
				for (int j = 0; j < symbols2.length; j++){
					if (symbols1[i].equals(symbols2[j])){
						bFound = true;
						break;
					}
				}
				if (!bFound){
					System.out.println("EXPRESSIONS DIFFERENT: symbols don't match");
					return false;
				}
			}
			//
			// make sure every symbol in symbol2 is in symbol1
			//
			for (int i = 0; i < symbols2.length; i++){
				boolean bFound = false;
				for (int j = 0; j < symbols1.length; j++){
					if (symbols2[i].equals(symbols1[j])){
						bFound = true;
						break;
					}
				}
				if (!bFound){
					System.out.println("EXPRESSIONS DIFFERENT: symbols don't match");
					return false;
				}
			}
			symbols = symbols1;
		}else{ // don't verify symbols
			if (symbols1==null && symbols2!=null){
				symbols = symbols2;
			}else if (symbols1!=null && symbols2==null){
				symbols = symbols1;
			}else{
				//
				// make combined list (without duplicates)
				//
				java.util.HashSet hashSet = new java.util.HashSet();
				for (int i = 0; i < symbols1.length; i++){
					hashSet.add(symbols1[i]);
				}
				for (int i = 0; i < symbols2.length; i++){
					hashSet.add(symbols2[i]);
				}
				symbols = (String[])hashSet.toArray(new String[hashSet.size()]);
			}
		}
		cbit.vcell.parser.SymbolTable symbolTable = new SimpleSymbolTable(symbols);
		exp1.bindExpression(symbolTable);
		exp2.bindExpression(symbolTable);
		double values[] = new double[symbols.length];
		//
		// go through 20 different sets of random inputs to test for equivalence
		// ignore cases of Exceptions during evaluations (out of domain of imbedded functions), keep trying until 20 successful evaluations
		//
		Random rand = new Random();
		final int MAX_TRIES = 1000;
		final int REQUIRED_NUM_EVALUATIONS = 20;
		int numEvaluations = 0;
		Exception savedException = null;
		for (int i = 0; i < MAX_TRIES && numEvaluations < REQUIRED_NUM_EVALUATIONS; i++){
			for (int j = 0; j < values.length; j++){
				values[j] = 0.01*(i+1)*rand.nextGaussian();
			}
			try {
				double result1 = exp1.evaluateVector(values);
				double result2 = exp2.evaluateVector(values);
				if (Double.isInfinite(result1) || Double.isNaN(result1)){
					throw new RuntimeException("exp1 = '"+exp1+"' evaluates to "+result1);
				}
				if (Double.isInfinite(result2) || Double.isNaN(result2)){
					throw new RuntimeException("exp1 = '"+exp2+"' evaluates to "+result2);
				}
				double scale = Math.abs(result1)+Math.abs(result2);
				double absdiff = Math.abs(result1-result2);
				if (scale > absoluteTolerance){
					if (absdiff > relativeTolerance*scale){
						System.out.println("EXPRESSIONS DIFFERENT: numerical test "+numEvaluations+", tolerance exceeded by "+(int)Math.log(absdiff/(relativeTolerance*scale))+"digits");
						return false;
					}
				}
				numEvaluations++;
			}catch (Exception e){
				savedException = e;
			}
		}
		if (numEvaluations < REQUIRED_NUM_EVALUATIONS){
			//savedException.printStackTrace(System.out);
			throw new RuntimeException("too many failed evaluations ("+numEvaluations+" of "+REQUIRED_NUM_EVALUATIONS+") ("+savedException.getMessage()+") of expressions '"+exp1+"' and '"+exp2+"'");
		}
		return true;
	}catch (cbit.vcell.parser.ExpressionException e){
		e.printStackTrace(System.out);
		System.out.println("EXPRESSIONS DIFFERENT: "+e);
		return false;
	}
}

/**
 * Insert the method's description here.
 * Creation date: (12/22/2002 5:02:53 PM)
 * @return cbit.vcell.parser.Expression
 */
public static Expression generateExpression(java.util.Random random, int maxDepth, boolean bIsConstraint) throws ExpressionException {
	SimpleNode node = generateSubtree(0,maxDepth,random,bIsConstraint);
	if (bIsConstraint){
		node = (SimpleNode)node.copyTreeBinary();
	}
	return new Expression(node.infixString(node.LANGUAGE_DEFAULT,node.NAMESCOPE_DEFAULT));
}
/**
 * Insert the method's description here.
 * Creation date: (12/22/2002 3:16:19 PM)
 * @return cbit.vcell.parser.Expression
 * @param seed long
 */
private static SimpleNode generateSubtree(int depth, int maxDepth, java.util.Random random, boolean bIsConstraint) {
	SimpleNode newNode = null;
	if (depth == 0 && bIsConstraint){
		ASTRelationalNode relNode = new ASTRelationalNode();
		double rtype = random.nextDouble();
		final String ops[] = {
			">",
			"<",
			"<=",
			">=",
			"==",
			//"!="
		};
		relNode.setOperationFromToken(ops[(int)Math.floor(rtype*ops.length)]);
		newNode = relNode;
	}else if (depth >= maxDepth){
		newNode = createTerminalNode(random,bIsConstraint);
	}else{
		newNode = createNode(random,bIsConstraint);
	}
	int numChildren = getNumberOfChildren(newNode);
	for (int i = 0; i < numChildren; i++){
		if (newNode instanceof ASTMultNode && i>0){  // InvertTerm is only ok if it's a second or later child of a mult node
			newNode.jjtAddChild(generateSubtree(depth+1,maxDepth,random,bIsConstraint));
		}else{
			SimpleNode child = null;
			while ((child = generateSubtree(depth+1,maxDepth,random,bIsConstraint)) instanceof ASTInvertTermNode){
			}
			newNode.jjtAddChild(child);
		}
	}
	return newNode;
}
/**
 * Insert the method's description here.
 * Creation date: (12/22/2002 3:41:17 PM)
 * @return int
 * @param node cbit.vcell.parser.SimpleNode
 */
private static int getNumberOfChildren(SimpleNode node) {
	if (node instanceof ASTAddNode){
		return 3;
	}else if (node instanceof ASTAndNode){
		return 2;
	}else if (node instanceof ASTFloatNode){
		return 0;
	}else if (node instanceof ASTFuncNode){
		ASTFuncNode fn = (ASTFuncNode)node;
		switch (fn.getFunction()){
			case ASTFuncNode.ABS: {
				return 1;
			}
			case ASTFuncNode.ACOS: {
				return 1;
			}
			case ASTFuncNode.ASIN: {
				return 1;
			}
			case ASTFuncNode.ATAN: {
				return 1;
			}
			case ASTFuncNode.ATAN2: {
				return 2;
			}
			case ASTFuncNode.COS: {
				return 1;
			}
			case ASTFuncNode.EXP: {
				return 1;
			}
			case ASTFuncNode.LOG: {
				return 1;
			}
			case ASTFuncNode.MAX: {
				return 2;
			}
			case ASTFuncNode.MIN: {
				return 2;
			}
			case ASTFuncNode.POW: {
				return 2;
			}
			case ASTFuncNode.SIN: {
				return 1;
			}
			case ASTFuncNode.SQRT: {
				return 1;
			}
			case ASTFuncNode.TAN: {
				return 1;
			}
			case ASTFuncNode.CEIL: {
				return 1;
			}
			case ASTFuncNode.FLOOR: {
				return 1;
			} 			
			case ASTFuncNode.CSC: {
				return 1;
			}
			case ASTFuncNode.COT: {
				return 1;
			}
			case ASTFuncNode.SEC: {
				return 1;
			}
			case ASTFuncNode.ACSC: {
				return 1;
			}
			case ASTFuncNode.ACOT: {
				return 1;
			}
			case ASTFuncNode.ASEC: {
				return 1;
			}
			case ASTFuncNode.SINH: {
				return 1;
			}
			case ASTFuncNode.COSH: {
				return 1;
			}
			case ASTFuncNode.TANH: {
				return 1;
			}
			case ASTFuncNode.CSCH: {
				return 1;
			}
			case ASTFuncNode.COTH: {
				return 1;
			}
			case ASTFuncNode.SECH: {
				return 1;
			}
			case ASTFuncNode.ASINH: {
				return 1;
			}
			case ASTFuncNode.ACOSH: {
				return 1;
			}
			case ASTFuncNode.ATANH: {
				return 1;
			}
			case ASTFuncNode.ACSCH: {
				return 1;
			}
			case ASTFuncNode.ACOTH: {
				return 1;
			}
			case ASTFuncNode.ASECH: {
				return 1;
			}
			case ASTFuncNode.FACTORIAL: {
				return 1;
			}			
			default:{
				throw new RuntimeException("unknown function type : "+fn.getFunction());
			}
		}
	}else if (node instanceof ASTIdNode){
		return 0;
	}else if (node instanceof ASTInvertTermNode){
		return 1;
	}else if (node instanceof ASTMinusTermNode){
		return 1;
	}else if (node instanceof ASTMultNode){
		return 3;
	}else if (node instanceof ASTNotNode){
		return 1;
	}else if (node instanceof ASTOrNode){
		return 2;
	}else if (node instanceof ASTPowerNode){
		return 2;
	}else if (node instanceof ASTRelationalNode){
		return 2;
	}else{
		throw new RuntimeException("unknown node type");
	}
}
}
