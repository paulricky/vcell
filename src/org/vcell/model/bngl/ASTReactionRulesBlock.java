/* Generated By:JJTree: Do not edit this line. ASTReactionRulesBlock.java */

package org.vcell.model.bngl;

public class ASTReactionRulesBlock extends SimpleNode {
	public ASTReactionRulesBlock(int id) {
		super(id);
	}

	public ASTReactionRulesBlock(BNGLParser p, int id) {
		super(p, id);
	}

	@Override
	public String toBNGL() {
		StringBuffer buffer = new StringBuffer();

		buffer.append("\nbegin reaction rules\n");
		for (int i = 0; i < jjtGetNumChildren(); i++) {
			buffer.append(jjtGetChild(i).toBNGL());
		}
		buffer.append("end reaction rules\n\n");
		return buffer.toString();
	}

	/** Accept the visitor. **/
	public Object jjtAccept(BNGLParserVisitor visitor, Object data) {
		return visitor.visit(this, data);
	}
}
