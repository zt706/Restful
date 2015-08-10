package br.com.restful.util;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Properties;

import net.sf.json.JSONObject;
import br.com.restful.db.DBHelper;
import br.com.restful.db.LitbDBPool;
import br.com.restful.recommend.CommonTools;

/*
 * 	首页推荐
 */

public class IndexPageRecommend
{
	private static String DB_RECOMM_CONN_STR = "";
	private static String DB_RECOMM_USER_NAME = "";
	private static String DB_RECOMM_PASSWORD = "";
	
	private static final String QUERYSQL_1 = "select * "
			+ "from recom_user_maybe_buy "
			+ "where "
			+ "site_id=1 and status=1 and user_id ="
			+ "%s ";
	
	private static final String QUERYSQL_2 = "select * "
			+ "from recom_product_ultimately_buy "
			+ "where "
			+ "site_id=1 and status=1 and goodsid in "
			+ "(%s) ";
	
	// 默认返回的推荐物品
	private static final String RECOMMENDIDS = "268,45972,2066";
	
	public static String getRecommendData(Properties props, String userId, String historyIds, String filterIds
							, String pageType
							, String recomm
							)
	{
		// System.out.println("goods is === " + userId);
		
		String input_filter_ids = filterIds;
		String page_type = pageType;
		String recomm_str = recomm;
		
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
				
		String recommendStr = "";
		String query_sql = "";
		
	    if (!userId.equals(""))
	    {
	    	query_sql = String.format(QUERYSQL_1, userId);
	    }
	    else if (!historyIds.equals(""))
	    {
	    	String [] tmp_str = historyIds.split(",");
	    	boolean all_is_num_flag = true;
	    	
	    	for (int i = 0; i < tmp_str.length; i++)
	    	{
	    		if (!isNum(tmp_str[i]))
	    		{
	    			all_is_num_flag = false;
	    		}
	    	}
	    	
	    	if (all_is_num_flag)
	    	{
	    		query_sql = String.format(QUERYSQL_2, historyIds);
	    		//System.out.println("404 == sql === " + query_sql);
	    	}
	    	else
	    	{
	    		//System.out.println("历史记录id非法  " + historyIds);
				// return "";
	    	}
	    }
	    else
	    {
	    	String recomm_ids_str = "";
	    	String [] static_recomm_ids = RECOMMENDIDS.split(",");
	    	
	    	HashSet<Long> filterIdSet = new HashSet<>();
			// 生成过滤id的集合
			filterIdSet = filterId2Set(filterIds);

			for (int i = 0; i < static_recomm_ids.length; i++)
			{
				//int recomm_id_int = Integer.parseInt(static_recomm_ids[i]);
				long recomm_id_int = Long.parseLong(static_recomm_ids[i]);
				
				if (!filterIdSet.contains(recomm_id_int))
				{
					if (recomm_ids_str.length() >= 1)
					{
						recomm_ids_str += "," + recomm_id_int ;
					}
					else
					{
						recomm_ids_str += recomm_id_int + "";
					}
				}
			}
			
			// 返回过滤后的默认推荐物品
	    	return recomm_ids_str;
	    }
		
	    // 最后赋值给recommendpids的map
	    HashMap<String, ArrayList<HashMap<String, String>>> recommendPids_map = new HashMap<>();
	    
	    // 用historyids 生成的推荐商品list
	    ArrayList<HashMap<String, String>> recomm_history_pid_list = new ArrayList<>();
	    // 用userid 生成的推荐商品list
	    ArrayList<HashMap<String, String>> recomm_user_id_list = new ArrayList<>();
	    
	    // 存放所有传入的historyid的数组
	    ArrayList<String> history_arr = new ArrayList<>();
	    
	    // 将收到的historyIds存入list
	    String [] history_split_str = historyIds.split(",");
	    for (int i = 0; i < history_split_str.length; i++)
	    {
	    	history_arr.add(i, history_split_str[i]);
	    }
	    
		//System.out.println(query_sql);
		DBHelper dbHelper = null;
		 
		try {
			parserDBProperties(props);
			dbHelper = LitbDBPool.getRecommendDbHelper(DB_RECOMM_CONN_STR, DB_RECOMM_USER_NAME, DB_RECOMM_PASSWORD);
			
			ResultSet resultSet = dbHelper.executeQuery(query_sql);
			
			if(resultSet != null)
			{
				int history_id_index = 0;
				
				while(resultSet.next())
				{
					// 每个historyid对应的推荐id过滤后的内容
					String recommIdsStr = "";		
					
					// 存放每个historyid及其对应的推荐商品的map
				    HashMap<String, String> recomm_history_map = new HashMap<>();
				    
					String recommendPidsStr = resultSet.getString("recommends");
					if(recommendPidsStr != null && !recommendPidsStr.isEmpty())
					{
						HashSet<Long> filterIdSet = new HashSet<>();
						
						// 生成过滤id的集合
						filterIdSet = filterId2Set(filterIds);
						
						String[] recommendArr = recommendPidsStr.split(",");
						
						if(recommendArr != null && recommendArr.length > 0)
						{
							for(String recommendPid:recommendArr)
							{
								//System.out.println(recommendPid);
								if(!filterIdSet.contains(Long.parseLong(recommendPid)))
								{
									if(recommIdsStr.length() > 0)
									{
										recommIdsStr += "," + recommendPid;
									}
									else{
										recommIdsStr += recommendPid;
									}
								}
								
								// 将推荐出来的商品加入过滤id中,作为下一个推荐列表的过滤id
								filterIds += "," + recommendPid;
							}
						}
					}
					
					String history_id = history_arr.get(history_id_index);
					
					//recomm_history_map.put(history_id, recommIdsStr);
					recomm_history_map.put("historyid", history_id);
					recomm_history_map.put("recommids", recommIdsStr);
					
					recomm_history_pid_list.add(recomm_history_map);
					recommendPids_map.put("recommend_ids_for_history", recomm_history_pid_list);
					
					recommendPids_map.put("recommend_ids_for_userid", recomm_user_id_list);
					
					history_id_index++;
				}
				
				// 生成返回需要的结构体
				IndexPageRecommStruct recomm_struct = new IndexPageRecommStruct
															  (
																page_type,
																recomm_str,
																"",
																"",
																input_filter_ids,
																"",
																userId,
																historyIds,
																"",
																"",
																recommendPids_map
															  );
				
				JSONObject jsonObject = JSONObject.fromObject(recomm_struct);
				
				recommendStr = jsonObject.toString();
				
				System.out.println("9090909 " + jsonObject);
			}
		} 
		catch (SQLException e) {
			e.printStackTrace();
		}
		finally
		{
			if(dbHelper != null)
			{
				dbHelper.close();
			}
		} 
		
		return recommendStr;
		
	}
	
	public static boolean isNum(String str)
	{
		return str.matches("^[0-9]+$");
	}
	
	public static HashSet<Long> filterId2Set(String filterIds)
	{
		HashSet<Long> filterIdSet = new HashSet<>();
		
		if(filterIds.length() > 0)
		{
			String[] tmpArr = filterIds.split(",");
			
			//System.out.println(tmpArr[0]);
			if(tmpArr.length > 0)
			{
				for(String filterId: tmpArr)
				{
					if(isNum(filterId))
					{
						filterIdSet.add(Long.parseLong(filterId));
					}
					else {
						//System.out.println("过滤id非法  " + filterId);
						// return "";
					}
					
				}
			}
		}
		
		return filterIdSet;
	}
	
	// 解析db配置文件
	private static void parserDBProperties(Properties props)
	{
		
		DB_RECOMM_CONN_STR = props.getProperty("db_recomm_conn_str");
		DB_RECOMM_USER_NAME = props.getProperty("db_recomm_user_name");
		DB_RECOMM_PASSWORD = props.getProperty("db_recomm_password");
	}
}

