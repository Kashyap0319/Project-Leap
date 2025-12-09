import requests

base_url = "http://localhost:8080/auth"

# Signup
signup_data = {
    "username": "testuser",
    "email": "testuser@example.com",
    "password": "secure123"
}
signup_resp = requests.post(f"{base_url}/signup", json=signup_data)
print("Signup status:", signup_resp.status_code)
print("Signup response:", signup_resp.text)

# Login
login_data = {
    "username": "testuser",
    "password": "secure123"
}
login_resp = requests.post(f"{base_url}/login", json=login_data)
print("Login status:", login_resp.status_code)
print("Login response:", login_resp.text)
