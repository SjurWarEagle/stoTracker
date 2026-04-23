#!/bin/bash
# Stop current container, rebuild and start fresh

echo "Stopping existing container..."
docker compose down

echo "Building new image..."
docker compose build --no-cache

echo "Starting application..."
docker compose up -d

echo "stoTracker is starting on http://localhost:4545"