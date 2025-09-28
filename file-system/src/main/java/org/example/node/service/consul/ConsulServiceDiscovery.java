package org.example.node.service.consul;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.*;

@Service
public class ConsulServiceDiscovery {

    public static final String $_SPRING_CLOUD_CONSUL_HOST = "${spring.cloud.consul.host}";
    public static final String $_SPRING_CLOUD_CONSUL_PORT = "${spring.cloud.consul.port}";
    private final RestTemplate restTemplate;
    private final String consulUrl;

    @Autowired
    public ConsulServiceDiscovery(@Value($_SPRING_CLOUD_CONSUL_HOST) String host,
                                  @Value($_SPRING_CLOUD_CONSUL_PORT) int port,
                                  RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
        this.consulUrl = "http://" + host + ":" + port;
    }

    public List<String> getAllServiceNames() {
        Map<String, Object> response = restTemplate.getForObject(
                consulUrl + "/v1/catalog/services", Map.class);
        if (response == null) return Collections.emptyList();

        List<String> serviceNames = new ArrayList<>(response.keySet());
        serviceNames.remove("consul");
        return serviceNames;
    }

    public List<String> getAllServiceNodes() {
        List<String> nodes = new ArrayList<>();
        for (String service : getAllServiceNames()) {
            String url = consulUrl + "/v1/catalog/service/" + service;
            List<Map<String, Object>> instances = restTemplate.getForObject(url, List.class);
            if (instances != null) {
                for (Map<String, Object> instance : instances) {
                    String address = (String) instance.get("ServiceAddress");
                    Integer port = (Integer) instance.get("ServicePort");
                    nodes.add("http://" + address + ":" + port);
                }
            }
        }
        return nodes;
    }
}
