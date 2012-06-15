package com.redwood.utils;

import android.opengl.GLES20;
import android.util.Log;

public class ShaderUtil {
	public static void checkError(String op)
	{
		int error;
		while((error = GLES20.glGetError()) != GLES20.GL_NO_ERROR)
		{
			Log.e("GLES20 Error", op + " GLError: " + error);
			throw new RuntimeException(op + " GLError: " + error);
		}
	}
	
	//������ɫ��
	public static int loadShader(int shaderType, String shaderCode)
	{
		int shader = 0;
		shader = GLES20.glCreateShader(shaderType);
		if(shader != 0)
		{
			GLES20.glShaderSource(shader, shaderCode);
			GLES20.glCompileShader(shader);
			int[] result = new int[1];
			GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, result, 0);
			if(result[0] == 0)				//����ʧ��
			{
				Log.e("GLESError", "Shader Compile fail " + shaderType + " : " + GLES20.glGetShaderInfoLog(shader));
				GLES20.glDeleteShader(shader);
				shader = 0;
				result = null;
			}
		}
		
		return shader;
	}
	
	//������ɫ���������ɳ���
	public static int createProgramWithShader(int vertexShader, int fragmentShader)
	{
		if(vertexShader == 0 || fragmentShader == 0)	return 0;
		
		int program = 0;
		program = GLES20.glCreateProgram();
		if(program != 0)
		{
			//���붥����ɫ��
			GLES20.glAttachShader(program, vertexShader);
			checkError("attachVertexShader");
			//����ƬԪ��ɫ��
			GLES20.glAttachShader(program, fragmentShader);
			checkError("attachFragmentShader");
			
			//���ӳ���
			GLES20.glLinkProgram(program);
			int[] result = new int[1];
			GLES20.glGetProgramiv(program, GLES20.GL_LINK_STATUS, result, 0);
			if(result[0] != GLES20.GL_TRUE)
			{
				Log.e("GLESError", "Shader Link fail" + program + " : " + GLES20.glGetProgramInfoLog(program));
				GLES20.glDeleteProgram(program);
				program = 0;
				result = null;
			}
		}
		
		return program;
	}
	
	//���ɳ���
	public static int createProgram(String vCode, String fCode)
	{
		int program = 0;
		int vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vCode);
		if(vertexShader == 0)
		{
			return program;
		}
		
		int fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fCode);
		if(fragmentShader == 0)
		{
			return program;
		}
		
		program = GLES20.glCreateProgram();
		if(program != 0)
		{
			//���붥����ɫ��
			GLES20.glAttachShader(program, vertexShader);
			checkError("attachVertexShader");
			//����ƬԪ��ɫ��
			GLES20.glAttachShader(program, fragmentShader);
			checkError("attachFragmentShader");
			
			//���ӳ���
			GLES20.glLinkProgram(program);
			int[] result = new int[1];
			GLES20.glGetProgramiv(program, GLES20.GL_LINK_STATUS, result, 0);
			if(result[0] != GLES20.GL_TRUE)
			{
				Log.e("GLESError", "Shader Link fail" + program + " : " + GLES20.glGetProgramInfoLog(program));
				GLES20.glDeleteProgram(program);
				program = 0;
				result = null;
			}
		}
		
		return program;
	}
}
