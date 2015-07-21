package br.com.restful.util;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Properties;

import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;

import br.com.restful.recommend.CommonTools;

public class SearchRecommend 
{
	private final static int MAX_RECOMM_PRODUCT_NUM = 15;
	
	private final static String DEFAULT_RECOMM_PRODUCT = "69285;12288;39353;38677;76110";
	
	public static String getRecommProductList(Properties props, String query, String filterIds)
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
				
		System.out.println(" ++++ " + filterIds);
		
		String productInfoStr = "";
		ArrayList<String> productInfoList = new ArrayList<String>();
		
		HashSet<Long> filterIdSet = new HashSet<>();
		
		if(filterIds.length() > 0)
		{
			String[] tmpArr = filterIds.split(",");
			
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
		
		HashSet<Long> recommProductSet = getRecommProduct(props, query);
	
		if(recommProductSet != null && recommProductSet.size() > 0)
		{
			for (long pid : recommProductSet) 
			{
				if(!filterIdSet.contains(pid))
				{
					
					productInfoList.add(Long.toString(pid));
					
					if(productInfoList.size() >= MAX_RECOMM_PRODUCT_NUM)
					{
						break;
					}
					
				}
			}
		}
		
		if(productInfoList != null && productInfoList.size() > 0)
		{
			for(int i = 0; i < productInfoList.size(); i++)
			{
				if(i == 0)
				{
					productInfoStr += productInfoList.get(i);
				}
				else {
					productInfoStr += "," + productInfoList.get(i);
				}
			}
		}
		
		return productInfoStr;
	}
	
	private static HashSet<Long> getRecommProduct(Properties props, String query)
	{
		HashSet<Long> productSet = new LinkedHashSet<>();
		
		String recommStr = "";
		
		SolrjBase solrjBase = new SolrjBase();
		
		solrjBase.init(props);
		SolrDocumentList docList = solrjBase.query(query, 0, 1);
		
		if(docList.size() > 0)
		{
			for (SolrDocument doc : docList) {
				recommStr = (String)doc.get("recommv2");
			}
		}
		solrjBase.destory();
		
		if(recommStr != null && !recommStr.isEmpty())
		{
			recommStr += DEFAULT_RECOMM_PRODUCT;
		}
		else{
			recommStr = DEFAULT_RECOMM_PRODUCT;
		}
		
		String[] recommProductArr = recommStr.split(";");
		
		if(recommProductArr.length > 0)
		{
			for(String pidStr : recommProductArr)
			{
				if(pidStr != null && pidStr.length() > 0)
				{
					productSet.add(Long.parseLong(pidStr));
				}
			}
		}
		
		return productSet;
	}
	
	public static boolean isNum(String str)
	{
		return str.matches("^[0-9]+$");
	}
	
	public static void main(String[] args)
	{	
		 // SearchRecommend recommProduct = new SearchRecommend();
		 
		 HashSet<Long> existProductSet = new HashSet<Long>();
		 //existProductSet.add(31367);
		 
		 //String productInfoStr = getRecommProductList("补肾壮阳", "31367,25706");
		 
		 //System.out.println(productInfoStr);
	}
}
