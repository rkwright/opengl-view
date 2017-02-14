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
 *                   Fixed some minor bugs.
 *                 - Added support for rotation in Z-plane and incremntal update
 *                   on mouse drag
 *     Ric Wright - May 2008 - Ported to JOGL
 *******************************************************************************/
package com.geofx.opengl.view;

import java.awt.Dimension;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

import javax.media.opengl.GL;


/**
 * Implements a scene grip, capable of rotating and moving a GL scene with the
 * help of the mouse and keyboard.
 * 
 * @author Bo Majewski
 */
public class SceneGrip implements KeyListener, MouseListener, MouseMotionListener, MouseWheelListener 
{
	private static final int 	MIN_MOUSE_MOVE	= 10;
	private static final float	ROT_INCR = 1.0f;
	private static final float	SHIFT_INCR = 0.1f;
	
	private GLScene 	scene;
	private float 		xrot;
	private float 		yrot;
	private float 		zrot;
	private float 		zoff;
	private float 		xoff;
	private float 		yoff;
	private float 		xcpy;
	private float 		ycpy;
	private boolean 	move;
	private int 		xdown;
	private int 		ydown;
	private int 		mouseDown;
	private boolean 	wasAnimating = false;

	public SceneGrip(GLScene scene)
	{
		this.scene = scene;

		scene.getCanvas().addKeyListener(this);
		scene.getCanvas().addMouseListener(this);
		scene.getCanvas().addMouseMotionListener(this);
		scene.getCanvas().addMouseWheelListener(this);

		this.init();
	}

	protected void init()
	{
		this.xrot = this.yrot = this.zrot = 0.0f;
		this.xoff = this.yoff = 0.0f;
		this.zoff = -20.0f;
	}

	public void dispose( GL gl )
	{
		//System.out.println("SceneGrip dispose");
		scene.getCanvas().removeMouseListener(this);
		scene.getCanvas().removeMouseMotionListener(this);
		scene.getCanvas().removeMouseWheelListener(this);
		scene.getCanvas().removeKeyListener(this);
	}

	public void keyPressed( KeyEvent e)
	{
		//System.out.println("SceneGrip.Key event: " + e + " keycode: " + Integer.toHexString(e.getKeyCode()) 
		//		+ " state: " + Integer.toHexString(e.getModifiers()));
		
		boolean bRender = true;
		
		if (scene.handleKeyEvent(e))
		{
			scene.render();
			return;
		}
		
		switch (e.getKeyCode())
		{
		case KeyEvent.VK_UP:
			if (e.isControlDown())
			{
				this.yrot -= ROT_INCR;
			}
			else
			{
				this.yoff += SHIFT_INCR;
			}
			break;
		case KeyEvent.VK_DOWN:
			if (e.isControlDown())
			{
				this.yrot += ROT_INCR;
			}
			else
			{
				this.yoff -= SHIFT_INCR;
			}
			break;
		case KeyEvent.VK_LEFT:
			if (e.isControlDown())
			{
				this.xrot -= ROT_INCR;
			}
			else
			{
				this.xoff -= SHIFT_INCR;
			}
			break;
		case KeyEvent.VK_RIGHT:
			if (e.isControlDown())
			{
				this.xrot += ROT_INCR;
			}
			else
			{
				this.xoff += SHIFT_INCR;
			}
			break;
		case KeyEvent.VK_PAGE_UP:
			if (e.isControlDown())
			{
				this.zrot -= ROT_INCR;
			}
			else
			{
				this.zoff += SHIFT_INCR;
			}
			break;
		case KeyEvent.VK_PAGE_DOWN:
			if (e.isControlDown())
			{
				this.zrot += ROT_INCR;
			}
			else
			{
				this.zoff -= SHIFT_INCR;
			}
			break;
		case KeyEvent.VK_HOME:
			this.init();
			break;
		default:
			{
				switch (e.getKeyChar())
				{
				case 'a':
					if (this.scene.animator != null)
					{
						if (this.scene.animator.isAnimating())
							this.scene.animator.stop();
						else
							this.scene.animator.start();
					}
					break;
	
				case 'b':
					this.scene.toggleBlending();
					break;

				case 'f':
					this.scene.toggleFilter();
					break;
	
				case 'l':
					this.scene.toggleLighting();
					break;
	
				default:
					bRender = false;
					return;
				}
			}
		}
		
		if (bRender)
		{
			scene.render();
		}
		
	}

	public void keyReleased(java.awt.event.KeyEvent arg0)
	{
		// TODO Auto-generated method stub
		
	}

	public void keyTyped(java.awt.event.KeyEvent arg0)
	{
		// TODO Auto-generated method stub
		
	}

