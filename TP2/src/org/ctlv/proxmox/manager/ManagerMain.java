package org.ctlv.proxmox.manager;

import org.ctlv.proxmox.api.ProxmoxAPI;

public class ManagerMain {

	public static void main(String[] args) throws Exception {

		ProxmoxAPI api = new ProxmoxAPI();
		Controller controle = new Controller(api);
		Analyzer analiste = new Analyzer(api, null);
		Monitor Olivier = new Monitor(api, analiste); 
		Olivier.run();
	}

}