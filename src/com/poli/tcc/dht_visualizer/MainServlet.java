package com.poli.tcc.dht_visualizer;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.poli.tcc.dht.DHT;
import com.poli.tcc.dht.DHTException.UsernameAlreadyTakenException;
import com.poli.tcc.dht.DHTNode;
import com.poli.tcc.dht.Utils;
import com.sun.org.apache.xerces.internal.impl.dv.util.Base64;

import net.tomp2p.dht.PeerDHT;
import net.tomp2p.peers.Number160;
import net.tomp2p.peers.PeerAddress;

/**
 * Servlet implementation class MainServlet
 */
@WebServlet("/MainServlet")
public class MainServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public MainServlet() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		request.getRequestDispatcher("main.jsp").forward(request, response);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		ArrayList<PeerInfo> peers = getPeers(request);
		response.setContentType("application/json");      
		PrintWriter out = response.getWriter();  
		out.print(peersToJson(peers));
		out.flush();
	}
	
	private PeerDHT connectToDHT(HttpServletRequest request) throws IOException {
		String peerName = "Visualizer";
//		String trackerIP = "whatsp2ptracker.ddns.net";
		String trackerIP = "192.168.0.105";
		int trackerPort = 4001;
		final Number160 peerId = DHT.createPeerID(peerName);
		KeyPairGenerator gen;
		try {
			gen = KeyPairGenerator.getInstance( "DSA" );
	        SecureRandom secRandom = SecureRandom.getInstance("SHA1PRNG");
	        gen.initialize(1024, secRandom);
	        KeyPair keyPair = gen.generateKeyPair();
			final DHTNode me = new DHTNode(peerId);
			me.setUsername(peerName);
			me.setIp(Utils.getIPAddress(true));
			me.setSignKeyPair(keyPair);
			DHT.start(me, 4005);
			if (DHT.connectTo(trackerIP, trackerPort)) {
				System.out.println("Successfully connected");
				request.getSession().setAttribute("serverPeer", DHT.serverPeer);
				return DHT.serverPeer;
			}
		} catch (NoSuchAlgorithmException | ClassNotFoundException | UsernameAlreadyTakenException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	private ArrayList<PeerInfo> getPeers(HttpServletRequest request) throws IOException {
		ArrayList<PeerInfo> peers = new ArrayList<PeerInfo>();
		PeerDHT serverPeer = (PeerDHT) request.getSession().getAttribute("serverPeer");
		if (serverPeer == null) {
			serverPeer = connectToDHT(request);
		}
		
		Iterator<PeerAddress> it = serverPeer.peerBean().peerMap().all().iterator();
		while (it.hasNext()) {
			PeerAddress paddr = it.next();
			PeerInfo peer = new PeerInfo();
			peer.id = paddr.peerId().toString();
			peer.name = DHT.getOriginalID(paddr.peerId());
			peer.address = paddr.inetAddress().getHostAddress() + ":" + paddr.tcpPort();
			PublicKey signPublicKey;
			try {
				signPublicKey = (PublicKey) DHT.get(peer.name);
				if (signPublicKey != null) {
					InetSocketAddress chatAddress = (InetSocketAddress) DHT.getProtected("chatAddress", signPublicKey);
					byte[] chatPublicKeyRingEncoded = (byte[]) DHT.getProtected("chatPublicKey", signPublicKey);
					peer.signPublicKey = Base64.encode(signPublicKey.getEncoded());
					peer.chatPublicKey = Base64.encode(chatPublicKeyRingEncoded);
					peer.chatAddress = chatAddress.getHostName() + ":" + chatAddress.getPort();
				}
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			peers.add(peer);
		}
		
		return peers;
	}
	
	private String peersToJson(ArrayList<PeerInfo> list) {
		JSONArray json = new JSONArray();
		for (int i = 0; i < list.size(); i++) {
			PeerInfo peer = list.get(i);
			JSONObject obj = new JSONObject();
			try {
				obj.put("id", peer.id);
				obj.put("name", peer.name);
				obj.put("address", peer.address);
				obj.put("signPublicKey", peer.signPublicKey);
				obj.put("chatPublicKey", peer.chatPublicKey);
				obj.put("chatAddress", peer.chatAddress);
				json.put(obj);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return json.toString();
	}

}
