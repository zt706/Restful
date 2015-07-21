package br.com.restful.cache;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Properties;

import net.sf.json.JSONObject;
import br.com.restful.cache.MemcachedConnector;
import br.com.restful.db.DBHelper;
import br.com.restful.db.LitbDBPool;
import br.com.restful.recommend.CommonTools;


/*
 *  从 10.0.22.106:1522 读出数据
 *  写入 10.0.22.104:11311 cache 服务器
 *  作为资讯页的推荐商品信息
 *  每个articleid一条缓存信息
 */

public class ImportArticleRecommStructIntoCache 
{
	private static String DB_RECOMM_CONN_STR = "";
	private static String DB_RECOMM_USER_NAME = "";
	private static String DB_RECOMM_PASSWORD = "";
	
	private static final String QUERYSQL = "select * "
				+ " FROM "
				+ " recom_article_recommend_product "
				+ " where "
				+ " site_id = 1 "
				+ " and "
				+ " status = 1 ";
	
	// memcache 中商品id所对应推荐对象的 cache key 前缀
	private static final String ARTICLE_CACHE_KEY_PRE = "article_recommend_list_";
	
	// 资讯页最多返回的商品数量
	private static String max_num_str = CommonTools.getProperties().getProperty("article_max_recomm_num");
	private static int MAX_PRODUCT_NUM = Integer.parseInt(max_num_str);
	
	// 获取资讯页文章对应的所有推荐商品结构体list
	public static void imoprttRecommListIntoCache()
	{
		String filterIds = ""; 
		
		Properties props = CommonTools.getProperties();
		
		// 将全站统一过滤id加入过滤ids
		String web_filter_str = CommonTools.getWebFilterIds(props);
		if (web_filter_str != null && web_filter_str.length() > 0)
		{
			if (filterIds.length() > 0 )
			{
				filterIds += "," + web_filter_str;
			}
			else
			{
				filterIds += web_filter_str;
			}
		}
		 
		// String query_sql = String.format(QUERYSQL, articleId);
		 
		// System.out.println(query_sql);
		DBHelper dbHelper = null;
		 
		 try 
		 {
			parserDBProperties(props);
			dbHelper = LitbDBPool.getRecommendDbHelper(DB_RECOMM_CONN_STR, DB_RECOMM_USER_NAME, DB_RECOMM_PASSWORD);
				
			ResultSet resultSet = dbHelper.executeQuery(QUERYSQL);
			
			if(resultSet != null)
			{
				while(resultSet.next())
				{
					ArrayList<ProductObj> recommProductObjList = new ArrayList<>();
					
					long articleId = resultSet.getLong("articleid");
				
					String recommendPidsStr = resultSet.getString("recommends");
					
					if(recommendPidsStr != null && !recommendPidsStr.isEmpty())
					{
						HashSet<Long> filterIdSet = new HashSet<>();
						
						if(filterIds.length() > 0)
						{
							String[] tmpArr = filterIds.split(",");
							
							// System.out.println(tmpArr[0]);
							if(tmpArr.length > 0)
							{
								for(String filterId: tmpArr)
								{
									if(isNum(filterId))
									{
										// filterIdSet.add(Integer.parseInt(filterId));
										filterIdSet.add(Long.parseLong(filterId));
									}
									else {
										// System.out.println("过滤id非法  " + filterId);
										// return "";
									}
								}
							}
						}
						
						String[] recommendArr = recommendPidsStr.split(",");
						
						if(recommendArr != null && recommendArr.length > 0)
						{
							for(String recommendPid:recommendArr)
							{
								// System.out.println(recommendPid);
								if(!filterIdSet.contains(Long.parseLong(recommendPid)))
								{
									// 每个推荐出来的id去cache中取出一个jsonobj对象
									// String product_info_json_str = getProductInfoFromCache(recommendPid);
									ProductObj productObj = getProductInfoFromCache(recommendPid);
									
									if (productObj != null)
									{
										recommProductObjList.add(productObj);
									}
									
									
									if (recommProductObjList.size() >= MAX_PRODUCT_NUM)
									{
										break;
									}
								}
							}
						}
					}
					
					// 将每个article对应的推荐商品结构体list存入cache
					String key = getArticleCacheKey(articleId + "");
					ImportInfoIntoCache(recommProductObjList, key);
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		finally
		{
			if(dbHelper != null)
			{
				dbHelper.close();
			}
		}
		
	}
	
	// 从memcache中取出每个商品对应的javaobj
	public static ProductObj getProductInfoFromCache(String goodsId)
	{
		String cache_key = ImportProductInfoIntoCache.getCacheKey(goodsId);
		// String product_info_str = "";
		
		ProductObj productObj = null;
		
		if(MemcachedConnector.mcc.get(cache_key) != null)
		{
			String product_info_str = (String) MemcachedConnector.mcc.get(cache_key);
			
			JSONObject jsonObject = JSONObject.fromObject(product_info_str);
			
			productObj = (ProductObj)JSONObject.toBean(jsonObject, ProductObj.class);
		}
		
		return productObj;
	}
	
	
	// 设置从当前开始计算的过期时间
	private static final int EXPIRE_TIME = 30 * 3600 * 1000;
		
	// 将对象存入缓存
	public static void ImportInfoIntoCache(ArrayList<ProductObj> recommProductObjList, String cache_key) throws SQLException
	{
		HashMap<String, ArrayList<ProductObj>> recomm_cache_map = new HashMap<>();
		recomm_cache_map.put("Products", recommProductObjList);
		
		JSONObject jsonObject = JSONObject.fromObject(recomm_cache_map);
		Date expireDate = new Date(System.currentTimeMillis() + EXPIRE_TIME);
		
		System.out.println("缓存key == " + cache_key);
		//System.out.println("huan cun value == " + jsonObject.toString());
		
		if(MemcachedConnector.mcc.get(cache_key) != null)
		{
			// 更新缓存服务器中的内容
			MemcachedConnector.mcc.replace(cache_key, jsonObject.toString(), expireDate);
		}
		else
		{
			// 添加新内容到缓存服务器中
			MemcachedConnector.mcc.add(cache_key, jsonObject.toString(), expireDate);
			
		}
	}
	
	public static boolean isNum(String str)
	{
		return str.matches("^[0-9]+$");
	}
	
	// 解析db配置文件
	private static void parserDBProperties(Properties props)
	{
		
		DB_RECOMM_CONN_STR = props.getProperty("db_recomm_conn_str");
		DB_RECOMM_USER_NAME = props.getProperty("db_recomm_user_name");
		DB_RECOMM_PASSWORD = props.getProperty("db_recomm_password");
	}
	
	// 使用文章id生成cache key
	public static String getArticleCacheKey(String articleId)
	{
		String cacke_key = ARTICLE_CACHE_KEY_PRE + articleId;
		return cacke_key;
	}
	
	public static void main(String [] args)
	{
		imoprttRecommListIntoCache();
	}
}
