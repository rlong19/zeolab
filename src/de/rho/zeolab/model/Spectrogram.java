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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import de.rho.zeolib.*;

public class Spectrogram 
{
	class FrequencyBins
	{
		Date timestamp;
		double[] frequencyBins;
		public FrequencyBins(Date timestamp, double[] bins)
		{
			this.timestamp = timestamp;
			this.frequencyBins = new double[7];
			if (bins!=null && bins.length==7)
				for(int f=0; f<7; f++)
					this.frequencyBins[f] = bins[f];
		}
	}
	
	List<FrequencyBins> frequencyBins;

	Calendar cal;
    
	public Spectrogram()
	{
		this.frequencyBins = new ArrayList<FrequencyBins>();
		this.cal = new GregorianCalendar();
	}

	public void reset()
	{
		this.frequencyBins.clear();
	}
	
	public double getFrequencyBin(int index, int bin)
	{
		return this.frequencyBins.get(index).frequencyBins[bin];
	}
	public Date getTimeStamp(int index)
	{
		return this.frequencyBins.get(index).timestamp;
	}
	public int getBinCount()
	{
		return this.frequencyBins.size();
	}
	
	public void addFrequencyBins(Date timestamp, double[] frequencyBins)
	{
		if (frequencyBins != null)
			this.frequencyBins.add(new FrequencyBins(timestamp, frequencyBins));
	}
}
