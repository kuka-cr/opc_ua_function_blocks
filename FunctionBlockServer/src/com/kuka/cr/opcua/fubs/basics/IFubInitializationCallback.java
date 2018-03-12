package com.kuka.cr.opcua.fubs.basics;

import com.kuka.cr.opcua.fubs.informationmodel.server.FubProgramStateMachineTypeNode;

public interface IFubInitializationCallback {
	/**
	 * Give subclasses the possibility to e.g. set something on the Generated Class, so that
	 * an external program can be called
	 */
	public void initializeFub(FubProgramStateMachineTypeNode fub);
}
