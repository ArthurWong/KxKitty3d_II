package com.redwood.core;

import java.util.ArrayList;

import javax.microedition.khronos.opengles.GL10;

import android.opengl.GLES20;
import android.util.Log;

import com.redwood.Shared;
import com.redwood.Utils;
import com.redwood.interfaces.IObject3dContainer;
import com.redwood.vos.Color4;
import com.redwood.vos.Light;
import com.redwood.vos.Number3d;
import com.redwood.vos.RenderType;

/**
 * @author Lee
 */
public class Object3d
{
	private String _name;
	
	private RenderType _renderType = RenderType.TRIANGLES;
	
	private boolean _isVisible = true;
	private boolean _vertexColorsEnabled = true;
	private boolean _doubleSidedEnabled = false;
	private boolean _texturesEnabled = true;
	private boolean _normalsEnabled = true;
	private boolean _ignoreFaces = false;
	private boolean _colorMaterialEnabled = false;
	private boolean _lightingEnabled = true;

	private Number3d _position = new Number3d(0,0,0);
	private Number3d _rotation = new Number3d(0,0,0);
	private Number3d _scale = new Number3d(1,1,1);

	private Color4 _defaultColor = new Color4();
	
	private float _pointSize = 3f;
	private boolean _pointSmoothing = true;
	private float _lineWidth = 1f;
	private boolean _lineSmoothing = false;

	
	protected ArrayList<Object3d> _children;
	
	protected Vertices _vertices; 
	protected TextureList _textures;
	protected FacesBufferedList _faces;

	protected boolean _animationEnabled = false;
	
	private Scene _scene;
	private IObject3dContainer _parent;
	
	//shader变量
	protected String _vertexShaderSrc;
	protected String _fragmentShaderSrc;
	protected int _vertexShader;
	protected int _fragmentShader;
	protected int _program;
	
	protected int _uMVPMatrixHandle;		//变换矩阵数据索引
	
	//坐标数据
	protected int _aPositionHandle;			//位置
	
	protected int _uMMatrixHandle;			//变换矩阵
	
	//光照数据
	protected int _aNormalHandle;			//法向量
	
	protected int[] _uEnableLightHandle;	//标记光源是否可用
	protected int[] _uAmbientColorHandle;	//记录环境光强度
	protected int[] _uDiffuseColorHandle;	//记录散射光强度
	protected int[] _uSpecularColorHandle;	//记录反射光强度
	protected int[] _uLightPosHandle;		//定位光位置
	
	protected int _uCameraHandle;			//照相机
	
	//颜色纹理数据
	protected int _aColorHandle;			//颜色
	
	protected int _aTextureCoorHandle0;		//纹理坐标
	protected int _aTextureCoorHandle1;
	
	protected int[] _uEnableTextureHandle;	//标记使用哪个纹理数据

	/**
	 * Maximum number of vertices and faces must be specified at instantiation.
	 */
	public Object3d(int $maxVertices, int $maxFaces)
	{
		_vertices = new Vertices($maxVertices, true,true,true);
		_faces = new FacesBufferedList($maxFaces);
		_textures = new TextureList();
		
		initShader();
	}
	
	/**
	 * Adds three arguments 
	 */
	public Object3d(int $maxVertices, int $maxFaces, Boolean $useUvs, Boolean $useNormals, Boolean $useVertexColors)
	{
		_vertices = new Vertices($maxVertices, $useUvs,$useNormals,$useVertexColors);
		_faces = new FacesBufferedList($maxFaces);
		_textures = new TextureList();
		
		initShader();
	}
	
	/**
	 * This constructor is convenient for cloning purposes 
	 */
	public Object3d(Vertices $vertices, FacesBufferedList $faces, TextureList $textures)
	{
		_vertices = $vertices;
		_faces = $faces;
		_textures = $textures;
		
		initShader();
	}
	
