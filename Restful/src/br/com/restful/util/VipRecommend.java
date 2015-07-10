package br.com.restful.util;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Properties;

import br.com.restful.db.DBHelper;
import br.com.restful.db.LitbDBPool;
import br.com.restful.recommend.CommonTools;

public class VipRecommend
{
	private static String DB_RECOMM_CONN_STR = "";
	private static String DB_RECOMM_USER_NAME = "";
	private static String DB_RECOMM_PASSWORD = "";
	
	private static final String QUERYSQL = "select *"
			+ " from recom_user_maybe_buy"
			+ " where site_id = 1"
			+ " and status = 1"
			+ " and user_id = %s";
	
	public static String getRecommendData(Properties props, String userId, String filterIds)
	{
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
				
		// 推荐商品
		String recommStr = "";
		String sqlStr = "";
		
		if(userId.length() > 0)
		{
			if(!isNum(userId))
			{
				//System.out.println("用户id非法");
				return "";
			}
			else 
			{
				sqlStr = String.format(QUERYSQL, userId);
			}
		}
		
		try
		{
			parserDBProperties(props);
			DBHelper helper = LitbDBPool.getRecommendDbHelper(DB_RECOMM_CONN_STR, DB_RECOMM_USER_NAME, DB_RECOMM_PASSWORD);
			ResultSet resultSet = helper.executeQuery(sqlStr);
			
			HashSet<Long> filterSet = new HashSet<Long>();
			
			if(resultSet != null)
			{
				if(filterIds.length() > 0)
				{
					String [] filterStr = filterIds.split(",");
					if (filterStr.length > 0)
					{
						for(String tmpFilterStr : filterStr)
						{
							if(isNum(tmpFilterStr))
							{
								filterSet.add(Long.parseLong(tmpFilterStr));
							}
							else
							{
								//System.out.println("过滤id非法");
								// return "";
							}
						}
					}
				}
			}
			
			if(resultSet.next())
			{
				String recommPidsStr = resultSet.getString("recommends");
				
				if(recommPidsStr != null && !recommPidsStr.isEmpty())
				{
					String [] recommArr = recommPidsStr.split(",");
					
					if(recommArr != null && recommArr.length > 0)
					{
						for(String tmpPidStr:recommArr)
						{
							if( !filterSet.contains(Long.parseLong(tmpPidStr)))
							{
								if (recommStr.length() > 1)
								{
									recommStr += "," + tmpPidStr;
								}
								else 
								{
									recommStr += tmpPidStr;
								}
							}
						}
					}
				}
			}
		} catch (SQLException e) {
			
			e.printStackTrace();
		}
		
		return recommStr;
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
