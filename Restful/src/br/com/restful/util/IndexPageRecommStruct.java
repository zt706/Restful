package br.com.restful.util;

import java.util.ArrayList;
import java.util.HashMap;

public class IndexPageRecommStruct 
{
	/*
	 *	首页推荐返回的推荐内容结构体
	 */
	private String Pagetype;
	private String Recomm;
	private String Goodsid;
	private String Generalid;
	private String Filterids;
	private String Keyword;
	private String Userid;
	private String Historyids;
	private String Categoryid;
	private String Articleid;
	private HashMap<String, ArrayList<HashMap<String, String>>> Recommendpids;
	
//	private ArrayList<HashMap<String, String>> Recomm_for_historyids;
//	private HashMap<String, String> Recomm_for_user_id;
	
	public IndexPageRecommStruct(
			String pagetype,
			String recomm,
			String goodsid,
			String generalid,
			String filterids,
			String keyword,
			String userid,
			String historyids,
			String categoryid,
			String articleid,
			HashMap<String, ArrayList<HashMap<String, String>>> recommendpids
			)
	{
		this.Pagetype = pagetype;
		this.Recomm = recomm;
		this.Goodsid = goodsid; 
		this.Generalid = generalid; 
		this.Filterids = filterids; 
		this.Keyword = keyword; 
		this.Userid = userid; 
		this.Historyids = historyids; 
		this.Categoryid = categoryid; 
		this.Articleid = articleid; 
		this.Recommendpids = recommendpids;	
	}

	public String getPagetype() {
		return Pagetype;
	}

	public void setPagetype(String pagetype) {
		Pagetype = pagetype;
	}

	public String getRecomm() {
		return Recomm;
	}

	public void setRecomm(String recomm) {
		Recomm = recomm;
	}

	public String getGoodsid() {
		return Goodsid;
	}

	public void setGoodsid(String goodsid) {
		Goodsid = goodsid;
	}

	public String getGeneralid() {
		return Generalid;
	}

	public void setGeneralid(String generalid) {
		Generalid = generalid;
	}

	public String getFilterids() {
		return Filterids;
	}

	public void setFilterids(String filterids) {
		Filterids = filterids;
	}

	public String getKeyword() {
		return Keyword;
	}

	public void setKeyword(String keyword) {
		Keyword = keyword;
	}

	public String getUserid() {
		return Userid;
	}

	public void setUserid(String userid) {
		Userid = userid;
	}

	public String getHistoryids() {
		return Historyids;
	}

	public void setHistoryids(String historyids) {
		Historyids = historyids;
	}

	public String getCategoryid() {
		return Categoryid;
	}

	public void setCategoryid(String categoryid) {
		Categoryid = categoryid;
	}

	public String getArticleid() {
		return Articleid;
	}

	public void setArticleid(String articleid) {
		Articleid = articleid;
	}

	public HashMap<String, ArrayList<HashMap<String, String>>> getRecommendpids() {
		return Recommendpids;
	}

	public void setRecommendpids(HashMap<String, ArrayList<HashMap<String, String>>> recommendpids) {
		Recommendpids = recommendpids;
	}

}
