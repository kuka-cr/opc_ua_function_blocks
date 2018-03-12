package com.kuka.cr.opcua.fubs.basics;

public enum ProgramTransitions {
	HaltedToReady(1), ReadyToRunning(2), RunningToHalted(3), RunningToReady(4), RunningToSuspended(
			5), SuspendedToRunning(6), SuspendedToHalted(7), SuspendedToReady(8), ReadyToHalted(9);

	private int id;

	private ProgramTransitions(int id) {
		this.id = id;
	}

	public int getValue() {
		return this.id;
	}
}
