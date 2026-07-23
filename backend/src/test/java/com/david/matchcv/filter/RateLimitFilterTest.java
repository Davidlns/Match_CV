package com.david.matchcv.filter;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

class RateLimitFilterTest {

    private static final String ENDPOINT_IA = "/api/roadmap";
    private static final String ENDPOINT_LIVRE = "/api/hello";

    private final RateLimitFilter filter = new RateLimitFilter();

    @Test
    void devePermitirRequisicoesAteOLimite() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", ENDPOINT_IA);
        request.setRemoteAddr("10.0.0.1");

        for (int i = 0; i < RateLimitFilter.LIMITE_POR_JANELA; i++) {
            AtomicBoolean chainCalled = new AtomicBoolean(false);
            filter.doFilter(request, new MockHttpServletResponse(),
                    (req, res) -> chainCalled.set(true));
            assertThat(chainCalled.get())
                    .as("Requisição %d deveria passar".formatted(i + 1))
                    .isTrue();
        }
    }

    @Test
    void deveBloquearQuandoLimiteForAtingido() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", ENDPOINT_IA);
        request.setRemoteAddr("10.0.0.2");

        // Esgota o limite.
        for (int i = 0; i < RateLimitFilter.LIMITE_POR_JANELA; i++) {
            filter.doFilter(request, new MockHttpServletResponse(), (req, res) -> {});
        }

        // Próxima requisição deve ser bloqueada com 429.
        MockHttpServletResponse response = new MockHttpServletResponse();
        filter.doFilter(request, response, (req, res) -> {});

        assertThat(response.getStatus()).isEqualTo(429);
        assertThat(response.getContentAsString()).contains("error");
    }

    @Test
    void naoDeveAplicarLimiteEmEndpointSemIA() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", ENDPOINT_LIVRE);
        request.setRemoteAddr("10.0.0.3");

        // Muitas requisições em /api/hello nunca devem ser bloqueadas.
        for (int i = 0; i < RateLimitFilter.LIMITE_POR_JANELA + 10; i++) {
            AtomicBoolean chainCalled = new AtomicBoolean(false);
            filter.doFilter(request, new MockHttpServletResponse(),
                    (req, res) -> chainCalled.set(true));
            assertThat(chainCalled.get()).isTrue();
        }
    }

    @Test
    void deveUsarXForwardedForParaIdentificarIpReal() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", ENDPOINT_IA);
        request.addHeader("X-Forwarded-For", "203.0.113.5, 10.1.1.1");
        request.setRemoteAddr("127.0.0.1"); // proxy interno

        AtomicBoolean chainCalled = new AtomicBoolean(false);
        filter.doFilter(request, new MockHttpServletResponse(),
                (req, res) -> chainCalled.set(true));

        assertThat(chainCalled.get()).isTrue();
    }

    @Test
    void deveIsolarlimitesPorIpDistinto() throws Exception {
        MockHttpServletRequest reqA = new MockHttpServletRequest("POST", ENDPOINT_IA);
        reqA.setRemoteAddr("10.0.0.4");
        MockHttpServletRequest reqB = new MockHttpServletRequest("POST", ENDPOINT_IA);
        reqB.setRemoteAddr("10.0.0.5");

        // IP A esgota o limite.
        for (int i = 0; i < RateLimitFilter.LIMITE_POR_JANELA; i++) {
            filter.doFilter(reqA, new MockHttpServletResponse(), (req, res) -> {});
        }

        // IP B ainda deve ter acesso livre.
        AtomicBoolean chainCalled = new AtomicBoolean(false);
        filter.doFilter(reqB, new MockHttpServletResponse(),
                (req, res) -> chainCalled.set(true));
        assertThat(chainCalled.get()).isTrue();
    }

    @Test
    void deveCobrir_todosOsEndpointsDeIA() throws Exception {
        String[] endpointsDeIa = {
                "/api/skills/extract",
                "/api/skills/analyze",
                "/api/cv/extract",
                "/api/analise/completa",
                "/api/roadmap",
                "/api/ai/ping"
        };

        for (String endpoint : endpointsDeIa) {
            RateLimitFilter filtroFresco = new RateLimitFilter();
            MockHttpServletRequest request = new MockHttpServletRequest("POST", endpoint);
            request.setRemoteAddr("10.1.0.1");

            // Esgota o limite neste endpoint.
            for (int i = 0; i < RateLimitFilter.LIMITE_POR_JANELA; i++) {
                filtroFresco.doFilter(request, new MockHttpServletResponse(), (req, res) -> {});
            }

            MockHttpServletResponse response = new MockHttpServletResponse();
            filtroFresco.doFilter(request, response, (req, res) -> {});

            assertThat(response.getStatus())
                    .as("Endpoint %s deveria ter rate limiting".formatted(endpoint))
                    .isEqualTo(429);
        }
    }
}
