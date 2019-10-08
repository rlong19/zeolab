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

import gnu.io.CommPortIdentifier;

import gnu.io.NoSuchPortException;
import gnu.io.PortInUseException;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.GregorianCalendar;
import java.util.TimeZone;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import de.rho.zeolib.*;
import de.rho.soundlib.*;
import de.rho.zeolab.core.*;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.util.logging.*;
import java.io.File;
import de.rho.zeolab.model.*;

public class ZeoLab extends JFrame implements IZeoSliceListener
{
	class JListLogHandler extends Handler
	{
		@Override
		public void close() throws SecurityException {}

		@Override
		public void flush() {}

		@Override
		public void publish(LogRecord record) 
		{
			String now = sdf.format(new Date(record.getMillis()));
			String className = record.getSourceClassName();
			int i = className.lastIndexOf(".");
			if (i>-1)
				className = className.substring(i+1);
			DefaultListModel model = (DefaultListModel)(zeoEventLog.getModel());
			model.addElement(now + " [" + className + "] " + record.getMessage());
			zeoEventLog.ensureIndexIsVisible(model.size() - 1);
		}
	}
	class SimpleLogFormatter extends Formatter 
	{
		@Override
		public String format(LogRecord record) 
		{
			String now = sdf.format(new Date(record.getMillis()));
			String className = record.getSourceClassName();
			int i = className.lastIndexOf(".");
			if (i>-1)
				className = className.substring(i+1);
			DefaultListModel model = (DefaultListModel)(zeoEventLog.getModel());
			return now + " [" + className + "] " + record.getMessage() + "\n";
		}
	}	

