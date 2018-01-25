package com.jverson.springboot.condition;

public class LinuxCmdServiceImpl implements CmdService {
	@Override
	public String getListCmd() {
		return "ls";
	}
}
