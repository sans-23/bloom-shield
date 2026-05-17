""" 
    here performance test code will be added for different scenarios with different filters. 
    This will help us identify bottlenecks and optimize the code for better performance. 
    Additionally, we can also consider using multiprocessing or multithreading to improve performance in certain scenarios.
"""

import requests
import time

def test_performance():
    url = "http://localhost:8080/api/register-user"
    
    start_time = time.time()
    for i in range(400000, 1000000):
        params = {
            "userName": f"user_{i}"
        }
        response = requests.get(url, params=params)
        if response.status_code != 200 and response.status_code != 409:  # Assuming 409 is returned for duplicate users
            print(f"Failed at {i} with status: {response.status_code}")
            break

        if response.status_code == 409:
            print(f"User {i} is a duplicate.")
            
    end_time = time.time()
    total_time = end_time - start_time
    print(f"Time taken to add 100000 users: {total_time:.2f} seconds")

if __name__ == "__main__":
    test_performance()