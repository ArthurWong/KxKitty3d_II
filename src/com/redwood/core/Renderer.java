package com.redwood.core;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;
import javax.microedition.khronos.opengles.GL11;

import com.redwood.Kitty3d;
import com.redwood.Shared;
import com.redwood.animation.AnimationObject3d;
import com.redwood.utils.FileUtil;
import com.redwood.utils.ShaderUtil;
import com.redwood.vos.FrustumManaged;
import com.redwood.vos.Light;
import com.redwood.vos.RenderType;
import com.redwood.vos.TextureVo;
import android.app.ActivityManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.GLU;
import android.opengl.GLUtils;
import android.opengl.Matrix;
import android.util.Log;


public class Renderer implements GLSurfaceView.Renderer
{
	public static final int NUM_GLLIGHTS = 8;

	private Scene _scene;
	private TextureManager _textureManager;
	
	// stats-related
	public static final int FRAMERATE_SAMPLEINTERVAL_MS = 1000; 
	private boolean _logFps = false;
	private long _frameCount = 0;
	private float _fps = 0;
	private long _timeLastSample;
	private ActivityManager _activityManager;
	private ActivityManager.MemoryInfo _memoryInfo;

	public String defVertexShaderSrc;
	public String defFragmentShaderSrc;
	public int defVertexShader;
	public int defFragmentShader;
	public int defProgram;
	
	public Renderer(Scene $scene)
	{
		_scene = $scene;
		
		_textureManager = new TextureManager();
		Shared.textureManager(_textureManager); 
		
		_activityManager = (ActivityManager) Shared.context().getSystemService( Context.ACTIVITY_SERVICE );
		_memoryInfo = new ActivityManager.MemoryInfo();
		
		initDefShader();
	}
	
	public void initDefShader()
	{
		//创建默认顶点着色器
		defVertexShaderSrc = FileUtil.loadFileAssets("simVertexShader.ss", Shared.context().getResources());
		defVertexShader = ShaderUtil.loadShader(GLES20.GL_VERTEX_SHADER, defVertexShaderSrc);
		//创建默认片元着色器
		defFragmentShaderSrc = FileUtil.loadFileAssets("simFragmentShader.ss", Shared.context().getResources());
		defFragmentShader = ShaderUtil.loadShader(GLES20.GL_FRAGMENT_SHADER, defFragmentShaderSrc);
		//创建默认着色器程序
		defProgram = ShaderUtil.createProgramWithShader(defVertexShader, defFragmentShader);
		if(defProgram == 0)
		{
			throw new Error("Shader Program Create Fail!!!");
		}
	}

	public void onSurfaceCreated(GL10 $gl, EGLConfig eglConfig) 
	{
		Log.i(Kitty3d.TAG, "Renderer.onSurfaceCreated()");
		
		RenderCaps.setRenderCaps();

		reset();
		
		_scene.init();
	}
	
	public void onSurfaceChanged(GL10 gl, int w, int h) 
	{
		Log.i(Kitty3d.TAG, "Renderer.onSurfaceChanged()");
		
		MatrixManager.setView(0, 0, w, h);
		
		updateViewFrustrum();
	}
	
	public void onDrawFrame(GL10 gl)
	{
		// Update 'model'
		_scene.update();
		
		// Update 'view'
		drawSetup();
		drawScene();

		if (_logFps) doFps();
	}
	

	/**
	 * Returns last sampled framerate (logFps must be set to true) 
	 */
	public float fps()
	{
		return _fps;
	}
	/**
	 * Return available system memory in bytes
	 */
	public long availMem()
	{
		_activityManager.getMemoryInfo(_memoryInfo);
		return _memoryInfo.availMem;
	}
	
