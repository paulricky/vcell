package cbit.vcell.client.desktop.biomodel;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.awt.event.MouseAdapter;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.vcell.model.rbm.MolecularType;

import cbit.vcell.graph.SpeciesTypeLargeShape;


public class SwingPaintDemo {

	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				createAndShowGUI();
			}
		});
	}
		
	private static void createAndShowGUI() {
		System.out.println("Created GUI on EDT? "+ SwingUtilities.isEventDispatchThread());
		JFrame f = new JFrame("Swing Paint Demo");
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		f.setSize(250,250);
		f.add(new MyPanel());
        f.pack();
		f.setVisible(true);
	}
}

class MyPanel extends JPanel {
	
	MolecularType mt = new MolecularType("egfr");
	Graphics panelContext = getGraphics();
	SpeciesTypeLargeShape speciesTypeShape = new SpeciesTypeLargeShape(50, 50, mt, panelContext);

	public MyPanel() {
		setBorder(BorderFactory.createLineBorder(Color.black));
		
		addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				moveSquare(e.getX(),e.getY());
			}
		});
		addMouseMotionListener(new MouseAdapter() {
			public void mouseDragged(MouseEvent e) {
				moveSquare(e.getX(),e.getY());
			}
		});
	}
	
	private void moveSquare(int x, int y) {
		// current square state, stored as final variables to avoid repeat invocations of the same methods.
		final int CURR_X = speciesTypeShape.getX();
		final int CURR_Y = speciesTypeShape.getY();
		final int CURR_W = speciesTypeShape.getWidth();
		final int CURR_H = speciesTypeShape.getHeight();
		final int OFFSET = 1;
		
		if ((CURR_X!=x) || (CURR_Y!=y)) {
			repaint(CURR_X,CURR_Y,CURR_W+OFFSET,CURR_H+OFFSET);	// repaint background over the old square location.
			speciesTypeShape.setX(x);			// Update coordinates.
			speciesTypeShape.setY(y);
			repaint(speciesTypeShape.getX(), speciesTypeShape.getY(), 		// repaint the square at the new location.
					speciesTypeShape.getWidth()+OFFSET, speciesTypeShape.getHeight() + OFFSET);
			} 
		}

	public Dimension getPreferredSize() {
		return new Dimension(250,200);
	}
	
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		g.drawString("This is my custom Panel!", 10, 20);
		speciesTypeShape.paintSelf(g);
	}
}

