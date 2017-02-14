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

import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.InputStream;
import java.io.StreamTokenizer;
import java.lang.reflect.Array;
import java.util.Vector;

import javax.media.opengl.GL;
import javax.media.opengl.GLAutoDrawable;

import org.eclipse.swt.widgets.Composite;

import com.geofx.map.util.Contour;
import com.geofx.map.util.ContourVector;
import com.geofx.opengl.view.GLScene;
import com.geofx.opengl.util.CTM;
import com.geofx.opengl.util.LineCap;
import com.geofx.opengl.util.LineJoin;
import com.geofx.opengl.util.PSGraphics;
import com.geofx.opengl.util.LineCap.CapType;
import com.geofx.opengl.util.LineJoin.JoinType;
import com.geofx.opengl.util.PSGraphics.HAlign;
import com.geofx.opengl.util.PSGraphics.VAlign;

public class ContourScene extends GLScene 
{
	public static final int 	MAX_ITEMS = 100;
	public static final String	fileName = "/resource/GFDATA4.DAT";

	float[] 		LightDiffuse =	 { 1.0f, 1.0f, 1.0f, 1.0f };
	float[] 		LightAmbient =	 { 0.3f, 0.3f, 0.3f, 1.0f };
	float[] 		LightPosition =	 { 10.8f, 10.8f, 10.8f, 0.0f };
	float[]			specular = { 1.0f, 1.0f, 1.0f, 1.0f };
	float[]			magenta = { 1.0f, 0.0f, 1.0f, 1.0f };
	float[]     	shininess = { 100.0f };	
	
	CTM	ctm = new CTM();

	PSGraphics			ps = null;
	private JoinType 	joinType = JoinType.Round;
	private CapType 	capType  = CapType.Round;
	private double		strokeWidth = 0.1;
	private double[][]	dashArray = { 
										{ 0.0 },
										{ 2.0 },
									   	{ 0.5, 0.2 },
									   	{ 0.1 }
									};
	
	private int 		dashIndex = 0;
	
	String[] fonts = {
						"Times New Roman",
						"Courier New",
						"Palatino Linotype Italic",
						"Verdana",
						"Monotype Corsiva"
					 };
	
	int	 fontIndex = 3;
	
	private VAlign 		valign = VAlign.BOTTOM;
	private HAlign 		halign = HAlign.LEFT;
	
	double[][]			array = null;
	
	Vector<ContourVector>	contourVectors;
	Contour					contour = new Contour();
	
	double 			contInterval = 100.0;

	public ContourScene(Composite parent, java.awt.Frame glFrame)
	{
		super(parent, glFrame);

		System.out.println("ContourScene - constructor");

		ps = new PSGraphics();
	
		this.grip.setOffsets(0, 0, -20f);
		//	this.grip.setRotation( 45.0f, -30.0f, 0.0f);

	}

	// a default constructor used by the ClassInfo enumerator
	public ContourScene()
	{
		super();
	}

	public void init ( GLAutoDrawable drawable ) 
	{
		super.init(drawable);	
		
		System.out.println("ContourScene - init");

		final GL gl = drawable.getGL();	

		gl.setSwapInterval(1);

        gl.glLightfv(GL.GL_LIGHT0, GL.GL_AMBIENT, LightAmbient, 0);						
        gl.glLightfv(GL.GL_LIGHT0, GL.GL_DIFFUSE, LightDiffuse, 0);				

		gl.glLightfv(GL.GL_LIGHT0, GL.GL_POSITION, LightPosition, 0);			

		gl.glMaterialfv(GL.GL_FRONT, GL.GL_SPECULAR, specular, 0);
		gl.glMaterialfv(GL.GL_FRONT, GL.GL_SHININESS, shininess, 0);
	
		setClearColor(0.1f, 0.1f, 0.1f, 0 );
		
	    this.lightingEnabled = true;	
	    
	    array = parseArrayFromFile();
		contInterval = 100.0;
		contourVectors = contour.ThreadContours(array, contInterval);
	    
	    initAnimator(drawable, 60);
   	}