	protected void drawSetup()
	{
		// View frustrum
		if (_scene.camera().frustum.isDirty()) {
			updateViewFrustrum();
			_scene.camera().frustum.clearDirtyFlag();
		}
		 
		// Camera 
		if(_scene.camera().position.isDirty())
		{
			MatrixManager.setCamera(_scene.camera().position.getX(), 
									_scene.camera().position.getY(), 
									_scene.camera().position.getZ(), 
									_scene.camera().target.x, 
									_scene.camera().target.y, 
									_scene.camera().target.z, 
									_scene.camera().upAxis.x,
									_scene.camera().upAxis.y, 
									_scene.camera().upAxis.z);
			_scene.camera().position.clearDirtyFlag();
		}
		
		// Background color
		if (_scene.backgroundColor().isDirty())
		{
			GLES20.glClearColor( 
				(float)_scene.backgroundColor().r() / 255f, 
				(float)_scene.backgroundColor().g() / 255f, 
				(float)_scene.backgroundColor().b() / 255f, 
				(float)_scene.backgroundColor().a() / 255f);
			_scene.backgroundColor().clearDirtyFlag();
		}
		
		GLES20.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);
	}

	protected void drawScene()
	{
		_scene.render();	
	}
	
	/**
	 * Used by TextureManager
	 */
	int uploadTextureAndReturnId(Bitmap $bitmap, boolean $generateMipMap) /*package-private*/
	{
		int glTextureId;
		
		int[] a = new int[1];
		GLES20.glGenTextures(1, a, 0); // create a 'texture name' and put it in array element 0
		glTextureId = a[0];
		GLES20.glBindTexture(GL10.GL_TEXTURE_2D, glTextureId);

		// 'upload' to gpu
		GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, $bitmap, 0);
		
		return glTextureId;
	}
	

	/**
	 * Used by TextureManager
	 */
	void deleteTexture(int $glTextureId) /*package-private*/
	{
		int[] a = new int[1];
		a[0] = $glTextureId;
		GLES20.glDeleteTextures(1, a, 0);
	}
	
	protected void updateViewFrustrum()
	{
		FrustumManaged vf = _scene.camera().frustum;
		MatrixManager.setFrustum(MatrixManager.TYPE_FRUSTUM, vf.zNear(), vf.zFar());
		vf.clearDirtyFlag();
	}

	/**
	 * If true, framerate and memory is periodically calculated and Log'ed,
	 * and gettable thru fps() 
	 */
	public void logFps(boolean $b)
	{
		_logFps = $b;
		
		if (_logFps) { // init
			_timeLastSample = System.currentTimeMillis();
			_frameCount = 0;
		}
	}
	
	private void doFps()
	{
		_frameCount++;

		long now = System.currentTimeMillis();
		long delta = now - _timeLastSample;
		if (delta >= FRAMERATE_SAMPLEINTERVAL_MS)
		{
			_fps = _frameCount / (delta/1000f); 

			_activityManager.getMemoryInfo(_memoryInfo);
			Log.v(Kitty3d.TAG, "FPS: " + Math.round(_fps) + ", availMem: " + Math.round(_memoryInfo.availMem/1048576) + "MB");

			_timeLastSample = now;
			_frameCount = 0;
		}
	}
	
	private void reset()
	{
		// Reset TextureManager
		Shared.textureManager().reset();

		// Do OpenGL settings which we are using as defaults, or which we will not be changing on-draw
		
	    // Explicit depth settings
		GLES20.glEnable(GLES20.GL_DEPTH_TEST);

		// Alpha enabled
		GLES20.glEnable(GLES20.GL_BLEND);
		GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);
		
		// "Transparency is best implemented using glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA) 
		// with primitives sorted from farthest to nearest."

		// Texture
		//_gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_NEAREST); // (OpenGL default is GL_NEAREST_MIPMAP)
		//_gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR); // (is OpenGL default)
		
		// CCW frontfaces only, by default
		GLES20.glFrontFace(GLES20.GL_CCW);
		GLES20.glCullFace(GLES20.GL_BACK);
		GLES20.glEnable(GLES20.GL_CULL_FACE);
	}
}
