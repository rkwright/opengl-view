/*******************************************************************************
 * 
 * 
 * Code from the JOGL Demos:  https://jogl-demos.dev.java.net/
 * 
 * Contributors:
 *    
 *     Ric Wright - May 2008 - Ported to Eclipse 3.2, minor tweaks
 *******************************************************************************/
package com.geofx.opengl.examples;

import java.awt.Color;
import java.awt.event.KeyEvent;
import java.lang.reflect.Array;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.Locale;

import javax.media.opengl.GL;
import javax.media.opengl.GLAutoDrawable;

import org.eclipse.swt.widgets.Composite;

import com.geofx.opengl.view.GLScene;
import com.geofx.opengl.util.CTM;
import com.geofx.opengl.util.Graph2D;
import com.geofx.opengl.util.LineCap;
import com.geofx.opengl.util.LineJoin;
import com.geofx.opengl.util.PSGraphics;
import com.geofx.opengl.util.Axis.AxisPos;
import com.geofx.opengl.util.LineCap.CapType;
import com.geofx.opengl.util.LineJoin.JoinType;
import com.geofx.science.models.TempWave;
import com.geofx.science.models.TempWave.SeriesIter;
import com.geofx.science.models.TempWave.TempData;

public class TempWaveScene extends GLScene 
{
	public static final int 	MAX_ITEMS = 100;

	private static final double FONT_SCALE = 0.04;

	float[] 		LightDiffuse =	 { 1.0f, 1.0f, 1.0f, 1.0f };
	float[] 		LightAmbient =	 { 0.3f, 0.3f, 0.3f, 1.0f };
	float[] 		LightPosition =	 { 10.8f, 10.8f, 10.8f, 0.0f };
	float[]			specular = { 1.0f, 1.0f, 1.0f, 1.0f };
	float[]			magenta = { 1.0f, 0.0f, 1.0f, 0.5f };
	float[]			black = { 0f, 0f, 0f, 1f };
	float[]     	shininess = { 100.0f };	
	private int[]	colors = { 0xff0000,
							   0xff00ff,
							   0x00ff00,
							   0xffff00,
							   0x0000ff,
							   0x00ffff,
							   0x00ff00,
							   0xff8000,
							   0x8000ff,
							   0x80ff00,
							   0x800080  };
	
	@SuppressWarnings("unused")
	// private DecimalFormat format = new DecimalFormat("####");

	private CTM 	ctm = new CTM();

	PSGraphics			ps = null;
	Graph2D				profileGr = null;
	Graph2D				seriesGr  = null;

	private JoinType 	joinType = JoinType.Bevel;
	private CapType 	capType  = CapType.Butt;
	private double		strokeWidth = 0.01;
	private double[][]	dashArray = { 
										{ 0.0 },
										{ 2.0 },
									   	{ 0.5, 0.2 },
									   	{ 0.1 }
									};
	
	private int 		dashIndex = 0;
	
	String[] fonts = {
						"Times New Roman",
						"Trebuchet MS",
						"Palatino Linotype Italic",
						"TrajanPro-Bold",
						"Monotype Corsiva"
					 };
	
	int	 fontIndex = 1;

	private TempWave				tempWave = new TempWave();
	private LinkedList<TempData> 	timeSeries = tempWave.getTimeSeries();
	private TempData 				tempData = tempWave.getTempData();

	public TempWaveScene(Composite parent, java.awt.Frame glFrame)
	{
		super(parent, glFrame);

		System.out.println("StrokePathScene - constructor");

		ps = new PSGraphics();
		profileGr = new Graph2D(ps);
		seriesGr = new Graph2D(ps);
	
		this.grip.setOffsets(0, 0, -10f);
		//	this.grip.setRotation( 45.0f, -30.0f, 0.0f);

	}

	// a default constructor used by the ClassInfo enumerator
	public TempWaveScene()
	{
		super();
	}

