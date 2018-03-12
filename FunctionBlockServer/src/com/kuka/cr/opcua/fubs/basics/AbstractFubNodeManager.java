package com.kuka.cr.opcua.fubs.basics;

import java.util.ArrayList;

import org.opcfoundation.ua.builtintypes.QualifiedName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.prosysopc.ua.nodes.UaNode;
import com.prosysopc.ua.server.MethodManagerUaNode;
import com.prosysopc.ua.server.NodeManagerUaNode;
import com.prosysopc.ua.server.UaServer;

/**
 * Abstract base class of a nodemanager, that handles fub in a certain namespace
 * 
 * @author Manuel Kaspar
 *
 */
public abstract class AbstractFubNodeManager extends NodeManagerUaNode {
	public static final String NAMESPACE = "http://kuka.com/robots/lbr/base_submodel";
	protected int nsIndex = -1;
	
	@SuppressWarnings("unused")
	private static final Logger log = LoggerFactory.getLogger(AbstractFubNodeManager.class.getName());
	
	protected UaServer server;
	protected BaseFubNodeManager baseFubNodeManager;
	protected FubMethodManagerListener fubMethodManagerListener;
	
	public AbstractFubNodeManager(UaServer server, BaseFubNodeManager baseFubNodeManager, String namespace) {
		super(server, namespace);

		this.server = server;
		this.baseFubNodeManager = baseFubNodeManager;
		fubMethodManagerListener = new FubMethodManagerListener(baseFubNodeManager);
		
		nsIndex = server.getAddressSpace().getNamespaceTable().getIndex(NAMESPACE);

		// This is needed, so that the clients can delete nodes
		// Since this is a rather sensitive feature set, you might want to
		// consider adding user
		// authentication for this feature in the future. The easiest way to do
		// this would be
		// to create a NodeManagerListener and override the onDeleteNode method
		// for user validation
		// (see MyNodeManagerListener in the SampleConsoleServer project).
		server.getAddressSpace().setNodeManagementEnabled(true);
		
		loadInformationModel();
		
		try {
			loadFubCreateMethods();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	protected abstract void loadInformationModel();
	
	protected void loadFubCreateMethods() throws Exception {
		UaNode fubType = baseFubNodeManager.getFubNode();

		ArrayList<UaNode> availableFubs = baseFubNodeManager.getAvailableFubs(fubType);

		for (UaNode fub : availableFubs) {
			UaNode method = getMethodNode(fub);
			
			fubMethodManagerListener.addMethod(method);
		}

		MethodManagerUaNode m = (MethodManagerUaNode) this.getMethodManager();
		m.addCallListener(fubMethodManagerListener);
	}
	
	private UaNode getMethodNode(UaNode parent) {
		UaNode method = parent.getComponent(new QualifiedName("Create"));
		
		return method;
	}
}
