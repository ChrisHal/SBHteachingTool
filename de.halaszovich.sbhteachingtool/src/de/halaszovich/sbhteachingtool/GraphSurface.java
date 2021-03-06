package de.halaszovich.sbhteachingtool;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Stroke;
//import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;
import java.util.Arrays;

import javax.swing.JPanel;

public class GraphSurface extends JPanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1013465855100861096L;
	private static final double MIN_PH = 7.0, MAX_PH=7.9, STD_PH=7.4,
			MIN_pCO2=13,MAX_pCO2=80, STD_pCO2=40,
			MIN_BE=-15e-3,MAX_BE=15e-3, STD_BE=0,
			MIN_HCO3=.008,MAX_HCO3=.041,STD_HCO3=.024;
	private static final int NUMTICKS=5;
	public boolean pCO2_uselogscale=false, HCO3_uselogscale=false;
	private static final int CROSS_OFFSET=5, // offset to use when drawing a cross
			POINTSTOKEEP=200; // number of data points that will be kept in buffer for trace drawing
	private int AxisOffsetLeft, AxisOffsetBottom, AxisOffsetRight, AxisOffsetTop, LeftAxisGap;
	private MainWindow Parent;
	private Font LabelFont=null;
	
	// for practical reasons, we will keep the data required to draw
	// the "trails" also in this class.
	
	private int insertPos;
	private double[] ph=null,pco2=null,be=null,hco3=null; // will be used as ring-buffer
	
	public GraphSurface(MainWindow parent, int axisOffsetLeft, int axisOffsetBottom,
			int axisOffsetRight, int axisOffsetTop, int leftAxisGap) {
		super();
		Parent=parent;
		AxisOffsetLeft = axisOffsetLeft;
		AxisOffsetBottom = axisOffsetBottom;
		AxisOffsetRight = axisOffsetRight;
		AxisOffsetTop = axisOffsetTop;
		LeftAxisGap = leftAxisGap;
		LabelFont = new Font("Arial", Font.PLAIN, 16); //$NON-NLS-1$
		insertPos=0;
		ph=new double[POINTSTOKEEP];
		Arrays.fill(ph, STD_PH);
		pco2=new double[POINTSTOKEEP];
		Arrays.fill(pco2, STD_pCO2);
		be=new double[POINTSTOKEEP];
		Arrays.fill(be,STD_BE);
		hco3=new double[POINTSTOKEEP];
		Arrays.fill(hco3, STD_HCO3);
	}

	public void resetGraph() {
		Arrays.fill(ph, STD_PH);
		Arrays.fill(pco2, STD_pCO2);
		Arrays.fill(be,STD_BE);
		Arrays.fill(hco3, STD_HCO3);
	}
	
	private void drawCross(Graphics2D g2d, int x, int y) {
		Stroke oldStroke=g2d.getStroke();
		g2d.setStroke(new BasicStroke(3.0f));
		g2d.drawLine(x-CROSS_OFFSET, y-CROSS_OFFSET, x+CROSS_OFFSET, y+CROSS_OFFSET);
		g2d.drawLine(x-CROSS_OFFSET, y+CROSS_OFFSET, x+CROSS_OFFSET, y-CROSS_OFFSET);
		g2d.setStroke(oldStroke);
	}
	private void drawTrace(Graphics2D g2d, double[] xdata, double[] ydata, CoordinateScaler xs, CoordinateScaler ys) {
		Path2D p = new Path2D.Double();
		// note that insertPos points to the oldest datapoint
		p.moveTo(xs.scale(xdata[insertPos]), ys.scale(ydata[insertPos]));
		int i;
		for(i=1;i<POINTSTOKEEP;++i) {
			p.lineTo(xs.scale(xdata[(insertPos+i)%POINTSTOKEEP]),ys.scale(ydata[(insertPos+i)%POINTSTOKEEP]));
		}
		g2d.draw(p);
	}
	
	// Grab data from parent MainWindow and store it in local ring buffer
	public void grabData() {
		ph[insertPos]=-Math.log10(Parent.getHp());
		pco2[insertPos]=Parent.getpCO2();
		be[insertPos]=Parent.getBE();
		hco3[insertPos]=Parent.getHCO3();
		insertPos=(insertPos+1)%POINTSTOKEEP;
	}
	
	private void doDrawing(Graphics g) {

		Graphics2D g2d = (Graphics2D) g;
		g2d.setStroke(new BasicStroke(2.0f));
		Stroke oldstroke=g2d.getStroke();
		float dash[] ={5.0f};
		Stroke brokenline= new BasicStroke(1.0f, BasicStroke.CAP_BUTT,
		        BasicStroke.JOIN_MITER, 10.0f, dash, 0.0f);
		g2d.setFont(LabelFont);
		FontMetrics fm=g2d.getFontMetrics();
		int spacing=fm.stringWidth("n"); //$NON-NLS-1$
		g2d.setColor(Color.black);

		Dimension size = getSize();
		Insets insets = getInsets();

		int w = size.width - insets.left - insets.right;
		int h = size.height - insets.top - insets.bottom;
		int verAxisLen=(h-AxisOffsetTop-AxisOffsetBottom-2*LeftAxisGap-2*fm.getHeight())/3;
		
		// 1st: top coordinate system, pCO2
		int 	pxLeft=AxisOffsetLeft,
				pxRight=w-AxisOffsetRight,
				pxTop=AxisOffsetTop+fm.getHeight(),
				pxBottom=pxTop+verAxisLen; // corners of system in pixels
		String leftlabel, rightlabel,toplabel,bottomlabel;
		int labelx,labely;
		
		g2d.drawLine(pxLeft, pxTop, pxRight, pxTop); // horizontal line
		
		g2d.drawLine(pxLeft, pxTop, pxLeft, pxBottom);
		g2d.drawString("pCO2", pxLeft+spacing, (pxBottom+pxTop)/2); //$NON-NLS-1$
		CoordinateScaler xscale=new CoordinateScaler(pxLeft,pxRight,MIN_PH,MAX_PH, false);
		CoordinateScaler yscale=new CoordinateScaler(pxBottom,pxTop,MIN_pCO2,MAX_pCO2, pCO2_uselogscale);
		
		// add values to axis
		leftlabel=String.format("%.1f",MIN_PH); //$NON-NLS-1$
		rightlabel=String.format("%.1f", MAX_PH); //$NON-NLS-1$
		labelx=pxLeft;labely=AxisOffsetTop+fm.getAscent();
		g2d.drawString("pH", (pxLeft+pxRight)/2, labely); //$NON-NLS-1$
		g2d.drawString(leftlabel, labelx, labely);
		labelx=pxRight-fm.stringWidth(rightlabel);
		g2d.drawString(rightlabel, labelx, labely);
		toplabel=String.format("%.0fmmHg", MAX_pCO2); //$NON-NLS-1$
		bottomlabel=String.format("%.0fmmHg", MIN_pCO2); //$NON-NLS-1$
		labelx=pxLeft+spacing/2;
		labely=pxTop+fm.getAscent();
		g2d.drawString(toplabel, labelx, labely);
		labely=pxBottom-fm.getDescent();
		g2d.drawString(bottomlabel, labelx, labely);
		
		// add some sub ticks
		double tickincr=(MAX_pCO2-MIN_pCO2)/NUMTICKS;
		int i;
		for(i=0;i<NUMTICKS;++i) {
			int y=(int)yscale.scale(MIN_pCO2+i*tickincr);
			g2d.drawLine(pxLeft, y, pxLeft+spacing/2, y);
		}
		
		int stdX=(int)xscale.scale(STD_PH);
		int stdY=(int)yscale.scale(STD_pCO2);
		g2d.setStroke(brokenline);
		g2d.drawLine(stdX, pxTop, stdX, pxBottom);
		g2d.drawLine(pxLeft,stdY,pxRight,stdY);
		g2d.setStroke(oldstroke);
		double xValue= -Math.log10(Parent.getHp()); // i.e. the pH
		double yValue=Parent.getpCO2();
		int pxX=(int)xscale.scale(xValue), pxY=(int)yscale.scale(yValue);
		drawCross(g2d,pxX,pxY);
		drawTrace(g2d,this.ph,this.pco2,xscale,yscale);
		
		
		// 2nd: middle coordinate system, HCO3 aktuell
		g2d.setColor(Color.red);
		pxTop=pxBottom+LeftAxisGap;
		pxBottom=pxTop+verAxisLen;
		g2d.drawLine(pxLeft,pxTop,pxLeft,pxBottom);
	
		final String label=Messages.getString("GraphSurface.labelHCO3"); //$NON-NLS-1$
		g2d.drawString(label,pxLeft+CROSS_OFFSET, (pxBottom+pxTop)/2);
		yscale=new CoordinateScaler(pxBottom,pxTop,MIN_HCO3,MAX_HCO3, HCO3_uselogscale);
		toplabel=String.format("%2.0fmM", 1000.0*MAX_HCO3); //$NON-NLS-1$
		bottomlabel=String.format("%2.0fmM", 1000.0*MIN_HCO3); //$NON-NLS-1$
		labely=pxTop+fm.getAscent();
		g2d.drawString(toplabel, labelx, labely);
		labely=pxBottom-fm.getDescent();
		g2d.drawString(bottomlabel, labelx, labely);
		stdY=(int)yscale.scale(STD_HCO3);
		g2d.setStroke(brokenline);
		g2d.drawLine(stdX,pxTop,stdX,pxBottom);
		g2d.drawLine(pxLeft,stdY,pxRight,stdY);
		g2d.setStroke(oldstroke);
		xValue=Parent.getHCO3();
		pxY=(int)yscale.scale(xValue);
		drawCross(g2d,pxX,pxY);
		drawTrace(g2d,this.ph,this.hco3,xscale,yscale);
		// add some sub ticks
		tickincr=(MAX_HCO3-MIN_HCO3)/NUMTICKS;
		for(i=0;i<=NUMTICKS;++i) {
			int y=(int)yscale.scale(MIN_HCO3+i*tickincr);
			g2d.drawLine(pxLeft, y, pxLeft+spacing/2, y);
		}
		
		// 3nd: bottom coordinate system, BE
		g2d.setColor(Color.blue);
		pxTop=pxBottom+LeftAxisGap;
		pxBottom=pxTop+verAxisLen;
		g2d.setStroke(brokenline);
		leftlabel=String.format("%.1f",MIN_PH); //$NON-NLS-1$
		rightlabel=String.format("%.1f", MAX_PH); //$NON-NLS-1$
		labelx=pxLeft;labely=pxBottom+fm.getAscent();
		g2d.drawString(leftlabel, labelx, labely);
		labelx=pxRight-fm.stringWidth(rightlabel);
		g2d.drawString(rightlabel, labelx, labely);
		g2d.drawString("pH", (pxLeft+pxRight)/2, labely); //$NON-NLS-1$
		g2d.drawLine(stdX, pxTop, stdX, pxBottom);
		g2d.drawString("BE", pxLeft+CROSS_OFFSET, (pxBottom+pxTop)/2); //$NON-NLS-1$
		yscale=new CoordinateScaler(pxBottom,pxTop,MIN_BE,MAX_BE, false);
		toplabel=String.format("%2.0fmM", 1000*MAX_BE); //$NON-NLS-1$
		bottomlabel=String.format("%2.0fmM", 1000*MIN_BE); //$NON-NLS-1$
		labelx=pxLeft+spacing/2;
		labely=pxTop+fm.getAscent();
		g2d.drawString(toplabel, labelx, labely);
		labely=pxBottom-fm.getDescent();
		g2d.drawString(bottomlabel, labelx, labely);	
		stdY=(int)yscale.scale(STD_BE);
		g2d.drawLine(pxLeft,stdY,pxRight,stdY);
		g2d.setStroke(oldstroke);
		yValue=Parent.getBE();
		pxY=(int)yscale.scale(yValue);
		g2d.drawLine(pxLeft, pxBottom, pxLeft, pxTop);
		g2d.drawLine(pxLeft, pxBottom, pxRight, pxBottom);
		// add some sub ticks
		tickincr=(MAX_BE-MIN_BE)/NUMTICKS;
		for(i=1;i<=NUMTICKS;++i) {
			int y=(int)yscale.scale(MIN_BE+i*tickincr);
			g2d.drawLine(pxLeft, y, pxLeft+spacing/2, y);
		}
		
		drawCross(g2d,pxX,pxY);
		drawTrace(g2d,this.ph,this.be,xscale,yscale);
	}

	@Override
	public void paintComponent(Graphics g) {

		super.paintComponent(g);
		doDrawing(g);
	}
}
