package com.jverson.springboot.condition;

public class WindowsCmdServiceImpl implements CmdService {

	@Override
	public String getListCmd() {
		return "dir";
	}

}
