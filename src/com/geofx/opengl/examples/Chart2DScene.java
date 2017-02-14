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

import java.awt.Font;

import javax.media.opengl.GL;
import javax.media.opengl.GLAutoDrawable;
import javax.vecmath.Point2d;

import org.eclipse.swt.widgets.Composite;

import com.geofx.opengl.view.GLScene;
import com.geofx.opengl.util.CTM;
import com.geofx.opengl.util.TextRenderer3D;
import com.sun.opengl.util.GLUT;

public class Chart2DScene extends GLScene 
{
	public static final int MAX_ITEMS = 100;

	TextRenderer3D	tr3;
	
	float[] 		LightDiffuse =	 { 1.0f, 1.0f, 1.0f, 1.0f };
	float[] 		LightAmbient =	 { 0.8f, 0.8f, 0.8f, 1.0f };
	float[] 		LightPosition =	 { 1.8f, 1.8f, 1.8f, 0.0f };
	float[]			mat_specular = { 1.0f, 1.0f, 1.0f, 1.0f };
	float[]			mat_ambient_magenta = { 1.0f, 0.0f, 1.0f, 1.0f };
	float[]     	mat_shininess = { 100.0f };	

	CTM	ctm = new CTM();

	public Chart2DScene(Composite parent, java.awt.Frame glFrame)
	{
		super(parent, glFrame);

		System.out.println("Chart2DScene - constructor");

	//	this.grip.setOffsets(0.0f, 0.0f, 0.0f);
	//	this.grip.setRotation( 0.0f, 0.0f, 0.0f);
	//	this.grip.setOffsets(0, 0, -2f);
	//	this.grip.setRotation( 45.0f, -30.0f, 0.0f);

	}

	// a default constructor used by the ClassInfo enumerator
	public Chart2DScene()
	{
		super();
	}

	public void init ( GLAutoDrawable drawable ) 
	{
		super.init(drawable);	
		
		System.out.println("Chart2DScene - init");

		final GL gl = drawable.getGL();	

		gl.setSwapInterval(1);

        gl.glLightfv(GL.GL_LIGHT0, GL.GL_DIFFUSE, LightAmbient, 0);						
        gl.glLightfv(GL.GL_LIGHT0, GL.GL_DIFFUSE, LightDiffuse, 0);				

		gl.glLightfv(GL.GL_LIGHT0, GL.GL_POSITION, LightPosition, 0);			

		tr3 = new TextRenderer3D(new Font("Lucida Bright Italic", Font.TRUETYPE_FONT, 2), 0.0f);   

		gl.glMaterialfv(GL.GL_FRONT, GL.GL_SPECULAR, mat_specular, 0);
		gl.glMaterialfv(GL.GL_FRONT, GL.GL_SHININESS, mat_shininess, 0);
	
		setClearColor(0.1f, 0.1f, 0.1f, 0 );
		
	    this.lightingEnabled = true;	
	              
	}

	public void reshape( GLAutoDrawable drawable, int x, int y, int width, int height ) 
	{
		System.out.println("Chart2DScene - reshape");

		final GL gl = drawable.getGL();		
		
		gl.glMatrixMode(GL.GL_PROJECTION);
		gl.glLoadIdentity();
		
		// gl.glOrtho(0.0, 1.0, 0.0, 1.0, -2.0, 4.0);
		
		/*
		glu.gluPerspective( 15.0f, 
		        (double) width / (double) height, 
	            0.1f,
	            100.0f);
		*/
		gl.glFrustum(-1.4, 1.4, -1, 1, 1, 5);
		
		gl.glMatrixMode(GL.GL_MODELVIEW);
		gl.glLoadIdentity();
	}
	
