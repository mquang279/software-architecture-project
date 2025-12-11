import http from "k6/http";
import { check, sleep } from "k6";
import { Rate } from "k6/metrics";

const errorRate = new Rate("errors");
const BASE_URL = "http://localhost:8079/api/v1";

// Test scenarios - realistic progressive load testing
export const options = {
    scenarios: {
        // Warm-up phase
        warmup: {
            executor: "ramping-vus",
            startVUs: 0,
            stages: [
                { duration: "30s", target: 10 },
                { duration: "30s", target: 10 },
            ],
            exec: "writeWorkload",
            startTime: "0s",
            tags: { test_type: "warmup" },
        },
        // Write tests - gradual increase
        write_low: {
            executor: "ramping-arrival-rate",
            startRate: 10,
            timeUnit: "1s",
            preAllocatedVUs: 5,
            maxVUs: 50,
            stages: [
                { duration: "30s", target: 50 },
                { duration: "30s", target: 50 },
                { duration: "30s", target: 100 },
            ],
            exec: "writeWorkload",
            startTime: "1m",
            tags: { test_type: "write", phase: "low" },
        },
        write_medium: {
            executor: "ramping-arrival-rate",
            startRate: 100,
            timeUnit: "1s",
            preAllocatedVUs: 20,
            maxVUs: 100,
            stages: [
                { duration: "30s", target: 200 },
                { duration: "30s", target: 200 },
                { duration: "30s", target: 300 },
            ],
            exec: "writeWorkload",
            startTime: "2m30s",
            tags: { test_type: "write", phase: "medium" },
        },
        write_high: {
            executor: "ramping-arrival-rate",
            startRate: 300,
            timeUnit: "1s",
            preAllocatedVUs: 50,
            maxVUs: 200,
            stages: [
                { duration: "30s", target: 500 },
                { duration: "30s", target: 500 },
            ],
            exec: "writeWorkload",
            startTime: "4m",
            tags: { test_type: "write", phase: "high" },
        },
        // Cool down before reads
        cooldown: {
            executor: "ramping-vus",
            startVUs: 1,
            stages: [{ duration: "30s", target: 1 }],
            exec: "readWorkload",
            startTime: "5m",
            tags: { test_type: "cooldown" },
        },
        // Read tests
        read_low: {
            executor: "ramping-arrival-rate",
            startRate: 10,
            timeUnit: "1s",
            preAllocatedVUs: 5,
            maxVUs: 50,
            stages: [
                { duration: "30s", target: 100 },
                { duration: "30s", target: 100 },
                { duration: "30s", target: 200 },
            ],
            exec: "readWorkload",
            startTime: "5m30s",
            tags: { test_type: "read", phase: "low" },
        },
        read_medium: {
            executor: "ramping-arrival-rate",
            startRate: 200,
            timeUnit: "1s",
            preAllocatedVUs: 20,
            maxVUs: 100,
            stages: [
                { duration: "30s", target: 400 },
                { duration: "30s", target: 400 },
                { duration: "30s", target: 600 },
            ],
            exec: "readWorkload",
            startTime: "7m",
            tags: { test_type: "read", phase: "medium" },
        },
        read_high: {
            executor: "ramping-arrival-rate",
            startRate: 600,
            timeUnit: "1s",
            preAllocatedVUs: 50,
            maxVUs: 200,
            stages: [
                { duration: "30s", target: 1000 },
                { duration: "30s", target: 1000 },
            ],
            exec: "readWorkload",
            startTime: "8m30s",
            tags: { test_type: "read", phase: "high" },
        },
        // Mixed workload test
        mixed_workload: {
            executor: "ramping-arrival-rate",
            startRate: 100,
            timeUnit: "1s",
            preAllocatedVUs: 30,
            maxVUs: 150,
            stages: [
                { duration: "30s", target: 300 },
                { duration: "1m", target: 300 },
                { duration: "30s", target: 100 },
            ],
            exec: "mixedWorkload",
            startTime: "9m30s",
            tags: { test_type: "mixed" },
        },
    },
    thresholds: {
        http_req_duration: ["p(95)<5000", "p(99)<10000"],
        http_req_failed: ["rate<0.8"],
        errors: ["rate<0.8"],
    },
    discardResponseBodies: true,
    // Add timeout settings
    httpDebug: "full",
    noConnectionReuse: false,
    userAgent: "k6-load-test",
};

// Store created user IDs for read operations
let createdUserIds = [];

