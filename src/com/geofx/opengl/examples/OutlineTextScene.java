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
import java.awt.geom.Rectangle2D;

import javax.media.opengl.GL;
import javax.media.opengl.GLAutoDrawable;

import org.eclipse.swt.widgets.Composite;

import com.geofx.opengl.view.GLScene;
import com.geofx.opengl.util.TextRenderer3D;

public class OutlineTextScene extends GLScene 
{
	public static final float AXIS_SIZE = 5.0f;

	TextRenderer3D	tr3;

	float[] 		LightDiffuse =	 { 1.0f, 1.0f, 1.0f, 1.0f };
	float[] 		LightAmbient =	 { 0.8f, 0.8f, 0.8f, 1.0f };
	float[] 		LightPosition =	 { 1.0f, 1.0f, 1.0f, 0.0f };
	float[]			mat_specular = { 1.0f, 1.0f, 1.0f, 1.0f };
	float[]			mat_ambient_magenta = { 1.0f, 0.0f, 1.0f, 1.0f };
	float[]     	mat_shininess = { 100.0f };

	private int compileIndex;	

	public OutlineTextScene(Composite parent, java.awt.Frame glFrame)
	{
		super(parent, glFrame);

		System.out.println("OutlineTextScene - constructor");

		this.grip.setOffsets(0.0f, 0.0f, -3.0f);
		this.grip.setRotation(45.0f, -30.0f, 0.0f);
	}

	// a default constructor used by the ClassInfo enumerator
	public OutlineTextScene()
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
		compileIndex = tr3.beginCompile();

		gl.glMaterialfv(GL.GL_FRONT, GL.GL_SPECULAR, mat_specular, 0);
		gl.glMaterialfv(GL.GL_FRONT, GL.GL_SHININESS, mat_shininess, 0);
	
		compileText();
		
	    this.lightingEnabled = true;	
	              
		initAnimator(drawable, 100);
	}

	private void compileText()
	{
		String str = "abcdef\r\nABCDEF";
		Rectangle2D rect = tr3.getBounds(str, 0.25f);
		
		float offX = (float) rect.getCenterX();
		float offY = (float) rect.getCenterY();
		float offZ = (float) (tr3.getDepth() / 2.0f);
		
		tr3.fill(str, -offX, offY, -offZ, 0.25f);
	}

	public void display(GLAutoDrawable drawable) 
	{
		super.display(drawable);
	
		System.out.println("OutlineTextScene - display");
		GL gl = drawable.getGL();

		try
		{
			updateLighting(gl);

			gl.glMaterialfv(GL.GL_FRONT, GL.GL_SPECULAR, mat_specular, 0);
			gl.glMaterialfv(GL.GL_FRONT, GL.GL_SHININESS, mat_shininess, 0);

			drawAxes(gl);
		
			gl.glMaterialfv(GL.GL_FRONT, GL.GL_AMBIENT_AND_DIFFUSE, mat_ambient_magenta, 0);
			tr3.call(compileIndex);
			           
			if (animator.isAnimating())
			{
				grip.adjustXRot(2.0f);
				grip.adjustYRot(1.0f);
				grip.adjustZRot(2.5f);
	    		fpsCounter.draw();
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}


	
	public void  enableLighting( GL gl )
	{
	    gl.glEnable(GL.GL_LIGHT0);
	}
	
	public String getDescription()
	{
		return "A simple demo of Outline Text using Java2D text";
	}

	public String getLabel()
	{
		return "Outline Text";
	}

}
