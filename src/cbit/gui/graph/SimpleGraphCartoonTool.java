package cbit.gui.graph;

/*
 * (C) Copyright University of Connecticut Health Center 2001.
 * All rights reserved.
 */
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import javax.swing.JViewport;

import org.vcell.util.gui.DialogUtils;

import com.genlogic.GraphLayout.GlgCube;
import com.genlogic.GraphLayout.GlgGraphEdge;
import com.genlogic.GraphLayout.GlgGraphLayout;
import com.genlogic.GraphLayout.GlgGraphNode;
import com.genlogic.GraphLayout.GlgPoint;

import edu.rpi.graphdrawing.Blackboard;
import edu.rpi.graphdrawing.Node;

public class SimpleGraphCartoonTool extends CartoonTool {
	private GraphModel graphModel = null;

	public static final String ANNEALER = "Annealer";
	public static final String CIRCULARIZER = "Circularizer";
	public static final String CYCLEIZER = "Cycleizer";
	public static final String FORCEDIRECT = "ForceDirect";
	public static final String LEVELLER = "Leveller";
	public static final String TRANSPOSE = "Transpose";
	public static final String RANDOMIZER = "Randomizer";
	public static final String RELAXER = "Relaxer";
	public static final String STABILIZER = "Stabilizer";

	// for dragging speciesContext's around
	private boolean bMoving = false;
	private Shape movingShape = null;
	private Point movingPointWorld = null;
	private Point movingOffsetWorld = null;

	// for dragging rectangle around
	private boolean bRectStretch = false;
	private RubberBandRectShape rectShape = null;

	// for dragging line around
	private Point endPointWorld = null;
	private int mode = -1;

	public SimpleGraphCartoonTool() {
		super();
	}

	@Override
	public GraphModel getGraphModel() {
		return graphModel;
	}

	public void transposeLayout() {
		// calculate offset and scaling so that resulting graph fits on canvas
		Enumeration<Shape> enumShapes = getGraphModel().getShapes();
		while (enumShapes.hasMoreElements()) {
			Shape shape = enumShapes.nextElement();
			if (ShapeUtil.isMovable(shape)) {
				Point location = shape.getLocation();
				int oldX = location.x;
				int oldY = location.y;
				location.y = oldX;
				location.x = oldY;
			}
		}
		getGraphPane().repaint();
	}

