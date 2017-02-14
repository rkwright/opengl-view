/*******************************************************************************
 * Copyright (c) 2005 Bo Majewski 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/org/documents/epl-v10.html
 * 
 * Code from an article by Bo Majewski
 * http://www.eclipse.org/articles/Article-SWT-OpenGL/opengl.html
 * 
 * Contributors:
 *     Bo Majewski - initial API and implementation
 *     Ric Wright  - Ported to Eclipse 3.2 and native SWT OpenGL support
 *     				 Added support for double-buffering, better disposition
 *                   of resources
 *     Ric Wright - May 2008 - ported to JOGL
 *******************************************************************************/
package com.geofx.opengl.view;

import java.awt.Font;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.util.Random;

import javax.media.opengl.GL;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLCanvas;
import javax.media.opengl.GLCapabilities;
import javax.media.opengl.GLEventListener;
import javax.media.opengl.glu.GLU;
import javax.media.opengl.glu.GLUquadric;

import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;

import com.geofx.opengl.util.FPSCounter;
import com.geofx.opengl.util.StatusMessage;
import com.sun.opengl.util.Animator;
import com.sun.opengl.util.FPSAnimator;
import com.sun.opengl.util.texture.Texture;
import com.sun.opengl.util.texture.TextureIO;

/**
 * Creates an OpenGL scene. In order to draw on it, override the
 * <code>drawScene</code> method.
 * 
 * @author Bo Majewski and Ric Wright
 */
public abstract class GLScene implements GLEventListener
{
	protected GLCanvas 			canvas;
	protected GLCapabilities 	glCapabilities;
	private java.awt.Frame 		glFrame;

	protected SceneGrip 		grip;
	protected FPSAnimator		animator;
	protected FPSCounter		fpsCounter;
	protected StatusMessage		statusMessage;
	protected Random			random = new Random();

	protected int 				filter = 0; 			// Which Filter To Use

    protected boolean			lightingChanged = true;
    protected boolean			lightingEnabled = false;
    protected boolean			blendingChanged = true;
    protected boolean			blendingEnabled = false;
	protected final GLU 		glu = new GLU();

	protected GLUquadric QUADRIC = glu.gluNewQuadric();
	private int					axesIndex = 0;

	protected float[]			clearColor = { 0.0f, 0.0f, 0.0f, 0.0f };

	/**
	 * Creates a new scene owned by the specified parent component.
	 * 
	 * @param parent   The Composite which holds the actual GLCanvas
	 */
	public GLScene(Composite parent, java.awt.Frame frame )
	{
		//System.out.println("GLScene - constructor");

		glCapabilities = new GLCapabilities();
		
		//dumpCapabilities(glCapabilities);
		
		glCapabilities.setDoubleBuffered(true);
		glCapabilities.setHardwareAccelerated(true);
		glCapabilities.setNumSamples(4);
		glCapabilities.setSampleBuffers(true);

		//dumpCapabilities(glCapabilities);

		this.canvas = new GLCanvas( glCapabilities ); 
		
		this.canvas.addGLEventListener( this );
		
		this.glFrame = frame;
		
		this.glFrame.add( this.canvas );		
		
		Rectangle clientArea = parent.getClientArea();
		this.canvas.setSize(clientArea.width, clientArea.height);
		
		this.grip = new SceneGrip(this);

		// set some typical values as defaults
		this.grip.setOffsets(0,0,-5);
		this.grip.setRotation(0,0,0);		
	}

	private void dumpCapabilities(GLCapabilities glCap)
	{
		System.out.println("\nGL Capabilities \nAlpha Accum Bits: " + glCap.getAccumAlphaBits());
		System.out.println("Blue Accum Bits: " + glCap.getAccumBlueBits());
		System.out.println("Green Accum Bits: " + glCap.getAccumGreenBits());
		System.out.println("Red Accum Bits: " + glCap.getAccumRedBits());
		System.out.println("Alpha Bits: " + glCap.getAlphaBits());
		System.out.println("Blue Bits: " + glCap.getBlueBits());
		System.out.println("Red Bits: " + glCap.getRedBits());
		System.out.println("Green Bits: " + glCap.getGreenBits());
		System.out.println("SampleBuffers: " + glCap.getSampleBuffers());
		System.out.println("Num Samples: " + glCap.getNumSamples());
		System.out.println("Stencil Bits: " + glCap.getStencilBits());
		System.out.println("Double-BUffered: " + glCap.getDoubleBuffered());
		System.out.println("Hardware Accelerated: " + glCap.getHardwareAccelerated());
		System.out.println("PbufferFloatingPointBuffers: " + glCap.getPbufferFloatingPointBuffers());
		System.out.println("PbufferRenderToTexture: " + glCap.getPbufferRenderToTexture());
		System.out.println("PbufferRenderToTextureRectangle: " + glCap.getPbufferRenderToTextureRectangle());
		System.out.println("SampleBuffers: " + glCap.getSampleBuffers());
		System.out.println("Stereo: " + glCap.getStereo() + "\n");
	}

	// a default constructor used by the ClassInfo enumerator
	public GLScene()
	{		
	}

