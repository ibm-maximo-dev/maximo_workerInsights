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

import psdi.mbo.MboSetRemote;
import psdi.util.MXException;

/**
 * Alert Mbo Set Remote Interface.
 * @author apbram
 */
public interface PlusGAlertsSetRemote extends MboSetRemote {
	
	public void setAllowedAlertTypeToIncident() throws MXException, RemoteException;
}
