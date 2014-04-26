package de.halaszovich.sbhteachingtool;

import java.awt.Component;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTextPane;
import javax.swing.ToolTipManager;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.apache.commons.math3.analysis.UnivariateFunction;
import org.apache.commons.math3.analysis.solvers.BracketingNthOrderBrentSolver;
import org.apache.commons.math3.analysis.solvers.UnivariateSolver;

import java.awt.Color;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;


public class MainWindow implements ChangeListener {

	private JFrame frmSBDemo;
	private JLabel lblpH, lblCO2, lblpCO2, lblBE, lblHCO3;
	private JSlider sliderSBE, sliderCO2;
	private GraphSurface graphSurface;
	private static final double STD_CO2=0.0012;
	private double Hp=1e-7, HA, pCO2, CO2=STD_CO2, Am, HCO3, SBE;
	
	public double getBE() {
		return Am+HCO3-0.048;
	}

	// some constants
	static final double K1 = 3.98107e-08; // pK1=7.4
	static final double K2 = 7.94328e-07;	// pK2=6.1
	static final double Kw = 2.3e-14;	// dissos. of water at 37dC
	static final double tot = 0.048; // total buffer base = [A-]+[HA]
	static final double stdSID = 0.04796; // strong ion differenz, tailored to make HA=0.024 for std. conditions
	static final double CO2concToPressure = 33333.3; // factor to convert mol/L to mmHg
//	private static final String APPNAME = "Säure Base Demo";
	private JPanel panelControls;




	public double getHp() {
		return Hp;
	}

	public void setHp(double hp) {
		Hp = hp;
	}

	public double getHA() {
		return HA;
	}

	public void setHA(double hA) {
		HA = hA;
	}

	public double getpCO2() {
		return pCO2;
	}

	public void setpCO2(double pCO2) {
		this.pCO2 = pCO2;
	}

	public double getCO2() {
		return CO2;
	}

	public void setCO2(double cO2) {
		CO2 = cO2;
	}

	public double getAm() {
		return Am;
	}

	public void setAm(double am) {
		Am = am;
	}

	public double getHCO3() {
		return HCO3;
	}

	public void setHCO3(double hCO3) {
		HCO3 = hCO3;
	}

	public double getSBE() {
		return SBE;
	}

	public void setSBE(double sBE) {
		SBE = sBE;
	}
	
	private void setHpFromHA() {
		this.Hp=K1*this.HA/(tot-this.HA);
	}
	private void setHCO3FromHpCO2() {
		this.HCO3= K2*this.CO2/this.Hp;
	}
	
	//find root of this function to get HA = conc. of un-diss. buffer acid
	public class rootHA implements UnivariateFunction {
			private  MainWindow w=null;
			public  void setParent(MainWindow mw) {
				w=mw;
			}
		    public double value(double HA) {
		        double y = w.getSBE()+stdSID-K1*HA/(HA-tot)+Kw*(HA-tot)/K1/HA-tot+HA+K2*w.getCO2()*(HA-tot)/K1/HA;
		        return y;
		    }
		}
		
	private double calcHA() {
		MainWindow.rootHA function =  new MainWindow.rootHA();
		function.setParent(this);
		double relativeAccuracy = 1.0e-15;
		final double absoluteAccuracy = 1.0e-12;
		final int    maxOrder         = 5;
		UnivariateSolver solver   = new BracketingNthOrderBrentSolver(relativeAccuracy, absoluteAccuracy, maxOrder);
		double y = solver.solve(100, (UnivariateFunction)function, .001, .0479,.024);
		return y;
	}
	
	private void updateValues() {
		this.HA=this.calcHA();
		this.setHpFromHA();
		this.Am=tot-this.HA;
		this.setHCO3FromHpCO2();
		this.pCO2=CO2concToPressure*this.CO2;
	}
	
