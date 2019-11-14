package main.java;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.concurrent.Semaphore;
import java.util.*;

import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClients;
import org.apache.http.concurrent.FutureCallback;

public class RESTCall {

	public enum TypeHTTPCall {
		HTTP_POST,
		HTTP_PUT,
		HTTP_GET
	}

	private String targetUrl;
	private String sourceInterface;
	private String statusdescription;
	private int statuscode;
	private Map<String, String> headers = new HashMap<>();
	private CloseableHttpAsyncClient httpclient = null;
	private long firstReferenceTime;
	private int referenceCounterTime;
	private int globalCounterTime;
	private Semaphore mutexProtectCounter;
	private Semaphore mutexProtectTime;
	private String username;
	private String password;
	private CredentialsProvider credentialProvider = null;

	public RESTCall(String targetUrl, String sourceInterface) {

		this.targetUrl = targetUrl;
		this.sourceInterface = sourceInterface;
		this.firstReferenceTime = System.currentTimeMillis();
		this.mutexProtectCounter = new Semaphore(1);
		this.mutexProtectTime = new Semaphore(1);
		this.globalCounterTime = 0;
		this.username = "";
		this.password = "";
	}

	public void setTargetURL(String targetURL) {
		this.targetUrl = targetURL;
	}

	public void resetCounterReference() {
		this.referenceCounterTime = 0;
		this.firstReferenceTime = System.currentTimeMillis();
	}

	public void setAuthenticationCredentials(String username, String password) {
		this.username = username;
		this.password = password;

		if(username.length()==0 || password.length()==0)
			return;

		this.credentialProvider = new BasicCredentialsProvider();
		this.credentialProvider.setCredentials(new AuthScope(AuthScope.ANY), 
			new UsernamePasswordCredentials(this.username, this.password));
	}

	public void startAsyncClient() {
		try {

			if(this.credentialProvider==null) {
				this.httpclient = HttpAsyncClients.createDefault();
				System.out.println("Launched HTTP Async Standard Request (NO AUTH FORESEEN)");
			}
			else
			{
				this.httpclient = HttpAsyncClients.custom()
					.setDefaultCredentialsProvider(this.credentialProvider)
					.build();
					System.out.println("Launched HTTP Async CUSTOM Request, Username: "+username+", password: "+password);
				}
			this.httpclient.start();
		} catch (Exception exc) {

		}
	}

	public void addHeader(String key, String value) {
		headers.put(key, value);
	}

