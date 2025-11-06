import http from "k6/http";
import { check, sleep } from "k6";
import { Rate } from "k6/metrics";

const errorRate = new Rate("errors");
const BASE_URL = __ENV.BASE_URL || "http://localhost:8080";

function randomString(prefix) {
    return `${prefix}_${Math.random().toString(36).substring(2, 10)}`;
}

function randomReleaseDate() {
    const now = new Date();
    const pastOffset = Math.floor(Math.random() * 365 * 5); // up to five years back
    const releaseDate = new Date(now.getTime() - pastOffset * 24 * 60 * 60 * 1000);
    return releaseDate.toISOString().split("T")[0];
}

function createMovie(params) {
    const payload = JSON.stringify({
        movieName: randomString("LoadTestMovie"),
        genre: ["ACTION"],
        movieLength: 90 + Math.floor(Math.random() * 61),
        movieLanguage: "English",
        releaseDate: randomReleaseDate(),
    });

    const res = http.post(`${BASE_URL}/api/v1/movies`, payload, params);

    const result = check(res, {
        "create movie - status is 201": (r) => r.status === 201,
        "create movie - response time < 1000ms": (r) => r.timings.duration < 1000,
    });

    if (!result) {
        errorRate.add(1);
        return null;
    }

    const body = res.json();
    return body && body.id ? body.id : null;
}

function createTheater(params) {
    const payload = JSON.stringify({
        name: randomString("LoadTestTheater"),
        location: randomString("Location"),
    });

    const res = http.post(`${BASE_URL}/api/v1/theaters`, payload, params);

    const result = check(res, {
        "create theater - status is 201": (r) => r.status === 201,
        "create theater - response time < 1000ms": (r) => r.timings.duration < 1000,
    });

    if (!result) {
        errorRate.add(1);
        return null;
    }

    const body = res.json();
    return body && body.id ? body.id : null;
}

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

// Read workload - GET show operations
export function readWorkload() {
    const operation = Math.floor(Math.random() * 3);
    let res;

    if (operation === 0) {
        // Get all shows with pagination
        const page = Math.floor(Math.random() * 10);
        const size = 10;
        res = http.get(`${BASE_URL}/api/v1/shows?page=${page}&size=${size}`);

        check(res, {
            "get all shows - status is 200": (r) => r.status === 200,
            "get all shows - response time < 500ms": (r) => r.timings.duration < 500,
        });
    } else if (operation === 1) {
        // Get show by ID
        const showId = Math.floor(Math.random() * 1000) + 1;
        res = http.get(`${BASE_URL}/api/v1/shows/${showId}`);

        check(res, {
            "get show by id - status is 200 or 404": (r) => r.status === 200 || r.status === 404,
            "get show by id - response time < 500ms": (r) => r.timings.duration < 500,
        });
    } else {
        // Filter shows by theater and/or movie
        const theaterId = Math.floor(Math.random() * 100) + 1;
        const movieId = Math.floor(Math.random() * 1000) + 1;
        const page = Math.floor(Math.random() * 10);
        const size = 10;
        res = http.get(`${BASE_URL}/api/v1/shows/filter?theaterId=${theaterId}&movieId=${movieId}&page=${page}&size=${size}`);

        check(res, {
            "filter shows - status is 200": (r) => r.status === 200,
            "filter shows - response time < 500ms": (r) => r.timings.duration < 500,
        });
    }

    const result = check(res, {
        "status is success": (r) => r.status === 200 || r.status === 404,
        "response time < 500ms": (r) => r.timings.duration < 500,
    });

    errorRate.add(!result);
    sleep(0.1);
}

// Write workload - POST create show
export function writeWorkload() {
    const params = {
        headers: {
            "Content-Type": "application/json",
        },
    };

    const createdMovieId = createMovie(params);
    if (!createdMovieId) {
        sleep(0.1);
        return;
    }

    const createdTheaterId = createTheater(params);
    if (!createdTheaterId) {
        sleep(0.1);
        return;
    }

    const futureDate = new Date(Date.now() + Math.random() * 30 * 24 * 60 * 60 * 1000);
    const showLengthMinutes = 90 + Math.floor(Math.random() * 61); // between 90 and 150 minutes
    // Use epoch milliseconds format for Java Instant
    const startTime = futureDate.getTime();
    const endTime = futureDate.getTime() + showLengthMinutes * 60 * 1000;

    const seatAreas = ["VIP", "PREMIUM", "STANDARD"];
    const seats = seatAreas.map((area, index) => ({
        seatCount: 20 + Math.floor(Math.random() * 31) + index * 5,
        seatPrice: 10 + Math.floor(Math.random() * 11) + index * 5,
        area: area,
    }));

    const payload = JSON.stringify({
        movieId: createdMovieId,
        theaterId: createdTheaterId,
        startTime: startTime,
        endTime: endTime,
        seats: seats,
    });

    const res = http.post(`${BASE_URL}/api/v1/shows`, payload, params);

    const result = check(res, {
        "create show - status is 201 or 200 or 400": (r) =>
            r.status === 201 || r.status === 200 || r.status === 400,
        "create show - response time < 1000ms": (r) => r.timings.duration < 1000,
    });

    errorRate.add(!result);
    sleep(0.1);
}
