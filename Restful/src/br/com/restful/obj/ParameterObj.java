package br.com.restful.obj;

public class ParameterObj 
{
	private boolean isNecessary = true;
	
	private String parameterKey = "";
	
	private Class<?> parameterType;
	
	public ParameterObj(boolean isNecessary, String parameterKey, Class<?> parameterType)
	{
		this.isNecessary = isNecessary;
		
		this.parameterKey = parameterKey;
		
		this.parameterType = parameterType;
	}
	
	public void setIsNeccessary(boolean isNecessary)
	{
		this.isNecessary = isNecessary;
	}
	
	public void setParameterKey(String parameterKey)
	{
		this.parameterKey = parameterKey;
	}
	
	public void setParameterType (Class<?> parameterType)
	{
		this.parameterType = parameterType;
	}
	
	public boolean getIsNeccessary()
	{
		return isNecessary;
	}
	
	public String getParameterKey()
	{
		return parameterKey;
	}
	
	public Class<?> getParameterType ()
	{
		return parameterType;
	}
}