	public void layout(String layoutName) throws Exception {
		Blackboard bb = new Blackboard();
		HashMap<String, Shape> nodeShapeMap = new HashMap<String, Shape>();
		// add nodes
		boolean bHasSelections = getGraphModel().getSelectedShape() != null;
		Enumeration<Shape> shapeEnum = getGraphModel().getShapes();
		while (shapeEnum.hasMoreElements()) {
			Shape shape = shapeEnum.nextElement();
			edu.rpi.graphdrawing.Node newNode = null;
			if (ShapeUtil.isMovable(shape)) {
				if (!bHasSelections || shape.isSelected()) {
					newNode = bb.addNode(shape.getLabel());
				}
			}
			// initialize node location to current absolute position
			if (newNode != null) {
				newNode.XY(shape.getAbsLocation().x, shape.getAbsLocation().y);
				nodeShapeMap.put(newNode.label(), shape);
			}
		}
		// add edges
		shapeEnum = getGraphModel().getShapes();
		while (shapeEnum.hasMoreElements()) {
			Shape shape = shapeEnum.nextElement();
			if (shape instanceof EdgeShape) {
				EdgeShape eShape = (EdgeShape) shape;
				Shape node1Shape = eShape.startShape;
				Shape node2Shape = eShape.endShape;
				if (!bHasSelections
						|| (node1Shape.isSelected() && node2Shape.isSelected())) {
					bb.addEdge(node1Shape.getLabel(), node2Shape.getLabel());
				}
			}
		}
		bb.setArea(0, 0, getGraphPane().getWidth(), getGraphPane().getHeight());
		bb.globals.D(20);
		bb.addEmbedder(ANNEALER, new edu.rpi.graphdrawing.Annealer(bb));
		bb.addEmbedder(CIRCULARIZER, new edu.rpi.graphdrawing.Circularizer(bb));
		bb.addEmbedder(CYCLEIZER, new edu.rpi.graphdrawing.Cycleizer(bb));
		bb.addEmbedder(FORCEDIRECT, new edu.rpi.graphdrawing.ForceDirect(bb));
		bb.addEmbedder(LEVELLER, new edu.rpi.graphdrawing.Leveller(bb));
		bb.addEmbedder(RANDOMIZER, new edu.rpi.graphdrawing.Randomizer(bb));
		bb.addEmbedder(RELAXER, new edu.rpi.graphdrawing.Relaxer(bb));
		bb.addEmbedder(STABILIZER, new edu.rpi.graphdrawing.Stabilizer(bb));
		bb.setEmbedding(layoutName);
		@SuppressWarnings("unchecked")
		List<Node> nodeList = bb.nodes();
		for (int i = 0; i < nodeList.size(); i++) {
			Node node = nodeList.get(i);
			System.out.println("Node " + node.label() + " @ (" + node.x() + ","
					+ node.y() + ")");
		}
		bb.PreprocessNodes();

		edu.rpi.graphdrawing.Embedder embedder = bb.embedder();
		embedder.Init();
		for (int i = 0; i < 1000; i++) {
			embedder.Embed();
		}

		bb.removeDummies();
		@SuppressWarnings("unchecked")
		Vector<Node> nodes = bb.nodes();
		nodeList = nodes;
		//
		// calculate offset and scaling so that resulting graph fits on canvas
		//
		double lowX = 100000;
		double highX = -100000;
		double lowY = 100000;
		double highY = -100000;
		for (int i = 0; i < nodeList.size(); i++) {
			Node node = nodeList.get(i);
			lowX = Math.min(lowX, node.x());
			highX = Math.max(highX, node.x());
			lowY = Math.min(lowY, node.y());
			highY = Math.max(highY, node.y());
		}
		double scaleX = getGraphPane().getWidth() / (1.5 * (highX - lowX));
		double scaleY = getGraphPane().getHeight() / (1.5 * (highY - lowY));
		int offsetX = getGraphPane().getWidth() / 6;
		int offsetY = getGraphPane().getHeight() / 6;
		for (int i = 0; i < nodeList.size(); i++) {
			Node node = nodeList.get(i);
			Shape shape = nodeShapeMap.get(node.label());
			Point parentLoc = shape.getParent().getAbsLocation();
			shape.setAbsLocation(new Point(
							(int) (scaleX * (node.x() - lowX)) + offsetX + parentLoc.x,
							(int) ((scaleY * (node.y() - lowY)) + offsetY + parentLoc.y)));
			System.out.println("Shape " + shape.getLabel() + " @ "
					+ shape.getAbsLocation());
		}
		getGraphPane().repaint();
	}