	public void display(GLAutoDrawable drawable) 
	{
		GL gl = drawable.getGL();

		super.display(drawable);

		System.out.println("Chart2DScene - display");

		try
		{
			updateLighting(gl);
		
			drawAxes(gl);

			drawChart(gl);
		
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	private void drawChart(GL gl)
	{
		gl.glPushMatrix();
		
		setViewPort(gl, -0.9f, -0.9f, 0.9f, 0.9f, 0, 0, 10, 10);
		gl.glEnable( GL.GL_NORMALIZE);
	
		gl.glLineWidth(5.0f);

		gl.glMaterialfv(GL.GL_FRONT, GL.GL_AMBIENT_AND_DIFFUSE, randomColor(), 0);
		hollowRectangle(gl, 0, 0, 10, 10 );
		
		gl.glMaterialfv(GL.GL_FRONT, GL.GL_AMBIENT_AND_DIFFUSE, randomColor(), 0);
		
			 
		gl.glMaterialfv(GL.GL_FRONT, GL.GL_AMBIENT_AND_DIFFUSE, randomColor(), 0);
		circle(gl, 2, 6, 1);
		
				

		gl.glPopMatrix();
	}

	@SuppressWarnings("unused")
	private void drawTeapot(GL gl, float scale)
	{
		GLUT glut = new GLUT();	
		gl.glTranslatef(0.5f, 0.5f, 0.0f);
		glut.glutSolidTeapot(scale);
	}
	
	private void setViewPort ( GL gl, float xVMin, float yVMin, float xVMax, float yVMax,
			                          float orgX, float orgY, float xMax, float yMax )
	{
		float width = (xVMax-xVMin);
		float height = (yVMax - yVMin);
	//	gl.glTranslatef(xVMin, yVMin, 0);
	//	gl.glScalef(width/(xMax-orgX), height/(yMax-orgY),width/(xMax-orgX));
		

		ctm.setToIdentity();
		ctm.translate(xVMin, yVMin);
		ctm.scale(width/(xMax-orgX), height/(yMax-orgY));
		//ctm.invert();
	//	Point3d	pt = new Point3d(xMax, yMax, 0);
	//	ctm.transform(pt);
	}
	
	public void  enableLighting( GL gl )
	{
	    gl.glEnable(GL.GL_LIGHT0);
	}
	
	public String getDescription()
	{
		return "A 2D plotting erxample";
	}

	public String getLabel()
	{
		return "2D Plot";
	}

	// draw some striped-pole axes for visdual reference
	protected void drawAxes2D(GL gl)
	{
		float[]			mat_ambient_red = { 1.0f, 0.0f, 0.0f, 1.0f };
		float[]			mat_ambient_blue = { 0.0f, 0.0f, 1.0f, 1.0f };
		
		drawAxis2D(gl, mat_ambient_red);

		drawAxis2D(gl, mat_ambient_blue);

	}

	// draw a single striped pole axis
	private void drawAxis2D(GL gl, float[] material )
	{
		float[]			mat_ambient_grey = { 0.5f, 0.5f, 0.5f, 1.0f };
		final  double AXIS_RADIUS =	0.05;
		final  int	  AXIS_HEIGHT =	5;
		final  float  AXIS_STEP  =	0.25f;

		gl.glPushMatrix();
			
		gl.glTranslatef(0.0f, 0.0f, (float)-AXIS_HEIGHT/2.0f);
		
		float 	pos = -AXIS_HEIGHT/2.0f;
		int		i = 0;
		while ( pos < AXIS_HEIGHT/2.0f )
		{
			if ((i++ & 1)==0)
				gl.glMaterialfv(GL.GL_FRONT, GL.GL_AMBIENT_AND_DIFFUSE, material, 0);
			else
				gl.glMaterialfv(GL.GL_FRONT, GL.GL_AMBIENT_AND_DIFFUSE, mat_ambient_grey, 0);
				
			glu.gluCylinder(QUADRIC, AXIS_RADIUS, AXIS_RADIUS, AXIS_STEP, 8, 1);
			gl.glTranslatef(0.0f, 0.0f, AXIS_STEP);
			pos += AXIS_STEP;
		}
		
		gl.glPopMatrix();
	}
	
	 
	private void hollowRectangle(GL gl, float x1, float y1, float x2, float y2)
	{
		Point2d pt = new Point2d(x1, y1);
		ctm.transform(pt);
		pt.set(x2,y2);
		ctm.transform(pt);
		
		gl.glPushMatrix();
		
		gl.glTranslated(pt.x, pt.y, 0);
		gl.glRotatef(-90, 1, 0, 0);

		float[]			mat_ambient_blue = { 0.0f, 0.0f, 1.0f, 1.0f };
		gl.glMaterialfv(GL.GL_FRONT, GL.GL_AMBIENT_AND_DIFFUSE, mat_ambient_blue, 0);

		glu.gluCylinder(QUADRIC, 0.005f, 0.005f, y2-y1, 32, 5);
	
		gl.glPopMatrix();

		gl.glPushMatrix();
		
		//gl.glLoadIdentity();
		gl.glTranslated(pt.x, pt.y, 0);
		gl.glRotatef(-90, 1, 0, 0);

		gl.glMaterialfv(GL.GL_FRONT, GL.GL_AMBIENT_AND_DIFFUSE, mat_ambient_blue, 0);

		glu.gluCylinder(QUADRIC, 0.025f, 0.025f, y2-y1, 32, 1);
			
		gl.glPopMatrix();
		
		gl.glPushMatrix();
		
		//gl.glLoadIdentity();
		gl.glTranslatef(x1, y2, 0);
		gl.glRotatef(90, 0, 1, 0);

		gl.glMaterialfv(GL.GL_FRONT, GL.GL_AMBIENT_AND_DIFFUSE, mat_ambient_blue, 0);

		glu.gluCylinder(QUADRIC, 0.025f, 0.025f, y2-y1, 32, 1);
	
		
		gl.glPopMatrix();

		gl.glPushMatrix();
		
		//gl.glLoadIdentity();
		gl.glTranslatef(x1, y1, 0);
		gl.glRotatef(90, 0, 1, 0);

		gl.glMaterialfv(GL.GL_FRONT, GL.GL_AMBIENT_AND_DIFFUSE, mat_ambient_blue, 0);

		glu.gluCylinder(QUADRIC, 0.025f, 0.025f, y2-y1, 32, 1);
	
		
		gl.glPopMatrix();
}
	
		 
	private void circle (GL gl, float x1, float y1, float radius )
	{
		gl.glBegin(GL.GL_TRIANGLE_FAN);
		gl.glVertex2f(x1, y1+radius);
		for ( int angle = 0; angle<=360; angle += 5 )
		{
			gl.glVertex2d(x1 + Math.sin((double)angle * Math.PI/180.0) * radius, y1 + Math.cos((double)angle * Math.PI/180.0) * radius);
		}

		gl.glEnd();
	}
}
