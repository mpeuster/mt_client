package de.upb.upbmonitor;

import android.support.v4.app.Fragment;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.NumberPicker;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.CompoundButton.OnCheckedChangeListener;

public class LocationFragment extends Fragment
{
	private static final String LTAG = "LocationFragment";

	private SharedPreferences settings;
	private Switch switchManualLocation;
	private NumberPicker npPositionX, npPositionY;
	private TextView tvPH, tvPX, tvPY;

	private int positionStepSize = 100;

	/**
	 * Returns a new instance of this fragment for the given section number.
	 */
	public static LocationFragment newInstance()
	{
		LocationFragment fragment = new LocationFragment();
		return fragment;
	}

	public LocationFragment()
	{
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState)
	{
		View rootView = inflater.inflate(R.layout.fragment_location, container,
				false);

		// get preference manager
		settings = PreferenceManager.getDefaultSharedPreferences(getActivity());

		// get pointers to control elements
		this.switchManualLocation = (Switch) rootView
				.findViewById(R.id.switchManualLocation);
		this.npPositionX = (NumberPicker) rootView
				.findViewById(R.id.npPositionX);
		this.npPositionY = (NumberPicker) rootView
				.findViewById(R.id.npPositionY);
		this.tvPH = (TextView) rootView.findViewById(R.id.textViewPHeadline);
		this.tvPX = (TextView) rootView.findViewById(R.id.textViewPX);
		this.tvPY = (TextView) rootView.findViewById(R.id.textViewPY);

		// fill number picker controls
		this.fillNumberPicker(this.npPositionX, 0, 2000, positionStepSize);
		this.fillNumberPicker(this.npPositionY, 0, 2000, positionStepSize);

		// load stored values
		this.switchManualLocation
				.setChecked(this.getManualLocationPreference());
		this.setEnableToPositionControls(this.getManualLocationPreference());
		this.npPositionX.setValue(this.getPositionXPreference()
				/ positionStepSize);
		this.npPositionY.setValue(this.getPositionYPreference()
				/ positionStepSize);

		// manual location switch listener
		this.switchManualLocation
				.setOnCheckedChangeListener(new OnCheckedChangeListener()
				{

					@Override
					public void onCheckedChanged(CompoundButton buttonView,
							boolean isChecked)
					{
						// store value in shared preference
						setManualLocationPreference(isChecked);
						if (isChecked)
							setEnableToPositionControls(true);
						else
							setEnableToPositionControls(false);
					}
				});

		// position change listener
		this.npPositionX
				.setOnValueChangedListener(new NumberPicker.OnValueChangeListener()
				{
					@Override
					public void onValueChange(NumberPicker picker, int oldVal,
							int newVal)
					{
						// store value in shared preference
						if (newVal != oldVal)
							setPositionXPreference(newVal * positionStepSize);
					}
				});

		// position change listener
		this.npPositionY
				.setOnValueChangedListener(new NumberPicker.OnValueChangeListener()
				{
					@Override
					public void onValueChange(NumberPicker picker, int oldVal,
							int newVal)
					{
						// store value in shared preference
						if (newVal != oldVal)
							setPositionYPreference(newVal * positionStepSize);
					}
				});

		return rootView;
	}

	private void setEnableToPositionControls(boolean b)
	{
		this.tvPH.setEnabled(b);
		this.tvPX.setEnabled(b);
		this.tvPY.setEnabled(b);
		this.npPositionX.setEnabled(b);
		this.npPositionY.setEnabled(b);
	}

	private void fillNumberPicker(NumberPicker np, int min, int max, int step)
	{
		int items = (max - min) / step + 1;
		String[] nums = new String[items];
		for (int i = 0; i < items; i++)
			nums[i] = Integer.toString(i * step);

		np.setDisplayedValues(nums);
		np.setMinValue(min);
		np.setMaxValue(items - 1);
		np.setValue(min);
	}

	private void setManualLocationPreference(boolean b)
	{
		if (settings == null)
		{
			Log.e(LTAG, "Could not access shared preferences.");
			return;
		}
		SharedPreferences.Editor e = settings.edit();
		e.putBoolean("pref_enable_manual_location", b);
		e.commit();
		Log.v(LTAG, "Changed manual location enaqbled to: " + b);
	}

	private boolean getManualLocationPreference()
	{
		if (settings == null)
		{
			Log.e(LTAG, "Could not access shared preferences.");
			return false;
		}
		return settings.getBoolean("pref_enable_manual_location", false);
	}

	private void setPositionXPreference(int x)
	{
		if (settings == null)
		{
			Log.e(LTAG, "Could not access shared preferences.");
			return;
		}
		SharedPreferences.Editor e = settings.edit();
		e.putInt("pref_manual_location_x", x);
		e.commit();
		Log.v(LTAG, "Changed manual location X to: " + x);
	}

	private int getPositionXPreference()
	{
		if (settings == null)
		{
			Log.e(LTAG, "Could not access shared preferences.");
			return 0;
		}
		return settings.getInt("pref_manual_location_x", 0);
	}

	private void setPositionYPreference(int y)
	{
		if (settings == null)
		{
			Log.e(LTAG, "Could not access shared preferences.");
			return;
		}
		SharedPreferences.Editor e = settings.edit();
		e.putInt("pref_manual_location_y", y);
		e.commit();
		Log.v(LTAG, "Changed manual location Y to: " + y);
	}

	private int getPositionYPreference()
	{
		if (settings == null)
		{
			Log.e(LTAG, "Could not access shared preferences.");
			return 0;
		}
		return settings.getInt("pref_manual_location_y", 0);
	}

}