	public void layoutGlg() {
		// Create graph object
		GlgGraphLayout graph = new GlgGraphLayout();
		graph.SetUntangle(true); // true
		GlgCube graphDim = new GlgCube();
		GlgPoint newPoint = new GlgPoint(0, 0, 0);
		graphDim.p1 = newPoint;
		// newPoint = new com.genlogic.GlgPoint(getGraphPane().getWidth()-20,
		// getGraphPane().getHeight()-10, 0);//400,400,0
		newPoint = new GlgPoint(1600, 1600, 0);
		graphDim.p2 = newPoint;
		graph.dimensions = graphDim;
		// Add nodes (Vertex) to the graph
		Enumeration<Shape> shapeEnum = getGraphModel().getShapes();
		com.genlogic.GraphLayout.GlgGraphNode graphNode;
		java.util.HashMap<Shape, GlgGraphNode> nodeMap = new java.util.HashMap<Shape, GlgGraphNode>();

		while (shapeEnum.hasMoreElements()) {
			Shape shape = shapeEnum.nextElement();
			// add to the graph
			if (ShapeUtil.isMovable(shape)) {
				graphNode = graph.AddNode(null, 0, null);
			} else {
				continue;
			}
			// add to the hashmap
			nodeMap.put(shape, graphNode);
		}
		// Add edges
		shapeEnum = getGraphModel().getShapes();
		while (shapeEnum.hasMoreElements()) {
			Shape shape = shapeEnum.nextElement();
			if (shape instanceof EdgeShape) {
				EdgeShape eShape = (EdgeShape) shape;
				graph.AddEdge(nodeMap.get(eShape.startShape), nodeMap
						.get(eShape.endShape), null, 0, null);
			}
		}
		// call layout algorithm
		while (!graph.SpringIterate()) {
			;
		}
		graph.Update();
		// resize and scale the graph
		// com.genlogic.GlgObject edgeArray = graph.edge_array;
		@SuppressWarnings("unchecked")
		List<GlgGraphEdge> edgeVector = graph.edge_array;
		double distance, minDistance = Double.MAX_VALUE;
		for (int i = 0; i < edgeVector.size(); i++) {
			GlgGraphEdge edge = edgeVector.get(i);
			distance = Point2D.distance(edge.start_node.display_position.x,
					edge.start_node.display_position.y,
					edge.end_node.display_position.x,
					edge.end_node.display_position.y);
			minDistance = distance < minDistance ? distance : minDistance;
		}
		double ratio = 1.0;
		if (minDistance > 40) {
			ratio = 40.0 / minDistance;
		}
		// Update positions
		shapeEnum = getGraphModel().getShapes();
		Point place;
		com.genlogic.GraphLayout.GlgPoint glgPoint;
		while (shapeEnum.hasMoreElements()) {
			Shape shape = shapeEnum.nextElement();
			// test if it is contained in the nodeMap
			graphNode = nodeMap.get(shape);
			if (graphNode != null) {
				glgPoint = graph.GetNodePosition(graphNode);
				// glgPoint = graphNode.display_position;
				place = new Point();
				place.setLocation(glgPoint.x * ratio, glgPoint.y * ratio + 30);
				shape.setAbsLocation(place);
			}
		}
		Dimension graphSize = new Dimension((int) (1600 * ratio) + 50,
				(int) (1600 * ratio) + 50);
		getGraphPane().setSize(graphSize);
		getGraphPane().setPreferredSize(graphSize);
		// update the window
		getGraphPane().invalidate();
		((JViewport) getGraphPane().getParent()).revalidate();
	}

	@Override
	protected void menuAction(Shape shape, String menuAction) {
		// default action is to ignore
		System.out.println("unsupported menu action '" + menuAction
				+ "' on shape '" + shape + "'");
	}

	@Override
	public void mouseClicked(java.awt.event.MouseEvent event) {
		try {
			// if right mouse button, then do popup menu
			if ((event.getModifiers() & (InputEvent.BUTTON2_MASK | InputEvent.BUTTON3_MASK)) != 0) {
				return;
			}
			switch (mode) {
			case SELECT_MODE: {
				if (event.getClickCount() == 2) {
					Shape selectedShape = getGraphModel().getSelectedShape();
					if (selectedShape != null) {
						menuAction(selectedShape, PROPERTIES_MENU_ACTION);
					}
				}
				break;
			}
			default:
				break;
			}
		} catch (Exception e) {
			System.out.println("CartoonTool.mouseClicked: uncaught exception");
			e.printStackTrace(System.out);
			Point canvasLoc = getGraphPane().getLocationOnScreen();
			java.awt.Point screenPoint = new java.awt.Point(event.getX(), event
					.getY());
			canvasLoc.x += screenPoint.x;
			canvasLoc.y += screenPoint.y;
			DialogUtils.showErrorDialog(getGraphPane(), "Error:\n"
					+ e.getMessage(), e);
		}
	}