	protected void initAnimator( GLAutoDrawable drawable, int rate )
	{
		this.animator = new FPSAnimator( drawable, rate, true);
	}


	public abstract String getLabel();
	
	public abstract String getDescription();
	
	// -------------------------------------------------------------------------
	// GLEventListener implementations
	// -------------------------------------------------------------------------
	
	public void init( GLAutoDrawable drawable ) 
	{	
		//System.out.println("GLScene - init");

		final GL gl = drawable.getGL();
		gl.glShadeModel(GL.GL_SMOOTH);
		gl.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
		gl.glClearDepth(1.0f);
		gl.glEnable(GL.GL_DEPTH_TEST);
		gl.glDepthFunc(GL.GL_LESS);
		gl.glHint(GL.GL_PERSPECTIVE_CORRECTION_HINT, GL.GL_NICEST);
		
		gl.glEnable(GL.GL_BLEND);
		
		//gl.glEnable(GL.GL_LINE_SMOOTH);		
		//gl.glEnable(GL.GL_POLYGON_SMOOTH);
		//gl.glHint(GL.GL_POLYGON_SMOOTH_HINT, GL.GL_NICEST);
		
		gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);

	    gl.glEnable(GL.GL_TEXTURE_2D); 	
	    
	    fpsCounter = new FPSCounter(drawable,12);
		statusMessage = new StatusMessage(drawable,new Font("Verdana", Font.TRUETYPE_FONT, 12));	              
	}

	public void reshape( GLAutoDrawable drawable, int x, int y, int width, int height ) 
	{
		//System.out.println("GLScene - reshape");

		final GL gl = drawable.getGL();		
		final GLU glu = new GLU();
		
		gl.setSwapInterval(1);

		gl.glViewport(0, 0, width, height);
		gl.glMatrixMode(GL.GL_PROJECTION);
		gl.glLoadIdentity();

		glu.gluPerspective( 45.0f, 
					        (double) width / (double) height, 
				            0.1f,
				            100.0f);

		gl.glMatrixMode(GL.GL_MODELVIEW);
		gl.glLoadIdentity();
	}
	
	public void display(GLAutoDrawable drawable) 
	{
		///System.out.println("GLScene - display");
		
		GL gl = drawable.getGL();

		gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);
		gl.glClearColor(clearColor[0], clearColor[1], clearColor[2], clearColor[3]);
		gl.glLoadIdentity();
		
		this.grip.adjust(gl);	
	}
	
	public void displayChanged(GLAutoDrawable drawable, boolean modeChanged, boolean deviceChanged) 
	{
		System.out.println("GLScene - displayChanged");
	}

	public void render()
	{
		//System.out.println("GLScene - render");

		// tell the canvas to update itself
		this.canvas.display();
	}

	public void dispose()
	{
		// dispose(this.canvas.getGL());
		//System.out.println("GLScene - dispose");

		this.glFrame.remove(this.canvas );
		
		this.canvas.removeGLEventListener(this );
	}

	 /**
     * Loads an image from the application JAR with the given filename 
     * and then makes a texture out of it.  The texture is set to use
     * "linear" (smooth) filtering by default.
     * <p>
     * REMIND: Ideally, we would load these textures at application
     * startup time (on a separate thread).  Currently, we're doing this
     * on the Event Dispatch Thread, which means there will be a noticeable
     * lag when the window is first shown or when it is resized, as we load
     * the textures from disk.  I'll leave this as an exercise for the reader.
     * <p>
     * (Hint: use TextureIO.newTextureData() to load the textures from disk
     * on a separate thread at startup and keep them in memory.  Then when
     * DemoPanel.init3DResources() is called, pass each TextureData object to
     * TextureIO.newTexture(), which will cause the textures to be loaded
     * into video memory.)
     */
	protected Texture createTexture( Object obj, String filename, int filterParm, boolean mipmap )
	{
		Texture t = null;
		try
		{
			t = TextureIO.newTexture(obj.getClass().getClassLoader().getResource(filename), mipmap, null);
			t.setTexParameteri(GL.GL_TEXTURE_MIN_FILTER, filterParm);
			t.setTexParameteri(GL.GL_TEXTURE_MAG_FILTER, filterParm);
		}
		catch (IOException e)
		{
			System.err.println("Error loading " + filename);
		}
		catch (Exception e)
		{
			System.err.println("Exception while loading " + filename);
			e.printStackTrace();
		}
		return t;
	}
	
	public Animator getAnimator()
	{
		return animator;
	}

	public GLCanvas getCanvas()
	{
		return canvas;
	}

	public void  updateLighting( GL gl )
	{
    	// System.out.println("Lighting: " + (lightingChanged ? "Changed" : "Not Changed") );

		// toggle lighting
	    if (lightingChanged)
		{
	    	System.out.println("Lighting: " + (lightingEnabled ? "Enabling" : "Disabling") );
	    	
			if (lightingEnabled)
			{
				gl.glEnable(GL.GL_LIGHTING);
				enableLighting(gl);
			}
			else
				gl.glDisable(GL.GL_LIGHTING);
			
			lightingChanged = false;
		}
	}

	public void  updateBlending( GL gl )
	{
		// toggle blending
		if (blendingChanged)
		{
	    	//System.out.println("Blending: " + (blendingEnabled ? "Disabling" : "Enabling") );
			if (blendingEnabled)
			{
				gl.glEnable(GL.GL_BLEND); 		// Turn Blending On
				gl.glDisable(GL.GL_DEPTH_TEST); // Turn Depth Testing Off
			}
			else
			{
				gl.glDisable(GL.GL_BLEND); 		// Turn Blending Off
				gl.glEnable(GL.GL_DEPTH_TEST); 	// Turn Depth Testing On
			}
	
			blendingChanged = false;
		}
	}
	
	public void toggleBlending()
	{
		this.blendingEnabled = !this.blendingEnabled;
		blendingChanged = true;
	}
	
	public void toggleLighting()
	{
		this.lightingEnabled = !this.lightingEnabled;
		lightingChanged = true;
	}

	// default implementation does nothing.  Up to subclasses..
	public void  enableLighting( GL gl )
	{
	}

	public void toggleFilter()
	{
		// default implementation does nothing...		
	}
	
	// allows subclasses to get key events and do something interesting...
	protected boolean handleKeyEvent( KeyEvent e )
	{
		return false;
	}
	
	
	// draw some striped-pole axes for visual reference
	protected void drawAxes(GL gl)
	{
		float[]			mat_ambient_red = { 1.0f, 0.0f, 0.0f, 1.0f };
		float[]			mat_ambient_green = { 0.0f, 1.0f, 0.0f, 1.0f };
		float[]			mat_ambient_blue = { 0.0f, 0.0f, 1.0f, 1.0f };
		
		if (axesIndex == 0)
		{
			axesIndex = gl.glGenLists(1);

			gl.glNewList(axesIndex, GL.GL_COMPILE);

			drawAxis(gl, 0, mat_ambient_red);

			drawAxis(gl, 1, mat_ambient_green);

			drawAxis(gl, 2, mat_ambient_blue);

			gl.glEndList();	
		}
		
		gl.glCallList( axesIndex );
	}

	// draw a single striped pole axis
	private void drawAxis(GL gl, int rot, float[] material )
	{
		float[]			mat_ambient_grey = { 0.5f, 0.5f, 0.5f, 1.0f };
		float[]			mat_ambient_greyPos = { 1, 1, 1, 1.0f };
		final  double AXIS_RADIUS =	0.025;
		final  int	  AXIS_HEIGHT =	5;
		final  float  AXIS_STEP  =	0.10f;
		final  int    AXIS_SEGMENTS = 32;

		gl.glPushMatrix();
		
		if (rot == 1)
			gl.glRotatef(-90, 1, 0, 0);
		else if (rot == 0)
			gl.glRotatef(90, 0, 1, 0);
			
		gl.glTranslatef(0.0f, 0.0f, (float)-AXIS_HEIGHT/2.0f);
		
		float 	pos = -AXIS_HEIGHT/2.0f;
		int		i = 0;
		while ( pos < AXIS_HEIGHT/2.0f )
		{
			if ((i++ & 1)==0)
				gl.glMaterialfv(GL.GL_FRONT, GL.GL_AMBIENT_AND_DIFFUSE, material, 0);
			else
				if (pos <= 0)
					gl.glMaterialfv(GL.GL_FRONT, GL.GL_AMBIENT_AND_DIFFUSE, mat_ambient_grey, 0);
				else
					gl.glMaterialfv(GL.GL_FRONT, GL.GL_AMBIENT_AND_DIFFUSE, mat_ambient_greyPos, 0);
				
			glu.gluCylinder(QUADRIC, AXIS_RADIUS, AXIS_RADIUS, AXIS_STEP, AXIS_SEGMENTS, 1);
			
			// move to the new location
			gl.glTranslatef(0.0f, 0.0f, AXIS_STEP);
			pos += AXIS_STEP;
		}
		
		gl.glPopMatrix();
	}

	protected void setClearColor(float r, float g, float b, float a )
	{
		this.clearColor[0] = r;
		this.clearColor[1] = g;
		this.clearColor[2] = b;
		this.clearColor[3] = a;
	}
	
	protected float[] randomColor()
	{
		// Get a bright and saturated color
		float r = 0;
		float g = 0;
		float b = 0;
		// float s = 0;
		do
		{
			r = random.nextFloat() * 0.5f + 0.5f;
			g = random.nextFloat() * 0.5f + 0.5f;
			b = random.nextFloat() * 0.5f + 0.5f;

			// float[] hsb = Color.RGBtoHSB((int) (255.0f * r), (int) (255.0f * g), (int) (255.0f * b), null);
			// s = hsb[1];
		} 
		while ((r < 0.6f && g < 0.6f && b < 0.6f)); // || s < 0.8f);
		
		float[] material = new float[4];
		material[0] = r;
		material[1] = g;
		material[2] = b;
		material[3] = 1;
		
		return material;
	}
}

