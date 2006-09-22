package cbit.vcell.model.render;
/*�
 * (C) Copyright University of Connecticut Health Center 2001.
 * All rights reserved.
�*/
import java.awt.Point;
import java.util.Enumeration;
import java.util.Vector;

import cbit.gui.graph.ContainerShape;
import cbit.gui.graph.GraphEvent;
import cbit.gui.graph.GraphPane;
import cbit.gui.graph.Shape;
import cbit.vcell.model.Catalyst;
import cbit.vcell.model.Diagram;
import cbit.vcell.model.Feature;
import cbit.vcell.model.Flux;
import cbit.vcell.model.FluxReaction;
import cbit.vcell.model.Membrane;
import cbit.vcell.model.NodeReference;
import cbit.vcell.model.Product;
import cbit.vcell.model.Reactant;
import cbit.vcell.model.ReactionParticipant;
import cbit.vcell.model.ReactionStep;
import cbit.vcell.model.SimpleReaction;
import cbit.vcell.model.SpeciesContext;
import cbit.vcell.model.Structure;
/**
 * This class was generated by a SmartGuide.
 * 
 */
public class ReactionCartoon extends ModelCartoon{
	private Structure structure = null;

/**
 * This method was created by a SmartGuide.
 * @param model cbit.vcell.model.Model
 */
public ReactionCartoon () {
	super();
	setResizable(false);
}


/**
 * This method was created in VisualAge.
 * @param modelObject java.lang.Object
 * @param location java.awt.Point
 */
void addShape(Object modelObject, Point location) throws Exception {

	if (getShapeFromModelObject(modelObject)!=null){
		throw new Exception("Shape object already defined for this object");
	}

	ReactionContainerShape reactionContainerShape = (ReactionContainerShape)getShapeFromModelObject(structure);
	
	Shape shape = null;
	if (modelObject instanceof SimpleReaction){
		shape = new SimpleReactionShape((SimpleReaction)modelObject,this);
		reactionContainerShape.addChildShape(shape);
	}else if (modelObject instanceof FluxReaction){
		shape = new FluxReactionShape((FluxReaction)modelObject,this);
		reactionContainerShape.addChildShape(shape);
	}else{
		throw new Exception("unknown type of ReactionStep '"+modelObject.getClass().toString());
	}
	Point origin = reactionContainerShape.getAbsLocation();
	shape.setLocation(new Point(location.x-origin.x,location.y-origin.y));
	addShape(shape);
	shape.setDirty(false);
	fireGraphChanged(new GraphEvent(this));
}


/**
 * This method was created in VisualAge.
 */
public void applyDefaults(Diagram diagram) {
	Vector nodeList = diagram.getNodeList();
	for (int i=0;i<nodeList.size();i++){
		NodeReference node = (NodeReference)nodeList.elementAt(i);
		Object obj = null;
		switch (node.nodeType){
			case NodeReference.SIMPLE_REACTION_NODE:{
				obj = getModel().getReactionStep(node.name);
				if (!(obj instanceof SimpleReaction)){
					System.out.println("ReactionCartoon.applyDefaults(), diagram reaction "+node.name+" type mismatch in model, using location anyway");
				}
				break;
			}
			case NodeReference.FLUX_REACTION_NODE:{
				obj = getModel().getReactionStep(node.name);
				if (!(obj instanceof FluxReaction)){
					System.out.println("ReactionCartoon.applyDefaults(), diagram flux "+node.name+" type mismatch in model, using location anyway");
				}
				break;
			}
			case NodeReference.SPECIES_CONTEXT_NODE:{
				obj = getModel().getSpeciesContext(node.name);
				break;
			}
		}
		Shape shape = getShapeFromModelObject(obj);
		if (shape!=null){
			shape.setLocation(node.location);
		}
	}
}


/**
 * This method was created by a SmartGuide.
 * @param o java.util.Observable
 * @param obj java.lang.Object
 */
public void cleanupAll() {

	if(getStructure() != null){
		getStructure().removePropertyChangeListener(this);
		if (getStructure() instanceof Membrane) {
			Membrane membrane = (Membrane) getStructure();
			if(membrane.getMembraneVoltage() != null){
				membrane.getMembraneVoltage().removePropertyChangeListener(this);
			}
			
			Feature inside = membrane.getInsideFeature();
			if(inside != null){
				inside.removePropertyChangeListener(this);
			}
				
			Feature outside = membrane.getOutsideFeature();
			if(outside != null){
				outside.removePropertyChangeListener(this);
			}

		}
	}
	
	if(getModel() != null){
		
		getModel().removePropertyChangeListener(this);
		
		SpeciesContext structSpeciesContext[] = getModel().getSpeciesContexts(structure);
		if(structSpeciesContext != null){
			for (int i=0;i<structSpeciesContext.length;i++){
					structSpeciesContext[i].removePropertyChangeListener(this);
					structSpeciesContext[i].getSpecies().removePropertyChangeListener(this);
			}
		}

		
		ReactionStep reactionSteps[] = getModel().getReactionSteps();
		if(reactionSteps != null){
			for (int i=0;i<reactionSteps.length;i++){
				reactionSteps[i].removePropertyChangeListener(this);
			}
		}
	}
}


/**
 * This method was created in VisualAge.
 * @return cbit.vcell.model.Structure
 */
public Structure getStructure() {
	return structure;
}


/**
 * This method was created by a SmartGuide.
 * @param g java.awt.Graphics
 */
public void paint(java.awt.Graphics2D g, GraphPane canvas) {

	super.paint(g,canvas);

	setRandomLayout(false);

}


/**
 * This method was created in VisualAge.
 * @param event java.beans.PropertyChangeEvent
 */
public void propertyChange(java.beans.PropertyChangeEvent event) {
	refreshAll();
}


/**
 * This method was created by a SmartGuide.
 * @param o java.util.Observable
 * @param obj java.lang.Object
 */
public void refreshAll() {

	try{
		
		if(getModel() == null || getStructure() == null){
			return;
		}
		//
		// 1) mark all shapes as dirty
		// 2) traverse model adding new shapes or cleaning those that already exist
		// 3) remove remaining dirty shapes
		//

		//
		//	mark all shapes as dirty
		//
		Enumeration enum_shapes = getShapes();
		while (enum_shapes.hasMoreElements()) {
			Shape shape = (Shape) enum_shapes.nextElement();
			shape.setDirty(true);
		}

		ContainerContainerShape containerShape = (ContainerContainerShape)getShapeFromModelObject(getModel());
		//
		// create all ReactionContainerShapes (one for each Structure)
		//
		Vector structList = new Vector();
		if (getStructure() instanceof Membrane) {
			//
			// add outside/membrane/inside containers (and add to a ContainerContainerShape)
			//
			Membrane membrane = (Membrane) getStructure();
			ReactionContainerShape membraneShape = (ReactionContainerShape) getShapeFromModelObject(membrane);
			if (membraneShape == null) {
				membraneShape = new ReactionContainerShape(membrane, this);
				addShape(membraneShape);
				membrane.removePropertyChangeListener(this);
				membrane.addPropertyChangeListener(this);
				membrane.getMembraneVoltage().removePropertyChangeListener(this);
				membrane.getMembraneVoltage().addPropertyChangeListener(this);
			}
			membraneShape.refreshLabel();
			membraneShape.setDirty(false);
			structList.addElement(membrane);
			
			Feature inside = membrane.getInsideFeature();
			ReactionContainerShape insideShape = (ReactionContainerShape) getShapeFromModelObject(inside);
			if (insideShape == null) {
				insideShape = new ReactionContainerShape(inside, this);
				addShape(insideShape);
				inside.removePropertyChangeListener(this);
				inside.addPropertyChangeListener(this);
			}
			insideShape.refreshLabel();
			insideShape.setDirty(false);
			structList.addElement(inside);
			
			Feature outside = membrane.getOutsideFeature();
			ReactionContainerShape outsideShape = (ReactionContainerShape) getShapeFromModelObject(outside);
			if (outsideShape == null) {
				outsideShape = new ReactionContainerShape(outside, this);
				addShape(outsideShape);
				outside.removePropertyChangeListener(this);
				outside.addPropertyChangeListener(this);
			}
			outsideShape.refreshLabel();
			outsideShape.setDirty(false);
			structList.addElement(outside);
			
			if (containerShape == null) {
				containerShape = new ContainerContainerShape(this, getModel(), insideShape, membraneShape, outsideShape);
				addShape(containerShape);
			}
		} else {
			ReactionContainerShape featureShape = (ReactionContainerShape) getShapeFromModelObject(getStructure());
			if (featureShape == null) {
				featureShape = new ReactionContainerShape(getStructure(), this);
				addShape(featureShape);
				getStructure().removePropertyChangeListener(this);
				getStructure().addPropertyChangeListener(this);
			}
			featureShape.refreshLabel();
			featureShape.setDirty(false);
			structList.addElement(getStructure());

			if (containerShape == null) {
				containerShape = new ContainerContainerShape(this, getModel(), featureShape);
				addShape(containerShape);
			}
		}
		containerShape.refreshLabel();
		containerShape.setDirty(false);
		//
		// add all species context shapes within the structures
		//
		Enumeration enum_struct = structList.elements();
		while (enum_struct.hasMoreElements()) {
			Structure structure = (Structure) enum_struct.nextElement();
			ReactionContainerShape reactionContainerShape = (ReactionContainerShape) getShapeFromModelObject(structure);
			SpeciesContext structSpeciesContext[] = getModel().getSpeciesContexts(structure);
			for (int i=0;i<structSpeciesContext.length;i++){
				SpeciesContextShape ss = (SpeciesContextShape) getShapeFromModelObject(structSpeciesContext[i]);
				if (ss == null) {
					ss = new SpeciesContextShape(structSpeciesContext[i], this);
					structSpeciesContext[i].removePropertyChangeListener(this);
					structSpeciesContext[i].addPropertyChangeListener(this);
					structSpeciesContext[i].getSpecies().removePropertyChangeListener(this);
					structSpeciesContext[i].getSpecies().addPropertyChangeListener(this);
					reactionContainerShape.addChildShape(ss);
					addShape(ss);
					ss.screenPos = reactionContainerShape.getRandomPosition();
				}
				ss.refreshLabel();
				ss.setDirty(false);
			}
		}

		//
		// add all reactionSteps that are in this structure (ReactionContainerShape), and draw the lines
		//
		//
		getModel().removePropertyChangeListener(this);
		getModel().addPropertyChangeListener(this);
		ReactionStep reactionSteps[] = getModel().getReactionSteps();
		for (int i=0;i<reactionSteps.length;i++){
			ReactionStep reactionStep = reactionSteps[i];
			ReactionContainerShape reactionContainerShape = (ReactionContainerShape) getShapeFromModelObject(getStructure());
			if (reactionStep.getStructure() == getStructure()) {
				//
				// add reaction steps as 'kinetic nodes'
				//
				ReactionStepShape reactionStepShape = (ReactionStepShape) getShapeFromModelObject(reactionStep);
				if (reactionStepShape == null) {
					if (reactionStep instanceof SimpleReaction) {
						reactionStepShape = new SimpleReactionShape((SimpleReaction) reactionStep, this);
					} else if (reactionStep instanceof FluxReaction) {
						reactionStepShape = new FluxReactionShape((FluxReaction) reactionStep, this);
					} else {
						throw new RuntimeException("unknown type of ReactionStep '" + reactionStep.getClass().toString());
					}
					addShape(reactionStepShape);
					reactionStep.addPropertyChangeListener(this);
					reactionStepShape.screenPos = reactionContainerShape.getRandomPosition();
					reactionContainerShape.addChildShape(reactionStepShape);
				}
				reactionStepShape.refreshLabel();
				reactionStepShape.setDirty(false);
				//
				// add reaction participants as edges
				//

				ReactionParticipant rp_Array[] = reactionStep.getReactionParticipants();
				
				for (int j = 0; j < rp_Array.length; j++) {
					SpeciesContext speciesContext = getModel().getSpeciesContext(rp_Array[j].getSpecies(),rp_Array[j].getStructure());
					//
					// add speciesContextShapes that are not in this structure, but are referenced from the reactionParticipants
					// these are only when reactionParticipants are from features that are outside of the membrane being displayed
					//
					SpeciesContextShape speciesContextShape = (SpeciesContextShape) getShapeFromModelObject(speciesContext);
					if (speciesContextShape == null) {
						speciesContextShape = new SpeciesContextShape(speciesContext, this);
						reactionContainerShape.addChildShape(speciesContextShape);
						addShape(speciesContextShape);
						speciesContextShape.screenPos = reactionContainerShape.getRandomPosition();
					}
					speciesContextShape.refreshLabel();
					speciesContextShape.setDirty(false);
					ReactionParticipantShape reactionParticipantShape = (ReactionParticipantShape) getShapeFromModelObject(rp_Array[j]);
					if (reactionParticipantShape == null) {
						if (rp_Array[j] instanceof Reactant) {
							reactionParticipantShape = new ReactantShape((Reactant) rp_Array[j], reactionStepShape, speciesContextShape, this);
						} else if (rp_Array[j] instanceof Product) {
							reactionParticipantShape = new ProductShape((Product) rp_Array[j], reactionStepShape, speciesContextShape, this);
						} else if (rp_Array[j] instanceof Catalyst) {
							reactionParticipantShape = new CatalystShape((Catalyst) rp_Array[j], reactionStepShape, speciesContextShape, this);
						} else if (rp_Array[j] instanceof Flux) {
							reactionParticipantShape = new FluxShape((Flux) rp_Array[j], reactionStepShape, speciesContextShape, this);
						} else {
							throw new RuntimeException("unsupported ReactionParticipant " + rp_Array[j].getClass());
						}
						addShape(reactionParticipantShape);
						containerShape.addChildShape(reactionParticipantShape);
					}
					reactionParticipantShape.setDirty(false);
					reactionParticipantShape.refreshLabel();
				}
			}
		}

		//
		//	remove all dirty shapes (enumerations aren't editable), so build list and delete from that.
		//
		enum_shapes = getShapes();
		Vector deleteList = new Vector();
		while (enum_shapes.hasMoreElements()) {
			Shape shape = (Shape) enum_shapes.nextElement();
			if (shape.isDirty()) {
				deleteList.add(shape);
			}
		}
		for (int i = 0; i < deleteList.size(); i++){
			removeShape((Shape)deleteList.elementAt(i));
		}
		
		//
		// update diagram
		//
		Diagram diagram = getModel().getDiagram(getStructure());
		if (diagram!=null){
			applyDefaults(diagram);
		}

		fireGraphChanged(new GraphEvent(this));
		
	}catch(Throwable e){
		handleException(e);
	}
}


/**
 * This method was created in VisualAge.
 */
public void setPositionsFromReactionCartoon(Diagram diagram) {
	Vector nodeList = new Vector();
	java.util.Enumeration enum1 = getShapes();
	while (enum1.hasMoreElements()){
		Shape shape = (Shape)enum1.nextElement();
		if (shape instanceof cbit.vcell.model.render.FluxReactionShape){
			nodeList.addElement(new NodeReference(NodeReference.FLUX_REACTION_NODE,((cbit.vcell.model.FluxReaction)shape.getModelObject()).getName(),shape.getLocation()));
		}else if (shape instanceof cbit.vcell.model.render.SimpleReactionShape){
			nodeList.addElement(new NodeReference(NodeReference.SIMPLE_REACTION_NODE,((cbit.vcell.model.ReactionStep)shape.getModelObject()).getName(),shape.getLocation()));
		}else if (shape instanceof cbit.vcell.model.render.SpeciesContextShape){
			nodeList.addElement(new NodeReference(NodeReference.SPECIES_CONTEXT_NODE,((cbit.vcell.model.SpeciesContext)shape.getModelObject()).getName(),shape.getLocation()));
		}
	}
	diagram.setNodeReferences((NodeReference[])cbit.util.BeanUtils.getArray(nodeList,NodeReference.class));
}


/**
 * This method was created in VisualAge.
 */
public void setRandomLayout(boolean bRandomize) {

	try {
		//
		// assert random characteristics
		//
		Shape topShape = getTopShape();
		if (topShape instanceof ContainerShape){
			ContainerShape containerShape = (ContainerShape)topShape;
			containerShape.setRandomLayout(bRandomize);
		}
		
	}catch (Exception e){
		System.out.println("top shape not found");
		e.printStackTrace(System.out);
	}

}


/**
 * This method was created in VisualAge.
 * @param structure cbit.vcell.model.Structure
 */
public void setStructure(Structure newStructure) {

	if(this.structure != null){
		cleanupAll();
	}
	
	this.structure = newStructure;
		
	refreshAll();
}
}