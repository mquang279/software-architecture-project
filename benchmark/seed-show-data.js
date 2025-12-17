import http from "k6/http";
import { check, sleep } from "k6";
import { Rate } from "k6/metrics";

const errorRate = new Rate("errors");
const BASE_URL = __ENV.BASE_URL || "http://localhost:8080";

function randomString(prefix) {
    return `${prefix}_${Math.random().toString(36).substring(2, 10)}`;
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
        seed_data: {
            executor: "constant-arrival-rate",
            rate: 50, // 50 requests per second -> ~15,000 shows in 5 minutes
            timeUnit: "1s",
            duration: "5m", // Run for 5 minutes
            preAllocatedVUs: 50,
            maxVUs: 200,
            exec: "writeWorkload",
        },
    },
    thresholds: {
        errors: ["rate<0.1"],
    },
};

export function writeWorkload() {
    const params = {
        headers: {
            "Content-Type": "application/json",
        },
    };

    const createdMovieId = createMovie(params);
    if (!createdMovieId) {
        return;
    }

    const createdTheaterId = createTheater(params);
    if (!createdTheaterId) {
        return;
    }

    const futureDate = new Date(Date.now() + Math.random() * 30 * 24 * 60 * 60 * 1000);
    const showLengthMinutes = 90 + Math.floor(Math.random() * 61);
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
        "create show - status is 201 or 200": (r) => r.status === 201 || r.status === 200,
    });

    errorRate.add(!result);
}
