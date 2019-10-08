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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Logger;

import javax.swing.ButtonGroup;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;

import de.rho.zeolab.model.*;
import de.rho.zeolib.IZeoSliceListener;
import de.rho.zeolib.ZeoSlice;

@SuppressWarnings("serial")
public class BrainwavePanel extends JPanel implements Runnable, ActionListener
{
	static Logger logger = Logger.getLogger(BrainwavePanel.class.getName());
	
	JPopupMenu menu;
	JMenu filterMenu;
	JMenu ampMenu;
	JMenu timescaleMenu;
	JMenu animationMenu;
	JMenu fpsMenu;
	
	Brainwave brainwave = null;
	int visibleSeconds = 5;
	int animationFrame = 0;
	int framesPerSecond = 12;
	int waveAmplitude = 100;
	
	SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
	boolean animated;
	
	public BrainwavePanel(Brainwave brainwave)
	{
		this.brainwave = brainwave;
		
		filterMenu = new JMenu("Filter");
		ButtonGroup filterGroup = new ButtonGroup();
		filterMenu.add(createMenuItem("Unfiltered", "F,0", filterGroup,false));
		filterMenu.add(createMenuItem("50 Hz", "F,1", filterGroup,true));
		filterMenu.add(createMenuItem("60 Hz", "F,2", filterGroup,false));

		ampMenu = new JMenu("Amplitude");
		ButtonGroup ampGroup = new ButtonGroup();
		ampMenu.add(createMenuItem("50", "AMP,50", ampGroup, false));
		ampMenu.add(createMenuItem("100", "AMP,100", ampGroup, false));
		ampMenu.add(createMenuItem("150", "AMP,150", ampGroup, true));
		ampMenu.add(createMenuItem("200", "AMP,200", ampGroup, false));
		ampMenu.add(createMenuItem("250", "AMP,250", ampGroup, false));
		ampMenu.add(createMenuItem("300", "AMP,300", ampGroup, false));
		ampMenu.add(createMenuItem("350", "AMP,350", ampGroup, false));

		timescaleMenu = new JMenu("Seconds");
		ButtonGroup timescaleGroup = new ButtonGroup();
		timescaleMenu.add(createMenuItem("1", "SEC,1", timescaleGroup, false));
		timescaleMenu.add(createMenuItem("2", "SEC,2", timescaleGroup, false));
		timescaleMenu.add(createMenuItem("3", "SEC,3", timescaleGroup, false));
		timescaleMenu.add(createMenuItem("4", "SEC,4", timescaleGroup, false));
		timescaleMenu.add(createMenuItem("5", "SEC,5", timescaleGroup, false));
		timescaleMenu.add(createMenuItem("10", "SEC,10", timescaleGroup, false));
		timescaleMenu.add(createMenuItem("30", "SEC,30", timescaleGroup, false));
		timescaleMenu.add(createMenuItem("60", "SEC,60", timescaleGroup, false));
		
		animationMenu = new JMenu("Animation");
		ButtonGroup animatedGroup = new ButtonGroup();
		animationMenu.add(createMenuItem("on", "ANI,ON", animatedGroup, false));
		animationMenu.add(createMenuItem("off", "ANI,OFF", animatedGroup, false));
		
		fpsMenu = new JMenu("FPS");
		ButtonGroup fpsGroup = new ButtonGroup();
		fpsMenu.add(createMenuItem("12", "FPS,12", fpsGroup,true));
		fpsMenu.add(createMenuItem("24", "FPS,24", fpsGroup,false));
		fpsMenu.add(createMenuItem("30", "FPS,30", fpsGroup,false));
		fpsMenu.add(createMenuItem("60", "FPS,60", fpsGroup,false));


		this.menu = new JPopupMenu();
		this.menu.add(filterMenu);
		this.menu.add(ampMenu);
		this.menu.add(timescaleMenu);
		this.menu.add(animationMenu);
		this.menu.add(fpsMenu);
		this.add(this.menu);

		this.setAnimated(true);
		this.setFilter(1);
		this.setAmplitude(150);
		this.setVisibleSeconds(5);
		
	    this.addMouseListener(new MouseAdapter() 
	    {
	    	public void mouseReleased(MouseEvent me) 
	    	{
	    		if ( me.isPopupTrigger() ) 
	    		{
	    			menu.show(me.getComponent(),me.getX(),me.getY());
	            }
	    	}
	    	public void mousePressed(MouseEvent me) 
	    	{
	    		if ( me.isPopupTrigger() ) 
	    		{
	    			menu.show(me.getComponent(),me.getX(),me.getY());
	            }
	    	}
	    });		
	}

