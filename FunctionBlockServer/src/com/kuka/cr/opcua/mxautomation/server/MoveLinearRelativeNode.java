/**
 * Prosys OPC UA Java SDK
 *
 * Copyright (c) Prosys PMS Ltd., http://www.prosysopc.com.
 * All rights reserved.
 */

package com.kuka.cr.opcua.mxautomation.server;

import com.kuka.cr.opcua.kukadatatypes.APO;
import com.kuka.cr.opcua.kukadatatypes.COORDSYS;
import com.kuka.cr.opcua.kukadatatypes.E6POS;
import com.prosysopc.ua.StatusException;
import com.prosysopc.ua.TypeDefinitionId;
import com.prosysopc.ua.server.NodeManagerUaNode;

import org.opcfoundation.ua.builtintypes.LocalizedText;
import org.opcfoundation.ua.builtintypes.NodeId;
import org.opcfoundation.ua.builtintypes.QualifiedName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@TypeDefinitionId("nsu=http://kuka.com/products/mxautomation;i=1003")
public class MoveLinearRelativeNode extends MoveLinearRelativeNodeBase {
	private final Logger log = LoggerFactory
			.getLogger(MoveLinearRelativeNode.class.getName());

	/**
	 * NodeBuilder uses this, you should not call this yourself.
	 */
	protected MoveLinearRelativeNode(NodeManagerUaNode nodeManager,
			NodeId nodeId, QualifiedName browseName, LocalizedText displayName) {
		super(nodeManager, nodeId, browseName, displayName);
	}

	@Override
	public void afterCreate() {
		// Use this method to initialize the nodes, when they are all created.
		// Note that 'super.afterCreate()' performs default initializations, so
		// consider
		// whether your own initializations should be done before or after it.
		super.afterCreate();
	}

	@Override
	protected void startProgram() throws StatusException, Exception {
		// Called every time we are going into running
		super.startProgram();

		this.log.info("Now executing a LinRel movement...");
		
		// Instruction was not sent to controller, yet
		getOutputVariable("Busy").setValue(false);
		getOutputVariable("Active").setValue(false);
		getOutputVariable("Done").setValue(false);
		getOutputVariable("Aborted").setValue(false);
		getOutputVariable("ErrorID").setValue((short) 0);

		E6POS pos = (E6POS) getInputVariable("Position").getValue();
		APO approximate = (APO) getInputVariable("Approximate").getValue();
		Short velocity = (Short) getInputVariable("Velocity").getValue();
		Short acceleration = (Short) getInputVariable("Acceleration")
				.getValue();
		
		Short orientationType = (Short) getInputVariable("OriType").getValue();
		COORDSYS coordSys = (COORDSYS) getInputVariable("CoordinateSystem").getValue();
		
		// TODO implement now the real execution of the robot
		log.info("Now executing dummy movement...");
		
		// Return values
		try {
			getOutputVariable("Busy").setValue(true);
			getOutputVariable("Active").setValue(true);
			String errorMsg = null;
			getOutputVariable("Aborted").setValue(errorMsg == null);
		}
		catch (IllegalArgumentException e) {
			getOutputVariable("Error").setValue(true);
			
			getOutputVariable("ErrorID").setValue((short) 42);
		}

		getOutputVariable("Active").setValue(false);
		getOutputVariable("Done").setValue(true);

		runningToHalted();
	}
}
