package br.com.restful.util;

import net.sf.json.JSONObject;
import br.com.restful.cache.ImportArticleRecommStructIntoCache;
import br.com.restful.cache.MemcachedConnector;

public class ArticlePage 
{
	public static String getArticlePageRecommList(int articleId)
	{
		// 使用articleid从cache 中取出推荐的list
		String recomm_list_str = getRecommList(articleId + "");
		
		return recomm_list_str;
	}
	
	// 从104cache服务器上读出article对应的推荐信息
	private static String getRecommList(String articleId)
	{
		String cache_key = ImportArticleRecommStructIntoCache.getArticleCacheKey(articleId);
		String json_str = "";
		
		// if (cacheKey.equals("2015-6-9-16_tianmao"))
			// System.out.println("key == " + cacheKey + " value == " + MemcachedConnector.mcc.get(cacheKey));
		
		if(MemcachedConnector.mcc.get(cache_key) != null)
		{
			json_str = (String) MemcachedConnector.mcc.get(cache_key);
			
		}
		
		return json_str;
	}
}

