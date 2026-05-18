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

def test_performance():
    # Scenario A: 1000 Valid Users (Tests Cache Hits & Filter overhead)
    # Assuming user_0 to user_999 were created previously
    valid_users = [f"user_{i}" for i in range(1000)]
    
    # Scenario B: 1000 Invalid Users (Tests Cache Penetration & DB protection)
    invalid_users = [f"missing_user_{i}" for i in range(1000)]
    
    print("=========================================")
    print("SCENARIO A: VALID USERS (CACHE WARM-UP)")
    print("=========================================")
    for name, url_template in BASE_URLS.items():
        test_scenario(name, url_template, valid_users, "1000 Existing Users")
        
    print("\n=========================================")
    print("SCENARIO B: INVALID USERS (CACHE PENETRATION)")
    print("=========================================")
    for name, url_template in BASE_URLS.items():
        test_scenario(name, url_template, invalid_users, "1000 Non-Existing Users")

if __name__ == "__main__":
    test_performance()