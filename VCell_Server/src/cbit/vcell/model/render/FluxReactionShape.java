package cbit.vcell.model.render;

/*�
 * (C) Copyright University of Connecticut Health Center 2001.
 * All rights reserved.
�*/
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;

import cbit.vcell.model.FluxReaction;
/**
 * This class was generated by a SmartGuide.
 * 
 */
public class FluxReactionShape extends ReactionStepShape {
/**
 * SpeciesShape constructor comment.
 * @param label java.lang.String
 * @param graphModel cbit.vcell.graph.GraphModel
 */
public FluxReactionShape(FluxReaction fluxReaction, ModelCartoon modelCartoon) {
	super(fluxReaction, modelCartoon);
	defaultFGselect = java.awt.Color.red;
}
/**
 * This method was created in VisualAge.
 * @return java.awt.Point
 */
public Point getAttachmentLocation(int attachmentType) {
	switch (attachmentType){
		case ATTACH_CENTER:
			return new Point(screenPos.x+screenSize.width/2,screenPos.y+screenSize.height/2);
		case ATTACH_LEFT:
			return new Point(screenPos.x,screenPos.y+screenSize.height/2);
		case ATTACH_RIGHT:
			return new Point(screenPos.x+screenSize.width,screenPos.y+screenSize.height/2);
	}
	return null;	
}
/**
 * This method was created by a SmartGuide.
 * @return cbit.vcell.model.Species
 */
public FluxReaction getFluxReaction() {
	return (FluxReaction) reactionStep;
}
/**
 * This method was created by a SmartGuide.
 * @return int
 * @param g java.awt.Graphics
 */
public Dimension getPreferedSize(java.awt.Graphics2D g) {

	preferedSize.width = 65;
	preferedSize.height = 25;

	if(getLabel() != null && getLabel().length() > 0){
		labelSize.width = g.getFontMetrics().stringWidth(getLabel());
		labelSize.height = g.getFontMetrics().getMaxAscent()+g.getFontMetrics().getMaxDescent();
	}
	return preferedSize;
}
/**
 * This method was created by a SmartGuide.
 * @param g java.awt.Graphics
 */
public void paint ( java.awt.Graphics2D g, int parentOffsetX, int parentOffsetY ) {

	int absPosX = screenPos.x + parentOffsetX;
	int absPosY = screenPos.y + parentOffsetY;
	//
	// draw and fill rounded rectangle
	//
	int hChannel = screenSize.height/2;
	g.setColor(backgroundColor);
	g.fillRoundRect(absPosX+1,absPosY+1,screenSize.width-1,screenSize.height-1,15,15);
//	g.fillRoundRect(absPosX+1,absPosY+1,screenSize.width-1,screenSize.height-1,15,15);
	g.setColor(forgroundColor);
	g.drawRoundRect(absPosX,absPosY,screenSize.width,screenSize.height,15,15);
	g.drawLine(absPosX,absPosY+hChannel/2, absPosX,absPosY+hChannel*3/2);
	g.drawLine(absPosX+screenSize.width,absPosY+hChannel/2, absPosX+screenSize.width,absPosY+hChannel*3/2);

	//
	// draw and white out center channel
	//
	g.setColor(java.awt.Color.white);
	g.fillRect(absPosX,absPosY+hChannel/2-1,screenSize.width+1,hChannel+2);
	g.setColor(forgroundColor);
	g.drawLine(absPosX,absPosY+hChannel/2-1,absPosX+screenSize.width,absPosY+hChannel/2-1);
	g.drawLine(absPosX,absPosY+hChannel*3/2+1,absPosX+screenSize.width,absPosY+hChannel*3/2+1);

	//
	// draw label
	//
//	if (getDisplayLabels() || isSelected()){
		//java.awt.FontMetrics fm = g.getFontMetrics();
		int textX = absPosX  + screenSize.width/2 - labelSize.width/2;
		int textY = absPosY + labelSize.height;
		if (getLabel()!=null && getLabel().length()>0){
			if(isSelected()){
				drawRaisedOutline(textX-5,textY-labelSize.height+3,labelSize.width+10,labelSize.height,
					g,Color.white,Color.black,Color.black);
			}
			g.setColor(Color.black);
			g.drawString(getLabel(),textX,textY);
			g.setColor(forgroundColor);
		}
//	}
	return;
}
/**
 * This method was created in VisualAge.
 */
public void refreshLabel() {
//	Species species = getFluxReaction().getFluxCarrier();
//	String carrier = (species!=null)?(species.getCommonName()):"?species?";
//	setLabel(carrier);
	setLabel(getFluxReaction().getName());
}
}