	private JMenuItem createMenuItem(String titel, String command, ButtonGroup group, boolean checked)
	{
		JRadioButtonMenuItem item = new JRadioButtonMenuItem(titel);
		item.setActionCommand(command);
		group.add(item);
		item.addActionListener(this);
		item.setSelected(checked);
		return item;
	}
	/*
	 * die methode geht davon aus, dass den menuitems 
	 * actioncommands der Form "A,value" zugeordnet sind
	 */
	private void selectMenuItem(JMenu menu, String value)
	{
		int countItems = menu.getSubElements()[0].getSubElements().length;
		for (int i=0; i<countItems; i++)
		{
			JMenuItem menuItem = (JMenuItem)menu.getSubElements()[0].getSubElements()[i];
			String command = menuItem.getActionCommand();
			String[] part = command.split(",");
			if (part.length==2)
				menuItem.setSelected(part[1].equals(value));
		}
	}
	
	public void enableFilterMenu(boolean b)
	{
		this.filterMenu.setEnabled(b);
	}
	
	public void setFilter(int f)
	{
		if (f>=0 && f<=2)
		{
			this.brainwave.setFilter(f);
			selectMenuItem(this.filterMenu, String.valueOf(f));
			this.repaint();
		}
	}
	public int getFilter()
	{
		return this.brainwave.getFilter();
	}
	
	public int getFPS()
	{
		return this.framesPerSecond;
	}
	public void setFPS(int fps)
	{
		if (fps>=1)
			this.framesPerSecond = fps;
		selectMenuItem(this.fpsMenu, String.valueOf(fps));
	}
	public void setVisibleSeconds(int s)
	{
		if (s < this.brainwave.getBufferSizeSeconds())
		{
			this.visibleSeconds = s;
			selectMenuItem(this.timescaleMenu,String.valueOf(s));
			this.repaint();
		}
	}
	public void setAmplitude(int a)
	{
		this.waveAmplitude = a;
		selectMenuItem(this.ampMenu, String.valueOf(a));
		this.repaint();
	}
	public int getAmplitude()
	{
		return this.waveAmplitude;
	}
		
	public int getVisibleSeconds()
	{
		return this.visibleSeconds;
	}
	
	public boolean getAnimated()
	{
		return this.animated;
	}
	public void setAnimated(boolean animated)
	{
		if (this.animated != animated)
		{
			this.animated = animated;
			if (this.animated)
			{
				Thread thread = new Thread(this);
				thread.start();
				selectMenuItem(this.animationMenu,"ON");
			}
			else
			{
				selectMenuItem(this.animationMenu,"OFF");
			}
		}
	}
	
	@Override
	public void run() 
	{
		while(this.animated)
		{
			long start = new Date().getTime(); 
			this.repaint();
			this.animationFrame++;
			if (this.animationFrame > this.framesPerSecond)
				this.animationFrame = this.framesPerSecond;
			long now = new Date().getTime(); 
			long duration = now - start;
			try 
			{
				Thread.sleep(Math.max(0,(int)(1000.0 / this.framesPerSecond) - duration));
			} 
			catch (InterruptedException e) 
			{
				e.printStackTrace();
			}
		}
	}

