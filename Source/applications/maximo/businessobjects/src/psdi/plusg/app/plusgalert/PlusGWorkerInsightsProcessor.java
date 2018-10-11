/*
 *
 * IBM Confidential
 *
 * OCO Source Materials
 *
 * 5724-U18
 *
 * (C) COPYRIGHT IBM CORP. 2016
 *
 * The source code for this program is not published or otherwise
 * divested of its trade secrets, irrespective of what has been
 * deposited with the U.S. Copyright Office.
 *
 */

package psdi.plusg.app.plusgalert;

import java.rmi.RemoteException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.ibm.json.java.JSONArray;
import com.ibm.json.java.JSONObject;
import com.ibm.tivoli.maximo.fdmbo.DefaultResourceProcessor;
import com.ibm.tivoli.maximo.oslc.OslcUtils;

import psdi.iface.mic.MicConstants;
import psdi.iface.router.Router;
import psdi.iface.router.RouterHandler;
import psdi.mbo.MboRemote;
import psdi.mbo.MboSetRemote;
import psdi.mbo.SqlFormat;
import psdi.security.UserInfo;
import psdi.server.MXServer;
import psdi.util.MXApplicationException;
import psdi.util.MXException;
import psdi.util.logging.MXLogger;
import psdi.util.logging.MXLoggerFactory;


public class PlusGWorkerInsightsProcessor extends DefaultResourceProcessor 
{
	protected static final MXLogger workerinsightsprocessor = MXLoggerFactory.getLogger(MicConstants.INTEGRATION_LOGGER);

	public PlusGWorkerInsightsProcessor() throws MXException, RemoteException 
	{
		super();
	}
	
	public void syncAlertsReadings(java.util.Date lastRun, UserInfo userInfo, String initTime, String allowedAlertTypeList, String alertTypeToIncidentList)	throws MXException, RemoteException
	{		
		Date timeToRun = lastRun;
		String updatedAtTime = "";
		
		if (timeToRun!=null){
			long time = timeToRun.getTime();
			updatedAtTime = String.valueOf(time);
		}else{
			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");		  
			try {
				Date parsedDate = dateFormat.parse(initTime);
				Timestamp timestamp = new java.sql.Timestamp(parsedDate.getTime());
				updatedAtTime  = String.valueOf(timestamp.getTime());			
			} catch (ParseException e) {
				workerinsightsprocessor.error("Date could not be parsed.", e);
			}			
		}
	
		List<String> listOfAlertTypes = Arrays.asList(allowedAlertTypeList.toLowerCase().split("\\s*,\\s*"));
		
		RouterHandler workerInsightsHandler = Router.getHandler("WORKERINSIGHTS");
		
		Map<String, Object> inputParam = new HashMap<String, Object>();		
		inputParam.put("includeDocs", true);
		inputParam.put("updatedAt", updatedAtTime);
		byte[] bytedata = workerInsightsHandler.invoke(inputParam, null);
		JSONObject alertsJsonObject = OslcUtils.bytesToJSONObject(bytedata);
		
		if(alertsJsonObject.get("error")!=null || alertsJsonObject.get("totalItems")==null){
			throw  new MXApplicationException("workerinsightserror ",(String) alertsJsonObject.get("reason"));
		}
		
		long itemSize = (long) alertsJsonObject.get("totalItems");
		
		if(itemSize>0){
			
			String targetMbo = "PLUSGALERTS"; 
			
			MboSetRemote safetyAlertNewSet = (MXServer.getMXServer().getMboSet(targetMbo, userInfo));
			
			((PlusGAlertsSet) safetyAlertNewSet).setAllowedAlertTypeToIncident();
			
			JSONArray typesArray = (JSONArray)alertsJsonObject.get("items");
			
			if(typesArray == null){
				workerinsightsprocessor.error("There are no items inside hazards json");
				return;
			}
			int typesSize = typesArray.size();
			for(int i=0;i<typesSize;i++)
			{
				JSONObject jo = (JSONObject)typesArray.get(i);
				
				MboSetRemote safetyAlertSearchSet = (MXServer.getMXServer().getMboSet(targetMbo, userInfo));
				SqlFormat sql2 = new SqlFormat(safetyAlertSearchSet.getUserInfo(), " alertid=:1");
				sql2.setObject(1, targetMbo, "alertid", (String) jo.get("_id"));
				safetyAlertSearchSet.setWhere(sql2.format());
				safetyAlertSearchSet.reset();
								
				JSONObject joActParam = (JSONObject) jo.get("actionParams");					
				
				JSONObject joExtra = (JSONObject) joActParam.get("extra");
				JSONObject riskLevel = (JSONObject) joExtra.get("riskLevel");	
				
				JSONObject location = (JSONObject) jo.get("location");
				
				if(riskLevel!=null && (listOfAlertTypes.contains("all")||listOfAlertTypes.contains(((String) riskLevel.get("title")).toLowerCase()))){
					
					MboRemote safetyAlert = null;
					if(safetyAlertSearchSet.count()==0){					
						safetyAlert = safetyAlertNewSet.add();
						
						safetyAlert.setValue("alertid", (String) jo.get("_id"));
						safetyAlert.setValue("alertsource", "WORKERINSIGHTS");
						safetyAlert.setValue("updatedat", (Long) jo.get("updatedAt"));	
						
						if(joActParam.get("hazardTitle")!=null){
							safetyAlert.setValue("alertdesc", (String) joActParam.get("hazardTitle"));	
						}						
						if(riskLevel.get("title")!=null){
							safetyAlert.setValue("alerttitle", (String) riskLevel.get("title"));	
						}						
						if(riskLevel.get("level")!=null){
							safetyAlert.setValue("alertlevel", (Long) riskLevel.get("level"));	
						}
						
						if(jo.get("type")!=null){
							safetyAlert.setValue("alerttype", (String) jo.get("type"));	
						}						
						if(jo.get("userId")!=null){
							safetyAlert.setValue("username", (String) jo.get("userId"));
						}						
						if(jo.get("shieldId")!=null){
							safetyAlert.setValue("shield", (String) jo.get("shieldId"));
						}						
						if(jo.get("timestamp")!=null){
							safetyAlert.setValue("timestamp", (Long) jo.get("timestamp"));	
						}						
						if(jo.get("createdAt")!=null){
							safetyAlert.setValue("createdat", (Long) jo.get("createdAt"));	
						}						
						if(location!=null){
							safetyAlert.setValue("latitudey", (Double) location.get("latitude"));	
							safetyAlert.setValue("longitudex", (Double) location.get("longitude"));	
						}						
					
						((PlusGAlertsRemote)safetyAlert).getThisMboSet().save();
					}	
					
				}else{
					workerinsightsprocessor.info("This alert is not configured to be recorded.");
				}
			}
			
		}else{
			workerinsightsprocessor.info("No new alerts from WorkerInsights.");
		}	
		
	}
		
}