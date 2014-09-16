package de.upb.upbmonitor.commandline;

import java.util.ArrayList;

import android.util.Log;

import com.stericson.RootTools.execution.CommandCapture;

public class CmdCallback extends CommandCapture
{
	private static final String LTAG = "Shell";
	private ArrayList<String> output;
	
	public CmdCallback(int id, boolean handlerEnabled, String[] command)
	{
		super(id, handlerEnabled, command);
		output = new ArrayList<String>();
	}
	
	public CmdCallback(String... command)
	{
		// we should use a handler for commands with custom callbacks!
		super(0, true, command);
		output = new ArrayList<String>();
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
		synchronized (output)
		{
		if(output!= null)
			output.add(line);
		}
	}

	@Override
	public void commandTerminated(int id, String reason)
	{
		this.notifyAll();
	}
	
	public synchronized ArrayList<String> getOutput()
	{
		synchronized (output)
		{
			return output;
		}
	}

}
