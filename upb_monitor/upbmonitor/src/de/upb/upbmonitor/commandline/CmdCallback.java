package de.upb.upbmonitor.commandline;

import android.util.Log;

import com.stericson.RootTools.execution.CommandCapture;

public class CmdCallback extends CommandCapture
{
	private static final String LTAG = "Shell";
	
	public CmdCallback(int id, boolean handlerEnabled, String[] command)
	{
		super(id, handlerEnabled, command);
	}
	
	public CmdCallback(String... command)
	{
		// we should use a handler for commands with custom callbacks!
		super(0, true, command);
	}
	
	@Override
	public void commandCompleted(int id, int exitCode)
	{
		Log.v(LTAG, "Cmd: '" + this.getCommand().trim() + "' Exitcode: " + exitCode);
		this.notifyAll();
	}

	@Override
	public void commandOutput(int id, String line)
	{
	}

	@Override
	public void commandTerminated(int id, String reason)
	{
		this.notifyAll();
	}

}
