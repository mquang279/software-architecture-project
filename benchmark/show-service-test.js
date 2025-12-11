import http from "k6/http";
import { check, sleep } from "k6";
import { Rate } from "k6/metrics";

const errorRate = new Rate("errors");
const BASE_URL = __ENV.BASE_URL || "http://localhost:8079";
const ENABLE_WRITE = __ENV.ENABLE_WRITE === "true";

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
    const genres = ["ACTION", "COMEDY", "DRAMA", "ROMANCE", "THRILLER"];
    const languages = ["English", "Spanish", "French", "German", "Japanese", "Korean", "Chinese"];
    const randomNum = Math.floor(Math.random() * 1000000);

    const payload = JSON.stringify({
        movieName: `Movie ${randomNum}`,
        genre: [genres[Math.floor(Math.random() * genres.length)]],
        movieLength: Math.floor(Math.random() * 120) + 60,
        movieLanguage: languages[Math.floor(Math.random() * languages.length)],
        releaseDate: new Date(Date.now() + Math.random() * 365 * 24 * 60 * 60 * 1000).toISOString(),
    });

    const res = http.post(`${BASE_URL}/api/v1/movies`, payload, params);

    const result = check(res, {
        "create movie - status is 201 or 200": (r) => r.status === 201 || r.status === 200,
        "create movie - response time < 1000ms": (r) => r.timings.duration < 1000,
    });

    if (!result) {
        if (res.status !== 201 && res.status !== 200) {
            console.log(`Create Movie Failed. Status: ${res.status}, Body: ${res.body}`);
        }
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
        write_50_rps: {
            executor: "constant-arrival-rate",
            rate: 50,
            timeUnit: "1s",
            duration: "30s",
            preAllocatedVUs: 20,
            maxVUs: 50,
            exec: "writeWorkload",
            startTime: "0s",
            tags: { test_type: "write", rps: "50", users: "20" },
        },
        write_100_rps: {
            executor: "constant-arrival-rate",
            rate: 100,
            timeUnit: "1s",
            duration: "30s",
            preAllocatedVUs: 50,
            maxVUs: 100,
            exec: "writeWorkload",
            startTime: "40s",
            tags: { test_type: "write", rps: "100", users: "50" },
        },
        write_150_rps: {
            executor: "constant-arrival-rate",
            rate: 150,
            timeUnit: "1s",
            duration: "30s",
            preAllocatedVUs: 75,
            maxVUs: 150,
            exec: "writeWorkload",
            startTime: "80s",
            tags: { test_type: "write", rps: "150", users: "75" },
        },
        write_200_rps: {
            executor: "constant-arrival-rate",
            rate: 200,
            timeUnit: "1s",
            duration: "30s",
            preAllocatedVUs: 100,
            maxVUs: 200,
            exec: "writeWorkload",
            startTime: "120s",
            tags: { test_type: "write", rps: "200", users: "100" },
        },
        write_250_rps: {
            executor: "constant-arrival-rate",
            rate: 250,
            timeUnit: "1s",
            duration: "30s",
            preAllocatedVUs: 125,
            maxVUs: 250,
            exec: "writeWorkload",
            startTime: "160s",
            tags: { test_type: "write", rps: "250", users: "125" },
        },
        read_1000_rps: {
            executor: "constant-arrival-rate",
            rate: 1000,
            timeUnit: "1s",
            duration: "30s",
            preAllocatedVUs: 100,
            maxVUs: 200,
            exec: "readWorkload",
            startTime: "200s",
            tags: { test_type: "read", rps: "1000", users: "100" },
        },
        read_2000_rps: {
            executor: "constant-arrival-rate",
            rate: 2000,
            timeUnit: "1s",
            duration: "30s",
            preAllocatedVUs: 500,
            maxVUs: 1000,
            exec: "readWorkload",
            startTime: "240s",
            tags: { test_type: "read", rps: "2000", users: "200" },
        },
        read_3000_rps: {
            executor: "constant-arrival-rate",
            rate: 3000,
            timeUnit: "1s",
            duration: "30s",
            preAllocatedVUs: 1500,
            maxVUs: 2500,
            exec: "readWorkload",
            startTime: "280s",
            tags: { test_type: "read", rps: "3000", users: "300" },
        },
        read_4000_rps: {
            executor: "constant-arrival-rate",
            rate: 4000,
            timeUnit: "1s",
            duration: "30s",
            preAllocatedVUs: 3500,
            maxVUs: 5000,
            exec: "readWorkload",
            startTime: "320s",
            tags: { test_type: "read", rps: "4000", users: "400" },
        },
        read_5000_rps: {
            executor: "constant-arrival-rate",
            rate: 5000,
            timeUnit: "1s",
            duration: "30s",
            preAllocatedVUs: 5000,
            maxVUs: 7000,
            exec: "readWorkload",
            startTime: "360s",
            tags: { test_type: "read", rps: "5000", users: "500" },
        }
    },
    thresholds: {
        http_req_duration: ["p(95)<200", "p(99)<500"],
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
    const movieId = Math.floor(Math.random() * 1000) + 1;

    if (operation === 0) {
        // Filter by movieId only
        res = http.get(`${BASE_URL}/api/v1/shows/filter?movieId=${movieId}&page=${page}&size=${size}`, { tags: { name: 'Filter_Movie' } });

        check(res, {
            "filter by movie - status is 200": (r) => r.status === 200,
            "filter by movie - response time < 200ms": (r) => r.timings.duration < 200,
            "filter by movie - has data": (r) => {
                if (r.status !== 200) return false;
                try {
                    const body = r.json();
                    return body && typeof body.data !== 'undefined';
                } catch (e) { return false; }
            },
        });
    } else if (operation === 1) {
        // Filter by movieId and time range
        const from = new Date(Date.now()).toISOString();
        const to = new Date(Date.now() + 100 * 365 * 24 * 60 * 60 * 1000).toISOString(); // 100 years ahead
        res = http.get(`${BASE_URL}/api/v1/shows/filter?movieId=${movieId}&from=${from}&to=${to}&page=${page}&size=${size}`, { tags: { name: 'Filter_Movie_Time' } });

        check(res, {
            "filter by movie and time - status is 200": (r) => r.status === 200,
            "filter by movie and time - response time < 200ms": (r) => r.timings.duration < 200,
            "filter by movie and time - has data": (r) => {
                if (r.status !== 200) return false;
                try {
                    const body = r.json();
                    return body && typeof body.data !== 'undefined';
                } catch (e) { return false; }
            },
        });
    } else if (operation === 2) {
        // Filter by movieId, time range and theaterId
        const from = new Date(Date.now()).toISOString();
        const to = new Date(Date.now() + 100 * 365 * 24 * 60 * 60 * 1000).toISOString();
        const theaterId = Math.floor(Math.random() * 100) + 1;
        res = http.get(`${BASE_URL}/api/v1/shows/filter?movieId=${movieId}&from=${from}&to=${to}&theaterId=${theaterId}&page=${page}&size=${size}`, { tags: { name: 'Filter_Movie_Time_Theater' } });

        check(res, {
            "filter by movie, time and theater - status is 200": (r) => r.status === 200,
            "filter by movie, time and theater - response time < 200ms": (r) => r.timings.duration < 200,
            "filter by movie, time and theater - has data": (r) => {
                if (r.status !== 200) return false;
                try {
                    const body = r.json();
                    return body && typeof body.data !== 'undefined';
                } catch (e) { return false; }
            },
        });
    } else {
        // Filter with no parameters (should return all shows with pagination)
        res = http.get(`${BASE_URL}/api/v1/shows/filter?page=${page}&size=${size}`, { tags: { name: 'Filter_All' } });

        check(res, {
            "filter no params - status is 200": (r) => r.status === 200,
            "filter no params - response time < 200ms": (r) => r.timings.duration < 200,
            "filter no params - has data": (r) => {
                if (r.status !== 200) return false;
                try {
                    const body = r.json();
                    return body && typeof body.data !== 'undefined';
                } catch (e) { return false; }
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

    const res = http.post(`${BASE_URL}/api/v1/shows`, payload, params);

    const result = check(res, {
        "create show - status is 201 or 200 or 400": (r) =>
            r.status === 201 || r.status === 200 || r.status === 400,
        "create show - response time < 1000ms": (r) => r.timings.duration < 1000,
    });

    errorRate.add(!result);
    sleep(0.1);
}