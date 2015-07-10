package br.com.restful.util;

import java.io.IOException;
import java.util.Collection;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.response.UpdateResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.SolrInputDocument;

public class SolrjBase 
{
	// private static final String DEFAULT_URL = "http://10.0.22.105:8080/solr/collection1";
    private HttpSolrServer server;
 
    public void init(Properties props) 
    {
    	String DEFAULT_URL = parserDBProperties(props);
    	init(DEFAULT_URL);
    }
    
    public void init(String solrUrl) {
        server = new HttpSolrServer(solrUrl);
 
        server.setMaxRetries(1);
        server.setConnectionTimeout(1000);
 
        // socket read timeout
        server.setSoTimeout(1000); 
        server.setDefaultMaxConnectionsPerHost(100);
        server.setMaxTotalConnections(100);
        server.setFollowRedirects(false); 
 
        // allowCompression defaults to false.
        // Server side must support gzip or deflate for this to have any effect.
        server.setAllowCompression(true);
 
        // SolrJ lets you upload content in XML and Binary format.
        // The default is set to be XML.
        // Use the following to upload using Binary format.
        // This is the same format which SolrJ uses to fetch results, and can
        // greatly improve performance as it reduces XML marshalling overhead.
        // Note -- be sure you have also enabled the
        // "BinaryUpdateRequestHandler" in your solrconfig.xml for example like:
        // <requestHandler name="/update/javabin"
        // class="solr.BinaryUpdateRequestHandler" />
        // server.setRequestWriter(new BinaryRequestWriter());
    }
 
    public void destory() 
    {
        server = null;
        System.runFinalization();
        System.gc();
    }
 
    /**
     * 获取查询结果，直接打印输出
     * @param query
     */
    public SolrDocumentList query(String query, int start, int rows) {
    	
    	// 防止solr报错把所有特殊字符替换
    	String regEx="[`~!@#$%^&*()\\-+=|{}':;',\\[\\].<>/?\"\\\\~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？]";     
        Pattern   p   =   Pattern.compile(regEx);        
        Matcher   m   =   p.matcher(query);        
        query = m.replaceAll("").trim();
        
    	SolrDocumentList list = new SolrDocumentList();
    
        SolrQuery params = new SolrQuery(query);
        params.set("rows", rows);
        try 
        {
            QueryResponse response = server.query(params);
            list = response.getResults();
//            System.out.println("总计：" + list.getNumFound() + "条，本批次:" + list.size() + "条");
            
//            for (int i = 0; i < list.size(); i++) 
//            {
//                SolrDocument doc = list.get(i);
////                System.out.println(doc.get("query") + "\t" + doc.get("recomm"));
//            }
        } catch (SolrServerException e) {
            e.printStackTrace();
        }
        
        return list;
    }
 
    public void addDoc(SolrInputDocument doc) {
//        SolrInputDocument doc = new SolrInputDocument();
//        doc.addField("objectId", 0);
//        doc.addField("webTitle", "测试标题");
//        doc.addField("webTime", new java.util.Date());
//        doc.addField("webContent", "这是一条测试内容");
        try {
            UpdateResponse response = server.add(doc);
            server.commit();
 
            System.out.println("Query Time：" + response.getQTime());
            System.out.println("Elapsed Time：" + response.getElapsedTime());
            System.out.println("Status：" + response.getStatus());
        } 
        catch (SolrServerException e) 
        {
        	e.printStackTrace();
        } 
        catch (IOException e) 
        {
        	e.printStackTrace();
        }
    }
    
    public void deleteAll() 
    {
    	try {
    		server.deleteByQuery("*:*");
    		server.commit();
		}
    	catch (SolrServerException e) 
    	{
    		// TODO Auto-generated catch block
    		e.printStackTrace();
		} 
    	catch (IOException e) 
    	{
			// TODO Auto-generated catch block
    		e.printStackTrace();
    	}
	}
 
    public void addDocs(Collection<SolrInputDocument> docs) 
    {
        try 
        {
            UpdateResponse response = server.add(docs);
            server.commit();
            
            System.out.println("Query Time：" + response.getQTime());
            System.out.println("Elapsed Time：" + response.getElapsedTime());
            System.out.println("Status：" + response.getStatus());
        } 
        catch (SolrServerException e) 
        {
            e.printStackTrace();
        } 
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    
    // 解析solr配置文件
 	private static String parserDBProperties(Properties props)
 	{
 		
 		String solr_str = props.getProperty("default_solr_url");
 		
 		return solr_str;
 	}
 	
 	
//    @Test
//    public void queryCase() {
//        // 这是一个稍复杂点的查询
// 
//        SolrQuery params = new SolrQuery("苏州");
////        params.set("q.op", "OR");
//        params.set("start", 0);
//        params.set("rows", 4);
////        params.set("fl", "*,score");
////        params.setIncludeScore(true);
////        params.set("sort", "webTime desc");
// 
//        params.setHighlight(true); // 开启高亮组件
//        params.addHighlightField("webTitle");// 高亮字段
//        params.addHighlightField("webContent");// 高亮字段
////        params.set("hl.useFastVectorHighlighter", "true"); 
//        params.set("hl.fragsize", "200");
////        params.setHighlightSimplePre("<SPAN class=\"red\">");// 高亮关键字前缀；
////        params.setHighlightSimplePost("</SPAN>");// 高亮关键字后缀
//        params.setHighlightSnippets(2); //结果分片数，默认为1
// 
//        try {
//            QueryResponse response = server.query(params);
// 
//            // 输出查询结果集
//            SolrDocumentList list = response.getResults();
//            System.out.println("总计：" + list.getNumFound() + "条，本批次:" + list.size() + "条");
//            for (int i = 0; i < list.size(); i++) {
//                SolrDocument doc = list.get(i);
////                System.out.println(doc.get("webTitle"));
//            }
// 
//            // 第一种：常用遍历Map方法；
//            Map<String, Map<String, List<String>>> map = response.getHighlighting();
//            Iterator<String> iterator = map.keySet().iterator();
//            while(iterator.hasNext()) {
//                String keyname = (String) iterator.next();
//                Map<String, List<String>> keyvalue = map.get(keyname);
//                System.out.println("objectId：" + keyname);
// 
//                // 第二种：JDK1.5之后的新遍历Map方法。
//                for (Map.Entry<String, List<String>> entry : keyvalue.entrySet()) {
//                    String subkeyname = entry.getKey().toString();
//                    List<String> subkeyvalue = entry.getValue();
// 
//                    System.out.print(subkeyname + "\n");
//                    for(String str: subkeyvalue) {
//                        System.out.print(str);
//                    }
//                    System.out.println();
//                }                
//            }
//        } catch (SolrServerException e) {
//            e.printStackTrace();
//        } 
//    }
}