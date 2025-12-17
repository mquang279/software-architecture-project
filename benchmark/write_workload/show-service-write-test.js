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

export function setup() {
    const params = {
        headers: {
            "Content-Type": "application/json",
        },
    };

    const theaterIds = [];
    // Create 50 theaters to spread the load
    for (let i = 0; i < 50; i++) {
        const id = createTheater(params);
        if (id) {
            theaterIds.push(id);
        }
    }

    console.log(`Setup complete. Created ${theaterIds.length} theaters.`);

    const movieIds = [];
    // Create 50 movies to spread the load
    for (let i = 0; i < 50; i++) {
        const id = createMovie(params);
        if (id) {
            movieIds.push(id);
        }
    }
    console.log(`Setup complete. Created ${movieIds.length} movies.`);

    return { theaterIds, movieIds };
}

export const options = {
    scenarios: {
        write_low_load: {
            executor: "constant-arrival-rate",
            rate: 300,
            timeUnit: "1s",
            duration: "1m",
            preAllocatedVUs: 50,
            maxVUs: 500,
            exec: "writeWorkload",
            startTime: "0s",
            tags: { test_type: "write", rps: "50", users_estimated: "50" },
        },
        write_medium_load: {
            executor: "constant-arrival-rate",
            rate: 600,
            timeUnit: "1s",
            duration: "1m",
            preAllocatedVUs: 100,
            maxVUs: 1000,
            exec: "writeWorkload",
            startTime: "1m10s",
            tags: { test_type: "write", rps: "250", users_estimated: "250" },
        },
        write_peak_load: {
            executor: "constant-arrival-rate",
            rate: 1000, // 10% of reads (This is your realistic peak)
            timeUnit: "1s",
            duration: "1m",
            preAllocatedVUs: 200,
            maxVUs: 2000,
            exec: "writeWorkload",
            startTime: "2m20s",
            tags: { test_type: "write", rps: "500", users_estimated: "500" },
        },
        write_stress_test: {
            executor: "constant-arrival-rate",
            rate: 1500,
            timeUnit: "1s",
            duration: "1m",
            preAllocatedVUs: 500,
            maxVUs: 5000,
            exec: "writeWorkload",
            startTime: "3m30s",
            tags: { test_type: "write", rps: "1000", users_estimated: "1000" },
        },
        cleanup_delay: {
            executor: "constant-vus",
            vus: 1,
            duration: "10s",
            startTime: "4m40s",
            exec: "doNothing",
        }
    },
    thresholds: {
        http_req_duration: ["p(95)<1000", "p(99)<2000"], // Relaxed for writes
        http_req_failed: ["rate<0.1"],
        errors: ["rate<0.1"],
    },
};

// Write workload - POST create show
export function writeWorkload(data) {
    const params = {
        headers: {
            "Content-Type": "application/json",
        },
    };

    let createdMovieId;
    if (data && data.movieIds && data.movieIds.length > 0) {
        createdMovieId = data.movieIds[Math.floor(Math.random() * data.movieIds.length)];
    } else {
        createdMovieId = createMovie(params);
    }

    if (!createdMovieId) {
        sleep(0.1);
        return;
    }

    let createdTheaterId;
    if (data && data.theaterIds && data.theaterIds.length > 0) {
        createdTheaterId = data.theaterIds[Math.floor(Math.random() * data.theaterIds.length)];
    } else {
        createdTheaterId = createTheater(params);
    }

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
