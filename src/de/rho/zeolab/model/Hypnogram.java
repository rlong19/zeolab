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
import java.util.logging.*;

import de.rho.zeolib.*;

public class Hypnogram  
{
	class SleepState
	{
		Date timestamp;
		int sleepstate;
		public SleepState(Date timestamp, int sleepstate)
		{
			this.timestamp = timestamp;
			this.sleepstate = sleepstate;
		}
		public SleepState(SleepState state)
		{
			this.timestamp = state.timestamp;
			this.sleepstate = state.sleepstate;
		}
	}

	static Logger logger = Logger.getLogger("de.rh.zeo.Log");
	
	List<SleepState> sleepStates;
	List<SleepState> filteredSleepStates;
	int filter = 2;
	Calendar cal;
    Date sleepStart = null;
	public Hypnogram()
	{
		this.sleepStates = new ArrayList<SleepState>();
		this.filteredSleepStates = new ArrayList<SleepState>();
		this.cal = new GregorianCalendar();
	}
	public void reset()
	{
		this.sleepStates.clear();
		this.filteredSleepStates.clear();
	}
	public void setFilter(int filter)
	{
		this.filter = filter;
		calcFilteredSleepStates();
	}
	
	public int getFilter()
	{
		return this.filter;
	}
	public Date getSleepStart()
	{
		return this.sleepStart;
	}
	public int getSleepState(int index)
	{
		return this.sleepStates.get(index).sleepstate;
	}
	public Date getTimeStamp(int index)
	{
		return this.sleepStates.get(index).timestamp;
	}
	public int getFilteredSleepState(int index)
	{
		return this.filteredSleepStates.get(index).sleepstate;
	}
	public int getStateCount()
	{
		return this.sleepStates.size();
	}
	
	public void addSleepState(int sleepState, Date timestamp)
	{
		if (sleepState > 0 && sleepState <=4)
		{
			this.sleepStates.add(new SleepState(timestamp, sleepState));
			calcFilteredSleepStates();
			if (this.sleepStart == null)
			{
				if (this.getStateCount() > this.getFilter())
					if (this.getFilteredSleepState(this.getStateCount()-this.getFilter()-1) != 1)
							this.sleepStart = this.getTimeStamp(this.getStateCount()-this.getFilter()-1);
			}
		}
	}
	
	private void calcFilteredSleepStates()
	{
		synchronized(this)
		{
			this.filteredSleepStates.clear();
			
			for(int i = 0; i<this.sleepStates.size(); i++)
				this.filteredSleepStates.add(new SleepState(this.sleepStates.get(i)));
	
			boolean moreToFilter = (filter > 0  && this.filteredSleepStates.size() > this.filter+1);
			int maxpasses = 10;
			int passes = 0;
			while(moreToFilter && passes<maxpasses)
			{
				passes++;
				moreToFilter = false;
				int lastSleepState = this.sleepStates.get(1).sleepstate;
				int currentStateCount = 0;
				for(int i = this.filter+2; i<this.sleepStates.size(); i++)
				{
					if (lastSleepState != 0)
					{
						if (lastSleepState == this.filteredSleepStates.get(i).sleepstate)
							currentStateCount++;
						else
						{
							if (currentStateCount<=this.filter)
							{
								moreToFilter = true;
								for(int j=i-this.filter; j<i; j++)
								{
									this.filteredSleepStates.get(j).sleepstate = this.filteredSleepStates.get(i-this.filter-1).sleepstate; 
								}
							}
							currentStateCount = 0;
						}
					}
					lastSleepState = this.filteredSleepStates.get(i).sleepstate;
				}
			}
		}
	}
}