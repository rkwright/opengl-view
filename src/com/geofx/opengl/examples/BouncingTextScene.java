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
import java.awt.Font;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Iterator;

import javax.media.opengl.GL;
import javax.media.opengl.GLAutoDrawable;
import javax.vecmath.Point3f;

import org.eclipse.swt.widgets.Composite;

import com.geofx.opengl.view.GLScene;
import com.geofx.opengl.util.TextRenderer3D;

public class BouncingTextScene extends GLScene 
{
	public static final int MAX_ITEMS = 100;

	TextRenderer3D	tr3;

	float[] 		LightDiffuse =	 { 1.0f, 1.0f, 1.0f, 1.0f };
	float[] 		LightAmbient =	 { 0.8f, 0.8f, 0.8f, 1.0f };
	float[] 		LightPosition =	 { 1.0f, 1.0f, 1.0f, 0.0f };
	float[]			mat_specular = { 1.0f, 1.0f, 1.0f, 1.0f };
	float[]			mat_ambient_magenta = { 1.0f, 0.0f, 1.0f, 1.0f };
	float[]     	mat_shininess = { 100.0f };	

	private ArrayList<TextInfo3D> textInfo = new ArrayList<TextInfo3D>();
	
	public BouncingTextScene(Composite parent, java.awt.Frame glFrame)
	{
		super(parent, glFrame);

		System.out.println("OutlineTextScene - constructor");

		this.grip.setOffsets(0.0f, 0.0f, -3.0f);
		this.grip.setRotation(45.0f, -30.0f, 0.0f);
	}

	// a default constructor used by the ClassInfo enumerator
	public BouncingTextScene()
	{
		super();
	}

	public void init ( GLAutoDrawable drawable ) 
	{
		super.init(drawable);	
		
		System.out.println("OutlineTextScene - init");

		final GL gl = drawable.getGL();	

		System.out.println("OutlineTextScene - init");
		     
        gl.glLightfv(GL.GL_LIGHT0, GL.GL_DIFFUSE, LightAmbient, 0);						
        gl.glLightfv(GL.GL_LIGHT0, GL.GL_DIFFUSE, LightDiffuse, 0);				

		gl.glLightfv(GL.GL_LIGHT0, GL.GL_POSITION, LightPosition, 0);			

		gl.glEnable(GL.GL_DEPTH_TEST);
	    gl.glDepthFunc(GL.GL_LESS);    

		tr3 = new TextRenderer3D(new Font("Lucida Bright Italic", Font.TRUETYPE_FONT, 2), 0.25f); 

		gl.glMaterialfv(GL.GL_FRONT, GL.GL_SPECULAR, mat_specular, 0);
		gl.glMaterialfv(GL.GL_FRONT, GL.GL_SHININESS, mat_shininess, 0);
	
		// compileText();
		
		// Create random text
		textInfo.clear();
		for (int i = 0; i < MAX_ITEMS; i++)
		{
			textInfo.add(randomTextInfo());
		}
		
	    this.lightingEnabled = true;	
	              
		initAnimator(drawable, 100);
	}

