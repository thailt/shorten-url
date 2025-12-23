# URL Shortening Service

A high-performance, scalable URL shortening service built with Spring Boot, featuring advanced caching strategies, Bloom filter optimization, and distributed ID generation.

## Summary of Functionality

This service provides a URL shortening API that converts long URLs into short, manageable aliases. Key features include:

- **Create Short URLs**: Generate unique short aliases for long URLs with optional expiration dates
- **Redirect to Original URLs**: Fast lookup and HTTP redirect to original URLs using short aliases
- **Custom Aliases**: Support for user-provided custom aliases
- **Expiration Support**: Optional expiration dates for short URLs
- **High Performance**: Multi-layer caching with LRU cache, Redis, and Bloom filter optimization
- **Scalable Architecture**: Designed for distributed systems with Snowflake ID generation

## Technical Stack

- **Framework**: Spring Boot 4.0.0
- **Language**: Java 21
- **Build Tool**: Gradle 8.14+
- **Database**: MySQL 8.0 (persistent storage)
- **Cache**: Redis 8 (Bloom filter support, caching layer)
- **Containerization**: Docker & Docker Compose

## Key Technologies

### 1. **NanoID** - Unpredictable ID Generation
- **Purpose**: Generates cryptographically strong, URL-safe unique identifiers
- **Use Case**: Alternative key generation strategy for unpredictable short URLs
- **Benefits**: 
  - Collision-resistant
  - URL-safe characters
  - Fast generation

### 2. **Snowflake** - Distributed ID Generation
- **Purpose**: Generates unique, time-ordered IDs for distributed systems
- **Use Case**: Primary key generation strategy for scalable deployments
- **Benefits**:
  - Guaranteed uniqueness across distributed nodes
  - Time-ordered (sortable)
  - No coordination required between nodes
  - Encoded to base58 for shorter URLs

### 3. **Bloom Filter** - Key Existence Check
- **Purpose**: Probabilistic data structure to quickly check if a key might exist
- **Use Case**: Pre-filtering non-existent aliases before database/Redis lookup
- **Benefits**:
  - Extremely fast lookups (O(1))
  - Memory efficient
  - Reduces unnecessary database queries for 404 cases
  - False positives possible, but no false negatives

### 4. **Redis Cache** - Alias Lookup Caching
- **Purpose**: Fast in-memory cache for frequently accessed aliases
- **Use Case**: Caching alias-to-URL mappings to reduce database load
- **Benefits**:
  - Sub-millisecond lookup times
  - Reduces database query load
  - 10-day TTL for cached entries
  - Supports high-throughput read operations

### 5. **MySQL** - Persistent Storage
- **Purpose**: Reliable, persistent storage for all alias mappings
- **Use Case**: Primary data store with ACID guarantees
- **Benefits**:
  - Data durability
  - Transaction support
  - Relational data integrity
  - Connection pooling with HikariCP

## Architecture Overview

### Request Flow - Create Short URL
1. Generate unique key using Snowflake or NanoID
2. Check Bloom filter for potential collisions
3. Save to MySQL database
4. Add to Bloom filter
5. Cache in Redis (10-day TTL)
6. Store in in-memory LRU cache

### Request Flow - Get/Redirect
1. Check in-memory LRU cache (fastest)
2. Check Bloom filter (if not present, return 404 immediately)
3. Check Redis cache
4. Query MySQL database
5. Cache result in Redis and LRU cache
6. Return redirect response

## Getting Started

### Prerequisites

- Docker and Docker Compose
- Java 21+ (for local development)
- Gradle 8.14+ (for local development)

### Running with Docker Compose

Start all services (Java app, MySQL, Redis):

```bash
# Build and start all services
docker compose up -d --build

# View logs
docker compose logs -f app

# Stop all services
docker compose down

# Stop and remove volumes (clean slate)
docker compose down -v
```

The application will be available at `http://localhost:8080`

**Services:**
- Application: `http://localhost:8080`
- MySQL: `localhost:3306`
- Redis: `localhost:6379`
- Prometheus: `http://localhost:9090` (metrics collection)
- Grafana: `http://localhost:3000` (dashboards, default: admin/admin)

### Local Development

```bash
# Build the project
./gradlew build

# Run the application
./gradlew bootRun

# Run tests
./gradlew test
```

## API Endpoints

### Create Short URL
```bash
POST /app/api/create
Content-Type: application/json

{
  "url": "https://example.com/very/long/url",
  "alias": "custom-alias",  # Optional
  "expire": "2025-12-31 23:59:59"  # Optional
}
```

**Response:**
```json
{
  "url": "https://example.com/very/long/url",
  "alias": "custom-alias",
  "expire": "2025-12-31 23:59:59",
  "shortenUrl": "http://localhost:8080/custom-alias"
}
```

### Redirect to Original URL
```bash
GET /{alias}
```

Returns HTTP 302 redirect to the original URL, or 404 if alias not found.

## Load Testing with k6

### Prerequisites

Install k6:
```bash
# macOS
brew install k6

# Linux
sudo gpg -k
sudo gpg --no-default-keyring --keyring /usr/share/keyrings/k6-archive-keyring.gpg --keyserver hkp://keyserver.ubuntu.com:80 --recv-keys C5AD17C747E3415A3642D57D77C6C491D6AC1D69
echo "deb [signed-by=/usr/share/keyrings/k6-archive-keyring.gpg] https://dl.k6.io/deb stable main" | sudo tee /etc/apt/sources.list.d/k6.list
sudo apt-get update
sudo apt-get install k6
```

### Run Load Tests

#### 1. Standard Load Test
Creates 1000 aliases, then performs 100,000 GET requests (50% existing, 50% non-existing):

```bash
k6 run load-test.js
```

