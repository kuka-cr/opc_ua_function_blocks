package com.kuka.cr.opcua.fubs.informationmodel.server;

/**
 * Prosys OPC UA Java SDK
 *
 * Copyright (c) Prosys PMS Ltd., http://www.prosysopc.com.
 * All rights reserved.
 */

import java.util.ArrayList;

import org.opcfoundation.ua.builtintypes.DateTime;
import org.opcfoundation.ua.builtintypes.LocalizedText;
import org.opcfoundation.ua.builtintypes.NodeId;
import org.opcfoundation.ua.builtintypes.QualifiedName;
import org.opcfoundation.ua.builtintypes.StatusCode;
import org.opcfoundation.ua.builtintypes.UnsignedInteger;
import org.opcfoundation.ua.builtintypes.UnsignedShort;
import org.opcfoundation.ua.core.Identifiers;

import com.kuka.cr.opcua.fubs.basics.ProgramState;
import com.kuka.cr.opcua.fubs.basics.ProgramTransitions;
import com.kuka.cr.opcua.fubs.informationmodel.VariableType;
import com.prosysopc.ua.StatusException;
import com.prosysopc.ua.TypeDefinitionId;
import com.prosysopc.ua.nodes.UaMethod;
import com.prosysopc.ua.nodes.UaNode;
import com.prosysopc.ua.server.DefaultNodeBuilderConfiguration;
import com.prosysopc.ua.server.EventManager;
import com.prosysopc.ua.server.NodeBuilderConfiguration;
import com.prosysopc.ua.server.NodeBuilderException;
import com.prosysopc.ua.server.NodeManagerUaNode;
import com.prosysopc.ua.server.ServiceContext;
import com.prosysopc.ua.types.opcua.ProgramStateMachineType;
import com.prosysopc.ua.types.opcua.server.BaseObjectTypeNode;
import com.prosysopc.ua.types.opcua.server.FiniteStateVariableTypeNode;
import com.prosysopc.ua.types.opcua.server.ProgramTransitionEventTypeNode;
import com.prosysopc.ua.types.opcua.server.ProgramTransitionEventTypeNodeBase;
import com.prosysopc.ua.types.opcua.server.ProgressEventTypeNode;
import com.prosysopc.ua.types.opcua.server.StateVariableTypeNode;

@TypeDefinitionId("nsu=http://kuka.com/fubs;i=1005")
public class FubProgramStateMachineTypeNode extends FubProgramStateMachineTypeNodeBase {
	private static int eventId = 0;
	private ProgramState state;
	private ProgramTransitionEventTypeNodeBase event;

	private String programName;

	// Method Ids
	private NodeId haltId;
	private NodeId resetId;
	private NodeId resumeId;
	private NodeId startId;
	private NodeId suspendId;
	
	protected ArrayList<VariableType> inputVariables;
	protected ArrayList<VariableType> outputVariables;

	/**
	 * NodeBuilder uses this, you should not call this yourself.
	 */
	protected FubProgramStateMachineTypeNode(NodeManagerUaNode nodeManager, NodeId nodeId, QualifiedName browseName,
			LocalizedText displayName) {
		super(nodeManager, nodeId, browseName, displayName);
	}

	@Override
	public void afterCreate() {
		// Use this method to initialize the nodes, when they are all created.
		// Note that 'super.afterCreate()' performs default initializations, so
		// consider
		// whether your own initializations should be done before or after it.
		super.afterCreate();

		this.event = this.createTransitionEvent();
	}

	@Override
	protected String onCreate(ServiceContext serviceContext) throws StatusException {
		// Doesn't work from prosys to have Methods on Types
		return null;
	}

	protected void startProgram() throws StatusException, Exception {
		// fireProgressEvent();

		event.setSourceNode(getNodeId());
		event.setSourceName(programName);
	}

	protected void stopProgram() {
		//runningToHalted();
	}

	public ProgramState getState() {
		return this.state;
	}

	private ProgramTransitionEventTypeNode createTransitionEvent() {
		// Create event
		NodeBuilderConfiguration conf = new DefaultNodeBuilderConfiguration();
		conf.addOptional(Identifiers.BaseEventType_SourceName);

		ProgramTransitionEventTypeNode event = null;

		try {
			event = nodeManager.createNodeBuilder(ProgramTransitionEventTypeNode.class, conf)
					.setName("TransitionEvent" + this.getNextUserEventId()).build();
			event.setSeverity(500);
			event.setMessage(new LocalizedText("Transition"));
		} catch (NodeBuilderException e) {
			e.printStackTrace();
		}

		return event;
	}
	