	public void initShader()
	{
		_vertexShader = Shared.renderer().defVertexShader;
		_fragmentShader = Shared.renderer().defFragmentShader;
		_program = Shared.renderer().defProgram;
		
		//绑定uniform变量
		_uMVPMatrixHandle = GLES20.glGetUniformLocation(_program, "mvp_matrix");
		_uMMatrixHandle = GLES20.glGetUniformLocation(_program, "modelview_matrix");
		_uCameraHandle = GLES20.glGetUniformLocation(_program, "uCamera");
		_uEnableLightHandle = new int[8];
		int i = 0;
		for(i = 0; i < 8; i++)
		{
			_uEnableLightHandle[i] = -1;
			_uEnableLightHandle[i] = GLES20.glGetUniformLocation(_program, "light_enable_state["+ i + "]");
		}
		
		_uLightPosHandle = new int[8];
		for(i = 0; i < 8; i++)
		{
			_uLightPosHandle[i] = -1;
			_uLightPosHandle[i] = GLES20.glGetUniformLocation(_program, "light_state[" + i + "].position");
		}
		
		_uAmbientColorHandle = new int[8];
		for(i = 0; i < 8; i++)
		{
			_uAmbientColorHandle[i] = -1;
			_uAmbientColorHandle[i] = GLES20.glGetUniformLocation(_program, "light_state[" + i + "].ambient_color");
		}
		
		_uDiffuseColorHandle = new int[8];
		for(i = 0; i < 8; i++)
		{
			_uDiffuseColorHandle[i] = -1;
			_uDiffuseColorHandle[i] = GLES20.glGetUniformLocation(_program, "light_state[" + i + "].diffuse_color");
		}
		
		_uSpecularColorHandle = new int[8];
		for(i = 0; i < 8; i++)
		{
			_uSpecularColorHandle[i] = -1;
			_uSpecularColorHandle[i] = GLES20.glGetUniformLocation(_program, "light_state[" + i + "].specular_color");
		}
		
		_uEnableTextureHandle = new int[2];
		for(i = 0; i < 2; i++)
		{
			_uEnableTextureHandle[i] = -1;
			_uEnableTextureHandle[i] = GLES20.glGetUniformLocation(_program, "enable_tex[" + i + "]");
		}
		
		//绑定Attribute变量
		_aPositionHandle = GLES20.glGetAttribLocation(_program, "a_position");
		_aColorHandle = GLES20.glGetAttribLocation(_program, "a_color");
		_aNormalHandle = GLES20.glGetAttribLocation(_program, "a_normal");
		_aTextureCoorHandle0 = GLES20.glGetAttribLocation(_program, "a_texCoord0");
		_aTextureCoorHandle1 = GLES20.glGetAttribLocation(_program, "a_texCoord1");
	}
	
	/**
	 * Holds references to vertex position list, vertex u/v mappings list, vertex normals list, and vertex colors list
	 */
	public Vertices vertices()
	{
		return _vertices;
	}

	/**
	 * List of object's faces (ie, index buffer) 
	 */
	public FacesBufferedList faces()
	{
		return _faces;
	}
	
	public TextureList textures()
	{
		return _textures;
	}
	
	/**
	 * Determines if object will be rendered.
	 * Default is true. 
	 */
	public boolean isVisible()
	{
		return _isVisible;
	}
	public void isVisible(Boolean $b)
	{
		_isVisible = $b;
	}
	
	/**
	 * Determines if backfaces will be rendered (ie, doublesided = true).
	 * Default is false.
	 */
	public boolean doubleSidedEnabled()
	{
		return _doubleSidedEnabled;
	}
	public void doubleSidedEnabled(boolean $b)
	{
		_doubleSidedEnabled = $b;
	}
	
	/**
	 * Determines if object uses GL_COLOR_MATERIAL or not.
	 * Default is false.
	 */
	public boolean colorMaterialEnabled()
	{
		return _colorMaterialEnabled;
	}
	
	public void colorMaterialEnabled(boolean $b)
	{
		_colorMaterialEnabled = $b;
	}
	
	public boolean lightingEnabled() {
		return _lightingEnabled;
	}

	public void lightingEnabled(boolean $b) {
		this._lightingEnabled = $b;
	}

	/**
	 * Determines whether animation is enabled or not. If it is enabled
	 * then this should be an AnimationObject3d instance.
	 * This is part of the Object3d class so there's no need to cast
	 * anything during the render loop when it's not necessary.
	 */
	public boolean animationEnabled()
	{
		return _animationEnabled;
	}
	public void animationEnabled(boolean $b)
	{
		_animationEnabled = $b;
	}
	/**
	 * Determines if per-vertex colors will be using for rendering object.
	 * If false, defaultColor property will dictate object color.
	 * If object has no per-vertex color information, setting is ignored.
	 * Default is true. 
	 */
	public boolean vertexColorsEnabled()
	{
		return _vertexColorsEnabled;
	}
	public void vertexColorsEnabled(Boolean $b)
	{
		_vertexColorsEnabled = $b;
	}