#### 2. Stress Test - Failed Requests
Tests system under high concurrent failed requests (404s):

```bash
k6 run stress-test-fail.js
```

This test gradually ramps up from 50 to 1000 virtual users, all requesting non-existing aliases to stress test the Bloom filter and error handling.

#### 3. Stress Test - Create Requests
Tests system under high concurrent create operations:

```bash
k6 run stress-test-create.js
```

This test gradually ramps up from 10 to 200 virtual users, all creating new aliases to stress test database write performance and ID generation.

### Custom Base URL

```bash
BASE_URL=http://localhost:8080 k6 run load-test.js
```

## Load Testing with wrk

### Prerequisites

Install wrk:
```bash
# macOS
brew install wrk

# Linux
sudo apt-get install wrk
# or compile from source
git clone https://github.com/wg/wrk.git
cd wrk && make
```

### Run wrk Load Tests

#### 1. Random GET Requests (2 minutes)
Generates maximum load with random aliases for 2 minutes:

```bash
# Using the shell script (recommended)
./wrk-random-get.sh

# Or directly with wrk
wrk -t12 -c400 -d120s -s wrk-random-get.lua --latency http://localhost:8080
```

**Parameters:**
- `-t12`: 12 threads (adjust based on CPU cores)
- `-c400`: 400 concurrent connections
- `-d120s`: 2 minutes duration
- `-s`: Lua script for random alias generation
- `--latency`: Show detailed latency statistics

#### 2. Custom Configuration

```bash
# More aggressive (higher throughput)
wrk -t$(nproc) -c800 -d120s -s wrk-random-get.lua --latency http://localhost:8080

# Less aggressive (lower resource usage)
wrk -t4 -c100 -d120s -s wrk-random-get.lua --latency http://localhost:8080
```

#### 3. With Existing Aliases (Optional)

If you have a list of existing aliases, create `aliases.txt` (one alias per line) and use:

```bash
wrk -t12 -c400 -d120s -s wrk-random-get-with-aliases.lua --latency http://localhost:8080
```

This will mix 50% existing aliases with 50% random aliases for more realistic testing.

#### 4. Create Aliases (POST Requests)

Test alias creation with POST requests for 2 minutes:

```bash
# Using the shell script (recommended)
./wrk-create-alias.sh

# Or directly with wrk
wrk -t12 -c400 -d120s -s wrk-create-alias.lua --latency http://localhost:8080
```

**With realistic URLs (matching k6 tests):**
```bash
wrk -t12 -c400 -d120s -s wrk-create-alias-realistic.lua --latency http://localhost:8080
```

**Note:** POST requests are typically slower than GET requests due to database writes. Adjust connection count (`-c`) if needed:
- Lower connections: `-c100` for write-heavy workloads
- Higher connections: `-c400` for maximum throughput

### wrk vs k6

- **wrk**: Best for maximum throughput, simple HTTP benchmarking, quick performance checks
- **k6**: Best for complex scenarios, CI/CD integration, detailed metrics and reporting

See `LOAD-TESTING-TOOLS.md` for detailed comparison.

### Test Scripts Overview

- **`load-test.js`**: Standard load test with mixed read operations
- **`stress-test-fail.js`**: Stress test for failed requests (404s)
- **`stress-test-create.js`**: Stress test for create operations

## Configuration

Key configuration in `application.properties`:

```properties
# Application
app.shorten-domain=localhost:8080
app.machine-id=1

# Database
spring.datasource.url=jdbc:mysql://localhost:3306/mydatabase
spring.datasource.username=myuser
spring.datasource.password=secret

# Redis
spring.data.redis.host=localhost
spring.data.redis.port=6379

# Connection Pool
spring.datasource.hikari.maximum-pool-size=90
spring.datasource.hikari.minimum-idle=30
```

## Performance Optimizations

1. **Multi-layer Caching**:
   - L1: In-memory LRU cache (5 entries, fastest)
   - L2: Redis cache (10-day TTL, sub-millisecond)
   - L3: MySQL database (persistent, slower)

2. **Bloom Filter Pre-filtering**:
   - Eliminates unnecessary database queries for non-existent aliases
   - Reduces database load by ~50% for 404 cases

3. **Connection Pooling**:
   - HikariCP with optimized pool settings
   - Prevents connection exhaustion under load

4. **Virtual Threads**:
   - Java 21 virtual threads enabled for better concurrency
   - Handles more concurrent requests with fewer OS threads

## Project Structure

```
src/main/java/com/trithai/utils/shortenurl/
├── controller/          # REST API endpoints
├── service/            # Business logic
│   └── impl/          # Service implementations
├── config/            # Configuration classes
├── dto/               # Data transfer objects
├── entity/            # JPA entities
├── exceptions/        # Exception handlers
└── Application.java   # Main application class
```

## Monitoring

The project includes Prometheus and Grafana for comprehensive monitoring. See [MONITORING.md](MONITORING.md) for detailed setup and usage.

### Quick Start

1. Start all services (includes monitoring):
   ```bash
   docker compose up -d --build
   ```

2. Access monitoring:
   - **Grafana**: http://localhost:3000 (admin/admin)
   - **Prometheus**: http://localhost:9090
   - **Application Metrics**: http://localhost:8080/actuator/prometheus

3. View pre-configured dashboard:
   - Login to Grafana
   - Navigate to Dashboards → Browse
   - Open "URL Shortening Service - Monitoring Dashboard"

### Monitored Components

- **Spring Boot Application**: JVM metrics, HTTP metrics, database connection pool
- **MySQL**: Connections, queries, performance metrics
- **Redis**: Memory usage, operations, cache hit/miss rates

## License

This project is part of a utility library for URL shortening services.