	public void setState(ProgramState state) {
		this.state = state;

		int namespaceIndex = nodeManager.getNamespaceIndex();

		if (this.event == null)
			this.event = createTransitionEvent();

		try {
			StateVariableTypeNode fromState = this.event.getFromStateNode();
			fromState.setValue(this.getCurrentStateNode().getValue());
			fromState.setId(this.getCurrentStateNode().getId());

			FiniteStateVariableTypeNode currentState = this.getCurrentStateNode();
			currentState.setValue(new LocalizedText(state.toString()));
			currentState.setNumber(new UnsignedInteger(state.getValue()));
			currentState.setId(new NodeId(namespaceIndex, stateIdFromState(state)));

			this.event.getToStateNode().setValue(currentState.getValue());
			this.event.getToStateNode().setId(currentState.getId());
		} catch (StatusException e) {
			e.printStackTrace();
		}
	}

	private String stateIdFromState(ProgramState state) {
		switch (state) {
		case Ready:
			return ProgramStateMachineType.READY;
		case Running:
			return ProgramStateMachineType.RUNNING;
		case Halted:
			return ProgramStateMachineType.HALTED;
		case Suspended:
			return ProgramStateMachineType.SUSPENDED;
		default:
			return null;
		}
	}

	protected void onHalt(ServiceContext arg0) throws StatusException {
		if (!this.canHalt())
			throw new StatusException(StatusCode.BAD);

		this.event.setSourceName("Halt");
		this.event.setSourceNode(this.haltId);

		switch (this.state) {
		case Ready:
			this.readyToHalted();
			break;
		case Suspended:
			this.suspendedToHalted();
			break;
		case Running:
			this.runningToHalted();
			break;
		default:
			break;
		}
	}

	protected void onReset(ServiceContext arg0) throws StatusException {
		if (!this.canReset())
			throw new StatusException(StatusCode.BAD);

		this.event.setSourceName("Reset");
		this.event.setSourceNode(this.resetId);

		this.haltedToReady();
	}

	protected void onResume(ServiceContext arg0) throws StatusException {
		if (!this.canResume())
			throw new StatusException(StatusCode.BAD);

		this.event.setSourceName("Resume");
		this.event.setSourceNode(this.resumeId);

		this.suspendedToRunning();
	}

	protected void onStart(ServiceContext arg0) throws StatusException {
		if (!this.canStart())
			throw new StatusException(StatusCode.BAD);

		this.event.setSourceName("Start");
		this.event.setSourceNode(this.startId);

		this.readyToRunning();
	}

	protected void onSuspend(ServiceContext arg0) throws StatusException {
		if (!this.canSuspend())
			throw new StatusException(StatusCode.BAD);

		this.event.setSourceName("Suspend");
		this.event.setSourceNode(this.suspendId);

		this.runningToSuspended();
	}

	public String getProgramName() {
		return programName;
	}

	public void setProgramName(String programName) {
		this.programName = programName;
	}

	public NodeId getHaltId() {
		return haltId;
	}

	public NodeId getResetId() {
		return resetId;
	}

	public NodeId getResumeId() {
		return resumeId;
	}

	public NodeId getStartId() {
		return startId;
	}

	public NodeId getSuspendId() {
		return suspendId;
	}

	public void setMethodIds() {
		this.startId = this.getComponent(new QualifiedName(ProgramStateMachineType.START)).getNodeId();
		this.suspendId = this.getComponent(new QualifiedName(ProgramStateMachineType.SUSPEND)).getNodeId();
		this.haltId = this.getComponent(new QualifiedName(ProgramStateMachineType.HALT)).getNodeId();
		this.resetId = this.getComponent(new QualifiedName(ProgramStateMachineType.RESET)).getNodeId();
		this.resumeId = this.getComponent(new QualifiedName(ProgramStateMachineType.RESUME)).getNodeId();

		this.setExecutable(false, this.resetId);
		this.setExecutable(false, this.resumeId);
		this.setExecutable(false, this.suspendId);
	}

	/**
	 * Set the executable flag for a method. This is not checked by UaExpert or
	 * the sdk. I think it is e.g. for HMI's to show if a method is executable.
	 * So you still have to check if a method is allowed to be executed.
	 * 
	 * @param value
	 * @param nodeId
	 *            the method nodeId to change
	 */
	private void setExecutable(boolean value, NodeId nodeId) {
		try {
			UaMethod method = (UaMethod) nodeManager.getServer().getAddressSpace().getNode(nodeId);
			method.setExecutable(value);
		} catch (StatusException e) {
			e.printStackTrace();
		}
	}

	public ProgramTransitionEventTypeNodeBase getEvent() {
		return this.event;
	}

	private boolean canHalt() {
		return this.state == ProgramState.Running || this.state == ProgramState.Ready
				|| this.state == ProgramState.Suspended;
	}

	private boolean canStart() {
		return this.state == ProgramState.Ready;
	}

