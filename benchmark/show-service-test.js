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
    const genres = ["ACTION", "COMEDY", "DRAMA", "HORROR", "ROMANCE", "THRILLER", "SCI_FI"];
    const languages = ["English", "Spanish", "French", "German", "Japanese", "Korean", "Chinese"];
    const randomNum = Math.floor(Math.random() * 1000000);

    const payload = JSON.stringify({
        movieName: `Movie ${randomNum}`,
        genre: [genres[Math.floor(Math.random() * genres.length)]],
        movieLength: Math.floor(Math.random() * 120) + 60,
        movieLanguage: languages[Math.floor(Math.random() * languages.length)],
        releaseDate: new Date(Date.now() + Math.random() * 365 * 24 * 60 * 60 * 1000).toISOString(),
    });

    // Add name tag to group all movie creation requests together
    const res = http.post(`${BASE_URL}/api/v1/movies`, payload, {
        ...params,
        tags: { name: 'CreateMovie' }
    });

    const result = check(res, {
        "create movie - status is 201 or 200": (r) => r.status === 201 || r.status === 200,
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

    // Add name tag to group all theater creation requests together
    const res = http.post(`${BASE_URL}/api/v1/theaters`, payload, {
        ...params,
        tags: { name: 'CreateTheater' }
    });

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
        // write_low_load: {
        //     executor: "constant-arrival-rate",
        //     rate: 50,  // Realistic baseline (1% of 5k reads)
        //     timeUnit: "1s",
        //     duration: "1m",
        //     preAllocatedVUs: 50,
        //     maxVUs: 200, // Writes block easily, allow extra VUs
        //     exec: "writeWorkload",
        //     startTime: "0s",
        //     tags: { test_type: "write", rps: "50", users_estimated: "50" },
        // },
        // write_medium_load: {
        //     executor: "constant-arrival-rate",
        //     rate: 250, // 5% of reads
        //     timeUnit: "1s",
        //     duration: "1m",
        //     preAllocatedVUs: 100,
        //     maxVUs: 1000,
        //     exec: "writeWorkload",
        //     startTime: "1m10s",
        //     tags: { test_type: "write", rps: "250", users_estimated: "250" },
        // },
        // write_peak_load: {
        //     executor: "constant-arrival-rate",
        //     rate: 500, // 10% of reads (This is your realistic peak)
        //     timeUnit: "1s",
        //     duration: "1m",
        //     preAllocatedVUs: 200,
        //     maxVUs: 2000,
        //     exec: "writeWorkload",
        //     startTime: "2m20s",
        //     tags: { test_type: "write", rps: "500", users_estimated: "500" },
        // },
        // write_stress_test: {
        //     executor: "constant-arrival-rate",
        //     rate: 1000,
        //     timeUnit: "1s",
        //     duration: "1m",
        //     preAllocatedVUs: 500,
        //     maxVUs: 5000,
        //     exec: "writeWorkload",
        //     startTime: "3m30s",
        //     tags: { test_type: "write", rps: "1000", users_estimated: "1000" },
        // },
        read_1k_rps: {
            executor: "constant-arrival-rate",
            rate: 1000,
            timeUnit: "1s",
            duration: "1m",
            preAllocatedVUs: 100,
            maxVUs: 1000,
            exec: "readWorkload",
            startTime: "0s",
            tags: { test_type: "read", rps: "1000" },
        },
        read_2k_rps: {
            executor: "constant-arrival-rate",
            rate: 2000,
            timeUnit: "1s",
            duration: "1m",
            preAllocatedVUs: 200,
            maxVUs: 2000,
            exec: "readWorkload",
            startTime: "1m10s",
            tags: { test_type: "read", rps: "2000" },
        },
        read_4k_rps: {
            executor: "constant-arrival-rate",
            rate: 4000,
            timeUnit: "1s",
            duration: "1m",
            preAllocatedVUs: 400,
            maxVUs: 4000,
            exec: "readWorkload",
            startTime: "2m20s",
            tags: { test_type: "read", rps: "4000" },
        },
        read_6k_rps: {
            executor: "constant-arrival-rate",
            rate: 6000,
            timeUnit: "1s",
            duration: "1m",
            preAllocatedVUs: 600,
            maxVUs: 6000,
            exec: "readWorkload",
            startTime: "3m30s",
            tags: { test_type: "read", rps: "6000" },
        },
        read_8k_rps: {
            executor: "constant-arrival-rate",
            rate: 8000,
            timeUnit: "1s",
            duration: "1m",
            preAllocatedVUs: 800,
            maxVUs: 8000,
            exec: "readWorkload",
            startTime: "4m40s",
            tags: { test_type: "read", rps: "8000" },
        },
        cleanup_delay: {
            executor: "constant-vus",
            vus: 1,
            duration: "10s",
            startTime: "5m40s",
            exec: "doNothing",
        }
    },
    thresholds: {
        http_req_duration: ["p(95)<50", "p(99)<200"],
        http_req_failed: ["rate<0.1"],
        errors: ["rate<0.1"],
    },
};

// Read workload - Test filterShows API with various filter combinations
export function readWorkload() {
    const operation = Math.floor(Math.random() * 4);
    let res;
    const page = Math.floor(Math.random() * 10);
    const size = 10 + Math.floor(Math.random() * 11); // size between 10 and 20

    if (operation === 0) {
        // Filter by movieId only - use a realistic range that might exist
        // Since shows are created with random movieIds, we just test the filtering capability
        const movieId = 87000 + Math.floor(Math.random() * 1000);
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
        // Filter by time range using very wide range to capture created shows
        // Shows are created with future dates, so use a very wide range
        const from = new Date(Date.now()).toISOString();
        const to = new Date(Date.now() + 100 * 365 * 24 * 60 * 60 * 1000).toISOString(); // 100 years ahead
        res = http.get(`${BASE_URL}/api/v1/shows/filter?from=${from}&to=${to}&page=${page}&size=${size}`, {
            tags: { name: 'FilterShowsByTimeRange' }
        });

        check(res, {
            "filter by time range - status is 200": (r) => r.status === 200,
            "filter by time range - response time < 200ms": (r) => r.timings.duration < 200,
            "filter by time range - has data": (r) => {
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

    const res = http.post(`${BASE_URL}/api/v1/shows`, payload, {
        ...params,
        tags: { name: 'CreateShow' }
    });

    const result = check(res, {
        "create show - status is 201 or 200 or 400": (r) =>
            r.status === 201 || r.status === 200 || r.status === 400,
        "create show - response time < 1000ms": (r) => r.timings.duration < 1000,
    });

    errorRate.add(!result);
    sleep(0.1);
}

export function doNothing() {
    sleep(1);
}
