package br.com.restful.cache;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.Date;

import net.sf.json.JSONObject;

/*
 *  从 10.0.22.106:1522 读出数据
 *  写入 10.0.22.104:11311 cache 服务器
 *  作为资讯页的推荐商品信息
 *  每个商品一条缓存记录
 */

public class ImportProductInfoIntoCache 
{
	// memcache 中商品id所对应推荐对象的 cache key 前缀
	private static final String CACHE_KEY_PRE = "product_info_";
		
	private static final String queryKadSalseSql_4 = "select "
			+" a.WareGeneralName,a.BrandCode,b.wareskucode,b.dyid,b.WARENAME,c.pic180,c.pic800,c.saleprice,a.isrx,"
			+ " a.ManufacturerName as producername, a.MODEL as spec,ATTR_OTCTYPE.WAREATTRVALUE as otctype "
			+ " from  "
			+ " BASE.wi_wareinfo a " 
			+ " inner join "
			+ " BASE.wi_waresku b " 
			+ " on a.productcode=b.productcode "
			+ " inner join BASE.wi_wareskupc c "
			+ " on b.wareskucode=c.wareskucode "
			+ " LEFT JOIN (SELECT ATR.PRODUCTCODE,ATR.WAREATTRVALUE FROM BASE.WI_ATTRVALUESTORE ATR WHERE ATR.WAREATTRCODE='285280' ) ATTR_OTCTYPE "
			+ " ON a.PRODUCTCODE=ATTR_OTCTYPE.PRODUCTCODE "
			+ " where  "
			+ " b.ispcrelease=1 "
			+ " and "
			+ " b.ISSHELFDOWNPC = 0 "
			+ " AND "
			+ " c.ISINDEX = 1 "; 
	
	private Connection getBIOracle106Conn() throws SQLException
	{
		String db_106_conn_str = "jdbc:oracle:thin:@10.0.22.106:1522:kaddw02";
		// String dbName = "kaddw02";
		
		String db_106_user_name = "bidev";
		String db_106_password = "bi2015dev";
		
		Connection connection = null;
		try
		{
			  if(connection == null || connection.isClosed())
			  {
				   // Class.forName("oracle.jdbc.driver.OracleDriver");
				   connection= DriverManager.getConnection(db_106_conn_str, db_106_user_name, db_106_password); 
			  }
		}
		catch (SQLException e) 
		{
		   e.printStackTrace();
		}
		  
		return connection;
	}
	
	// 从 oracle中获取商品信息
	// 并存入cache
	private void getProductInfo(Connection connection) throws SQLException
	{
		PreparedStatement pState = connection.prepareStatement(queryKadSalseSql_4);
		
		System.out.println(queryKadSalseSql_4);
		
		ResultSet rs = pState.executeQuery();
		
		while (rs.next()) 
		{
			long ware_sku_code = rs.getLong("wareskucode");
			
			int item_id = rs.getInt("dyid");
			String product_thumb = (rs.getString("pic180") == null ? "" : rs.getString("pic180"));
			String product_thumb_big = (rs.getString("pic800") == null ? "" : rs.getString("pic800"));
			if (product_thumb_big.length() > 5)
			{
				product_thumb_big = product_thumb_big + "_230x230.jpg";
			}
			
			String title = (rs.getString("WARENAME") == null ? "" : rs.getString("WARENAME"));
			String spec = rs.getString("spec");
			
			float price = rs.getFloat("saleprice");
			DecimalFormat decimalFormat=new DecimalFormat(".00");
			String product_price_str = decimalFormat.format(price);
			
			String usgae_dosage = " "; 
			String product_intro =  " "; 
			String producter_name = (rs.getString("producername") == null ? "" : rs.getString("producername"));
			String product_code = " ";
			int is_rx = rs.getInt("isrx");
			
			String general_name = (rs.getString("WareGeneralName") == null ? "" : rs.getString("WareGeneralName")); 
			String brand_code = (rs.getString("BrandCode") == null ? "" : rs.getString("BrandCode")); 
			String otc_type = (rs.getString("otctype") == null ? "" : rs.getString("otctype")); 
			
			ProductObj productObj = new ProductObj(
										  item_id
										, product_thumb
										, product_thumb_big
										, title
										, spec
										, product_price_str
										, usgae_dosage
										, product_intro
										, producter_name
										, product_code
										, is_rx
										, general_name
										, brand_code
										, otc_type
										);
			
			// 生成cache key
			String cache_key = getCacheKey(ware_sku_code + "");
			// 塞入缓存
			ImportInfoIntoCache(productObj, cache_key);
		} 
	}
	
	// 
	public static String getCacheKey(String wareSkuCode)
	{
		String key = CACHE_KEY_PRE + wareSkuCode;
		
		return key;
	}
	
	// 设置从当前开始计算的过期时间
	private static final int EXPIRE_TIME = 30 * 3600 * 1000; 
	
	// 将 KadSale 对象存入缓存
	public static void ImportInfoIntoCache(ProductObj productObj, String cache_key) throws SQLException
	{
		
		JSONObject jsonObject = JSONObject.fromObject(productObj);
		Date expireDate = new Date(System.currentTimeMillis() + EXPIRE_TIME);
		
		System.out.println("缓存key == " + cache_key);
		// System.out.println("huan cun value == " + jsonObject.toString());
		
		if(MemcachedConnector.mcc.get(cache_key) != null)
		{
			// 更新缓存服务器中的内容
			MemcachedConnector.mcc.replace(cache_key, jsonObject.toString(), expireDate);
		}
		else
		{
			// 添加新内容到缓存服务器中
			 MemcachedConnector.mcc.add(cache_key, jsonObject.toString(), expireDate);
			
		}
	}
	
	
	public static void main(String [] args) throws SQLException
	{
		ImportProductInfoIntoCache imp = new ImportProductInfoIntoCache();
		
		Connection connect = imp.getBIOracle106Conn();
		
		imp.getProductInfo(connect);
		
	}
		
}