	public int post(String json) {
		try {
			HttpClient client = HttpClientBuilder.create().build();
			RequestConfig cfg = null;

			if (sourceInterface != null && !"ANY".equals(sourceInterface)) {
				NetworkInterface ni = NetworkInterface.getByName(sourceInterface);
				InetAddress source = ni.getInetAddresses().nextElement();
				RequestConfig.Builder cb = RequestConfig.custom().setLocalAddress(source);
				cfg = cb.build();
			}

			HttpPost post = new HttpPost(targetUrl);
			if (cfg != null) {
				post.setConfig(cfg);
			}

			// Set the content and headers
			StringEntity requestEntity = new StringEntity(json, ContentType.APPLICATION_JSON);
			post.setEntity(requestEntity);

			for (Map.Entry<String, String> header : headers.entrySet()) {
				post.addHeader(header.getKey(), header.getValue());
			}

			// Send it
			HttpResponse resp = client.execute(post);
			int status = resp.getStatusLine().getStatusCode();
			System.err.println("POST status: " + status);
			if (status == 500 || status == 405) {
				System.err.println("POST error, status: " + status + ": " + resp.getStatusLine().getReasonPhrase()
						+ ", content:\n" + json);
			} else {
			}
			statuscode = status;
			statusdescription = resp.getStatusLine().getReasonPhrase();
			return statuscode;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return -1;
	}

	public int RESTAsync(String url, TypeHTTPCall httpType, String json)
	{
		switch(httpType){
			case HTTP_GET:
				return 0;
			case HTTP_POST:
				return postAsync(url, json);

			case HTTP_PUT:
				return putAsync(url, json);
		}

		return -1;
	}

	public int postAsync(String url, String json) {
		try {
			RequestConfig cfg = null;

			if (sourceInterface != null && !"ANY".equals(sourceInterface)) {
				NetworkInterface ni = NetworkInterface.getByName(sourceInterface);
				InetAddress source = ni.getInetAddresses().nextElement();
				RequestConfig.Builder cb = RequestConfig.custom().setLocalAddress(source);
				cfg = cb.build();
			}

			HttpPost post = new HttpPost(url);
			if (cfg != null) {
				post.setConfig(cfg);
			}

			// Set the content and headers
			StringEntity requestEntity = new StringEntity(json, ContentType.APPLICATION_JSON);
			post.setEntity(requestEntity);

			for (Map.Entry<String, String> header : headers.entrySet()) {
				post.addHeader(header.getKey(), header.getValue());
			}

	
			// Send it
			// Future<HttpResponse> future =
			this.httpclient.execute(post, new FutureCallback<HttpResponse>() {

				@Override
				public void completed(final HttpResponse response) {
					try {
						// latch.countDown();
						mutexProtectTime.acquire();
						long intervalFromStart = System.currentTimeMillis() - firstReferenceTime;
						mutexProtectTime.release();

						mutexProtectTime.acquire();
						mutexProtectCounter.acquire();
						System.out.println(
							json+"|->Response: "+response.getStatusLine() + "-> TimeFromRef_ms: " + Long.toString(intervalFromStart)
										+ ", Count: " + Integer.toString(referenceCounterTime));
						mutexProtectTime.release();
						mutexProtectCounter.release();

						mutexProtectCounter.acquire();
						referenceCounterTime = referenceCounterTime + 1;
						mutexProtectCounter.release();
					} catch (Exception ex) {

					}
				}

				@Override
				public void failed(final Exception ex) {
					// latch.countDown();
					System.out.println(post.getRequestLine() + "->" + ex);
				}

				@Override
				public void cancelled() {
					// latch.countDown();
					System.out.println(post.getRequestLine() + " cancelled");
				}

			});

			// HttpResponse response = future.get();

			// httpclient.close();
		} catch (Exception exc) {
			System.out.println(String.format("Exception PostAsync: %s", exc.getMessage()));
		} finally {
			return 1;
		}
	}

	public int putAsync(String url, String json) {

		try {
			RequestConfig cfg = null;

			if (sourceInterface != null && !"ANY".equals(sourceInterface)) {
				NetworkInterface ni = NetworkInterface.getByName(sourceInterface);
				InetAddress source = ni.getInetAddresses().nextElement();
				RequestConfig.Builder cb = RequestConfig.custom().setLocalAddress(source);
				cfg = cb.build();
			}

			HttpPut put = new HttpPut(url);
			if (cfg != null) {
				put.setConfig(cfg);
			}

			// Set the content and headers
			StringEntity requestEntity = new StringEntity(json, ContentType.APPLICATION_JSON);
			put.setEntity(requestEntity);

			for (Map.Entry<String, String> header : headers.entrySet()) {
				put.addHeader(header.getKey(), header.getValue());
			}

			// Send it
			// Future<HttpResponse> future =
			this.httpclient.execute(put, new FutureCallback<HttpResponse>() {

				@Override
				public void completed(final HttpResponse response) {
					try {
						// latch.countDown();
						mutexProtectTime.acquire();
						long intervalFromStart = System.currentTimeMillis() - firstReferenceTime;
						mutexProtectTime.release();

						mutexProtectTime.acquire();
						mutexProtectCounter.acquire();
						System.out.println(
								response.getStatusLine() + "-> TimeFromRef_ms: " + Long.toString(intervalFromStart)
										+ ", Count: " + Integer.toString(referenceCounterTime)+", globalCounter: "+Integer.toString(globalCounterTime));
						mutexProtectTime.release();
						mutexProtectCounter.release();

						mutexProtectCounter.acquire();
						referenceCounterTime = referenceCounterTime + 1;
						globalCounterTime 		= globalCounterTime +1;
						mutexProtectCounter.release();
					} catch (Exception ex) {

					}
				}

				@Override
				public void failed(final Exception ex) {
					// latch.countDown();
					System.out.println(put.getRequestLine() + "->" + ex);
				}

				@Override
				public void cancelled() {
					// latch.countDown();
					System.out.println(put.getRequestLine() + " cancelled");
				}

			});

			// HttpResponse response = future.get();

			// httpclient.close();
		} catch (Exception exc) {
			System.out.println(String.format("Exception PutAsync: %s", exc.getMessage()));
		} finally {

			return 1;

		}
	}

	public int put(String json) {
		try {

			HttpClient client = HttpClientBuilder.create().build();
			RequestConfig cfg = null;

			if (sourceInterface != null && !"ANY".equals(sourceInterface)) {
				NetworkInterface ni = NetworkInterface.getByName(sourceInterface);
				InetAddress source = ni.getInetAddresses().nextElement();
				RequestConfig.Builder cb = RequestConfig.custom().setLocalAddress(source);
				cfg = cb.build();
			}

			HttpPut put = new HttpPut(targetUrl);
			if (cfg != null) {
				put.setConfig(cfg);
			}

			// Set the content and headers
			StringEntity requestEntity = new StringEntity(json, ContentType.APPLICATION_JSON);
			put.setEntity(requestEntity);

			for (Map.Entry<String, String> header : headers.entrySet()) {
				put.addHeader(header.getKey(), header.getValue());
			}

			// Send it
			HttpResponse resp = client.execute(put);
			int status = resp.getStatusLine().getStatusCode();
			System.err.println("PUT status: " + status);
			if (status == 500 || status == 405) {
				System.err.println("PUT error, status: " + status + ": " + resp.getStatusLine().getReasonPhrase()
						+ ", content:\n" + json);
			} else {
			}
			statuscode = status;
			statusdescription = resp.getStatusLine().getReasonPhrase();
			return statuscode;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return -1;
	}

	public String get() {
		try {
			HttpClient client = HttpClientBuilder.create().build();
			RequestConfig cfg = null;

			if (sourceInterface != null && !"ANY".equals(sourceInterface)) {
				NetworkInterface ni = NetworkInterface.getByName(sourceInterface);
				InetAddress source = ni.getInetAddresses().nextElement();
				RequestConfig.Builder cb = RequestConfig.custom().setLocalAddress(source);
				cfg = cb.build();
			}

			HttpGet get = new HttpGet(targetUrl);
			if (cfg != null) {
				get.setConfig(cfg);
			}

			HttpResponse resp = client.execute(get);

			BufferedReader rd = new BufferedReader(new InputStreamReader(resp.getEntity().getContent()));

			StringBuilder result = new StringBuilder();
			String line = "";
			while ((line = rd.readLine()) != null) {
				result.append(line);
			}
			return result.toString();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "";
	}

	public int delete() {
		try {

			HttpClient client = HttpClientBuilder.create().build();
			RequestConfig cfg = null;

			if (sourceInterface != null && !"ANY".equals(sourceInterface)) {
				NetworkInterface ni = NetworkInterface.getByName(sourceInterface);
				InetAddress source = ni.getInetAddresses().nextElement();
				RequestConfig.Builder cb = RequestConfig.custom().setLocalAddress(source);
				cfg = cb.build();
			}

			HttpDelete del = new HttpDelete(targetUrl);
			if (cfg != null) {
				del.setConfig(cfg);
			}

			// Send it
			HttpResponse resp = client.execute(del);
			int status = resp.getStatusLine().getStatusCode();
			statuscode = status;
			statusdescription = resp.getStatusLine().getReasonPhrase();
			return statuscode;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return -1;
	}

	public int getStatuscode() {
		return statuscode;
	}

	public String getStatusDescription() {
		return statusdescription;
	}

	public static void main(String[] args) {
		RESTCall rc = new RESTCall("http://monica-demo-api.herokuapp.com/api/sensor", "192.168.2.105");
		// RESTCall rcget = new
		// RESTCall("http://monica-demo-api.herokuapp.com/api/sensors",
		// "192.168.2.105");
		String body = "{\"id\": \"TEST\",\"clusterId\": \"TEST\", \"timestamp\": \"2017-05-09T09:08:57.847Z\", \"lat\": 0, \"lon\": 0, \"name\": \"monica_1\", \"type\": \"TAG\", \"status\": \"ACTIVE\"}";
		rc.post(body);
		// System.err.println("GOT: " + rcget.get());
	}

}
