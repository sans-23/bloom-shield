import requests
import time
import redis
import statistics

# Initialize Redis client to flush cache
r = redis.Redis(host='localhost', port=6379, db=0)

BASE_URLS = {
    "DB Only (/api)": "http://localhost:8080/api/user/{}",
    "Cache + DB (/api/v1)": "http://localhost:8080/api/v1/user/{}",
    "Filter + Cache + DB (/api/v2)": "http://localhost:8080/api/v2/user/{}"
}

def flush_cache():
    r.flushall()
    print("--- Flushed Redis Cache ---")

def test_scenario(name, url_template, users, description):
    print(f"\nRunning: {name} - {description}")
    flush_cache()
    
    latencies = []
    start_time = time.time()
    
    # Use a session to avoid TCP connection overhead on every request
    session = requests.Session()
    
    for user in users:
        url = url_template.format(user)
        req_start = time.time()
        res = session.get(url)
        req_end = time.time()
        
        # We record the roundtrip latency
        latencies.append((req_end - req_start) * 1000) # in ms
        
    session.close()
    end_time = time.time()
    total_time = end_time - start_time
    
    avg_latency = statistics.mean(latencies)
    p95_latency = statistics.quantiles(latencies, n=100)[94] if len(latencies) >= 100 else max(latencies)
    
    print(f"Total Time: {total_time:.2f} s")
    print(f"Avg Latency: {avg_latency:.2f} ms")
    print(f"P95 Latency: {p95_latency:.2f} ms")

def test_performance(invalid_percent):
    # Number of total queries to simulate
    NUM_QUERIES = 20000
    
    # We want a high rate of repetition to show cache effects.
    # We pick from a pool of 100 unique users, querying each ~20 times.
    UNIQUE_POOL_SIZE = 100
    
    # Assuming user_0 to user_{UNIQUE_POOL_SIZE-1} exist in the DB
    valid_pool = [f"user_{i}" for i in range(UNIQUE_POOL_SIZE)]
    # These users do not exist in the DB
    invalid_pool = [f"missing_user_{i}" for i in range(UNIQUE_POOL_SIZE)]
    
    import random
    random.seed(42) # For reproducible results
    
    # Mixed "Real-World" Traffic
    num_invalid = int((invalid_percent / 100.0) * NUM_QUERIES)
    num_valid = NUM_QUERIES - num_invalid
    mixed_queries = [random.choice(valid_pool) for _ in range(num_valid)] + [random.choice(invalid_pool) for _ in range(num_invalid)]
    random.shuffle(mixed_queries)

    print("\n=========================================")
    print(f"REAL-WORLD MIXED TRAFFIC - {NUM_QUERIES} queries ({invalid_percent}% invalid)")
    print("=========================================")
    for name, url_template in BASE_URLS.items():
        test_scenario(name, url_template, mixed_queries, f"{NUM_QUERIES} Mixed Queries")

if __name__ == "__main__":
    import argparse
    parser = argparse.ArgumentParser(description='Run Bloom-Shield performance benchmarks.')
    parser.add_argument('--invalid-percent', type=float, default=20.0,
                        help='Percentage of queries that are for invalid users (default: 20.0)')
    args = parser.parse_args()
    
    test_performance(args.invalid_percent)