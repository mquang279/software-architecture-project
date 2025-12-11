import http from "k6/http";
import { check, sleep } from "k6";
import { Rate } from "k6/metrics";

const errorRate = new Rate("errors");
const BASE_URL = __ENV.BASE_URL || "http://localhost:8079";

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

// Read workload - GET payment operations
export function readWorkload() {
    const operation = Math.floor(Math.random() * 3);
    let res;

    if (operation === 0) {
        // Get payment by ID
        const paymentId = Math.floor(Math.random() * 10000) + 1;
        res = http.get(`${BASE_URL}/api/v1/payments/${paymentId}`);

        check(res, {
            "get payment by id - status is 200 or 404": (r) => r.status === 200 || r.status === 404,
            "get payment by id - response time < 500ms": (r) => r.timings.duration < 500,
        });
    } else if (operation === 1) {
        // Get payment by reservation ID
        const reservationId = Math.floor(Math.random() * 10000) + 1;
        res = http.get(`${BASE_URL}/api/v1/payments/reservation/${reservationId}`);

        check(res, {
            "get payment by reservation - status is 200 or 404": (r) => r.status === 200 || r.status === 404,
            "get payment by reservation - response time < 500ms": (r) => r.timings.duration < 500,
        });
    } else {
        // Get payments by user with pagination
        const userId = Math.floor(Math.random() * 5000) + 1;
        const page = Math.floor(Math.random() * 10);
        const size = 10;
        res = http.get(`${BASE_URL}/api/v1/payments?userId=${userId}&page=${page}&size=${size}`);

        check(res, {
            "get payments by user - status is 200": (r) => r.status === 200,
            "get payments by user - response time < 500ms": (r) => r.timings.duration < 500,
        });
    }

    const result = check(res, {
        "status is success": (r) => r.status === 200 || r.status === 404,
        "response time < 500ms": (r) => r.timings.duration < 500,
    });

    errorRate.add(!result);
    sleep(0.1);
}

// Write workload - POST process payment
export function writeWorkload() {
    const paymentMethods = ["CREDIT_CARD", "DEBIT_CARD", "E_WALLET", "CASH"];
    const userId = Math.floor(Math.random() * 5000) + 1;
    const reservationId = Math.floor(Math.random() * 10000) + 1;
    const amount = Number((Math.random() * 90 + 10).toFixed(2));

    const payload = JSON.stringify({
        reservationId: reservationId,
        amount: amount,
        paymentMethod: paymentMethods[Math.floor(Math.random() * paymentMethods.length)],
    });

    const params = {
        headers: {
            "Content-Type": "application/json",
        },
    };

    const res = http.post(`${BASE_URL}/api/v1/payments?userId=${userId}`, payload, params);

    const result = check(res, {
        "process payment - status is 200 or 400": (r) => r.status === 200 || r.status === 400,
        "process payment - response time < 1000ms": (r) => r.timings.duration < 1000,
    });

    errorRate.add(!result);
    sleep(0.1);
}
