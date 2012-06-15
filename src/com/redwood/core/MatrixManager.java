package com.redwood.core;

import java.util.Stack;

import android.opengl.GLES20;
import android.opengl.Matrix;

public class MatrixManager {
	public static float[] mMatrix = new float[16];
	
	private static float[] _pMatrix = new float[16];
	private static float[] _vMatrix = new float[16];
	private static float[] _cameraPos = new float[3];
	private static float _ratio;
	public static final int TYPE_FRUSTUM = 1;
	public static final int TYPE_ORTHO = 2;
	
	private static Stack<float[]> matrixStack = new Stack<float[]>();
	public static void pushModelMatrix()
	{
		if(matrixStack == null)			return;
		matrixStack.push(mMatrix.clone());
	}
	
	public static void popModelMatrix()
	{
		if(matrixStack == null)			return;
		mMatrix = null;
		mMatrix = matrixStack.pop();
	}
	
	public static void initModelMatrixStack()
	{
		if(matrixStack == null)
		{
			matrixStack = new Stack<float[]>();
		}
		matrixStack.clear();
	}
	
	public static void translateModelMatrix(float x, float y, float z)
	{
		Matrix.translateM(mMatrix, 0, x, y, z);
	}
	
	public static void scaleModelMatrix(float sX, float sY, float sZ)
	{
		Matrix.scaleM(mMatrix, 0, sX, sY, sZ);
	}
	
	public static void rotateModelMatrix(float xAngle, float yAngle, float zAngle)
	{
		Matrix.rotateM(mMatrix, 0, xAngle, 1, 0, 0);
		Matrix.rotateM(mMatrix, 0, yAngle, 0, 1, 0);
		Matrix.rotateM(mMatrix, 0, zAngle, 0, 0, 1);
	}
	
	public static void setView(int x, int y, int w, int h)
	{
		GLES20.glViewport(x, y, w, h);
		_ratio = (float) w / h;
	}
	
	public static void setFrustum(int type, float near, float far)
	{
		switch (type) {
		case TYPE_ORTHO:
			Matrix.orthoM(_pMatrix, 0, -_ratio, _ratio, -1f, 1f, near, far);
			break;

		default:
			Matrix.frustumM(_pMatrix, 0, -_ratio, _ratio, -1f, 1f, near, far);
			break;
		}
	}
	
	public static void setCamera(float cx, float cy, float cz, float tx, float ty, float tz, float upx, float upy, float upz)
	{
		Matrix.setLookAtM(_vMatrix, 0, cx, cy, cz, tx, ty, tz, upx, upy, upz);
	}
	
	public static float[] getCamera()
	{
		return _cameraPos;
	}
	
	public static float[] getFinalMatrix(float[] mMatrix)
	{
		float[] MVPMatrix = new float[16];
		Matrix.multiplyMM(MVPMatrix, 0, _vMatrix, 0, mMatrix, 0);
		Matrix.multiplyMM(MVPMatrix, 0, _pMatrix, 0, MVPMatrix, 0);
		return MVPMatrix;
	}
}
