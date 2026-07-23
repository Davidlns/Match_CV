package com.david.matchcv.filter;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Rate limiting básico por IP nos endpoints que chamam a IA.
 *
 * Algoritmo: janela fixa de 1 minuto com até 20 requisições por IP.
 * Se o limite for atingido, devolve 429 antes de chegar ao controller.
 * A identificação do IP respeita o header X-Forwarded-For (proxy/load balancer).
 *
 * Nota: implementação em memória — suficiente para MVP público. Em produção
 * com múltiplas instâncias, substituir por Redis + Bucket4j.
 */
@Component
@Order(1)
public class RateLimitFilter implements Filter {

    static final int LIMITE_POR_JANELA = 20;
    static final long JANELA_MS = 60_000L;

    private final ConcurrentHashMap<String, Janela> janelas = new ConcurrentHashMap<>();

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        if (ehEndpointDeIa(httpRequest.getRequestURI())) {
            String ip = resolverIp(httpRequest);
            Janela janela = janelas.computeIfAbsent(ip, k -> new Janela());
            if (!janela.tentar()) {
                httpResponse.setStatus(429);
                httpResponse.setContentType("application/json;charset=UTF-8");
                httpResponse.getWriter().write(
                        "{\"error\":\"Muitas requisições. Aguarde um momento antes de tentar novamente.\"}");
                return;
            }
        }

        chain.doFilter(request, response);
    }

    private boolean ehEndpointDeIa(String uri) {
        return uri.startsWith("/api/skills/")
                || uri.startsWith("/api/cv/")
                || uri.startsWith("/api/analise/")
                || uri.startsWith("/api/roadmap")
                || uri.equals("/api/ai/ping");
    }

    private String resolverIp(HttpServletRequest request) {
        String xff = request.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isBlank()) {
            return xff.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    /**
     * Janela de tempo fixa por IP. Reseta o contador quando a janela expira.
     * Double-checked locking garante que o reset acontece uma única vez por ciclo.
     */
    static class Janela {
        private final AtomicInteger contador = new AtomicInteger(0);
        private volatile long inicio = System.currentTimeMillis();

        boolean tentar() {
            long agora = System.currentTimeMillis();
            if (agora - inicio >= JANELA_MS) {
                synchronized (this) {
                    if (agora - inicio >= JANELA_MS) {
                        contador.set(0);
                        inicio = agora;
                    }
                }
            }
            return contador.incrementAndGet() <= LIMITE_POR_JANELA;
        }
    }
}