	public void display(GLAutoDrawable drawable) 
	{
		super.display(drawable);
	
		//System.out.println("OutlineTextScene - display");
		GL gl = drawable.getGL();

		try
		{
			updateLighting(gl);

			gl.glMaterialfv(GL.GL_FRONT, GL.GL_SPECULAR, mat_specular, 0);
			gl.glMaterialfv(GL.GL_FRONT, GL.GL_SHININESS, mat_shininess, 0);

			drawAxes(gl);
		
			// simpleString(gl);	
	        
			for (Iterator<TextInfo3D> iter = textInfo.iterator(); iter.hasNext();)
			{
				TextInfo3D info = (TextInfo3D) iter.next();
				
				updateTextInfo( info );
					
				gl.glPushAttrib(GL.GL_TRANSFORM_BIT);
				gl.glMatrixMode(GL.GL_MODELVIEW);
				gl.glPushMatrix();
				gl.glEnable( GL.GL_NORMALIZE);
				
				gl.glTranslatef(info.position.x, info.position.y, info.position.z);
				gl.glRotatef(info.angle.x, 1, 0, 0);
				gl.glRotatef(info.angle.y, 0, 1, 0);
				gl.glRotatef(info.angle.z, 0, 0, 1);
				
				// System.out.println(" x,y,z: " + info.position.x + " " + info.position.y + " " + info.position.z + " angle: " + info.angle );
			
				gl.glMaterialfv(GL.GL_FRONT, GL.GL_AMBIENT_AND_DIFFUSE, info.material, 0);

				tr3.call(info.index);
				
				gl.glPopMatrix();
				gl.glPopAttrib();	
			}
			           
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

	@SuppressWarnings("unused")
	private void simpleString(GL gl)
	{
		String str = "abcde";
		Rectangle2D rect = tr3.getBounds(str, 0.25f);
		
		float offX = (float) rect.getCenterX();
		float offY = (float) rect.getCenterY();
		float offZ = (float) (tr3.getDepth() / 2.0f);
					
		gl.glMaterialfv(GL.GL_FRONT, GL.GL_AMBIENT_AND_DIFFUSE, mat_ambient_magenta, 0);

		int index = tr3.beginCompile();
		
		tr3.fill(str, -offX, offY, -offZ, 0.25f);
		
		tr3.endCompile();
		
		tr3.call(index);
	}
	
	public void  enableLighting( GL gl )
	{
	    gl.glEnable(GL.GL_LIGHT0);
	}
	
	public String getDescription()
	{
		return "A simple demo of bouncing text";
	}

	public String getLabel()
	{
		return "Bouncing Text";
	}

	//------------------ Private Stuff below here ------------------------------
	private static final float 	INIT_ANG_VEL_MAG = 0.3f;
	private static final float 	INIT_VEL_MAG = 0.25f;
	private static final float 	MAX_BOUNDS = 1.5f;
	private static final float  SCALE_FACTOR = 0.05f;

	// Information about each piece of text
	private static class TextInfo3D
	{
		Point3f	angularVelocity;
		Point3f	velocity;
		Point3f	position;
		Point3f	angle;
		float 	h;
		float 	s;
		float 	v;
		int		index;		// display list index
		float 	curTime;	// Cycle the saturation
		float[] material = new float[4];

		// Cache of the RGB color
		float 	r;
		float 	g;
		float 	b;

		String 	text;
	}

	Point3f		tmp = new Point3f();
	
	private void updateTextInfo( TextInfo3D info )
	{
		// Update velocities and positions of all text
		float deltaT = 0.1f; 

		// Randomize things a little bit every little once in a while
		if (random.nextInt(10000) == 0)
		{
			info.angularVelocity = randomRotation(INIT_ANG_VEL_MAG, INIT_ANG_VEL_MAG, INIT_ANG_VEL_MAG);
			info.velocity = randomVelocity(INIT_VEL_MAG, INIT_VEL_MAG, INIT_VEL_MAG);
		}

		// Now update angles and positions
		tmp.set(info.angularVelocity);
		tmp.scale(deltaT*deltaT);
		info.angle.add(tmp);
		
		tmp.set(info.velocity);
		tmp.scale(deltaT);
		info.position.add(tmp);

		// Wrap angles and positions
		info.angle.x = clampAngle(info.angle.x);
		info.angle.y = clampAngle(info.angle.y);
		info.angle.z = clampAngle(info.angle.z);
	
		info.velocity.x = clampBounds(info.position.x, info.velocity.x );
		info.velocity.y = clampBounds(info.position.y, info.velocity.y );
		info.velocity.z = clampBounds(info.position.z, info.velocity.z );
	}

	private float clampBounds( float pos, float velocity )
	{
		if (pos < -MAX_BOUNDS || pos > MAX_BOUNDS)
		{
			velocity *= -1.0f;
		}
		
		return velocity;
	}
	
	private float clampAngle(float angle)
	{
		if (angle < 0)
		{
			angle += 360;
		}
		else if (angle > 360)
		{
			angle -= 360;
		}
		
		return angle;
	}
	
	private TextInfo3D randomTextInfo()
	{
		TextInfo3D info = new TextInfo3D();
		info.text = randomString();
		info.angle = randomRotation(INIT_ANG_VEL_MAG, INIT_ANG_VEL_MAG, INIT_ANG_VEL_MAG);
		info.position = randomVector(MAX_BOUNDS, MAX_BOUNDS, MAX_BOUNDS);
		
		Rectangle2D rect = tr3.getBounds(info.text, SCALE_FACTOR);

		float offX = (float) rect.getCenterX();
		float offY = (float) rect.getCenterY();
		float offZ = (float) (tr3.getDepth() / 2.0f);

		tr3.setDepth(random.nextFloat() * 0.9f + 0.1f);
		info.index = tr3.beginCompile();
			
		tr3.fill(info.text, -offX, offY, -offZ, SCALE_FACTOR);

		tr3.endCompile();
		
		info.angularVelocity = randomRotation(INIT_ANG_VEL_MAG, INIT_ANG_VEL_MAG, INIT_ANG_VEL_MAG);
		info.velocity = randomVelocity(INIT_VEL_MAG, INIT_VEL_MAG, INIT_VEL_MAG);

		Color c = randomColorX();
		c.getColorComponents(info.material);
		info.material[3] = random.nextFloat() * 0.9f + 0.1f;
	
		return info;
	}

	private String randomString()
	{
		switch (random.nextInt(3))
		{
		case 0:
			return "OpenGL";
		case 1:
			return "Java3D";
		default:
			return "JOGL";
		}
	}

	private Point3f randomVector(float x, float y, float z)
	{
		return new Point3f(x * random.nextFloat(), y * random.nextFloat(), z * random.nextFloat());
	}

	private Point3f randomVelocity(float x, float y, float z)
	{
		return new Point3f(x * (random.nextFloat() - 0.5f), y * (random.nextFloat() - 0.5f), z * (random.nextFloat() - 0.5f));
	}

	private Point3f randomRotation(float x, float y, float z)
	{
		return new Point3f(random.nextFloat() * 360.0f, random.nextFloat() * 360.0f, random.nextFloat() * 360.0f);
	}
	
	private Color randomColorX()
	{
		// Get a bright and saturated color
		float r = 0;
		float g = 0;
		float b = 0;
		float s = 0;
		do
		{
			r = random.nextFloat();
			g = random.nextFloat();
			b = random.nextFloat();

			float[] hsb = Color.RGBtoHSB((int) (255.0f * r), (int) (255.0f * g), (int) (255.0f * b), null);
			s = hsb[1];
		} 
		while ((r < 0.6f && g < 0.6f && b < 0.6f) || s < 0.8f);
		
		return new Color(r, g, b);
	}
}
