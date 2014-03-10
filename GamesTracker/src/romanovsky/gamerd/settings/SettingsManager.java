package romanovsky.gamerd.settings;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;

import romanovsky.gamerd.R;
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
				Node autoExpandNode = root.selectSingleNode("expand");
				List<Integer> autoExpandList = new ArrayList<Integer>();
				if (autoExpandNode != null) {
					String autoExpandValue = autoExpandNode.valueOf("@autoExpand");
					if (!autoExpandValue.equals("")) {
						String[] split = autoExpandValue.split(",");
						for (String autoExpandSplit : split) {
							autoExpandList.add(Integer.valueOf(autoExpandSplit));
						}
					}
				}
				settings.setAutoExpand(autoExpandList);
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
		StringBuilder autoExpandBuilder = new StringBuilder();
		for (Integer autoExpand : settings.getAutoExpand()) {
			autoExpandBuilder.append(autoExpand).append(",");
		}
		if (autoExpandBuilder.length() > 0) {
			autoExpandBuilder.setLength(autoExpandBuilder.length() - 1);
		}
		Element autoExpandNode = root.addElement("expand");
		autoExpandNode.addAttribute("autoExpand", autoExpandBuilder.toString());
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