	public void init ( GLAutoDrawable drawable ) 
	{
		super.init(drawable);	
		
		System.out.println("StrokePathScene - init");

		final GL gl = drawable.getGL();	

		gl.setSwapInterval(1);

        gl.glLightfv(GL.GL_LIGHT0, GL.GL_AMBIENT, LightAmbient, 0);						
        gl.glLightfv(GL.GL_LIGHT0, GL.GL_DIFFUSE, LightDiffuse, 0);				

		gl.glLightfv(GL.GL_LIGHT0, GL.GL_POSITION, LightPosition, 0);			

		gl.glMaterialfv(GL.GL_FRONT, GL.GL_SPECULAR, specular, 0);
		gl.glMaterialfv(GL.GL_FRONT, GL.GL_SHININESS, shininess, 0);
	
	    this.lightingEnabled = true;	
	    
	    initAnimator(drawable, 60);
	        
	    float saturation = 1.0f;
	    float brightness = 1.0f;
	    for ( int i=0; i<10; i++ )
	    {
			colors[i] = Color.HSBtoRGB((float)i/9.0f, saturation, brightness) & 0xffffff;
	    }
	    
		setupGraphs(gl);
  	}

	public void reshape( GLAutoDrawable drawable, int x, int y, int width, int height ) 
	{
		System.out.println("StrokePathScene - reshape");

		final GL gl = drawable.getGL();		
		
		gl.glMatrixMode(GL.GL_PROJECTION);
		gl.glLoadIdentity();
				
		glu.gluPerspective( 15.0f, 
		        (double) width / (double) height, 
	            0.1f,
	            100.0f);
		
		gl.glMatrixMode(GL.GL_MODELVIEW);
		gl.glLoadIdentity();
	}
	
