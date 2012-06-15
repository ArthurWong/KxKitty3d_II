package com.redwood.vos;

import com.redwood.interfaces.IDirtyParent;

public class BooleanManaged extends AbstractDirtyManaged 
{
	private boolean _b;

	public BooleanManaged(boolean $value, IDirtyParent $parent)
	{
		super($parent);
		set($value);
	}
	
	public boolean get()
	{
		return _b;
	}
	public void set(boolean $b)
	{
		_b = $b;
		setDirtyFlag();
	}
}
