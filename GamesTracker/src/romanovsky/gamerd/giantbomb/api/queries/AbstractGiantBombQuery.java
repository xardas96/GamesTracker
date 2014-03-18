package romanovsky.gamerd.giantbomb.api.queries;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

public abstract class AbstractGiantBombQuery<T> {
	protected String baseUrl;
	protected String apiKey;
	protected String[] fields;
	private int offset = 0;
	private int limit = 20;
	private int totalResults = 1;

	public AbstractGiantBombQuery(String baseUrl) {
		this.baseUrl = baseUrl;
	}

	public void setApiKey(String apiKey) {
		this.apiKey = apiKey;
	}

	public boolean reachedOffset() {
		return offset >= totalResults;
	}

	public int getTotalResults() {
		return totalResults;
	}

	public List<T> execute(boolean untilToday) throws Exception {
		List<T> result = new ArrayList<T>();
		Document doc = DocumentHelper.parseText(getResponse());
		Element root = doc.getRootElement();
		String statusValue = root.selectSingleNode("status_code").getText();
		int status = Integer.parseInt(statusValue);
		if (status == 1) {
			int results = Integer.valueOf(root.selectSingleNode("number_of_page_results").getText());
			totalResults = Integer.valueOf(root.selectSingleNode("number_of_total_results").getText());
			result.addAll(parseResponse(root, untilToday));
			offset += results;
		}
		return result;
	}

	protected abstract List<T> parseResponse(Element root, boolean untilToday);

	private URL buildQuery() throws MalformedURLException {
		StringBuilder sb = new StringBuilder(baseUrl);
		sb.append("?api_key=").append(apiKey);
		sb.append("&offset=").append(offset);
		sb.append("&limit=").append(limit);
		appendInfo(sb);
		URL url = new URL(sb.toString());
		return url;
	}

	protected String getResponse() throws Exception {
		URL url = buildQuery();
		InputStream is = url.openStream();
		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
		StringBuilder sb = new StringBuilder();
		String line = "";
		while ((line = reader.readLine()) != null) {
			sb.append(line);
		}
		return sb.toString();
	}

	protected abstract void appendInfo(StringBuilder infoBuilder);

}