	/**
	 * Determines if textures (if any) will used for rendering object.
	 * Default is true.  
	 */
	public boolean texturesEnabled()
	{
		return _texturesEnabled;
	}
	public void texturesEnabled(Boolean $b)
	{
		_texturesEnabled = $b;
	}
	
	/**
	 * Determines if object will be rendered using vertex light normals.
	 * If false, no lighting is used on object for rendering.
	 * Default is true.
	 */
	public boolean normalsEnabled()
	{
		return _normalsEnabled;
	}
	public void normalsEnabled(boolean $b)
	{
		_normalsEnabled = $b;
	}

	/**
	 * When true, Renderer draws using vertex points list, rather than faces list.
	 * (ie, using glDrawArrays instead of glDrawElements) 
	 * Default is false.
	 */
	public boolean ignoreFaces()
	{
		return _ignoreFaces;
	}
	public void ignoreFaces(boolean $b)
	{
		_ignoreFaces = $b;
	}	
	
	/**
	 * Options are: TRIANGLES, LINES, and POINTS
	 * Default is TRIANGLES.
	 */
	public RenderType renderType()
	{
		return _renderType;
	}
	public void renderType(RenderType $type)
	{
		_renderType = $type;
	}
	
	/**
	 * Convenience 'pass-thru' method  
	 */
	public Number3dBufferList points()
	{
		return _vertices.points();
	}
	
	/**
	 * Convenience 'pass-thru' method  
	 */
	public UvBufferList uvs()
	{
		return _vertices.uvs();
	}
	
	/**
	 * Convenience 'pass-thru' method  
	 */
	public Number3dBufferList normals()
	{
		return _vertices.normals();
	}
	
	/**
	 * Convenience 'pass-thru' method  
	 */
	public Color4BufferList colors()
	{
		return _vertices.colors();
	}
	
	/**
	 * Convenience 'pass-thru' method  
	 */
	public boolean hasUvs()
	{
		return _vertices.hasUvs();
	}

	/**
	 * Convenience 'pass-thru' method  
	 */
	public boolean hasNormals()
	{
		return _vertices.hasNormals();
	}
	
	/**
	 * Convenience 'pass-thru' method  
	 */
	public boolean hasVertexColors()
	{
		return _vertices.hasColors();
	}


	/**
	 * Clear object for garbage collection.
	 */
	public void clear()
	{
		if (this.vertices().points() != null) 	this.vertices().points().clear();
		if (this.vertices().uvs() != null) 		this.vertices().uvs().clear();
		if (this.vertices().normals() != null) 	this.vertices().normals().clear();
		if (this.vertices().colors() != null) 	this.vertices().colors().clear();
		if (_textures != null) 					_textures.clear();
		
		if (this.parent() != null) 				this.parent().removeChild(this);
	}

	//

	/**
	 * Color used to render object, but only when colorsEnabled is false.
	 */
	public Color4 defaultColor()
	{
		return _defaultColor;
	}
	
	public void defaultColor(Color4 color) {
		_defaultColor = color;
	}

	/**
	 * X/Y/Z position of object. 
	 */
	public Number3d position()
	{
		return _position;
	}
	
	/**
	 * X/Y/Z euler rotation of object, using Euler angles.
	 * Units should be in degrees, to match OpenGL usage. 
	 */
	public Number3d rotation()
	{
		return _rotation;
	}

	/**
	 * X/Y/Z scale of object.
	 */
	public Number3d scale()
	{
		return _scale;
	}
	
	/**
	 * Point size (applicable when renderType is POINT)
	 * Default is 3. 
	 */
	public float pointSize()
	{
		return _pointSize; 
	}
	public void pointSize(float $n)
	{
		_pointSize = $n;
	}

	/**
	 * Point smoothing (anti-aliasing), applicable when renderType is POINT.
	 * When true, points look like circles rather than squares.
	 * Default is true.
	 */
	public boolean pointSmoothing()
	{
		return _pointSmoothing;
	}
	public void pointSmoothing(boolean $b)
	{
		_pointSmoothing = $b;
	}