	public void reshape( GLAutoDrawable drawable, int x, int y, int width, int height ) 
	{
		System.out.println("ContourScene - reshape");

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

		System.out.println("ContourScene - display");

		try
		{
			updateLighting(gl);
		
			drawAxes(gl);

			drawContours(gl);
			
			if (animator.isAnimating())
			{
	    		fpsCounter.draw();
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	private double[][] parseArrayFromFile()
	{
		StreamTokenizer st;
		double[][]	ray = null;

		try
		{
			FileInputStream stream = (FileInputStream) this.getClass().getResourceAsStream(fileName);

			BufferedReader br = new BufferedReader(new FileReader(stream.getFD()));
		    st = new StreamTokenizer(br);

			int cols = -1;
			int rows = -1;
			int i = 0;
			int j = 0;
			
		    while(st.nextToken() != StreamTokenizer.TT_EOF) 
		    {
		        String s;
		        switch(st.ttype) 
		        {
		          case StreamTokenizer.TT_EOL:
		            s = new String("EOL");
		            break;
		          case StreamTokenizer.TT_NUMBER:
		            s = Double.toString(st.nval);
		            if (rows == -1)
		            	rows = (int) Math.round(st.nval);
		            else if (cols == -1)
		            {
		            	cols = (int) Math.round(st.nval);
		            	
		            	ray = new double[rows][cols];
		            }
		            else
		            {
		            	ray[i][j++] = st.nval;
		            	if (j >= cols)
		            	{
		            		j= 0;
		            		i++;
		            	}
		            	
		            }
		            	
		            break;
		          case StreamTokenizer.TT_WORD:
		            s = st.sval; // Already a String
		            break;
		          default: // single character in ttype
		            s = String.valueOf((char)st.ttype);
		        }	
		    }
		    
			System.out.println("Parse complete");
		}
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
		}		
		catch (Exception e)
		{
			e.printStackTrace();
		}
		
		return ray;
	}

	private void drawContours( GL gl )
	{
		gl.glPushMatrix();

		setViewPort(gl, -1f, 0f, 0f, 1f, -1, 1, 1, 0);

		ps.setFlatness(0.001f);
		ps.setDepth(0.01);
		ps.setlinejoin(joinType);
		ps.setlinewidth(strokeWidth);
		ps.setlinecap(capType);
		ps.setdash(dashArray[dashIndex], 0.0);
		ps.setDepth(0.01f);
		ps.setrgbcolor(0xff0000);
				
		ps.pushmatrix();

		ctm.setToIdentity();
		ctm.scale(1/64.0, 1/64.0);
		ps.setmatrix(ctm);
		
		int ptCount = 0;
		
		for ( int i=0; i<contourVectors.size(); i++ )
		{
			ContourVector	vector = contourVectors.get(i);
			
			if (vector.strokeIndex == -1)
			{
				vector.strokeIndex = gl.glGenLists(1);
				
				gl.glNewList( vector.strokeIndex, GL.GL_COMPILE);
	
				ps.newpath();
				
				ps.moveto(vector.x.get(0), vector.y.get(0)); 
				//System.out.println(String.format("%2d:  %6.2f %6.2f", 0, vector.x.get(0), vector.y.get(0) ));

				ptCount += vector.x.size();
				
				for ( int k=1; k<vector.x.size(); k++ )
				{
					ps.lineto(vector.x.get(k), vector.y.get(k)); 
					//System.out.println(String.format("%2d:  %6.2f %6.2f", k, vector.x.get(k), vector.y.get(k) ));
				}
	
				ps.stroke();
				
				gl.glEndList();	

			}

			gl.glCallList( vector.strokeIndex );

		}
		
		ps.popmatrix();

		System.out.println(String.format("vectors: %4d, ptCount = %8d", contourVectors.size(), ptCount));
		
		gl.glPopMatrix();
	}


	/**
	 * Sets up the current model-view so that the specified viewport is scaled so that 
	 * it is mapped to the x,y values.
	 */
	private void setViewPort ( GL gl, float xVMin, float yVMin, float xVMax, float yVMax,
			                          float orgX, float orgY, float xMax, float yMax )
	{
		float width = (xVMax-xVMin);
		float height = (yVMax - yVMin);
	
		ctm.setToIdentity();
		ctm.translate(xVMin, yVMin);
		ctm.scale(width/(xMax-orgX), height/(yMax-orgY));
		ctm.translate(-orgX, -orgY);

		ps.setmatrix(ctm);

	}



	public void  enableLighting( GL gl )
	{
	    gl.glEnable(GL.GL_LIGHT0);
	}
	
	public String getDescription()
	{
		return "Threaded Contour Example";
	}

	public String getLabel()
	{
		return "Threaded Contour";
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

			case 'v':
				valign = PSGraphics.convertVAlign((valign .ordinal() + 1) % 3);
				break;

			case 'h':
				halign = PSGraphics.convertHAlign((halign .ordinal() + 1) % 3);
				break;

			case 'p':
				pathTestNumber = ++pathTestNumber % pathTestInfo.length;
				break;

			case 'f':
				fontIndex = ++fontIndex % fonts.length;
				break;

			case 'l':
				strokeWidth *= 2.0;
				if (strokeWidth > 1.0)
					strokeWidth = 0.025;
				ps.setlinewidth(strokeWidth);
				break;

			case 'd':
				dashIndex = ++dashIndex % Array.getLength(dashArray);
				break;

			default:
				handled = false;
		}
		
		return handled;
	}

	//=================== Test Code Section ===================================

	int		pathTestNumber = 15;
	private String[] pathTestInfo = {
									  "acute_angle.ps",
									  "charpath.ps",
									  "triangle.ps",
									  "triangle_nocp.ps",
									  "triangle5.ps",
									  "triangle5_nocp.ps",
									  "triangle_rev.ps",
									  "square.ps",
									  "square_nocp.ps",
									  "cross.ps",
									  "good_cross.ps",
									  "cross_nocp.ps",
									  "star.ps",
									  "moveto.ps",
									  "stub.ps",
									  "tcurve.ps"
									};

	
	@SuppressWarnings("unused")
	private void testPathStyles()
	{
	
	}
	


}
