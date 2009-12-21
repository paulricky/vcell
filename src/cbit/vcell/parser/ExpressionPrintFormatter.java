package cbit.vcell.parser;

/*�
 * (C) Copyright University of Connecticut Health Center 2001.
 * All rights reserved.
�*/
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.util.Vector;
/**
 * This class was generated by a SmartGuide.
 * 
 */
public class ExpressionPrintFormatter {
	private java.awt.FontMetrics fm = null;
	private SimpleNode rootNode = null;
	private int fontOffsetY = 0;
	private int fontQuarterHeight = 0;
	private int fontHalfHeight = 0;
	private int fontHeight = 0;
	private int fontMaxAscent = 0;
	private int fontMaxDescent = 0;
	private int                 productStringWidth   = 0;
	private int                 sumStringWidth       = 0;
	private int                 relStringWidth       = 0;
	private int                 neqStringWidth       = 0;
	private static final String productString        = " . ";
	private static final String assignString         = "   =   ";
	private static final String sumString            = " + ";
	private static final String relString            = " >= ";
	private static final String neqString            = "  != ";
	private static final String orString             = " || ";
	private static final String andString            = " && ";
	private static final String minusString          = "  - ";
	private static final String leftParenString      = " ( ";
	private static final String rightParenString     = " ) ";
	private static final String commaString          = " , ";
	private static final String eString              = "e";
	private static final String twoString            = " 2";
	private static final String laplacianString      = "   ";
	private static final String notString            = " ! ";
	private int                 assignStringWidth    = 0;
	private int                 parenStringWidth     = 0;
	private int                 quotientLineHeight   = 0;
	private int                 commaStringWidth     = 0;
	private int                 eStringWidth         = 0;
	private int                 laplacianStringWidth = 0;
	private int                 twoStringWidth       = 0;
	private int                 orStringWidth        = 0;
	private int                 notStringWidth       = 0;
	private int                 andStringWidth       = 0;
/**
 * This method was created by a SmartGuide.
 * @param font java.awt.Font
 */
public ExpressionPrintFormatter (Expression exp) {
	this.rootNode = exp.getRootNode();
}
/**
 * This method was created by a SmartGuide.
 * @return java.awt.Dimension
 * @param node cbit.vcell.parser.SimpleNode
 * @exception java.lang.Exception The exception description.
 */
private FormatSize getSize(SimpleNode node) throws ExpressionException {
	if (node instanceof ASTRelationalNode ||
		 node instanceof ASTAndNode ||
		 node instanceof ASTOrNode){
		Vector terms = new Vector();
		for (int i=0;i<node.jjtGetNumChildren();i++){
			SimpleNode child = (SimpleNode)node.jjtGetChild(i);
			terms.addElement(child);	
		}
		if (node instanceof ASTRelationalNode && terms.size() != 2){
			throw new ExpressionException(node.getClass().getName()+" must have two nodes");
		}else if (terms.size() < 2){
			throw new ExpressionException(node.getClass().getName()+" must have at least two nodes");
		}
		//
		// calculate sizes for string
		//
		Vector sumSizes = new Vector();
		int operatorWidth = 0;
		if (node instanceof ASTRelationalNode){
			ASTRelationalNode relNode = (ASTRelationalNode)node;
			if (relNode.getOperation().length()==1){
				operatorWidth = sumStringWidth;
			}else if (neqString.indexOf(relNode.opString)>-1){
				operatorWidth = neqStringWidth;
			}else if (relNode.getOperation().length()==2){
				operatorWidth = relStringWidth;
			}
		}else if (node instanceof ASTAndNode){
			operatorWidth = andStringWidth;
		}else if (node instanceof ASTOrNode){
			operatorWidth = orStringWidth;
		}			
		FormatSize sumBound = getSizeHorizontalList(terms,sumSizes,operatorWidth);		
		sumBound.width += parenStringWidth*2;		
		if (node.jjtGetChild(0) instanceof ASTMinusTermNode){
			sumBound.width += sumStringWidth;
		}

		return sumBound;
		
	}else if (node instanceof DerivativeNode){
		SimpleNode displayRoot = ((DerivativeNode)node).displayExp.getRootNode();
		Vector terms = new Vector();
		terms.addElement(displayRoot);
		terms.addElement((SimpleNode)node.jjtGetChild(0));
		//
		// calculate sizes for argument of laplacian
		//
		Vector argSizes = new Vector();
		FormatSize argBound = getSizeHorizontalList(terms,argSizes,0);		

		return argBound;

	}else if (node instanceof ASTLaplacianNode){
		Vector terms = new Vector();
		for (int i=0;i<node.jjtGetNumChildren();i++){
			SimpleNode child = (SimpleNode)node.jjtGetChild(i);
			terms.addElement(child);
		}
		if (terms.size() != 1){
			throw new ExpressionException("ASTLaplacianNode must have one child node");
		}	
		//
		// calculate sizes for argument of laplacian
		//
		Vector argSizes = new Vector();
		FormatSize argBound = getSizeHorizontalList(terms,argSizes,0);		

		//
		// calculate size for exponent 2 (i.e. nabla^2)
		// 
		FormatSize exponentSize = new FormatSize();
		exponentSize.width = twoStringWidth;
		exponentSize.high = fontMaxAscent;
		exponentSize.low = 0;
		
		//
		// calculate total bounds
		//
		FormatSize laplacianBound = new FormatSize();
		laplacianBound.width = exponentSize.width + argBound.width + laplacianStringWidth;
		laplacianBound.high = Math.max(exponentSize.low + exponentSize.high + fontMaxAscent,
												argBound.high);
		laplacianBound.low = Math.max(fontMaxDescent,	argBound.low);
		return laplacianBound;

	}else if (node instanceof ASTAssignNode){
		Vector terms = new Vector();
		for (int i=0;i<node.jjtGetNumChildren();i++){
			SimpleNode child = (SimpleNode)node.jjtGetChild(i);
			terms.addElement(child);
		}
		if (terms.size() != 2){
			throw new ExpressionException("ASTAssignNode must have two nodes");
		}	
		//
		// calculate sizes for string
		//
		Vector assignSizes = new Vector();
		FormatSize assignBound = getSizeHorizontalList(terms,assignSizes,assignStringWidth);		

		return assignBound;
		
	}else if (node instanceof ASTAddNode){
		Vector terms = new Vector();
		for (int i=0;i<node.jjtGetNumChildren();i++){
			SimpleNode child = (SimpleNode)node.jjtGetChild(i);
			if (child instanceof ASTMinusTermNode){
				terms.addElement(child.jjtGetChild(0));
			}else{	
				terms.addElement(child);
			}	
		}
		if (terms.size() < 2){
			throw new ExpressionException("ASTAddNode must have at least two nodes");
		}	
		//
		// calculate sizes for string
		//
		Vector sumSizes = new Vector();
		FormatSize sumBound = getSizeHorizontalList(terms,sumSizes,sumStringWidth);		
		sumBound.width += parenStringWidth*2;		
		if (node.jjtGetChild(0) instanceof ASTMinusTermNode){
			sumBound.width += sumStringWidth;
		}

		return sumBound;
		
	}else if (node instanceof ASTFloatNode){
		FormatSize size = new FormatSize();
		size.width = fm.stringWidth(node.infixString(node.LANGUAGE_DEFAULT));
		size.high = fontMaxAscent;
		size.low = fontMaxDescent;
		return size;
	}else if (node instanceof ASTNotNode){
		Vector terms = new Vector();
		for (int i=0;i<node.jjtGetNumChildren();i++){
			SimpleNode child = (SimpleNode)node.jjtGetChild(i);
			terms.addElement(child);
		}
		if (terms.size() != 1){
			throw new ExpressionException("ASTNotNode must have one node");
		}	
		//
		// calculate sizes for string
		//
		Vector sumSizes = new Vector();
		FormatSize sumBound = getSizeHorizontalList(terms,sumSizes,sumStringWidth);		
		sumBound.width += parenStringWidth*2 + fm.stringWidth(notString);		
		return sumBound;
		
	}else if (node instanceof ASTFuncNode){
		Vector terms = new Vector();
		for (int i=0;i<node.jjtGetNumChildren();i++){
			SimpleNode child = (SimpleNode)node.jjtGetChild(i);
			terms.addElement(child);
		}
		if (terms.size() < 1){
			throw new ExpressionException("ASTFuncNode must have at least one node");
		}	
		//
		// calculate sizes for string
		//
		Vector sumSizes = new Vector();
		FormatSize sumBound = getSizeHorizontalList(terms,sumSizes,sumStringWidth);		
		String functionName = ((ASTFuncNode)node).getName();
		if (functionName.equalsIgnoreCase("pow")){
			FormatSize mantissaSize = (FormatSize)sumSizes.elementAt(0);
			FormatSize exponentSize = (FormatSize)sumSizes.elementAt(1);
			sumBound.width = parenStringWidth*2 + mantissaSize.width + exponentSize.width;
			sumBound.high = mantissaSize.high + exponentSize.high + exponentSize.low;
			sumBound.low = mantissaSize.low;
			return sumBound;
		}else if (functionName.equalsIgnoreCase("exp")){
			FormatSize exponentSize = (FormatSize)sumSizes.elementAt(0);
			sumBound.width = exponentSize.width + eStringWidth;
			sumBound.high = exponentSize.low + exponentSize.high + fontMaxAscent;
			sumBound.low = fontMaxDescent;
			return sumBound;
		}else{	
			sumBound.width += parenStringWidth*2 + fm.stringWidth(((ASTFuncNode)node).getName());		
			return sumBound;
		}	
		
	}else if (node instanceof ASTPowerNode){
		Vector terms = new Vector();
		for (int i=0;i<node.jjtGetNumChildren();i++){
			SimpleNode child = (SimpleNode)node.jjtGetChild(i);
			terms.addElement(child);
		}
		if (terms.size() != 2){
			throw new ExpressionException("ASTPowerNode must have two nodes");
		}	
		//
		// calculate sizes for string
		//
		Vector sumSizes = new Vector();
		FormatSize sumBound = getSizeHorizontalList(terms,sumSizes,sumStringWidth);		
		FormatSize mantissaSize = (FormatSize)sumSizes.elementAt(0);
		FormatSize exponentSize = (FormatSize)sumSizes.elementAt(1);
		sumBound.width = parenStringWidth*2 + mantissaSize.width + exponentSize.width;
		sumBound.high = mantissaSize.high + exponentSize.high + exponentSize.low;
		sumBound.low = mantissaSize.low;
		return sumBound;
	}else if (node instanceof ASTIdNode){
		FormatSize size = new FormatSize();
		size.width = fm.stringWidth(node.infixString(node.LANGUAGE_DEFAULT));
		size.high = fontMaxAscent;
		size.low = fontMaxDescent;
		return size;
	}else if (node instanceof ASTInvertTermNode){
		throw new ExpressionException("node type not supported yet");
	}else if (node instanceof ASTMinusTermNode){
		SimpleNode child = (SimpleNode)node.jjtGetChild(0);
		FormatSize size = getSize(child);
		size.width += sumStringWidth;
		return size;
	}else if (node instanceof ASTMultNode){
		if (node.jjtGetNumChildren() == 1){
			return getSize((SimpleNode)node.jjtGetChild(0));
		}
			
		Vector numerators = new Vector();
		Vector denominators = new Vector();
		for (int i=0;i<node.jjtGetNumChildren();i++){
			SimpleNode child = (SimpleNode)node.jjtGetChild(i);
			if (child instanceof ASTInvertTermNode){
				denominators.addElement(child.jjtGetChild(0));
			}else{
				numerators.addElement(child);
			}
		}
		if (numerators.size() < 1){
			throw new ExpressionException("ASTMultNode must have at least one node in the numerator");
		}	
		//
		// calculate sizes for numerator
		//
		Vector numeratorSizes = new Vector();
		FormatSize numeratorBound = getSizeHorizontalList(numerators,numeratorSizes,productStringWidth);		
			
		//
		// calculate sizes for denominator
		//
		Vector denominatorSizes = new Vector();
		FormatSize denominatorBound = getSizeHorizontalList(denominators,denominatorSizes,productStringWidth);		
		
		//
		// calculate size for whole quotient
		//
		FormatSize quotientBound = new FormatSize();
		quotientBound.width = Math.max(numeratorBound.width,denominatorBound.width);
//		quotientBound.high = numeratorBound.high;
//		quotientBound.low = numeratorBound.low + denominatorBound.high + denominatorBound.low + quotientLineHeight;
		if (denominators.size() == 0){
			quotientBound.high = numeratorBound.high;
			quotientBound.low = numeratorBound.low;
		}else{
			quotientBound.high = numeratorBound.high + numeratorBound.low + quotientLineHeight/2;
			quotientBound.low = denominatorBound.high + denominatorBound.low + quotientLineHeight/2;
		}		
		
		return quotientBound;
	}else if (node instanceof DerivativeNode){
		throw new ExpressionException("node type DirivativeNode not supported yet");
	}else{
		throw new ExpressionException("node type "+node.getClass().toString()+" not supported yet");
	}		
}
/**
 * This method was created by a SmartGuide.
 * @return java.awt.Dimension
 * @param g java.awt.Graphics
 */
public Dimension getSize(Graphics2D g) throws ExpressionException {
	if (g==null){
		throw new ExpressionException("graphics is null");
	}
	fm = g.getFontMetrics();
	fontMaxAscent = fm.getMaxAscent();
	fontMaxDescent = fm.getMaxDescent();
	fontHeight = fontMaxAscent + fontMaxDescent;
	fontHalfHeight = fontHeight/2;
	fontQuarterHeight = fontHeight/4;
	fontOffsetY = fm.getHeight() - fontMaxDescent;
	assignStringWidth = fm.stringWidth(assignString);
	eStringWidth = fm.stringWidth(eString);
	productStringWidth = fm.stringWidth(productString);
	commaStringWidth = fm.stringWidth(commaString);
	sumStringWidth = Math.max(fm.stringWidth(sumString),fm.stringWidth(minusString));
	relStringWidth = fm.stringWidth(relString);
	neqStringWidth = fm.stringWidth(neqString);
	parenStringWidth = Math.max(fm.stringWidth(leftParenString),fm.stringWidth(rightParenString));
	laplacianStringWidth = fm.stringWidth(laplacianString);
	twoStringWidth = fm.stringWidth(twoString);
	andStringWidth = fm.stringWidth(andString);
	orStringWidth = fm.stringWidth(orString);
	notStringWidth = fm.stringWidth(notString);
	FormatSize fs = getSize(rootNode);
	return new Dimension(fs.width, fs.high+fs.low);
}
/**
 * This method was created by a SmartGuide.
 * @return java.awt.Dimension
 * @param nodeList java.util.Vector
 * @param sizeList java.util.Vector
 * @param separatorWidth int
 * @exception java.lang.Exception The exception description.
 */
private FormatSize getSizeHorizontalList(Vector nodeList, Vector sizeList, int separatorWidth) throws ExpressionException {
	FormatSize bound = new FormatSize();
	sizeList.removeAllElements();
	for (int i=0;i<nodeList.size();i++){
		FormatSize sz = getSize((SimpleNode)nodeList.elementAt(i));
		sizeList.addElement(sz);
		bound.high = Math.max(bound.high,sz.high);
		bound.low = Math.max(bound.low,sz.low);
		bound.width += sz.width;
	}	
	if (nodeList.size()>1){
		bound.width += (nodeList.size()-1)*separatorWidth;
	}
	return bound;
}
/**
 * This method was created by a SmartGuide.
 * @param g java.awt.Graphics
 */
public void paint(java.awt.Graphics2D g) throws ExpressionException {
	Dimension size = getSize(g);
	FormatSize fs = getSize(rootNode);
	Rectangle bounds = g.getClipBounds();
/*
	if (bounds.width < size.width){
		throw new Exception("graphics context not wide enough");
	}
	if (bounds.height < size.height){
		throw new Exception("graphics context not tall enough");
	}
*/		
	paint(g,rootNode,bounds.x + (bounds.width-size.width)/2,bounds.y + (bounds.height-size.height)/2 + fs.high);
}
/**
 * draw the expression with y at the center and x at the left
 * @param g java.awt.Graphics
 * @param node cbit.vcell.parser.SimpleNode
 */
private void paint(java.awt.Graphics2D g, SimpleNode node, int x, int y) throws ExpressionException {
	if (node instanceof ASTRelationalNode ||
		 node instanceof ASTAndNode ||
		 node instanceof ASTOrNode){
		Vector terms = new Vector();
		for (int i=0;i<node.jjtGetNumChildren();i++){
			SimpleNode child = (SimpleNode)node.jjtGetChild(i);
			terms.addElement(child);
		}
		if (node instanceof ASTRelationalNode && terms.size() != 2){
			throw new ExpressionException(node.getClass().getName()+" must have two nodes");
		}else if (terms.size() < 2){
			throw new ExpressionException(node.getClass().getName()+" must have at least two nodes");
		}	
		//
		// calculate sizes for string
		//
		Vector sumSizes = new Vector();
		FormatSize sumBound = getSizeHorizontalList(terms,sumSizes,sumStringWidth);		
		sumBound.width += parenStringWidth*2;		
					

		//
		// get operator string and font metrics
		//	
		int operatorWidth = 0;
		String operatorString = null;
		if (node instanceof ASTRelationalNode){
			ASTRelationalNode relNode = (ASTRelationalNode)node;
			operatorString = " "+relNode.opString+" ";
			if (neqString.indexOf(relNode.opString)>-1){
				operatorWidth = neqStringWidth;
				operatorString = neqString;
			}else if (relNode.opString.length()==2){
				operatorWidth = relStringWidth;
			}else{
				operatorWidth = sumStringWidth;
			}
		}else if (node instanceof ASTAndNode){
			operatorString = " && ";
			operatorWidth = andStringWidth;
		}else if (node instanceof ASTOrNode){
			operatorString = " || ";
			operatorWidth = orStringWidth;
		}			
		//
		// draw relational or boolean operation
		//
		int posX = 0;
		int posY = 0;
		g.drawString(leftParenString,x+posX,y+posY);
		posX += parenStringWidth;
		if (node.jjtGetChild(0) instanceof ASTMinusTermNode){
			g.drawString(minusString,x+posX,y+posY);
			posX += sumStringWidth;
		}
		for (int i=0;i<terms.size();i++){
			SimpleNode termNode = (SimpleNode)terms.elementAt(i);
			FormatSize size = (FormatSize)sumSizes.elementAt(i);
			paint(g,termNode,x+posX,y+posY);
			posX += size.width;
			if (i<(terms.size()-1)){
				g.drawString(operatorString,x+posX,y+posY);
				posX += operatorWidth;
			}	
		}	
		g.drawString(rightParenString,x+posX,y+posY);
		
	}else if (node instanceof DerivativeNode){
		Vector terms = new Vector();
		terms.addElement(((DerivativeNode)node).displayExp.getRootNode());
		for (int i=0;i<node.jjtGetNumChildren();i++){
			SimpleNode child = (SimpleNode)node.jjtGetChild(i);
			terms.addElement(child);
		}
		if (terms.size() != 2){
			throw new ExpressionException("DerivativeNode must have one node");
		}	
		//
		// calculate sizes for string
		//
		Vector argSizes = new Vector();
		FormatSize argBound = getSizeHorizontalList(terms,argSizes,0);		
				
		int posX = 0;
		int posY = 0;
		for (int i=0;i<terms.size();i++){
			SimpleNode termNode = (SimpleNode)terms.elementAt(i);
			FormatSize size = (FormatSize)argSizes.elementAt(i);
			paint(g,termNode,x+posX,y+posY);
			posX += size.width;
		}	
	}else if (node instanceof ASTLaplacianNode){
		Vector terms = new Vector();
		for (int i=0;i<node.jjtGetNumChildren();i++){
			SimpleNode child = (SimpleNode)node.jjtGetChild(i);
			terms.addElement(child);
		}
		if (terms.size() != 1){
			throw new ExpressionException("ASTLaplacianNode must have one node");
		}	
		//
		// calculate sizes for string
		//
		Vector argSizes = new Vector();
		FormatSize argBound = getSizeHorizontalList(terms,argSizes,0);		
				
		int posX = 0;
		//
		// draw laplacian operator
		//			
		int posY = 0;
		g.drawLine(	x+posX+laplacianStringWidth/2,	y+posY,
						x+posX,								y+posY-fontMaxAscent);
		g.drawLine(	x+posX+laplacianStringWidth/2,	y+posY,
						x+posX+laplacianStringWidth,		y+posY-fontMaxAscent);
		g.drawLine(	x+posX,								y+posY-fontMaxAscent,
						x+posX+laplacianStringWidth,		y+posY-fontMaxAscent);
		posX += laplacianStringWidth;
			
		//
		// draw exponent 2
		//
		g.drawString(twoString,x+posX,y+posY-fontMaxAscent);
		posX += twoStringWidth;
		//
		// draw argument
		//
		SimpleNode termNode = (SimpleNode)terms.elementAt(0);
		paint(g,termNode,x+posX,y+posY);

	}else if (node instanceof ASTAssignNode){
		Vector terms = new Vector();
		for (int i=0;i<node.jjtGetNumChildren();i++){
			SimpleNode child = (SimpleNode)node.jjtGetChild(i);
			terms.addElement(child);
		}
		if (terms.size() != 2){
			throw new ExpressionException("ASTAssignNode must have two nodes");
		}	
		//
		// calculate sizes for string
		//
		Vector assignSizes = new Vector();
		FormatSize assignBound = getSizeHorizontalList(terms,assignSizes,assignStringWidth);		
					
		//
		// draw assign
		//
		int posX = 0;
		int posY = 0;
		
		SimpleNode termNode = (SimpleNode)terms.elementAt(0);
		FormatSize size = (FormatSize)assignSizes.elementAt(0);
		paint(g,termNode,x+posX,y+posY);
		posX += size.width;

		g.drawString(assignString,x+posX,y+posY);
		posX += assignStringWidth;

		termNode = (SimpleNode)terms.elementAt(1);
		size = (FormatSize)assignSizes.elementAt(1);
		paint(g,termNode,x+posX,y+posY);
		posX += size.width;
					
	}else if (node instanceof ASTAddNode){
		Vector terms = new Vector();
		for (int i=0;i<node.jjtGetNumChildren();i++){
			SimpleNode child = (SimpleNode)node.jjtGetChild(i);
			if (child instanceof ASTMinusTermNode){
				terms.addElement(child.jjtGetChild(0));
			}else{	
				terms.addElement(child);
			}	
		}
		if (terms.size() < 2){
			throw new ExpressionException("ASTAddNode must have at least two nodes");
		}	
		//
		// calculate sizes for string
		//
		Vector sumSizes = new Vector();
		FormatSize sumBound = getSizeHorizontalList(terms,sumSizes,sumStringWidth);		
		sumBound.width += parenStringWidth*2;		
					
		//
		// draw sum
		//
		int posX = 0;
		int posY = 0;
		g.drawString(leftParenString,x+posX,y+posY);
		posX += parenStringWidth;
		if (node.jjtGetChild(0) instanceof ASTMinusTermNode){
			g.drawString(minusString,x+posX,y+posY);
			posX += sumStringWidth;
		}
		for (int i=0;i<terms.size();i++){
			SimpleNode termNode = (SimpleNode)terms.elementAt(i);
			FormatSize size = (FormatSize)sumSizes.elementAt(i);
			paint(g,termNode,x+posX,y+posY);
			posX += size.width;
			if (i<(terms.size()-1)){
				if (node.jjtGetChild(i+1) instanceof ASTMinusTermNode){
					g.drawString(minusString,x+posX,y+posY);
				}else{	
					g.drawString(sumString,x+posX,y+posY);
				}	
				posX += sumStringWidth;
			}	
		}	
		g.drawString(rightParenString,x+posX,y+posY);
	}else if (node instanceof ASTFloatNode){
		g.drawString(node.infixString(node.LANGUAGE_DEFAULT),x,y);
	}else if (node instanceof ASTNotNode){
		Vector terms = new Vector();
		for (int i=0;i<node.jjtGetNumChildren();i++){
			SimpleNode child = (SimpleNode)node.jjtGetChild(i);
			terms.addElement(child);
		}
		if (terms.size() != 1){
			throw new ExpressionException("ASTNotNode must have one node");
		}	
		//
		// calculate sizes for string
		//
		Vector argSizes = new Vector();
		FormatSize argBound = getSizeHorizontalList(terms,argSizes,commaStringWidth);		
		argBound.width += parenStringWidth*2;		
				
		//
		// draw function
		//
		int posX = 0;
		int posY = 0;
		g.drawString(notString,x+posX,y+posY);
		posX += notStringWidth;
		//
		// draw arguments
		//
		posY = 0;
		g.drawString(leftParenString,x+posX,y+posY);
		posX += parenStringWidth;
		for (int i=0;i<terms.size();i++){
			SimpleNode termNode = (SimpleNode)terms.elementAt(i);
			FormatSize size = (FormatSize)argSizes.elementAt(i);
			posY = 0;
			paint(g,termNode,x+posX,y+posY);
			posX += size.width;
			if (i<(terms.size()-1)){
				posY = 0;
				g.drawString(commaString,x+posX,y+posY-fontQuarterHeight);
				posX += commaStringWidth;
			}	
		}	
		posY = 0;
		g.drawString(rightParenString,x+posX,y+posY);

	}else if (node instanceof ASTFuncNode){
		Vector terms = new Vector();
		for (int i=0;i<node.jjtGetNumChildren();i++){
			SimpleNode child = (SimpleNode)node.jjtGetChild(i);
			terms.addElement(child);
		}
		if (terms.size() < 1){
			throw new ExpressionException("ASTFuncNode must have at least two nodes");
		}	
		//
		// calculate sizes for string
		//
		Vector argSizes = new Vector();
		FormatSize argBound = getSizeHorizontalList(terms,argSizes,commaStringWidth);		
		argBound.width += parenStringWidth*2;		
				
		String functionName = ((ASTFuncNode)node).getName();
		if (functionName.equalsIgnoreCase("pow")){
			int posX = 0;
			//
			// draw base argument
			//
			SimpleNode termNode = (SimpleNode)terms.elementAt(0);
			FormatSize size0 = (FormatSize)argSizes.elementAt(0);
			FormatSize size1 = (FormatSize)argSizes.elementAt(1);

			int posY = 0;
			g.drawString(leftParenString,x+posX,y+posY);
			posX += parenStringWidth;
			paint(g,termNode,x+posX,y+posY);
			posX += size0.width;
			g.drawString(rightParenString,x+posX,y+posY);
			posX += parenStringWidth;
			
			//
			// draw exponent argument
			//
			termNode = (SimpleNode)terms.elementAt(1);
			posY = - size1.low - size0.high;
			paint(g,termNode,x+posX,y+posY);
			
		}else if (functionName.equalsIgnoreCase("exp")){
			int posX = 0;
			//
			// draw base argument
			//
			FormatSize sizeExponent = (FormatSize)argSizes.elementAt(0);
			int posY = 0;
			g.drawString(eString,x+posX,y+posY);
			posX += eStringWidth;
			
			//
			// draw exponent argument
			//
			SimpleNode termNode = (SimpleNode)terms.elementAt(0);
			posY = - sizeExponent.low - fontMaxAscent;
			paint(g,termNode,x+posX,y+posY);
			
		}else{		
			//
			// draw function
			//
			int posX = 0;
			int posY = 0;
			g.drawString(functionName,x+posX,y+posY);
			posX += fm.stringWidth(functionName);
			//
			// draw arguments
			//
			posY = 0;
			g.drawString(leftParenString,x+posX,y+posY);
			posX += parenStringWidth;
			for (int i=0;i<terms.size();i++){
				SimpleNode termNode = (SimpleNode)terms.elementAt(i);
				FormatSize size = (FormatSize)argSizes.elementAt(i);
				posY = 0;
				paint(g,termNode,x+posX,y+posY);
				posX += size.width;
				if (i<(terms.size()-1)){
					posY = 0;
					g.drawString(commaString,x+posX,y+posY-fontQuarterHeight);
					posX += commaStringWidth;
				}	
			}	
			posY = 0;
			g.drawString(rightParenString,x+posX,y+posY);
		}	
						
	}else if (node instanceof ASTPowerNode){
		Vector terms = new Vector();
		for (int i=0;i<node.jjtGetNumChildren();i++){
			SimpleNode child = (SimpleNode)node.jjtGetChild(i);
			terms.addElement(child);
		}
		if (terms.size() != 2){
			throw new ExpressionException("ASTPowerNode must have two nodes");
		}	
		//
		// calculate sizes for string
		//
		Vector argSizes = new Vector();
		FormatSize argBound = getSizeHorizontalList(terms,argSizes,commaStringWidth);		
		argBound.width += parenStringWidth*2;		
				
		int posX = 0;
		//
		// draw base argument
		//
		SimpleNode termNode = (SimpleNode)terms.elementAt(0);
		FormatSize size0 = (FormatSize)argSizes.elementAt(0);
		FormatSize size1 = (FormatSize)argSizes.elementAt(1);

		int posY = 0;
		g.drawString(leftParenString,x+posX,y+posY);
		posX += parenStringWidth;
		paint(g,termNode,x+posX,y+posY);
		posX += size0.width;
		g.drawString(rightParenString,x+posX,y+posY);
		posX += parenStringWidth;
		
		//
		// draw exponent argument
		//
		termNode = (SimpleNode)terms.elementAt(1);
		posY = - size1.low - size0.high;
		paint(g,termNode,x+posX,y+posY);

	}else if (node instanceof ASTIdNode){
		g.drawString(node.infixString(node.LANGUAGE_DEFAULT),x,y);
	}else if (node instanceof ASTInvertTermNode){
		throw new ExpressionException("node type not supported yet");
	}else if (node instanceof ASTMinusTermNode){
		FormatSize termBound = getSize(node);
		int posX = 0;
		int posY = 0;
		g.drawString(minusString,x+posX,y+posY);
		posX += sumStringWidth;
		SimpleNode child = (SimpleNode)node.jjtGetChild(0);
		posY = 0;
		paint(g,child,x+posX,y+posY);

	}else if (node instanceof ASTMultNode){
		if (node.jjtGetNumChildren() == 1){
			paint(g,(SimpleNode)node.jjtGetChild(0),x,y);
			return;
		}
			
		Vector numerators = new Vector();
		Vector denominators = new Vector();
		for (int i=0;i<node.jjtGetNumChildren();i++){
			SimpleNode child = (SimpleNode)node.jjtGetChild(i);
			if (child instanceof ASTInvertTermNode){
				denominators.addElement(child.jjtGetChild(0));
			}else{
				numerators.addElement(child);
			}
		}
		if (numerators.size() < 1){
			throw new ExpressionException("ASTMultNode must have at least one node in the numerator");
		}	
		//
		// calculate sizes for numerator
		//
		Vector numeratorSizes = new Vector();
		FormatSize numeratorBound = getSizeHorizontalList(numerators,numeratorSizes,productStringWidth);		
			
		//
		// calculate sizes for denominator
		//
		Vector denominatorSizes = new Vector();
		FormatSize denominatorBound = getSizeHorizontalList(denominators,denominatorSizes,productStringWidth);		
		
		//
		// calculate size for whole quotient
		//
		FormatSize quotientBound = new FormatSize();
		quotientBound.width = Math.max(numeratorBound.width,denominatorBound.width);
		quotientBound.high = numeratorBound.high + numeratorBound.low + quotientLineHeight/2;
		quotientBound.low = denominatorBound.high + denominatorBound.low + quotientLineHeight/2;
		
		//
		// draw numerator
		//
		int posY=0;
		if (denominators.size()>0){
			posY= -numeratorBound.low - quotientLineHeight/2;
		}	
		int posX = (quotientBound.width - numeratorBound.width)/2;
		for (int i=0;i<numerators.size();i++){
			SimpleNode numNode = (SimpleNode)numerators.elementAt(i);
			FormatSize size = (FormatSize)numeratorSizes.elementAt(i);
			paint(g,numNode,x+posX,y+posY);
			posX += size.width;
			if (i<(numerators.size()-1)){
				g.drawString(productString,x+posX,y+posY-fontQuarterHeight);
				posX += productStringWidth;
			}	
		}	

		if (denominators.size()>0){			
			//
			// draw the denominator
			//
			posX = (quotientBound.width - denominatorBound.width)/2;
			posY = quotientLineHeight/2 + denominatorBound.high;
			for (int i=0;i<denominators.size();i++){
				SimpleNode numNode = (SimpleNode)denominators.elementAt(i);
				FormatSize size = (FormatSize)denominatorSizes.elementAt(i);
				paint(g,numNode,x+posX,y+posY);
				posX += size.width;
				if (i<(denominators.size()-1)){
					g.drawString(productString,x+posX,y+posY-fontQuarterHeight);
					posX += productStringWidth;
				}	
			}	
			//
			// draw the quotient line
			//
			posX = 0;
			posY = 0;
			g.drawLine(x+posX,y+posY,x+posX+quotientBound.width,y+posY);
		}	
					
	}else if (node instanceof DerivativeNode){
		throw new ExpressionException("node type DerivativeNode not supported yet");
	}else{
		throw new ExpressionException("node type "+node.getClass().toString()+" not supported yet");
	}		
}
}
