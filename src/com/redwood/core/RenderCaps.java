package com.redwood.core;

import java.nio.IntBuffer;

import javax.microedition.khronos.opengles.GL10;
import javax.microedition.khronos.opengles.GL11;

import com.redwood.Kitty3d;

import android.opengl.GLES20;
import android.util.Log;

/**
 * Simple static class holding values representing various capabilities of 
 * hardware's concrete OpenGL capabilities that are relevant to min3d's 
 * supported features. 
 */
public class RenderCaps 
{
	private static float _openGlVersion;
	private static int _maxTextureUnits;
	private static int _maxTextureSize;
	private static int _aliasedPointSizeMin;
	private static int _aliasedPointSizeMax;
	private static int _aliasedLineSizeMin;
	private static int _aliasedLineSizeMax;
	private static int _maxLights;
	
	
	public static float openGlVersion()
	{
		return _openGlVersion;
	}

	public static int maxTextureUnits()
	{
		return _maxTextureUnits;
	}
	
	public static int aliasedPointSizeMin()
	{
		return _aliasedPointSizeMin;
	}
	
	public static int aliasedPointSizeMax()
	{
		return _aliasedPointSizeMax;
	}
	
	public static int aliasedLineSizeMin()
	{
		return _aliasedLineSizeMin;
	}
	
	public static int aliasedLineSizeMax()
	{
		return _aliasedLineSizeMax;
	}
	
	/**
	 * Called by Renderer.onSurfaceCreate() 
	 */
	static void setRenderCaps() /* package-private*/
	{
	    IntBuffer i;

	    // OpenGL ES version
		
		i = IntBuffer.allocate(1);
		GLES20.glGetIntegerv(GLES20.GL_MAX_VERTEX_ATTRIBS, i);
		int maxVertexAttribs = i.get(0);
		
		i = IntBuffer.allocate(1);
		GLES20.glGetIntegerv(GLES20.GL_MAX_VERTEX_UNIFORM_VECTORS, i);
		int maxVertexUniformVectors = i.get(0);
		
		i = IntBuffer.allocate(1);
		GLES20.glGetIntegerv(GLES20.GL_MAX_VARYING_VECTORS, i);
		int maxVaryingVectors = i.get(0);
		
		i = IntBuffer.allocate(1);
		GLES20.glGetIntegerv(GLES20.GL_MAX_COMBINED_TEXTURE_IMAGE_UNITS, i);
		int maxCombinedTextureImageUnits = i.get(0);
		
		i = IntBuffer.allocate(1);
		GLES20.glGetIntegerv(GLES20.GL_MAX_VERTEX_TEXTURE_IMAGE_UNITS, i);
		int maxVertexTextureImageUnits = i.get(0);
		
	    // Max texture units
		i = IntBuffer.allocate(1);
		GLES20.glGetIntegerv(GLES20.GL_MAX_TEXTURE_IMAGE_UNITS, i);
		_maxTextureUnits = i.get(0);
		
	    // Max texture size
		i = IntBuffer.allocate(1);
		GLES20.glGetIntegerv(GLES20.GL_MAX_TEXTURE_SIZE, i);
		_maxTextureSize = i.get(0);
		
		// Aliased point size range
		i = IntBuffer.allocate(2);
		GLES20.glGetIntegerv(GLES20.GL_ALIASED_POINT_SIZE_RANGE, i);
		_aliasedPointSizeMin = i.get(0);
		_aliasedPointSizeMax = i.get(1);

		// Aliased line width range
		i = IntBuffer.allocate(2);
		GLES20.glGetIntegerv(GLES20.GL_ALIASED_LINE_WIDTH_RANGE, i);
		_aliasedLineSizeMin = i.get(0);
		_aliasedLineSizeMax = i.get(1);

		Log.v(Kitty3d.TAG, "RenderCaps - openGLVersion: " + _openGlVersion);
		Log.v(Kitty3d.TAG, "RenderCaps - maxTextureUnits: " + _maxTextureUnits);
		Log.v(Kitty3d.TAG, "RenderCaps - maxTextureSize: " + _maxTextureSize);
	}
}
