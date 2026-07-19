const BASE_URL = 'http://localhost:8081/api/auth';

export async function checkUser(username) {
  const res = await fetch(`${BASE_URL}/check-user`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ username })
  });
  return res.json();
}

export async function login(username, password) {
  const res = await fetch(`${BASE_URL}/login`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ username, password: password.trim() })
  });
  return res.json();
}