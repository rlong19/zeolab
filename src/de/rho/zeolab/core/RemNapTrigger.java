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

import java.util.Date;
import java.util.logging.*;

import de.rho.zeolib.*;
import de.rho.soundlib.*;
import de.rho.zeolab.model.*;

public class RemNapTrigger implements IZeoSliceListener 
{
	static Logger logger = Logger.getLogger(RemNapTrigger.class.getName());
	
	public boolean active = true;
	public int timeToFirstSoundMin = 5;
	public int timeBetweenSoundsMin = 2;
	public int wakeDetectWindowMin = 2;
	public int minSleepTimeMin = 240;
	Date lastSoundTimestamp = null;
	Date remStartTimestamp = null;
	public double startVol = 0.1;
	public double maxVol = 1.0;
	public double volOffset = 0.1;
	public double volDownRemEnd = 0.2;
	public double volDownWakeDetected = 0.0;
	boolean wakeDetected = false;
	double volume = startVol;	
	Hypnogram hypnogram;
	WavPlayer wavPlayer = new WavPlayer();
	
	public String wavFileName="thisisadream.wav";
	
	
	public RemNapTrigger(Hypnogram hypnogram)
	{
		this.hypnogram = hypnogram;
	//	this.hypnogram.addSliceListener(this);
	}

	@Override
	public void receivedSlice(ZeoSlice s) 
	{
		if (s.sleepState>0 && this.active)
		{
			if (this.hypnogram.getStateCount() > this.hypnogram.getFilter())
			{
				int index = this.hypnogram.getStateCount()-this.hypnogram.getFilter()-1;
			
				if (this.hypnogram.getFilteredSleepState(index)!=2)
				{
					if (this.remStartTimestamp != null)
						logger.info("REM END");
					this.remStartTimestamp = null;
					this.lastSoundTimestamp = null;
					this.volume -= this.volDownRemEnd;
					if (this.volume<this.startVol)
						this.volume = this.startVol;
				}
				else
				{
					Date zeoTimeStamp = new Date(s.timestamp.getTime()); 
				    if (this.remStartTimestamp == null)
				    {
				    	logger.info("REM START");
						this.remStartTimestamp = zeoTimeStamp;
				    }
				    if (this.hypnogram.getSleepStart()!=null && 
				    	s.timestamp.getTime() - this.hypnogram.getSleepStart().getTime() > this.minSleepTimeMin * 60000)
				    {
					    if (this.lastSoundTimestamp == null)
					    {
					    	if (zeoTimeStamp.getTime() - this.remStartTimestamp.getTime() >= this.timeToFirstSoundMin * 60000)
						    {
					    		this.wakeDetected = false;
					    		logger.info("play sound " + round2(this.volume));
					    		try 
					    		{
					    			this.wavPlayer.setVolume(volume);
									this.wavPlayer.start(wavFileName);
								} 
					    		catch (Exception e) 
					    		{
									e.printStackTrace();
								}
						        this.lastSoundTimestamp = zeoTimeStamp;
						    }
						}
					    else
					    {
					    	if (zeoTimeStamp.getTime() - this.lastSoundTimestamp.getTime() >= this.timeBetweenSoundsMin * 60000)
					    	{
					    		if (!this.wakeDetected)
					    		{
					    			this.volume += this.volOffset;
					    			if (this.volume > this.maxVol)
					    				this.volume = this.maxVol;
					    		}
					    		logger.info("play sound " + round2(this.volume));
					    		try 
					    		{
					    			this.wavPlayer.setVolume(volume);
									this.wavPlayer.start(wavFileName);
								} 
					    		catch (Exception e) 
					    		{
									e.printStackTrace();
								}
					    		this.wakeDetected = false;
					    		this.lastSoundTimestamp = zeoTimeStamp;
					    	}
					    	else
					    	{
					    		if (zeoTimeStamp.getTime() - this.lastSoundTimestamp.getTime() < this.wakeDetectWindowMin * 60000)
					    		{
					    			if (this.hypnogram.getSleepState(index) != 2)
					    			{
					    				this.wakeDetected = true;
					    				logger.info("WAKE DETECTED");
					    				this.volume -= this.volDownWakeDetected;
										if (this.volume<this.startVol)
											this.volume = this.startVol;
					    			}
					    		}
					    	}
					    }
				    }
				}
			}		
		}
	}
	
	private static double round2(double value) 
	{
		double result = value * 100;
		result = Math.round(result);
		result = result / 100;
		return result;
	}	
}
