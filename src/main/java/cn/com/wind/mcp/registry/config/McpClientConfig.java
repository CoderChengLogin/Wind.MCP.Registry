package cn.com.wind.mcp.registry.config;

import java.nio.charset.StandardCharsets;
import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.web.client.RestTemplate;

@Configuration
public class McpClientConfig {

    @Bean
    @ConfigurationProperties(prefix = "mcp")
    public McpProperties mcpProperties() {
        return new McpProperties();
    }

    @Bean
    public RestTemplate mcpRestTemplate(RestTemplateBuilder builder, McpProperties mcpProperties) {
        RestTemplate restTemplate = builder
            .setConnectTimeout(Duration.ofMillis(mcpProperties.getClient().getTimeout()))
            .setReadTimeout(Duration.ofMillis(mcpProperties.getClient().getTimeout()))
            .build();

        restTemplate.getMessageConverters().add(0, new StringHttpMessageConverter(StandardCharsets.UTF_8));
        return restTemplate;
    }

    public static class McpProperties {
        private Server server = new Server();
        private Client client = new Client();

        public Server getServer() {return server;}

        public void setServer(Server server) {this.server = server;}

        public Client getClient() {return client;}

        public void setClient(Client client) {this.client = client;}

        public static class Server {
            private String url;

            public String getUrl() {return url;}

            public void setUrl(String url) {this.url = url;}
        }

        public static class Client {
            private int timeout = 30000;
            private String clientName = "aimarket-backend";

            public int getTimeout() {return timeout;}

            public void setTimeout(int timeout) {this.timeout = timeout;}

            public String getClientName() {return clientName;}

            public void setClientName(String clientName) {this.clientName = clientName;}
        }
    }
}