# Crypto Wallet Management System

A Spring Boot application for managing cryptocurrency wallets with real-time price updates and portfolio simulation capabilities.

## 🚀 Features

- **Wallet Management**: Create unique wallets per email address
- **Asset Management**: Add cryptocurrency assets to wallets with real-time price validation
- **Price Updates**: Automated price updates from CoinCap API with configurable intervals
- **Portfolio Simulation**: Simulate wallet performance over time periods
- **Concurrent Processing**: Multi-threaded price updates (max 3 concurrent requests)
- **REST API**: Complete RESTful API for all operations

## 📋 Prerequisites

- **Java 17+** 
- **Docker & Docker Compose** (for database)
- **CoinCap API Key** (free registration required)

## 🔑 API Key Setup

### Get Your Free CoinCap API Key

1. Visit: https://pro.coincap.io/signup
2. Sign up for a free account
3. Copy your API key from the dashboard

### Configure API Key

#### Option 1: Environment Variable (Recommended)
```bash
export COINCAP_API_KEY=your_actual_api_key_here
```

#### Option 2: Application Properties
Edit `src/main/resources/application.properties`:
```ini
coincap.api-key=your_actual_api_key_here
```

⚠️ **Never commit API keys to version control!**

## 🏃‍♂️ Quick Start

### 1. Clone and Setup
```bash
git clone <your-repository-url>
cd crypto-wallet
```

### 2. Start Database
```bash
# Start PostgreSQL in Docker
./start-db.sh

# Or manually:
docker-compose -f docker-compose-db-only.yml up -d
```

### 3. Set API Key
```bash
export COINCAP_API_KEY=your_actual_api_key_here
```

### 4. Run Application
```bash
./gradlew bootRun
```

The application will be available at: http://localhost:8080

## 📚 API Documentation (Swagger)

Interactive API documentation is available via Swagger UI:

### Access Swagger UI
Once the application is running, visit:
- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **OpenAPI JSON**: http://localhost:8080/api-docs

### Features
- ✅ Interactive API testing
- ✅ Request/response examples
- ✅ Schema definitions
- ✅ Try out endpoints directly from the browser

### Quick Start with Swagger
1. Start the application (`./gradlew bootRun`)
2. Open http://localhost:8080/swagger-ui.html in your browser
3. Explore and test all available endpoints
4. Use the "Try it out" button to execute requests

## 🐳 Docker Deployment

### Full Docker Setup (Database + Application)
```bash
# Copy environment file
cp .env.example .env

# Edit .env and add your API key:
# COINCAP_API_KEY=your_actual_api_key_here

# Deploy everything
./deploy.sh
```

### Database Only (for local development)
```bash
# Start just the database
./start-db.sh

# Run application locally
./gradlew bootRun
```

## 📊 Database Configuration

### Local Development
- **Host**: localhost:5432
- **Database**: crypto_wallet
- **Username**: crypto_user
- **Password**: crypto_password

### Connection String
```
jdbc:postgresql://localhost:5432/crypto_wallet
```

## ⚙️ Configuration

### Price Update Interval
Configure how often prices are updated (default: 5 minutes):

```ini
# In application.properties
price.update.interval.in.minutes=5

# Examples:
# Every 1 minute: price.update.interval.in.minutes=1
# Every 15 minutes: price.update.interval.in.minutes=15
```

### Environment Variables
| Variable | Description | Default |
|----------|-------------|---------|
| `COINCAP_API_KEY` | CoinCap API key (required) | - |
| `PRICE_UPDATE_INTERVAL` | Price update interval in minutes | 5 |

## 📅 Date Format

### Simulation Requests
The simulation endpoint accepts dates in **ISO 8601 format** (`YYYY-MM-DD`):

- **Format**: `YYYY-MM-DD`
- **Examples**: `"2025-01-07"`, `"2024-12-25"`, `"2025-11-03"`
- **Default**: If `date` is `null` or omitted, uses current date

### Example with Date
```bash
curl -X POST http://localhost:8080/api/wallets/simulate \
  -H "Content-Type: application/json" \
  -d '{
    "assets": [
      {
        "symbol": "BTC",
        "quantity": 0.5,
        "value": 35000
      }
    ],
    "date": "2025-01-07"
  }'
```

### Example without Date (uses current date)
```bash
curl -X POST http://localhost:8080/api/wallets/simulate \
  -H "Content-Type: application/json" \
  -d '{
    "assets": [
      {
        "symbol": "BTC",
        "quantity": 0.5,
        "value": 35000
      }
    ]
  }'
```

## 🌐 API Endpoints

### Health Check
```bash
GET /api/wallets/health
```

### Create Wallet
```bash
POST /api/wallets
Content-Type: application/json

{
  "email": "user@example.com"
}
```