	/**
	 * Standard JOGL display method.  Called on every refresh
	 */
	public void display(GLAutoDrawable drawable) 
	{
		GL gl = drawable.getGL();

		super.display(drawable);

		//System.out.println("StrokePathScene - display");

		try
		{
			updateLighting(gl);
		
			//drawAxes(gl);

			// if this is a just a refresh, don't update the model
			if (animator.isAnimating())
			{
				tempWave.updateSim();
			}

			drawProfile(gl);
			drawSeries(gl);
						
			if (animator.isAnimating())
			{
	    		fpsCounter.draw();
				statusMessage.draw();
			}

		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	private void setupGraphs(GL gl)
	{
		profileGr.setFontScale(FONT_SCALE);
		
		// first the profile
		profileGr.setViewPort(gl, -1f, 0f, 0f, 1f, -20, 10, 20, 0);

		profileGr.setProps(JoinType.Round, CapType.Round, 0.01, 0.01, 0xc0c0c0);
	
		profileGr.setAxis(AxisPos.LEFT, 2, 0, 0, 2, "depth (m)", null);

		profileGr.setAxis(AxisPos.BOTTOM, 10, 0, 0, 2, "temperature (deg C)", null);

		profileGr.drawAxes(gl);

		// then the series
		seriesGr.setFontScale(FONT_SCALE);

		timeSeries = tempWave.getTimeSeries();
					
		seriesGr.setViewPort(gl, -1f, -1f, 1f, 0f, 0, -20, 1, 20);

		seriesGr.setProps(JoinType.Round, CapType.Round, 0.01, 0.01, 0xc0c0c0);
	
		seriesGr.setAxis(AxisPos.LEFT, 10, 0, 0, 2, "temperature (deg C)", null);

		seriesGr.setAxis(AxisPos.BOTTOM, 0, 0, 0, 2, "time", null);
		
		seriesGr.drawAxes(gl);
	}
	
	private void drawProfile( GL gl )
	{	
		tempData = tempWave.getTempData();

		gl.glPushMatrix();
		
		profileGr.setViewPort(gl, -1f, 0f, 0f, 1f, -20, 10, 20, 0);

		profileGr.drawAxes(gl);

		ps.setlinejoin(joinType);
		ps.setlinecap(capType);
		
		ps.newpath();
		
		if (tempData != null)
		{
			double[] temps = tempData.temp;
			double[] depths = tempWave.getDepths();
	
			profileGr.drawPolyline(temps, depths);
		}
		
		gl.glPopMatrix();
	}
	
	private void drawSeries( GL gl )
	{

		gl.glPushMatrix();
	
		seriesGr.setViewPort(gl, -1f, -1f, 1f, 0f, 0, -20, 1, 20);

		seriesGr.drawAxes(gl);

		if (timeSeries != null && timeSeries.size() > 0)
		{
			double 	time0 = timeSeries.getFirst().time;
			double 	timeFin = time0 + tempWave.getSeriesSpan();
			int 	numDepths = Array.getLength(timeSeries.getFirst().temp);

			System.out.println("TimeSeries Len: " + tempWave.getSeriesSpan());
			
			ps.setlinejoin(joinType);
			ps.setlinecap(capType);
	
			seriesGr.setViewPort(gl, -1f, -1f, 1f, 0f, time0, -20, timeFin, 20);

			SeriesIter	iter = tempWave.getSeriesIter();
	
			for ( int n=0; n<numDepths; n++ )
			{
				gl.glTranslated(0.0, 0.0, -0.0001 * n);
				
				iter.reset();
					
				tempWave.setDepthIndex(n);
				
				seriesGr.setSymbolColor(colors[n]);
				
				seriesGr.drawPolyline(iter);
			}
		}
		
		gl.glPopMatrix();
	}
	
	public void  enableLighting( GL gl )
	{
	    gl.glEnable(GL.GL_LIGHT0);
	}
	
	public String getDescription()
	{
		return "A temperature wave animation";
	}

	public String getLabel()
	{
		return "Temp Wave";
	}

	// allows subclasses to get key events and do something interesting...
	protected boolean handleKeyEvent( KeyEvent e )
	{
		boolean	handled = true;
		switch (e.getKeyChar())
	
		{
			case 'c':
				capType = LineCap.convert((capType.ordinal() + 1) % 3);
				break;
	
			case 'j':
				joinType = LineJoin.convert((joinType.ordinal() + 1) % 3);
				break;

			case 'f':
				fontIndex = ++fontIndex % fonts.length;
				break;

			case 'l':
				strokeWidth *= 2.0;
				if (strokeWidth > 0.2)
					strokeWidth = 0.005;
				ps.setlinewidth(strokeWidth);
				break;

			case 'd':
				dashIndex = ++dashIndex % Array.getLength(dashArray);
				break;

			default:
				handled = false;
		}
		
		String str =  "  cap: " + capType.toString() + "  join: " +	joinType.toString();
		statusMessage.setMessage(str);

		return handled;
	}
	
	public void testFormat()
	{
		long n = 461012;
		
		System.out.println(String.format(Locale.FRANCE," The result of is %6.3f", Math.PI));
		
		System.out.format("%d%n", n); //  -->  "461012"
		System.out.format("%08d%n", n); //  -->  "00461012"
		System.out.format("%+8d%n", n); //  -->  " +461012"
		System.out.format("%,8d%n", n); //  -->  " 461,012"
		System.out.format("%+,8d%n%n", n); //  -->  "+461,012"

		double pi = Math.PI;
		System.out.format("%f%n", pi); //  -->  "3.141593"
		System.out.format("%.3f%n", pi); //  -->  "3.142"
		System.out.format("%10.3f%n", pi); //  -->  "     3.142"
		System.out.format("%-10.3f%n", pi); //  -->  "3.142"
		System.out.format(Locale.FRANCE, "%-10.4f%n%n", pi); //  -->  "3,1416"

		Calendar c = Calendar.getInstance();
		System.out.format("%tB %te, %tY%n", c, c, c); //  -->  "May 29, 2006"
		System.out.format("%tl:%tM %tp%n", c, c, c); //  -->  "2:34 am"
		System.out.format("%tD%n", c); //  -->  "05/29/06"
	}
	
	/*
	public void testSignificantDigits()
	{
		System.out.println("Value of " + 0.00013543 + " with 3 signficant digits = " + significantDigits( 0.00013543,3));
		System.out.println("Value of " + Math.PI + " with 3 signficant digits = " + significantDigits(Math.PI,3));
		System.out.println("Value of " + 12345678.0 + " with 2 signficant digits = " + significantDigits(12345678.0,2));
		System.out.println("Value of " + 412.0 + " with 2 signficant digits = " + significantDigits(412.0,2));
		System.out.println("Value of " + 10.0 + " with 2 signficant digits = " + significantDigits(10.0,2));
	}
	*/

	
	
}
