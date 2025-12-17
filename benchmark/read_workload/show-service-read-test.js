import http from "k6/http";
import { check, sleep } from "k6";
import { Rate } from "k6/metrics";

const errorRate = new Rate("errors");
const BASE_URL = __ENV.BASE_URL || "http://localhost:8080";

export function setup() {
    const resMovies = http.get(`${BASE_URL}/api/v1/movies?page=0&pageSize=1000`);
    const resTheaters = http.get(`${BASE_URL}/api/v1/theaters?page=0&pageSize=1000`);

    let movieIds = [];
    let theaterIds = [];

    if (resMovies.status === 200) {
        try {
            const body = resMovies.json();
            if (body.data && Array.isArray(body.data)) {
                movieIds = body.data.map((m) => m.id);
            }
        } catch (e) {
            console.error("Failed to parse movies", e);
        }
    }

    if (resTheaters.status === 200) {
        try {
            const body = resTheaters.json();
            if (body.data && Array.isArray(body.data)) {
                theaterIds = body.data.map((t) => t.id);
            }
        } catch (e) {
            console.error("Failed to parse theaters", e);
        }
    }

    // Fallback to avoid crashes if no data exists
    if (movieIds.length === 0) movieIds = [1];
    if (theaterIds.length === 0) theaterIds = [1];

    console.log(`Setup complete. Found ${movieIds.length} movies and ${theaterIds.length} theaters.`);
    return { movieIds, theaterIds };
}

export const options = {
    scenarios: {
        // read_4k_rps: {
        //     executor: "constant-arrival-rate",
        //     rate: 4000,
        //     timeUnit: "1s",
        //     duration: "1m",
        //     preAllocatedVUs: 1000,
        //     maxVUs: 2000,
        //     exec: "readWorkload",
        //     startTime: "0s",
        //     tags: { test_type: "read", rps: "1000" },
        // },
        // read_6k_rps: {
        //     executor: "constant-arrival-rate",
        //     rate: 6000,
        //     timeUnit: "1s",
        //     duration: "1m",
        //     preAllocatedVUs: 1000,
        //     maxVUs: 3000,
        //     exec: "readWorkload",
        //     startTime: "1m10s",
        //     tags: { test_type: "read", rps: "2000" },
        // },
        read_7k_rps: {
            executor: "constant-arrival-rate",
            rate: 7000,
            timeUnit: "1s",
            duration: "1m",
            preAllocatedVUs: 2000,
            maxVUs: 8000,
            exec: "readWorkload",
            startTime: "0s",
            tags: { test_type: "read", rps: "4000" },
        },
        read_8k_rps: {
            executor: "constant-arrival-rate",
            rate: 8000,
            timeUnit: "1s",
            duration: "1m",
            preAllocatedVUs: 2000,
            maxVUs: 10000,
            exec: "readWorkload",
            startTime: "1m10s",
            tags: { test_type: "read", rps: "6000" },
        },
        read_9k_rps: {
            executor: "constant-arrival-rate",
            rate: 9000,
            timeUnit: "1s",
            duration: "1m",
            preAllocatedVUs: 2000,
            maxVUs: 10000,
            exec: "readWorkload",
            startTime: "2m20s",
            tags: { test_type: "read", rps: "6000" },
        },
        read_10k_rps: {
            executor: "constant-arrival-rate",
            rate: 10000,
            timeUnit: "1s",
            duration: "1m",
            preAllocatedVUs: 2000,
            maxVUs: 10000,
            exec: "readWorkload",
            startTime: "3m30s",
            tags: { test_type: "read", rps: "6000" },
        },
        read_12k_rps: {
            executor: "constant-arrival-rate",
            rate: 12000,
            timeUnit: "1s",
            duration: "1m",
            preAllocatedVUs: 2000,
            maxVUs: 10000,
            exec: "readWorkload",
            startTime: "4m40s",
            tags: { test_type: "read", rps: "6000" },
        },
        cleanup_delay: {
            executor: "constant-vus",
            vus: 1,
            duration: "10s",
            startTime: "6m",
            exec: "doNothing",
        }
    },
    thresholds: {
        http_req_duration: ["p(95)<50", "p(99)<200"],
        http_req_failed: ["rate<0.1"],
        errors: ["rate<0.1"],
    },
};

