package org.springframework.boot.autoconfigure.web.servlet;

/**
 * Compatibility shim for Springdoc on Spring Boot 4 until the starter targets
 * the relocated Boot 4 WebMvcProperties type.
 */
public class WebMvcProperties {

    private String webjarsPathPattern = "/webjars/**";

    public String getWebjarsPathPattern() {
        return webjarsPathPattern;
    }

    public void setWebjarsPathPattern(String webjarsPathPattern) {
        this.webjarsPathPattern = webjarsPathPattern;
    }
}
