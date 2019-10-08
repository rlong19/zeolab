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
import java.awt.event.*;
import java.util.*;
import java.util.List;
import javax.swing.*;

import de.rho.zeolab.core.*;

public class TriggerEventEditor extends JDialog implements ActionListener
{
	ZeoTriggerEvent triggerEvent = null;
	JCheckBox active;
	JCheckBox onlyOny;
	JCheckBox repeat;
	JComboBox stateBefore;
	JComboBox countBefore;
	JComboBox stateCurrent;
	JComboBox countCurrent;
	
	JComboBox countSleep;
	JComboBox countAlarm;
    JSpinner alarmTime;
    JSpinner startTime;
    JCheckBox alarmTimeValid;
    JCheckBox startTimeValid;
	JComboBox action;
	
	JTextField argument;
	JButton selectFile;
	JButton test;
	JButton cancel;
	JButton ok;
	
	boolean okPressed = false;
	
	public boolean isOkPressed() { return this.okPressed; }
	
	public TriggerEventEditor(ZeoTriggerEvent triggerEvent, List<IZeoTriggerAction> actions, JFrame owner)
	{
		super(owner,"Edit Trigger",true);
		this.setSize(250, 350);
		this.setResizable(false);
		this.setLayout(new GridLayout(0,2,2,2));
		Container cp = this.getContentPane();
		
		cp.add(new JLabel("Status:"));
		this.active = new JCheckBox("aktiviert");
		cp.add(this.active);

		cp.add(new JLabel("Häufigkeit:"));
		this.onlyOny = new JCheckBox("nur einmal");
		cp.add(this.onlyOny);
		
		cp.add(new JLabel("Wiederholung:"));
		this.repeat = new JCheckBox("ein");
		cp.add(this.repeat);

		cp.add(new JLabel("Vorherige Phase:"));
		this.stateBefore = new JComboBox();
		this.stateBefore.addItem("-");
		this.stateBefore.addItem("Wach");
		this.stateBefore.addItem("REM");
		this.stateBefore.addItem("Leicht");
		this.stateBefore.addItem("Tief");
		this.stateBefore.addItem("NREM");
		this.stateBefore.addItem("Schlaf");
		cp.add(this.stateBefore);
		
		cp.add(new JLabel("Wie oft?"));
		this.countBefore = new JComboBox();
		for(int i=1; i<=12; i++)
			this.countBefore.addItem(i + "x (" + i*5 + " Min.)");
		cp.add(this.countBefore);

		cp.add(new JLabel("Aktuelle Phase:"));
		this.stateCurrent = new JComboBox();
		this.stateCurrent.addItem("-");
		this.stateCurrent.addItem("Wach");
		this.stateCurrent.addItem("REM");
		this.stateCurrent.addItem("Leicht");
		this.stateCurrent.addItem("Tief");
		this.stateCurrent.addItem("NREM");
		this.stateCurrent.addItem("Schlaf");
		cp.add(this.stateCurrent);
		
		cp.add(new JLabel("Wie oft?"));
		this.countCurrent = new JComboBox();
		for(int i=1; i<=12; i++)
			this.countCurrent.addItem(i + "x (" + i*5 + " Min.)");
		cp.add(this.countCurrent);		

		this.startTime = new JSpinner(new SpinnerDateModel(new Date(),
                null, null, Calendar.YEAR));
		this.startTime.setEditor(new JSpinner.DateEditor(this.startTime,
                "HH:mm:ss"));
		this.startTimeValid = new JCheckBox("Start-Zeit:");
		this.startTimeValid.addItemListener(new ItemListener() 
		{
			public void itemStateChanged(ItemEvent itemEvent) 
			{
				int state = itemEvent.getStateChange();
				startTime.setEnabled(state == ItemEvent.SELECTED);
			}
		});
				cp.add(this.startTimeValid);
		cp.add(this.startTime);

		this.countSleep = new JComboBox();
		for(int i=0; i<=72; i++)
			this.countSleep.addItem(i + "x (" + i*5 + " Min.)");
		cp.add(new JLabel("Mind. geschlafen:"));
		cp.add(this.countSleep);
		
		this.alarmTime = new JSpinner(new SpinnerDateModel(new Date(),
                null, null, Calendar.YEAR));
		this.alarmTime.setEditor(new JSpinner.DateEditor(this.alarmTime,
                "HH:mm:ss"));
		
		this.alarmTimeValid = new JCheckBox("Alarm-Zeit:");
		this.alarmTimeValid.addItemListener(new ItemListener() 
		{
			public void itemStateChanged(ItemEvent itemEvent) 
			{
				int state = itemEvent.getStateChange();
				alarmTime.setEnabled(state == ItemEvent.SELECTED);
				countAlarm.setEnabled(state == ItemEvent.SELECTED);
			}
		});
		
		cp.add(this.alarmTimeValid);
		cp.add(this.alarmTime);
		
		this.countAlarm = new JComboBox();
		for(int i=1; i<=12; i++)
			this.countAlarm.addItem(i + "x (" + i*5 + " Min.)");
		cp.add(new JLabel("Alarm-Fenster:"));
		cp.add(this.countAlarm);
		
		 cp.add(new JLabel("Aktion:"));
		this.action = new JComboBox();
		for(IZeoTriggerAction action:actions)
			this.action.addItem(action);
		cp.add(this.action);
		
		
		this.selectFile = new JButton("Argument:");
		this.selectFile.setActionCommand("SELECTFILE");
		this.selectFile.addActionListener(this);
		cp.add(this.selectFile);

		this.argument = new JTextField(20);
		cp.add(this.argument);

		cp.add(new JLabel("Testen:"));
		this.test = new JButton("Test");
		this.test.setActionCommand("TEST");
		this.test.addActionListener(this);
		cp.add(this.test);

		this.cancel = new JButton("Abbrechen");
		this.cancel.setActionCommand("CANCEL");
		this.cancel.addActionListener(this);
		cp.add(this.cancel);

		this.ok = new JButton("OK");
		this.ok.setActionCommand("OK");
		this.ok.addActionListener(this);
		cp.add(this.ok);
	
		setTriggerEvent(triggerEvent);

		pack();
		
	}
	
