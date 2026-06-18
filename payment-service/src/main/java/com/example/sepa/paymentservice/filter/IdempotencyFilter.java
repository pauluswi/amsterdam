package com.example.sepa.paymentservice.filter;

import com.example.sepa.paymentservice.entity.IdempotencyKey;
import com.example.sepa.paymentservice.repository.IdempotencyKeyRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.HexFormat;
import java.util.Optional;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 1) // Run after CorrelationIdFilter
@RequiredArgsConstructor
@Slf4j
public class IdempotencyFilter extends OncePerRequestFilter {

    public static final String IDEMPOTENCY_KEY_HEADER = "Idempotency-Key";
    private final IdempotencyKeyRepository idempotencyKeyRepository;
    private final ObjectMapper objectMapper; // To serialize/deserialize response body

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String idempotencyKey = request.getHeader(IDEMPOTENCY_KEY_HEADER);

        if (idempotencyKey == null || idempotencyKey.isEmpty()) {
            // No idempotency key, proceed as normal
            filterChain.doFilter(request, response);
            return;
        }

        // Wrap request and response to cache content for hashing and storing
        ContentCachingRequestWrapper requestWrapper = new ContentCachingRequestWrapper(request);
        ContentCachingResponseWrapper responseWrapper = new ContentCachingResponseWrapper(response);

        String requestHash = calculateRequestHash(requestWrapper);

        Optional<IdempotencyKey> storedKey = idempotencyKeyRepository.findByKey(idempotencyKey);

        if (storedKey.isPresent()) {
            IdempotencyKey existingKey = storedKey.get();
            if (existingKey.getRequestHash().equals(requestHash)) {
                // Matching key and request hash, return stored response
                log.info("Idempotency key {} found, returning stored response (status: {})", idempotencyKey, existingKey.getResponseStatus());
                responseWrapper.setStatus(existingKey.getResponseStatus());
                responseWrapper.getWriter().write(existingKey.getResponseBody());
                responseWrapper.copyBodyToResponse(); // Important to write cached content to actual response
                return;
            } else {
                // Idempotency key exists but request hash does not match
                log.warn("Idempotency key {} found but request hash mismatch. Rejecting request.", idempotencyKey);
                responseWrapper.setStatus(HttpStatus.BAD_REQUEST.value());
                responseWrapper.getWriter().write("{\"error\": \"Idempotency key already used with a different request.\"}");
                responseWrapper.copyBodyToResponse();
                return;
            }
        }

        // Key not found, proceed with processing
        try {
            filterChain.doFilter(requestWrapper, responseWrapper);

            // After processing, store the response
            String responseBody = new String(responseWrapper.getContentAsByteArray(), responseWrapper.getCharacterEncoding());
            IdempotencyKey newKey = IdempotencyKey.builder()
                    .key(idempotencyKey)
                    .requestHash(requestHash)
                    .responseStatus(responseWrapper.getStatus())
                    .responseBody(responseBody)
                    .createdAt(Instant.now())
                    // .expiresAt(Instant.now().plus(Duration.ofHours(24))) // Optional: for cleanup
                    .build();
            idempotencyKeyRepository.save(newKey);
            log.info("Idempotency key {} and response stored.", idempotencyKey);

        } finally {
            // Ensure the response body is copied to the actual response
            responseWrapper.copyBodyToResponse();
        }
    }

    private String calculateRequestHash(ContentCachingRequestWrapper requestWrapper) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            // Include method, path, and request body in the hash
            String content = requestWrapper.getMethod() + requestWrapper.getRequestURI() + new String(requestWrapper.getContentAsByteArray(), requestWrapper.getCharacterEncoding());
            byte[] hash = digest.digest(content.getBytes());
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException | IOException e) {
            log.error("Error calculating request hash", e);
            throw new IllegalStateException("Failed to calculate request hash for idempotency", e);
        }
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        // Apply idempotency filter only to POST requests for payment initiation
        // You might want to adjust this based on your API design
        return !request.getMethod().equalsIgnoreCase("POST") || !request.getRequestURI().startsWith("/payments");
    }
}