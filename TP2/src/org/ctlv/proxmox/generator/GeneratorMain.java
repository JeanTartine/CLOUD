package org.ctlv.proxmox.generator;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import javax.security.auth.login.LoginException;

import org.ctlv.proxmox.api.Constants;
import org.ctlv.proxmox.api.ProxmoxAPI;
import org.ctlv.proxmox.api.data.LXC;
import org.ctlv.proxmox.api.data.Node;
import org.json.JSONException;

import com.sun.corba.se.impl.orbutil.closure.Constant;

public class GeneratorMain {
	
	static Random rndTime = new Random(new Date().getTime());
	public static int getNextEventPeriodic(int period) {
		return period;
	}
	public static int getNextEventUniform(int max) {
		return rndTime.nextInt(max);
	}
	public static int getNextEventExponential(int inv_lambda) {
		float next = (float) (- Math.log(rndTime.nextFloat()) * inv_lambda);
		return (int)next;
	}
	
	public static void main(String[] args) throws InterruptedException, LoginException, JSONException, IOException {
		
	
		long baseID = Constants.CT_BASE_ID;
		int lambda = 30;
		
		
		Map<String, List<LXC>> myCTsPerServer = new HashMap<String, List<LXC>>();

		ProxmoxAPI api = new ProxmoxAPI();
		Random rndServer = new Random(new Date().getTime());
		Random rndRAM = new Random(new Date().getTime()); 
		
		long memAllowedOnServer1 = (long) (api.getNode(Constants.SERVER1).getMemory_total() * Constants.MAX_THRESHOLD);
		long memAllowedOnServer2 = (long) (api.getNode(Constants.SERVER2).getMemory_total() * Constants.MAX_THRESHOLD);
		
		while (true) {
			
			// 1. Calculer la quantit� de RAM utilis�e par mes CTs sur chaque serveur
			String node1 = "srv-px3";
			String node2 = "srv-px4";
			long memory_used_srv_px3;
			long memory_used_srv_px4;
			long disk_used_srv_px3;
			long disk_used_srv_px4;
			
			ProxmoxAPI monApi = new ProxmoxAPI();
			
			Node srv_px3 = monApi.getNode(node1);
			Node srv_px4 = monApi.getNode(node2);
			
			memory_used_srv_px3 = srv_px3.getMemory_used()*100/memAllowedOnServer1;
			memory_used_srv_px4 = srv_px4.getMemory_used()*100/memAllowedOnServer2;
			
			disk_used_srv_px3 = srv_px3.getRootfs_used()*100/srv_px3.getRootfs_total();
			disk_used_srv_px4 = srv_px4.getRootfs_used()*100/srv_px4.getRootfs_total();
			
			System.out.println("srv-px3:  \nCPU usage: " + srv_px3.getCpu() +"%\n" 
			+ "Disk usage: " + disk_used_srv_px3 + "%\n" + "Memory usage: " + memory_used_srv_px3 + "%\n");
			System.out.println("srv-px4:  \nCPU usage: " + srv_px4.getCpu() +"%\n" 
			+ "Disk usage: " + disk_used_srv_px4 + "%\n" + "Memory usage: " + memory_used_srv_px4 + "%\n");
			
			
			
			
			// M�moire autoris�e sur chaque serveur
			float memRatioOnServer1 = 0;
			// ...
			float memRatioOnServer2 = 0;
			// ... 
			long IDCT_srv3 = Constants.CT_BASE_ID+1;	
			long IDCT_srv4 = Constants.CT_BASE_ID;	
			long nbct_srv3 = 1;
			long nbct_srv4 = 1;
			String NameCT_srv3 = Constants.CT_BASE_NAME+Long.toString(nbct_srv3);
			String NameCT_srv4 = Constants.CT_BASE_NAME+Long.toString(nbct_srv4);
			
			
			if (srv_px3.getMemory_used() < srv_px3.getMemory_total() && srv_px4.getMemory_used() < srv_px4.getMemory_total()) {  // Exemple de condition de l'arr�t de la g�n�ration de CTs
				
				// choisir un serveur al�atoirement avec les ratios sp�cifi�s 66% vs 33%
				String serverName;
				if (rndServer.nextFloat() < Constants.CT_CREATION_RATIO_ON_SERVER1) {
					serverName = Constants.SERVER1;
					while(monApi.getCT(serverName, Long.toString(IDCT_srv3))!= null) {
						System.out.println("cette ct existe dejà");
						if(monApi.getCT(serverName, Long.toString(IDCT_srv3)).getStatus().equals("running") == true)
						{
							System.out.println("cette ct run stoppons là");
							monApi.stopCT(serverName,Long.toString(IDCT_srv3));
							TimeUnit.SECONDS.sleep(3);
						}
						System.out.println("detruisons la ct");
						monApi.deleteCT(serverName,Long.toString(IDCT_srv3));
						TimeUnit.SECONDS.sleep(3);
						IDCT_srv3 += 2;
					}
					NameCT_srv3 = Constants.CT_BASE_NAME+Long.toString(IDCT_srv3);
					monApi.createCT(serverName, Long.toString(IDCT_srv3), NameCT_srv3, 512);
					TimeUnit.SECONDS.sleep(35);
					monApi.startCT(serverName,Long.toString(IDCT_srv3));
					TimeUnit.SECONDS.sleep(3);
					System.out.println(NameCT_srv3 + ":");
					System.out.println("Status: " + monApi.getCT(serverName, Long.toString(IDCT_srv3)).getStatus());
					System.out.println("CPU usage: " + monApi.getCT(serverName, Long.toString(IDCT_srv3)).getCpu() + "%");
					System.out.println("Disk usage: " + (monApi.getCT(serverName, Long.toString(IDCT_srv3)).getDisk()*100/monApi.getCT(serverName, Long.toString(IDCT_srv3)).getMaxdisk()) + "%");
					System.out.println("Memory usage: " + (monApi.getCT(serverName, Long.toString(IDCT_srv3)).getMem()*100/monApi.getCT(serverName, Long.toString(IDCT_srv3)).getMaxmem()) + "%");
					System.out.println("Name: " + serverName + "\n");
					
				}
				else {
					serverName = Constants.SERVER2;
					// cr�er un contenaire sur ce serveur
					while(monApi.getCT(serverName, Long.toString(IDCT_srv4))!= null) {
						System.out.println("cette ct existe dejà");
						if(monApi.getCT(serverName, Long.toString(IDCT_srv4)).getStatus().equals("running") == true)
						{
							System.out.println("cette ct run stoppons là");
							monApi.stopCT(serverName,Long.toString(IDCT_srv4));
							TimeUnit.SECONDS.sleep(3);
						}
						System.out.println("detruisons la ct");
						monApi.deleteCT(serverName,Long.toString(IDCT_srv4));
						TimeUnit.SECONDS.sleep(3);
						IDCT_srv4 += 2;
					}
					NameCT_srv4 = Constants.CT_BASE_NAME+Long.toString(IDCT_srv4);
					monApi.createCT(serverName, Long.toString(IDCT_srv4), NameCT_srv4, 512);
					TimeUnit.SECONDS.sleep(35);
					monApi.startCT(serverName,Long.toString(IDCT_srv4));
					TimeUnit.SECONDS.sleep(3);
					System.out.println(NameCT_srv3 + ":");
					System.out.println("CPU usage: " + monApi.getCT(serverName, Long.toString(IDCT_srv4)).getCpu() + "%");
					System.out.println("Disk usage: " + (monApi.getCT(serverName, Long.toString(IDCT_srv4)).getDisk()*100/monApi.getCT(serverName, Long.toString(IDCT_srv4)).getMaxdisk()) + "%");
					System.out.println("Memory usage: " + (monApi.getCT(serverName, Long.toString(IDCT_srv4)).getMem()*100/monApi.getCT(serverName, Long.toString(IDCT_srv4)).getMaxmem()) + "%");
					System.out.println("Name: " + serverName + "\n");
				}
					
				
								
				// planifier la prochaine cr�ation
				int timeToWait = getNextEventExponential(lambda); // par exemple une loi expo d'une moyenne de 30sec
				
				// attendre jusqu'au prochain �v�nement
				Thread.sleep(1000 * timeToWait);
			}
			else {
				System.out.println("Servers are loaded, waiting ...");
				Thread.sleep(Constants.GENERATION_WAIT_TIME* 1000);
			}
		}
		
	}

}
