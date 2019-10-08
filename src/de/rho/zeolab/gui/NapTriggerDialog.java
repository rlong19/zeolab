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

import java.awt.Container;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;


import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import de.rho.soundlib.*;


@SuppressWarnings("serial")
public class NapTriggerDialog extends JDialog implements ActionListener, ChangeListener
{
	JCheckBox active;
	JLabel timeToFirstSoundMinLabel;
	JSlider timeToFirstSoundMinSlider;
	JLabel timeBetweenSoundsMinLabel;
	JSlider timeBetweenSoundsMinSlider;
	JLabel wakeDetectWindowMinLabel;
	JSlider wakeDetectWindowMinSlider;
	JLabel minSleepTimeMinLabel;
	JSlider minSleepTimeMinSlider;
	JLabel startVolLabel;
	JSlider startVolSlider;
	JLabel maxVolLabel;
	JSlider maxVolSlider;
	JLabel volOffsetLabel;
	JSlider volOffsetSlider;
	JLabel volDownRemEndLabel;
	JSlider volDownRemEndSlider;
	JLabel volDownWakeDetectedLabel;
	JSlider volDownWakeDetectedSlider;
	JButton selectWav;
	JTextField wavFileName;
	JButton test;
	JButton cancel;
	JButton ok;
	JFileChooser fc;
	WavPlayer wavPlayer;
	
	boolean okPressed = false;	
	double playerVolume = 0.5;
	
	public NapTriggerDialog(JFrame owner)
	{
		super(owner,"Nap-Trigger",true);
		
		this.setResizable(false);
		this.setLayout(new GridLayout(0,2,4,4));
		Container cp = this.getContentPane();
		
		cp.add(new JLabel("Active"));
		this.active = new JCheckBox();
		cp.add(this.active);
		this.timeToFirstSoundMinLabel = new JLabel();
		cp.add(this.timeToFirstSoundMinLabel);
		this.timeToFirstSoundMinSlider = createSlider(1,20,1,5,true);
		cp.add(this.timeToFirstSoundMinSlider);
		this.timeBetweenSoundsMinLabel = new JLabel();
		cp.add(this.timeBetweenSoundsMinLabel);
		this.timeBetweenSoundsMinSlider = createSlider(1,20,1,5,true);
		cp.add(this.timeBetweenSoundsMinSlider);
		this.wakeDetectWindowMinLabel = new JLabel();
		cp.add(this.wakeDetectWindowMinLabel);
		this.wakeDetectWindowMinSlider = createSlider(1,20,1,5,true);
		cp.add(this.wakeDetectWindowMinSlider);
		this.minSleepTimeMinLabel = new JLabel();
		cp.add(this.minSleepTimeMinLabel);
		this.minSleepTimeMinSlider = createSlider(1,600,10,60,false);
		cp.add(this.minSleepTimeMinSlider);
		this.startVolLabel = new JLabel();
		cp.add(this.startVolLabel);
		this.startVolSlider = createSlider(0,100,1,10,false);
		cp.add(this.startVolSlider);
		this.maxVolLabel = new JLabel();
		cp.add(this.maxVolLabel);
		this.maxVolSlider = createSlider(0,100,1,10,false);
		cp.add(this.maxVolSlider);
		this.volOffsetLabel = new JLabel();
		cp.add(this.volOffsetLabel);
		this.volOffsetSlider = createSlider(0,100,1,10,false);
		cp.add(this.volOffsetSlider);
		this.volDownRemEndLabel = new JLabel();
		cp.add(this.volDownRemEndLabel);
		this.volDownRemEndSlider = createSlider(0,100,1,10,false);
		cp.add(this.volDownRemEndSlider);
		this.volDownWakeDetectedLabel = new JLabel();
		cp.add(this.volDownWakeDetectedLabel);
		this.volDownWakeDetectedSlider = createSlider(0,100,1,10,false);
		cp.add(this.volDownWakeDetectedSlider);
		
		this.selectWav = new JButton("WAV-File");
		this.selectWav.setActionCommand("SELECTFILE");
		this.selectWav.addActionListener(this);
		cp.add(this.selectWav);

		this.wavFileName = new JTextField(30);
		cp.add(this.wavFileName);
		
		cp.add(new JLabel("Sound-Test with Start-Volume"));
		this.test = new JButton("Play");
		this.test.setActionCommand("TEST");
		this.test.addActionListener(this);
		cp.add(this.test);
		
		this.cancel = new JButton("Cancel");
		this.cancel.setActionCommand("CANCEL");
		this.cancel.addActionListener(this);
		cp.add(this.cancel);

		this.ok = new JButton("OK");
		this.ok.setActionCommand("OK");
		this.ok.addActionListener(this);
		cp.add(this.ok);
			
		pack();
		
		this.fc = new JFileChooser();
		this.wavPlayer = new WavPlayer();
		
		this.addWindowListener(new WindowAdapter() 
		{
		    public void windowActivated(WindowEvent we) 
		    {
		    	playerVolume = wavPlayer.getVolume();
		    }
		    public void windowDeactivated(WindowEvent we) 
		    {
		        wavPlayer.setVolume(playerVolume);
		    }
		});		
	}

