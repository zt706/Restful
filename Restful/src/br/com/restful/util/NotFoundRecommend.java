package br.com.restful.util;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Properties;

import br.com.restful.db.DBHelper;
import br.com.restful.db.LitbDBPool;
import br.com.restful.recommend.CommonTools;

public class NotFoundRecommend
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
	
	public static String getRecommendData(Properties props, String userId, String historyIds, String filterIds)
	{
		// System.out.println("goods is === " + userId);
		
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
			// 生成重复id的集合
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
			
			// 返回去重后的默认推荐物品
	    	return recomm_ids_str;
	    }
		 
		//System.out.println(query_sql);
		DBHelper dbHelper = null;
		 
		try {
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
						
						// 生成重复id的集合
						filterIdSet = filterId2Set(filterIds);
						
						String[] recommendArr = recommendPidsStr.split(",");
						
						if(recommendArr != null && recommendArr.length > 0)
						{
							for(String recommendPid:recommendArr)
							{
								//System.out.println(recommendPid);
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
		} 
		catch (SQLException e) {
			e.printStackTrace();
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
