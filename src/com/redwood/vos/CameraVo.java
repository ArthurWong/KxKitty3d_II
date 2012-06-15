package com.redwood.vos;

/**
 * Encapsulates camera-related properties, including view frustrum.
 */
public class CameraVo
{
	//public Number3d position = new Number3d(0,0, 5); // ... note, not 'managed'
	public Number3dManaged position = new Number3dManaged(null);
	public Number3d target = new Number3d(0,0,0);
	public Number3d upAxis = new Number3d(0,1,0);
	
	public FrustumManaged frustum = new FrustumManaged(null);

	
	public CameraVo()
	{
		position.setAll(0, 0, 5);
	}
}
