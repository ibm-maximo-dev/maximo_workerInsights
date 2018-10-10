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
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

import com.ibm.tivoli.maximo.oilandgas.mbo.PlusGBasicStatelessMbo;
import com.ibm.tivoli.maximo.oilandgas.mbo.strategy.PlusGMboBehaviorFactory;

import psdi.mbo.MboRemote;
import psdi.mbo.MboSet;
import psdi.mbo.MboSetRemote;
import psdi.server.MXServer;
import psdi.util.MXException;

/**
 * 
 * @author apbram
 */
public class PlusGAlerts extends PlusGBasicStatelessMbo implements PlusGAlertsRemote {


	/**
	 * Constructor.
	 * @param ms MboSet that will be associated to this Mbo.
	 * @throws RemoteException when invoking super constructor.
	 */
	public PlusGAlerts(MboSet ms) throws RemoteException {
		super(ms);
		
		setFactoryClass(new PlusGMboBehaviorFactory());		
	}

	@Override
	public void add() throws MXException, RemoteException {
	}
	
	protected void setAttributesBehavior() {
	}

	@Override
	protected void setDuplicableRelationships() {
	}

	@Override
	protected void setAttributeDependency() {

	}
	
	@Override
	protected void save() throws MXException, RemoteException {
		
		List<String> listOfAlertTypes = ((PlusGAlertsSet) this.getThisMboSet()).getAllowedAlertType();	
		
		if( listOfAlertTypes==null){
			((PlusGAlertsSet) this.getThisMboSet()).setAllowedAlertTypeToIncident();			
			listOfAlertTypes = ((PlusGAlertsSet) this.getThisMboSet()).getAllowedAlertType();	
		}

	
		if(listOfAlertTypes.contains("all")|| listOfAlertTypes.contains(this.getString("alerttitle").toLowerCase())){
			
			MboSetRemote incidentSet = (MboSet) (MXServer.getMXServer().getMboSet("INCIDENT", this.getUserInfo()));
			
			MboRemote incMbo = incidentSet.add();
			incMbo.setValue("description", this.getString("alertdesc"));
			incMbo.setValue("affectedperson", this.getString("username"));
			incMbo.setValue("plusgalertid", this.getString("alertid"));
			
			Timestamp stamp = new Timestamp(this.getLong("updatedat"));
			Date date = new Date(stamp.getTime());
			
			incMbo.setValue("reportdate", date);
			
			incMbo.setValue("reportedby", "WORKERINSIGHTS");
			
			if(this.getString("alertsource").equalsIgnoreCase("ENVIRONMENT")){
				incMbo.setValue("PLUSGINCIDENTGROUP", "ENVIRONMENTAL");
			}else if (this.getString("alertsource").equalsIgnoreCase("WORKERINSIGHTS")){
				incMbo.setValue("PLUSGINCIDENTGROUP", "INJURY");
			}
			
			incMbo.setValue("PLUSGINCTYPE", "HIGH POTENTIAL");
			
			incMbo.setValue("REPORTEDPRIORITY", 1);
			incMbo.setValue("INTERNALPRIORITY", 1);
			
			incidentSet.save();
		}
		
		super.save();		
	}
	
}
