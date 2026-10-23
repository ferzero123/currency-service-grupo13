package com.cibertec.client;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.*;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;
import java.net.URI;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

class CountryCurrencyClientTest {
    private RestTemplate restTemplate;
    private MockRestServiceServer server;
    private CountryCurrencyClient client;
    private static final String BASE = "https://api.restcountries.com/countries/v5";
    private static final String RESPONSE = """
            {"success":true,"data":{"objects":[{
              "currencies":[{"code":"PEN","name":"Peruvian sol","symbol":"S/","extra":"ignored"}],
              "flag":{"url_png":"https://flags.example/pe.png","url_svg":"https://flags.example/pe.svg",
                      "description":"Bandera del Peru","emoji":"PE"}
            }],"meta":{"more":false}}}
            """;

    @BeforeEach
    void setUp() {
        restTemplate = new RestTemplate();
        server = MockRestServiceServer.createServer(restTemplate);
        client = new CountryCurrencyClient(restTemplate, BASE, "test-key");
    }

    @Test
    void mapsV5CurrenciesAndFlagToExistingContract() {
        server.expect(requestTo(BASE + "/name?q=peru&response_fields=currencies,flag.url_png,flag.url_svg,flag.description&limit=1"))
                .andExpect(method(HttpMethod.GET))
                .andExpect(header(HttpHeaders.AUTHORIZATION, "Bearer test-key"))
                .andRespond(withSuccess(RESPONSE, MediaType.APPLICATION_JSON));
        var result = client.getByCountryName(" peru ");
        assertEquals("Peruvian sol", result.getCurrencies().get("PEN").getName());
        assertEquals("S/", result.getCurrencies().get("PEN").getSymbol());
        assertEquals("https://flags.example/pe.png", result.getFlags().getPng());
        assertEquals("https://flags.example/pe.svg", result.getFlags().getSvg());
        assertEquals("Bandera del Peru", result.getFlags().getAlt());
        server.verify();
    }

    @Test
    void encodesSearchInputWithoutInjectingParameters() {
        server.expect(requestTo(URI.create(BASE + "/name?q=Trinidad%20%26%20Tobago%2B&response_fields=currencies,flag.url_png,flag.url_svg,flag.description&limit=1")))
                .andRespond(withSuccess(RESPONSE, MediaType.APPLICATION_JSON));
        client.getByCountryName("Trinidad & Tobago+");
        server.verify();
    }

    @Test
    void mapsMultipleCurrencies() {
        server.expect(anything()).andRespond(withSuccess(RESPONSE.replace(
                "\"extra\":\"ignored\"}", "\"extra\":\"ignored\"},{\"code\":\"USD\",\"name\":\"US dollar\",\"symbol\":\"$\"}"),
                MediaType.APPLICATION_JSON));
        assertEquals(2, client.getByCountryName("peru").getCurrencies().size());
        server.verify();
    }

    @Test
    void missingKeyDoesNotCallProvider() {
        client = new CountryCurrencyClient(restTemplate, BASE, "");
        assertStatus(503);
        server.verify();
    }

    @Test
    void publicDemoKeyDoesNotCallProvider() {
        client = new CountryCurrencyClient(restTemplate, BASE, "rc_live_demo");
        assertStatus(503);
        server.verify();
    }

    @Test
    void rejectsBlankCountry() {
        assertEquals(400, assertThrows(ResponseStatusException.class,
                () -> client.getByCountryName(" ")).getStatusCode().value());
        server.verify();
    }

    @Test
    void rejectsDemoResponse() {
        server.expect(anything()).andRespond(withSuccess(
                RESPONSE.replace("\"meta\":", "\"_demo\":{},\"meta\":"), MediaType.APPLICATION_JSON));
        assertStatus(503);
        server.verify();
    }

    @Test
    void emptySearchReturns404() {
        server.expect(anything()).andRespond(withSuccess(
                "{\"success\":true,\"data\":{\"objects\":[]}}", MediaType.APPLICATION_JSON));
        assertStatus(404);
        server.verify();
    }

    @Test
    void provider404Returns404() {
        server.expect(anything()).andRespond(withStatus(HttpStatus.NOT_FOUND));
        assertStatus(404);
        server.verify();
    }

    @Test
    void rejectedKeyReturns503WithoutLeakingProviderBody() {
        server.expect(anything()).andRespond(withStatus(HttpStatus.UNAUTHORIZED)
                .body("sensitive-provider-body"));
        var error = assertThrows(ResponseStatusException.class, () -> client.getByCountryName("peru"));
        assertEquals(503, error.getStatusCode().value());
        assertFalse(error.toString().contains("sensitive-provider-body"));
        assertFalse(error.toString().contains("test-key"));
        server.verify();
    }

    @Test
    void quotaExceededReturns503() {
        server.expect(anything()).andRespond(withStatus(HttpStatus.TOO_MANY_REQUESTS));
        assertStatus(503);
        server.verify();
    }

    @Test
    void malformedEnvelopeReturns502() {
        server.expect(anything()).andRespond(withSuccess(
                "{\"success\":false,\"data\":null}", MediaType.APPLICATION_JSON));
        assertStatus(502);
        server.verify();
    }

    @Test
    void invalidCurrencyEntryReturns502() {
        server.expect(anything()).andRespond(withSuccess(RESPONSE.replace("\"code\":\"PEN\",", ""),
                MediaType.APPLICATION_JSON));
        assertStatus(502);
        server.verify();
    }

    @Test
    void upstreamServerErrorReturns502() {
        server.expect(anything()).andRespond(withServerError());
        assertStatus(502);
        server.verify();
    }

    private void assertStatus(int expected) {
        assertEquals(expected, assertThrows(ResponseStatusException.class,
                () -> client.getByCountryName("peru")).getStatusCode().value());
    }
}
