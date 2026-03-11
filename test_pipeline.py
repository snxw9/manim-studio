import requests
import json
import time

BASE_URL = "http://localhost:8000"

def test_pipeline():
    # 1. Generate code
    print("Generating Manim code...")
    prompt = "Create a simple animation showing a circle transforming into a square"
    response = requests.post(f"{BASE_URL}/generate", json={"prompt": prompt})
    
    if response.status_code != 200:
        print(f"Error generating code: {response.text}")
        return
    
    code = response.json().get("code")
    print("Generated Code:")
    print("-" * 20)
    print(code)
    print("-" * 20)
    
    # 2. Render code
    print("Rendering animation...")
    response = requests.post(f"{BASE_URL}/render", json={"code": code})
    
    if response.status_code != 200:
        print(f"Error rendering code: {response.text}")
        return
    
    output_path = response.json().get("output_path")
    print(f"Render Success! Output: {output_path}")

if __name__ == "__main__":
    # Wait for server to start
    time.sleep(5)
    test_pipeline()
