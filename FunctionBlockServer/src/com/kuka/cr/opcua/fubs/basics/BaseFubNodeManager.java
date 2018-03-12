package com.kuka.cr.opcua.fubs.basics;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import org.javatuples.Pair;
import org.opcfoundation.ua.builtintypes.NodeId;
import org.opcfoundation.ua.core.Identifiers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import com.kuka.cr.opcua.BasicHelperFunctions;
import com.kuka.cr.opcua.OPCUABrowsePath;
import com.kuka.cr.opcua.fubs.informationmodel.server.FubProgramStateMachineTypeNode;
import com.prosysopc.ua.ModelException;
import com.prosysopc.ua.StatusException;
import com.prosysopc.ua.nodes.UaNode;
import com.prosysopc.ua.nodes.UaReference;
import com.prosysopc.ua.server.NodeBuilderException;
import com.prosysopc.ua.server.NodeManagerUaNode;
import com.prosysopc.ua.server.UaServer;

public class BaseFubNodeManager extends NodeManagerUaNode {
	public static final String NAMESPACE = "http://kuka.com/fubs";
	protected int nsIndex = -1;
	
	@SuppressWarnings("unused")
	private static final Logger log = LoggerFactory.getLogger(BaseFubNodeManager.class.getName());

	protected UaServer server;
	
	protected ArrayList<Pair<NodeId, IFubInitializationCallback>> fubInitializationCallbacks = new ArrayList<>();
	
	protected int programCounter = 0;

	public BaseFubNodeManager(UaServer server) {
		super(server, NAMESPACE);

		this.server = server;
		nsIndex = server.getAddressSpace().getNamespaceTable().getIndex(NAMESPACE);

		loadInformationModel();
	}

	private void loadInformationModel() {
		server.registerModel(com.kuka.cr.opcua.fubs.informationmodel.server.InformationModel.MODEL);

		try {
			server.getAddressSpace().loadModel(new File("codegen/lib/fubs.xml").toURI());
		} catch (SAXException | IOException | ModelException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 
	 * @return FubProgramStateMachinetype Node
	 * @throws Exception
	 */
	public UaNode getFubNode() throws Exception {
		OPCUABrowsePath browsePath = new OPCUABrowsePath();
		browsePath.append(null, "Types");
		browsePath.append(null, "ObjectTypes");
		browsePath.append(null, "BaseObjectType");
		browsePath.append(null, "StateMachineType");
		browsePath.append(null, "FiniteStateMachineType");
		browsePath.append(null, "ProgramStateMachineType");
		browsePath.append(NAMESPACE, "FubProgramStateMachineType");

		String indexedBrowsePath = browsePath.generateIndexedBrowsePath(getServer());

		NodeId nodeID = BasicHelperFunctions.translateBrowsePathToNodeID(server, indexedBrowsePath);
		UaNode fubType = server.getAddressSpace().getNode(nodeID);

		return fubType;
	}

	public ArrayList<UaNode> getAvailableFubs(UaNode fubType) {
		ArrayList<UaNode> availableFubs = new ArrayList<>();
		UaReference[] refs = getReferences(fubType.getNodeId());

		for (UaReference ref : refs) {
			if (ref.getReferenceTypeId().equals(Identifiers.HasSubtype) && ref.getTargetNode().equals(fubType) == false)
				availableFubs.add(ref.getTargetNode());
		}

		return availableFubs;
	}
	
	public String createProgram(UaNode fubType) throws NodeBuilderException {
		String name = fubType.getBrowseName().getName() + programCounter++;
		final NodeId programId = new NodeId(nsIndex, name);
		
		// !!! The derived type must be registered as model. Elseway we get an error, here !!!
		FubProgramStateMachineTypeNode myprog = (FubProgramStateMachineTypeNode) 
				createInstance(fubType.getNodeId(), name, programId);
		myprog.setProgramName(name);
		myprog.setState(ProgramState.Ready);

		try {
			addNodeAndReference(server.getNodeManagerRoot().getObjectsFolder(), myprog,
					Identifiers.Organizes);
		} catch (StatusException e) {
			e.printStackTrace();
		}

		// Now the programs needs to get to know its method ids
		myprog.setMethodIds();

		server.getNodeManagerRoot().getObjectsFolder().addReference(programId, 
				Identifiers.HasEventSource,
				false);
		
		callCallbackForFub(fubType.getNodeId(), myprog);

		return name;
	}
	
	private void callCallbackForFub(NodeId fubNodeId, FubProgramStateMachineTypeNode fub) {
		for (Pair<NodeId, IFubInitializationCallback> cb : fubInitializationCallbacks) {
			if (fubNodeId.equals(cb.getValue0()))
				cb.getValue1().initializeFub(fub);
		}
	}
	
	/**
	 * Give subclasses the possibility to e.g. set something on the Generated Class, so that
	 * an external program can be called
	 * 
	 * @param nodeId the node Id of the fub for that the callback should be called
	 * @param callback the callback function
	 */
	public void setFubInitializationCallback(NodeId nodeId, IFubInitializationCallback callback) {
		fubInitializationCallbacks.add(new Pair<NodeId, IFubInitializationCallback>(nodeId, callback));
	}
}
