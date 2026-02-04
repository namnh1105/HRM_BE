#!/bin/bash

# VPS Setup Script for Worksphere Application
# Run this script on your VPS to set up the environment

set -e

echo "🚀 Starting Worksphere VPS Setup..."

# Check if running as root
if [ "$EUID" -ne 0 ]; then
    echo "Please run as root (sudo ./setup-vps.sh)"
    exit 1
fi

# Install Java 17
echo "📦 Installing Java 17..."
apt-get update
apt-get install -y openjdk-17-jdk-headless

# Verify Java installation
java -version

# Create worksphere user
echo "👤 Creating worksphere user..."
if ! id "worksphere" &>/dev/null; then
    useradd -r -s /bin/false worksphere
fi

# Create application directories
echo "📁 Creating application directories..."
mkdir -p /opt/worksphere/backup
mkdir -p /opt/worksphere/logs

# Set permissions
chown -R worksphere:worksphere /opt/worksphere
chmod 755 /opt/worksphere

# Create .env file template
echo "📝 Creating environment file template..."
cat > /opt/worksphere/.env << 'ENVEOF'
# Application Stage Configuration
STAGE=production

# Database Configuration
DB_HOST=localhost
DB_PORT=5432
DB_NAME=worksphere_db
DB_USERNAME=postgres
DB_PASSWORD=your_database_password

# Google OAuth2 Configuration
GOOGLE_OAUTH2_CLIENT_ID=your_google_client_id
GOOGLE_OAUTH2_CLIENT_SECRET=your_google_client_secret
GOOGLE_OAUTH2_REDIRECT_URI=https://your-domain.com/login/oauth2/code/google

# Frontend Configuration
FRONTEND_URL=https://your-frontend-domain.com
OAUTH_SUCCESS_REDIRECT_PATH=/oauth/success

# Redis Configuration
REDIS_HOST=localhost
REDIS_PORT=6379
REDIS_PASSWORD=

# JWT Configuration
JWT_SECRET=your-super-secret-key-for-jwt-tokens-min-32-characters-base64-encoded
JWT_ACCESS_TOKEN_EXPIRATION=900000
JWT_REFRESH_TOKEN_EXPIRATION=604800000

# Server Configuration
SERVER_PORT=8080

# Database Show SQL Configuration
SHOW_SQL=false
FORMAT_SQL=false

# Application Configuration
APP_NAME=Worksphere API
APP_VERSION=1.0.0
APP_DESCRIPTION=Authentication and User Management API
ENVEOF

chown worksphere:worksphere /opt/worksphere/.env
chmod 600 /opt/worksphere/.env

# Copy and enable systemd service
echo "⚙️ Setting up systemd service..."
cp worksphere.service /etc/systemd/system/worksphere.service
systemctl daemon-reload
systemctl enable worksphere

echo ""
echo "✅ VPS Setup Complete!"
echo ""
echo "📋 Next Steps:"
echo "1. Edit /opt/worksphere/.env with your actual configuration values"
echo "2. Set up your database (PostgreSQL) and Redis"
echo "3. Configure your firewall to allow traffic on port 8080 (or your configured port)"
echo "4. Set up a reverse proxy (Nginx) for HTTPS"
echo "5. Deploy the application using GitHub Actions"
echo ""
echo "🔧 Useful Commands:"
echo "  - Start:   sudo systemctl start worksphere"
echo "  - Stop:    sudo systemctl stop worksphere"
echo "  - Status:  sudo systemctl status worksphere"
echo "  - Logs:    sudo journalctl -u worksphere -f"
echo ""

