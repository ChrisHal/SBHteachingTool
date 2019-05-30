package de.halaszovich.sbhteachingtool;

import java.awt.Component;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.KeyEvent;
import java.awt.event.ItemListener;

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
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import java.awt.Color;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;

import javax.swing.JSeparator;
import javax.swing.ImageIcon;
import javax.swing.border.TitledBorder;

import java.util.Hashtable;
import java.util.ResourceBundle;
import javax.swing.JCheckBox;


public class MainWindow extends JFrame implements ChangeListener, ItemListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public static final String 
		APP_NAME=Messages.getString("MainWindow.AppNAme"), //$NON-NLS-1$ // used eclipse's mechanism for this one
		APP_VERSION=Messages.getString("MainWindow.APP_VERSION"), //$NON-NLS-1$
		COPYRIGHT="(C) Christian R. Halaszovich", //$NON-NLS-1$
		INFO_SLIDERSBE=ResourceBundle.getBundle("de.halaszovich.sbhteachingtool.messages").getString("MainWindow.INFO_SLIDERSBE"); //$NON-NLS-1$ //$NON-NLS-2$
	static final String INFO_SLIDERCO2 = ResourceBundle.getBundle("de.halaszovich.sbhteachingtool.messages").getString("MainWindow.INFO_SLIDERCO2");; //$NON-NLS-1$ //$NON-NLS-2$
	private JFrame frmSBDemo;
	private JLabel lblpH, lblCO2, lblpCO2, lblBE, lblHCO3;
	private JSlider sliderSBE, sliderpCO2;
	private GraphSurface graphSurface;
	private static final double STD_CO2=0.0012;
	private double Hp=1e-7, HA, pCO2, CO2=STD_CO2, Am, HCO3, SBE;
	
	// some constants
	static final double K1 = 3.98107e-08; // pK1=7.4
	static final double K2 = 7.94328e-07;	// pK2=6.1
	static final double Kw = 2.3e-14;	// dissos. of water at 37dC
	static final double tot = 0.048; // total buffer base = [A-]+[HA]
	static final double stdSID = 0.04796; // strong ion differenz, tailored to make HA=0.024 for std. conditions
	static final double CO2concToPressure = 33333.3; // factor to convert mol/L to mmHg
	private static final double STD_pCO2 = 40.0;
	
	//private static final String APPNAME = "Säure Base Demo";
	private JPanel panelControls;
	private JCheckBox chckbxLogScale;

	public double getBE() {
		double be=Am+HCO3-MainWindow.tot;
		return be;
	}


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
		this.Hp = K1*this.HA/(tot-this.HA);
	}
	private void setHCO3FromHpCO2() {
		this.HCO3 = K2*this.CO2/this.Hp;
	}
		
	private double calcHA() {
		double sbe=this.getSBE(), co2=this.getCO2();
		double t1 = K1*K1,      t2 = sbe*sbe,
	      t3 = stdSID*stdSID,
	      t4 = K1*K2,
	      t5 = t1*tot,
	      t6 = K2*K2,
	      t7 = CO2*CO2,
	      t8 = tot*tot,
	      t9 = Math.sqrt(t1*t2+2.0*t1*sbe*stdSID+t1*t3+2.0*t4*co2*sbe+2.0*t4*co2*
	stdSID-2.0*t5*sbe-2.0*t5*stdSID+t6*t7+2.0*t4*co2*tot+t8*t1),
	      t10 = (-K1*sbe-K1*stdSID-K2*co2+tot*K1+t9)/K1/2.0;
		return t10;

	}
	
	private void updateValues() {
		this.HA=this.calcHA();
		this.setHpFromHA();
		this.Am=tot-this.HA;
		this.setHCO3FromHpCO2();
		this.pCO2=CO2concToPressure*this.CO2;
	}
	
	private void updateValueDisplays() {
		this.lblpH.setText(String.format("pH = %.2f",(-Math.log10(this.Hp)))); //$NON-NLS-1$
		this.lblCO2.setText(String.format("pCO2 = %.0f mmHg, [CO2] = %.1f mmol/L",this.pCO2,this.CO2*1000.)); //$NON-NLS-1$
		this.lblpCO2.setText(String.format("pCO2 = %.0f mmHg",this.pCO2)); //$NON-NLS-1$
		this.lblHCO3.setText(String.format(Messages.getString("MainWindow.LabelHCO3akt"),this.HCO3*1000.0)); //$NON-NLS-1$
		this.lblBE.setText(String.format("BE = %.1f mmol/L",Math.round(10000.0*this.getBE())/10.0));// round to nearest 0.1 //$NON-NLS-1$
				// some tricks to avoid leading zero
		this.graphSurface.repaint();
	}
	
	private void resetValues(){
		this.sliderSBE.setValue(0);
		this.sliderpCO2.setValue((int)STD_pCO2);//matches std. [CO2] of 1.2 mmol/l
		updateValues();
		this.graphSurface.resetGraph();
		updateValueDisplays();
	}
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			@Override
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

	static boolean isAtLeastJava1_7() {
		String version=System.getProperty("java.version");
		String[] parts=version.split("\\.",3);
		final int MAJORVER=1, MINORVER=7;
		return parts.length > 1 && 
				Integer.parseInt(parts[0])>=MAJORVER && Integer.parseInt(parts[1])>=MINORVER;
	}
	 /** Listen to the slider. */
    @Override
	public void stateChanged(ChangeEvent e) {
        JSlider source = (JSlider)e.getSource();
        int val=source.getValue();
        if (source==this.sliderSBE) {
        	this.SBE=1e-3*val;
        } else if(source==this.sliderpCO2) {
        	this.CO2=val/CO2concToPressure;
        }
        this.updateValues();
        this.graphSurface.grapData();
        this.updateValueDisplays();
        }

    /**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		final int height=510;
		frmSBDemo = new JFrame();
		frmSBDemo.setTitle(ResourceBundle.getBundle("de.halaszovich.sbhteachingtool.messages").getString("MainWindow.frmSBDemo.title")); //$NON-NLS-1$ //$NON-NLS-2$
		frmSBDemo.setBounds(100, 100, 708, height);
//		frmSBDemo.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frmSBDemo.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); // this seems to be the recommended behavior?
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
		
		final ImageIcon questionmark = new ImageIcon(MainWindow.class.getResource("/Question.gif")); //$NON-NLS-1$
		JButton btnHelpSBE = new JButton(""); //$NON-NLS-1$
		btnHelpSBE.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				JOptionPane.showMessageDialog(panelControls, MainWindow.INFO_SLIDERSBE, Messages.getString("MainWindow.TitleHelp1"), JOptionPane.INFORMATION_MESSAGE, questionmark); //$NON-NLS-1$
			}
		});	
		btnHelpSBE.setIcon(questionmark);
		btnHelpSBE.setBounds(267, 10, 30, 32);
		panelControls.add(btnHelpSBE);
		
		JButton btnHelpCO2 = new JButton(""); //$NON-NLS-1$
		btnHelpCO2.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				JOptionPane.showMessageDialog(panelControls, MainWindow.INFO_SLIDERCO2, Messages.getString("MainWindow.TtileHelp2"), JOptionPane.INFORMATION_MESSAGE, questionmark); //$NON-NLS-1$
			}
		});	
		btnHelpCO2.setIcon(questionmark);
		btnHelpCO2.setBounds(267, 121, 30, 32);
		panelControls.add(btnHelpCO2);
		
		JTextPane txtLabelSliderSBE = new JTextPane();
		txtLabelSliderSBE.setContentType("text/html"); //$NON-NLS-1$
		txtLabelSliderSBE.setEditable(false);
		txtLabelSliderSBE.setBackground(UIManager.getColor("Label.background")); //$NON-NLS-1$
		txtLabelSliderSBE.setText(ResourceBundle.getBundle("de.halaszovich.sbhteachingtool.messages").getString("MainWindow.txtLabelSliderSBE.text")); //$NON-NLS-1$ //$NON-NLS-2$
		txtLabelSliderSBE.setBounds(38, 10, 205, 32);
		panelControls.add(txtLabelSliderSBE);
		
		JPanel panel_1 = new JPanel();
		panel_1.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), ResourceBundle.getBundle("de.halaszovich.sbhteachingtool.messages").getString("MainWindow.panel_1.borderTitle"),  //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$
				TitledBorder.LEADING, TitledBorder.TOP, null, null));
		panel_1.setBounds(57, 266, 197, 137);
		panelControls.add(panel_1);
		panel_1.setLayout(null);
		//		uncomment to get center aligned text
		//		SimpleAttributeSet aSet = new SimpleAttributeSet();
		//        StyleConstants.setAlignment(aSet, StyleConstants.ALIGN_CENTER);
		//        StyledDocument doc = txtLabelSliderSBE.getStyledDocument();
		//        doc.setParagraphAttributes(0, 100, aSet, false);
				
		
		// NOTE: the label text will be dynamically set by updateValueDisplays()
		// so it does not matter what we put in here. We should put in some text to help
		// with GUI building.
		this.lblpH = new JLabel("pH: NaN"); //$NON-NLS-1$
		lblpH.setBounds(10, 110, 178, 16);
		panel_1.add(lblpH);

		this.lblHCO3 = new JLabel("[HCO3-] x mmol/L"); //$NON-NLS-1$
		lblHCO3.setBounds(10, 50, 178, 16);
		panel_1.add(lblHCO3);

		this.lblBE = new JLabel("BE x mmol/L"); //$NON-NLS-1$
		lblBE.setBounds(10, 80, 178, 16);
		panel_1.add(lblBE);

		this.lblpCO2 = new JLabel("pCO2"); //$NON-NLS-1$
		lblpCO2.setBounds(10, 20, 178, 16);
		panel_1.add(lblpCO2);

		this.lblCO2 = new JLabel("CO2: x mmol/L"); //$NON-NLS-1$
		lblCO2.setBounds(40, 210, 255, 16);
		panelControls.add(lblCO2);

		sliderSBE = new JSlider(SwingConstants.HORIZONTAL,-15,15,0);
		sliderSBE.setBounds(28, 45, 269, 52);
		panelControls.add(sliderSBE);
		sliderSBE.addChangeListener(this);
		//Turn on labels at major tick marks.
		sliderSBE.setMajorTickSpacing(5);
		sliderSBE.setMinorTickSpacing(1);
		sliderSBE.setPaintTicks(true);
		sliderSBE.setPaintLabels(true);

		JButton btnReset = new JButton(ResourceBundle.getBundle("de.halaszovich.sbhteachingtool.messages").
				getString("MainWindow.btnReset.text")); //$NON-NLS-1$ //$NON-NLS-2$
		btnReset.setBounds(73, 412, 164, 29);
		panelControls.add(btnReset);

		JSeparator separator = new JSeparator();
		separator.setBounds(10, 108, 290, 2);
		panelControls.add(separator);

		JSeparator separator_1 = new JSeparator();
		separator_1.setBounds(10, 254, 290, 2);
		panelControls.add(separator_1);
		
		this.sliderpCO2 = new JSlider(SwingConstants.HORIZONTAL,14,80,40);
		sliderpCO2.setBounds(28, 158, 269, 52);
		panelControls.add(sliderpCO2);
		sliderpCO2.addChangeListener(this);
		//Turn on labels at major tick marks.
		sliderpCO2.setMajorTickSpacing(2);
		// sliderpCO2.setMinorTickSpacing(5);
		 sliderpCO2.setPaintTicks(true);
		
		// setting up custom labels
		Hashtable<Integer, JLabel> labelTable = new Hashtable<Integer,JLabel>();
		int i;
		for(i=20;i<=80;i+=10) {
			labelTable.put(i, new JLabel(String.format("%d",i)));
		}
		sliderpCO2.setLabelTable(labelTable);
		sliderpCO2.setPaintLabels(true);
				
				JTextPane sliderLabel2 = new JTextPane();
				sliderLabel2.setContentType("text/html");
				sliderLabel2.setText(ResourceBundle.getBundle("de.halaszovich.sbhteachingtool.messages").getString("MainWindow.txtLabelSliderCO2")); //$NON-NLS-1$ //$NON-NLS-2$
				sliderLabel2.setBounds(38, 120, 205, 43);
				sliderLabel2.setEditable(false);
				sliderLabel2.setBackground(UIManager.getColor("Label.background")); //$NON-NLS-1$
				panelControls.add(sliderLabel2);
				sliderLabel2.setAlignmentX(Component.CENTER_ALIGNMENT);
				
				chckbxLogScale = new JCheckBox(ResourceBundle.getBundle("de.halaszovich.sbhteachingtool.messages").getString("MainWindow.chckbxLogScale.text")); //$NON-NLS-1$ //$NON-NLS-2$
				chckbxLogScale.setBounds(38, 227, 175, 21);
				chckbxLogScale.addItemListener(this);
				panelControls.add(chckbxLogScale);
		btnReset.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				resetValues();
			}
				});
		

		JMenuBar menuBar = new JMenuBar();
		frmSBDemo.setJMenuBar(menuBar);
		
		JMenu mnDatei = new JMenu(ResourceBundle.getBundle("de.halaszovich.sbhteachingtool.messages").getString("MainWindow.mnDatei.text")); //$NON-NLS-1$ //$NON-NLS-2$
		menuBar.add(mnDatei);
		char mnemonickey=Messages.getString("MainWindow.mnDatei").charAt(0);
		if (MainWindow.isAtLeastJava1_7()) {
			// supported in >=1.7
			mnDatei.setMnemonic(java.awt.event.KeyEvent.getExtendedKeyCodeForChar(mnemonickey));
		} else {
			//obsolete in >=1.7
			mnDatei.setMnemonic(mnemonickey);
		}
		mnemonickey=Messages.getString("MainWindow.mnDateiItem").charAt(0);
		JMenuItem mntmberAcidBase = new JMenuItem(ResourceBundle.getBundle("de.halaszovich.sbhteachingtool.messages").getString("MainWindow.mntmberSureBase.text")); //$NON-NLS-1$ //$NON-NLS-2$
		if(MainWindow.isAtLeastJava1_7()) {
			mntmberAcidBase.setMnemonic(
					java.awt.event.KeyEvent.getExtendedKeyCodeForChar(mnemonickey));
		} else {
			mntmberAcidBase.setMnemonic(mnemonickey);
		}
		mntmberAcidBase.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JOptionPane.showMessageDialog(frmSBDemo,
						String.format(Messages.getString("MainWindow.VersionFormatStr"), MainWindow.APP_NAME,  //$NON-NLS-1$
								MainWindow.APP_VERSION, System.getProperty("java.version"),
								MainWindow.COPYRIGHT),
						Messages.getString("MainWindow.TitleAbout"),JOptionPane.PLAIN_MESSAGE); //$NON-NLS-1$
			}
		});
		
		JMenuItem mntmReset = new JMenuItem(ResourceBundle.getBundle("de.halaszovich.sbhteachingtool.messages").getString("MainWindow.mntmReset.text")); //$NON-NLS-1$ //$NON-NLS-2$
		mntmReset.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				resetValues();
			}
		});
		mntmReset.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R,ActionEvent.CTRL_MASK));
		mnDatei.add(mntmReset);
		mnDatei.add(mntmberAcidBase);
		
		JMenuItem mntmQuit = new JMenuItem(ResourceBundle.getBundle("de.halaszovich.sbhteachingtool.messages").getString("MainWindow.mntmQuit.text")); //$NON-NLS-1$ //$NON-NLS-2$
		mntmQuit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				frmSBDemo.dispose(); // seems to close app sometimes on Mac OS X
				//frmSBDemo.setVisible(false); // should trigger closing of app (not on Mac OS X)
			}
		});
		mntmQuit.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, ActionEvent.CTRL_MASK));
		mnDatei.addSeparator();
		mnDatei.add(mntmQuit);

		this.graphSurface=new GraphSurface(this, 10,5,10,5,10);
		graphSurface.setBackground(Color.white);
		
		GridBagConstraints gbc_panel = new GridBagConstraints();
		gbc_panel.fill = GridBagConstraints.BOTH;
		gbc_panel.gridx = 1;
		gbc_panel.gridy = 0;
//		JPanel panel = new JPanel(); // comment out if in production, to avoid spurious warning
//		frmSBDemo.getContentPane().add(panel, gbc_panel); // use this line for GUI designer
		frmSBDemo.getContentPane().add(graphSurface, gbc_panel); // use this line for working app
		
	}


	@Override
	public void itemStateChanged(ItemEvent e) {
		Object source = e.getItemSelectable();
		if(source == this.chckbxLogScale) {
			this.graphSurface.pCO2_uselogscale = e.getStateChange()==java.awt.event.ItemEvent.SELECTED;
			this.updateValues();
			this.updateValueDisplays();
			this.graphSurface.grapData();
;		}
	}

}
