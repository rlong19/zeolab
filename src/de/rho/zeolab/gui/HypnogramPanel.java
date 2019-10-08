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
import java.awt.Stroke;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.*;

import javax.swing.ButtonGroup;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;

import de.rho.zeolib.*;
import de.rho.zeolab.model.*;

@SuppressWarnings("serial")
public class HypnogramPanel extends JPanel implements ActionListener
{
	Hypnogram hypnogram;
	Calendar cal;
	JPopupMenu menu;
	JMenu filterMenu;
	JMenu timescaleMenu;
	int timescale;
	
	public HypnogramPanel(Hypnogram hypnogram)
	{
		this.hypnogram = hypnogram;
		this.cal = new GregorianCalendar();
		
		int filter = this.hypnogram.getFilter();
		
		this.filterMenu = new JMenu("Filter");
		ButtonGroup filterGroup = new ButtonGroup();
		this.filterMenu.add(createMenuItem("Unfiltered", "F,0", filterGroup,filter==0));
		this.filterMenu.add(createMenuItem("1", "F,1", filterGroup,filter==1));
		this.filterMenu.add(createMenuItem("2", "F,2", filterGroup,filter==2));
		this.filterMenu.add(createMenuItem("3", "F,3", filterGroup,filter==3));
		this.filterMenu.add(createMenuItem("4", "F,4", filterGroup,filter==4));
		this.filterMenu.add(createMenuItem("5", "F,5", filterGroup,filter==5));
		this.filterMenu.add(createMenuItem("10", "F,10", filterGroup,filter==10));

		this.timescaleMenu = new JMenu("Time Window");
		ButtonGroup timescaleGroup = new ButtonGroup();
		this.timescaleMenu.add(createMenuItem("1h", "T,120", timescaleGroup,this.timescale==120));
		this.timescaleMenu.add(createMenuItem("6h", "T,720", timescaleGroup,this.timescale==720));
		this.timescaleMenu.add(createMenuItem("9h", "T,1080", timescaleGroup,this.timescale==1080));
		this.timescaleMenu.add(createMenuItem("12h", "T,1440", timescaleGroup,this.timescale==1440));
		this.timescaleMenu.add(createMenuItem("24h", "T,2880", timescaleGroup,this.timescale==2880));
		
		this.menu = new JPopupMenu();
		this.menu.add(this.filterMenu);
		this.menu.add(this.timescaleMenu);
		this.add(this.menu);
		
		this.setTimeScale(720);
		
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

	public void setTimeScale(int ts)
	{
		this.timescale = ts;
		selectMenuItem(this.timescaleMenu, ts);
		this.repaint();
	}
	
	public void setFilter(int f)
	{
		this.hypnogram.setFilter(f);
		selectMenuItem(this.filterMenu, f);
		this.repaint();
	}
	
	public int getTimeScale()
	{
		return this.timescale;
	}
	
	public int getFilter()
	{
		return this.hypnogram.getFilter();
	}
	/*
	 * die methode geht davon aus, dass den menuitems 
	 * actioncommands der Form "A,value" zugeordnet sind
	 */
	private void selectMenuItem(JMenu menu, int value)
	{
		int countItems = menu.getSubElements()[0].getSubElements().length;
		for (int i=0; i<countItems; i++)
		{
			JMenuItem menuItem = (JMenuItem)menu.getSubElements()[0].getSubElements()[i];
			String command = menuItem.getActionCommand();
			String[] part = command.split(",");
			if (part.length==2)
			{
				int itemValue = Integer.parseInt(part[1]);
				menuItem.setSelected(value==itemValue);
			}
		}
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
	@Override
	public void paint(Graphics g)
	{
		synchronized(this.hypnogram)
		{
			Graphics2D g2d = (Graphics2D)g;
			Stroke str0 = new BasicStroke(0);
			Stroke str2 = new BasicStroke(2);
			Stroke str4 = new BasicStroke(4);
			g.setColor(Color.black);
			g.fillRect(0,0,this.getWidth(), this.getHeight());
			g.setColor(Color.darkGray);
			g2d.setStroke(str0);
			for(int i=0; i<4; i++)
			{
				int y = (int)(this.getHeight() / 4 * (0.5 + i));
				g.drawLine(0, y, this.getWidth(), y);
			}
	
			int barCount = this.timescale; // anzahl der blocks, die geplottet werden sollen
			int barIndex = barCount-1; // block, der grade geplottet wird (von barcount-1 bis 0)
			int hypnoIndex = this.hypnogram.getStateCount()-1;
			float barWidth = this.getWidth() / (float)barCount;

			int x1 = 0;
			while(barIndex>=0 && hypnoIndex>=0)
			{
				Date timestamp = this.hypnogram.getTimeStamp(hypnoIndex);
				
				int sleepstate = this.hypnogram.getSleepState(hypnoIndex);
				x1 = (int)(barIndex * barWidth);
				if (hypnoIndex>0)
				{
					this.cal.setTime(this.hypnogram.getTimeStamp(hypnoIndex-1));
					int lastBlockHour = this.cal.get(Calendar.HOUR_OF_DAY);
					this.cal.setTime(timestamp);
					if (this.cal.get(Calendar.HOUR_OF_DAY) != lastBlockHour)
					{
						g.setColor(Color.darkGray);
						g2d.setStroke(str0);
						g.drawLine(x1, 0, x1, this.getHeight());
						g.setColor(Color.gray);
						g.drawString(this.cal.get(Calendar.HOUR_OF_DAY) + ":00", x1+4, 14);
					}
					if (this.hypnogram.getSleepStart() != null && this.hypnogram.getSleepStart().equals(timestamp))
					{
						g.setColor(Color.green);
						g2d.setStroke(str0);
						g.drawLine(x1, 0, x1, this.getHeight());
					}
				}
	
				g2d.setStroke(str0);
				g.setColor(Color.gray);
				
				int x2 = (int)((barIndex + 1) * barWidth);
				int y1 = (int)(this.getHeight() / 4 * (-0.5 + sleepstate));
				if (hypnoIndex>0 && barIndex>0)
				{
					int lastSleepstate = this.hypnogram.getSleepState(hypnoIndex-1);
					if (lastSleepstate>0 && sleepstate != lastSleepstate)
					{
						int y2 = (int)(this.getHeight() / 4 * (-0.5 + lastSleepstate));
						g.drawLine(x1, y1, x1, y2);
					}
				}
				
				g.drawLine(x1, y1, x2, y1);
				
				// gefiltert:
				
				sleepstate = this.hypnogram.getFilteredSleepState(hypnoIndex);
				x1 = (int)(barIndex * barWidth);
	
				x2 = (int)((barIndex + 1) * barWidth);
				y1 = (int)(this.getHeight() / 4 * (-0.5 + sleepstate));
				if (hypnoIndex>0 && barIndex>0)
				{
					int lastSleepstate = this.hypnogram.getFilteredSleepState(hypnoIndex-1);
					if (lastSleepstate>0 && sleepstate != lastSleepstate)
					{
						int y2 = (int)(this.getHeight() / 4 * (-0.5 + lastSleepstate));
						g2d.setStroke(str2);
						g.setColor(Color.white);
						g.drawLine(x1, y1, x1, y2);
					}
				}
				
				if (sleepstate == 2)
				{
					g2d.setStroke(str4);
					g.setColor(Color.green);
				}
				else if(sleepstate == 4)
				{
					g2d.setStroke(str4);
					g.setColor(Color.blue);
				}
				else
				{
					g2d.setStroke(str2);
					g.setColor(Color.white);
				}
				g.drawLine(x1, y1, x2, y1);
				
				hypnoIndex--;
				barIndex--;
			}
			if (this.hypnogram.getStateCount()>0 && x1>0)
			{
				g.setColor(Color.darkGray);
				g2d.setStroke(str0);
				g.drawLine(x1, 0, x1, this.getHeight());
				g.setColor(Color.gray);
				g.drawString(this.cal.get(Calendar.HOUR_OF_DAY) + ":" + this.cal.get(Calendar.MINUTE), x1+4, 14);
			}	
			g.setColor(Color.gray);
			g.drawString("Filter: " + this.hypnogram.getFilter(), 4, 14);
		}
	}

	
	@Override
	public void actionPerformed(ActionEvent e) 
	{
		String[] command = ((JMenuItem) e.getSource()).getActionCommand().split(",");
    	try
    	{
    		int i = Integer.parseInt(command[1]);
    		if (command[0].equals("F"))
    			this.setFilter(i);
    		if (command[0].equals("T"))
    			this.setTimeScale(i);
    	}
    	catch(NumberFormatException ex)
    	{
    		ex.printStackTrace();
    	}
	}
}
