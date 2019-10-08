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
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import de.rho.zeolib.*;
import java.util.logging.*;

public class ZeoRecorder implements IZeoSliceListener
{
	static Logger logger = Logger.getLogger(ZeoRecorder.class.getName());

	boolean recording = false;
	boolean recordingPlainAscii = false;
	BufferedWriter outPlainAscii = null;
	Date lastTimestampPlainAscii = null;
	String targetFolder = System.getProperty("user.dir");
	boolean recordingEDF = false;
	BufferedOutputStream outEDF = null;
	Date lastTimestampEDF = null;
	String filenameEDF = "";
	long datarecordsEDF = 0;

	String patientInfo = "Zeo";
	String recordingInfo = "Recorded with ZeoLab";
	
	boolean recordingCSV = false;
	BufferedWriter outCSVRaw = null;
	BufferedWriter outCSVHisto = null;
	BufferedWriter outCSVSpectro = null;
	BufferedWriter outCSVEvents = null;
	String filenamePrefix = null;
	
	DecimalFormat decimalFormatter = new DecimalFormat( "0.0" );
	SimpleDateFormat dateFormatter = new SimpleDateFormat("dd.MM.yy");
	SimpleDateFormat timeFormatter = new SimpleDateFormat("HH.mm.ss");
	SimpleDateFormat timestampFormatter = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
	SimpleDateFormat timestampFormatterCSV = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
	SimpleDateFormat filenameDateFormatter = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
	GregorianCalendar cal = new GregorianCalendar();
	
	boolean zeroOutBadSignal = false;
	int filter = 0;

	double[] waveFilterBuffer = new double[256];
	
	public void setZeroOutBadSignal(boolean flag)
	{
		this.zeroOutBadSignal = flag;
	}
	
	public void setRecordAscii(boolean flag)
	{
		this.recordingPlainAscii = flag;
	}

	public void setFilter(int f)
	{
		if (f>=0 && f<=2)
			this.filter = f;
	}
	
	public int getFilter()
	{
		return this.filter;
	}
	public void setRecordEDF(boolean flag)
	{
		this.recordingEDF = flag;
	}

	public void setRecordCSV(boolean flag)
	{
		this.recordingCSV = flag;
	}
	
	public boolean getZeroOutBadSignal()
	{
		return this.zeroOutBadSignal;
	}
	
	public boolean getRecordAscii()
	{
		return this.recordingPlainAscii;
	}
	
	public boolean getRecordEDF()
	{
		return this.recordingEDF;
	}
	
	public boolean getRecordCSV()
	{
		return this.recordingCSV;
	}
	
	public boolean isRecording()
	{
		return this.recording;
	}
	
	public String getPatientInfo()
	{
		return this.patientInfo;
	}
	public void setPatientInfo(String info)
	{
		this.patientInfo = info;
	}
	public String getRecordingInfo()
	{
		return this.recordingInfo;
	}
	public void setRecordingInfo(String info)
	{
		this.recordingInfo = info;
	}
	public String getTargetFolder()
	{
		return this.targetFolder;
	}
	public void setTargetFolder(String folder)
	{
		if (!folder.equals("") && (new File(folder)).exists())
			this.targetFolder = folder;
	}
	
	public void start()
	{
		this.datarecordsEDF = 0;
		this.lastTimestampEDF = null;
		this.lastTimestampPlainAscii = null;
		this.recording = true;
		logger.info("recording started");
	}
	
	public void stop()
	{
		try 
		{
			if (this.outPlainAscii != null)
			{
				this.outPlainAscii.close();
				this.outPlainAscii = null;
			}
			if (this.outEDF != null)
			{
				this.outEDF.close();
				this.outEDF = null;
				writeEDFNumberOfDataRecords();	
			}
			if (this.outCSVEvents != null)
			{
				this.outCSVEvents.close();
				this.outCSVEvents = null;
			}
			if (this.outCSVRaw != null)
			{
				this.outCSVRaw.close();
				this.outCSVRaw = null;
			}
			if (this.outCSVHisto!= null)
			{
				this.outCSVHisto.close();
				this.outCSVHisto = null;
			}
			if (this.outCSVSpectro!= null)
			{
				this.outCSVSpectro.close();
				this.outCSVSpectro = null;
			}
			this.filenamePrefix = null;
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		}
		this.recording = false;
		logger.info("recording stopped");
	}
	
