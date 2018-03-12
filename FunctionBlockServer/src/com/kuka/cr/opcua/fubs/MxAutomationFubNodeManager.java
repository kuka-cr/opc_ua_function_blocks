package com.kuka.cr.opcua.fubs;

import org.opcfoundation.ua.builtintypes.NodeId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.kuka.cr.opcua.BasicOpcUaServer;
import com.kuka.cr.opcua.fubs.basics.AbstractFubNodeManager;
import com.kuka.cr.opcua.fubs.basics.BaseFubNodeManager;
import com.kuka.cr.opcua.fubs.basics.FubMethodManagerListener;
import com.kuka.cr.opcua.fubs.basics.IFubInitializationCallback;
import com.kuka.cr.opcua.fubs.informationmodel.server.FubProgramStateMachineTypeNode;

/**
 * This is a node manager for a namespace of a concrete fub (subtype of
 * FubProgramStateMachineType)
 * 
 * @author Manuel Kaspar
 *
 */
public class MxAutomationFubNodeManager extends AbstractFubNodeManager {
	public static final String NAMESPACE = "http://kuka.com/robots/lbr/base_submodel";
	
	protected int namespaceInd = -1;
	
	@SuppressWarnings("unused")
	private static final Logger log = LoggerFactory.getLogger(MxAutomationFubNodeManager.class.getName());
	
	public MxAutomationFubNodeManager(BasicOpcUaServer server, BaseFubNodeManager baseFubNodeManager) {
		super(server, baseFubNodeManager, NAMESPACE);

		fubMethodManagerListener = new FubMethodManagerListener(baseFubNodeManager);
		
		namespaceInd = server.getAddressSpace().getNamespaceTable().getIndex(NAMESPACE);
		
		baseFubNodeManager.setFubInitializationCallback(new NodeId(namespaceInd, 1003), new LinearRelativeCallback());
	}
	
	protected void loadInformationModel() {
		server.registerModel(com.kuka.cr.opcua.mxautomation.server.InformationModel.MODEL);
	}
	
	public FubMethodManagerListener getMethodManagerListener() {
		return fubMethodManagerListener;
	}
	
	class LinearRelativeCallback implements IFubInitializationCallback {
		@Override
		public void initializeFub(FubProgramStateMachineTypeNode fub) {
			// TODO Do initialization steps for the function block
			// ((MoveLinearRelativeNode) fub).dosomething();
		}
	}
}
