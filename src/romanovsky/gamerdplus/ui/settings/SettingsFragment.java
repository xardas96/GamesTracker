package romanovsky.gamerdplus.ui.settings;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import romanovsky.gamerdplus.R;
import romanovsky.gamerdplus.async.AsyncTask;
import romanovsky.gamerdplus.database.SQLiteHelper;
import romanovsky.gamerdplus.settings.Settings;
import romanovsky.gamerdplus.settings.SettingsManager;
import romanovsky.gamerdplus.ui.CustomFragment;
import android.graphics.PorterDuff.Mode;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

public class SettingsFragment extends CustomFragment {
	private int seekBarValue;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		final View rootView = inflater.inflate(R.layout.settings_fragment, container, false);
		refresh(rootView);
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

	private class Initializer extends AsyncTask<Void, Void, Settings> {
		private View rootView;
		private SettingsManager manager;

		public Initializer(View rootView) {
			this.rootView = rootView;
			manager = new SettingsManager(getActivity());
		}

		@Override
		protected Settings doInBackground(Void... params) {
			return manager.loadSettings();
		}

		@Override
		protected void onPostExecute(final Settings result) {
			final CheckBox autoUpdateCheckBox = (CheckBox) rootView.findViewById(R.id.autoUpdateOnStartupCheckBox);
			autoUpdateCheckBox.setChecked(result.isAutoUpdate());
			autoUpdateCheckBox.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					result.setAutoUpdate(autoUpdateCheckBox.isChecked());
					manager.saveSettings(result);
				}
			});

			final List<Integer> autoExpand = result.getAutoExpand();
			LinearLayout autoExpandLinearLayout = (LinearLayout) rootView.findViewById(R.id.checkBoxExpandLayout);
			String[] categories = getResources().getStringArray(R.array.list_categories);
			for (int i = 0; i < categories.length; i++) {
				String category = categories[i];
				CheckBox categoryBox = new CheckBox(getActivity());
				categoryBox.setText(category);
				categoryBox.setChecked(autoExpand.contains(i));
				final int indexForListener = i;
				categoryBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {

					@Override
					public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
						if (isChecked) {
							autoExpand.add((Integer) indexForListener);
						} else {
							autoExpand.remove((Integer) indexForListener);
						}
						result.setAutoExpand(autoExpand);
						manager.saveSettings(result);
					}
				});
				autoExpandLinearLayout.addView(categoryBox);
			}

			final TextView daysLabelTextView = (TextView) rootView.findViewById(R.id.daysLabelTextView);
			final CheckBox notifyCheckBox = (CheckBox) rootView.findViewById(R.id.notifyCheckbox);
			final SeekBar seekBar = (SeekBar) rootView.findViewById(R.id.daysSeekBar);
			seekBar.getProgressDrawable().setColorFilter(getResources().getColor(R.color.green), Mode.SRC_IN);
			seekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

				@Override
				public void onStopTrackingTouch(SeekBar seekBar) {
					result.setNotify(notifyCheckBox.isChecked());
					result.setDuration(seekBarValue);
					manager.saveSettings(result);
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
					result.setNotify(!result.isNotify());
					result.setDuration(seekBarValue);
					daysLabelTextView.setEnabled(result.isNotify());
					seekBar.setEnabled(result.isNotify());
					manager.saveSettings(result);
				}
			});
			notifyCheckBox.setChecked(result.isNotify());
			if (result.isNotify()) {
				seekBarValue = result.getDuration();
			} else {
				seekBarValue = 1;
			}
			setLabelForSeekBar(seekBarValue, daysLabelTextView);
			seekBar.setProgress(seekBarValue - 1);
			daysLabelTextView.setEnabled(result.isNotify());
			seekBar.setEnabled(result.isNotify());

			Button clearImageCacheButton = (Button) rootView.findViewById(R.id.clearImageCacheButton);
			clearImageCacheButton.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					CacheDeleter deleter = new CacheDeleter();
					deleter.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, (Void) null);
				}
			});

			Button backupDbButton = (Button) rootView.findViewById(R.id.backupDbButton);
			final Button restoreDbButton = (Button) rootView.findViewById(R.id.restoreDbButton);
			backupDbButton.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					try {
						copyFileWithSameName(SQLiteHelper.DB_PATH, Environment.getExternalStorageDirectory() + SQLiteHelper.BACKUP_PATH, SQLiteHelper.DB_NAME);
						restoreDbButton.setEnabled(true);
						Toast.makeText(getActivity(), getResources().getString(R.string.db_backuped), Toast.LENGTH_SHORT).show();
					} catch (IOException e) {
						Toast.makeText(getActivity(), getResources().getString(R.string.db_backup_error), Toast.LENGTH_SHORT).show();
					}
				}
			});

			File existingBackup = new File(Environment.getExternalStorageDirectory() + SQLiteHelper.BACKUP_PATH + SQLiteHelper.DB_NAME);
			restoreDbButton.setEnabled(existingBackup.exists());
			restoreDbButton.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					try {
						copyFileWithSameName(Environment.getExternalStorageDirectory() + SQLiteHelper.BACKUP_PATH, SQLiteHelper.DB_PATH, SQLiteHelper.DB_NAME);
						Toast.makeText(getActivity(), getResources().getString(R.string.db_restored), Toast.LENGTH_SHORT).show();
					} catch (IOException e) {
						Toast.makeText(getActivity(), getResources().getString(R.string.db_restore_error), Toast.LENGTH_SHORT).show();
					}
				}
			});
		}
	}

	private void copyFileWithSameName(String sourceDir, String destDir, String fileName) throws IOException {
		boolean success = true;
		File file = new File(destDir);
		if (file.exists()) {
			success = true;
		} else {
			success = file.mkdir();
		}
		if (success) {
			File sourceFile = new File(sourceDir + fileName);
			FileInputStream fis = new FileInputStream(sourceFile);
			File destFile = new File(destDir + fileName);
			OutputStream output = new FileOutputStream(destFile);
			byte[] buffer = new byte[1024];
			int length;
			while ((length = fis.read(buffer)) > 0) {
				output.write(buffer, 0, length);
			}
			output.flush();
			output.close();
			fis.close();
		}
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

	@Override
	public void refresh(View view) {
		Initializer initializer = new Initializer(view);
		initializer.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, (Void) null);
	}

	@Override
	public void filter(String filter) {
	}
}
