package com.ids.beca.usuarios.integration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "integracion.catalogo")
public class CatalogoProperties {

	private static final int DEFAULT_CONNECTION_TIMEOUT = 5000;
	private static final int DEFAULT_READ_TIMEOUT = 10000;

	private String url;
	private int connectionTimeoutMs = DEFAULT_CONNECTION_TIMEOUT;
	private int readTimeoutMs = DEFAULT_READ_TIMEOUT;

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public int getConnectionTimeoutMs() {
		return connectionTimeoutMs;
	}

	public void setConnectionTimeoutMs(int connectionTimeoutMs) {
		this.connectionTimeoutMs = connectionTimeoutMs;
	}

	public int getReadTimeoutMs() {
		return readTimeoutMs;
	}

	public void setReadTimeoutMs(int readTimeoutMs) {
		this.readTimeoutMs = readTimeoutMs;
	}

	public boolean isConfigured() {
		return url != null && !url.isBlank();
	}

}
