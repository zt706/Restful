package br.com.restful.util;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

import net.sf.json.JSONObject;
import br.com.restful.cache.ImportProductInfoIntoCache;
import br.com.restful.cache.MemcachedConnector;
import br.com.restful.cache.ProductObj;
import br.com.restful.db.DBHelper;
import br.com.restful.db.LitbDBPool;
import br.com.restful.recommend.CommonTools;
import br.com.restful.recommend.JsonUtil;


/*
 * 资讯页
 */
public class ArticlePageRecommend 
{
	private static String DB_RECOMM_CONN_STR = "";
	private static String DB_RECOMM_USER_NAME = "";
	private static String DB_RECOMM_PASSWORD = "";
	
	private static final String QUERYSQL = "select * "
				+ " FROM "
				+ " recom_article_recommend_product "
				+ " where "
				+ " articleid = %s "
				+ " AND "
				+ " site_id = 1 "
				+ " and "
				+ " status = 1 ";
	
	// memcache 中商品id所对应推荐对象的 cache key 前缀
	private static final String CACHE_KEY_PRE = "product_info_";
	
	// 资讯页最多返回的商品数量
	private static final int MAX_PRODUCT_NUM = 6;
	
	public static String getArticleRecommendIds(Properties props, int articleId, String filterIds)
	{
		String recommendStr = "";
		
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
		 
		String query_sql = String.format(QUERYSQL, articleId);
		 
		// System.out.println(query_sql);
		DBHelper dbHelper = null;
		 
		 try 
		 {
			parserDBProperties(props);
			dbHelper = LitbDBPool.getRecommendDbHelper(DB_RECOMM_CONN_STR, DB_RECOMM_USER_NAME, DB_RECOMM_PASSWORD);
				
			ResultSet resultSet = dbHelper.executeQuery(query_sql);
			
			if(resultSet != null)
			{
				if(resultSet.next())
				{
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
									if(recommendStr.length() > 0)
									{
										recommendStr += "," + recommendPid;
									}
									else{
										recommendStr += recommendPid;
									}
								}
							}
						}
					}
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
//		 HashMap<String, HashMap<String, String>> recomm_ids_info_map =  getRecommStruct(recommendStr);
		 ArrayList<HashMap<String, String>> recomm_ids_info_list = getRecommList(recommendStr);
//		 ArrayList<ProductObj> recomm_all_info_list = getAllRecommInfo(recommendStr);
		 
//		 HashMap<String, HashMap<String, HashMap<String, String>>> recomm_ids_info_map_tmp = new HashMap<>();
		 HashMap<String, ArrayList<HashMap<String, String>>> recomm_ids_info_list_tmp = new HashMap<>();
//		 HashMap<String, ArrayList<ProductObj>> recomm_all_info_map_tmp = new HashMap<>();
		 
		 // 最多返回6个
//		 HashMap<String, HashMap<String, String>> recomm_ids_info_map_six = new LinkedHashMap<>();
		 ArrayList<HashMap<String, String>> recomm_ids_info_list_six = new ArrayList<>();
		 
		 if (recomm_ids_info_list.size() > MAX_PRODUCT_NUM)
		 {
			 for(HashMap<String, String> valueMap : recomm_ids_info_list)
			 {
				 recomm_ids_info_list_six.add(valueMap);
				 
				 if(recomm_ids_info_list_six.size() >= MAX_PRODUCT_NUM)
				 {
					 break;
				 }
			 }
			 
			 recomm_ids_info_list_tmp.put("Products", recomm_ids_info_list_six);
		 }
		 else
		 {
			 recomm_ids_info_list_tmp.put("Products", recomm_ids_info_list);
		 }
		 
		 String recomm_ids_info_str = "";
		 recomm_ids_info_str = JsonUtil.object2json(recomm_ids_info_list_tmp );
		 
