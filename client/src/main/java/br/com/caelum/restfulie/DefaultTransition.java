package br.com.caelum.restfulie;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * Default implementation of a transition.
 * 
 * @author guilherme silveira
 * @author lucas souza
 */
public class DefaultTransition implements Transition {

	private String rel;
	private String href;
	
	private static final Map<String,String> defaultMethods = new HashMap<String,String>();
	static {
		defaultMethods.put("latest", "GET");
		defaultMethods.put("show", "GET");
		defaultMethods.put("update", "POST");
		defaultMethods.put("cancel", "DELETE");
		defaultMethods.put("destroy", "DELETE");
		defaultMethods.put("suspend", "DELETE");
	}

	public DefaultTransition(String rel, String href) {
		this.rel = rel;
		this.href = href;
	}

	public String getHref() {
		return href;
	}

	public String getRel() {
		return rel;
	}

	public <T> Response execute(T arg) {
		// TODO 1: use httpclient new version to execute GET
		// TODO 1.5: return result
		// TODO 2: support POST and others by default
		// TODO 3: allow method override
		// TODO 4: GET should automatically de-serialize result
		// TODO 5: receive parameters by default
		// TODO 5.5: allow httpclient customization
		// TODO 6: support other methods appart from httpclient
		try {
			URL url = new URL(href);
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			connection.setDoOutput(false);
			String methodName = methodName();
			connection.setRequestMethod(methodName);
	        return new DefaultResponse(connection, methodName.equals("GET"));
		} catch (MalformedURLException e) {
			throw new TransitionException("Unable to execute transition " + rel + " @ " + href, e);
		} catch (ProtocolException e) {
			throw new TransitionException("Unable to execute transition " + rel + " @ " + href, e);
		} catch (IOException e) {
			throw new TransitionException("Unable to execute transition " + rel + " @ " + href, e);
		}


	}

	private String methodName() {
		if(defaultMethods.containsKey(rel)) {
			return defaultMethods.get(rel);
		}
		return "POST";
	}
	
	public <T> Response execute() {
		return execute(null);
	}

}