package com.david.matchcv.exception;

/**
 * Exceção de domínio para qualquer falha ao falar com a IA da Anthropic.
 * Esconde os detalhes técnicos do SDK do resto da aplicação.
 */
public class AiException extends RuntimeException {

    public AiException(String message, Throwable cause) {
        super(message, cause);
    }
}
