package com.aayush.authforge.authfordgeapi.common.location;

import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Service
public class LocationService {

    private final RestTemplate restTemplate = new RestTemplate();

public String getLocation(String ip) {

    if (ip == null ||
        ip.equals("127.0.0.1") ||
        ip.equals("0:0:0:0:0:0:0:1") ||
        ip.startsWith("192.168")) {

        return "Localhost";
    }

    try {

        String url = "https://ipapi.co/" + ip + "/json/";

        Map<String, Object> response =
                restTemplate.getForObject(url, Map.class);

        String city = (String) response.get("city");
        String country = (String) response.get("country_name");

        if (city == null && country == null) {
            return "Unknown";
        }

        return (city != null ? city : "")
                + (country != null ? ", " + country : "");

    } catch (RestClientException e) {
        return "Unknown";
    }
}
}
