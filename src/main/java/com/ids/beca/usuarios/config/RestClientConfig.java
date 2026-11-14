
package com.ids.beca.usuarios.config;

import java.io.IOException;
import java.net.HttpURLConnection;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import com.ids.beca.usuarios.integration.CatalogoClient;
import com.ids.beca.usuarios.integration.CatalogoProperties;

@Configuration
public class RestClientConfig {

    @Bean
    public RestClient restClient(RestClient.Builder builder,
            CatalogoProperties propiedades) {

        SimpleClientHttpRequestFactory factory =
                new SimpleClientHttpRequestFactory() {
                    @Override
                    protected void prepareConnection(
                            HttpURLConnection connection,
                            String httpMethod) throws IOException {

                        super.prepareConnection(connection, httpMethod);
                        connection.setInstanceFollowRedirects(false);
                    }
                };

        factory.setConnectTimeout(
                propiedades.getConnectionTimeoutMs());
        factory.setReadTimeout(
                propiedades.getReadTimeoutMs());

        return builder.requestFactory(factory).build();
    }

    @Bean
    public CatalogoClient catalogoClient(RestClient restClient,
            CatalogoProperties propiedades) {

        return new CatalogoClient(restClient, propiedades);
    }
}
