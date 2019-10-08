/* 
    ZeoLab - a recording and visualisation tool 
             for the Zeo Bedside Sleep Manager
 
    Copyright (C) 2011  Ricky Ho (member of www.klartraumforum.de)

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

package de.rho.zeolab.gui;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.ArrayList;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import de.rho.zeolab.core.IZeoTriggerAction;

public class SettingsDialog extends JDialog implements ActionListener, ChangeListener
{
	JFileChooser fc = new JFileChooser();
		
	public JComboBox comPort;
	public JLabel replaySpeedFactorLabel;
	public JSlider replaySpeedFactorSlider;
	public JTextField patientInfo;
	public JTextField recordingInfo;
	public JCheckBox zeroOutBadSignal;
	public JCheckBox recordAscii;
	public JCheckBox recordEDF;
	public JCheckBox recordCSV;
	public JCheckBox autoRecord;
	public JComboBox recordingFilter;
	public JCheckBox enableCommands;
	public JTextField statechangeCommand; // command that should be executed when the sleepstage changes
	public JTextField eventCommand; // command that should be executed when a zeo event occurs
	public JButton selectTargetFolder;
	public JTextField recordTargetFolder;
	JButton cancel;
	JButton ok;
	
	boolean okPressed = false;	
	
	public SettingsDialog(JFrame owner, ArrayList<String> ports)
	{
		super(owner,"Settings",true);
		
		this.setResizable(false);
		this.setLayout(new GridLayout(0,2,2,2));
		Container cp = this.getContentPane();
		
		cp.add(new JLabel("COM-Port"));
		this.comPort = new JComboBox();
		for(String port:ports)
			this.comPort.addItem(port);
		cp.add(this.comPort);
		
		this.replaySpeedFactorLabel = new JLabel();
		cp.add(this.replaySpeedFactorLabel);
		this.replaySpeedFactorSlider = new JSlider();
		this.replaySpeedFactorSlider.setMinimum(1);
		this.replaySpeedFactorSlider.setMaximum(1000);
		this.replaySpeedFactorSlider.setMinorTickSpacing(1);
		this.replaySpeedFactorSlider.setMajorTickSpacing(10);
		this.replaySpeedFactorSlider.setPaintTicks(true);   
		this.replaySpeedFactorSlider.setPaintTrack(true);   
		this.replaySpeedFactorSlider.setSnapToTicks(false);
		this.replaySpeedFactorSlider.addChangeListener(this);
		cp.add(this.replaySpeedFactorSlider);
		
		cp.add(new JLabel("EDF Patient-Info:"));
		this.patientInfo = new JTextField();
		cp.add(this.patientInfo);
		
		cp.add(new JLabel("EDF Recording-Info:"));
		this.recordingInfo = new JTextField();
		cp.add(this.recordingInfo);
		
		cp.add(new JLabel("Bad Signal = 0"));
		this.zeroOutBadSignal = new JCheckBox();
		cp.add(this.zeroOutBadSignal);

		cp.add(new JLabel("Ascii record"));
		this.recordAscii = new JCheckBox();
		cp.add(this.recordAscii);

		cp.add(new JLabel("EDF record"));
		this.recordEDF = new JCheckBox();
		cp.add(this.recordEDF);

		cp.add(new JLabel("CSV record"));
		this.recordCSV = new JCheckBox();
		cp.add(this.recordCSV);

		this.selectTargetFolder = new JButton("Folder");
		this.selectTargetFolder.setActionCommand("RECORDFOLDER");
		this.selectTargetFolder.addActionListener(this);
		cp.add(this.selectTargetFolder);
		this.recordTargetFolder = new JTextField();
		cp.add(this.recordTargetFolder);
		
		cp.add(new JLabel("Automatic"));
		this.autoRecord = new JCheckBox();
		cp.add(this.autoRecord);

		cp.add(new JLabel("Filter when recording:"));
		this.recordingFilter= new JComboBox();
		this.recordingFilter.addItem("unfiltered");
		this.recordingFilter.addItem("50 Hz");
		this.recordingFilter.addItem("60 Hz");
		cp.add(this.recordingFilter);
		
		cp.add(new JLabel("Active Commands"));
		this.enableCommands = new JCheckBox();
		cp.add(this.enableCommands);

		cp.add(new JLabel("State-Command"));
		this.statechangeCommand = new JTextField();
		cp.add(this.statechangeCommand);

		cp.add(new JLabel("Event-Command"));
		this.eventCommand = new JTextField();
		cp.add(this.eventCommand);

		this.cancel = new JButton("Cancel");
		this.cancel.setActionCommand("CANCEL");
		this.cancel.addActionListener(this);
		cp.add(this.cancel);

		this.ok = new JButton("OK");
		this.ok.setActionCommand("OK");
		this.ok.addActionListener(this);
		cp.add(this.ok);
			
		pack();
	}

	
	@Override
	public void actionPerformed(ActionEvent arg0) 
	{
		String command = ((JButton) arg0.getSource()).getActionCommand();
    	if (command.equals("OK"))
    	{
    		this.okPressed = true;
    		this.setVisible(false);
    	}		
    	if (command.equals("CANCEL"))
    	{
    		this.setVisible(false);
    	}
    	if (command.equals("RECORDFOLDER"))
    	{
    		fc.setCurrentDirectory(new java.io.File(this.recordTargetFolder.getText()));
    		fc.setDialogTitle("Choose Folder");
    		fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
    		fc.setAcceptAllFileFilterUsed(false);
    		if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION)
    		{
    			this.recordTargetFolder.setText(fc.getSelectedFile().toString());
    			this.recordTargetFolder.setToolTipText(fc.getSelectedFile().toString());
    		}
    	}
	}


	@Override
	public void stateChanged(ChangeEvent arg0) 
	{
		if (arg0.getSource() == this.replaySpeedFactorSlider)
			this.replaySpeedFactorLabel.setText("Playback Speed = " + this.replaySpeedFactorSlider.getValue() + "x");
		
	}
}
