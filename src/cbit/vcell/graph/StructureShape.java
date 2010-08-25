package cbit.vcell.graph;
/*�
 * (C) Copyright University of Connecticut Health Center 2001.
 * All rights reserved.
�*/
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;

import cbit.gui.graph.ElipseShape;
import cbit.gui.graph.GraphModel;
import cbit.gui.graph.visualstate.ImmutableVisualState;
import cbit.gui.graph.visualstate.VisualState;
import cbit.vcell.model.Model;
import cbit.vcell.model.Structure;
/**
 * This Class was generated by a SmartGuide.
 * 
 */
public abstract class StructureShape extends ElipseShape {
	private Structure structure = null;
	private Model model = null;
	protected static final int defaultSpacingX = 30;
	protected static final int defaultSpacingY = 10;

	/**
	 * This method was created by a SmartGuide.
	 * @param feature cbit.vcell.model.Feature
	 */
	public StructureShape (Structure structure, Model model, GraphModel graphModel) {
		super(graphModel);
		this.structure = structure;
		this.model = model;
	}

	@Override
	public VisualState createVisualState() {
		return new ImmutableVisualState(this, VisualState.PaintLayer.COMPARTMENT);
	}

	public Font getLabelFont(Graphics g) {
		return getBoldFont(g);
	}


	/**
	 * This method was created in VisualAge.
	 * @return cbit.vcell.model.Model
	 */
	public cbit.vcell.model.Model getModel() {
		return model;
	}


	/**
	 * This method was created in VisualAge.
	 * @return java.lang.Object
	 */
	@Override
	public Object getModelObject() {
		return structure;
	}


	/**
	 * This method was created by a SmartGuide.
	 * @return cbit.vcell.model.Feature
	 */
	public Structure getStructure() {
		return structure;
	}


	/**
	 * This method was created by a SmartGuide.
	 * @param g java.awt.Graphics
	 */

	@Override
	public void paintSelf ( Graphics2D g, int absPosX, int absPosY ) {

		g.setColor(backgroundColor);
		g.fillOval(absPosX+1,absPosY+1,shapeSize.width-1,shapeSize.height-1);
		g.setColor(forgroundColor);
		g.drawOval(absPosX,absPosY,shapeSize.width,shapeSize.height);

		//java.awt.FontMetrics fm = g.getFontMetrics();
		//if (getLabel()!=null && getLabel().length()>0){
		//if(this instanceof FeatureShape){
		//g.drawString(getLabel(),labelPos.x+absPosX,labelPos.y+absPosY);
		//}else{
		//int textX = absPosX + screenSize.width/2 - fm.stringWidth(getLabel())/2;
		//int textY = absPosY + 5 + fm.getMaxAscent();
		//g.drawString(getLabel(),textX,textY);
		//}
		//}
		//		SpeciesContextShape selectedShape = null;
		//		for (int i=0;i<childShapeList.size();i++){
		//			Shape child = (Shape)childShapeList.elementAt(i);
		//			if((child instanceof SpeciesContextShape) && child.isSelected()){
		//				selectedShape = (SpeciesContextShape)child;
		//			} else {
		//				child.paint(g,absPosX,absPosY);
		//			}
		//		}
		//		if(selectedShape != null){//To make sure its on top
		//			selectedShape.paint(g,absPosX,absPosY);
		//		}


		if (getLabel()!=null && getLabel().length()>0){
			Font origFont = g.getFont();
			g.setFont(getLabelFont(g));
			if(this instanceof FeatureShape){
				if(isSelected()){
					drawRaisedOutline(absPosX+labelPos.x-5,absPosY+labelPos.y-labelSize.height+3,labelSize.width+10,labelSize.height,
							g,Color.white,Color.black,Color.black);
				}
				g.setColor(Color.black);
				g.drawString(getLabel(),labelPos.x+absPosX,labelPos.y+absPosY);
			}else{
				int textX = absPosX + shapeSize.width/2 - labelSize.width/2;
				int textY = absPosY + labelSize.height -3;
				if(isSelected()){
					drawRaisedOutline(textX-5,textY-labelSize.height+3,labelSize.width+10,labelSize.height,
							g,Color.white,Color.black,Color.black);
				}
				g.setColor(Color.black);
				g.drawString(getLabel(),textX,textY);
			}
			g.setFont(origFont);
		}

	}

	/**
	 * This method was created in VisualAge.
	 */
	@Override
	public void refreshLabel() {
		setLabel(getStructure().getName());
	}
}