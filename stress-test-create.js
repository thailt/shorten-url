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

const urls = [
  'https://www.educative.io/courses/grokking-the-system-design-interview/system-design-tinyurl',
  'https://github.com/grafana/k6',
  'https://www.docker.com/',
  'https://spring.io/projects/spring-boot',
  'https://redis.io/',
  'https://www.mysql.com/',
  'https://gradle.org/',
  'https://www.java.com/',
  'https://kubernetes.io/',
  'https://www.postgresql.org/',
];

const errorRate = new Rate('errors');
const responseTime = new Trend('response_time');
const successRate = new Rate('success');

export const options = {
  stages: [
    { duration: '30s', target: 10 },
    { duration: '1m', target: 25 },
    { duration: '1m', target: 50 },
    { duration: '1m', target: 100 },
    { duration: '1m', target: 200 },
    { duration: '2m', target: 200 },
    { duration: '30s', target: 0 },
  ],
  thresholds: {
    http_req_duration: ['p(95)<200', 'p(99)<500'],
    http_req_failed: ['rate<0.1'],
    errors: ['rate<0.1'],
    success: ['rate>0.9'],
  },
};

function generateUniqueAlias() {
  const timestamp = Date.now();
  const random = Math.random().toString(36).substring(2, 15);
  const vuId = __VU || 0;
  const iterId = __ITER || 0;
  return `stress-${vuId}-${iterId}-${timestamp}-${random}`;
}

export default function () {
  const urlIndex = Math.floor(Math.random() * urls.length);
  const alias = generateUniqueAlias();
  const startTime = Date.now();

  const payload = JSON.stringify({
    url: urls[urlIndex],
    alias: alias,
    expire: null,
  });

  const response = http.post(
    `${BASE_URL}/app/api/create`,
    payload,
    {
      headers: commonHeaders,
      timeout: '2s',
    }
  );

  const duration = Date.now() - startTime;
  responseTime.add(duration);

  const isSuccess = response.status === 200;
  const isError = response.status >= 400 && response.status < 500;

  successRate.add(isSuccess);
  errorRate.add(isError);

  check(response, {
    'status is 200': (r) => r.status === 200,
    'response has alias': (r) => {
      if (r.status === 200) {
        try {
          const body = JSON.parse(r.body);
          return body.alias !== undefined;
        } catch (e) {
          return false;
        }
      }
      return false;
    },
    'response time < 5s': (r) => r.timings.duration < 5000,
  });

  if (response.status !== 200) {
    console.log(`Failed create: ${response.status} - ${response.body.substring(0, 100)}`);
  }

  sleep(0.5);
}

