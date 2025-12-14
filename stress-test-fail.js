import http from 'k6/http';
import { check, sleep } from 'k6';
import { Rate, Trend } from 'k6/metrics';

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';

const commonHeaders = {
  'accept': '*/*',
  'accept-language': 'en,vi;q=0.9,en-US;q=0.8',
  'content-type': 'application/json',
  'dnt': '1',
  'origin': 'https://tinyurl.com',
  'priority': 'u=1, i',
  'referer': 'https://tinyurl.com/',
  'sec-ch-ua': '"Google Chrome";v="143", "Chromium";v="143", "Not A(Brand";v="24"',
  'sec-ch-ua-mobile': '?0',
  'sec-ch-ua-platform': '"macOS"',
  'sec-fetch-dest': 'empty',
  'sec-fetch-mode': 'cors',
  'sec-fetch-site': 'same-origin',
  'user-agent': 'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36',
  'x-requested-with': 'XMLHttpRequest',
};

const errorRate = new Rate('errors');
const responseTime = new Trend('response_time');

export const options = {
  stages: [
    { duration: '30s', target: 50 },
    { duration: '1m', target: 100 },
    { duration: '1m', target: 200 },
    { duration: '1m', target: 500 },
    { duration: '1m', target: 1000 },
    { duration: '2m', target: 1000 },
    { duration: '30s', target: 0 },
  ],
  thresholds: {
    http_req_duration: ['p(95)<2000', 'p(99)<5000'],
    http_req_failed: ['rate<0.5'],
    errors: ['rate<0.5'],
  },
};

function generateRandomAlias() {
  const chars = 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789';
  let result = '';
  for (let i = 0; i < 10; i++) {
    result += chars.charAt(Math.floor(Math.random() * chars.length));
  }
  return result;
}

export default function () {
  const alias = generateRandomAlias();
  const startTime = Date.now();

  const response = http.get(`${BASE_URL}/${alias}`, {
    headers: commonHeaders,
    redirects: 0,
    timeout: '10s',
  });

  const duration = Date.now() - startTime;
  responseTime.add(duration);

  const isError = response.status !== 404 && response.status !== 302;
  errorRate.add(isError);

  check(response, {
    'status is 404 (not found) or 302 (redirect)': (r) => r.status === 404 || r.status === 302,
    'response time < 5s': (r) => r.timings.duration < 5000,
  });

  sleep(0.1);
}

