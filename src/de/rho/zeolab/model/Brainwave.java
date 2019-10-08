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

package de.rho.zeolab.model;

import java.util.Date;
import java.util.logging.Logger;
import de.rho.zeolib.*;

public class Brainwave
{
	static Logger logger = Logger.getLogger(Brainwave.class.getName());
	
	int bufferSizeSeconds;
	double[] rawWave;
	double[] filteredWave;
	Date[] timestamp;
	int[] sqi;
	double[] impedance;
	boolean[] badSignal;
	int filter = 0;
	
	public Brainwave()
	{
		setBufferSizeSeconds(120);
	}
	
	public int getBufferSizeSeconds()
	{
		return this.bufferSizeSeconds;
	}

	public void setBufferSizeSeconds(int seconds)
	{
		if (this.bufferSizeSeconds!=seconds && seconds>1)
		{
			this.bufferSizeSeconds = seconds;
			this.rawWave = new double[128*this.bufferSizeSeconds];
			this.filteredWave = new double[128*this.bufferSizeSeconds];
			this.timestamp = new Date[this.bufferSizeSeconds];
			this.sqi = new int[this.bufferSizeSeconds];
			this.impedance = new double[this.bufferSizeSeconds];
			this.badSignal = new boolean[this.bufferSizeSeconds];
		}
	}
	
	public int getFilter()
	{
		return this.filter;
	}
	
	public void setFilter(int i)
	{
		if (i>=0 && i<=2 && i!=this.filter)
			this.filter = i;
	}
	
	public double getRawWave(int i)
	{
		if (i<this.bufferSizeSeconds*128)
			return this.rawWave[i];
		else
			return 0;
	}
	public double getFilteredWave(int i)
	{
		if (i<this.bufferSizeSeconds*128)
			return this.filteredWave[i];
		else
			return 0;
	}
	
	public Date getTimestamp()
	{
		return this.timestamp[this.bufferSizeSeconds-1];
	}
	public boolean getBadSignal(int i)
	{
		if (i<this.bufferSizeSeconds*128)
			return this.badSignal[i/128];
		else
			return false;
	}
	public int getSqi()
	{
		return this.sqi[this.bufferSizeSeconds-1];
	}
	public double getImpedance()
	{
		return this.impedance[this.bufferSizeSeconds-1];
	}
	
	public void addWavedata(double[] data, Date timestamp, boolean badSignal, int sqi, double impedance)
	{
		if (data != null)
		{
			synchronized(this)
			{
				logger.fine("data!=null");
				for(int i=0; i<(this.bufferSizeSeconds-1)*128; i++)
					this.rawWave[i] = this.rawWave[i+128];
				for(int i=0; i<this.bufferSizeSeconds-1; i++)
				{
					this.timestamp[i] = this.timestamp[i+1];
					this.badSignal[i] = this.badSignal[i+1];
					this.sqi[i] = this.sqi[i+1];
					this.impedance[i] = this.impedance[i+1];
				}
				for (int i=0; i<128; i++)
					this.rawWave[(this.bufferSizeSeconds-1)*128 + i] = data[i];
				
				if (this.filter==0)
				{
					for(int i=0; i<this.bufferSizeSeconds*128; i++)
						this.filteredWave[i] = this.rawWave[i];
				}
				else
					if (this.filter==1)
						this.filteredWave = ZeoUtility.filter50Hz(this.rawWave);
					else
						this.filteredWave = ZeoUtility.filter60Hz(this.rawWave);
				this.timestamp[this.bufferSizeSeconds-1] = timestamp;
				this.badSignal[this.bufferSizeSeconds-1] = badSignal;
				this.sqi[this.bufferSizeSeconds-1] = sqi;
				this.impedance[this.bufferSizeSeconds-1] = impedance;
			}
		}
	}
	

}
