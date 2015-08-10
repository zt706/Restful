package br.com.restful.recommend;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import br.com.restful.recommend.JsonUtil;
import br.com.restful.util.ArticlePage;
import br.com.restful.util.CartRecommend;
import br.com.restful.util.CategoryAttentionlistRecomm;
import br.com.restful.util.HotSaleRecommend;
import br.com.restful.util.IndexPageRecommend;
import br.com.restful.util.JingZanInCategory;
import br.com.restful.util.JingZanInProduct;
import br.com.restful.util.NotFoundRecommend;
import br.com.restful.util.SearchRecommend;
import br.com.restful.util.SimilarGoods;
import br.com.restful.util.VipRecommend;

public class RecommendService extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	private static Properties dbProps = null;
	private static Properties solrProps = null;
	
	private static final String [] QUERYKEYARR = {
			"format",
			"callback", 
			"pagetype",
			"recomm",
			"goodsid",
			"generalid",
			"filterids",
			"keyword",
			"userid",
			"historyids",
			"categoryid",
			"articleid",
	};
	
	
	//重载doGet()方法
	 public void doGet(HttpServletRequest request, HttpServletResponse response)
		        throws IOException, ServletException

	 { 
		 	long start_time=System.currentTimeMillis();
	 
		 	// 只有资讯页才使用的变量
	        String recommendPidStringV2 = "";
	        boolean is_article_page = false;
	        
	        // 只有首页才使用的变量
	        String recommendPidStringV3 = "";
	        boolean is_index_page = false;
	        
		 	// 获得配置文件
	        dbProps = getProperties();
	        solrProps = getProperties();
		 	
	 		// 设置正确的 MIME 类型（application/json | application/jsonp）和字符编码
		 	if (request.getParameter("format") != null
			   && !request.getParameter("format").isEmpty()
			   && request.getParameter("format").equals("jsonp"))
		 	{
		 		response.setContentType("application/x-javascript;charset=UTF-8");
		 	}
		 	else
		 	{
		 		response.setContentType("application/json;charset=UTF-8");
		 	}
	        PrintWriter out = response.getWriter();
	        
	        Map<String, String> parameterMap = new HashMap<>();
	        /**
	         * 验证参数
	         */
	        boolean passVerify = false;
		    	
//		    parameterMap = request.getParameterMap();
	        
	        // 取出http请求的参数
	        for(String queryKey : QUERYKEYARR)
	        {
	        	String value_str = request.getParameter(queryKey);
	        	
	        	if (value_str != null && !value_str.isEmpty())
	        	{
	        		if (queryKey.equals("recomm"))
		        	{
	        			value_str =  value_str.toLowerCase();
		        	}
	        	}
	        	
	        	parameterMap.put(queryKey, value_str);
	        }
	        parameterMap.put("recommendpids","");
		    
		    if(parameterMap != null)
		    {
		    	// 检验pagetype recomm goodsid 三个必须的参数
		    	if(parameterMap.containsKey("pagetype")
		    			&& parameterMap.get("pagetype") != null
		    			&& parameterMap.containsKey("recomm")
		    			&& parameterMap.get("recomm") != null)
		    	{
		    		/*
		    		 * 商品详情页
		    		 */
		    		if(parameterMap.get("pagetype").equals("product")
		    			&& (parameterMap.get("goodsid") != null && isNum(parameterMap.get("goodsid")))
		    		  )
		    		{
		    			long goodsId = 0;
		    			
		    			if (parameterMap.get("goodsid") != null && !parameterMap.get("goodsid").isEmpty())
		    			{
		    				goodsId = Long.parseLong(parameterMap.get("goodsid"));
		    			}
		    			
		    			if(goodsId > 0)
		    			{
		    				String filterIds = "";
		    				String recommendPidString = "";
		    				
		    				// 检验过滤商品id
		    				if(parameterMap.containsKey("filterids") 
		    						&& parameterMap.get("filterids")!= null
		    						&& !parameterMap.get("filterids").isEmpty())
		    				{
		    					// 获得http请求中filterids字段的值
		    					filterIds = parameterMap.get("filterids");
		    				}
				    		
		    				// 获取商品详情页热销区块的推荐数据
		    				if(parameterMap.get("recomm").equals("hotsale"))
			    			{
			    				recommendPidString = HotSaleRecommend.getRecommendDate(dbProps, goodsId, filterIds);
			    			}
			    			else if(parameterMap.get("recomm").equals("similarrecomm"))
			    			{
			    				// 获取商品详情页下架商品区块的推荐数据
			    				recommendPidString = SimilarGoods.getSimilarGoods(dbProps, goodsId, filterIds);
			    			}
			    			else if(parameterMap.get("recomm").equals("jingzan"))
			    			{
			    				// 使用goodsid
			    				// 获取商品详情页jingzan区块的推荐数据
			    				recommendPidString = JingZanInProduct.getRecommendData(dbProps, goodsId, filterIds);
			    			}
		    				
		    				parameterMap.put("recommendpids",recommendPidString);
		    			}
		    		}
		    		else if (parameterMap.get("pagetype").equals("cart")
		    				&& parameterMap.get("recomm").equals("alsobuy")
		    				&& parameterMap.get("goodsid") != null )
		    		{
		    			/*
			    		 * 购物车的alsobuy区块
			    		 */
			    		
		    			String goodsids = parameterMap.get("goodsid");
		    			
		    			if (goodsids.length() > 0)
		    			{
		    				String filterids = "";
		    				
		    				// 检验过滤商品id
		    				if(parameterMap.containsKey("filterids") 
		    					&& parameterMap.get("filterids")!= null
		    					&& !parameterMap.get("filterids").isEmpty())
		    				{
		    					// 获得http请求中filterids字段的值
		    					filterids = parameterMap.get("filterids");
		    				}
		    				
		    				// 获取购物车页的推荐数据
		    				String recommendPidString = CartRecommend.getRecommendData(dbProps, goodsids, filterids);
		    				parameterMap.put("recommendpids",recommendPidString);
		    			}
					}
		    		else if(parameterMap.get("pagetype").equals("search")
		    				&& parameterMap.get("recomm").equals("alsobuywiththekey")
		    				&& parameterMap.get("keyword") != null 
		    				&& !(parameterMap.get("keyword")).isEmpty())
		    		{
		    			/*
		    			 * 搜索页面推荐
		    			 */
		    			String keyword = parameterMap.get("keyword");
		    			  
		    			if(keyword.length() > 0)
		    			{
		    				String filterIds = "";
		    				
		    				try
		    				{
		    					keyword =  java.net.URLDecoder.decode(keyword, "utf-8");
							} 
		    				catch (Exception e) 
		    				{
								System.out.println("搜索关键词解码异常");
							}
		    				
		    				// 检验过滤商品id
		    				if(parameterMap.containsKey("filterids")
		    						&& parameterMap.get("filterids")!= null 
		    						&& !parameterMap.get("filterids").isEmpty())
		    				{
		    					// 获得http请求中filterids字段的值
		    					filterIds = parameterMap.get("filterids");
		    				}
		    				
		    				// 获取购物车页的推荐数据
		    				String recommendPidString = SearchRecommend.getRecommProductList(solrProps, keyword, filterIds);
		    				parameterMap.put("recommendpids",recommendPidString);
		    			}
		    		}
		    		else if(parameterMap.get("pagetype").equals("vippage")
		    				&& parameterMap.get("recomm").equals("guesslike")
		    				&& parameterMap.get("userid") != null
		    				&& !parameterMap.get("userid").isEmpty()
		    				&& isNum(parameterMap.get("userid")))
		    		{
		    			/*
		    			 * vip页面推荐
		    			 */
		    			String userId = parameterMap.get("userid");
		    			
		    			if(userId.length() > 0)
		    			{
		    				String filterIds = "";
		    				
		    				if(parameterMap.containsKey("filterids")
		    						&& parameterMap.get("filterids") != null
		    						&& !parameterMap.get("filterids").isEmpty())
		    				{
		    					filterIds = parameterMap.get("filterids");
		    				}
		    				
		    				String recommendPidString = VipRecommend.getRecommendData(dbProps, userId, filterIds);
		    				parameterMap.put("recommendpids",recommendPidString);
		    			}
		    		}
		    		else if(parameterMap.get("pagetype").equals("404page")
			    			&& parameterMap.get("recomm").equals("recommend"))
			    	{
		    			/*
		    			 * 404页面推荐
		    			 */
	    			
		    			String filterIds = "";
		    			// String recommendPidString = "";
	    				
	    				if(parameterMap.containsKey("filterids")
	    						&& parameterMap.get("filterids") != null
	    						&& !parameterMap.get("filterids").isEmpty())
	    				{
	    					filterIds = parameterMap.get("filterids");
	    				}
	    				
	    				String userId = "";
	    				String historyIds = "";
	    				String recommendPidString = "";
	    				
	    				// 优先使用userid
		    			if (parameterMap.containsKey("userid") 
				    		&& parameterMap.get("userid") != null
				    		&& isNum(parameterMap.get("userid"))
				    		&& !parameterMap.get("userid").isEmpty())
			    		{
		    				userId = parameterMap.get("userid");
		    				
		    				recommendPidString = NotFoundRecommend.getRecommendData(dbProps, userId, historyIds, filterIds);
			    		}
		    			else if(parameterMap.containsKey("historyids")
				    		&& parameterMap.get("historyids") != null
				    		&& !parameterMap.get("historyids").isEmpty())
		    			{
		    				historyIds = parameterMap.get("historyids");
		    				
		    				recommendPidString = NotFoundRecommend.getRecommendData(dbProps, userId, historyIds, filterIds);
		    			}
		    			else
		    			{
		    				// userid historyid 都为空
		    				recommendPidString = NotFoundRecommend.getRecommendData(dbProps, userId, historyIds, filterIds);
		    			}
		    			
		    			// 存入推荐id
		    			parameterMap.put("recommendpids",recommendPidString);
			    	}
		    		else if (parameterMap.get("pagetype").equals("category")
		    				&& parameterMap.get("categoryid") != null 
		    				&& isNum((parameterMap.get("categoryid"))))
		    		{
		    			/*
		    			 * 列表页推荐
		    			 */
		    			int categoryId = Integer.parseInt(parameterMap.get("categoryid"));
		    			
		    			if (categoryId > 0)
		    			{
		    				String filterIds = "";
		    				String recommendPidString = "";
		    				
		    				// 检验过滤商品id
		    				if(parameterMap.containsKey("filterids") 
		    						&& parameterMap.get("filterids")!= null
		    						&& !parameterMap.get("filterids").isEmpty())
		    				{
		    					filterIds = parameterMap.get("filterids");
		    				}
		    				
		    				if (parameterMap.get("recomm").equals("jingzan"))
		    				{
		    					// 列表页晶赞推荐
		    					recommendPidString = JingZanInCategory.getRecommendData(dbProps, categoryId, filterIds);
		    				}
		    				else if (parameterMap.get("recomm").equals("attentionlist"))
		    				{
		    					// 列表页关注排行
		    					recommendPidString = CategoryAttentionlistRecomm.getAttentionGoods(dbProps, categoryId, filterIds);
		    				}
		    				
		    				// 存入推荐id
			    			parameterMap.put("recommendpids",recommendPidString);
		    			}
		    		}
		    		else if (parameterMap.get("pagetype").equals("article")
		    				&& parameterMap.get("articleid") != null 
		    				&& isNum((parameterMap.get("articleid"))))
		    		{
		    			/*
		    			 * 资讯页推荐
		    			 */
		    			
		    			int articleId = Integer.parseInt(parameterMap.get("articleid"));
		    			
		    			if (articleId > 0)
		    			{
		    				String filterIds = "";
		    				
		    				// 检验过滤商品id
		    				if(parameterMap.containsKey("filterids") 
		    						&& parameterMap.get("filterids")!= null
		    						&& !parameterMap.get("filterids").isEmpty())
		    				{
		    					filterIds = parameterMap.get("filterids");
		    				}
		    				
		    				if (parameterMap.get("recomm").equals("guesslike"))
		    				{
		    					// 资讯页的猜你喜欢
		    					//recommendPidStringV2 = ArticlePageRecommend.getArticleRecommendIds(dbProps, articleId, filterIds);
		    					recommendPidStringV2 = ArticlePage.getArticlePageRecommList(articleId);
		    					is_article_page = true;
		    				}
		    				
		    				// 存入推荐id
		    				//parameterMap.put("recommendpids",recommendPidStringV2);
		    			}
		    			
		    		}
		    		else if (parameterMap.get("pagetype").equals("indexpage")
		    				&& parameterMap.get("recomm").equals("indexguesslike"))
		    		{
		    			/*
		    			 * 首页推荐
		    			 */
		    			String pageType = parameterMap.get("pagetype");
		    			String recomm = parameterMap.get("recomm");
		    			
		    			String filterIds = "";
		    			String recommendPidString = "";
	    				
	    				if(parameterMap.containsKey("filterids")
	    					&& parameterMap.get("filterids") != null
	    					&& !parameterMap.get("filterids").isEmpty())
	    				{
	    					filterIds = parameterMap.get("filterids");
	    				}

	    				// userid可以没有内容,但是一定要写
	    				if ((parameterMap.containsKey("userid") 
					    	&& parameterMap.get("userid") != null)
					    	&&
					    	(parameterMap.containsKey("historyids") 
				    		&& parameterMap.get("historyids") != null
				    		))
	    				{
	    					String userId = "";
	    					String historyIds = "";
	    					
	    					if (isNum(parameterMap.get("userid")))
	    					{
	    						//userId = parameterMap.get("userid");
	    					}
	    					
	    					historyIds = parameterMap.get("historyids");
	    					
	    					recommendPidStringV3 = IndexPageRecommend.getRecommendData(dbProps, userId, historyIds, filterIds
	    											, pageType
	    											, recomm
	    											);
	    					is_index_page = true;
	    				}
		    		}
		    	}		    	
		    }
	        
	        /**
	         * 返回推荐数据或者提示信息
	         */
		   
		   String callback_str = "";
		   String json_str = "";
		   if (parameterMap.get("format") != null
			   && !parameterMap.get("format").isEmpty()
			   && parameterMap.get("format").equals("jsonp")
			   && parameterMap.get("callback") != null
			   && !parameterMap.get("callback").isEmpty())
		   {
			   ///RecommService/?pagetype=category&recomm=jingzan&categoryid=122
			   // &filterids=&format=jsonp&callback=jQuery172042133693560026586_1434361809080
			   
			   // jsonp 格式
			   callback_str = parameterMap.get("callback");
			   
			   if (is_article_page)
			   {
				   // 资讯页
				   json_str = callback_str + "(" + recommendPidStringV2 + ")";
			   }
			   else
			   {
				   json_str = callback_str + "(" + JsonUtil.object2json(parameterMap) + ")";
			   }
			   
		   }
		   else
		   {
			   // json 格式
			   
			   if(is_article_page)
			   {
				   json_str = recommendPidStringV2;
			   }
			   else if (is_index_page)
			   {
				   json_str = recommendPidStringV3;
			   }
			   else
			   {
				   json_str = JsonUtil.object2json(parameterMap);
			   }
		   }
			  
	       // 格式化为json/jsonp数据
	       out.write(json_str);
	       out.flush();
	       
	       long end_time=System.currentTimeMillis();
	       long time = end_time - start_time;
	       //System.out.println(parameterMap.get("pagetype") + "  used time ===  " + time);
	}
	 
	public static boolean isNum(String str)
	{
		if (str == null)
			return false;
		
		return str.matches("^[0-9]+$");
	}
	
	// 获得配置文件对象
	public static Properties getProperties()
	{
		return CommonTools.getProperties();
	}
	
	// 获取返回的参数列表
	public static String [] getQueryKeyArry()
	{
		return QUERYKEYARR;
	}
	
	public static void main(String [] args) throws SQLException
	{
		
		
	}
}