// Read workload - GET user operations
export function readWorkload() {
    const params = {
        headers: {
            "Content-Type": "application/json",
        },
        timeout: "10s",
    };

    // Randomly choose between different read operations
    const operation = Math.floor(Math.random() * 4);

    let res;
    switch (operation) {
        case 0:
            // Get all users with pagination
            const page = Math.floor(Math.random() * 10);
            const pageSize = 15;
            res = http.get(
                `${BASE_URL}/users?page=${page}&pageSize=${pageSize}`,
                params
            );

            check(res, {
                "get all users - status is 200": (r) => r.status === 200,
                "get all users - response time < 2000ms": (r) =>
                    r.timings.duration < 2000,
            });
            break;

        case 1:
            // Get user by ID
            const userId = createdUserIds.length > 0
                ? createdUserIds[Math.floor(Math.random() * createdUserIds.length)]
                : Math.floor(Math.random() * 100) + 1;

            res = http.get(`${BASE_URL}/users/${userId}`, params);

            check(res, {
                "get user by id - status is 200 or 404": (r) =>
                    r.status === 200 || r.status === 404,
                "get user by id - response time < 2000ms": (r) =>
                    r.timings.duration < 2000,
            });
            break;

        case 2:
            // Get user by email
            const randomNum = Math.floor(Math.random() * 1000000);
            res = http.get(
                `${BASE_URL}/users/email?email=user${randomNum}@test.com`,
                params
            );

            check(res, {
                "get user by email - status is 200 or 404": (r) =>
                    r.status === 200 || r.status === 404,
                "get user by email - response time < 2000ms": (r) =>
                    r.timings.duration < 2000,
            });
            break;

        default:
            // Get all users (default operation)
            res = http.get(`${BASE_URL}/users?page=0&pageSize=15`, params);

            check(res, {
                "get users default - status is 200": (r) => r.status === 200,
                "get users default - response time < 2000ms": (r) =>
                    r.timings.duration < 2000,
            });
    }

    const result = res && res.status >= 200 && res.status < 500;
    errorRate.add(!result);
    sleep(0.5);
}

// Write workload - POST user registration
export function writeWorkload() {
    const randomNum = Math.floor(Math.random() * 1000000);
    const timestamp = Date.now();

    const payload = JSON.stringify({
        name: `User ${randomNum}`,
        email: `user${randomNum}_${timestamp}@test.com`,
        password: "Test123!@#",
        role: Math.random() > 0.5 ? "USER" : "ADMIN",
    });

    const params = {
        headers: {
            "Content-Type": "application/json",
        },
        timeout: "10s",
    };

    const res = http.post(`${BASE_URL}/users/register`, payload, params);

    const result = check(res, {
        "register user - status is 201 or 200 or 409": (r) =>
            r.status === 201 || r.status === 200 || r.status === 409,
        "register user - response time < 2000ms": (r) =>
            r.timings.duration < 2000,
    });

    // Store created user ID if successful
    if (res.status === 201 || res.status === 200) {
        try {
            const responseBody = JSON.parse(res.body);
            if (responseBody.id) {
                createdUserIds.push(responseBody.id);
                // Limit array size to prevent memory issues
                if (createdUserIds.length > 1000) {
                    createdUserIds.shift();
                }
            }
        } catch (e) {
            // Ignore parse errors
        }
    }

    errorRate.add(!result);
    sleep(0.5);
}

// Update workload - PUT user update
export function updateWorkload() {
    if (createdUserIds.length === 0) {
        // If no users created yet, skip
        sleep(0.5);
        return;
    }

    const userId = createdUserIds[Math.floor(Math.random() * createdUserIds.length)];
    const randomNum = Math.floor(Math.random() * 1000000);

    const payload = JSON.stringify({
        name: `Updated User ${randomNum}`,
        email: `updated${randomNum}@test.com`,
    });

    const params = {
        headers: {
            "Content-Type": "application/json",
        },
        timeout: "10s",
    };

    const res = http.put(`${BASE_URL}/users/${userId}`, payload, params);

    const result = check(res, {
        "update user - status is 200 or 404": (r) =>
            r.status === 200 || r.status === 404,
        "update user - response time < 2000ms": (r) =>
            r.timings.duration < 2000,
    });

    errorRate.add(!result);
    sleep(0.5);
}

// Mixed workload - combination of read, write, and update operations
export function mixedWorkload() {
    const operation = Math.random();

    if (operation < 0.6) {
        // 60% read operations
        readWorkload();
    } else if (operation < 0.85) {
        // 25% write operations
        writeWorkload();
    } else {
        // 15% update operations
        updateWorkload();
    }
}

// Setup function - runs once at the beginning
export function setup() {
    console.log("Starting user-service load test...");
    console.log(`Target URL: ${BASE_URL}`);
    return { startTime: Date.now() };
}

// Teardown function - runs once at the end
export function teardown(data) {
    const duration = (Date.now() - data.startTime) / 1000;
    console.log(`Test completed in ${duration} seconds`);
    console.log(`Total users created: ${createdUserIds.length}`);
}
