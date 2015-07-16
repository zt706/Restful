package br.com.restful.cache;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import br.com.restful.db.DBHelper;
import br.com.restful.db.OracleHelper;
import br.com.restful.recommend.CommonTools;


/*
 *  从 10.0.22.106:1522 读出数据
 *  写入 10.0.22.104:11311 cache 服务器
 *  作为全局的过滤商品id
 */

public class CacheProductInfo 
{
	private static final String queryOnstockProductSql = 	
			  " select WW.WARESKUCODE,WW.DYID,WW.WARENAME "
			+ " FROM BASE.WI_WARESKU ww, "
			+ " BASE.WI_WARESKUPC wwp "
			+ " WHERE "
			+ " WW.WARESKUCODE = WWP.WARESKUCODE "
			+ " AND "
			+ " WWP.ISINDEX = 0 "
			+ " AND "
			+ " WW.ISSHELFDOWNPC = 0 ";
	
	private HashSet<Long> getOnStockGoodsSet(DBHelper helper) throws SQLException
	{
		HashSet<Long> onStockGoodsSet = new HashSet<>();
		
		ResultSet rs = helper.executeQuery(queryOnstockProductSql);
		
		while (rs.next()) 
		{
			long goodsId = rs.getLong("WARESKUCODE");
			
			if(goodsId > 0)
			{
				onStockGoodsSet.add(goodsId);
				
				//System.out.println(goodsId);
			}
		}
		
		return onStockGoodsSet;
	}
	
	private DBHelper getBIOracle106DBHelper() throws SQLException
	{
		String db_106_conn_str = "jdbc:oracle:thin:@10.0.22.106:1522/";
		String dbName = "kaddw02";
		
		String db_106_user_name = "bidev";
		String db_106_password = "bi2015dev";
		
		return new OracleHelper(db_106_conn_str + dbName, db_106_user_name , db_106_password);
	}
	
	private static final int EXPIRE_TIME = 3 * 3600 * 1000; 
	
	public void ImportFilterIdsIntoCache(HashSet<Long> ids_set) throws SQLException
	{
		if(ids_set.size() > 0)
		{
			String filter_ids_in_cache = ids_set.toString();
			
			filter_ids_in_cache = filter_ids_in_cache.replace("[", "").replace("]", "");
			filter_ids_in_cache = filter_ids_in_cache.replaceAll(" ", "");
			
			Date expireDate = new Date(System.currentTimeMillis() + EXPIRE_TIME);
				
			String cacheKey = getFilterIdsMemKey();
			
			System.out.println("缓存key == " + cacheKey);
			
			if(MemcachedConnector.mcc.get(cacheKey) != null)
			{
				// 更新缓存服务器中的内容
				MemcachedConnector.mcc.replace(cacheKey, filter_ids_in_cache, expireDate);
			}
			else
			{
				// 添加内容到缓存服务器中
				MemcachedConnector.mcc.add(cacheKey, filter_ids_in_cache, expireDate);
			}
			
		}
	}
	
	public static String getFilterIdsMemKey() 
	{
		Properties props = CommonTools.getProperties();
		String cache_key = CommonTools.getCacheKey(props);
		
		return cache_key;
	}
	
	 public static void main(String[] args) throws SQLException
	 {
		 CacheProductInfo cacheProductInfo = new CacheProductInfo();
		 
		 DBHelper helper = cacheProductInfo.getBIOracle106DBHelper();
		 
		 HashSet<Long> ids_set = cacheProductInfo.getOnStockGoodsSet(helper);
		 
		 cacheProductInfo.ImportFilterIdsIntoCache(ids_set);
	 }
}
