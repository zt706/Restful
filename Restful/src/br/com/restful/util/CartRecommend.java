package br.com.restful.util;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import br.com.restful.db.DBHelper;
import br.com.restful.db.LitbDBPool;
import br.com.restful.recommend.CommonTools;

public class CartRecommend 
{
	private static String DB_RECOMM_CONN_STR = "";
	private static String DB_RECOMM_USER_NAME = "";
	private static String DB_RECOMM_PASSWORD = "";
	
	private static final String QUERYSQL = "select * "
			+ "from recom_cart_bought_together "
			+ "where "
			+ "site_id=1 and status=1 and goodsid in"
			+ "(%s) ";
	private static final int MAX_RECOMMEND_NUM = 15;
	
	public static String getRecommendData(Properties props, String goodsids, String filterIds)
	{
		String recommStr  = "";
		String sqlString = "";
		
		// 将商品id直接放入过滤id
		if (filterIds.equals(""))
		{
			filterIds += goodsids;
		}
		else
		{
			filterIds += "," + goodsids;
		}
		
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
		
		// key为goodsid, value为推荐物品的priority / 该商品的priority
		HashMap<Long,Double> recommStruct = new HashMap<Long,Double>();
		
		DBHelper helper = null;
		
		if (goodsids.length() > 0)
		{
			String [] tmpGoodsids = goodsids.split(",");
			
			for (String tmp:tmpGoodsids)
			{
				if(!isNum(tmp))
				{
					// 出现非数字形式的goodsid
					// System.out.println("商品id非法" + tmp);
					return "";
				}
			}
			
			sqlString = String.format(QUERYSQL, goodsids);
		}
		
		try 
		{	parserDBProperties(props);
			helper = LitbDBPool.getRecommendDbHelper(DB_RECOMM_CONN_STR, DB_RECOMM_USER_NAME, DB_RECOMM_PASSWORD);
			ResultSet resultSet = helper.executeQuery(sqlString);
			
			if(resultSet != null)
			{
				HashSet<Long> filterIdSet = new HashSet<>();
				
				if(filterIds.length() > 0)
				{
					String[] tmpArr = filterIds.split(",");
					
					if(tmpArr.length > 0)
					{
						for(String filterId: tmpArr)
						{
							if (isNum(filterId))
							{
								//filterIdSet.add(Integer.parseInt(filterId));
								filterIdSet.add(Long.parseLong(filterId));
							}
							else
							{
								// System.out.println("过滤id非法  " + filterId);
								// return "";
							}
						}
					}
				}
				
				while(resultSet.next())
				{
					int goodPriorityInt = resultSet.getInt("priority");
					String recommendPidsStr = resultSet.getString("recommends");
					
					if(recommendPidsStr != null && !recommendPidsStr.isEmpty())
					{
						String[] recommendArr = recommendPidsStr.split(";");
						
						if(recommendArr != null && recommendArr.length > 0)
						{
							for(String recommendPidFenghao:recommendArr)
							{
								// 输出推荐的id
								//System.out.println("tuijian id ====== " + recommendPidFenghao);
								
								// 按逗号切割
								String[] recommendPidDouhao = recommendPidFenghao.split(",");
								//int recommGoodId = Integer.parseInt(recommendPidDouhao[0]);
								long recommGoodId = Long.parseLong(recommendPidDouhao[0]);
								
								double priority = Double.parseDouble(recommendPidDouhao[1]);
								
								if(!filterIdSet.contains(recommGoodId))
								{
									//System.out.printf("%d ,,, %f ,,,, %f\n", goodPriorityInt, priority, priority / goodPriorityInt);
									
									// 用推荐集合物品的priority / 商品的priority
									if(!recommStruct.containsKey(recommGoodId))
									{
										recommStruct.put(recommGoodId , (priority / goodPriorityInt));
									}
									else{
										recommStruct.put(recommGoodId , recommStruct.get(recommGoodId) + (priority / goodPriorityInt));
									}
									
									/*
									if (recommStruct.size() > MAX_RECOMMEND_NUM)
									{
										break;
									}
									*/
								}
							}
						}
					}
				}
			    
				// 降序排序
			    List<Map.Entry<Long,Double>> list = new ArrayList<Map.Entry<Long,Double>>(recommStruct.entrySet());			    
			    Collections.sort(list,new Comparator<Map.Entry<Long,Double>>(){
						@Override
						public int compare(Entry<Long, Double> o1,
								Entry<Long, Double> o2) 
						{
							if(o1.getValue()>o2.getValue())
							{
								return -1;
							}else if(o1.getValue()<o2.getValue())
							{
								return 1;
							}
							else
							{
								if(o1.getKey() > o2.getKey())
								{
									return -1;
								}
								else if(o1.getKey() < o2.getKey())
								{
									return 1;
								}
								else
								{
									return 0;
								}
							}
						}
			    });
			    
			    int num_goods = 0;
			    // 返回多个商品的推荐结果
			    for(Map.Entry<Long,Double> mapping : list)
			    { 
			    	//System.out.println(mapping.getKey()+":"+ mapping.getValue()); 
			    	// 返回前15个商品
		            if (num_goods < MAX_RECOMMEND_NUM)
		            {
		            	if (recommStr.length() > 1)
				    	{
				    		recommStr += "," + mapping.getKey(); 
				    	}
				    	else 
				    	{
				    		recommStr += mapping.getKey();
						}
		            }
		            else
		            {
		            	break;
		            }
		            
		            num_goods += 1;
		        } 
			}
			
		} catch (SQLException e) 
		{
			e.printStackTrace();
		}
		finally
		{
			if(helper != null)
			{
				helper.close();
			}
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