### Add Asset to Wallet
```bash
POST /api/wallets/{email}/assets
Content-Type: application/json

{
  "symbol": "BTC",
  "quantity": 1.5,
  "price": 50000.00
}
```

### Get Wallet
```bash
GET /api/wallets/{email}
```

### Simulate Wallet Performance
```bash
POST /api/wallets/simulate
Content-Type: application/json

{
  "assets": [
    {
      "symbol": "BTC",
      "quantity": 0.5,
      "value": 35000
    },
    {
      "symbol": "ETH",
      "quantity": 4.25,
      "value": 15310.71
    }
  ],
  "date": "2025-01-07"
}
```

**Date Format**: ISO 8601 format (`YYYY-MM-DD`). If omitted, defaults to current date.

**Examples**:
- `"2025-01-07"` (January 7, 2025)
- `"2024-12-25"` (December 25, 2024)
- `null` or omitted (uses current date)

## 📝 API Examples

### Create a Wallet
```bash
curl -X POST http://localhost:8080/api/wallets \
  -H "Content-Type: application/json" \
  -d '{"email": "john@example.com"}'
```

### Add Bitcoin to Wallet
```bash
curl -X POST http://localhost:8080/api/wallets/john@example.com/assets \
  -H "Content-Type: application/json" \
  -d '{
    "symbol": "BTC",
    "quantity": 0.5,
    "price": 50000.00
  }'
```

### Get Wallet Info
```bash
curl http://localhost:8080/api/wallets/john@example.com
```

**Response:**
```json
{
  "id": "123",
  "email": "john@example.com",
  "total": 158000.00,
  "assets": [
    {
      "symbol": "BTC",
      "quantity": 1.5,
      "price": 100000.00,
      "value": 150000.00
    },
    {
      "symbol": "ETH",
      "quantity": 2,
      "price": 4000,
      "value": 8000
    }
  ]
}
```

## ⚡ Performance Features

### Concurrent Price Updates
- Maximum 3 concurrent API requests to CoinCap
- Batched processing for multiple assets
- Configurable update intervals

### Caching
- Asset price caching to reduce API calls
- Database connection pooling
- Optimized database queries with indexes

## 🧪 Testing

### Run All Tests
```bash
./gradlew test
```

### Run Specific Test Categories
```bash
# Unit tests only
./gradlew test --tests "*Test"

# Integration tests
./gradlew test --tests "*IntegrationTest"

# Repository tests
./gradlew test --tests "*RepositoryTest"
```

## 🔧 Development Tools

### Database Access
```bash
# Connect to database
docker-compose -f docker-compose-db-only.yml exec database psql -U crypto_user -d crypto_wallet

# View database logs
docker-compose -f docker-compose-db-only.yml logs -f database
```

### Application Logs
```bash
# View application logs
./gradlew bootRun --debug

# Or with Docker
docker-compose logs -f app
```

## 🚨 Error Handling

The API returns structured error responses:

### Wallet Not Found (404)
```json
{
  "timestamp": "2024-01-01T12:00:00",
  "status": 404,
  "error": "Wallet Not Found",
  "message": "Wallet not found for email: user@example.com"
}
```

### Invalid Asset Symbol (400)
```json
{
  "timestamp": "2024-01-01T12:00:00",
  "status": 400,
  "error": "Invalid Asset Symbol",
  "message": "Price not found for symbol: INVALID. Please verify the symbol exists on CoinCap."
}
```

### Email Already Has Wallet (409)
```json
{
  "timestamp": "2024-01-01T12:00:00",
  "status": 409,
  "error": "Wallet Already Exists",
  "message": "Email 'user@example.com' already has a wallet associated. Each email can only have one wallet."
}
```

## 🔒 Security Notes

- Never commit API keys to version control
- Use environment variables for sensitive configuration
- API keys should have minimal required permissions
- Consider rate limiting for production deployments

## 📚 Technologies Used

- **Java 17**
- **Spring Boot 3.x**
- **Spring Data JPA**
- **PostgreSQL**
- **Flyway** (database migrations)
- **Quartz Scheduler** (price updates)
- **Docker & Docker Compose**
- **Gradle** (build tool)


### API Key Issues
```bash
# Verify API key is set
echo $COINCAP_API_KEY

# Test API key manually
curl "https://api.coincap.io/v2/assets/bitcoin" \
  -H "Authorization: Bearer $COINCAP_API_KEY"
```

## 🚀 Quick Commands Summary

```bash
# Setup
export COINCAP_API_KEY=your_key_here
./start-db.sh

# Run
./gradlew bootRun

# Test
curl http://localhost:8080/api/wallets/health

# Create wallet
curl -X POST http://localhost:8080/api/wallets \
  -H "Content-Type: application/json" \
  -d '{"email": "test@example.com"}'
```
