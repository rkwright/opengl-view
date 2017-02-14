/*******************************************************************************
 * Copyright (c) 2008 Ric Wright 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/org/documents/epl-v10.html
 * 
 * Contributors:
 *     Ric Wright - initial implementation
 ****************************************************************************/

package com.geofx.science.models;

import java.util.LinkedList;

import javax.vecmath.Point2d;

import com.geofx.opengl.util.Constants;
import com.geofx.opengl.util.DataIteratorXY;

public class TempWave
{
	private static final int 	NDEPTHS = 10;
	private static final double OMEGA_DAY = Constants.TWO_PI / (24.0 * 3600.0); // seconds in a day, so temp cycles in one day
	private static final double OMEGA_YEAR = Constants.TWO_PI / (24.0 * 3600.0 * 365.0); // seconds in a year, so temp cycles in one year
	
	protected int			curTime = 0;  // Current time, in seconds
	protected double[] 		depth  = { 0, 0.5, 1.0, 1.5, 2.0, 3.0, 4.0, 5.0, 7.5, 10.0 };
	protected double 		diffuse = 1e-04;
	protected double    	timeStep = 3600.0;
	protected double    	dailyAmplitude = 10.0;
	protected double    	annualAmplitude = 10.0;
	protected double    	meanTemp       = 0.0;
	protected double    	airTemperature = 0.0;			// today's air temperature
	protected double    	diurnalAmplitude = 0.0;			// today's temperature amplitude, i.e. (max-min)/2
	protected double    	damping        = Math.sqrt(2.0 * diffuse / OMEGA_DAY);
	protected double    	rateValue	   = 2;						// stepping rate of simulation
	//	protected DateTime	date =  new DateTime();	// 1 July 1970

	private int 			seriesSpan = (int)(24 * 7 * 3600);	
	private int 			seriesLen = (int)(seriesSpan / timeStep / rateValue);	

	private LinkedList<TempData> 	timeSeries = new LinkedList<TempData>();
	
	private SeriesIter		seriesIter = new SeriesIter(timeSeries);
	
	private int				depthIndex = 0;

//	private DecimalFormat format = new DecimalFormat("##00.0");
//	private DecimalFormat timefmt = new DecimalFormat("00000000");

	public class TempData
	{
		public double[] temp = new double[NDEPTHS];
		public double 	time;
	}
	
	public TempWave()
	{
		
	}
	
	public void initSim( int nSteps)
	{
		for ( int n=0; n<nSteps; n++ )
		{
			updateSim();
		}
	}

	public void updateSim ()
	{
		updateAirTemp();
		
		// then all the depth through time
		updateAllDepths();

		//dumpData();

		curTime += timeStep * rateValue;

		updateDate();
	}
	
	// update the air temp based on diurnal and annual cycle. Note returns delta from mean temp
	private void updateAirTemp ()
	{
		double diurnal_proportion = 0.3333;
		double annual_proportion = 0.05;
		
		diurnalAmplitude = dailyAmplitude * ((1.0-diurnal_proportion/2.0) + Math.random() * diurnal_proportion);
		airTemperature   = meanTemp + ((1.0-annual_proportion/2.0) + Math.random() * annual_proportion) * annualAmplitude * Math.cos(curTime * OMEGA_YEAR);
	}


	/**	 
	 * Update the temeperature for each depth at the current time* 
	 *
	 */
	private void updateAllDepths ()
	{
		TempData tempData = new TempData();
		tempData.time = curTime;

		for ( int j=0; j<NDEPTHS; j++ )
		{
			double dampingDepth    = depth[j] / damping;
			double dampedAmplitude = diurnalAmplitude * Math.exp(-depth[j] / damping);
			tempData.temp[j] = airTemperature + dampedAmplitude * Math.cos(curTime * OMEGA_DAY - dampingDepth);
		}
		
		timeSeries.add(tempData);
		if (timeSeries.size() > seriesLen)
			timeSeries.remove(0);
	}
	/*
	@SuppressWarnings("unused")
	private void dumpData( TempData tempData )
	{
		System.out.print("Time:" + timefmt.format(curTime) + " air: " + format.format(airTemperature));
		for ( int j=0; j<NDEPTHS; j++ )
		{
			System.out.print(" " + format.format(tempData.temp[j]));
		}
		System.out.println();
	}
	*/
	
	// update the date info
	private void updateDate ()
	{
	
	}

	public TempData getTempData()
	{
		return timeSeries.size() > 0 ? timeSeries.getLast() : null;
	}

	public double[] getDepths()
	{
		return depth;
	}

	public LinkedList<TempData> getTimeSeries()
	{
		return timeSeries;
	}

	public int getSeriesSpan()
	{
		return seriesSpan;
	}
	
	public void setDepthIndex(int depthIndex)
	{
		this.depthIndex = depthIndex;
	}

	public SeriesIter getSeriesIter()
	{
		return seriesIter;
	}
	public class SeriesIter implements DataIteratorXY
	{
		private int						index = 0;
		private LinkedList<TempData>	list = null;

		public SeriesIter ( LinkedList<TempData> list )
		{
			this.list = list;
		}
		
		public boolean hasNext()
		{
			return index < list.size();
		}

		public double getX()
		{
			return list.get(index).time;
		}

		public double getY()
		{
			return list.get(index).temp[depthIndex];
		}

		public void reset()
		{
			index = 0;
		}

		public void next()
		{
			index++;
		}

		public Point2d getXY(int index)
		{
			return null;
		}

		public int size()
		{
			return list.size();
		}
		
	}
}
