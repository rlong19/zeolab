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


import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

import de.rho.zeolib.*;

public class ZeoPlayer implements Runnable
{
	List<ZeoSlice> slices = null;
	List<IZeoSliceListener> sliceListeners = null;
	Thread thread;
	boolean stop = false;
	boolean paused = false;

	int replaySpeedFactor = 10;
	
	String filenamePrefix;
	
	public ZeoPlayer()
	{
		System.out.println("construct");
		this.slices = new ArrayList<ZeoSlice>();
		this.sliceListeners = new ArrayList<IZeoSliceListener>();
	}
	
	public void addSliceListener(IZeoSliceListener listener)
	{
		if (!this.sliceListeners.contains(listener))
			this.sliceListeners.add(listener);
	}
	public void removeSliceListener(IZeoSliceListener listener)
	{
		if (this.sliceListeners.contains(listener))
			this.sliceListeners.remove(listener);
	}
	public void removeAllListeners()
	{
		this.sliceListeners.clear();
	}
	
	public void setFilenamePrefix(String prefix)
	{
		this.filenamePrefix = prefix;
	}
	
	public int getReplaySpeedFactor()
	{
		return this.replaySpeedFactor;
	}
	
	public void setReplaySpeedFactor(int f)
	{
		this.replaySpeedFactor = f;
		System.out.println(this.replaySpeedFactor);
	}
	public void start()
	{
		if(this.paused){
			this.paused = false;
		}
		else
		{
			this.stop = false;
			this.paused = false;
			this.thread = new Thread(this); 
			this.thread.start();
		}
	}
	public void stop()
	{
		this.stop = true;
	}
	public void pause()
	{
		this.paused = true;
	}
	public void resume()
	{
		this.paused = false;
		//this.thread.notify();
	}
	@Override
	public void run() 
	{
		Calendar cal = new GregorianCalendar();
		Calendar cal2 = new GregorianCalendar();
		SimpleDateFormat timestampFormatterCSV = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
		BufferedReader inRaw;
		BufferedReader inHypno;
		BufferedReader inSpectro;
		try 
		{
			Date nextHypnoTimestamp = null;
			int nextHypnoSleepstate = -1;
			Date nextSpectroTimestamp = null;
			double[] nextSpectroBins = null;
			String lineRaw;
			inRaw = new BufferedReader(new FileReader(new File(this.filenamePrefix + "rawdata.csv")));
			inHypno = new BufferedReader(new FileReader(new File(this.filenamePrefix + "hypnogram.csv")));
			inSpectro = new BufferedReader(new FileReader(new File(this.filenamePrefix + "spectrogram.csv")));
			//skip first line
			inRaw.readLine(); 
			inHypno.readLine();
			inSpectro.readLine();
			while (!this.stop)
			{
				if(this.paused){
					Thread.sleep(1000);
				}
				else
				{
					lineRaw = inRaw.readLine();
					if(lineRaw == null)
						break;
					try
					{
						String[] columns = lineRaw.split(",");
						ZeoSlice slice = new ZeoSlice();
						slice.badSignal = columns[4].equals("Y");
						slice.impedance = Integer.valueOf(columns[3]);
						slice.sqi = Short.valueOf(columns[2]);
						slice.timestamp = timestampFormatterCSV.parse(columns[0]);
						slice.version = Integer.valueOf(columns[1]);
						slice.waveform = new double[128];
						for(int i = 0; i<128; i++)
							slice.waveform[i] = Double.valueOf(columns[5+i]);
						
						if (nextHypnoTimestamp != null)
						{
							cal.setTime(nextHypnoTimestamp);
							cal2.setTime(slice.timestamp);
							if (slice.timestamp.equals(nextHypnoTimestamp) || cal.before(cal2))
							{
								slice.sleepState = nextHypnoSleepstate;
						
								String lineHypno;
								try
								{
									lineHypno = inHypno.readLine();
									columns = lineHypno.split(",");
									nextHypnoTimestamp = timestampFormatterCSV.parse(columns[0]);
									int state = Integer.valueOf(columns[5]);
									if (state==0)
										nextHypnoSleepstate = 0;
									else
										nextHypnoSleepstate = 5 - state;
								}
								catch(Exception e)
								{
									System.out.println(e);
								}
							}
						}
						else
						{
							String lineHypno;
							try
							{
								lineHypno = inHypno.readLine();
								columns = lineHypno.split(",");
								nextHypnoTimestamp = timestampFormatterCSV.parse(columns[0]);
								nextHypnoSleepstate = Integer.valueOf(columns[5]);
							}
							catch(Exception e)
							{
								System.out.println(e);
							}
						}
							
						if (nextSpectroTimestamp != null)
						{
							cal.setTime(nextSpectroTimestamp);
							cal2.setTime(slice.timestamp);
							if (slice.timestamp.equals(nextSpectroTimestamp) || cal.before(cal2))
							{
								slice.frequencyBins = nextSpectroBins;
						
								String lineSpectro;
								try
								{
									lineSpectro = inSpectro.readLine();
									if(lineSpectro != null){
										columns = lineSpectro.split(",");
										nextSpectroTimestamp = timestampFormatterCSV.parse(columns[0]);
										nextSpectroBins = new double[7];
										for(int f=0;f<7;f++)
											nextSpectroBins[f] = Double.valueOf(columns[5+f]);
									}
								}
								catch(Exception e)
								{
									System.out.println(e);
								}
							}
						}
						else
						{
							String lineSpectro;
							try
							{
								lineSpectro = inSpectro.readLine();
								columns = lineSpectro.split(",");
								nextSpectroTimestamp = timestampFormatterCSV.parse(columns[0]);
								nextSpectroBins = new double[7];
								for(int f=0;f<7;f++)
									nextSpectroBins[f] = Double.valueOf(columns[5+f]);
							}
							catch(Exception e)
							{
								System.out.println(e);
							}
						}
						
						for(IZeoSliceListener listener:this.sliceListeners)
							if (listener!=null)
								listener.receivedSlice(slice);
						Thread.sleep((int)(1000.0 * 1.0/this.replaySpeedFactor));
					}
					catch(Exception e)
					{
						System.out.println(e);
					}
				}
			}
			inRaw.close();
			inHypno.close();
			inSpectro.close();
		}
		catch(Exception e)
		{
			System.out.println(e);
		}
	}
}
