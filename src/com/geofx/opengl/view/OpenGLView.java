/*******************************************************************************
 * Copyright (c) 2008 Ric Wright 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/org/documents/epl-v10.html
 * 
 * Contributors:
 *     Ric Wright - initial implementation
 *     Ric Wright - May 2008 - ported to JOGL
 *******************************************************************************/

package com.geofx.opengl.view;

import org.eclipse.swt.SWT;
import org.eclipse.swt.awt.SWT_AWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;

import com.geofx.opengl.view.GLScene;

/**
 * This sample class demonstrates how to plug-in a new workbench view. The view
 * shows data obtained from the model. The sample creates a dummy model on the
 * fly, but a real implementation would connect to the model available either in
 * this or another plug-in (e.g. the workspace). The view is connected to the
 * model using a content provider.
 * <p>
 * The view uses a label provider to define how model objects should be
 * presented in the view. Each view can present the same model objects using
 * different labels and icons, if needed. Alternatively, a single label provider
 * can be shared between views in order to ensure that objects of the same type
 * are presented in the same way everywhere.
 * <p>
 */

public class OpenGLView extends ViewPart 
{

	private GLScene 			scene;

	private Composite 			composite;
	private java.awt.Frame 		glFrame;
	private String 				sceneName;
	
	/**
	 * The constructor.
	 */
	public OpenGLView()
	{
		Activator.setGLView( this );
	}

	public void createPartControl( Composite parent )
	{		
		sceneName = Activator.getSceneName();

		System.setProperty("sun.awt.noerasebackground","true");
		composite = new Composite(parent, SWT.EMBEDDED |SWT.NO_BACKGROUND); 
		composite.setLayout( new FillLayout() );   
		
		glFrame = SWT_AWT.new_Frame(composite); 
		
		this.scene = Activator.constructScene(sceneName, composite, glFrame);
	}

	public void dispose()
	{
		System.out.println("OpenglView.dispose called");
		this.scene.dispose();
	}
	
	/**
	 * A public method to update the view.  
	 *
	 */
	public void updateView()
	{
		if (!sceneName.equals(Activator.getSceneName()))
		{
			sceneName = Activator.getSceneName();
			
			System.out.println("OpenGLView - disposing old scene: "+ this.scene.getLabel());
			this.scene.dispose();
			
			System.out.println("OpenGLView - constructing new scene: " + sceneName);
			this.scene = Activator.constructScene(sceneName, composite, glFrame);
			
			// we need to explicitly request the focus or we never get it
			this.glFrame.requestFocus();		
		}
		
		this.scene.render();
	}


	/**
	 * We don't need this but we have to implement it
	 */
	@Override
	public void setFocus() {}
}