	JSlider createSlider(int min, int max, int minor, int major, boolean snap)
	{
		JSlider slider = new JSlider();
		slider.setMinimum(min);
		slider.setMaximum(max);
		slider.setMinorTickSpacing(minor);
		slider.setMajorTickSpacing(major);
		slider.setPaintTicks(true);   
		slider.setPaintLabels(false);  
		slider.setPaintTrack(true);   
		slider.setSnapToTicks(snap);
		slider.addChangeListener(this);
		return slider;
	}
	@Override
	public void actionPerformed(ActionEvent arg0) 
	{
		String command = ((JButton) arg0.getSource()).getActionCommand();
		if (command.equals("TEST"))
		{
			try 
			{
				this.wavPlayer.setVolume(this.startVolSlider.getValue()*0.01);
				this.wavPlayer.start(this.wavFileName.getText());
			} 
			catch (Exception e) 
			{
				e.printStackTrace();
			}
		}
    	if (command.equals("OK"))
    	{
    		this.okPressed = true;
    		this.setVisible(false);
    	}		
    	if (command.equals("CANCEL"))
    	{
    		this.setVisible(false);
    	}		
    	if (command.equals("SELECTFILE"))
    	{
    		if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION)
    			this.wavFileName.setText(fc.getSelectedFile().getAbsolutePath());
    	}	
	}

	public static double round2(double value) 
	{
		double result = value * 100;
		result = Math.round(result);
		result = result / 100;
		return result;
	}

	@Override
	public void stateChanged(ChangeEvent arg0) 
	{
		if (arg0.getSource()==this.timeToFirstSoundMinSlider)
			this.timeToFirstSoundMinLabel.setText(this.timeToFirstSoundMinSlider.getValue() + " Minutes to first Sound");
		if (arg0.getSource()==this.timeBetweenSoundsMinSlider)
			this.timeBetweenSoundsMinLabel.setText(this.timeBetweenSoundsMinSlider.getValue() + " Minutes between Sounds");
		if (arg0.getSource()==this.wakeDetectWindowMinSlider)
			this.wakeDetectWindowMinLabel.setText("Wake-Detect " + this.wakeDetectWindowMinSlider.getValue() + " Minutes after Sound");
		if (arg0.getSource()==this.minSleepTimeMinSlider)
			this.minSleepTimeMinLabel.setText("Active for " + this.minSleepTimeMinSlider.getValue() + " Minutes of Sleep");
		if (arg0.getSource()==this.startVolSlider)
			this.startVolLabel.setText("Start-Volume = " + round2(this.startVolSlider.getValue() * 0.01));
		if (arg0.getSource()==this.maxVolSlider)
			this.maxVolLabel.setText("Max. Volume = " + round2(this.maxVolSlider.getValue() * 0.01));
		if (arg0.getSource()==this.volOffsetSlider)
			this.volOffsetLabel.setText("Increase Sound = " + round2(this.volOffsetSlider.getValue() * 0.01));
		if (arg0.getSource()==this.volDownRemEndSlider)
			this.volDownRemEndLabel.setText("Reduce at REM-End = " + round2(this.volDownRemEndSlider.getValue() * 0.01));
		if (arg0.getSource()==this.volDownWakeDetectedSlider)
			this.volDownWakeDetectedLabel.setText("Reduce at Wake-Detect = " + round2(this.volDownWakeDetectedSlider.getValue() * 0.01));
	}	
}