	class ActionStartReplay extends AbstractAction
	{
		public ActionStartReplay(String text, Icon icon, boolean enabled) 
		{
			super(text, icon);
			setEnabled(enabled);			
		}		
		public void actionPerformed(ActionEvent event) 
		{
			if(paused){
				player.resume();
				paused = false;
				actionStartReplay.setEnabled(false);
	    		actionPause.setEnabled(true);
			}
			else if (fc.showOpenDialog(null) == JFileChooser.APPROVE_OPTION)
    		{
				int speed = player.getReplaySpeedFactor();
    			player.stop();
    			String filename = fc.getSelectedFile().getAbsolutePath();
    			String prefix = filename.substring(0,fc.getSelectedFile().getAbsolutePath().lastIndexOf('_')+1);
				replay();
				player.setFilenamePrefix(prefix);
				player.start();
				player.setReplaySpeedFactor(speed);
				actionStartReplay.setEnabled(false);
	    		actionStartRecording.setEnabled(false);
	    		actionPause.setEnabled(true);
	    		actionStop.setEnabled(true);
    		}
		}
	}	
	class ActionPause extends AbstractAction
	{
		public ActionPause(String text, Icon icon, boolean enabled) 
		{
			super(text, icon);
			setEnabled(enabled);			
		}		
		public void actionPerformed(ActionEvent event) 
		{
			action();
		}
		public void action()
		{
			if (replaying)
			{
				paused = true;
				player.pause();
	    		actionPause.setEnabled(false);
	    		actionStartReplay.setEnabled(true);
			}

		}
	}	
	class ActionStartRecording extends AbstractAction
	{
		public ActionStartRecording(String text, Icon icon, boolean enabled) 
		{
			super(text, icon);
			setEnabled(enabled);			
		}		
		public void actionPerformed(ActionEvent event) 
		{
			action();
		}
		public void action()
		{
			recorder.start();
			actionStartReplay.setEnabled(false);
    		actionStartRecording.setEnabled(false);
    		actionStop.setEnabled(true);
		}
	}	
	class ActionStop extends AbstractAction
	{
		public ActionStop(String text, Icon icon, boolean enabled) 
		{
			super(text, icon);
			setEnabled(enabled);			
		}		
		public void actionPerformed(ActionEvent event) 
		{
			action();
		}
		public void action()
		{
			if (replaying)
			{
				player.resume();
				player.stop();
				live();
				actionPause.setEnabled(false);
			}
			else
				recorder.stop();
			actionStartReplay.setEnabled(true);
			actionStartRecording.setEnabled(true);
    		actionStop.setEnabled(false);
		}
	}	
	class ActionBrainwaves extends AbstractAction
	{
		public ActionBrainwaves(String text, Icon icon, boolean enabled) 
		{
			super(text, icon);
			setEnabled(enabled);			
		}		
		public void actionPerformed(ActionEvent event) 
		{
			brainwavesFrame.setVisible(!brainwavesFrame.isVisible());
		}
	}	
	class ActionSpectrogram extends AbstractAction
	{
		public ActionSpectrogram(String text, Icon icon, boolean enabled) 
		{
			super(text, icon);
			setEnabled(enabled);			
		}		
		public void actionPerformed(ActionEvent event) 
		{
			spectrogramFrame.setVisible(!spectrogramFrame.isVisible());
		}
	}	
	class ActionSpectroview extends AbstractAction
	{
		public ActionSpectroview(String text, Icon icon, boolean enabled) 
		{
			super(text, icon);
			setEnabled(enabled);			
		}		
		public void actionPerformed(ActionEvent event) 
		{
			spectroviewFrame.setVisible(!spectroviewFrame.isVisible());
		}
	}	
	class ActionHypnogram extends AbstractAction
	{
		public ActionHypnogram(String text, Icon icon, boolean enabled) 
		{
			super(text, icon);
			setEnabled(enabled);			
		}		
		public void actionPerformed(ActionEvent event) 
		{
			hypnogramFrame.setVisible(!hypnogramFrame.isVisible());
		}
	}	
	class ActionAddTrigger extends AbstractAction
	{
		public ActionAddTrigger(String text, Icon icon, boolean enabled) 
		{
			super(text, icon);
			setEnabled(enabled);			
		}		
		public void actionPerformed(ActionEvent event) 
		{
    		ZeoTriggerEvent newTriggerEvent = new ZeoTriggerEvent();
    		triggerEventEditor.setTriggerEvent(newTriggerEvent);
    		triggerEventEditor.setModal(true);
    		triggerEventEditor.setVisible(true);
    		if (triggerEventEditor.isOkPressed())
    		{
    			trigger.add(newTriggerEvent);
    			DefaultListModel model = (DefaultListModel)triggerList.getModel();
    			model.addElement(newTriggerEvent);
    		}
    		actionSaveTriggers.setEnabled(triggerList.getModel().getSize()>0);
		}
	}	
	class ActionRemoveTrigger extends AbstractAction
	{
		public ActionRemoveTrigger(String text, Icon icon, boolean enabled) 
		{
			super(text, icon);
			setEnabled(enabled);			
		}		
		public void actionPerformed(ActionEvent event) 
		{
    		int i = triggerList.getSelectedIndex();
    		if (i!=-1)
    		{
    			trigger.remove((ZeoTriggerEvent)triggerList.getSelectedValue());
    			DefaultListModel model = (DefaultListModel)triggerList.getModel();
    			model.remove(i);
    		}
    		actionSaveTriggers.setEnabled(triggerList.getModel().getSize()>0);    		
		}
	}	
	class ActionLoadTriggers extends AbstractAction
	{
		public ActionLoadTriggers(String text, Icon icon, boolean enabled) 
		{
			super(text, icon);
			setEnabled(enabled);			
		}		
		public void actionPerformed(ActionEvent event) 
		{
    		JFileChooser fc = new JFileChooser();
    		if (fc.showOpenDialog(null) == JFileChooser.APPROVE_OPTION)
    			loadTriggerEvents(fc.getSelectedFile().getAbsolutePath());
    		DefaultListModel model = (DefaultListModel)triggerList.getModel();
    		model.removeAllElements();
    		for(ZeoTriggerEvent triggerEvent: trigger.getEvents())
    			model.addElement(triggerEvent);
    		actionSaveTriggers.setEnabled(triggerList.getModel().getSize()>0);
		}
	}	
	class ActionSaveTriggers extends AbstractAction
	{
		public ActionSaveTriggers(String text, Icon icon, boolean enabled) 
		{
			super(text, icon);
			setEnabled(enabled);			
		}		
		public void actionPerformed(ActionEvent event) 
		{
    		JFileChooser fc = new JFileChooser();
    		if (fc.showSaveDialog(null) == JFileChooser.APPROVE_OPTION)
    			saveTriggerEvents(fc.getSelectedFile().getAbsolutePath());
		}
	}	
	class ActionSettings extends AbstractAction
	{
		public ActionSettings(String text, Icon icon, boolean enabled) 
		{
			super(text, icon);
			setEnabled(enabled);			
		}		
		public void actionPerformed(ActionEvent event) 
		{
			showSettingsDialog();
		}
	}
	class ActionNapTriggerSettings extends AbstractAction
	{
		public ActionNapTriggerSettings(String text, Icon icon, boolean enabled) 
		{
			super(text, icon);
			setEnabled(enabled);			
		}		
		public void actionPerformed(ActionEvent event) 
		{
			//Preferences prefs = Preferences.userNodeForPackage(getClass());
			napTriggerDialog.active.setSelected(remNapTrigger.active);
			napTriggerDialog.timeToFirstSoundMinSlider.setValue(remNapTrigger.timeToFirstSoundMin);
			napTriggerDialog.timeBetweenSoundsMinSlider.setValue(remNapTrigger.timeBetweenSoundsMin);
			napTriggerDialog.wakeDetectWindowMinSlider.setValue(remNapTrigger.wakeDetectWindowMin);
			napTriggerDialog.minSleepTimeMinSlider.setValue(remNapTrigger.minSleepTimeMin);
			napTriggerDialog.startVolSlider.setValue((int)(remNapTrigger.startVol * 100));
			napTriggerDialog.maxVolSlider.setValue((int)(remNapTrigger.maxVol * 100));
			napTriggerDialog.volOffsetSlider.setValue((int)(remNapTrigger.volOffset * 100));
			napTriggerDialog.volDownRemEndSlider.setValue((int)(remNapTrigger.volDownRemEnd* 100));;
			napTriggerDialog.volDownWakeDetectedSlider.setValue((int)(remNapTrigger.volDownWakeDetected* 100));;
			napTriggerDialog.wavFileName.setText(remNapTrigger.wavFileName);

			napTriggerDialog.setModal(true);
			napTriggerDialog.setVisible(true);
			if (napTriggerDialog.okPressed)
			{
				remNapTrigger.active = napTriggerDialog.active.isSelected();
				remNapTrigger.timeToFirstSoundMin = napTriggerDialog.timeToFirstSoundMinSlider.getValue();
				remNapTrigger.timeBetweenSoundsMin = napTriggerDialog.timeBetweenSoundsMinSlider.getValue();
				remNapTrigger.wakeDetectWindowMin = napTriggerDialog.wakeDetectWindowMinSlider.getValue();
				remNapTrigger.minSleepTimeMin = napTriggerDialog.minSleepTimeMinSlider.getValue();
				remNapTrigger.startVol = napTriggerDialog.startVolSlider.getValue() * 0.01;
				remNapTrigger.maxVol = napTriggerDialog.maxVolSlider.getValue() * 0.01;
				remNapTrigger.volOffset = napTriggerDialog.volOffsetSlider.getValue() * 0.01;
				remNapTrigger.volDownRemEnd = napTriggerDialog.volDownRemEndSlider.getValue() * 0.01;
				remNapTrigger.volDownWakeDetected = napTriggerDialog.volDownWakeDetectedSlider.getValue() * 0.01;
				remNapTrigger.wavFileName = napTriggerDialog.wavFileName.getText();
				saveSettings();
			}
		}
	}

