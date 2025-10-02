# Metatest API Key Configuration

Metatest now uses API key authentication for secure communication with the Metatest API server. This guide explains how to configure API keys in your project.

## Quick Setup

1. **Get your API key** from the Metatest web dashboard:
   - Register/login at the Metatest API
   - Create a project  
   - Get the API key from project settings: `GET /api/v1/projects/{id}/api-key`

2. **Configure authentication** using one of the methods below

## Configuration Methods

### Method 1: Properties File (Recommended)

Create `src/main/resources/metatest.properties`:
```properties
metatest.api.key=mt_proj_your_api_key_here
metatest.project.id=your-project-uuid-here
metatest.api.url=http://localhost:8080
```

### Method 2: Environment Variables

```bash
export METATEST_API_KEY=mt_proj_your_api_key_here
export METATEST_PROJECT_ID=your-project-uuid-here
export METATEST_API_URL=http://localhost:8080
```

### Method 3: System Properties

```bash
-Dmetatest.api.key=mt_proj_your_api_key_here
-Dmetatest.project.id=your-project-uuid-here
-Dmetatest.api.url=http://localhost:8080
```

### Method 4: Gradle Configuration (Future)

```gradle
metatest {
    apiKey = "mt_proj_your_api_key_here"
    projectId = "your-project-uuid-here"
    apiUrl = "http://localhost:8080"
}
```

## Example Usage

### Running Tests with Metatest

```bash
# With environment variables
export METATEST_API_KEY=mt_proj_abc123def456...
export METATEST_PROJECT_ID=550e8400-e29b-41d4-a716-446655440000
./gradlew test -DrunWithMetatest=true

# With system properties  
./gradlew test -DrunWithMetatest=true \
  -Dmetatest.api.key=mt_proj_abc123def456... \
  -Dmetatest.project.id=550e8400-e29b-41d4-a716-446655440000
```

### Example Test (No Code Changes Required)

Your existing tests work unchanged:

```java
@Test
public void testOrderCreation() {
    // Metatest automatically intercepts HTTP calls
    // and applies fault strategies configured via API
    OrderService orderService = new OrderService();
    Order order = orderService.createOrder("product-123", 2);
    assertNotNull(order.getId());
}
```

## Configuration Priority

Settings are loaded in this priority order:
1. **System properties** (`-Dmetatest.api.key=...`)
2. **Environment variables** (`METATEST_API_KEY=...`)  
3. **Properties file** (`metatest.properties`)
4. **Default values**

## Security Best Practices

- **Never commit API keys** to version control
- Use **environment variables** in CI/CD pipelines
- Use **properties files** for local development
- Add `metatest.properties` to `.gitignore`

## Complete Workflow

1. **Register** on Metatest web dashboard
2. **Create project** and get API key
3. **Configure fault strategies** via web interface
4. **Set API key** in your test project (env vars/properties)
5. **Run tests** with `-DrunWithMetatest=true`
6. **View results** in web dashboard

## Troubleshooting

### Error: "Metatest API key not configured"
- Ensure API key is set via properties file, env vars, or system properties
- Check that the key starts with `mt_proj_`

### Error: "Metatest project ID not configured"  
- Ensure project ID (UUID) is configured
- Get the project ID from the web dashboard

### Error: "Failed to fetch fault strategies"
- Verify API key is valid and not expired
- Check that API URL is correct and accessible
- Ensure project exists and API key belongs to it

### Network Issues
- Check firewall settings
- Verify API URL is accessible from test environment
- For localhost, ensure Metatest API is running

## Migration from Old Version

If migrating from hardcoded JWT tokens:
1. Remove any hardcoded credentials from code
2. Set up API key configuration as described above
3. Update any custom HTTP clients to use new authentication

## Support

For issues:
- Check configuration priority and spelling
- Verify API key format starts with `mt_proj_`
- Test API connectivity manually with curl
- Enable debug logging: `-Dmetatest.debug=true`