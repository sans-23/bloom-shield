import argparse
import requests
import time
import redis
import statistics
import random
import sys

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
    
    return {
        "total_time": total_time,
        "avg_latency": avg_latency,
        "p95_latency": p95_latency
    }

def test_performance(invalid_percent, num_queries=20000):
    # We want a high rate of repetition to show cache effects.
    UNIQUE_POOL_SIZE = 100
    
    # Assuming user_0 to user_{UNIQUE_POOL_SIZE-1} exist in the DB
    valid_pool = [f"user_{i}" for i in range(UNIQUE_POOL_SIZE)]
    # These users do not exist in the DB
    invalid_pool = [f"missing_user_{i}" for i in range(UNIQUE_POOL_SIZE)]
    
    random.seed(42) # For reproducible results
    
    # Mixed "Real-World" Traffic
    num_invalid = int((invalid_percent / 100.0) * num_queries)
    num_valid = num_queries - num_invalid
    mixed_queries = [random.choice(valid_pool) for _ in range(num_valid)] + [random.choice(invalid_pool) for _ in range(num_invalid)]
    random.shuffle(mixed_queries)

    print("\n=========================================")
    print(f"REAL-WORLD MIXED TRAFFIC - {num_queries} queries ({invalid_percent}% invalid)")
    print("=========================================")
    for name, url_template in BASE_URLS.items():
        test_scenario(name, url_template, mixed_queries, f"{num_queries} Mixed Queries")

def run_sweep():
    percentages = list(range(5, 105, 5))
    results = {name: {"total_time": [], "avg_latency": [], "p95_latency": []} for name in BASE_URLS.keys()}
    
    # We use 2000 queries per step to ensure the sweep finishes in a reasonable time.
    NUM_QUERIES = 2000
    UNIQUE_POOL_SIZE = 100
    
    valid_pool = [f"user_{i}" for i in range(UNIQUE_POOL_SIZE)]
    invalid_pool = [f"missing_user_{i}" for i in range(UNIQUE_POOL_SIZE)]
    
    random.seed(42)
    
    for pct in percentages:
        print(f"\n{'='*50}\nRUNNING SWEEP FOR {pct}% INVALID TRAFFIC\n{'='*50}")
        
        num_invalid = int((pct / 100.0) * NUM_QUERIES)
        num_valid = NUM_QUERIES - num_invalid
        
        mixed_queries = [random.choice(valid_pool) for _ in range(num_valid)] + \
                        [random.choice(invalid_pool) for _ in range(num_invalid)]
        random.shuffle(mixed_queries)
        
        for name, url_template in BASE_URLS.items():
            metrics = test_scenario(name, url_template, mixed_queries, f"{NUM_QUERIES} Mixed Queries ({pct}% invalid)")
            results[name]["total_time"].append(metrics["total_time"])
            results[name]["avg_latency"].append(metrics["avg_latency"])
            results[name]["p95_latency"].append(metrics["p95_latency"])
            
    # Plotting the results
    try:
        import matplotlib.pyplot as plt
        
        fig, axs = plt.subplots(3, 1, figsize=(10, 15))
        
        metrics_to_plot = ["total_time", "avg_latency", "p95_latency"]
        titles = ["Total Time (s)", "Average Latency (ms)", "P95 Latency (ms)"]
        
        for idx, metric in enumerate(metrics_to_plot):
            ax = axs[idx]
            for name, data in results.items():
                ax.plot(percentages, data[metric], marker='o', label=name)
            
            ax.set_title(f"{titles[idx]} vs Invalid Traffic Percentage")
            ax.set_xlabel("Invalid Traffic (%)")
            ax.set_ylabel(titles[idx])
            ax.legend()
            ax.grid(True)
            
        plt.tight_layout()
        plt.savefig("benchmark_results.png")
        print("\nGraphs successfully saved to benchmark_results.png")
    except ImportError:
        print("\nmatplotlib is not installed. Please run 'pip install matplotlib' to generate graphs.")

if __name__ == "__main__":
    parser = argparse.ArgumentParser(description='Run Bloom-Shield performance benchmarks.')
    parser.add_argument('--invalid-percent', type=float, default=20.0,
                        help='Percentage of queries that are for invalid users (default: 20.0)')
    parser.add_argument('--sweep', action='store_true',
                        help='Run a sweep from 5% to 100% invalid traffic and generate graphs')
    args = parser.parse_args()
    
    if args.sweep:
        run_sweep()
    else:
        test_performance(args.invalid_percent)