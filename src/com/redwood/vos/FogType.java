package com.redwood.vos;

import javax.microedition.khronos.opengles.GL10;

import android.opengl.GLES20;

public enum FogType {
	LINEAR (GLES20.GL_LINEAR);
	
	private final int _glValue;
	
	private FogType(int $glValue)
	{
		_glValue = $glValue;
	}
	
	public int glValue()
	{
		return _glValue;
	}
}
