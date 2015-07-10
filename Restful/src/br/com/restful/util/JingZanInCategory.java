package br.com.restful.util;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Properties;

import br.com.restful.db.DBHelper;
import br.com.restful.db.LitbDBPool;


/*
 *  类别页(category )的晶赞推荐区块
 */

public class JingZanInCategory 
{
	private static String DB_RECOMM_CONN_STR = "";
	private static String DB_RECOMM_USER_NAME = "";
	private static String DB_RECOMM_PASSWORD = "";
	
	private static final String QUERYSQL = "select *"
			+ " from recom_category_jingzan_recomm"
			+ " where site_id = 1"
			+ " and status = 1"
			+ " and categoryid = %s"
			+ " order by frequency desc";
	
	public static String getRecommendData(Properties props, int categoryIds, String filterIds)
	{
		 String recommendStr = ""; 
		 String query_sql = String.format(QUERYSQL, categoryIds);
		 
		 // System.out.println(query_sql);
		 DBHelper dbHelper = null;
		 
		 try 
		 {
			parserDBProperties(props);
			dbHelper = LitbDBPool.getRecommendDbHelper(DB_RECOMM_CONN_STR, DB_RECOMM_USER_NAME, DB_RECOMM_PASSWORD);
			
			ResultSet resultSet = dbHelper.executeQuery(query_sql);
			
			if(resultSet != null)
			{
				while(resultSet.next())
				{
					String recommendPidsStr = "";
					
					recommendPidsStr = resultSet.getString("recommendid") ;
					
					if(recommendPidsStr != null && !recommendPidsStr.isEmpty())
					{
						HashSet<Integer> filterIdSet = new HashSet<>();
						
						if(filterIds.length() > 0)
						{
							String[] tmpArr = filterIds.split(",");
							
							// System.out.println("guo lv id " + tmpArr[0]);
							if(tmpArr.length > 0)
							{
								for(String filterId: tmpArr)
								{
									if(isNum(filterId))
									{
										filterIdSet.add(Integer.parseInt(filterId));
									}
									else {
										//System.out.println("过滤id非法  " + filterId);
										// return "";
									}
								}
							}
						}
						
						// System.out.println("tui jian id " + recommendPidsStr);
						if(!filterIdSet.contains(Integer.parseInt(recommendPidsStr)))
						{
							recommendStr += recommendPidsStr +  ",";
						}
					}
					
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		 // 去掉最后的逗号
		if (recommendStr.length() > 1)
		{
			recommendStr = recommendStr.substring(0, recommendStr.length() - 1);
		}
		return recommendStr;
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
}
