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
import java.io.InputStream;
import java.lang.reflect.Array;
import java.util.ArrayList;

import javax.media.opengl.GL;
import javax.media.opengl.GLAutoDrawable;

import org.eclipse.swt.widgets.Composite;

import com.geofx.opengl.view.GLScene;
import com.geofx.opengl.util.CTM;
import com.geofx.opengl.util.Edge;
import com.geofx.opengl.util.LineCap;
import com.geofx.opengl.util.LineJoin;
import com.geofx.opengl.util.PSGraphics;
import com.geofx.opengl.util.PathElm;
import com.geofx.opengl.util.LineCap.CapType;
import com.geofx.opengl.util.LineJoin.JoinType;
import com.geofx.opengl.util.PSGraphics.HAlign;
import com.geofx.opengl.util.PSGraphics.VAlign;

public class StrokePathScene extends GLScene 
{
	public static final int 	MAX_ITEMS = 100;
	public static final String	fileName = "/resource/triangle_nocp.ps";

	float[] 		LightDiffuse =	 { 1.0f, 1.0f, 1.0f, 1.0f };
	float[] 		LightAmbient =	 { 0.3f, 0.3f, 0.3f, 1.0f };
	float[] 		LightPosition =	 { 10.8f, 10.8f, 10.8f, 0.0f };
	float[]			specular = { 1.0f, 1.0f, 1.0f, 1.0f };
	float[]			magenta = { 1.0f, 0.0f, 1.0f, 1.0f };
	float[]     	shininess = { 100.0f };	
	
	CTM	ctm = new CTM();

	PSGraphics			ps = null;
	private JoinType 	joinType = JoinType.Bevel;
	private CapType 	capType  = CapType.Butt;
	private double		strokeWidth = 0.05;
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
	

	public StrokePathScene(Composite parent, java.awt.Frame glFrame)
	{
		super(parent, glFrame);

		System.out.println("StrokePathScene - constructor");

		ps = new PSGraphics();
	
		this.grip.setOffsets(0, 0, -20f);
		//	this.grip.setRotation( 45.0f, -30.0f, 0.0f);

	}

	// a default constructor used by the ClassInfo enumerator
	public StrokePathScene()
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
	
		setClearColor(0.1f, 0.1f, 0.1f, 0 );
		
	    this.lightingEnabled = true;	
	    
	    initArcTestInfo();
	    
	    initAnimator(drawable, 200);
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

		System.out.println("StrokePathScene - display");

