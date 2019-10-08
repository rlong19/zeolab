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

import java.io.Serializable;
import java.util.Date;

public class ZeoTriggerEvent implements Serializable
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	static String[] sleepStates = {"Other","Awake","REM","Light","Deep","NREM","Sleep"};
	public boolean active=true;
	public boolean onlyOnce=false;
	public boolean repeat = false;
	public boolean hasTriggered = false;
	public int stateBefore=0; // dieser Sleepstate muss vor dem aktuellen vorgelegen haben
	public int countBefore=1; // mindestens so oft muss er vorgelegen haben
	public int stateCurrent=1; // dieser Sleepstate muss aktuell vorliegen
	public int countCurrent=1; // mindestens zum sovielten mal
	
	public Date alarmTime = null; // wird diese zeit erreicht, wird auf jeden fall getriggert
	public int countAlarm = 0; // zeitfenster (in 5min blöcken) innerhalb dessen vor alarmtime getriggert wird
	public int countSleep = 0; // soviele 5min blöcken müssen seit einschlafen vergangen sein, damit getriggert wird
	public Date startTime = null; // erst ab diesem zeitpunkt wird getriggert (
	
	public IZeoTriggerAction action=null; // auszulösende Aktion
	public String argument="";
	
	@Override
	public String toString()
	{
		String argstring = this.argument;
		if (argstring.length()>12)
			argstring = "..."+ argstring.substring(argstring.length()-12);
		return sleepStates[this.stateBefore] + " (" + this.countBefore + "x) -> " + 
			sleepStates[this.stateCurrent] + " (" + this.countCurrent + "x) : " + 
			action + " (" + argstring + ")";
	}
}