	@Override
	public void mouseDragged(java.awt.event.MouseEvent event) {
		if ((event.getModifiers() & (InputEvent.BUTTON2_MASK | InputEvent.BUTTON3_MASK)) != 0) {
			return;
		}
		boolean bShift = (event.getModifiers() & InputEvent.SHIFT_MASK) == InputEvent.SHIFT_MASK;
		boolean bCntrl = (event.getModifiers() & InputEvent.CTRL_MASK) == InputEvent.CTRL_MASK;
		//
		try {
			switch (mode) {
			case SELECT_MODE: {
				Point worldPoint = screenToWorld(event.getX(), event.getY());
				if (bMoving) {
					Shape selectedShapes[] = getGraphModel()
							.getAllSelectedShapes();
					// constrain to stay within the corresponding parent for the
					// "movingShape" as well as all other selected (hence
					// moving) shapes.
					Point movingParentLoc = movingShape.getParent()
							.getAbsLocation();
					Dimension movingParentSize = movingShape.getParent()
							.getSize();
					worldPoint.x = Math.max(movingOffsetWorld.x
							+ movingParentLoc.x, Math.min(movingOffsetWorld.x
							+ movingParentLoc.x + movingParentSize.width
							- movingShape.getSize().width, worldPoint.x));
					worldPoint.y = Math.max(movingOffsetWorld.y
							+ movingParentLoc.y, Math.min(movingOffsetWorld.x
							+ movingParentLoc.y + movingParentSize.height
							- movingShape.getSize().height, worldPoint.y));
					for (int i = 0; selectedShapes != null
							&& i < selectedShapes.length; i++) {
						if (selectedShapes[i] != movingShape) {
							Point selectedParentLoc = selectedShapes[i].getParent().getAbsLocation();
							Dimension selectedParentSize = selectedShapes[i].getParent().getSize();
							int selectedMovingOffsetX = movingOffsetWorld.x
									+ (movingShape.getAbsLocation().x - selectedShapes[i].getAbsLocation().x);
							int selectedMovingOffsetY = movingOffsetWorld.y
									+ (movingShape.getAbsLocation().y - selectedShapes[i].getAbsLocation().y);
							worldPoint.x = Math.max(selectedMovingOffsetX
									+ selectedParentLoc.x, Math.min(
											selectedMovingOffsetX
													+ selectedParentLoc.x
													+ selectedParentSize.width
													- selectedShapes[i]
															.getSize().width,
											worldPoint.x));
							worldPoint.y = Math.max(selectedMovingOffsetY
									+ selectedParentLoc.y, Math.min(
											selectedMovingOffsetY
													+ selectedParentLoc.y
													+ selectedParentSize.height
													- selectedShapes[i]
															.getSize().height,
											worldPoint.y));
						}
					}
					getGraphPane().setCursor(
							Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
					Point newMovingPoint = new Point(worldPoint.x
							- movingOffsetWorld.x, worldPoint.y
							- movingOffsetWorld.y);
					int deltaX = newMovingPoint.x - movingPointWorld.x;
					int deltaY = newMovingPoint.y - movingPointWorld.y;
					movingPointWorld = newMovingPoint;
					movingShape.setLocation(new Point(movingPointWorld.x
							- movingParentLoc.x, movingPointWorld.y
							- movingParentLoc.y));
					// for any other "movable" shapes that are selected, move
					// them also
					for (int i = 0; selectedShapes != null
							&& i < selectedShapes.length; i++) {
						if (selectedShapes[i] != movingShape) {
							selectedShapes[i]
									.setLocation(new Point(selectedShapes[i].getLocation().x
											+ deltaX, selectedShapes[i].getLocation().y
											+ deltaY));
						}
					}
					getGraphPane().invalidate();
					((JViewport) getGraphPane().getParent()).revalidate();
					getGraphPane().repaint();
				} else if (bRectStretch) {
					// constain to stay within parent
					Point parentLoc = rectShape.getParent().getAbsLocation();
					Dimension parentSize = rectShape.getParent().getSize();
					worldPoint.x = Math.max(1, Math.min(parentSize.width - 1,
							worldPoint.x - parentLoc.x))
							+ parentLoc.x;
					worldPoint.y = Math.max(1, Math.min(parentSize.height - 1,
							worldPoint.y - parentLoc.y))
							+ parentLoc.y;
					getGraphPane().setCursor(
							Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
					// getGraphPane().repaint();
					Graphics2D g = (Graphics2D) getGraphPane().getGraphics();
					java.awt.geom.AffineTransform oldTransform = g
							.getTransform();
					g.scale(0.01 * getGraphModel().getZoomPercent(),
							0.01 * getGraphModel().getZoomPercent());
					g.setXORMode(Color.white);
					rectShape.setEnd(endPointWorld);
					rectShape.paint(g, 0, 0);
					endPointWorld = worldPoint;
					rectShape.setEnd(endPointWorld);
					rectShape.paint(g, 0, 0);
					g.setTransform(oldTransform);
				} else {
					Shape shape = (getGraphModel().getSelectedShape() != null ? getGraphModel().getSelectedShape()
							: getGraphModel().pickWorld(worldPoint));
					if (!bCntrl && !bShift && (ShapeUtil.isMovable(shape))) {
						bMoving = true;
						movingShape = shape;
						movingPointWorld = shape.getAbsLocation();
						movingOffsetWorld = new Point(worldPoint.x
								- movingPointWorld.x, worldPoint.y
								- movingPointWorld.y);
					} else if (shape instanceof ContainerShape || bShift
							|| bCntrl) {
						bRectStretch = true;
						endPointWorld = new Point(worldPoint.x + 1,
								worldPoint.y + 1);
						rectShape = new RubberBandRectShape(worldPoint,
								endPointWorld, getGraphModel());
						rectShape.setEnd(endPointWorld);
						if (!(shape instanceof ContainerShape)) {
							shape.getParent().addChildShape(rectShape);
						} else {
							shape.addChildShape(rectShape);
						}
						Graphics2D g = (Graphics2D) getGraphPane().getGraphics();
						AffineTransform oldTransform = g.getTransform();
						g.scale(0.01 * getGraphModel().getZoomPercent(),
								0.01 * getGraphModel().getZoomPercent());
						g.setXORMode(Color.white);
						rectShape.paint(g, 0, 0);
						g.setTransform(oldTransform);
					}
				}
				break;
			}
			default: {
				break;
			}
			}
		} catch (Exception e) {
			System.out.println("CartoonTool.mouseDragged: uncaught exception");
			e.printStackTrace(System.out);
		}
	}

	@Override
	public void mousePressed(java.awt.event.MouseEvent event) {
		if (getGraphModel() == null) {
			return;
		}
		try {
			int eventX = event.getX();
			int eventY = event.getY();
			Point worldPoint = new Point(
					(int) (eventX * 100.0 / getGraphModel().getZoomPercent()),
					(int) (eventY * 100.0 / getGraphModel().getZoomPercent()));
			// Always select with MousePress
			boolean bShift = (event.getModifiers() & InputEvent.SHIFT_MASK) == InputEvent.SHIFT_MASK;
			boolean bCntrl = (event.getModifiers() & InputEvent.CTRL_MASK) == InputEvent.CTRL_MASK;
			if (mode == SELECT_MODE
					|| (event.getModifiers() & InputEvent.BUTTON1_MASK) != 0) {
				selectEventFromWorld(worldPoint, bShift, bCntrl);
			}
			// if mouse popupMenu event, popup menu
			if (event.isPopupTrigger() && mode == SELECT_MODE) {
				popupMenu(getGraphModel().getSelectedShape(), eventX, eventY);
				return;
			}
		} catch (Exception e) {
			System.out.println("CartoonTool.mousePressed: uncaught exception");
			e.printStackTrace(System.out);
		}
	}

	@Override
	public void mouseReleased(MouseEvent event) {
		if (getGraphModel() == null) {
			return;
		}
		try {
			// Pick shape
			int eventX = event.getX();
			int eventY = event.getY();
			java.awt.Point worldPoint = new Point(
					(int) (eventX * 100.0 / getGraphModel().getZoomPercent()),
					(int) (eventY * 100.0 / getGraphModel().getZoomPercent()));
			Shape pickedShape = getGraphModel().pickWorld(worldPoint);
			// if mouse popupMenu event, popup menu
			if (event.isPopupTrigger() && mode == SELECT_MODE) {
				if (pickedShape == getGraphModel().getSelectedShape()) {
					popupMenu(getGraphModel().getSelectedShape(), event.getX(),
							event.getY());
				}
				return;
			}
			if ((event.getModifiers() & (InputEvent.BUTTON2_MASK | InputEvent.BUTTON3_MASK)) != 0) {
				return;
			}
			// else do select and move
			switch (mode) {
			case SELECT_MODE: {
				getGraphPane().setCursor(Cursor.getDefaultCursor());
				if (bMoving) {
					getGraphPane().invalidate();
					((JViewport) getGraphPane().getParent()).revalidate();
					getGraphPane().repaint();
				} else if (bRectStretch) {
					Point absLoc = rectShape.getLocation();
					Dimension size = rectShape.getSize();
					// remove temporary rectangle
					getGraphModel().removeShape(rectShape);
					rectShape = null;
					Rectangle rect = new Rectangle(absLoc.x, absLoc.y,
							size.width, size.height);
					boolean bShift = (event.getModifiers() & InputEvent.SHIFT_MASK) == InputEvent.SHIFT_MASK;
					boolean bCntrl = (event.getModifiers() & InputEvent.CTRL_MASK) == InputEvent.CTRL_MASK;
					selectEventFromWorld(rect, bShift, bCntrl);
					getGraphPane().repaint();
				}
				bMoving = false;
				movingShape = null;
				bRectStretch = false;
				rectShape = null;
				break;
			}
			default: {
				break;
			}
			}
		} catch (Exception e) {
			System.out.println("CartoonTool.mouseReleased: uncaught exception");
			e.printStackTrace(System.out);
		}

	}

	private void selectEventFromWorld(Point worldPoint, boolean bShift,
			boolean bCntrl) {
		if (getGraphModel() == null) {
			return;
		}
		if (!bShift && !bCntrl) {
			//
			Shape pickedShape = getGraphModel().pickWorld(worldPoint);
			//
			if (pickedShape == null || !pickedShape.isSelected()) {
				getGraphModel().clearSelection();
			}
			if (pickedShape != null && pickedShape.isSelected()) {
				return;
			}
			if (pickedShape instanceof ContainerShape) {
				if (pickedShape.isSelected()) {
					getGraphModel().clearSelection();
					return;
				}
			}
			if (pickedShape != null) {
				getGraphModel().select(pickedShape);
			}

		} else if (bShift) {
			Shape pickedShape = getGraphModel().pickWorld(worldPoint);
			if (pickedShape == null) {
				return;
			}
			if (pickedShape instanceof ContainerShape) {
				return;
			}
			if (getGraphModel().getSelectedShape() instanceof ContainerShape) {
				getGraphModel().clearSelection();
			}
			getGraphModel().select(pickedShape);
		} else if (bCntrl) {
			Shape pickedShape = getGraphModel().pickWorld(worldPoint);
			if (pickedShape == null) {
				return;
			}
			if (pickedShape instanceof ContainerShape) {
				return;
			}
			if (pickedShape.isSelected()) {
				getGraphModel().deselect(pickedShape);
			} else {
				getGraphModel().select(pickedShape);
			}
		}
	}

	private void selectEventFromWorld(Rectangle rect, boolean bShift,
			boolean bCntrl) {
		if (!bShift && !bCntrl) {
			getGraphModel().clearSelection();
			Shape shapes[] = getGraphModel().pickWorld(rect);
			for (int i = 0; i < shapes.length; i++) {
				if (ShapeUtil.isMovable(shapes[i])) {
					getGraphModel().select(shapes[i]);
				}
			}
		} else if (bShift) {
			if (getGraphModel().getSelectedShape() instanceof ContainerShape) {
				getGraphModel().clearSelection();
			}
			Shape shapes[] = getGraphModel().pickWorld(rect);
			for (int i = 0; i < shapes.length; i++) {
				if (ShapeUtil.isMovable(shapes[i])) {
					getGraphModel().select(shapes[i]);
				}
			}
		} else if (bCntrl) {
			if (getGraphModel().getSelectedShape() instanceof ContainerShape) {
				getGraphModel().clearSelection();
			}
			Shape shapes[] = getGraphModel().pickWorld(rect);
			for (int i = 0; i < shapes.length; i++) {
				if (ShapeUtil.isMovable(shapes[i])) {
					if (shapes[i].isSelected()) {
						getGraphModel().deselect(shapes[i]);
					} else {
						getGraphModel().select(shapes[i]);
					}
				}
			}
		}
	}

	public void setGraphModel(GraphModel graphModel) {
		this.graphModel = graphModel;
	}

	@Override
	protected boolean shapeHasMenuAction(Shape shape, String menuAction) {
		return false;
	}

	@Override
	protected boolean shapeHasMenuActionEnabled(Shape shape, String menuAction) {
		return false;
	}

	@Override
	public void updateMode(int newMode) {
		if (newMode == mode) {
			return;
		}
		bMoving = false;
		movingShape = null;
		bRectStretch = false;
		rectShape = null;
		endPointWorld = null;
		if (getGraphModel() != null) {
			getGraphModel().clearSelection();
		}
		this.mode = newMode;
		if (getGraphPane() != null) {
			switch (mode) {
			case SELECT_MODE: {
				getGraphPane().setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
				break;
			}
			default: {
				System.out.println("ERROR: mode " + newMode + "not defined");
				break;
			}
			}
		}
		return;
	}
}