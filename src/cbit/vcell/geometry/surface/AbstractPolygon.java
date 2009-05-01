package cbit.vcell.geometry.surface;
/**
 * Insert the type's description here.
 * Creation date: (6/28/2003 12:08:02 AM)
 * @author: John Wagner
 */
public class AbstractPolygon implements Polygon, java.io.Serializable {
	private Node[] fieldNodes = new Node[0];

/**
 * Quadrilateral constructor comment.
 */
public AbstractPolygon(Node[] nodes) {
	super();
	// Just set the pointer...we can make a copy
	// of the array later if we decide to...
	fieldNodes = nodes;
}


/**
 * Insert the method's description here.
 * Creation date: (9/22/2005 11:37:45 AM)
 * @return cbit.vcell.geometry.Coordinate
 */
public org.vcell.util.Coordinate calculateCentroid() {

	if(getNodeCount() > 0){
		double x=0;
		double y=0;
		double z=0;
		for (int k = 0; k < getNodeCount(); k++){
			x+= getNodes(k).getX();
			y+= getNodes(k).getY();
			z+= getNodes(k).getZ();
		}
		return new org.vcell.util.Coordinate(x/getNodeCount(),y/getNodeCount(),z/getNodeCount());
	}

	return null;
}


/**
 * Insert the method's description here.
 * Creation date: (5/6/2004 2:14:34 PM)
 * @return double
 */
public double getArea() {
	//
	// using a single node as the reference, assumes a convex (or mildly concave) polygon
	//
	double area = 0.0;
	int N = getNodeCount();
	int j = 0;
	int i=(j+1)%N;
	for (int k = 0; k < getNodeCount(); k++){
		Node node_i = getNodes(i);
		Node node_j = getNodes(j);
		double vect1x = node_i.getX()-node_j.getX(); 
		double vect1y = node_i.getY()-node_j.getY(); 
		double vect1z = node_i.getZ()-node_j.getZ();
		Node node_i_plus_1 = getNodes((i+1)%N);
		double vect2x = node_i_plus_1.getX()-node_j.getX(); 
		double vect2y = node_i_plus_1.getY()-node_j.getY(); 
		double vect2z = node_i_plus_1.getZ()-node_j.getZ(); 
		double crossx = vect1y*vect2z - vect1z*vect2y;
		double crossy = -(vect1x*vect2z - vect1z*vect2x);
		double crossz = vect1x*vect2y - vect1y*vect2x;
		double length = Math.sqrt(crossx*crossx + crossy*crossy + crossz*crossz);
		area += length/2.0;
		i = (i+1)%N;
	}
	return area;
}


/**
 * Quadrilateral constructor comment.
 */
public int getNodeCount() {
	return(fieldNodes.length);
}


/**
 * Quadrilateral constructor comment.
 */
public Node[] getNodes() {
	return(fieldNodes);
}


/**
 * Quadrilateral constructor comment.
 */
public Node getNodes(int n) {
	return(fieldNodes[n]);
}


/**
 * Insert the method's description here.
 * Creation date: (7/7/2004 1:30:00 PM)
 * @return cbit.vcell.render.Vect3d
 */
public void getUnitNormal(cbit.vcell.render.Vect3d unitNormal) {
	//
	// using first triangle as the reference, assumes fairly planar
	//
	double area = 0.0;
	int N = getNodeCount();
	int j = 0;
	int i=(j+1)%N;
//	for (int k = 0; k < getNodeCount(); k++){
		Node node_i = getNodes(i);
		Node node_j = getNodes(j);
		double vect1x = node_i.getX()-node_j.getX(); 
		double vect1y = node_i.getY()-node_j.getY(); 
		double vect1z = node_i.getZ()-node_j.getZ();
		Node node_i_plus_1 = getNodes((i+1)%N);
		double vect2x = node_i_plus_1.getX()-node_j.getX(); 
		double vect2y = node_i_plus_1.getY()-node_j.getY(); 
		double vect2z = node_i_plus_1.getZ()-node_j.getZ(); 
		double crossx = vect1y*vect2z - vect1z*vect2y;
		double crossy = -(vect1x*vect2z - vect1z*vect2x);
		double crossz = vect1x*vect2y - vect1y*vect2x;
		double length = Math.sqrt(crossx*crossx + crossy*crossy + crossz*crossz);
		area += length/2.0;
		i = (i+1)%N;
//	}
	unitNormal.set(crossx/length,crossy/length,crossz/length);
}


/**
 * Quadrilateral constructor comment.
 */
public void reverseDirection() {
	for (int n = 0; n < getNodeCount()/2; n++) {
		Node node = fieldNodes[n];
		fieldNodes[n] = fieldNodes[getNodeCount() - 1 - n];
		fieldNodes[getNodeCount() - 1 - n] = node;
	}
}
}