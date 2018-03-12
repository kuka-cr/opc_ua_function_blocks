package com.kuka.cr.opcua.fubs.basics;

public enum ProgramState {
	Ready(1), Running(2), Suspended(3), Halted(4);
	
	private int id;
	
	private ProgramState(int id) {
		this.id = id;
	}
	
	public int getValue() {
		return this.id;
	}
}
