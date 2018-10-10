/*
 *
 * IBM Confidential
 *
 * OCO Source Materials
 *
 * 5724-U18
 *
 * (C) COPYRIGHT IBM CORP. 2017,2018
 *
 * The source code for this program is not published or otherwise
 * divested of its trade secrets, irrespective of what has been
 * deposited with the U.S. Copyright Office.
 *
 */

package psdi.plusg.app.plusgalert;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.util.EntityUtils;

import com.ibm.tivoli.maximo.watson.analytics.util.CloudEnv;

import psdi.iface.mic.MaxEndPointInfo;
import psdi.iface.router.BaseRouterHandler;
import psdi.iface.router.RouterPropsInfo;
import psdi.util.MXApplicationException;
import psdi.util.MXException;

/**
 * WorkerInsights integration
 *
 * This Class extends the BaseRouterHandler class. 
 * 
 * @version 1.0
 */
public class PlusGWorkerInsightsHandler extends BaseRouterHandler
{
	private static List<RouterPropsInfo> props = new ArrayList<RouterPropsInfo>(7);

	public static final String URL = "URL";
			
	//Get TENANT 
	public static final String TENANT = "TENANT";
	
	//Get authtoken
	public static final String AUTHTOKEN = "AUTHTOKEN";
    
	static
	{
		props.add(new RouterPropsInfo(URL));
		props.add(new RouterPropsInfo(TENANT));		
		props.add(new RouterPropsInfo(AUTHTOKEN));	
	}
		
	/**
	 * @param endPointInfo
	 */
	public PlusGWorkerInsightsHandler(MaxEndPointInfo endPointInfo)
    {
        super(endPointInfo);
    }
    
    /**
     * 
     */
    public PlusGWorkerInsightsHandler()
    {
        super();
    }
    
    /**
	 * Get the HTTP Basic Authentication header
	 * 
	 * @param username user name
	 * @param password password
	 * @return Base64 encoded HTTP Basic Authentication header
	 */
	private static String getAuthHeader(String authtoken)
	{			
			
		return "Bearer " + authtoken;
	}
    
    
    /* (non-Javadoc)
     * @see psdi.iface.router.BaseRouterHandler#invoke(java.util.Map, byte[])
     */
    public byte[] invoke(Map metaData, byte[] data)
		throws MXException
    {
    	data = super.invoke(metaData, data);
    	    	
		HttpClient httpclient = CloudEnv.createHTTPSSLClient(CloudEnv.createHTTPClient());
	
		 HttpGet request = new HttpGet(getUrl(metaData));
		 request.addHeader ("Authorization",getAuthHeader(getAUTHTOKEN()));	
		 
		try
	    {
			HttpResponse response = httpclient.execute(request);
		    HttpEntity resEntity = response.getEntity();
		    String responseString = EntityUtils.toString(resEntity, "UTF-8");
		    return responseString.getBytes("utf-8");
	    }
	    catch(Exception e)
	    {
	     	throw new MXApplicationException("iface","workerinsightserror",e);
	    }
    }
    
 	/* (non-Javadoc)
	 * @see psdi.iface.router.RouterHandler#getProperties()
	 */
	public List<RouterPropsInfo> getProperties()
	{
		return props;
	}

	/**
	* @param metaData 
	 * @return url to connect to.
	 * @throws MXApplicationException 
	 * @throws URISyntaxException 
	*/
    public String getUrl(Map metaData) throws MXApplicationException
    {
//    	"https://ioti-stage1.us-south.containers.mybluemix.net/api/v1/te_3pj67k0r/hazards?includeDocs=true&updatedAt=1530465485000";
    	String timeUpdated = (String) metaData.get("updatedAt"); 
    	try {    		
			return new URIBuilder(getPropertyValue(URL)).addParameter("updatedAt", timeUpdated).addParameter("descending", "false").build().toString();
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			throw new MXApplicationException("workerinsights","urlerror",e);
		}

    }
    
	/**
	* @return api username
	*/
    public String getAUTHTOKEN()
    {
        return getPropertyValue(AUTHTOKEN);
    }    
    
    /**
	* @return api tenant
	*/
    public String getTENANT()
    {
        return getPropertyValue(TENANT);
    }     
   
   
}
