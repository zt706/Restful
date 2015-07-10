package br.com.restful.cache;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;

import br.com.restful.recommend.CommonTools;

import com.danga.MemCached.MemCachedClient;
import com.danga.MemCached.SockIOPool;

public class MemcachedConnector 
{
	// 创建一个 memcached 客户端对象  
    public static MemCachedClient mcc = new MemCachedClient();  
  
    // 创建 memcached连接池  
    static  
    { 
    	// 指定memcached服务地址 
    	// String[] servers = { "10.0.22.104:11311" };
    	String tmp_str = CommonTools.getCacheServer();
    	String[] servers = { tmp_str };
  
	    // 指定memcached服务器负载量  
	    Integer[] weights ={3};  
	  
	    // 从连接池获取一个连接实例  
	    SockIOPool pool = SockIOPool.getInstance();  
	  
	    // 设置服务器和服务器负载量  
	    pool.setServers( servers );  
	  
	    pool.setWeights( weights );  
	  
	    // 设置一些基本的参数  
	    //设置初始连接数5 最小连接数 5 最大连接数 250  
	    //设置一个连接最大空闲时间6小时  
	    pool.setInitConn( 5 );  
	    pool.setMinConn( 5 );  
	    pool.setMaxConn( 250 );  
	    pool.setMaxIdle( 1000 * 60 * 60 * 6 );  

	    // 设置主线程睡眠时间  
	    // 每隔30秒醒来 然后  
	    // 开始维护 连接数大小  
	    pool.setMaintSleep(30);  

	    // 设置tcp 相关的树形  
	    // 关闭nagle算法  
	    // 设置 读取 超时3秒钟 set the read timeout to 3 secs  
	    // 不设置连接超时  
	    pool.setNagle( false );  
	    pool.setSocketTO( 3000 );  
	    pool.setSocketConnectTO( 0 );  
	  
	    // 开始初始化 连接池  
	    pool.initialize();  
	  
	    // 设置压缩模式  
//	    //如果超过64k压缩数据  
//	    mcc.setCompressEnable( true );  
//	    mcc.setCompressThreshold( 64 * 1024 );  
  
    }
    
    public static ArrayList<String> getMemKeyList()
    {
    	ArrayList<String> list = new ArrayList<String>();  
	    Map<String, Map<String, String>> items = mcc.statsItems();  
	    for (Iterator<String> itemIt = items.keySet().iterator(); itemIt.hasNext();) 
	    {
	        String itemKey = itemIt.next();  
	        Map<String, String> maps = items.get(itemKey);  
	        for (Iterator<String> mapsIt = maps.keySet().iterator(); mapsIt.hasNext();) 
	        {  
	        	String mapsKey = mapsIt.next();  
	        	String mapsValue = maps.get(mapsKey);  
	        	if (mapsKey.endsWith("number")) 
	        	{    
	        		String[] arr = mapsKey.split(":");
	        		int slabNumber = Integer.valueOf(arr[1].trim());  
	        		int limit = Integer.valueOf(mapsValue.trim());  
	        		Map<String, Map<String, String>> dumpMaps = mcc.statsCacheDump(slabNumber, limit);
	                   
	        		for (Iterator<String> dumpIt = dumpMaps.keySet().iterator(); dumpIt.hasNext();) 
	        		{  
	        			String dumpKey = dumpIt.next();  
	        			Map<String, String> allMap = dumpMaps.get(dumpKey);  
	        			for (Iterator<String> allIt = allMap.keySet().iterator(); allIt.hasNext();) 
	        			{  
	        				String allKey = allIt.next();  
	        				list.add(allKey.trim());  
	        				System.out.println(allKey.trim());
	        			}  
	        		}  
	        	}
	        }
	    }
	    
	    return list;
    }
}