	private boolean canReset() {
		return this.state == ProgramState.Halted;
	}

	private boolean canResume() {
		return this.state == ProgramState.Suspended;
	}

	private boolean canSuspend() {
		return this.state == ProgramState.Running;
	}

	private void fireEvent() {
		try {
			event.getTransitionNode().setId(getLastTransition());
			event.getTransitionNode().setValue(getLastTransitionNode().getValue());
			event.setIntermediateResult("TODO");
			event.setTime(DateTime.currentTime());

			byte[] eventId = this.getNextUserEventId();
			final DateTime now = DateTime.currentTime();
			event.triggerEvent(now, now, eventId);

			// Trigger event deletes the event, so we must create a new one
			// This could be overwritten by event.setDelete....
			event = createTransitionEvent();
		} catch (StatusException e) {
			e.printStackTrace();
		}
	}

	private void fireProgressEvent() {
		ProgressEventTypeNode ev = nodeManager.createEvent(ProgressEventTypeNode.class);
		ev.setSourceNode(this.getNodeId());
		ev.setSourceName(this.programName);
		ev.setSeverity(500);
		ev.setMessage(new LocalizedText("Progress"));
		ev.setContext(this.getNodeId());
		ev.setProgress(new UnsignedShort(((1000 - 50) / 10)));

		ev.triggerEvent(this.getNextUserEventId());
	}

	private byte[] getNextUserEventId() throws RuntimeException {
		return EventManager.createEventId(eventId++);
	}

	// TODO make this nicer so not everything is repeated all the time
	protected void haltedToReady() {
		try {
			this.getLastTransitionNode().setValue(new LocalizedText("HaltedToReady"));
			this.getLastTransitionNode().setId(ProgramStateMachineType.HALTED_TO_READY);
			this.getLastTransitionNode().setNumber(new UnsignedInteger(ProgramTransitions.HaltedToReady.getValue()));
			this.getLastTransitionNode().setTransitionTime(DateTime.currentTime());

			this.state = ProgramState.Ready;

			this.fireEvent();

			this.setRecycleCount(this.getRecycleCount() + 1);
		} catch (StatusException e) {
			e.printStackTrace();
		}
	}

	protected void readyToHalted() {
		try {
			this.getLastTransitionNode().setValue(new LocalizedText("ReadyToHalted"));
			this.getLastTransitionNode().setId(ProgramStateMachineType.READY_TO_HALTED);
			this.getLastTransitionNode().setNumber(new UnsignedInteger(ProgramTransitions.ReadyToHalted.getValue()));
			this.getLastTransitionNode().setTransitionTime(DateTime.currentTime());

			this.state = ProgramState.Halted;

			this.fireEvent();

			this.setExecutable(false, this.haltId);
			this.setExecutable(true, this.resetId);
		} catch (StatusException e) {
			e.printStackTrace();
		}
	}

	protected void readyToRunning() {
		try {
			this.getLastTransitionNode().setValue(new LocalizedText("ReadyToRunning"));
			this.getLastTransitionNode().setId(ProgramStateMachineType.READY_TO_RUNNING);
			this.getLastTransitionNode().setNumber(new UnsignedInteger(ProgramTransitions.ReadyToRunning.getValue()));
			this.getLastTransitionNode().setTransitionTime(DateTime.currentTime());

			this.setState(ProgramState.Running);
			this.fireEvent();
			
			try {
				this.startProgram();
			} catch (Exception e) {
				e.printStackTrace();
			}

			this.setExecutable(false, this.startId);
			this.setExecutable(true, this.suspendId);
		} catch (StatusException e) {
			e.printStackTrace();
		}
	}

	protected void runningToHalted() {
		try {
			this.getLastTransitionNode().setValue(new LocalizedText("RunningToHalted"));
			this.getLastTransitionNode().setId(ProgramStateMachineType.RUNNING_TO_HALTED);
			this.getLastTransitionNode().setNumber(new UnsignedInteger(ProgramTransitions.RunningToHalted.getValue()));
			this.getLastTransitionNode().setTransitionTime(DateTime.currentTime());

			this.setState(ProgramState.Halted);

			this.stopProgram();

			this.fireEvent();

			this.setExecutable(false, this.suspendId);
			this.setExecutable(false, this.haltId);
			this.setExecutable(true, this.resetId);
		} catch (StatusException e) {
			e.printStackTrace();
		}
	}