	/**
	 * Line width (applicable when renderType is LINE)
	 * Default is 1. 
	 * 
	 * Remember that maximum line width is OpenGL-implementation specific, and varies depending 
	 * on whether lineSmoothing is enabled or not. Eg, on Nexus One,  lineWidth can range from
	 * 1 to 8 without smoothing, and can only be 1f with smoothing. 
	 */
	public float lineWidth()
	{
		return _lineWidth;
	}
	public void lineWidth(float $n)
	{
		_lineWidth = $n;
	}
	
	/**
	 * Line smoothing (anti-aliasing), applicable when renderType is LINE
	 * Default is false.
	 */
	public boolean lineSmoothing()
	{
		return _lineSmoothing;
	}
	public void lineSmoothing(boolean $b)
	{
		_lineSmoothing = $b;
	}
	
	/**
	 * Convenience property 
	 */
	public String name()
	{
		return _name;
	}
	public void name(String $s)
	{
		_name = $s;
	}
	
	public IObject3dContainer parent()
	{
		return _parent;
	}
	
	//
	
	void parent(IObject3dContainer $container) /*package-private*/
	{
		_parent = $container;
	}
	
	/**
	 * Called by Scene
	 */
	void scene(Scene $scene) /*package-private*/
	{
		_scene = $scene;
	}
	/**
	 * Called by DisplayObjectContainer
	 */
	Scene scene() /*package-private*/
	{
		return _scene;
	}
	
	/**
	 * Can be overridden to create custom draw routines on a per-object basis, 
	 * rather than using Renderer's built-in draw routine. 
	 * 
	 * If overridden, return true instead of false.
	 */
	public Boolean customRenderer(GL10 gl)
	{
		return false;
	}
	
	//绘制自身
	public void renderMe()
	{
		if(!isVisible())		return;
		
		MatrixManager.pushModelMatrix();
		
		MatrixManager.translateModelMatrix(this.position().x, this.position().y, this.position().z);
		MatrixManager.rotateModelMatrix(this.rotation().x, this.rotation().y, this.rotation().z);
		MatrixManager.scaleModelMatrix(this.scale().x, this.scale().y, this.scale().z);
		
		doRender();
		
		MatrixManager.popModelMatrix();
	}
	