	public void setTriggerEvent(ZeoTriggerEvent triggerEvent)
	{
		if (triggerEvent != null)
			this.triggerEvent = triggerEvent;
		else
			this.triggerEvent = new ZeoTriggerEvent();
	
		if (this.triggerEvent != null)
		{
			this.active.setSelected(this.triggerEvent.active);
			this.onlyOny.setSelected(this.triggerEvent.onlyOnce);
			this.repeat.setSelected(this.triggerEvent.repeat);
			this.countBefore.setSelectedIndex(this.triggerEvent.countBefore-1);
			this.stateBefore.setSelectedIndex(this.triggerEvent.stateBefore);
			this.countCurrent.setSelectedIndex(this.triggerEvent.countCurrent-1);
			this.stateCurrent.setSelectedIndex(this.triggerEvent.stateCurrent);
			if (this.triggerEvent.startTime != null)
			{
				this.startTimeValid.setSelected(true);
				this.startTime.setEnabled(true);
				this.startTime.setValue(this.triggerEvent.startTime);
			}
			else
			{
				this.startTimeValid.setSelected(false);
				this.startTime.setEnabled(false);
			}
			if (this.triggerEvent.alarmTime != null)
			{
				this.alarmTimeValid.setSelected(true);
				this.alarmTime.setEnabled(true);
				this.alarmTime.setValue(this.triggerEvent.alarmTime);
				this.countAlarm.setEnabled(true);
				this.countAlarm.setSelectedIndex(this.triggerEvent.countAlarm-1);
			}
			else
			{
				this.alarmTimeValid.setSelected(false);
				this.alarmTime.setEnabled(false);
				this.countAlarm.setEnabled(false);
			}
			this.countSleep.setSelectedIndex(this.triggerEvent.countSleep);
			this.action.setSelectedItem(this.triggerEvent.action);
			this.argument.setText(this.triggerEvent.argument);
		}
		else
		{
			this.active.setSelected(false);
			this.onlyOny.setSelected(false);
			this.repeat.setSelected(false);
			this.alarmTimeValid.setSelected(false);
			this.startTimeValid.setSelected(false);
			this.countSleep.setSelectedIndex(0);
			this.countBefore.setSelectedIndex(0);
			this.stateBefore.setSelectedIndex(0);
			this.countCurrent.setSelectedIndex(0);
			this.stateCurrent.setSelectedIndex(0);
			this.action.setSelectedItem(null);
			this.argument.setText("");
		}
	}

	@Override
	public void actionPerformed(ActionEvent arg0) 
	{
		String command = ((JButton) arg0.getSource()).getActionCommand();
    	if (command.equals("OK"))
    	{
    		this.triggerEvent.active = this.active.isSelected();
    		this.triggerEvent.onlyOnce = this.onlyOny.isSelected();
    		this.triggerEvent.repeat = this.repeat.isSelected();
    		this.triggerEvent.hasTriggered = false;
    		this.triggerEvent.countBefore = this.countBefore.getSelectedIndex()+1;
    		this.triggerEvent.stateBefore = this.stateBefore.getSelectedIndex();
    		this.triggerEvent.countCurrent = this.countCurrent.getSelectedIndex()+1;
    		this.triggerEvent.stateCurrent = this.stateCurrent.getSelectedIndex();
    		
    		if (this.startTimeValid.isSelected())
    			this.triggerEvent.startTime = (Date)this.startTime.getValue();
    		else
    			this.triggerEvent.startTime = null;
    		this.triggerEvent.countSleep = this.countSleep.getSelectedIndex();
    		
    		if (this.alarmTimeValid.isSelected())
    		{
    			this.triggerEvent.alarmTime = (Date)this.alarmTime.getValue();
    			this.triggerEvent.countAlarm = this.countAlarm.getSelectedIndex()+1;
    		}
    		else
    		{
    			this.triggerEvent.alarmTime = null;
    			this.triggerEvent.countAlarm = 0;
    		}
    		
    		this.triggerEvent.action = (IZeoTriggerAction)this.action.getSelectedItem();
    		this.triggerEvent.argument = this.argument.getText();
    		
    		this.okPressed = true;
    		this.setVisible(false);
    	}
    	if (command.equals("TEST"))
    	{
    		if (this.action.getSelectedIndex()>=0)
    			((IZeoTriggerAction)this.action.getSelectedItem()).actionTriggered(this.argument.getText());
    	}
    	if (command.equals("SELECTFILE"))
    	{
    		JFileChooser fc = new JFileChooser("d:\\WAV");
    		if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION)
    			this.argument.setText(fc.getSelectedFile().getAbsolutePath());

    	}
	}
}
