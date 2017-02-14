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

import javax.media.opengl.GL;
import javax.media.opengl.GLAutoDrawable;
import javax.vecmath.Vector3f;

import org.eclipse.swt.widgets.Composite;

import com.geofx.opengl.view.GLScene;
import com.geofx.opengl.util.TextRenderer3D;

public class LightedCubeScene extends GLScene 
{

	TextRenderer3D	fo;
	
	public LightedCubeScene(Composite parent, java.awt.Frame glFrame)
	{
		super(parent, glFrame);

		System.out.println("LightedCubeScene - constructor");

		this.grip.setOffsets(0.0f, 0.0f, -3.0f);
		this.grip.setRotation(45.0f, -30.0f, 0.0f);
	}

	// a default constructor used by the ClassInfo enumerator
	public LightedCubeScene()
	{
		super();
	}


	public void init ( GLAutoDrawable drawable ) 
	{
		super.init(drawable);	
		
		System.out.println("LightedCubeScene - init");

		final GL gl = drawable.getGL();	

		System.out.println("OutlineTextScene - init");

		float[] 	LightDiffuse =	 { 1.0f, 1.0f, 1.0f, 1.0f };
		float[] 	LightAmbient =	 { 0.5f, 0.5f, 0.5f, 1.0f };
		float[] 	LightPosition =	 { 1.0f, 1.0f, 1.0f, 0.0f };
		float[]		mat_specular = { 1.0f, 1.0f, 1.0f, 1.0f };
		float[]     mat_shininess = { 50.0f };	
        
        gl.glLightfv(GL.GL_LIGHT0, GL.GL_DIFFUSE, LightAmbient, 0);						
        gl.glLightfv(GL.GL_LIGHT0, GL.GL_DIFFUSE, LightDiffuse, 0);				

		gl.glMaterialfv(GL.GL_FRONT, GL.GL_SPECULAR, mat_specular, 0);
		gl.glMaterialfv(GL.GL_FRONT, GL.GL_SHININESS, mat_shininess, 0);

		gl.glLightfv(GL.GL_LIGHT0, GL.GL_POSITION, LightPosition, 0);			

		gl.glEnable(GL.GL_DEPTH_TEST);
	    gl.glDepthFunc(GL.GL_LESS);    
	           
	    this.lightingEnabled = true;	
	              
	}

	public void display(GLAutoDrawable drawable) 
	{
		super.display(drawable);
	
		System.out.println("GLScene - display");
		GL gl = drawable.getGL();

		try
		{
			updateLighting(gl);
		
			drawAxes(gl);

			drawCube(gl);
           
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	private void drawCube(GL gl)
	{
		// Six faces of cube
		// Top face
		gl.glPushMatrix();
		gl.glRotatef(-90, 1, 0, 0);
		drawFace(gl, 1.0f, 0.2f, 0.2f, 0.8f, "Top");
		gl.glPopMatrix();
		
		// Front face
		drawFace(gl, 1.0f, 0.8f, 0.2f, 0.2f, "Front");
		
		// Right face
		gl.glPushMatrix();
		gl.glRotatef(90, 0, 1, 0);
		drawFace(gl, 1.0f, 0.2f, 0.8f, 0.2f, "Right");
		
		// Back face
		gl.glRotatef(90, 0, 1, 0);
		drawFace(gl, 1.0f, 0.8f, 0.8f, 0.2f, "Back");
		
		// Left face
		gl.glRotatef(90, 0, 1, 0);
		drawFace(gl, 1.0f, 0.2f, 0.8f, 0.8f, "Left");
		gl.glPopMatrix();
		
		// Bottom face
		gl.glPushMatrix();
		gl.glRotatef(90, 1, 0, 0);
		drawFace(gl, 1.0f, 0.8f, 0.2f, 0.8f, "Bottom");
		
		gl.glPopMatrix();
	}
	
	private void drawFace(GL gl, float faceSize, float r, float g, float b, String text)
	{
		float halfFaceSize = faceSize / 2;
		
		// Face is centered around the local coordinate system's z axis,
		// at a z depth of faceSize / 2
		

		gl.glColor3i( 0xff, 0xff, 0);
		
		Vector3f vecA = new Vector3f( halfFaceSize, 0.0f, 0.0f);
		Vector3f vecB = new Vector3f( 0.0f, halfFaceSize, 0.0f);
		Vector3f normal = new Vector3f();
		normal.cross( vecA, vecB );
		normal.normalize();
		
		/*
		gl.glBegin(GL.GL_LINE_STRIP);
		gl.glVertex3f(0.0f, 0.0f, 0.0f);
		gl.glVertex3f(0.0f, 0.0f, faceSize);
		gl.glEnd();
		*/
		
		gl.glNormal3f( normal.x, normal.y, normal.z ); 
		//gl.glNormal3f( 0.0f, 0.0f, faceSize);

		gl.glColor3f(r, g, b);
		gl.glBegin(GL.GL_QUADS);
		gl.glVertex3f(-halfFaceSize, -halfFaceSize, halfFaceSize);
		gl.glVertex3f(halfFaceSize, -halfFaceSize, halfFaceSize);
		gl.glVertex3f(halfFaceSize, halfFaceSize, halfFaceSize);
		gl.glVertex3f(-halfFaceSize, halfFaceSize, halfFaceSize);
		gl.glEnd();
	}
	
	public void  enableLighting( GL gl )
	{
	    gl.glEnable(GL.GL_LIGHT0);
	}
	
	public String getDescription()
	{
		return "A simple demo of a lighted cube";
	}

	public String getLabel()
	{
		return "Lighted Cube";
	}

}
