package br.com.restful.util;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Properties;

import br.com.restful.db.DBHelper;
import br.com.restful.db.LitbDBPool;


/*
 * 商品详情页(product)的晶赞推荐区块
 */

public class JingZanInProduct 
{
	private static String DB_RECOMM_CONN_STR = "";
	private static String DB_RECOMM_USER_NAME = "";
	private static String DB_RECOMM_PASSWORD = "";
	
	private static final String QUERYSQL_GOODSID = "select *"
			+ " from recom_product_ultimately_buy"
			+ " where site_id = 1"
			+ " and status = 1"
			+ " and goodsid = %s";
	
	public static String getRecommendData(Properties props, long Ids, String filterIds)
	{
		String recommendStr = Ids + "";
		
		String query_sql = "";
		query_sql = String.format(QUERYSQL_GOODSID, Ids); 
		 
		 //System.out.println(query_sql);
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
					String recommendPidsStr = "";
					
					recommendPidsStr = resultSet.getString("recommends");
					
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
									if (recommendStr.length() > 0)
									{
										recommendStr += "," + recommendPid;
									}
									else
									{
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

	// 解析db配置文件
	private static void parserDBProperties(Properties props)
	{
		
		DB_RECOMM_CONN_STR = props.getProperty("db_recomm_conn_str");
		DB_RECOMM_USER_NAME = props.getProperty("db_recomm_user_name");
		DB_RECOMM_PASSWORD = props.getProperty("db_recomm_password");
	}
}
