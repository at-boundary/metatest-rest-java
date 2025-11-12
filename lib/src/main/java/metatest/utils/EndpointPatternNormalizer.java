package metatest.utils;

import java.util.regex.Pattern;

/**
 * Utility class for normalizing URL paths to endpoint patterns.
 *
 * Converts concrete URLs like "/api/v1/orders/123" to patterns like "/api/v1/orders/{id}".
 * This enables aggregating results across multiple tests that hit the same endpoint pattern
 * but with different parameter values.
 *
 * Example transformations:
 * - /api/v1/orders/123 -> /api/v1/orders/{id}
 * - /api/v1/users/abc-def-123 -> /api/v1/users/{id}
 * - /api/v1/products/p-12345/reviews/456 -> /api/v1/products/{id}/reviews/{id}
 * - /api/v1/search?query=test -> /api/v1/search (query params ignored)
 */
public class EndpointPatternNormalizer {

    // Pattern to match numeric IDs (e.g., 123, 456789, +1, -1, +123, -456)
    // Matches optional +/- followed by digits
    private static final Pattern NUMERIC_ID = Pattern.compile("^[+-]?\\d+$");

    // Pattern to match floating point numbers (e.g., 1.5, +2.3, -3.14)
    private static final Pattern FLOAT_ID = Pattern.compile("^[+-]?\\d+\\.\\d+$");

    // Pattern to match UUIDs (e.g., 550e8400-e29b-41d4-a716-446655440000)
    private static final Pattern UUID_PATTERN = Pattern.compile(
        "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$"
    );

    // Pattern to match alphanumeric IDs with separators (e.g., abc-123, p-12345, user_456)
    private static final Pattern ALPHANUMERIC_ID = Pattern.compile("^[a-zA-Z0-9]+-[0-9]+$|^[a-zA-Z]+-[a-zA-Z0-9-]+$|^[a-zA-Z0-9]+_[0-9]+$");

    // Pattern to match long alphanumeric strings (likely tokens or IDs)
    private static final Pattern LONG_ALPHANUMERIC = Pattern.compile("^[a-zA-Z0-9]{16,}$");

    private EndpointPatternNormalizer() {
    }

    /**
     * Normalizes a URL path to an endpoint pattern by replacing dynamic segments with {id}.
     *
     * @param path The URL path to normalize (e.g., "/api/v1/orders/123")
     * @return The normalized pattern (e.g., "/api/v1/orders/{id}")
     */
    public static String normalize(String path) {
        if (path == null || path.isEmpty()) {
            return path;
        }

        // Remove query parameters if present
        int queryIndex = path.indexOf('?');
        if (queryIndex != -1) {
            path = path.substring(0, queryIndex);
        }

        // Remove trailing slash for consistency
        if (path.length() > 1 && path.endsWith("/")) {
            path = path.substring(0, path.length() - 1);
        }

        // Split path into segments
        String[] segments = path.split("/");
        StringBuilder normalized = new StringBuilder();

        for (int i = 0; i < segments.length; i++) {
            String segment = segments[i];

            if (i == 0 && segment.isEmpty()) {
                // Leading slash - preserve it
                normalized.append("/");
                continue;
            }

            if (segment.isEmpty()) {
                // Skip empty segments (e.g., double slashes)
                continue;
            }

            // Check if segment looks like a dynamic parameter
            if (isLikelyDynamicParameter(segment)) {
                if (normalized.length() > 1) {
                    normalized.append("/");
                }
                normalized.append("{id}");
            } else {
                if (normalized.length() > 1) {
                    normalized.append("/");
                }
                normalized.append(segment);
            }
        }

        // Handle root path case
        if (normalized.length() == 0) {
            return "/";
        }

        return normalized.toString();
    }

    /**
     * Determines if a path segment is likely a dynamic parameter (ID, UUID, token, etc.)
     * rather than a static resource name.
     *
     * @param segment The path segment to check
     * @return true if the segment looks like a dynamic parameter
     */
    private static boolean isLikelyDynamicParameter(String segment) {
        // Empty segment is not a parameter
        if (segment == null || segment.isEmpty()) {
            return false;
        }

        // Common resource names that should NOT be treated as IDs
        // Check this FIRST before numeric patterns
        String lowerSegment = segment.toLowerCase();
        if (lowerSegment.equals("v1") || lowerSegment.equals("v2") || lowerSegment.equals("v3") ||
            lowerSegment.equals("api") || lowerSegment.startsWith("v") && lowerSegment.length() == 2) {
            return false;
        }

        // Numeric ID (e.g., "123", "456789", "+1", "-1")
        if (NUMERIC_ID.matcher(segment).matches()) {
            return true;
        }

        // Floating point numbers (e.g., "1.5", "+2.3", "-3.14")
        if (FLOAT_ID.matcher(segment).matches()) {
            return true;
        }

        // UUID (e.g., "550e8400-e29b-41d4-a716-446655440000")
        if (UUID_PATTERN.matcher(segment).matches()) {
            return true;
        }

        // Alphanumeric ID with separators (e.g., "abc-123", "p-12345", "user_456")
        if (ALPHANUMERIC_ID.matcher(segment).matches()) {
            return true;
        }

        // Long alphanumeric strings (likely tokens or encoded IDs)
        if (LONG_ALPHANUMERIC.matcher(segment).matches()) {
            return true;
        }

        return false;
    }

    /**
     * Checks if two paths match the same endpoint pattern.
     *
     * @param path1 First path
     * @param path2 Second path
     * @return true if both paths normalize to the same pattern
     */
    public static boolean matchesPattern(String path1, String path2) {
        return normalize(path1).equals(normalize(path2));
    }
}