	protected void runningToReady() {
		try {
			this.getLastTransitionNode().setValue(new LocalizedText("RunningToReady"));
			this.getLastTransitionNode().setId(ProgramStateMachineType.RUNNING_TO_READY);
			this.getLastTransitionNode().setNumber(new UnsignedInteger(ProgramTransitions.RunningToReady.getValue()));
			this.getLastTransitionNode().setTransitionTime(DateTime.currentTime());

			this.setState(ProgramState.Ready);

			this.stopProgram();

			this.fireEvent();

			this.setExecutable(false, this.suspendId);
			this.setExecutable(true, this.startId);
		} catch (StatusException e) {
			e.printStackTrace();
		}
	}

	protected void runningToSuspended() {
		try {
			this.getLastTransitionNode().setValue(new LocalizedText("RunningToSuspended"));
			this.getLastTransitionNode().setId(ProgramStateMachineType.RUNNING_TO_SUSPENDED);
			this.getLastTransitionNode()
					.setNumber(new UnsignedInteger(ProgramTransitions.RunningToSuspended.getValue()));
			this.getLastTransitionNode().setTransitionTime(DateTime.currentTime());

			this.setState(ProgramState.Suspended);

			this.stopProgram();

			this.fireEvent();

			this.setExecutable(false, this.suspendId);
			this.setExecutable(true, this.resumeId);
		} catch (StatusException e) {
			e.printStackTrace();
		}
	}

	protected void suspendedToHalted() {
		try {
			this.getLastTransitionNode().setValue(new LocalizedText("SuspendedToHalted"));
			this.getLastTransitionNode().setId(ProgramStateMachineType.SUSPENDED_TO_HALTED);
			this.getLastTransitionNode()
					.setNumber(new UnsignedInteger(ProgramTransitions.SuspendedToHalted.getValue()));
			this.getLastTransitionNode().setTransitionTime(DateTime.currentTime());

			this.setState(ProgramState.Halted);

			this.stopProgram();

			this.fireEvent();

			this.setExecutable(true, this.resetId);
			this.setExecutable(false, this.haltId);
		} catch (StatusException e) {
			e.printStackTrace();
		}
	}

	protected void suspendedToReady() {
		try {
			this.getLastTransitionNode().setValue(new LocalizedText("SuspendedToReady"));
			this.getLastTransitionNode().setId(ProgramStateMachineType.SUSPENDED_TO_READY);
			this.getLastTransitionNode().setNumber(new UnsignedInteger(ProgramTransitions.SuspendedToReady.getValue()));
			this.getLastTransitionNode().setTransitionTime(DateTime.currentTime());

			this.setState(ProgramState.Ready);

			this.stopProgram();

			this.setExecutable(false, this.resumeId);
			this.setExecutable(true, this.startId);
		} catch (StatusException e) {
			e.printStackTrace();
		}
	}

	protected void suspendedToRunning() {
		try {
			this.getLastTransitionNode().setValue(new LocalizedText("SuspendedToRunning"));
			this.getLastTransitionNode().setId(ProgramStateMachineType.SUSPENDED_TO_RUNNING);
			this.getLastTransitionNode()
					.setNumber(new UnsignedInteger(ProgramTransitions.SuspendedToRunning.getValue()));
			this.getLastTransitionNode().setTransitionTime(DateTime.currentTime());

			this.setState(ProgramState.Running);

			try {
				this.startProgram();
			} catch (Exception e) {
				e.printStackTrace();
			}

			this.fireEvent();

			this.setExecutable(false, this.resumeId);
			this.setExecutable(true, this.suspendId);
		} catch (StatusException e) {
			e.printStackTrace();
		}
	}
	
	protected ArrayList<VariableType> getInputVariables() {
		if (inputVariables == null)
			inputVariables = getVariableNodes(getInputVariablesNode());
		
		return inputVariables;
	}

	private ArrayList<VariableType> getVariableNodes(BaseObjectTypeNode parent) {
		UaNode[] varNodes = parent.getComponents();
		
		ArrayList<VariableType> vars = new ArrayList<>();
		
		for (int i = 0; i < varNodes.length; i++)
			vars.add((VariableType) varNodes[i]);
		
		return vars;
	}
	
	protected ArrayList<VariableType> getOutputVariables() {
		if (outputVariables == null)
			outputVariables = getVariableNodes(getOutputVariablesNode());
		
		return outputVariables;
	}
	
	protected VariableType getInputVariable(String name) throws Exception {
		getInputVariables();
		
		for (int i = 0; i < inputVariables.size(); i++) {
			if (inputVariables.get(i).getBrowseName().getName().equals(name))
				return inputVariables.get(i);
		}
		
		throw new Exception("Variable name not found");
	}
	
	protected VariableType getOutputVariable(String name) throws Exception {
		getOutputVariables();
		
		for (int i = 0; i < outputVariables.size(); i++) {
			if (outputVariables.get(i).getBrowseName().getName().equals(name))
				return outputVariables.get(i);
		}
		
		throw new Exception("Variable name not found");
	}
}