	static Logger logger = Logger.getLogger("de.rho");
	FileHandler fileHandler = null;
	
	JFileChooser fc = new JFileChooser();
	
	Boolean headbandDocked = null;
	
	boolean autoRecording = false;
	boolean replaying = false;
	boolean paused = false;
	JFrame brainwavesFrame;
	JFrame hypnogramFrame;
	JFrame spectrogramFrame;
	JFrame spectroviewFrame;
	SettingsDialog settingsDialog;
	NapTriggerDialog napTriggerDialog;

	
	Brainwave brainwave; 
	Hypnogram hypnogram;
	Spectrogram spectrogram;

	BrainwavePanel brainwavePanel;
	HypnogramPanel hypnogramPanel;
	SpectrogramPanel spectrogramPanel;
	SpectroviewPanel spectroviewPanel;
	
	JList zeoEventLog;
	JList triggerList;
	JScrollPane triggerListPane;
	JScrollPane eventListPane;

	TriggerEventEditor triggerEventEditor;
	
	ZeoBaseLink baseLink;
	ZeoParser parser;
	ZeoRecorder recorder;
	ZeoTrigger trigger;
	
	ZeoPlayer player;
	
	RemNapTrigger remNapTrigger;
	
	SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
	
	GregorianCalendar cal = new GregorianCalendar();

	TriggerActionStartSound actionStartSound = new TriggerActionStartSound();
	TriggerActionStopSound actionStopSound = new TriggerActionStopSound();
	TriggerActionSetVolume actionSetVolume = new TriggerActionSetVolume();
	TriggerActionSystemCommand actionSystemCommand = new TriggerActionSystemCommand();
	
	WavPlayer wavPlayer = new WavPlayer();
	
	JSplitPane splitPane;

	boolean commandsEnabled;
	String statechangeCommand = "";
	String eventCommand = "";
	
	ActionStartReplay actionStartReplay = new ActionStartReplay("PLAY",this.createImageIcon("res/icon_play.png", ""), true);
	ActionStartRecording actionStartRecording = new ActionStartRecording("START",this.createImageIcon("res/icon_record.png", ""), true);
	ActionStop actionStop = new ActionStop("STOP",this.createImageIcon("res/icon_stop.png", ""), false);
	ActionPause actionPause = new ActionPause("PAUSE",this.createImageIcon("res/icon_pause.png", ""), false);
	ActionAddTrigger actionAddTrigger = new ActionAddTrigger("STOP",this.createImageIcon("res/icon_add.png", ""), false);
	ActionRemoveTrigger actionRemoveTrigger = new ActionRemoveTrigger("STOP",this.createImageIcon("res/icon_erase.png", ""), false);
	ActionLoadTriggers actionLoadTriggers = new ActionLoadTriggers("STOP",this.createImageIcon("res/icon_open.png", ""), false);
	ActionSaveTriggers actionSaveTriggers = new ActionSaveTriggers("STOP",this.createImageIcon("res/icon_save.png", ""), false);
	ActionBrainwaves actionBrainwaves = new ActionBrainwaves("Brainwaves", this.createImageIcon("res/icon_brainwaves.png", ""), true);
	ActionSpectrogram actionSpectrogram = new ActionSpectrogram("Spectrogram",this.createImageIcon("res/icon_spectrogram.png", ""), true);
	ActionSpectroview actionSpectroview = new ActionSpectroview("Spectroview",this.createImageIcon("res/icon_spectroview.png", ""), true);
	ActionHypnogram actionHypnogram = new ActionHypnogram("Hypnogram", this.createImageIcon("res/icon_hypnogram.png", ""), true);
	ActionSettings actionSettings = new ActionSettings("Einstellungen", this.createImageIcon("res/icon_settings.png", ""), true);
	ActionNapTriggerSettings actionNapTriggerSettings = new ActionNapTriggerSettings("Nap-Trigger", this.createImageIcon("res/icon_nap.png", ""), true);
	ArrayList<String> ports;
	String comPort = "";
	
	int sleepState = 0;
	
