package com.kuka.cr.opcua;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

import org.apache.log4j.BasicConfigurator;
import org.opcfoundation.ua.builtintypes.NodeId;
import org.opcfoundation.ua.builtintypes.Variant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import com.kuka.cr.opcua.fubs.MxAutomationFubNodeManager;
import com.kuka.cr.opcua.fubs.basics.BaseFubNodeManager;
import com.kuka.cr.opcua.fubs.basics.FubMethodManagerListener;
import com.prosysopc.ua.ModelException;
import com.prosysopc.ua.StatusException;
import com.prosysopc.ua.nodes.UaMethod;
import com.prosysopc.ua.nodes.UaNode;
import com.prosysopc.ua.server.UaServerException;

/**
 * This class controls this demo function block server Have a look in the readme
 * for more information
 * 
 * @author Manuel Kaspar
 * 
 */
public class FunctionBlockServer {
	private static final Logger log = LoggerFactory
			.getLogger(FunctionBlockServer.class);

	private MxAutomationFubNodeManager mxNodeManager;
	private BasicOpcUaServer server;

	public FunctionBlockServer(BasicOpcUaServer server) {
		this.server = server;
	}

	/**
	 * Loading the node manager for abstract fubs and then for the concrete fubs
	 * available on this AAS
	 */
	public void loadFubs() {
		BaseFubNodeManager baseFubNodeManager = new BaseFubNodeManager(server);

		mxNodeManager = new MxAutomationFubNodeManager(server,
				baseFubNodeManager);
	}

	/**
	 * Just a dummy function to create a fub for demo purposes. This is normally
	 * done from a client...
	 * 
	 * @throws StatusException
	 */
	public void createFub() throws StatusException {
		UaNode fubNode = server.getAddressSpace().getNode(new NodeId(5, 1003));
		UaMethod methodNode = (UaMethod) server.getAddressSpace().getNode(
				new NodeId(3, 7006));

		FubMethodManagerListener methodManagerListener = mxNodeManager
				.getMethodManagerListener();
		methodManagerListener.onCall(null, null, fubNode, null, methodNode,
				null, null, null, new Variant[1]);
	}

	public static void main(String[] args) {
		BasicConfigurator.configure();

		BasicOpcUaServer server = null;

		try {
			server = new BasicOpcUaServer(16663, 0, "fubtestserver",
					"FunctionBlockTestServer");
		} catch (Exception e) {
			e.printStackTrace();
		}

		try {
			server.setDiscoveryServerUrl("opc.tcp://localhost:4840");
		} catch (URISyntaxException e1) {
			e1.printStackTrace();
		}

		FunctionBlockServer fub_server = null;

		// Load the different models
		try {
			server.registerModel(com.kuka.cr.opcua.kukadatatypes.server.InformationModel.MODEL);
			server.getAddressSpace().loadModel(
					new File("codegen/lib/kuka_datatypes.xml").toURI());

			fub_server = new FunctionBlockServer(server);
			fub_server.loadFubs();

			server.registerModel(com.kuka.cr.opcua.mxautomation.server.InformationModel.MODEL);
			server.getAddressSpace().loadModel(
					new File("codegen/lib/mxautomation.xml").toURI());
		} catch (ModelException | SAXException | IOException e) {
			e.printStackTrace();
		}

		try {
			server.start();
		} catch (UaServerException e) {
			e.printStackTrace();
		}

		log.info("Press a key to start function block...");

		try {
			System.in.read();
		} catch (IOException e) {
			e.printStackTrace();
		}

		try {
			fub_server.createFub();
		} catch (StatusException e1) {
			e1.printStackTrace();
		}

		log.info("Now you can e.g. use UaExpert for parametrizing the function block and then calling the start method for execution");

		while (true)
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
	}
}