	public void mouseClicked(java.awt.event.MouseEvent arg0)
	{
		// TODO Auto-generated method stub
		
	}

	public void mouseEntered(java.awt.event.MouseEvent e)
	{
		//System.out.println("mouse entered" + e);
		
	}

	public void mouseExited(java.awt.event.MouseEvent arg0)
	{
		// TODO Auto-generated method stub
		
	}

	public void mousePressed(java.awt.event.MouseEvent e)
	{
		// if we're animating, stop while we drag
		if (scene.animator != null && scene.animator.isAnimating())
		{
			wasAnimating  = true;
			scene.animator.stop();
		}
		
		//System.out.println("start of pressed - mousedown " + this.mouseDown);
				
		if (++this.mouseDown > 0)
		{	
			this.mouseDown = 1;
			if ((this.move = e.getButton() == java.awt.event.MouseEvent.BUTTON1)) 
			{
				this.xcpy = xoff;
				this.ycpy = yoff;
				scene.getCanvas().setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.MOVE_CURSOR));
			}
			else
			{
				this.xcpy = xrot;
				this.ycpy = yrot;
				scene.getCanvas().setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.HAND_CURSOR));
			}

			this.xdown = e.getX();
			this.ydown = e.getY();
		}
	
		//System.out.println("pressed - mousedown " + this.mouseDown);
	}

	public void mouseReleased(java.awt.event.MouseEvent e)
	{
		//System.out.println("released - mousedown " + this.mouseDown );
		if (--this.mouseDown < 1)
		{
			this.mouseDown = 0;
			scene.getCanvas().setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.DEFAULT_CURSOR));
			scene.render();
		}
		
		if (wasAnimating)
		{
			wasAnimating  = false;
			scene.animator.start();
		}

	}

	public void mouseDragged(java.awt.event.MouseEvent e)
	{
		//System.out.println("dragged - mousedown " + this.mouseDown + " x,y: " + e.getX() + " " + e.getY() );
		
		Dimension p = scene.getCanvas().getSize();

		// System.out.println("moved - mousedown " + this.mouseDown);
		if (this.mouseDown > 0)
		{
			int dx = e.getX() - this.xdown;
			int dy = e.getY() - this.ydown;

			if (this.move)
			{
				yoff = this.ycpy + ((zoff + 1.0f) * dy) / (2.0f * (float)p.getHeight());
				xoff = this.xcpy - ((zoff + 1.0f) * dx) / (2.0f * (float)p.getWidth());
			}
			else
			{
				xrot = this.xcpy + dy / 2.0f;
				yrot = this.ycpy + dx / 2.0f;
			}
			
			if (Math.abs(dx) > MIN_MOUSE_MOVE || Math.abs(dy) > MIN_MOUSE_MOVE)
			{
				scene.render();
			}
		}
	}

	public void mouseMoved(java.awt.event.MouseEvent e) {}

	public void mouseWheelMoved(MouseWheelEvent arg0) {}

	//--------- geometry adjustments --------------------------------
	public void adjust( GL gl )
	{
		gl.glTranslatef(this.xoff, this.yoff, this.zoff);
		gl.glRotatef(this.xrot, 1.0f, 0.0f, 0.0f);
		gl.glRotatef(this.yrot, 0.0f, 1.0f, 0.0f);
		gl.glRotatef(this.zrot, 0.0f, 0.0f, 1.0f);
	}

	public void setOffsets(float x, float y, float z)
	{
		this.xoff = x;
		this.yoff = y;
		this.zoff = z;
	}

	public void setRotation(float x, float y, float z)
	{
		this.xrot = x;
		this.yrot = y;
		this.zrot = z;
	}

	public float getXRot()
	{
		return xrot;
	}

	public void setXRot(float xrot)
	{
		this.xrot = xrot;
	}

	public void adjustXRot(float dxrot)
	{
		this.xrot += dxrot;
	}

	public float getYRot()
	{
		return yrot;
	}

	public void setYRot(float yrot)
	{
		this.yrot = yrot;
	}

	public void adjustYRot(float dyrot)
	{
		this.yrot += dyrot;
	}

	public float getZRot()
	{
		return zrot;
	}

	public void setZRot(float zrot)
	{
		this.zrot = zrot;
	}

	public void adjustZRot(float dzrot)
	{
		this.zrot += dzrot;
	}

	public float getZOff()
	{
		return zoff;
	}

	public void setZOff(float zoff)
	{
		this.zoff = zoff;
	}

	public float getXOff()
	{
		return xoff;
	}

	public void setXOff(float xoff)
	{
		this.xoff = xoff;
	}

	public float getYOff()
	{
		return yoff;
	}

	public void setYOff(float yoff)
	{
		this.yoff = yoff;
	}


}
