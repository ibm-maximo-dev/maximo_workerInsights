/*
 * IBM Confidential
 *
 * OCO Source Materials
 *
 * 5724-R54
 *
 * (C) COPYRIGHT IBM CORP. 2013
 *
 * The source code for this program is not published or otherwise
 * divested of its trade secrets, irrespective of what has been
 * deposited with the U.S. Copyright Office.
 */
package psdi.plusg.app.plusgalert;

import java.rmi.RemoteException;
import java.util.Arrays;
import java.util.List;

import psdi.app.system.CrontaskParam;
import psdi.app.system.CrontaskParamSet;
import psdi.mbo.Mbo;
import psdi.mbo.MboServerInterface;
import psdi.mbo.MboSet;
import psdi.mbo.SqlFormat;
import psdi.server.MXServer;
import psdi.util.MXException;

import com.ibm.tivoli.maximo.oilandgas.mbo.PlusGBasicMboSet;

/**
 * Alert Mbo Set.
 * @author apbram
 */
public class PlusGAlertsSet extends PlusGBasicMboSet implements PlusGAlertsSetRemote {

	List<String> listOfAllowedAlertTypes = null;
	
	/**
	 * Constructor.
	 * @param ms MboServerInterface
	 * @throws RemoteException when invoking super class constructor
	 */
	public PlusGAlertsSet(MboServerInterface ms) throws RemoteException {
		super(ms);
	}

	/**
	 * Creates a new {@link PlusGLocationAudit} instance.
	 * @param ms MboSet that will be associated with the new Mbo.
	 * @return {@link PlusGLocationAudit} new instance.
	 * @throws MXException when creating the new mbo instance.
	 * @throws RemoteException when creating the new mbo instance.
	 */
	@Override
	protected Mbo getMboInstance(MboSet ms) throws MXException, RemoteException {
		return new PlusGAlerts(ms);
	}
	
	public List<String> getAllowedAlertType(){
		return listOfAllowedAlertTypes;
	}
	
	public void setAllowedAlertTypeToIncident() throws MXException, RemoteException {
		
		SqlFormat sqf = new SqlFormat("crontaskname=:1 and parameter=:2");
		try {
			sqf.setObject(1, "CRONTASKPARAM", "crontaskname", "WORKERINSIGHTS");
			sqf.setObject(2, "CRONTASKPARAM", "parameter", "ALERT_TO_INCIDENT");
			CrontaskParamSet cronParamSet = (CrontaskParamSet)MXServer.getMXServer().getMboSet("CRONTASKPARAM", getUserInfo());
			cronParamSet.resetQbe();
			cronParamSet.setQbe("crontaskname", "="+"WORKERINSIGHTS"); 
			cronParamSet.reset();
			
			CrontaskParam cronParam = (CrontaskParam)cronParamSet.getMbo(0);
			
			listOfAllowedAlertTypes = Arrays.asList(cronParam.getString("value").toLowerCase().split("\\s*,\\s*"));
			
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	
}
