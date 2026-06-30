#!/usr/bin/env python3
"""
Defence GIS Tracking System — GPS Position Simulator

Generates simulated GPS position updates for testing the asset tracking system.
Sends positions via HTTP POST to the backend Position API.

Usage:
    python gps_simulator.py [--url URL] [--interval SECONDS] [--assets N]

Default:
    URL:      http://localhost:8080/DefenceGIS/api/positions
    Interval: 5 seconds
    Assets:   4 (matching seed data IDs 1-4)
"""

import json
import time
import random
import math
import argparse
import urllib.request
import urllib.error

# ── Configuration ────────────────────────────────────────
DEFAULT_API_URL = "http://localhost:8080/DefenceGIS/api/positions"
DEFAULT_INTERVAL = 5  # seconds between updates
DEFAULT_NUM_ASSETS = 4

# Base location: Jodhpur, Rajasthan (DRDO context)
BASE_LAT = 26.2389
BASE_LNG = 73.0243

# ── Simulated Asset State ────────────────────────────────
class AssetSimulator:
    """Simulates a single asset moving with realistic GPS patterns."""

    def __init__(self, asset_id, lat, lng):
        self.asset_id = asset_id
        self.lat = lat
        self.lng = lng
        self.speed = random.uniform(10.0, 60.0)   # km/h
        self.heading = random.uniform(0, 360)       # degrees
        self.altitude = random.uniform(200, 500)    # meters

    def update(self):
        """Move the asset by a small random amount."""
        # Change heading gradually
        self.heading = (self.heading + random.uniform(-30, 30)) % 360

        # Calculate displacement (roughly meters)
        distance_m = self.speed * (DEFAULT_INTERVAL / 3600) * 1000  # meters
        delta_lat = (distance_m * math.cos(math.radians(self.heading))) / 111320
        delta_lng = (distance_m * math.sin(math.radians(self.heading))) / (111320 * math.cos(math.radians(self.lat)))

        self.lat += delta_lat
        self.lng += delta_lng

        # Random speed variation
        self.speed = max(5.0, min(80.0, self.speed + random.uniform(-5, 5)))
        self.altitude += random.uniform(-2, 2)

    def to_payload(self):
        """Return JSON-ready dict for the Position API."""
        return {
            "assetId": self.asset_id,
            "latitude": round(self.lat, 6),
            "longitude": round(self.lng, 6),
            "speed": round(self.speed, 1),
            "heading": round(self.heading, 1),
            "altitude": round(self.altitude, 1),
            "accuracy": round(random.uniform(2.0, 10.0), 1)
        }


def send_position(api_url, payload):
    """Send a position update via HTTP POST."""
    data = json.dumps(payload).encode('utf-8')
    req = urllib.request.Request(
        api_url,
        data=data,
        headers={"Content-Type": "application/json"},
        method="POST"
    )
    try:
        with urllib.request.urlopen(req, timeout=10) as resp:
            status = resp.status
            body = resp.read().decode('utf-8')
            return status, body
    except urllib.error.HTTPError as e:
        return e.code, e.read().decode('utf-8')
    except urllib.error.URLError as e:
        return 0, str(e.reason)


def main():
    parser = argparse.ArgumentParser(description="GPS Position Simulator for Defence GIS")
    parser.add_argument("--url", default=DEFAULT_API_URL, help="Backend API URL")
    parser.add_argument("--interval", type=int, default=DEFAULT_INTERVAL, help="Seconds between updates")
    parser.add_argument("--assets", type=int, default=DEFAULT_NUM_ASSETS, help="Number of assets")
    args = parser.parse_args()

    # Initialize asset simulators with slightly offset positions
    simulators = []
    for i in range(args.assets):
        lat = BASE_LAT + random.uniform(-0.02, 0.02)
        lng = BASE_LNG + random.uniform(-0.02, 0.02)
        sim = AssetSimulator(asset_id=i + 1, lat=lat, lng=lng)
        simulators.append(sim)

    print(f"╔══════════════════════════════════════════════════╗")
    print(f"║  Defence GIS — GPS Position Simulator            ║")
    print(f"╠══════════════════════════════════════════════════╣")
    print(f"║  API URL:  {args.url:<38} ║")
    print(f"║  Assets:   {args.assets:<38} ║")
    print(f"║  Interval: {args.interval}s{' ' * 36}║")
    print(f"╚══════════════════════════════════════════════════╝")
    print(f"\nSending positions... (Ctrl+C to stop)\n")

    cycle = 0
    try:
        while True:
            cycle += 1
            for sim in simulators:
                sim.update()
                payload = sim.to_payload()
                status, body = send_position(args.url, payload)

                symbol = "✅" if status == 201 else "⚠️" if status == 200 else "❌"
                print(f"  {symbol} Asset-{sim.asset_id:02d}  "
                      f"lat={payload['latitude']:.4f}  "
                      f"lng={payload['longitude']:.4f}  "
                      f"speed={payload['speed']}km/h  "
                      f"→ HTTP {status}")

            print(f"  ── Cycle {cycle} complete ──\n")
            time.sleep(args.interval)

    except KeyboardInterrupt:
        print(f"\n\nSimulator stopped after {cycle} cycles.")
        total = cycle * args.assets
        print(f"Total positions sent: {total}")


if __name__ == "__main__":
    main()
