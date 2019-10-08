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

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Date;

import javax.swing.ButtonGroup;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;

import de.rho.zeolib.*;
import de.rho.zeolab.model.*;

public class SpectroviewPanel extends JPanel implements ActionListener
{
	Spectrogram spectrogram;
	
	Color[] frequencyColors = new Color[7];
	//double[] frequencyBins = new double[7];
	int gammaFactor = 1;
	JPopupMenu menu;
	JMenu factorMenu;
	
	public SpectroviewPanel(Spectrogram spectrogram)
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

		this.menu = new JPopupMenu();
		this.menu.add(factorMenu);
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
	private JMenuItem createMenuItem(String titel, String command, ButtonGroup group, boolean checked)
	{
		JRadioButtonMenuItem item = new JRadioButtonMenuItem(titel);
		item.setActionCommand(command);
		group.add(item);
		item.addActionListener(this);
		item.setSelected(checked);
		return item;
	}
	
	public void setGammaFactor(int f)
	{
		this.gammaFactor = f;
		selectMenuItem(this.factorMenu, String.valueOf(f));
		this.repaint();
	}
	public int getGammaFactor()
	{
		return this.gammaFactor;
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
		Graphics2D g2d = (Graphics2D)g;
		g2d.setColor(Color.black);
		g2d.fillRect(0,0,width,height);
	//	g2d.setFont(new Font("Arial",12,height / 10));
		int fontHeight = fm.getHeight();
		
		if (this.spectrogram.getBinCount()>0)
		{
			int index = this.spectrogram.getBinCount()-1;
			for(int f=0; f<7; f++)
			{
				double binHeight = this.spectrogram.getFrequencyBin(index, f)*(height-fontHeight-2);
				if (f==6)
					binHeight = Math.min((double)height-fontHeight-2,binHeight * this.gammaFactor);
				g2d.setColor(this.frequencyColors[f]);
				g2d.fillRect(f * width/7, height - fontHeight - (int)(binHeight) - 3, 
						width/7, (int)(binHeight)+1);
				
				String text = ZeoUtility.getFrequencyBin(f);
				int x = f * width/7 + width/14 - fm.stringWidth(text)/2;
				g2d.drawString(text,x,height-4);
				if (f==6)
				{
					text = this.gammaFactor + "x";
					x = f * width/7 + width/14 - fm.stringWidth(text)/2;
					g2d.drawString(text, x,  height - fontHeight - (int)(binHeight) - 5);
				}
			}
			
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
    			this.gammaFactor = i;
    	}
    	catch(NumberFormatException ex)
    	{
    		ex.printStackTrace();
    	}
   	}
}