	public void resetAnimation()
	{
		this.animationFrame = 0;
	}
	
	@Override
	public void paint(Graphics g)
	{
		synchronized(this.brainwave)
		{
		
			int width = this.getWidth();
			int height = this.getHeight();
			Graphics2D g2d = (Graphics2D) g;
			g.setColor(Color.black);
			g.fillRect(0,0,width,height);
			int zeroHeight = height / 2;
			int y;
			int lx=0, ly=zeroHeight;
			for(float i=-this.waveAmplitude; i<=this.waveAmplitude; i+=50)
			{
				if (i==0)
					y = zeroHeight;
				else
					y = zeroHeight + (int)(zeroHeight *  (i/(double)this.waveAmplitude));
				g.setColor(Color.darkGray);
				g.drawLine(0, y, this.getWidth(), y);
			}	
			if (this.brainwave != null)
			{
				g2d.setStroke(new BasicStroke(2));
				
				int waveIndex = 
					(int)(this.brainwave.getBufferSizeSeconds() - this.visibleSeconds - 1) * 128 
						+ (int)(this.animationFrame * (128.0 / this.framesPerSecond)); 

				if (!this.animated)
					waveIndex = 
						(int)(this.brainwave.getBufferSizeSeconds() - this.visibleSeconds) * 128; 
						
				for(int i=0; i<this.visibleSeconds * 128; i++)
				{
					if (this.brainwave.getBadSignal(waveIndex))
						g.setColor(Color.red);
					else
						g.setColor(Color.blue);
						int x = (int)((i+1) * (this.getWidth() / (this.visibleSeconds *128.0)));
					y = (int) (zeroHeight - this.brainwave.getFilteredWave(waveIndex++) * height / (2 * this.waveAmplitude));
					g.drawLine(lx, ly, x, y);
					lx = x;
					ly = y;
				}

				g.setColor(Color.gray);
				
				if(this.brainwave.getFilter()==0)
					g.drawString("unfiltered", 4, 12);
				if(this.brainwave.getFilter()==1)
					g.drawString("50Hz-Filter", 4, 12);
				if(this.brainwave.getFilter()==2)
					g.drawString("60Hz-Filter", 4, 12);
				
				g.drawString("Frame:" + this.animationFrame, 100,12);
				
			}
			if (this.brainwave.getTimestamp() != null)
			{
				g.setColor(Color.gray);
				g.drawString("Time: " +sdf.format(this.brainwave.getTimestamp()),4,this.getHeight()-20);
				g.drawString("SQI: " + this.brainwave.getSqi(), this.getWidth()/2, getHeight()-20);
				g.drawString("Imp: " + (int)this.brainwave.getImpedance(), this.getWidth()/4*3,getHeight()-20);
			}		
		}	
	}
	@Override
	public void actionPerformed(ActionEvent action) 
	{
		String[] command = ((JMenuItem) action.getSource()).getActionCommand().split(",");
    	try
    	{
    		if (command[0].equals("F"))
    		{
        		int i = Integer.parseInt(command[1]);
        		this.brainwave.setFilter(i);
        		this.repaint();
    		}
    		if (command[0].equals("AMP"))
    		{
        		int i = Integer.parseInt(command[1]);
    			this.waveAmplitude = i;
    			this.repaint();
    		}
    		if (command[0].equals("SEC"))
    		{
        		int i = Integer.parseInt(command[1]);
        		this.visibleSeconds = i;
        		this.repaint();
    		}
    		if (command[0].equals("ANI"))
    		{
    			//this.animated = command[1].equals("ON");
    			setAnimated(command[1].equals("ON"));
    		}
    		if (command[0].equals("FPS"))
    		{
    			int i = Integer.parseInt(command[1]);
        		this.framesPerSecond = i;
    		}
    	}
    	catch(NumberFormatException ex)
    	{
    		ex.printStackTrace();
    	}    	
	}	
}
