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
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.font.TextMeasurer;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import javax.swing.ButtonGroup;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;

import de.rho.zeolib.*;
import de.rho.zeolab.model.*;

public class SpectrogramPanel extends JPanel implements ActionListener
{
	Spectrogram spectrogram;
	Calendar cal = new GregorianCalendar();
	Color[] frequencyColors = new Color[7];
	double maxBar = 0.5;
	int timescale = 600;
	int gammaFactor = 10;
	SimpleDateFormat formatter = new SimpleDateFormat("HH:mm");
	JPopupMenu menu;
	JMenu factorMenu;
	JMenu maxbarMenu;
	JMenu timescaleMenu;
	
	public SpectrogramPanel(Spectrogram spectrogram)
	{
		this.spectrogram = spectrogram;
		
		this.frequencyColors[0] = new Color(101,0,154);
		this.frequencyColors[1] = new Color(0,12,254);
		this.frequencyColors[2] = new Color(0,246,254);
		this.frequencyColors[3] = new Color(18,255,0);
		this.frequencyColors[4] = new Color(255,247,0);
		this.frequencyColors[5] = new Color(255,121,1);
		this.frequencyColors[6] = new Color(255,0,0);
		
		this.factorMenu = new JMenu("Gamma-Factor");
		ButtonGroup factorGroup = new ButtonGroup();
		this.factorMenu.add(createMenuItem("1", "F,1", factorGroup,this.gammaFactor==1));
		this.factorMenu.add(createMenuItem("5", "F,5", factorGroup,this.gammaFactor==5));
		this.factorMenu.add(createMenuItem("10", "F,10", factorGroup,this.gammaFactor==10));
		this.timescaleMenu = new JMenu("Time Scale");
		ButtonGroup timescaleGroup = new ButtonGroup();
		this.timescaleMenu.add(createMenuItem("60", "T,60", timescaleGroup,this.timescale==60));
		this.timescaleMenu.add(createMenuItem("300", "T,300", timescaleGroup,this.timescale==300));
		this.timescaleMenu.add(createMenuItem("600", "T,600", timescaleGroup,this.timescale==600));
		this.timescaleMenu.add(createMenuItem("1hr", "T,3600", timescaleGroup,this.timescale==3600));
		this.timescaleMenu.add(createMenuItem("6hr", "T,21600", timescaleGroup,this.timescale==21600));
		this.maxbarMenu = new JMenu("Max.%");
		ButtonGroup maxbarGroup = new ButtonGroup();
		this.maxbarMenu.add(createMenuItem("25", "M,0.25", maxbarGroup,this.maxBar==0.25));
		this.maxbarMenu.add(createMenuItem("33", "M,0.33", maxbarGroup,this.maxBar==0.33));
		this.maxbarMenu.add(createMenuItem("50", "M,0.50", maxbarGroup,this.maxBar==0.50));
		this.maxbarMenu.add(createMenuItem("66", "M,0.66", maxbarGroup,this.maxBar==0.66));
		this.maxbarMenu.add(createMenuItem("75", "M,0.75", maxbarGroup,this.maxBar==0.75));
		this.maxbarMenu.add(createMenuItem("100", "M,1.00", maxbarGroup,this.maxBar==1.00));

		this.menu = new JPopupMenu();
		this.menu.add(factorMenu);
		this.menu.add(timescaleMenu);
		this.menu.add(maxbarMenu);
		this.add(this.menu);
		
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
	
	private JMenuItem createMenuItem(String title, String command, ButtonGroup group, boolean checked)
	{
		JRadioButtonMenuItem item = new JRadioButtonMenuItem(title);
		item.setActionCommand(command);
		group.add(item);
		item.addActionListener(this);
		item.setSelected(checked);
		return item;
	}
	
	public int getGammaFactor()
	{
		return this.gammaFactor;
	}
	
	public void setGammaFactor(int g)
	{
		this.gammaFactor = g;
		selectMenuItem(this.factorMenu, String.valueOf(g));
		this.repaint();
	}
	
	public double getMaxBar()
	{
		return this.maxBar;
	}
	
	public void setMaxBar(double max)
	{
		this.maxBar = max;
		selectMenuItem(this.maxbarMenu, String.valueOf(max));
		this.repaint();
	}
	
	public int getTimeScale()
	{
		return this.timescale;
	}
	
	public void setTimescale(int ts)
	{
		this.timescale = ts;
		selectMenuItem(this.timescaleMenu, String.valueOf(ts));
		this.repaint();
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
	
	@Override
	public void paint(Graphics g)
	{
		FontMetrics fm = g.getFontMetrics();
    
		int width = this.getWidth();
		int height = this.getHeight();
	    int fontHeight = fm.getHeight();
	    
		Graphics2D g2d = (Graphics2D) g;
		g2d.setColor(Color.black);
		g2d.fillRect(0,0,width,height);
		Stroke str0 = new BasicStroke(0);
		Stroke str2 = new BasicStroke(2);

		g2d.setStroke(str0);
		g2d.setColor(Color.darkGray);
		double barY = height;
		for(int b=0; b<10; b++)
		{
			barY -= (double)height/10.0/maxBar;
			g2d.drawLine(0, (int)barY, this.getWidth(), (int)barY);
			String text = (b+1)*10+"%";
			g2d.drawString(text, width - fm.stringWidth(text) - 2, (int)barY -2);
		}
		
		
		for(int f=0; f<7; f++)
		{
			int plotCount = this.timescale; // number of x-values that are to be plotted
			int plotIndex = plotCount-1; // x-value, which is being plotted (from plt count-1 to 0)
			int spectroIndex = this.spectrogram.getBinCount()-1;
			float xstep = this.getWidth() / (float)plotCount;

			double x1 = this.getWidth();
			double x2 = x1-xstep;
			while(plotIndex>=0 && spectroIndex>=1)
			{
				if (f==0)
				{
					this.cal.setTime(this.spectrogram.getTimeStamp(spectroIndex));
					if (this.cal.get(Calendar.MINUTE) == 0 && this.cal.get(Calendar.SECOND) == 0)
					{
						g2d.setStroke(str0);
						g2d.setColor(Color.darkGray);
						g2d.drawLine((int)x1, 0, (int)x1, this.getHeight());
						g.drawString(formatter.format(this.spectrogram.getTimeStamp(spectroIndex)), (int)x1+4, 14);
					}
				}
				g2d.setStroke(str2);
				g.setColor(this.frequencyColors[f]);
								
				int y1 = 0;
				int y2 = 0;
			
				if (f==6)
				{
					y1 = (int) (height - this.spectrogram.getFrequencyBin(spectroIndex,f)*this.gammaFactor*height/maxBar);
					y2 = (int) (height - this.spectrogram.getFrequencyBin(spectroIndex-1,f)*this.gammaFactor*height/maxBar);
				}
				else
				{
					y1 = (int) (height - this.spectrogram.getFrequencyBin(spectroIndex,f)*height/maxBar);
					y2 = (int) (height - this.spectrogram.getFrequencyBin(spectroIndex-1,f)*height/maxBar);
				}
				g.drawLine((int)x1, y1, (int)x2, y2);

				spectroIndex--;
				plotIndex--;
				x1 -= xstep;
				x2 -= xstep;
			}
		}
		
		for (int f=0; f<7; f++)
		{
			String text = ZeoUtility.getFrequencyBinName(f) + " (" + ZeoUtility.getFrequencyBin(f) + " Hz)";
			int x = 4;
			int y = (f+1) * (fontHeight+2); 
			g.setColor(Color.black);
			g.drawString(text, x+1, y+1);
			g.setColor(this.frequencyColors[f]);
			g.drawString(text, x, y);
		}
		
	}

	@Override
	public void actionPerformed(ActionEvent e) 
	{
		String[] command = ((JMenuItem) e.getSource()).getActionCommand().split(",");
    	try
    	{
    		if (command[0].equals("F"))
    		{
        		int i = Integer.parseInt(command[1]);
    			this.gammaFactor = i;
    			this.repaint();
    		}
    		if (command[0].equals("T"))
    		{
        		int i = Integer.parseInt(command[1]);
    			this.timescale = i;
    			this.repaint();
    		}
    		if (command[0].equals("M"))
    		{
    			double d = Double.parseDouble(command[1]);
    			this.maxBar = d;
    			this.repaint();
    		}
    	}
    	catch(NumberFormatException ex)
    	{
    		ex.printStackTrace();
    	}
   	}
	
}
