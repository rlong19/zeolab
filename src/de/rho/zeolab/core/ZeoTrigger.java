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

package de.rho.zeolab.core;


import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;


import de.rho.zeolib.*;
import de.rho.zeolab.model.*;


/*
 * ZeoTrigger verwaltet eine Liste von TriggerEvents und lauscht
 * auf ZeoSlices, um festzustellen, ob ein Event getriggert
 * werden muss. In dem Fall wird eine Aktion ausgelöst (z.B.
 * Abspielen eines Sounds)
 * 
 * States:
 * 0 = beliebig (aber anders als der jeweils anders eignestellte, also current oder before)
 * 1 = wach
 * 2 = REM
 * 3 = light
 * 4 = deep
 * 5 = NREM (light oder deep) = 3,4
 * 6 = Sleep (nicht wach) = 2,3,4
 */
public class ZeoTrigger implements IZeoSliceListener 
{
	ArrayList<ZeoTriggerEvent> triggerEvents;
	Hypnogram hypnogram;
	Calendar cal;
	public ZeoTrigger(Hypnogram hypnogram)
	{
		this.triggerEvents = new ArrayList<ZeoTriggerEvent>();
		this.hypnogram = hypnogram;
	//	this.hypnogram.addSliceListener(this);
		this.cal = new GregorianCalendar();
	}

	public void add(ZeoTriggerEvent triggerEvent)
	{
		this.triggerEvents.add(triggerEvent);
	}
	
	public void remove(ZeoTriggerEvent triggerEvent)
	{
		if (this.triggerEvents.contains(triggerEvent))
			this.triggerEvents.remove(triggerEvent);
	}
	
	public ArrayList<ZeoTriggerEvent> getEvents()
	{
		return this.triggerEvents;
	}
	
	
	@Override
	public void receivedSlice(ZeoSlice s) 
	{
		//this.hypnogram.receivedSlice(s);
		//if (this.hypnogram.new5MinutesBlock)
		checkTriggerEvents();
	}

	private boolean containsSleepPhase(int extendedPhase, int phase)
	{
		if (extendedPhase>=1 && extendedPhase<=4)
			return extendedPhase == phase;
		if (extendedPhase==5)
			return phase >= 3;
		return phase >=2;
	}
	
	private boolean isEarlierTime(Date earlier, Date later)
	{
		int h1, h2;
		int m1, m2;
		this.cal.setTime(earlier);
		h1 = this.cal.get(Calendar.HOUR_OF_DAY);
		m1 = this.cal.get(Calendar.MINUTE);
		this.cal.setTime(later);
		h2 = this.cal.get(Calendar.HOUR_OF_DAY);
		m2 = this.cal.get(Calendar.MINUTE);
		
		if (h1==h2)
			return m1<m2;
		
		int hdist1 = h2 - h1;    
		if (hdist1<0) hdist1+=24; 

		int hdist2 = h1 - h2;  
		if (hdist2<0) hdist2+=24; 
		
		return hdist1<hdist2;
		
	}
	
	private void checkTriggerEvents()
	{
		for(ZeoTriggerEvent triggerEvent:this.triggerEvents)
		{
			boolean trigger = true;
			if (triggerEvent.active && ((!triggerEvent.onlyOnce && triggerEvent.alarmTime==null) || !triggerEvent.hasTriggered))
			{
				/*
				 * AlarmTime und CountAlarm (Window)
				 *
				 * Wenn alarmtime gesetzt ist und wir die alarmtime grade überschritten haben, wird
				 * auf jeden fall getriggert
				 * 
				 * anderenfalls: Wenn alarmtime ist, wird trigger auf false gesetzt, wenn wir 
				 * vor dem countalarm-Windows sind
				 * 
				 * 
				 */
				boolean alarm = false;
				if (triggerEvent.alarmTime != null && !triggerEvent.hasTriggered)
				{
					if (this.hypnogram.getStateCount()>1)
					{
						int hypnoIndex = this.hypnogram.getStateCount()-1;
						if (isEarlierTime(this.hypnogram.getTimeStamp(hypnoIndex-1), triggerEvent.alarmTime) &&
							!isEarlierTime(this.hypnogram.getTimeStamp(hypnoIndex),triggerEvent.alarmTime))
						{
							alarm = true;
							System.out.println("alarm: " + this.hypnogram.getTimeStamp(hypnoIndex).toString());
						}
					}
				}
				if (!alarm)
				{
					if (trigger)
					{
						for(int i=0; i<triggerEvent.countCurrent; i++)
						{
							if (triggerEvent.stateCurrent == 0) // irgendwas anderes als before
							{
								if (this.hypnogram.getSleepState(this.hypnogram.getStateCount()-i-1) == triggerEvent.stateBefore)
								{
									trigger = false;
									break;
								}
							}
							else
							{
								if (!containsSleepPhase(triggerEvent.stateCurrent, this.hypnogram.getSleepState(this.hypnogram.getStateCount()-i-1)))
								{
									trigger = false;
									break;
								}
							}
						}
						if (trigger)
						{
							for(int i=triggerEvent.countCurrent; i<triggerEvent.countCurrent+triggerEvent.countBefore; i++)
							{
								if (triggerEvent.stateBefore == 0) // irgendwas anderes als current
								{
									if (this.hypnogram.getSleepState(this.hypnogram.getStateCount()-i-1) == triggerEvent.stateCurrent)
									{
										trigger = false;
										break;
									}
								}
								else
								{
									if (!containsSleepPhase(triggerEvent.stateBefore, this.hypnogram.getSleepState(this.hypnogram.getStateCount()-i-1)))
									{
										trigger = false;
										break;
									}
								}
							}
						}
					}
				}
				if (trigger)
				{
					if (triggerEvent.action != null)
						triggerEvent.action.actionTriggered(triggerEvent.argument);
					triggerEvent.hasTriggered = true;
				}
			}
		}
	}
}