	private void updateValueDisplays() {
		this.lblpH.setText(String.format("pH = %.2f",(-Math.log10(this.Hp))));
		this.lblCO2.setText(String.format("[CO2] = %.1f mmol/L",this.CO2*1000.));
		this.lblpCO2.setText(String.format("pCO2 = %.0f mmHg",this.pCO2));
		this.lblHCO3.setText(String.format("[HCO3-]akt = %.1f mmol/L",this.HCO3*1000.));
		this.lblBE.setText(String.format("BE = %.1f mmol/L",1000.*(Am+HCO3-0.048)));
		this.graphSurface.repaint();
	}
	
	private void resetValues(){
		this.sliderSBE.setValue(0);
		this.sliderCO2.setValue(12);//(int)(1e4*STD_CO2));
		updateValues();
		this.graphSurface.resetGraph();
		updateValueDisplays();
	}
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					MainWindow window = new MainWindow();
					window.frmSBDemo.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public MainWindow() {
		initialize();
		updateValues();
		updateValueDisplays();
	}

	 /** Listen to the slider. */
    public void stateChanged(ChangeEvent e) {
        JSlider source = (JSlider)e.getSource();
        int val=source.getValue();
        if (source==this.sliderSBE) {
        	this.SBE=1e-3*(double)val;
        } else if(source==this.sliderCO2) {
        	this.CO2=1e-4*(double)val;
        }
        this.updateValues();
        this.graphSurface.grapData();
        this.updateValueDisplays();
        }
	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		ToolTipManager.sharedInstance().setDismissDelay(10000); // display tooltips for 10 sec
		final int height=510;
		frmSBDemo = new JFrame();
		frmSBDemo.setTitle("S\u00E4ure Base Demo");
		frmSBDemo.setBounds(100, 100, 708, height);
		frmSBDemo.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);;
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{316, 346, 0};
		gridBagLayout.rowHeights = new int[]{450, 0};
		gridBagLayout.columnWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
		gridBagLayout.rowWeights = new double[]{1.0, Double.MIN_VALUE};
		frmSBDemo.getContentPane().setLayout(gridBagLayout);
		
		panelControls = new JPanel();
		GridBagConstraints gbc_panelControls = new GridBagConstraints();
		gbc_panelControls.fill = GridBagConstraints.BOTH;
		gbc_panelControls.insets = new Insets(0, 0, 0, 5);
		gbc_panelControls.gridx = 0;
		gbc_panelControls.gridy = 0;
		frmSBDemo.getContentPane().add(panelControls, gbc_panelControls);
		panelControls.setLayout(null);
		panelControls.setPreferredSize(null);
		
		JTextPane txtLabelSliderSBE = new JTextPane();
		txtLabelSliderSBE.setToolTipText("<html>Positive Werte Simulieren eine metabolische Alkalose<br>"
				+"bzw. die Kompensation einer respiratorischen Azidose,<br>negative eine metabolische Azidose bzw. die<br>"
				+"Kompensation einer respiratorischen Alkalose.</html>");
		txtLabelSliderSBE.setEditable(false);
		txtLabelSliderSBE.setBackground(UIManager.getColor("Label.background"));
		txtLabelSliderSBE.setText("nicht-respiratorische Komponente:\n\u00DCberschuss starker Basen (mmol/L)");
		txtLabelSliderSBE.setBounds(48, 24, 269, 32);
		panelControls.add(txtLabelSliderSBE);
		//		uncomment to get center aligned text
		//		SimpleAttributeSet aSet = new SimpleAttributeSet();
		//        StyleConstants.setAlignment(aSet, StyleConstants.ALIGN_CENTER);
		//        StyledDocument doc = txtLabelSliderSBE.getStyledDocument();
		//        doc.setParagraphAttributes(0, 100, aSet, false);
				
				this.lblpH = new JLabel("pH: NaN");
				lblpH.setBounds(95, 331, 180, 16);
				panelControls.add(lblpH);
				
				this.lblHCO3 = new JLabel("[HCO3-] x mmol/L");
				lblHCO3.setBounds(95, 273, 180, 16);
				panelControls.add(lblHCO3);
				
				this.lblBE = new JLabel("BE x mmol/L");
				lblBE.setBounds(95, 302, 180, 16);
				panelControls.add(lblBE);
				
				this.lblpCO2 = new JLabel("pCO2");
				lblpCO2.setBounds(95, 244, 180, 16);
				panelControls.add(lblpCO2);
				
				this.lblCO2 = new JLabel("CO2: x mmol/L");
				lblCO2.setBounds(95, 215, 180, 16);
				panelControls.add(lblCO2);
				
				sliderSBE = new JSlider(JSlider.HORIZONTAL,-15,15,0);
				sliderSBE.setBounds(38, 56, 269, 52);
				panelControls.add(sliderSBE);
				sliderSBE.addChangeListener(this);
				//Turn on labels at major tick marks.
				sliderSBE.setMajorTickSpacing(5);
				sliderSBE.setMinorTickSpacing(1);
				sliderSBE.setPaintTicks(true);
				sliderSBE.setPaintLabels(true);
				
				JTextPane sliderLabel2 = new JTextPane();
				sliderLabel2.setToolTipText("<html>Werte >1,2 mmol/L (Hypoventilation, entspr. >40 mmHg pCO2)<br>"
						+"simulieren eine respiratorische Azidose oder Kompensation<br>"
						+"einer metabol. Alkalose, kleinere Werte (Hyperventilation) eine Alkalose oder<br>"
						+"die Kompensation einer metabol. Azidose.</html>");
				sliderLabel2.setText("respiratorische Komponente:\n[CO2] (0.1 mmol/L)");
				sliderLabel2.setBounds(48, 116, 269, 32);
				sliderLabel2.setEditable(false);
				sliderLabel2.setBackground(UIManager.getColor("Label.background"));
				panelControls.add(sliderLabel2);
				sliderLabel2.setAlignmentX(Component.CENTER_ALIGNMENT);
				
				this.sliderCO2 = new JSlider(JSlider.HORIZONTAL,4,24,12);
				sliderCO2.setBounds(38, 148, 269, 52);
				panelControls.add(sliderCO2);
				sliderCO2.addChangeListener(this);
				//Turn on labels at major tick marks.
				sliderCO2.setMajorTickSpacing(10);
				sliderCO2.setMinorTickSpacing(1);
				sliderCO2.setPaintTicks(true);
				sliderCO2.setPaintLabels(true);
				
				JButton btnReset = new JButton("reset");
				btnReset.setBounds(79, 374, 164, 29);
				panelControls.add(btnReset);
				btnReset.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent arg0) {
						resetValues();
					}
				});
		

		JMenuBar menuBar = new JMenuBar();
		frmSBDemo.setJMenuBar(menuBar);
		
		JMenu mnDatei = new JMenu("Datei");
		menuBar.add(mnDatei);
		
		JMenuItem mntmberSureBase = new JMenuItem("\u00DCber S\u00E4ure Base Demo...");
		mntmberSureBase.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JOptionPane.showMessageDialog(frmSBDemo,"Säure Base Demo\n(c) 2014 Christian R. Halaszovich","About",JOptionPane.PLAIN_MESSAGE);
			}
		});
		mnDatei.add(mntmberSureBase);

		this.graphSurface=new GraphSurface(this, 10,5,10,5,10);
		graphSurface.setBackground(Color.white);
		
		GridBagConstraints gbc_panel = new GridBagConstraints();
		gbc_panel.fill = GridBagConstraints.BOTH;
		gbc_panel.gridx = 1;
		gbc_panel.gridy = 0;
//		JPanel panel = new JPanel(); // comment out if in production, to avoid spurious warning
		frmSBDemo.getContentPane().add(graphSurface, gbc_panel); // use this line for working app
//		frmSBDemo.getContentPane().add(panel, gbc_panel); // use this line for GUI designer
		
	}
}