	public ZeoLab()
	{
		super("ZeoLab");
		
		this.baseLink = new ZeoBaseLink();
		this.parser = new ZeoParser();
		
		this.player = new ZeoPlayer();
		
		Container cp = this.getContentPane();
		
		cp.add(createToolBar(), BorderLayout.NORTH);

		Border loweredetched = 
			BorderFactory.createEtchedBorder(EtchedBorder.LOWERED);
		this.zeoEventLog = new JList();
		this.zeoEventLog.setModel(new DefaultListModel());

		this.eventListPane = new JScrollPane(this.zeoEventLog);
		this.eventListPane.setBorder(BorderFactory.createTitledBorder(loweredetched, "Log"));

		this.triggerList = new JList();
		this.triggerList.setModel(new DefaultListModel());
		this.triggerList.addListSelectionListener(
				new ListSelectionListener() 
				{
					public void valueChanged(ListSelectionEvent evt) 
					{
						if (evt.getValueIsAdjusting())
							return;
						actionRemoveTrigger.setEnabled(triggerList.getSelectedIndex()>=0);
					}		
				});

		this.triggerList.addMouseListener(
    			new MouseAdapter() 
    			{
    				public void mouseClicked(MouseEvent evt)
    				{
    					if (evt.getClickCount()==2)
    					{
    			    		int i = triggerList.getSelectedIndex();
    			    		if (i!=-1)
    			    		{
    			    			triggerEventEditor.setTriggerEvent((ZeoTriggerEvent)triggerList.getSelectedValue());
    			        		triggerEventEditor.setModal(true);
    			        		triggerEventEditor.setVisible(true);
    			    		}
    					}
    				}    				
    			});
				
		
		this.triggerListPane = new JScrollPane(this.triggerList);
		this.triggerListPane.setBorder(BorderFactory.createTitledBorder(loweredetched, "Trigger"));

		/*
		this.splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		
		this.splitPane.setTopComponent(this.triggerListPane);
		this.splitPane.setBottomComponent(this.eventListPane);
		
		cp.add(this.splitPane);
*/
		cp.add(this.eventListPane);
		
		this.spectrogram = new Spectrogram();
		this.spectrogramPanel = new SpectrogramPanel(this.spectrogram);
		this.spectrogramFrame = new JFrame("Spectrogram"); 
		this.spectrogramFrame.getContentPane().add(this.spectrogramPanel);
		
		this.spectroviewPanel = new SpectroviewPanel(this.spectrogram);
		this.spectroviewFrame = new JFrame("Spectrum Analyzer");
		this.spectroviewFrame.getContentPane().add(this.spectroviewPanel);
	
		this.hypnogram = new Hypnogram();
		this.hypnogramPanel = new HypnogramPanel(this.hypnogram);
		this.hypnogramFrame = new JFrame("Hypnogram");
		this.hypnogramFrame.getContentPane().add(this.hypnogramPanel);
		
		this.brainwave = new Brainwave();
		this.brainwavePanel = new BrainwavePanel(this.brainwave);
		this.brainwavesFrame = new JFrame("Brainwave");
		this.brainwavesFrame.getContentPane().add(this.brainwavePanel);

		this.remNapTrigger = new RemNapTrigger(this.hypnogram);
		this.napTriggerDialog = new NapTriggerDialog(this);

		DefaultListModel model = (DefaultListModel)(zeoEventLog.getModel());
		model.addElement("ZeoLab - a recording and visualisation tool"); 
		model.addElement("for the Zeo Bedside Sleep Manager");
		model.addElement("\n");
		model.addElement("Copyright (C) 2011  Ricky Ho, member of www.klartraumforum.de");
		model.addElement("\n");
		model.addElement("This program is free software: you can redistribute it and/or modify");
		model.addElement("it under the terms of the GNU General Public License as published by");
		model.addElement("the Free Software Foundation, either version 3 of the License, or");
		model.addElement("(at your option) any later version.");
		model.addElement("\n");
		model.addElement("This program is distributed in the hope that it will be useful,");
		model.addElement("but WITHOUT ANY WARRANTY; without even the implied warranty of");
		model.addElement("MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the");
		model.addElement("GNU General Public License for more details.");
		model.addElement("\n");
		model.addElement("You should have received a copy of the GNU General Public License");
		model.addElement("along with this program.  If not, see <http://www.gnu.org/licenses/>.");
		model.addElement("\n");
		
		/*
		 * Hinweis: der FileHandler wird in der live()-Methode geadded
		 * und in der replay() removed
		 */
		this.logger.addHandler(new JListLogHandler());
		this.logger.setLevel(Level.INFO);

		try 
		{
			fileHandler = new FileHandler("zeolab.log", true);
			fileHandler.setFormatter(new SimpleLogFormatter());
		} 
		catch (SecurityException e1) 
		{
			e1.printStackTrace();
		} 
		catch (IOException e1) 
		{
			e1.printStackTrace();
		}
		

		
		this.ports = getCommPorts();
		this.settingsDialog = new SettingsDialog(this, ports);
		
		ArrayList<IZeoTriggerAction> actions = new ArrayList<IZeoTriggerAction>();
		actions.add(actionStartSound);
		//actions.add(actionSetVolume);
		//actions.add(actionStopSound);
		actions.add(actionSystemCommand);
		this.triggerEventEditor = new TriggerEventEditor(null, actions, this);

		
		this.recorder = new ZeoRecorder();
		this.trigger = new ZeoTrigger(this.hypnogram);
	
		
		loadSettings();
		
		if (!checkSettings())
			showSettingsDialog();
		
		connect();
		live();

		
	    this.setVisible(true);
	    Dimension size = this.getSize();
	    Insets insets = this.getInsets();
	    int insetwidth = insets.left + insets.right;
	    int insetheight = insets.top + insets.bottom;
	    this.setSize((int)size.getWidth() + insetwidth,(int)size.getHeight() + insetheight); 

	    // Discspace-Check:
	    File checkfile = new File(System.getProperty("user.dir"));
	    long usableSpace = checkfile.getUsableSpace(); 
	    logger.info("free disc space = " + usableSpace /1024 /1024 + " mb");
	    if (usableSpace /1024 /1024 < 100)
	    	JOptionPane.showMessageDialog(this, "Attention! Disk almost full!");

		setSleepState(0);
	    
        this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);		
		this.addWindowListener(new WindowAdapter() 
		{
		   public void windowClosing(WindowEvent e) 
		   {
			   saveSettings();
			   setVisible(false);
               dispose();
               System.exit(0);
		   }
		});
		

	}

	private void setSleepState(int state)
	{
		this.sleepState = state;
		
		this.logger.info("Sleepstate: " + ZeoUtility.getSleepStage(state));
		try 
		{
			String command = this.statechangeCommand;
			if (this.commandsEnabled && !command.equals(""))
			{
				command = command.replace("%STATE", ZeoUtility.getSleepStage(state));
				Runtime.getRuntime().exec(command);
			}
		} 
		catch (IOException e) 
		{
			this.logger.warning(e.getMessage());
		}
	}
	
	private void showSettingsDialog()
	{
		settingsDialog.comPort.setSelectedIndex(-1);
		for(int i=0; i<ports.size();i++)
			if (settingsDialog.comPort.getItemAt(i).equals(comPort))
				settingsDialog.comPort.setSelectedIndex(i);
		settingsDialog.replaySpeedFactorSlider.setValue(player.getReplaySpeedFactor());
		settingsDialog.patientInfo.setText(recorder.getPatientInfo());
		settingsDialog.recordingInfo.setText(recorder.getRecordingInfo());
		settingsDialog.zeroOutBadSignal.setSelected(recorder.getZeroOutBadSignal());
		settingsDialog.recordAscii.setSelected(recorder.getRecordAscii());
		settingsDialog.recordEDF.setSelected(recorder.getRecordEDF());
		settingsDialog.recordCSV.setSelected(recorder.getRecordCSV());
		settingsDialog.autoRecord.setSelected(autoRecording);
		settingsDialog.recordingFilter.setSelectedIndex(recorder.getFilter());
		settingsDialog.recordTargetFolder.setText(recorder.getTargetFolder());
		settingsDialog.recordTargetFolder.setToolTipText(recorder.getTargetFolder());
		settingsDialog.enableCommands.setSelected(commandsEnabled);
		settingsDialog.statechangeCommand.setText(statechangeCommand);
		settingsDialog.eventCommand.setText(eventCommand);
		
		settingsDialog.setModal(true);
		settingsDialog.setVisible(true);
		if (settingsDialog.okPressed)
		{
			if (settingsDialog.comPort.getSelectedItem() != null)
				comPort = settingsDialog.comPort.getSelectedItem().toString();
			player.setReplaySpeedFactor(settingsDialog.replaySpeedFactorSlider.getValue());
			recorder.setPatientInfo(settingsDialog.patientInfo.getText());
			recorder.setRecordingInfo(settingsDialog.recordingInfo.getText());
			recorder.setZeroOutBadSignal(settingsDialog.zeroOutBadSignal.isSelected());
			recorder.setRecordAscii(settingsDialog.recordAscii.isSelected());
			recorder.setRecordEDF(settingsDialog.recordEDF.isSelected());
			recorder.setRecordCSV(settingsDialog.recordCSV.isSelected());
			recorder.setFilter(settingsDialog.recordingFilter.getSelectedIndex());
			recorder.setTargetFolder(settingsDialog.recordTargetFolder.getText());
			autoRecording = settingsDialog.autoRecord.isSelected();
			commandsEnabled = settingsDialog.enableCommands.isSelected();
			statechangeCommand = settingsDialog.statechangeCommand.getText();
			eventCommand = settingsDialog.eventCommand.getText();
			saveSettings();
		}		
	}
	
	private boolean checkSettings()
	{
		Preferences prefs = Preferences.userNodeForPackage(this.getClass());
	    return prefs.getBoolean("prefsExist", false) == true;
	}
	
	private void saveSettings()
	{
	    Preferences prefs = Preferences.userNodeForPackage(this.getClass());
	    prefs.putBoolean("prefsExist", true);
	    prefs.put("comPort", this.comPort);
	    prefs.putInt("replayspeedfactor", this.player.getReplaySpeedFactor());
	    prefs.put("patientInfo", this.recorder.getPatientInfo());
	    prefs.put("recordingInfo", this.recorder.getRecordingInfo());
	    prefs.putBoolean("zeroOutBadSignal", this.recorder.getZeroOutBadSignal());
	    prefs.putBoolean("recordAscii", this.recorder.getRecordAscii());
	    prefs.putBoolean("recordEDF", this.recorder.getRecordEDF());
	    prefs.putBoolean("recordCSV", this.recorder.getRecordCSV());
	    prefs.putBoolean("autoRecording", this.autoRecording);
	    prefs.putInt("recordFilter", this.recorder.getFilter());
	    prefs.put("recordTargetFolder", this.recorder.getTargetFolder());
	    prefs.putInt("waveFilter", this.brainwavePanel.getFilter());
	    prefs.putInt("waveAmp",this.brainwavePanel.getAmplitude());
	    prefs.putInt("waveTimescale", this.brainwavePanel.getVisibleSeconds());
	    prefs.putBoolean("waveAnimated", this.brainwavePanel.getAnimated());
	    prefs.putInt("waveFps", this.brainwavePanel.getFPS());
	    prefs.putInt("hypnogramFilter", this.hypnogramPanel.getFilter());
	    prefs.putInt("hypnogramTimescale", this.hypnogramPanel.getTimeScale());
	    prefs.putInt("spectrogramGamma", this.spectrogramPanel.getGammaFactor());
	    prefs.putInt("spectrogramTimescale", this.spectrogramPanel.getTimeScale());
	    prefs.putDouble("spectrogramMaxBar", this.spectrogramPanel.getMaxBar());
	    prefs.putInt("spectroviewGamma", this.spectroviewPanel.getGammaFactor());
	    
	    Insets insets = this.getInsets();
	    prefs.putInt("mainX", this.getX());
	    prefs.putInt("mainY", this.getY());
	    prefs.putInt("mainWidth", this.getWidth() - insets.left - insets.right);
	    prefs.putInt("mainHeight", this.getHeight() - insets.top - insets.bottom);
	    prefs.putInt("hypnoX", this.hypnogramFrame.getX());
	    prefs.putInt("hypnoY", this.hypnogramFrame.getY());
	    prefs.putInt("hypnoWidth", this.hypnogramFrame.getWidth());
	    prefs.putInt("hypnoHeight", this.hypnogramFrame.getHeight());
	    prefs.putInt("wavesX", this.brainwavesFrame.getX());
	    prefs.putInt("wavesY", this.brainwavesFrame.getY());
	    prefs.putInt("wavesWidth", this.brainwavesFrame.getWidth());
	    prefs.putInt("wavesHeight", this.brainwavesFrame.getHeight());
	    prefs.putInt("spectrogramX", this.spectrogramFrame.getX());
	    prefs.putInt("spectrogramY", this.spectrogramFrame.getY());
	    prefs.putInt("spectrogramWidth", this.spectrogramFrame.getWidth());
	    prefs.putInt("spectrogramHeight", this.spectrogramFrame.getHeight());
	    prefs.putInt("spectroviewX", this.spectroviewFrame.getX());
	    prefs.putInt("spectroviewY", this.spectroviewFrame.getY());
	    prefs.putInt("spectroviewWidth", this.spectroviewFrame.getWidth());
	    prefs.putInt("spectroviewHeight", this.spectroviewFrame.getHeight());
	    prefs.putBoolean("spectroviewV",this.spectroviewFrame.isVisible());
	    prefs.putBoolean("spectrogramV",this.spectrogramFrame.isVisible());
	    prefs.putBoolean("wavesV",this.brainwavesFrame.isVisible());
	    prefs.putBoolean("hypnoV",this.hypnogramFrame.isVisible());
	    		
	    prefs.putBoolean("commandsEnabled", this.commandsEnabled);
	    prefs.put("statechangeCommand", this.statechangeCommand);
	    prefs.put("eventCommand", this.eventCommand);
	    
	    prefs.putBoolean("remNapTrigger_active", this.remNapTrigger.active);
		prefs.putInt("remNapTrigger_timeToFirstSoundMin",this.remNapTrigger.timeToFirstSoundMin);
		prefs.putInt("remNapTrigger.timeBetweenSoundsMin",this.remNapTrigger.timeBetweenSoundsMin);
		prefs.putInt("remNapTrigger.wakeDetectWindowMin",this.remNapTrigger.wakeDetectWindowMin);
		prefs.putInt("remNapTrigger.minSleepTimeMin",this.remNapTrigger.minSleepTimeMin); 
		prefs.putDouble("remNapTrigger_startVol",this.remNapTrigger.startVol);
		prefs.putDouble("remNapTrigger_maxVol",this.remNapTrigger.maxVol);
		prefs.putDouble("remNapTrigger_volOffset",this.remNapTrigger.volOffset);
		prefs.putDouble("remNapTrigger_volDownRemEnd",this.remNapTrigger.volDownRemEnd);
		prefs.putDouble("remNapTrigger_volDownWakeDetected",this.remNapTrigger.volDownWakeDetected);
		prefs.put("remNapTrigger_wavFileName",this.remNapTrigger.wavFileName);
	    
	    try 
	    {
			prefs.flush();
		} 
	    catch (BackingStoreException e) 
	    {
	    	this.logger.info(e.toString());
			e.printStackTrace();
		}
	}
	
	private void loadSettings()
	{
		Preferences prefs;
	    prefs = Preferences.userNodeForPackage(this.getClass());
	    this.comPort = prefs.get("comPort", "");
	    if (this.player!=null)
	    	this.player.setReplaySpeedFactor(prefs.getInt("replayspeedfactor", 1));
	    if (this.recorder != null)
	    {
	    	this.recorder.setPatientInfo(prefs.get("patientInfo","Zeo"));
	    	this.recorder.setRecordingInfo(prefs.get("recordingInfo","Recorded with Zeolab"));
		    this.recorder.setZeroOutBadSignal(prefs.getBoolean("zeroOutBadSignal",false));
		    this.recorder.setRecordAscii(prefs.getBoolean("recordAscii",false));
		    this.recorder.setRecordEDF(prefs.getBoolean("recordEDF",false));
		    this.recorder.setRecordCSV(prefs.getBoolean("recordCSV",false));
		    this.recorder.setFilter(prefs.getInt("recordFilter",1));
		    this.recorder.setTargetFolder(prefs.get("recordTargetFolder",""));
	    }
	    this.autoRecording = prefs.getBoolean("autoRecording",false);
	    this.setBounds(prefs.getInt(
	    		"mainX",0),prefs.getInt("mainY",0),
	    		prefs.getInt("mainWidth",384),prefs.getInt("mainHeight",50));
	    		
	    this.hypnogramFrame.setBounds(prefs.getInt(
	    		"hypnoX",385),prefs.getInt("hypnoY",0),
	    		prefs.getInt("hypnoWidth",512),prefs.getInt("hypnoHeight",128));
	    this.brainwavesFrame.setBounds(prefs.getInt(
	    		"wavesX",385),prefs.getInt("wavesY",0),
	    		prefs.getInt("wavesWidth",512),prefs.getInt("wavesHeight",256));
	    this.spectrogramFrame.setBounds(prefs.getInt(
	    		"spectrogramX",385),prefs.getInt("spectrogramY",0),
	    		prefs.getInt("spectrogramWidth",512),prefs.getInt("spectrogramHeight",256));
	    this.spectroviewFrame.setBounds(prefs.getInt(
	    		"spectroviewX",385),prefs.getInt("spectroviewY",0),
	    		prefs.getInt("spectroviewWidth",256),prefs.getInt("spectroviewHeight",256));
	    this.hypnogramFrame.setVisible(prefs.getBoolean("hypnoV",false));
	    this.brainwavesFrame.setVisible(prefs.getBoolean("wavesV",false));
	    this.spectrogramFrame.setVisible(prefs.getBoolean("spectrogramV",false));
	    this.spectroviewFrame.setVisible(prefs.getBoolean("spectroviewV",false));
	    
	    this.brainwavePanel.setFilter(prefs.getInt("waveFilter",1));
	    this.brainwavePanel.setAmplitude(prefs.getInt("waveAmp",350));
	    this.brainwavePanel.setAnimated(prefs.getBoolean("waveAnimated", true));
	    this.brainwavePanel.setVisibleSeconds(prefs.getInt("waveTimescale", 5));
	    this.brainwavePanel.setFPS(prefs.getInt("waveFps", 12));
	    this.hypnogramPanel.setFilter(prefs.getInt("hypnogramFilter",2));
	    this.hypnogramPanel.setTimeScale(prefs.getInt("hypnogramTimescale",1080));
	    this.spectrogramPanel.setGammaFactor(prefs.getInt("spectrogramGamma", 1));
	    this.spectrogramPanel.setTimescale(prefs.getInt("spectrogramTimescale",1080));
	    this.spectrogramPanel.setMaxBar(prefs.getDouble("spectrogramMaxBar", 1.0));
	    this.spectroviewPanel.setGammaFactor(prefs.getInt("spectroviewGamma", 1));
	    this.commandsEnabled = prefs.getBoolean("commandsEnabled", false);
	    this.statechangeCommand = prefs.get("statechangeCommand", "");
	    this.eventCommand = prefs.get("eventCommand", "");
	    
	    if (this.remNapTrigger != null)
	    {
	    	this.remNapTrigger.active = prefs.getBoolean("remNapTrigger_active",false);
			this.remNapTrigger.timeToFirstSoundMin = prefs.getInt("remNapTrigger_timeToFirstSoundMin",10);
			this.remNapTrigger.timeBetweenSoundsMin = prefs.getInt("remNapTrigger.timeBetweenSoundsMin",5);
			this.remNapTrigger.wakeDetectWindowMin = prefs.getInt("remNapTrigger.wakeDetectWindowMin",2);
			this.remNapTrigger.minSleepTimeMin = prefs.getInt("remNapTrigger.minSleepTimeMin",240);
			this.remNapTrigger.startVol = prefs.getDouble("remNapTrigger_startVol",0.5);
			this.remNapTrigger.maxVol = prefs.getDouble("remNapTrigger_maxVol",1.0);
			this.remNapTrigger.volOffset = prefs.getDouble("remNapTrigger_volOffset",0.1);
			this.remNapTrigger.volDownRemEnd = prefs.getDouble("remNapTrigger_volDownRemEnd",0.2);
			this.remNapTrigger.volDownWakeDetected = prefs.getDouble("remNapTrigger_volDownWakeDetected",0.0);
			this.remNapTrigger.wavFileName = prefs.get("remNapTrigger_wavFileName","");
	    }
	}
	
	JToolBar createToolBar()
	{
		JToolBar toolBar = new JToolBar("ZeoLab");
	    toolBar.setFloatable(false);
		JButton button = null;
		button = new JButton(actionBrainwaves);
		button.setText("");
	    toolBar.add(button);
		button = new JButton(actionSpectrogram);
		button.setText("");
	    toolBar.add(button);
		button = new JButton(actionSpectroview);
		button.setText("");
	    toolBar.add(button);
		button = new JButton(actionHypnogram);
		button.setText("");
	    toolBar.add(button);
	    toolBar.addSeparator();
	    /*
		button = new JButton(actionLoadTriggers);
		button.setText("");
	    toolBar.add(button);
		button = new JButton(actionSaveTriggers);
		button.setText("");
	    toolBar.add(button);
		button = new JButton(actionAddTrigger);
		button.setText("");
	    toolBar.add(button);
		button = new JButton(actionRemoveTrigger);
		button.setText("");
	    toolBar.add(button);
	    toolBar.addSeparator();
	    */
		button = new JButton(actionNapTriggerSettings);
		button.setText("");
	    toolBar.add(button);
	    toolBar.addSeparator();
		button = new JButton(actionStartReplay);
		button.setText("");
	    toolBar.add(button);
		button = new JButton(actionStartRecording);
		button.setText("");
	    toolBar.add(button);
		button = new JButton(actionPause);
		button.setText("");
	    toolBar.add(button);
		button = new JButton(actionStop);
		button.setText("");
	    toolBar.add(button);
	    toolBar.addSeparator();
		button = new JButton(actionSettings);
		button.setText("");
	    toolBar.add(button);
	    return toolBar;
    }	

	private ArrayList<String> getCommPorts()
	{
		ArrayList<String> result = new ArrayList<String>();
		Enumeration ports = CommPortIdentifier.getPortIdentifiers();
        while(ports.hasMoreElements()) 
        {
			CommPortIdentifier port = (CommPortIdentifier) ports.nextElement();
			if(port.getPortType() == CommPortIdentifier.PORT_SERIAL ||
					port.getPortType() == CommPortIdentifier.PORT_RAW	)
				result.add(port.getName());
        }
        return result;
	}
          
	private void connect()
	{
		this.baseLink.addListener(parser);
		try 
		{
			this.baseLink.connect(this.comPort);
		} 
		catch (Exception e) 
		{
        	for(String comPort:this.ports)
        		this.logger.info("avaliable comPort=" + comPort);
        	this.logger.info("selected comPort=" + this.comPort);
        	this.logger.info(e.toString());
        	if (e.getMessage()!=null)
        		this.logger.info(e.getMessage());
		}
	}
	private void live()
	{
		this.logger.addHandler(this.fileHandler);
		this.spectrogram.reset();
		this.hypnogram.reset();
		this.hypnogramPanel.repaint();
		this.brainwavePanel.enableFilterMenu(true);
		this.player.removeAllListeners();
		this.parser.addSliceListener(this.recorder);
		this.parser.addSliceListener(this.trigger);
		this.parser.addSliceListener(this); // sollte als letzter drankommen, wenn alle anderen das slice schon verarbeitet haben
		this.replaying = false;
		this.paused = false;
		this.wavPlayer.setVolume(this.remNapTrigger.startVol);
	}
	private void replay()
	{
		this.logger.removeHandler(this.fileHandler);
		this.spectrogram.reset();
		this.hypnogram.reset();
		this.brainwavePanel.enableFilterMenu(false);
		this.parser.removeAllListeners();
		this.player = new ZeoPlayer();
		this.player.addSliceListener(this.trigger);
		this.player.addSliceListener(this);
		this.replaying = true;
		this.paused = false;
	}
	
	public static void main ( String[] args )
    {
		JFrame frame = new ZeoLab();
		frame.setVisible(true);
    }

	private void loadTriggerEvents(String filename)
	{
		try
		{
			FileInputStream fis = new FileInputStream(filename);
			ObjectInputStream ois = new ObjectInputStream(fis);
			int count = ois.readInt();
			for(int i=0; i<count; i++)
			{
				ZeoTriggerEvent triggerEvent = (ZeoTriggerEvent)ois.readObject();
				if (triggerEvent!=null)
				{
					// objekt-identität herstellen...
					if (triggerEvent.action instanceof TriggerActionStartSound)
						triggerEvent.action = this.actionStartSound;
					if (triggerEvent.action instanceof TriggerActionStopSound)
						triggerEvent.action = this.actionStopSound;
					if (triggerEvent.action instanceof TriggerActionSetVolume)
						triggerEvent.action = this.actionSetVolume;
					if (triggerEvent.action instanceof TriggerActionSystemCommand)
						triggerEvent.action = this.actionSystemCommand;
					this.trigger.add(triggerEvent);
				}
			}
			ois.close();
		}
		catch(IOException ioe)
		{
			System.out.println("IOException beim Laden:" + ioe.getMessage());
			ioe.printStackTrace();
		}
		catch(ClassNotFoundException cnfe)
		{
			System.out.println("ClassNotFoundException beim Laden:" + cnfe.getMessage());
			cnfe.printStackTrace();
		}		
	}
	
	private void saveTriggerEvents(String filename)
	{
		try
		{
			ArrayList<ZeoTriggerEvent> events = this.trigger.getEvents(); 
			FileOutputStream fos = new FileOutputStream(filename);
	    	ObjectOutputStream oos = new ObjectOutputStream(fos);
	    	oos.writeInt(events.size());
	    	for(ZeoTriggerEvent triggerEvent:events)
	    		oos.writeObject(triggerEvent);
	    	oos.flush();
	    	oos.close();
		}
		catch(IOException ioe)
		{
			System.out.println("IOException beim Speichern:" + ioe.getMessage());
			ioe.printStackTrace();
		}
	}	

	protected static ImageIcon createImageIcon(String path, String description) 
    {
        java.net.URL imgURL = ZeoLab.class.getResource(path); 
        if (imgURL != null) 
        {
            return new ImageIcon(imgURL, description);
        } else 
        {
            System.err.println("Couldn't find icon file: " + path);
            return null;
        }
    }

	@Override
	public void receivedSlice(ZeoSlice s) 
	{
		if (s.timestamp!=null)
		{
			String title = "ZeoLab (" + s.timestamp.toString() + " / SQI:" + s.sqi + " / I:" + (int)s.impedance;
			if (this.recorder.isRecording())
				title += " *REC*)";
			else 
				title += ")";
			this.setTitle(title);
			if (s.event!=-1)
			{
				this.logger.info("Zeo-Event: " + ZeoUtility.getEvent(s.event));
				try 
				{
					String command = this.eventCommand;
					if (this.commandsEnabled && !command.equals(""))
					{
						command = command.replace("%EVENT", ZeoUtility.getEvent(s.event));
						Runtime.getRuntime().exec(command);
					}
				} 
				catch (IOException e) 
				{
					this.logger.warning(e.getMessage());
				}
				
				if (s.event == 0x0E)
				{
					this.headbandDocked = true;
					if (this.autoRecording)
						this.actionStop.action();
					setSleepState(0);
				}
				if (s.event == 0x0F)
				{
					this.headbandDocked = false;
					if (this.autoRecording)
						this.actionStartRecording.action();
				}
			}
			if (s.waveform != null)
			{
				this.brainwave.addWavedata(s.waveform, s.timestamp, s.badSignal, s.sqi, s.impedance);
				if (this.brainwavePanel.isVisible())
				{
					this.brainwavePanel.resetAnimation();
					this.brainwavePanel.repaint();
				}
			}
			if (s.frequencyBins != null)
			{
				this.spectrogram.addFrequencyBins(s.timestamp, s.frequencyBins);
				if (this.spectroviewPanel.isVisible())
					this.spectroviewPanel.repaint();
				if (this.spectrogramPanel.isVisible())
					this.spectrogramPanel.repaint();
			}
			if (s.sleepState > 0)
			{
				this.hypnogram.addSleepState(s.sleepState, s.timestamp);
				if (this.hypnogramPanel.isVisible())
					this.hypnogramPanel.repaint();
			}
			// checken, ob im gefilterten hypnogram ein statechange vorliegt:
			if (s.sleepState!=-1 && this.headbandDocked != null && this.headbandDocked != true)
			{
				if (this.hypnogram.getStateCount() >= this.hypnogram.getFilter()+1)
				{
					int currentState = this.hypnogram.getFilteredSleepState(this.hypnogram.getStateCount() - this.hypnogram.getFilter() - 1);
					if (this.sleepState != currentState)
					{
						setSleepState(currentState);
					}
				}
			}

			this.remNapTrigger.receivedSlice(s);
		}
	}

}
