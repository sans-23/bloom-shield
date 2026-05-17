""" 
    here performance test code will be added for different scenarios with different filters. 
    This will help us identify bottlenecks and optimize the code for better performance. 
    Additionally, we can also consider using multiprocessing or multithreading to improve performance in certain scenarios.
"""

import requests

def test_performance():
    url = "http://localhost:8080/api/register-user"
    payload = {
        "filterType": "ip",
        "filterValue": ""
    }
    response = requests.get(url, json=payload)
    print(f"Status Code: {response.status_code}")
    print(f"Response Time: {response.elapsed.total_seconds()} seconds")

if __name__ == "__main__":
    test_performance()