package br.com.restful.obj;

import java.util.ArrayList;

public class ParameterCollectObj 
{
	private ArrayList<ParameterObj> parameterObjList;
	
	private String recommendClassName;
	
	public ParameterCollectObj(String recommendClassName)
	{
		parameterObjList = new ArrayList<>();
		
		this.recommendClassName = recommendClassName;
	}
	
	public void addParamaterObj(ParameterObj parameterObj)
	{
		parameterObjList.add(parameterObj);
	}
	
	public void setRecommendClassName()
	{
		
	}
}