	public void writePlainAscii(ZeoSlice s) 
	{
		try
		{
			if (s.waveform != null)
			{
				if (this.outPlainAscii == null)
				{
					String filename = this.filenamePrefix + ".txt";
					logger.info("created " + filename);
					this.outPlainAscii = new BufferedWriter(new FileWriter(filename));
				}
				Date currentTimestamp = s.timestamp;
				if (lastTimestampPlainAscii != null)
				{
					cal.setTime(lastTimestampPlainAscii);
					cal.add(Calendar.SECOND, 1);
					while(cal.getTime().before(currentTimestamp))
					{
						for (int c=0; c<128; c++)
						{
							this.outPlainAscii.write("0.0");
							this.outPlainAscii.newLine();
						}		
						cal.add(Calendar.SECOND, 1);
					}
				}
			
				if (s.badSignal && this.zeroOutBadSignal)
				{
					for (int c=0; c<128; c++)
					{
						this.outPlainAscii.write("0.0");
						this.outPlainAscii.newLine();
					}
				}
				else
				{
					if (this.filter==0)
					{
						for (int i=0; i<128; i++)
						{
							this.outPlainAscii.write(String.valueOf(s.waveform[i]));
							this.outPlainAscii.newLine();
						}
					}
					else
					{
						double[] filtered = null;
						if (this.filter==1)
							filtered = ZeoUtility.filter50Hz(s.waveform);
						else
							filtered = ZeoUtility.filter60Hz(s.waveform);
						for (int i=0; i<128; i++)
						{
							this.outPlainAscii.write(String.valueOf(filtered[i]));
							this.outPlainAscii.newLine();
						}
					}
				}
				lastTimestampPlainAscii = currentTimestamp;
				this.outPlainAscii.flush();
			}
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
	}

	private void writeEDFHeader(String patient, String recording, Date timestamp)
	{
		try
		{
			this.outEDF.write(createCharArray("0",8));
			this.outEDF.write(createCharArray(patient,80));
			this.outEDF.write(createCharArray(recording,80));
			this.outEDF.write(createCharArray(dateFormatter.format(timestamp),8));
			this.outEDF.write(createCharArray(timeFormatter.format(timestamp),8));
			this.outEDF.write(createCharArray("512     ",8));
			this.outEDF.write(createCharArray("", 44));
			this.outEDF.write(createCharArray("-1", 8));
			this.outEDF.write(createCharArray("1", 8));
			this.outEDF.write(createCharArray("1", 4));
			this.outEDF.write(createCharArray("Zeo Raw Data", 16));
			this.outEDF.write(createCharArray("Zeo Electrode", 80));
			this.outEDF.write(createCharArray("uV", 8));
			this.outEDF.write(createCharArray("-315.0", 8));
			this.outEDF.write(createCharArray("315.0", 8));
			this.outEDF.write(createCharArray("-32768", 8));
			this.outEDF.write(createCharArray("32767", 8));
			this.outEDF.write(createCharArray("", 80));
			this.outEDF.write(createCharArray("128", 8));
			this.outEDF.write(createCharArray("", 32));
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
	}
	
	private void writeEDFNumberOfDataRecords()
	{
		try 
		{
			RandomAccessFile raf = new RandomAccessFile(this.filenameEDF, "rw" );
			raf.seek(236);
			byte[] bytes = createCharArray(String.valueOf(this.datarecordsEDF), 8);
			raf.write(bytes);
			raf.close();
		} 
		catch (FileNotFoundException e) 
		{
			e.printStackTrace();
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		}
	}
	
	private byte[] createCharArray(String s, int l)
	{
		while(s.length()<l)
			s+=" ";
		if (s.length() > l)
			s = s.substring(0,l);
		return s.getBytes();
	}
	
	private void writeEDF(ZeoSlice s) 
	{
		try
		{
			byte[] twobytes = new byte[2];
			
			if (s.waveform != null)
			{
				if (this.outEDF == null)
				{
					this.filenameEDF = this.filenamePrefix + ".edf";
					logger.info("created " + this.filenameEDF);
					
					this.outEDF = new BufferedOutputStream(new FileOutputStream(this.filenameEDF));
					writeEDFHeader(this.patientInfo, this.recordingInfo, s.timestamp);
				}
				Date currentTimestamp = s.timestamp;
				if (lastTimestampEDF != null)
				{
					cal.setTime(lastTimestampEDF);
					cal.add(Calendar.SECOND, 1);
					while(cal.getTime().before(currentTimestamp))
					{
						int2Bytes(twobytes,0);
						for (int c=0; c<128; c++)
						{
							this.outEDF.write(twobytes);
						}
						this.datarecordsEDF++;
						cal.add(Calendar.SECOND, 1);
					}
				}
			
				if (s.badSignal && this.zeroOutBadSignal)
				{
					for (int c=0; c<128; c++)
					{
						int2Bytes(twobytes,0);
						this.outEDF.write(twobytes);
					}
				}
				else
				{
					if (this.filter == 0)
					{
						for (int i=0; i<128; i++)
						{
							int value = (int) (s.waveform[i] / 315.0 * 0x8000);
							int2Bytes(twobytes,value);
							this.outEDF.write(twobytes);
						}
					}
					else
					{
						double[] filtered = null;
						if (this.filter==1)
							filtered = ZeoUtility.filter50Hz(this.waveFilterBuffer);
						else
							filtered = ZeoUtility.filter60Hz(this.waveFilterBuffer);
						for (int i=0; i<128; i++)
						{
							int value = (int) (filtered[i+90] / 315.0 * 0x8000);
							int2Bytes(twobytes,value);
							this.outEDF.write(twobytes);
						}
					}
				}
				this.datarecordsEDF++;
				lastTimestampEDF = currentTimestamp;
				this.outEDF.flush();
			}
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
	}
	
	private void int2Bytes(byte[] bytes, int i)
	{
		// int -> byte[]
		for (int s = 0; s < 2; ++s) 
		{
		    int shift = s << 3; // i * 8
		    bytes[s] = (byte)((i & (0xff << shift)) >>> shift);
		}
	}
	
	private void writeCSV(ZeoSlice s)
	{
		try
		{
			if (s.waveform != null)
			{
				if (this.outCSVRaw == null)
				{
					String filename = this.filenamePrefix + "_rawdata.csv";
					logger.info("created " + filename);
					this.outCSVRaw = new BufferedWriter(new FileWriter(filename));
					this.outCSVRaw.write("Time Stamp,Version,SQI,Impedance,Bad Signal (Y/N),Voltage (uV)");
					this.outCSVRaw.newLine();
				}
				this.outCSVRaw.write(timestampFormatterCSV.format(s.timestamp) + ",");
				this.outCSVRaw.write(s.version + ",");
				this.outCSVRaw.write(s.sqi + ",");
				this.outCSVRaw.write((int)s.impedance + ",");
				this.outCSVRaw.write(s.badSignal?"Y":"N");
				if (this.filter==0)
				{
					for(int i=0; i<128; i++)
						this.outCSVRaw.write("," + formatDecimal( s.waveform[i]));
				}
				else
				{
					double filtered[] = null;
					if (this.filter==1)
						filtered = ZeoUtility.filter50Hz(this.waveFilterBuffer);
					else
						filtered = ZeoUtility.filter60Hz(this.waveFilterBuffer);
					for(int i=0; i<128; i++)
						this.outCSVRaw.write("," + formatDecimal(filtered[i+90]));
				}
				this.outCSVRaw.newLine();
				this.outCSVRaw.flush();
			}
			if (s.frequencyBins != null)
			{
				if (this.outCSVSpectro== null)
				{
					String filename = this.filenamePrefix + "_spectrogram.csv";
					logger.info("created " + filename);
					this.outCSVSpectro = new BufferedWriter(new FileWriter(filename));
					this.outCSVSpectro.write("Time Stamp,Version,SQI,Impedance,Bad Signal (Y/N),2-4 Hz,4-8 Hz,8-13 Hz,11-14 Hz,13-18 Hz,18-21 Hz,30-50 Hz");
					this.outCSVSpectro.newLine();
				}
				this.outCSVSpectro.write(timestampFormatterCSV.format(s.timestamp) + ",");
				this.outCSVSpectro.write(s.version + ",");
				this.outCSVSpectro.write(s.sqi + ",");
				this.outCSVSpectro.write((int)s.impedance + ",");
				this.outCSVSpectro.write(s.badSignal?"Y":"N");
				for(int i=0; i<7; i++)
					this.outCSVSpectro.write("," + s.frequencyBins[i]);
				this.outCSVSpectro.newLine();
				this.outCSVSpectro.flush();
			}
			if (s.sleepState != -1)
			{
				if (this.outCSVHisto== null)
				{
					String filename = this.filenamePrefix + "_hypnogram.csv";
					logger.info("created " + filename);
					this.outCSVHisto = new BufferedWriter(new FileWriter(filename));
					this.outCSVHisto.write("Time Stamp,Version,SQI,Impedance,Bad Signal (Y/N),State (0-4),State (named)");
					this.outCSVHisto.newLine();
				}
				this.outCSVHisto.write(timestampFormatterCSV.format(s.timestamp) + ",");
				this.outCSVHisto.write(s.version + ",");
				this.outCSVHisto.write(s.sqi + ",");
				this.outCSVHisto.write((int)s.impedance + ",");
				this.outCSVHisto.write(s.badSignal?"Y,":"N,");
				if (s.sleepState == 0)
					this.outCSVHisto.write(s.sleepState + ",");
				else
					this.outCSVHisto.write((5-s.sleepState) + ","); // wg. komp. mit zeorecorder
				this.outCSVHisto.write(ZeoUtility.getSleepStage(s.sleepState));
				this.outCSVHisto.newLine();
				this.outCSVHisto.flush();
			}
			if (s.event != -1)
			{
				if (this.outCSVEvents == null)
				{
					String filename = this.filenamePrefix + "_events.csv";
					logger.info("created " + filename);
					this.outCSVEvents = new BufferedWriter(new FileWriter(filename));
					this.outCSVEvents.write("Time Stamp,Version,Event");
					this.outCSVEvents.newLine();
				}
				this.outCSVEvents.write("\"" + timestampFormatter.format(s.timestamp) + "\",");
				this.outCSVEvents.write("\"" + s.version + "\",");
				this.outCSVEvents.write("\"" + ZeoUtility.getEvent(s.event)+ "\"");
				this.outCSVEvents.newLine();
				this.outCSVEvents.flush();				
			}
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}		
	}

	private String formatDecimal(double value)
	{
		return this.decimalFormatter.format(value).replace(',', '.');
	}
	
	@Override
	public void receivedSlice(ZeoSlice s) 
	{
		if (s.waveform != null)
			for(int i=0; i<128; i++)
			{
				this.waveFilterBuffer[i] = this.waveFilterBuffer[i+128];
				this.waveFilterBuffer[i+128] = s.waveform[i];
			}
		
		if (this.recording)
		{
			if (this.filenamePrefix == null)
				this.filenamePrefix = this.targetFolder + File.separator + filenameDateFormatter.format(s.timestamp);
			
			if (this.recordingPlainAscii)
				writePlainAscii(s);
			if (this.recordingEDF)
				writeEDF(s);
			if (this.recordingCSV)
				writeCSV(s);
		}
	}
}