		 return recomm_ids_info_str;
	}
	
	// 使用推荐的id从mem cache 中取出对应的结构体
	public static HashMap<String, HashMap<String, String>> getRecommStruct(String RecommIds)
	{
		String [] recommid_slpit = RecommIds.split(",");
		
		// 保存所有商品详细信息的map
		HashMap<String, HashMap<String, String>> info_map = new LinkedHashMap<>();
		
		if (recommid_slpit != null && recommid_slpit.length > 0)
		{
			for (int i = 0; i < recommid_slpit.length; i++)
			{
				// 保存每个商品的详细信息的map
				HashMap<String, String> id_info_map = new HashMap<>();
				
				String goods_id = recommid_slpit[i];
				String cache_key = getCacheKey(goods_id);
				
				ProductObj productObj = getKadRecommObj(cache_key);
				
				if (productObj != null)
				{
					// 商品序号
					int item_id = productObj.getItemId();
					id_info_map.put("ItemId", item_id + "");
					
					// 图片链接
					String product_thumb = productObj.getProductThumb();
					id_info_map.put("ProductThumb", product_thumb);
					
					// 产品名字
					String product_name = productObj.getTitle();
					id_info_map.put("Title", product_name);
					
					// 产品规格
					String product_spec = productObj.getSpec();
					id_info_map.put("Spec", product_spec);
					
					// 价格
					float product_price = productObj.getPrice();
					id_info_map.put("Price", product_price + "");
					
					// 使用剂量
					String product_dosage = productObj.getUsageDosage();
					id_info_map.put("UsageDosage", product_dosage);
					
					// 产品介绍
					String product_intro = productObj.getProductIntro();
					id_info_map.put("ProductIntro", product_intro);
					
					// 厂家名字
					String producter_name = productObj.getProducterName();
					id_info_map.put("ProducerName", producter_name);
					
					// 产品代码
					String product_code = productObj.getProductCode();
					id_info_map.put("ProductCode", product_code);
					
					// 是否处方药
					id_info_map.put("IsRx", productObj.getIsRx() + "");
					
					// 通用名
					id_info_map.put("GeneralName", productObj.getGeneralName());
					
					// 品牌名
					id_info_map.put("BrandCode", productObj.getBrandCode());
					
					// otc类型
					id_info_map.put("OtcType", productObj.getOtctype());
					
					// 存入大map
					// String json_str = JsonUtil.object2json(id_info_map);
					info_map.put(i + "", id_info_map);
				}
			}
		}
		
		 return info_map;
	}
	
	public static ArrayList<HashMap<String, String>>  getRecommList(String RecommIds)
	{
		String [] recommid_slpit = RecommIds.split(",");
		
		// 保存所有商品详细信息的list
		ArrayList<HashMap<String, String>> info_list = new ArrayList<>();
		
		if (recommid_slpit != null && recommid_slpit.length > 0)
		{
			for (int i = 0; i < recommid_slpit.length; i++)
			{
				// 保存每个商品的详细信息的map
				HashMap<String, String> id_info_map = new HashMap<>();
				
				String goods_id = recommid_slpit[i];
				String cache_key = getCacheKey(goods_id);
				
				ProductObj productObj = getKadRecommObj(cache_key);
				
				if (productObj != null)
				{
					// 商品序号
					int item_id = productObj.getItemId();
					id_info_map.put("ItemId", item_id + "");
					
					// 图片链接
					String product_thumb = productObj.getProductThumb();
					id_info_map.put("ProductThumb", product_thumb);
					
					// 图片链接 大图
					String product_thumb_big = productObj.getProductThumbBig();
					if (product_thumb_big != null && !product_thumb_big.equals(""))
					{
						product_thumb_big = product_thumb_big + "_230x230.jpg";
					}
					id_info_map.put("ProductThumbBig", product_thumb_big);
					
					// 产品名字
					String product_name = productObj.getTitle();
					id_info_map.put("Title", product_name);
					
					// 产品规格
					String product_spec = productObj.getSpec();
					id_info_map.put("Spec", product_spec);
					
					// 价格
					float product_price = productObj.getPrice();
					DecimalFormat decimalFormat=new DecimalFormat(".00");
					String product_price_str = decimalFormat.format(product_price);
					id_info_map.put("Price", product_price_str);
					
					// 使用剂量
					String product_dosage = productObj.getUsageDosage();
					id_info_map.put("UsageDosage", product_dosage);
					
					// 产品介绍
					String product_intro = productObj.getProductIntro();
					id_info_map.put("ProductIntro", product_intro);
					
					// 厂家名字
					String producter_name = productObj.getProducterName();
					id_info_map.put("ProducerName", producter_name);
					
					// 产品代码
					String product_code = productObj.getProductCode();
					id_info_map.put("ProductCode", product_code);
					
					// 是否处方药
					id_info_map.put("IsRx", productObj.getIsRx() + "");
					
					// 通用名
					id_info_map.put("GeneralName", productObj.getGeneralName());
					
					// 品牌名
					id_info_map.put("BrandCode", productObj.getBrandCode());
					
					// otc类型
					id_info_map.put("OtcType", productObj.getOtctype());
					
					// 存入大map
					// String json_str = JsonUtil.object2json(id_info_map);
					info_list.add( id_info_map);
				}
			}
		}
		
		 return info_list;
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
	
	// 使用商品id生成cache key
	public static String getCacheKey(String articleId)
	{
		// 使用导入缓存时的cache key, 保证一致
		String cacke_key = ImportProductInfoIntoCache.getCacheKey(articleId);
		
		return cacke_key;
	}
	
	// 从cache中取出数据并转化为java对象
	public static ProductObj getKadRecommObj(String cacheKey)
	{
		// System.out.println(" cong cache zhong  qu chu ==== " + cacheKey );
		
		ProductObj productObj = null;
		
		// if (cacheKey.equals("2015-6-9-16_tianmao"))
			// System.out.println("key == " + cacheKey + " value == " + MemcachedConnector.mcc.get(cacheKey));
		
		if(MemcachedConnector.mcc.get(cacheKey) != null)
		{
			productObj = new ProductObj();
			
			String str = (String) MemcachedConnector.mcc.get(cacheKey);
			
			JSONObject jsonObject = JSONObject.fromObject(str);
			
			productObj = (ProductObj)JSONObject.toBean(jsonObject, ProductObj.class);
		}
		
		return productObj;
	}
	
	// 根据推荐id直接返回 ProductObj 对象
	public static ArrayList<ProductObj> getAllRecommInfo(String RecommIds)
	{
		String [] recommid_slpit = RecommIds.split(",");
		
		// 保存所有商品详细信息的map
		ArrayList<ProductObj> info_list = new ArrayList<>();
		
		if (recommid_slpit != null && recommid_slpit.length > 0)
		{
			for (int i = 0; i < recommid_slpit.length; i++)
			{
				// 保存每个商品的详细信息的map
				HashMap<String, String> id_info_map = new HashMap<>();
				
				String goods_id = recommid_slpit[i];
				String cache_key = getCacheKey(goods_id);
				
				ProductObj probj = getKadRecommObj(cache_key);
				
				if (probj != null)
				{
					info_list.add(probj);
				}
			}
		}
		
		return info_list;
	}
}
