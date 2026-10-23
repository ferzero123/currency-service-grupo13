package com.cibertec.client;

import com.cibertec.dto.Currency;
import com.cibertec.dto.CurrencyDetail;
import com.cibertec.dto.Flags;
import com.cibertec.dto.external.CountryCurrencyPage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class CountryCurrencyClient {
    private final RestTemplate restTemplate;
    private final String baseUrl;
    private final String apiKey;

    @Autowired
    public CountryCurrencyClient(
            @Value("${restcountries.v5.base-url:https://api.restcountries.com/countries/v5}") String baseUrl,
            @Value("${restcountries.v5.api-key:${RESTCOUNTRIES_API_KEY:}}") String apiKey) {
        this(createRestTemplate(), baseUrl, apiKey);
    }

    CountryCurrencyClient(RestTemplate restTemplate, String baseUrl, String apiKey) {
        this.restTemplate = restTemplate;
        this.baseUrl = baseUrl.replaceAll("/+$", "");
        this.apiKey = apiKey == null ? "" : apiKey.trim();
    }

    private static RestTemplate createRestTemplate() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(5000);
        factory.setReadTimeout(15000);
        return new RestTemplate(factory);
    }

    public Currency getByCountryName(String name) {
        if (name == null || name.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El nombre del pais es obligatorio.");
        }
        if (apiKey.isBlank() || "rc_live_demo".equals(apiKey)) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "Configura una RESTCOUNTRIES_API_KEY propia en currency-service.");
        }

        URI uri = UriComponentsBuilder.fromUriString(baseUrl).pathSegment("name")
                .queryParam("q", "{name}")
                .queryParam("response_fields", "currencies,flag.url_png,flag.url_svg,flag.description")
                .queryParam("limit", 1)
                .encode().buildAndExpand(Map.of("name", name.trim())).toUri();
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(apiKey);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));

        CountryCurrencyPage response;
        try {
            response = restTemplate.exchange(uri, HttpMethod.GET,
                    new HttpEntity<Void>(headers), CountryCurrencyPage.class).getBody();
        } catch (HttpClientErrorException.NotFound ex) {
            throw notFound();
        } catch (HttpClientErrorException ex) {
            int status = ex.getStatusCode().value();
            if (status == 401 || status == 403) {
                throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                        "REST Countries rechazo la API key. Revisa RESTCOUNTRIES_API_KEY.");
            }
            if (status == 429) {
                throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                        "Se alcanzo el limite de consultas de REST Countries. Intenta mas tarde.");
            }
            throw upstreamFailure();
        } catch (RestClientException ex) {
            throw upstreamFailure();
        }

        if (response == null || Boolean.FALSE.equals(response.success())
                || response.data() == null || response.data().objects() == null) {
            throw upstreamFailure();
        }
        if (response.data().demo() != null) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "REST Countries devolvio una muestra demo. Configura una API key propia.");
        }
        if (response.data().objects().isEmpty()) {
            throw notFound();
        }

        // Se conserva el primer pais coincidente, como en la implementacion anterior.
        var country = response.data().objects().getFirst();
        if (country == null || country.currencies() == null || country.flag() == null) {
            throw upstreamFailure();
        }
        Map<String, CurrencyDetail> currencies = new LinkedHashMap<>();
        for (var item : country.currencies()) {
            if (item == null || item.code() == null || item.code().isBlank()
                    || item.name() == null || item.name().isBlank()) {
                throw upstreamFailure();
            }
            currencies.put(item.code(), new CurrencyDetail(item.name(), item.symbol()));
        }
        var flag = country.flag();
        return new Currency(currencies, new Flags(flag.png(), flag.svg(), flag.alt()));
    }

    private ResponseStatusException notFound() {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, "No se encontro informacion del pais.");
    }

    private ResponseStatusException upstreamFailure() {
        return new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                "No se pudo obtener una respuesta valida de REST Countries.");
    }
}
