package cbit.gui.graph.groups;

/* A shape representing a collapsible group of nodes
 * September 2010
 */

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Point;

import cbit.gui.ShapeLayoutUtil;
import cbit.gui.graph.GraphModel;
import cbit.gui.graph.LayoutException;
import cbit.gui.graph.Shape;
import cbit.gui.graph.visualstate.VisualState;
import cbit.gui.graph.visualstate.VisualState.PaintLayer;
import cbit.gui.graph.visualstate.imp.DefaultCollapsibleVisualState;

public class GroupShape extends Shape {

	public final Dimension size = new Dimension(20, 20);
	public final int rectangleWidth = 20;
	public final int rectangleHeight = 20;
	public final int labelPadding = 2;

	protected final VCNodeGroup nodeGroup;

	public GroupShape(GraphModel graphModel, VCNodeGroup nodeGroup) {
		super(graphModel);
		this.nodeGroup = nodeGroup;
		defaultBG = Color.BLUE;
		backgroundColor = defaultBG;
	}

	@Override
	public VisualState createVisualState() {
		return new DefaultCollapsibleVisualState(this, PaintLayer.NODE);
	}

	@Override
	public Object getModelObject() {
		return nodeGroup;
	}

	@Override
	public Dimension getPreferedSize(Graphics2D g) {
		return size;
	}

	@Override
	protected boolean isInside(Point p) {
		return p.x >= relativePos.x && p.x <= relativePos.x + shapeSize.width
				&& p.y >= relativePos.y
				&& p.y <= relativePos.y + shapeSize.height;
	}

	@Override
	public void paintSelf(Graphics2D graphics, int xAbs, int yAbs) {
		graphics.setColor(backgroundColor);
		if (!bNoFill) {
			graphics.fillRect(xAbs, yAbs, rectangleWidth, rectangleHeight);
		}
		graphics.setColor(forgroundColor);
		graphics.drawRect(xAbs, yAbs, rectangleWidth, rectangleHeight);

		labelPos = ShapeLayoutUtil.placeTextUnder(graphics, new Dimension(
				rectangleWidth, rectangleHeight), labelPadding, getLabel());

		graphics.drawString(getLabel(), xAbs + labelPos.x, yAbs + labelPos.y);

	}

	@Override
	public void refreshLabel() {
		setLabel(nodeGroup.getName());
	}

	@Override
	public void refreshLayout() throws LayoutException {
	}

}
