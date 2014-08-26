package de.upb.upbmonitor.commandline;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.TimeoutException;

import android.util.Log;

import com.stericson.RootTools.RootTools;
import com.stericson.RootTools.exceptions.RootDeniedException;
import com.stericson.RootTools.execution.Command;
import com.stericson.RootTools.execution.CommandCapture;

/**
 * Wraps the RootTools API in order to execute command line commands.
 *  
 * @author manuel
 */
public class Shell
{
	private static final String LTAG = "Shell";
	private static ArrayList<String> output;
	
	public static void execute(String... command)
	{
		execute(true, false, null, command);
	}
	
	public static ArrayList<String> executeBlocking(String... command)
	{
		return execute(true, true, null, command);
	}
	
	public static void executeCustom(CmdCallback cmd)
	{
		execute(true, true, cmd, "");
	}
	
	/**
	 * Executes command as root or not.
	 * Returns ArrayList<String> of its outputs if used in blocking mode.
	 * 
	 * @param asRoot
	 * @param command
	 * @return ArrayList<String>
	 */
	private static ArrayList<String> execute(boolean asRoot, final boolean asBlocking, CmdCallback custom_cmd, String... command)
	{
		// create output array
		output = new ArrayList<String>();
		
		// define command
		Command cmd;
		
		if(custom_cmd == null)
		{
			cmd = new CmdCallback(0, false, command)
			{
				@Override
				public void commandOutput(int id, String line)
				{
					if(asBlocking)
						output.add(line);
				}
			};
		}
		else
		{
			cmd = custom_cmd;
		}
		
		try
		{
			// execute command
			RootTools.getShell(asRoot).add(cmd);
			
			if(asBlocking)
			{
				// wait until command has finished
				synchronized (cmd)
				{
					cmd.wait();
				}
			}
		} catch (IOException e)
		{
			e.printStackTrace();
		} catch (TimeoutException e)
		{
			e.printStackTrace();
		} catch (RootDeniedException e)
		{
			e.printStackTrace();
		} catch (InterruptedException e)
		{
			e.printStackTrace();
		}
		// return output lines of command
		return output;
	}
}
