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
import psdi.app.system.CrontaskInstanceRemote;
import psdi.app.system.CrontaskParamInfo;
import psdi.iface.mic.MicConstants;
import psdi.server.MXServer;
import psdi.server.SimpleCronTask;
import psdi.util.MXException;
import psdi.util.logging.MXLogger;
import psdi.util.logging.MXLoggerFactory;


/**
 * @author apbram
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class PlusGWorkerInsightsCronTask extends SimpleCronTask
{
	private static CrontaskParamInfo[] params = null;

	protected static final MXLogger integrationLogger = MXLoggerFactory.getLogger(MicConstants.INTEGRATION_LOGGER);
	boolean stopOnError = true;
	private String qualifiedInstanceName = null;
	
	static
	{
		params = new CrontaskParamInfo[4];
		
		params[0] = new CrontaskParamInfo();
		params[0].setName("ALLOWED_ALERT");
		params[1] = new CrontaskParamInfo();
		params[1].setName("ALERT_TO_INCIDENT");
		params[2] = new CrontaskParamInfo();
		params[2].setName("PROCESSOR");
		params[2].setDefault("psdi.plusg.app.plusgsi.PlusGWorkerInsightsProcessor");		
		params[3] = new CrontaskParamInfo();
		params[3].setName("INIT_TIME");
	}
	// does nothing.
	public void start()
	{
		try
		{
			cronAction();
//			setSleepTime(0);//will call cronAction immediately
		}
		catch(Exception e)
		{
			if(integrationLogger.isErrorEnabled())
			{
				integrationLogger.error(e.getMessage(), e);
			}
		}
	}

	public void cronAction()
	{
       
	    try
	    {
	        integrationLogger.info("cronAction start for getting alerts crontask "+qualifiedInstanceName+
	            " for server "+MXServer.getMXServer().getName());
	        	        
	        String alertProcessor = getParamAsString("PROCESSOR");
			if (alertProcessor == null || alertProcessor.equals(""))
			{
				alertProcessor = "psdi.plusg.app.plusgsi.PlusGWorkerInsightsProcessor";
			}
	        
			PlusGWorkerInsightsProcessor processor = (PlusGWorkerInsightsProcessor)Class.forName(alertProcessor).newInstance();
			processor.syncAlertsReadings(getLastRunDate(), MXServer.getMXServer().getUserInfo("MAXADMIN"), getParamAsString("INIT_TIME"), getParamAsString("ALLOWED_ALERT"), getParamAsString("ALERT_TO_INCIDENT"));
	    }
		catch(Exception e)
		{
		    integrationLogger.error(e.getMessage(), e);
		} 
	    finally
	    {
	        try
	        {
	            integrationLogger.info("cronAction end for Integration crontask "+qualifiedInstanceName+" for server "+MXServer.getMXServer().getName());
		    }
			catch(RemoteException e)
			{
			    integrationLogger.error(e.getMessage(), e);
			}
	    }

	}

	public CrontaskParamInfo[] getParameters() throws MXException, RemoteException
	{
		return params;
	}

	public void setCrontaskInstance(CrontaskInstanceRemote inst)
	{
		try
		{
			super.setCrontaskInstance(inst);
			qualifiedInstanceName = inst.getString("crontaskname")+"."+inst.getString("instancename");
		}
		catch(Exception e)
		{
			integrationLogger.error(e.getMessage(), e);
		}
	}
}
