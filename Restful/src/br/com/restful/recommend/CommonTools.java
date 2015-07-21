package br.com.restful.recommend;

import java.io.FileInputStream;
import java.util.Properties;

import br.com.restful.cache.MemcachedConnector;


/*
 * 获取配置的公共接口 
 * 
 */

public class CommonTools 
{
	private static String propsFile = "E:\\workspace\\Restful\\Restful\\config\\recomm-database.properties";
	//private static String propsFile = "/home/recomm/recommendconf/recomm-database.properties";
	
	// 获得配置文件对象
	public static Properties getProperties()
	{
		Properties props = null;
		try
	    {
	        FileInputStream propsIn = new FileInputStream(propsFile);
	        props = new Properties();
	        props.load(propsIn);
	        propsIn.close();
	    }
	    catch(Exception e)
	    {
	    	e.printStackTrace();
	    }
		
		return props;
	}
		 
	// 获取104 memcache server 地址
	public static String getCacheServer()
	{
		Properties props = getProperties();
		String server_str = ""; 
		
		server_str = props.getProperty("cache_server");
		
		return server_str;
	}
	
	// 从配置文件中获取全站过滤商品id对应的memcache key
	public static String getCacheKey(Properties props)
	{
		String cache_key = props.getProperty("filter_cache_key");
		
		return cache_key;
	}
	
	// 从memcache中获取全站通用的过滤商品id
	public static String getWebFilterIds(Properties props)
	{
		// 获取memcache 中保存的过滤商品id的cachekey
		String cache_key = props.getProperty("filter_cache_key");
		
		String filter_ids = "";
		if(MemcachedConnector.mcc.get(cache_key) != null)
		{
			filter_ids = (String) MemcachedConnector.mcc.get(cache_key);
		}
		
		return filter_ids;
	}
}
