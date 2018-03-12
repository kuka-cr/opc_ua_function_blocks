package com.kuka.cr.opcua.fubs.basics;

import java.util.ArrayList;

import org.opcfoundation.ua.builtintypes.DiagnosticInfo;
import org.opcfoundation.ua.builtintypes.NodeId;
import org.opcfoundation.ua.builtintypes.StatusCode;
import org.opcfoundation.ua.builtintypes.Variant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.prosysopc.ua.StatusException;
import com.prosysopc.ua.nodes.UaMethod;
import com.prosysopc.ua.nodes.UaNode;
import com.prosysopc.ua.server.CallableListener;
import com.prosysopc.ua.server.NodeBuilderException;
import com.prosysopc.ua.server.ServiceContext;

/**
 * A MethodManagerListener to listen to function block create calls
 * 
 * @author Manuel Kaspar
 */
public class FubMethodManagerListener implements CallableListener {
	private static Logger logger = LoggerFactory.getLogger(FubMethodManagerListener.class);

	private ArrayList<UaNode> methods = new ArrayList<>();
	private BaseFubNodeManager fubsNodeManager;

	/**
	 * @param myMethod
	 *            the method node to handle.
	 */
	public FubMethodManagerListener(BaseFubNodeManager fubNodeManager) {
		super();

		this.fubsNodeManager = fubNodeManager;
	}

	public void addMethod(UaNode myMethod) {
		this.methods.add(myMethod);
	}

	@Override
	public boolean onCall(ServiceContext serviceContext, NodeId objectId, UaNode object, NodeId methodId,
			UaMethod method, final Variant[] inputArguments, final StatusCode[] inputArgumentResults,
			final DiagnosticInfo[] inputArgumentDiagnosticInfos, final Variant[] outputs) 
					throws StatusException {
		logger.debug("Method was called");
		
		// Handle method calls
		// Note that the outputs array is already allocated
		if (method.getBrowseName().getName().equals("Create")) {
			// Create instance of the method
			logger.info("Create FUB called");

			String programName = "";
			try {
				programName = fubsNodeManager.createProgram(object);
			} catch (NodeBuilderException e) {
				e.printStackTrace();

				return false;
			}

			outputs[0] = new Variant(programName);

			return true;
		} else {
			logger.info("Method name not found");

			return false;
		}
	}
}
