package xardas.gamestracker.settings;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;

import xardas.gamestracker.R;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

public class SettingsManager {
	private Context context;
	private static final String SETTINGS = "settings.xml";

	public SettingsManager(Context context) {
		this.context = context;

	}

	public Settings loadSettings() {
		Settings settings = new Settings();
		File settingsFile = new File(context.getCacheDir() + File.separator + SETTINGS);
		if (settingsFile.exists()) {
			SAXReader reader = new SAXReader();
			try {
				Document settingsDocument = reader.read(settingsFile);
				Element root = settingsDocument.getRootElement();
				Node notifyNode = root.selectSingleNode("notify");
				if (notifyNode != null) {
					boolean notify = Boolean.valueOf(notifyNode.valueOf("@notify"));
					settings.setNotify(notify);
					if (notify) {
						int duration = Integer.valueOf(notifyNode.valueOf("@duration"));
						settings.setDuration(duration);
					}
				}
			} catch (DocumentException e) {
				Log.e(getClass().getSimpleName(), e.getMessage(), e);
			}
		}
		return settings;
	}

	public void saveSettings(Settings settings) {
		File settingsFile = new File(context.getCacheDir() + File.separator + SETTINGS);
		Document settingsDocument = DocumentHelper.createDocument();
		Element root = settingsDocument.addElement("root");
		Element notifyElement = root.addElement("notify");
		notifyElement.addAttribute("notify", String.valueOf(settings.isNotify()));
		notifyElement.addAttribute("duration", String.valueOf(settings.getDuration()));
		XMLWriter writer;
		try {
			writer = new XMLWriter(new FileWriter(settingsFile));
			writer.write(settingsDocument);
			writer.close();
			Toast.makeText(context, context.getResources().getString(R.string.settings_saved), Toast.LENGTH_SHORT).show();
		} catch (IOException e) {
			Log.e(getClass().getSimpleName(), e.getMessage(), e);
			Toast.makeText(context, context.getResources().getString(R.string.settings_not_saved), Toast.LENGTH_SHORT).show();
		}
	}
}