	public void doRender()
	{
		GLES20.glUseProgram(_program);
		GLES20.glUniformMatrix4fv(_uMVPMatrixHandle, 1, false, MatrixManager.getFinalMatrix(MatrixManager.mMatrix), 0);
		GLES20.glUniformMatrix4fv(_uMMatrixHandle, 1, false, MatrixManager.mMatrix, 0);
		GLES20.glUniform3fv(_uCameraHandle, 1, Utils.makeFloatBuffer3(MatrixManager.getCamera()[0], MatrixManager.getCamera()[1], MatrixManager.getCamera()[2]));
		
		//设置位置
		GLES20.glVertexAttribPointer(_aPositionHandle, 4, GLES20.GL_FLOAT, false, 4 * 4, vertices().points().buffer());
		GLES20.glEnableVertexAttribArray(_aPositionHandle);
		
		//设置颜色
		if(this.hasVertexColors() && this.vertexColorsEnabled())
		{
			vertices().colors().buffer().position(0);
			GLES20.glVertexAttribPointer(_aColorHandle, 4, GLES20.GL_UNSIGNED_BYTE, false, 4 * 4, vertices().colors().buffer());
			GLES20.glEnableVertexAttribArray(_aColorHandle);
		}
		else
		{
			GLES20.glDisableVertexAttribArray(_aColorHandle);
		}
		
		//设置法向量
		if (hasNormals() && normalsEnabled()) {
			vertices().normals().buffer().position(0);
			GLES20.glVertexAttribPointer(_aNormalHandle, 3, GLES20.GL_FLOAT, false, 3 * 4, vertices().normals().buffer());
			GLES20.glEnableVertexAttribArray(_aNormalHandle);
		}
		else {
			GLES20.glDisableVertexAttribArray(_aNormalHandle);
		}
		
		int[] enableTexture = {0, 0};
		//设置纹理坐标
		if(hasUvs() && texturesEnabled())
		{
			//有纹理
			if(textures().size() > 0)
			{
				//一个纹理
				if(textures().size() == 1)
				{
					vertices().uvs().buffer().position(0);
					GLES20.glVertexAttribPointer(_aTextureCoorHandle0, 2, GLES20.GL_FLOAT, false, 2 * 4, vertices().uvs().buffer());
					enableTexture[0] = 1;
				}
				//两个纹理
				else
				{
					vertices().uvs().buffer().position(0);
					GLES20.glVertexAttribPointer(_aTextureCoorHandle0, 2, GLES20.GL_FLOAT, false, 2 * 4, vertices().uvs().buffer());
					GLES20.glVertexAttribPointer(_aTextureCoorHandle1, 2, GLES20.GL_FLOAT, false, 2 * 4, vertices().uvs().buffer());
					enableTexture[0] = 1;		
					enableTexture[1] = 1;
				}
			}
			//没有纹理
			else
			{
				GLES20.glDisableVertexAttribArray(_aTextureCoorHandle0);
				GLES20.glDisableVertexAttribArray(_aTextureCoorHandle1);
			}
		}
		else
		{
			GLES20.glDisableVertexAttribArray(_aTextureCoorHandle0);
			GLES20.glDisableVertexAttribArray(_aTextureCoorHandle1);
		}
		GLES20.glUniform1i(_uEnableTextureHandle[0], enableTexture[0]);
		GLES20.glUniform1i(_uEnableTextureHandle[1], enableTexture[1]);
		
		//设置光源
		// GL_LIGHTS enabled/disabled based on enabledDirty list
		for (int glIndex = 0; glIndex < Renderer.NUM_GLLIGHTS; glIndex++)
		{
			if (_scene.lights().glIndexEnabledDirty()[glIndex] == true)
			{
				if (_scene.lights().glIndexEnabled()[glIndex] == true) 
				{
					GLES20.glUniform1i(_uEnableLightHandle[glIndex], 1);
					
					// make light's properties dirty to force update
					_scene.lights().getLightByGlIndex(glIndex).setAllDirty();
				} 
				else 
				{
					GLES20.glUniform1i(_uEnableLightHandle[glIndex], 0);
				}
				
				_scene.lights().glIndexEnabledDirty()[glIndex] = false; // clear dirtyflag
			}
		}
		
		// Lights' properties 

		Light[] lights = _scene.lights().toArray();
		for (int i = 0; i < lights.length; i++)
		{
			Light light = lights[i];
			
			if (light.isDirty()) // .. something has changed
			{
				// Check all of Light's properties for dirty 
				
				if (light.position.isDirty())
				{
					light.commitPositionAndTypeBuffer();
					GLES20.glUniform1fv(_uLightPosHandle[i], 1, light._positionAndTypeBuffer);
					light.position.clearDirtyFlag();
				}
				if (light.ambient.isDirty()) 
				{
					light.ambient.commitToFloatBuffer();
					GLES20.glUniform1fv(_uAmbientColorHandle[i], 1, light.ambient.floatBuffer());
					light.ambient.clearDirtyFlag();
				}
				if (light.diffuse.isDirty()) 
				{
					light.diffuse.commitToFloatBuffer();
					GLES20.glUniform1fv(_uDiffuseColorHandle[i], 1, light.diffuse.floatBuffer());
					light.diffuse.clearDirtyFlag();
				}
				if (light.specular.isDirty())
				{
					light.specular.commitToFloatBuffer();
					GLES20.glUniform1fv(_uSpecularColorHandle[i], 1, light.specular.floatBuffer());
					light.specular.clearDirtyFlag();
				}
				
				light.clearDirtyFlag();
			}
		}
		
		int len = faces().size();
		GLES20.glDrawElements(GLES20.GL_TRIANGLES, len * FacesBufferedList.PROPERTIES_PER_ELEMENT, GLES20.GL_UNSIGNED_SHORT, faces().buffer());
	}
	
	public Object3d clone()
	{
		Vertices v = _vertices.clone();
		FacesBufferedList f = _faces.clone();
			
		Object3d clone = new Object3d(v, f, _textures);
		
		clone.position().x = position().x;
		clone.position().y = position().y;
		clone.position().z = position().z;
		
		clone.rotation().x = rotation().x;
		clone.rotation().y = rotation().y;
		clone.rotation().z = rotation().z;
		
		clone.scale().x = scale().x;
		clone.scale().y = scale().y;
		clone.scale().z = scale().z;
		
		return clone;
	}
}
