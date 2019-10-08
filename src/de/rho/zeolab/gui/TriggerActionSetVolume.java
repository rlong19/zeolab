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

import java.io.Serializable;
import java.util.Date;

import de.rho.zeolab.core.IZeoTriggerAction;

public class TriggerActionSetVolume implements IZeoTriggerAction, Serializable
{
	@Override
	public void actionTriggered(String argument) 
	{
		Date now = new Date();
		System.out.println(now.toString() + ": set volume " + Integer.parseInt(argument)*0.01f);
		//wavPlayer.setVolume(Integer.parseInt(argument)*0.01f);
	}
	@Override
	public String toString()
	{
		return "SET VOLUME";
	}
}