export function readWorkload(data) {
    const operation = Math.floor(Math.random() * 4);
    let res;
    const page = Math.floor(Math.random() * 2);
    const size = 10 + Math.floor(Math.random() * 11); // size between 10 and 20

    // Get random IDs from setup data
    const movieIds = data && data.movieIds ? data.movieIds : [1];
    const theaterIds = data && data.theaterIds ? data.theaterIds : [1];

    if (operation === 0) {
        // Filter by movieId only - use a real ID from setup
        const movieId = movieIds[Math.floor(Math.random() * movieIds.length)];
        res = http.get(`${BASE_URL}/api/v1/shows/filter?movieId=${movieId}&page=${page}&size=${size}`, {
            tags: { name: 'FilterShowsByMovie' }
        });

        check(res, {
            "filter by movie - status is 200": (r) => r.status === 200,
            "filter by movie - response time < 200ms": (r) => r.timings.duration < 200,
            "filter by movie - has data": (r) => {
                try {
                    const body = r.json();
                    return body && typeof body.data !== 'undefined';
                } catch (e) {
                    return false;
                }
            },
        });
    } else if (operation === 1) {
        // Filter by theaterId only
        const theaterId = theaterIds[Math.floor(Math.random() * theaterIds.length)];
        res = http.get(`${BASE_URL}/api/v1/shows/filter?theaterId=${theaterId}&page=${page}&size=${size}`, {
            tags: { name: 'FilterShowsByTheater' }
        });

        check(res, {
            "filter by theater - status is 200": (r) => r.status === 200,
            "filter by theater - response time < 200ms": (r) => r.timings.duration < 200,
            "filter by theater - has data": (r) => {
                try {
                    const body = r.json();
                    return body && typeof body.data !== 'undefined';
                } catch (e) {
                    return false;
                }
            },
        });
    } else if (operation === 2) {
        // Filter by theaterId and movieId
        const theaterId = theaterIds[Math.floor(Math.random() * theaterIds.length)];
        const movieId = movieIds[Math.floor(Math.random() * movieIds.length)];
        res = http.get(`${BASE_URL}/api/v1/shows/filter?theaterId=${theaterId}&movieId=${movieId}&page=${page}&size=${size}`, {
            tags: { name: 'FilterShowsByTheaterAndMovie' }
        });

        check(res, {
            "filter by theater and movie - status is 200": (r) => r.status === 200,
            "filter by theater and movie - response time < 200ms": (r) => r.timings.duration < 200,
            "filter by theater and movie - has data": (r) => {
                try {
                    const body = r.json();
                    return body && typeof body.data !== 'undefined';
                } catch (e) {
                    return false;
                }
            },
        });
    } else {
        // Filter with no parameters (should return all shows with pagination)
        res = http.get(`${BASE_URL}/api/v1/shows/filter?page=${page}&size=${size}`, {
            tags: { name: 'FilterShowsAll' }
        });

        check(res, {
            "filter no params - status is 200": (r) => r.status === 200,
            "filter no params - response time < 200ms": (r) => r.timings.duration < 200,
            "filter no params - has data": (r) => {
                try {
                    const body = r.json();
                    return body && typeof body.data !== 'undefined';
                } catch (e) {
                    return false;
                }
            },
        });
    }

    const result = check(res, {
        "status is success": (r) => r.status === 200,
        "response time < 200ms": (r) => r.timings.duration < 200,
    });

    if (res.status !== 200) {
        console.error(`Request failed. Status: ${res.status}. URL: ${res.url}. Body: ${res.body}`);
    }

    errorRate.add(!result);
    sleep(0.1);
}

export function doNothing() {
    sleep(1);
}
