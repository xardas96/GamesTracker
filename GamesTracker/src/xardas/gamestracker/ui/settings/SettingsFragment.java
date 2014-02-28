package xardas.gamestracker.ui.settings;

import java.io.File;

import xardas.gamestracker.R;
import xardas.gamestracker.settings.Settings;
import xardas.gamestracker.settings.SettingsManager;
import android.graphics.PorterDuff.Mode;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

public class SettingsFragment extends Fragment {
	private int seekBarValue;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		final View rootView = inflater.inflate(R.layout.settings_fragment, container, false);
		final SettingsManager manager = new SettingsManager(getActivity());
		final Settings settings = manager.loadSettings();
		final TextView daysLabelTextView = (TextView) rootView.findViewById(R.id.daysLabelTextView);
		final CheckBox notifyCheckBox = (CheckBox) rootView.findViewById(R.id.notifyCheckbox);
		final SeekBar seekBar = (SeekBar) rootView.findViewById(R.id.daysSeekBar);
		seekBar.getProgressDrawable().setColorFilter(getResources().getColor(R.color.green), Mode.SRC_IN);
		seekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				settings.setNotify(notifyCheckBox.isChecked());
				settings.setDuration(seekBarValue);
				manager.saveSettings(settings);
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
			}

			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				if (fromUser) {
					seekBarValue = progress + 1;
					setLabelForSeekBar(seekBarValue, daysLabelTextView);
				}
			}
		});
		notifyCheckBox.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				settings.setNotify(!settings.isNotify());
				settings.setDuration(seekBarValue);
				daysLabelTextView.setEnabled(settings.isNotify());
				seekBar.setEnabled(settings.isNotify());
				manager.saveSettings(settings);
			}
		});
		notifyCheckBox.setChecked(settings.isNotify());
		if (settings.isNotify()) {
			seekBarValue = settings.getDuration();
		} else {
			seekBarValue = 1;
		}
		setLabelForSeekBar(seekBarValue, daysLabelTextView);
		seekBar.setProgress(seekBarValue - 1);
		daysLabelTextView.setEnabled(settings.isNotify());
		seekBar.setEnabled(settings.isNotify());

		Button clearImageCacheButton = (Button) rootView.findViewById(R.id.clearImageCacheButton);
		clearImageCacheButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				CacheDeleter deleter = new CacheDeleter();
				deleter.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, (Void) null);
			}
		});

		return rootView;
	}

	private void setLabelForSeekBar(int value, TextView daysLabelTextView) {
		String label;
		if (value == 1) {
			label = getActivity().getResources().getString(R.string.remind_day);
		} else {
			label = getActivity().getResources().getString(R.string.remind_days);
		}
		daysLabelTextView.setText(String.format(label, seekBarValue));
	}

	private class CacheDeleter extends AsyncTask<Void, Void, Void> {

		@Override
		protected void onPreExecute() {
			if (getActivity() != null) {
				Toast.makeText(getActivity(), getResources().getString(R.string.settings_clearing_cache), Toast.LENGTH_SHORT).show();
			}
		}

		@Override
		protected Void doInBackground(Void... params) {
			File cache = getActivity().getCacheDir();
			final File[] cacheFiles = cache.listFiles();
			for (File cacheFile : cacheFiles) {
				if (cacheFile.isDirectory()) {
					File[] images = cacheFile.listFiles();
					for (File image : images) {
						image.delete();
					}
					cacheFile.delete();
					publishProgress((Void) null);
				}
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			if (getActivity() != null) {
				Toast.makeText(getActivity(), getResources().getString(R.string.settings_cache_cleared), Toast.LENGTH_SHORT).show();
			}
		}
	}
}
