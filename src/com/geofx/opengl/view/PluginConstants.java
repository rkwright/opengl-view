/*******************************************************************************
 * Copyright (c) 2008 Ric Wright 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/org/documents/epl-v10.html
 * 
 * Contributors:
 *     Ric Wright - initial implementation
 *******************************************************************************/

package com.geofx.opengl.view;

import org.eclipse.core.runtime.QualifiedName;

/**
 * A placeholder for the various constants needed throughout the plugin
 * project. Most of these are references to ids defined in the plugin.xml
 * file.
 */
public final class PluginConstants
{
	/**
	 * plugin id from plugin.xml
	 */
	public static final String			PLUGIN_ID				= "com.geofx.opengl.view";
	
	/**
	 * namespace URI for the properties
	 */
	public static final String			PROPERTY_NAMESPACE		= "http://www.geofx.com/opengl";
	
	/**
	 * property name for the scene's name
	 */
	public static final String			SCENENAME    			= "scenename";
	
	/**
	 * property name for the examples package
	 */
	public static final String			EXAMPLES_PACKAGE    	= "com.geofx.opengl.examples";

	/**
	 * property name for the default scene name
	 */
	public static final String			DEFAULT_SCENENAME    	= "com.geofx.opengl.examples.RotationScene";


	/**
	 * property qualified name for the scene name
	 */	
	public static final QualifiedName	SCENENAME_PROPERTY_NAME	= new QualifiedName(PROPERTY_NAMESPACE, SCENENAME);
	
	
}
