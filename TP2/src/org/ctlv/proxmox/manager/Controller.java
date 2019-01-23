package org.ctlv.proxmox.manager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.security.auth.login.LoginException;

import org.ctlv.proxmox.api.ProxmoxAPI;
import org.ctlv.proxmox.api.data.LXC;
import org.ctlv.proxmox.api.data.Node;
import org.json.JSONException;

public class Controller {

	ProxmoxAPI api;
	public Controller(ProxmoxAPI api){
		this.api = api;
	}
	
	// migrer un conteneur du serveur "srcServer" vers le serveur "dstServer"
	public void migrateFromTo(String ctID, String srcServer, String dstServer) throws LoginException, JSONException, IOException, InterruptedException  {
		
		Node source = api.getNode(srcServer);
		Node destination = api.getNode(dstServer);
		
		api.migrateCT(srcServer, ctID, dstServer);
		TimeUnit.SECONDS.sleep(5);
		
	}

	// arr�ter le plus vieux conteneur sur le serveur "server"
	public void offLoad(String server) throws LoginException, JSONException, IOException, InterruptedException {
		long maxValue = 0;
		String idVieux = null;
		List<LXC> listeCT = api.getCTs(server);
		
		for(LXC ct : listeCT) {
			long newUptime = ct.getUptime();
			if(newUptime > maxValue);
			maxValue = newUptime;
			idVieux = ct.getVmid();
		}
		
		api.stopCT(server, idVieux);
		TimeUnit.SECONDS.sleep(3);
	}

}
