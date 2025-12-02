import http from "k6/http";
import { check, sleep } from "k6";
import { Rate } from "k6/metrics";

const errorRate = new Rate("errors");
const BASE_URL = __ENV.BASE_URL || "http://localhost:8080";

export const options = {
    scenarios: {
        // Write workload tests (run first)
        write_10k_rps_100_users: {
            executor: "constant-arrival-rate",
            rate: 10000,
            timeUnit: "1s",
            duration: "30s",
            preAllocatedVUs: 100,
            maxVUs: 200,
            exec: "writeWorkload",
            startTime: "0s",
            tags: { test_type: "write", rps: "10k", users: "100" },
        },
        write_25k_rps_500_users: {
            executor: "constant-arrival-rate",
            rate: 25000,
            timeUnit: "1s",
            duration: "30s",
            preAllocatedVUs: 500,
            maxVUs: 1000,
            exec: "writeWorkload",
            startTime: "40s",
            tags: { test_type: "write", rps: "25k", users: "500" },
        },
        write_50k_rps_1000_users: {
            executor: "constant-arrival-rate",
            rate: 50000,
            timeUnit: "1s",
            duration: "30s",
            preAllocatedVUs: 1000,
            maxVUs: 2000,
            exec: "writeWorkload",
            startTime: "80s",
            tags: { test_type: "write", rps: "50k", users: "1000" },
        },
        write_75k_rps_2500_users: {
            executor: "constant-arrival-rate",
            rate: 75000,
            timeUnit: "1s",
            duration: "30s",
            preAllocatedVUs: 2500,
            maxVUs: 3500,
            exec: "writeWorkload",
            startTime: "120s",
            tags: { test_type: "write", rps: "75k", users: "2500" },
        },
        write_100k_rps_5000_users: {
            executor: "constant-arrival-rate",
            rate: 100000,
            timeUnit: "1s",
            duration: "30s",
            preAllocatedVUs: 5000,
            maxVUs: 7000,
            exec: "writeWorkload",
            startTime: "160s",
            tags: { test_type: "write", rps: "100k", users: "5000" },
        },
        // Read workload tests (run after writes)
        read_10k_rps_100_users: {
            executor: "constant-arrival-rate",
            rate: 10000,
            timeUnit: "1s",
            duration: "30s",
            preAllocatedVUs: 100,
            maxVUs: 200,
            exec: "readWorkload",
            startTime: "200s",
            tags: { test_type: "read", rps: "10k", users: "100" },
        },
        read_25k_rps_500_users: {
            executor: "constant-arrival-rate",
            rate: 25000,
            timeUnit: "1s",
            duration: "30s",
            preAllocatedVUs: 500,
            maxVUs: 1000,
            exec: "readWorkload",
            startTime: "240s",
            tags: { test_type: "read", rps: "25k", users: "500" },
        },
        read_50k_rps_1000_users: {
            executor: "constant-arrival-rate",
            rate: 50000,
            timeUnit: "1s",
            duration: "30s",
            preAllocatedVUs: 1000,
            maxVUs: 2000,
            exec: "readWorkload",
            startTime: "280s",
            tags: { test_type: "read", rps: "50k", users: "1000" },
        },
        read_75k_rps_2500_users: {
            executor: "constant-arrival-rate",
            rate: 75000,
            timeUnit: "1s",
            duration: "30s",
            preAllocatedVUs: 2500,
            maxVUs: 3500,
            exec: "readWorkload",
            startTime: "320s",
            tags: { test_type: "read", rps: "75k", users: "2500" },
        },
        read_100k_rps_5000_users: {
            executor: "constant-arrival-rate",
            rate: 100000,
            timeUnit: "1s",
            duration: "30s",
            preAllocatedVUs: 5000,
            maxVUs: 7000,
            exec: "readWorkload",
            startTime: "360s",
            tags: { test_type: "read", rps: "100k", users: "5000" },
        },
    },
    thresholds: {
        http_req_duration: ["p(95)<500", "p(99)<1000"],
        http_req_failed: ["rate<0.1"],
        errors: ["rate<0.1"],
    },
};

// Read workload - Test get seats by showId with various pagination options
export function readWorkload() {
    // Generate a showId (lower range for higher chance of finding seats)
    const showId = Math.floor(Math.random() * 1000) + 1;
    const page = Math.floor(Math.random() * 10);
    const size = 10 + Math.floor(Math.random() * 11); // size between 10 and 20

    const res = http.get(`${BASE_URL}/api/v1/seats?showId=${showId}&page=${page}&size=${size}`);

    check(res, {
        "get seats by showId - status is 200": (r) => r.status === 200,
        "get seats by showId - response time < 500ms": (r) => r.timings.duration < 500,
        "get seats by showId - has pagination data": (r) => {
            const body = r.json();
            return body && typeof body.data !== 'undefined' && typeof body.pageNumber !== 'undefined';
        },
        "get seats by showId - valid page info": (r) => {
            const body = r.json();
            return body && body.pageNumber >= 0 && body.pageSize > 0;
        },
    });

    const result = check(res, {
        "status is success": (r) => r.status === 200,
        "response time < 500ms": (r) => r.timings.duration < 500,
    });

    errorRate.add(!result);
    sleep(0.1);
}

// Write workload - POST lock/unlock seats or update status
export function writeWorkload() {
    const operation = Math.floor(Math.random() * 3);
    const numSeats = Math.floor(Math.random() * 5) + 1;
    const seatIds = [];

    for (let i = 0; i < numSeats; i++) {
        seatIds.push(Math.floor(Math.random() * 10000) + 1);
    }

    const payload = JSON.stringify(seatIds);
    const params = {
        headers: {
            "Content-Type": "application/json",
        },
    };

    let res;

    if (operation === 0) {
        // Lock seats
        res = http.post(`${BASE_URL}/api/v1/seats/lock`, payload, params);

        check(res, {
            "lock seats - status is 200 or 400": (r) => r.status === 200 || r.status === 400,
            "lock seats - response time < 1000ms": (r) => r.timings.duration < 1000,
        });
    } else if (operation === 1) {
        // Unlock seats
        res = http.post(`${BASE_URL}/api/v1/seats/unlock`, payload, params);

        check(res, {
            "unlock seats - status is 200 or 400": (r) => r.status === 200 || r.status === 400,
            "unlock seats - response time < 1000ms": (r) => r.timings.duration < 1000,
        });
    } else {
        // Update seat status
        const statuses = ["AVAILABLE", "BOOKED", "LOCKED"];
        const status = statuses[Math.floor(Math.random() * statuses.length)];
        res = http.put(`${BASE_URL}/api/v1/seats/status?status=${status}`, payload, params);

        check(res, {
            "update status - status is 200 or 400": (r) => r.status === 200 || r.status === 400,
            "update status - response time < 1000ms": (r) => r.timings.duration < 1000,
        });
    }

    const result = res && (res.status === 200 || res.status === 400);
    errorRate.add(!result);
    sleep(0.1);
}
