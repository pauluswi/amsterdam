# ADR-004: Use Keycloak for OAuth2/JWT Authentication and Authorization

## Status
Accepted

## Context

The showcase application exposes REST APIs that require secure access. To demonstrate modern enterprise security practices and protect sensitive payment operations, authentication and authorization mechanisms are necessary. The chosen approach should be robust, industry-standard, and relatively easy to set up for a showcase environment. Implementing a full-fledged identity and access management (IAM) solution from scratch is out of scope and unnecessary for the project's goals.

## Decision

Keycloak will be used as the Identity and Access Management (IAM) solution to provide OAuth2/JWT-based authentication and authorization for all exposed REST APIs. It will act as an external, mock identity provider for the showcase.

## Rationale

1.  **Industry Standard**: OAuth2 and JWT are widely adopted standards for securing APIs, demonstrating adherence to modern security practices.
2.  **Feature Rich**: Keycloak provides comprehensive IAM features, including user management, role-based access control (RBAC), client management, and various authentication flows (e.g., client credentials, authorization code).
3.  **Ease of Integration**: Spring Security has excellent integration capabilities with OAuth2 resource servers, making it straightforward to secure Spring Boot applications with JWTs issued by Keycloak.
4.  **Showcase Suitability**: Keycloak can be easily deployed via Docker Compose, fitting well with the local deployment strategy of the showcase. It allows for realistic security scenarios without the complexity of managing a production-grade IAM system.
5.  **Extensibility**: The use of standard OAuth2/JWT means that Keycloak could be swapped out for another IAM provider (e.g., Auth0, Okta, AWS Cognito) with minimal changes to the application code, if needed in a different context.

## Consequences

### Positive
-   Demonstrates a secure API architecture using industry-standard protocols.
-   Provides robust authentication and authorization capabilities out-of-the-box.
-   Separates security concerns from business logic, leading to cleaner code.
-   Easy local setup and configuration for development and demonstration purposes.
-   Supports role-based access control, allowing fine-grained authorization.

### Negative
-   Adds an external dependency (Keycloak server) to the deployment environment, increasing operational complexity slightly.
-   Requires initial configuration of realms, clients, and users within Keycloak.
-   Developers need to understand OAuth2/JWT concepts and Keycloak configuration.
-   Potential for misconfiguration if security settings are not carefully managed.

## Implementation Notes

-   Keycloak will be deployed as a Docker container alongside other services in `docker-compose.yml`.
-   Spring Security will be configured in each microservice to act as an OAuth2 Resource Server, validating JWTs issued by Keycloak.
-   The `spring-boot-starter-oauth2-resource-server` dependency will be used.
-   API endpoints will be secured using `@PreAuthorize` annotations or method security expressions based on roles defined in Keycloak.
-   A dedicated Keycloak realm and client will be created for the showcase, with predefined users and roles for testing.
-   Environment variables will be used to configure Keycloak server URL and realm details in the Spring Boot applications.

## Alternatives Considered

1.  **Basic Authentication**:
    *   **Pros**: Simplest to implement.
    *   **Cons**: Less secure, does not scale well, not suitable for modern API security, and does not demonstrate enterprise-grade security.
2.  **API Keys**:
    *   **Pros**: Simple for client-to-server authentication.
    *   **Cons**: Lacks robust authorization features, difficult to manage user identities, and not an industry standard for user-facing APIs.
3.  **Custom Token-Based Authentication**:
    *   **Pros**: Full control over implementation.
    *   **Cons**: High development effort, prone to security vulnerabilities if not implemented perfectly, and reinvents the wheel.

## References

-   Keycloak Documentation: https://www.keycloak.org/documentation
-   Spring Security OAuth2 Resource Server: https://docs.spring.io/spring-security/reference/servlet/oauth2/resource-server/index.html
-   OAuth 2.0 Authorization Framework: https://datatracker.ietf.org/doc/html/rfc6749
-   JSON Web Token (JWT): https://datatracker.ietf.org/doc/html/rfc7519