		try
		{
			updateLighting(gl);
		
			drawAxes(gl);

			drawPath(gl);
			
			//testEdge();
			
			//testAngle();
			
			//testStrokeShape();

			 //testBezierArc();	
			
			statusMessage.draw();

		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * First loads and then strokes the current path.
	 * 
	 * @param gl
	 */
	private void drawPath(GL gl)
	{
		String str = "path: " + pathTestInfo[pathTestNumber] + "  cap: " + capType.toString() + "  join: " + 
						joinType.toString() + " " + fonts[fontIndex];
		statusMessage.setMessage(str);

		gl.glPushMatrix();
		
		// setViewPort(gl, -1f, -1f, 1f, 1f, 0, 0, 10, 10);
		gl.glEnable( GL.GL_NORMALIZE);
	
		gl.glMaterialfv(GL.GL_FRONT, GL.GL_AMBIENT_AND_DIFFUSE, magenta, 0);
	
			// PSGraphics.printPath( "Path before stroking: ", path );
		
		ps.setlinejoin(joinType);
		ps.setlinewidth(strokeWidth);
		ps.setlinecap(capType);
		ps.setdash(dashArray[dashIndex], 0.0);
		ps.setDepth(0.4f);
		ps.setrgbcolor(0xff0000);

		// matrixTest();
		
		// drawCharPath(gl);

		// bezierTest();	
		
		// stringAlignTest();

		loadPath();
		
		gl.glPopMatrix();
	}

	private void loadPath()
	{
		String name = "/resource/" + pathTestInfo[pathTestNumber];
		InputStream stream = this.getClass().getResourceAsStream(name);
		
		if (ps.loadpath( stream ))
		{
			ps.stroke();
			
		}
	}

	private void drawCharPath( GL gl )
	{
		gl.glPushMatrix();

		setViewPort(gl, -1f, 0f, 0f, 1f, -20, 10, 20, 0);

		ps.setFlatness(0.001f);
		ps.setDepth(0.01);
		ps.newpath();
		
		ps.setrgbcolor(0x00ff00);
		ps.setfont(fonts[fontIndex]); 
		ps.scalefont(0.1);

		//ps.scale(2, 1);
		ps.setstringalign(halign, valign);

		ps.moveto(0.0,0.0); 
		
		//ps.charpath("IMWABC");
		//ps.stroke();
		
		ps.pushmatrix();

		ctm.setToIdentity();
		ps.setmatrix(ctm);
		
		ps.show("1");
		
		ps.popmatrix();

		
		gl.glPopMatrix();
	}

	@SuppressWarnings("unused")
	private void bezierTest()
	{
		ps.setFlatness(0.005f);
		
		ps.newpath();
		
		ps.moveto(0.0,0.0); 
		ps.curveto(-1.0, 1.0, 1.0, 1.0, -1.0, 0.0);

		//ps.arcn(0.0, 0.0, 1.0, 0.0, 360.0);
		
		ps.flattenpath();
	}
	
	@SuppressWarnings("unused")
	private void stringAlignTest()
	{
		ps.setfont(fonts[fontIndex]); 

		ps.newpath();
		ps.moveto(0.0, 0.0);
		ps.setstringalign(HAlign.RIGHT, VAlign.BOTTOM);
		ps.show("Shazaam!");
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

	/**
	 * Sets up the current model-view so that the specified viewport is scaled so that 
	 * it is mapped to the x,y values.
	 * 
	 * 4 1 scale

		0.1 setlinewidth
		
		0.0   0.0 moveto
		0.0   1.0 lineto
		0.5   1.0 lineto
		0.75    1.5 lineto
		1.75 1.5 lineto 
		1.75 2.5 lineto
	 */
	@SuppressWarnings("unused")
	private void matrixTest ()
	{
		ctm.setToIdentity();
		ctm.translate(0,0);
		ctm.scale(4,1);

		ps.setmatrix(ctm);
		
		ps.setlinewidth(0.1);
		
		ps.newpath();
		ps.moveto(0,0);
		ps.lineto(0,1);
		ps.lineto(0.5,1);
		ps.lineto(0.75,1.5);
		ps.lineto(1.75,1.5);
		ps.lineto(1.75,2.5);
		
		ps.stroke();
		

	}

	public void  enableLighting( GL gl )
	{
	    gl.glEnable(GL.GL_LIGHT0);
	}
	
	public String getDescription()
	{
		return "A 2D path-stroking example";
	}

	public String getLabel()
	{
		return "Stroke Path";
	}

	// allows subclasses to get key events and do something interesting...
	protected boolean handleKeyEvent( KeyEvent e )
	{
		boolean	handled = true;
		switch (e.getKeyChar())
	
		{
			case 'n':
				arcTestNumber = ++arcTestNumber % arcTestInfo.size();
				break;

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
	
	private class ArcTestInfo
	{
		double	angleS;
		double	angleF;
		boolean	clockwise;
		
		ArcTestInfo ( double angleS, double angleF, boolean clockwise )
		{
			this.angleS = angleS;
			this.angleF = angleF;
			this.clockwise = clockwise;
		}
	}

	int		arcTestNumber = 0;
	private ArrayList<ArcTestInfo> arcTestInfo = new ArrayList<ArcTestInfo>();

	private void initArcTestInfo()
	{
		arcTestInfo.add( new ArcTestInfo(0,270,true));
		arcTestInfo.add( new ArcTestInfo(0,180,true));
		arcTestInfo.add( new ArcTestInfo(0,90,true));
		arcTestInfo.add( new ArcTestInfo(0,80,true));
		arcTestInfo.add( new ArcTestInfo(0,360,false));
		arcTestInfo.add( new ArcTestInfo(0,360,true));
		arcTestInfo.add( new ArcTestInfo(360,0,true));
		arcTestInfo.add( new ArcTestInfo(360,0,false));
		arcTestInfo.add( new ArcTestInfo(45,135,true));
		arcTestInfo.add( new ArcTestInfo(45,135,false));
		arcTestInfo.add( new ArcTestInfo(30,60,false));
		arcTestInfo.add( new ArcTestInfo(30,60,true));
		arcTestInfo.add( new ArcTestInfo(60,30,false));
		arcTestInfo.add( new ArcTestInfo(60,30,true));
		arcTestInfo.add( new ArcTestInfo(230,260,false));
		arcTestInfo.add( new ArcTestInfo(230,260,true));
		arcTestInfo.add( new ArcTestInfo(145,180,false));
		arcTestInfo.add( new ArcTestInfo(145,180,true));
	}
	

	@SuppressWarnings("unused")
	private void testBezierArc()
	{
		ArcTestInfo info = arcTestInfo.get(arcTestNumber);
		
		double angleS = Math.toRadians(info.angleS);
		double angleF = Math.toRadians(info.angleF);
		
		String msg = "start: " + String.format("%6.1f",info.angleS) + "  finish: " + String.format("%6.1f",info.angleF) +
								" dir: " + (info.clockwise ? "clockwise" : " counter-clockwise");
		
	 	statusMessage.setMessage(msg);
		
		ps.newpath();
		
		ps.moveto(0,0);
		ps.lineto(Math.cos(angleS) * 1.0, -Math.sin(angleS) * 1.0);

		if (info.clockwise)
			ps.arcn( 0.0, 0.0, 1.0, angleS, angleF);
		else
			ps.arc( 0.0, 0.0, 1.0, angleS, angleF);
	
		//strokedPath.lineTo(cx, cy);
		ps.closepath();

		//ps.dumppath();

		ps.fill();
		
	}
	
	@SuppressWarnings("unused")
	private void testAngle()
	{
		PathElm p0 = new PathElm();
		PathElm p1 = new PathElm();
		PathElm p2 = new PathElm();
		
		System.out.println("Bends left 90 degrees");
		p0.x = 0.0f;
		p0.y = 0.0f;
		
		p1.x = 4.0f;
		p1.y = 4.0f;
		
		p2.x = 7.9999f;
	 	p2.y = 8.0f;

	
		Edge edge = new Edge(p0,p1);
		Edge edge1 = new Edge(p1,p2);
		
		System.out.println("Angle is " + edge.getIncludeAngle(edge1) + " and angle is " + (edge.isObtuse(edge1) ? "obtuse" : "convex"));
	}
	
	@SuppressWarnings("unused")
	private void testEdge()
	{
		PathElm p0 = new PathElm();
		PathElm p1 = new PathElm();
		PathElm p2 = new PathElm();
		PathElm p3 = new PathElm();
		
		System.out.println("Simple case, intersection at 5,5");
		p0.x = 0.0f;
		p0.y = 0.0f;
		
		p1.x = 10.0f;
		p1.y = 10.0f;
		
		p2.x = 10.0f;
		p2.y = 0.0f;

		p3.x = 0;
		p3.y = 10;
		
		Edge edge = new Edge(p0,p1);
		Edge edge2 = new Edge(p2,p3);
		
//	edge.intersection(p0,p1,p2,p3);
		
//	edge.LinesCross(p0.x,p0.y,p1.x,p1.y,p2.x,p2.y,p3.x,p3.y);

		PathElm interx = new PathElm();
		
		if (edge.intersect(edge2, interx))
			System.out.println("Intersection at " + interx.x + " " + interx.y);
		else
			System.out.println("Edges do not intersect");

		System.out.println("Nearly parallel, but don't quite intersect");
		p0.x = 0.0f;
		p0.y = 0.0f;
		
		p1.x = 5.0f;
		p1.y = 0.0f;
		
		p2.x = 10.0f;
		p2.y = 0.0f;

		p3.x = 5f;
		p3.y = 0.001f;

		edge.setP0(p0);
		edge.setP1(p1);
		
		edge2.setP0(p2);
		edge2.setP1(p3);
		
		if (edge.intersect(edge2, interx))
			System.out.println("Intersection at " + interx.x + " " + interx.y);
		else
			System.out.println("Edges do not intersect");

		System.out.println("Very tips intersect at 5,5");
		p3.y = 0.0f;

		edge2.setP1(p3);

		if (edge.intersect(edge2, interx))
			System.out.println("Intersection at " + interx.x + " " + interx.y);
		else
			System.out.println("Edges do not intersect");

		System.out.println("Overlap from 4,0 to 5,0");
		p3.x = 4.0f;
		p3.y = 0.0f;

		edge2.setP1(p3);
				
		if (edge.intersect(edge2, interx))
			System.out.println("Intersection at " + interx.x + " " + interx.y);
		else
			System.out.println("Edges do not intersect");

		System.out.println("colinear but don't overlap");
		p3.x = 6.0f;
		p3.y = 0.0f;

		edge2.setP1(p3);
				
		if (edge.intersect(edge2, interx))
			System.out.println("Intersection at " + interx.x + " " + interx.y);
		else
			System.out.println("Edges do not intersect");

		System.out.println("truly parallel - don't intersect");
		p3.x = 4.0f;
		p3.y = 1.0f;

		p2.y = 1.0f;

		edge2.setP1(p2);
		edge2.setP1(p3);

						
		if (edge.intersect(edge2, interx))
			System.out.println("Intersection at " + interx.x + " " + interx.y);
		else
			System.out.println("Edges do not intersect");


		System.out.println("One edge is just a point");
		p2.x = 5.0f;
		p2.y = 0.0f;
		
		p3.x = 5.0f;
		p3.y = 0.0f;

		edge2.setP1(p2);
		edge2.setP1(p3);
				
		if (edge2.intersect(edge, interx))
			System.out.println("Intersection at " + interx.x + " " + interx.y);
		else
			System.out.println("Edges do not intersect");

		//boolean flag = isObtuse(p0, p1, p2);
	}

}
