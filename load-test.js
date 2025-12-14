import http from 'k6/http';
import { check } from 'k6';

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

export const options = {
  vus: 50,
  iterations: 100000,
  thresholds: {
    http_req_duration: ['p(95)<500'],
    http_req_failed: ['rate<0.01'],
  },
};

function generateUniqueAlias(index) {
  const timestamp = Date.now();
  const random = Math.random().toString(36).substring(2, 9);
  return `trithai-${index}-${timestamp}-${random}`;
}

export function setup() {
  console.log('Creating 1000 aliases...');
  const createdAliases = [];

  for (let i = 0; i < 1000; i++) {
    const urlIndex = i % urls.length;
    const alias = generateUniqueAlias(i);
    
    const payload = JSON.stringify({
      url: urls[urlIndex],
      alias: alias,
      expire: null,
    });

    const response = http.post(
      `${BASE_URL}/app/api/create`,
      payload,
      { headers: commonHeaders }
    );

    if (response.status === 200) {
      const body = JSON.parse(response.body);
      createdAliases.push(body.alias);
    } else {
      console.log(`Failed to create alias ${i}: ${response.status} - ${response.body}`);
    }

    if ((i + 1) % 100 === 0) {
      console.log(`Created ${i + 1} aliases...`);
    }
  }

  console.log(`Successfully created ${createdAliases.length} aliases`);
  return { createdAliases };
}

function generateRandomAlias() {
  const chars = 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789';
  let result = '';
  for (let i = 0; i < 10; i++) {
    result += chars.charAt(Math.floor(Math.random() * chars.length));
  }
  return result;
}

export default function (data) {
  const { createdAliases } = data;
  const existingRatio = 0.5;

  let alias;
  const random = Math.random();

  if (random < existingRatio && createdAliases.length > 0) {
    alias = createdAliases[Math.floor(Math.random() * createdAliases.length)];
  } else {
    alias = generateRandomAlias();
  }

  const response = http.get(`${BASE_URL}/${alias}`, {
    headers: commonHeaders,
    redirects: 0,
  });

  check(response, {
    'status is 302 or 404': (r) => r.status === 302 || r.status === 404,
  });
}

