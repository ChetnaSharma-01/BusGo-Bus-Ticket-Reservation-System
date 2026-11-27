# Bus Ticket Reservation System (Spring Boot + JPA)

A backend system for searching bus trips, booking seats, and paying for
tickets online, built to match the project report and PPT prepared for this
topic. It follows a layered architecture: **Controller → Service →
Repository → Entity**, backed by MySQL via Spring Data JPA.

---

## 1. Prerequisites

Install these before importing the project:

1. **JDK 17** or later (`java -version` to check)
2. **Eclipse IDE for Enterprise Java and Web Developers** (this bundle
   already includes Maven/m2e support - no extra plugin needed)
3. **MySQL Server 8.x**, running locally, with a known root/user password
4. **Postman** (or any REST client) to test the APIs, since there is no
   frontend UI in this build

---

## 2. Project structure

```
bus-ticket-reservation/
 ├─ pom.xml
 ├─ src/main/java/com/niet/busticket/
 │   ├─ BusTicketReservationApplication.java   (main class - run this)
 │   ├─ entity/        User, Bus, Route, Trip, Seat, Booking, Payment (+ enums)
 │   ├─ repository/    Spring Data JPA interfaces (one per entity)
 │   ├─ dto/           Request/response objects used by the controllers
 │   ├─ service/        Business logic (fare calc, seat locking, etc.)
 │   ├─ controller/    REST endpoints
 │   └─ exception/     Custom exceptions + a global error handler
 └─ src/main/resources/application.properties
```

This maps directly onto the "Project Structure" and "System Design" sections
of the project report.

---

## 3. Import into Eclipse

1. Unzip the project folder anywhere on disk.
2. In Eclipse: **File → Import → Maven → Existing Maven Projects**.
3. Click **Browse**, select the `bus-ticket-reservation` folder (the one
   containing `pom.xml`), then **Finish**.
4. Eclipse/Maven will download all dependencies automatically (needs
   internet on first import) - watch the small progress bar in the bottom
   right corner.
5. Once the build finishes with no red error markers, you're ready to run.

---

## 4. Configure the database

Open `src/main/resources/application.properties` and update the username and
password to match your local MySQL installation:

```properties
spring.datasource.username=root
spring.datasource.password=root
```

You do **not** need to manually create the `busticketdb` database - the
connection URL includes `createDatabaseIfNotExist=true`, and
`spring.jpa.hibernate.ddl-auto=update` will create all the tables
automatically from the entity classes the first time the app runs.

---

## 5. Run the application

Right-click **`BusTicketReservationApplication.java`** in Eclipse's Package
Explorer → **Run As → Java Application**.

Watch the Console view for:

```
Bus Ticket Reservation System started successfully
API base URL: http://localhost:8080/api
```

If you see a MySQL connection error instead, double-check that MySQL is
running and that the username/password in `application.properties` are
correct.

---

## 6. Testing the APIs (Postman walkthrough)

Because there's no login/session UI, this is the order to test the full
booking flow end-to-end using Postman. All requests use `Content-Type:
application/json`.

### Step 1 — Register a user
`POST http://localhost:8080/api/users/register`
```json
{ "name": "Riya Sharma", "email": "riya@example.com", "password": "secret123" }
```
Note the returned `id` — you'll use it as `userId` later.

### Step 2 — Add a bus (admin)
`POST http://localhost:8080/api/admin/buses`
```json
{ "busNumber": "UP16AB1234", "operatorName": "NIET Travels", "type": "AC", "totalSeats": 10 }
```
Note the returned `id` as `busId`.

### Step 3 — Add a route (admin)
`POST http://localhost:8080/api/admin/routes`
```json
{ "source": "Delhi", "destination": "Lucknow", "distanceKm": 500 }
```
Note the returned `id` as `routeId`.

### Step 4 — Create a trip (admin)
This automatically generates the seat map (S1..S10) for this trip.
`POST http://localhost:8080/api/admin/trips`
```json
{
  "busId": 1,
  "routeId": 1,
  "departureTime": "2026-12-05T09:00:00",
  "arrivalTime": "2026-12-05T17:00:00",
  "fare": 850
}
```
Note the returned `id` as `tripId`. Use a **future** date/time — the API
rejects trips scheduled in the past.

### Step 5 — Search trips
`GET http://localhost:8080/api/trips?source=Delhi&destination=Lucknow&date=2026-12-05`

### Step 6 — View the seat map
`GET http://localhost:8080/api/trips/1/seats`

Note the `seatId` values of one or two seats that show `"booked": false`.

### Step 7 — Create a booking
`POST http://localhost:8080/api/bookings`
```json
{ "userId": 1, "tripId": 1, "seatIds": [1, 2] }
```
Note the returned `bookingId`. Its status is `PENDING` until paid for.

### Step 8 — Pay for the booking
`POST http://localhost:8080/api/payments/1`
```json
{ "paymentMethod": "UPI" }
```
The booking's status flips to `CONFIRMED` and a transaction reference is
returned — this is your simulated e-ticket confirmation.

### Step 9 — View booking history
`GET http://localhost:8080/api/bookings/user/1`

### Step 10 — Cancel a booking
`DELETE http://localhost:8080/api/bookings/1`
This releases the seats back to the available pool — search the seat map
again (Step 6) to confirm they show `"booked": false` once more.

### Bonus — Prove the concurrency safeguard
Send the **same** `POST /api/bookings` request twice in quick succession
with the same `seatIds` (e.g. using Postman's Runner, or two browser tabs).
Exactly one request will succeed with `201 Created`; the other will receive
`409 Conflict` with the message `"Seat S1 is already booked"` — this is the
pessimistic-lock mechanism described in Chapter 4 of the report in action.

---

## 7. Notes matching the project report

- **Concurrency handling:** implemented via `@Lock(LockModeType.PESSIMISTIC_WRITE)`
  in `SeatRepository.findSeatsForUpdate()`, used inside `BookingService.createBooking()`.
- **Seats belong to trips, not buses:** see `Seat.java` and how
  `TripService.createTrip()` generates a fresh seat row per trip.
- **Payment is simulated:** `PaymentService` always returns `SUCCESS` and
  generates a mock transaction reference — swapping in a real gateway
  (Razorpay/Stripe) is listed under "Future Work" in the report.
- **Security (JWT/Spring Security) is intentionally left out** of this build
  to keep it easy to run in a classroom setting without extra login setup;
  it is also listed as a natural next step under Future Work.

---

## 8. Common issues

| Problem | Fix |
|---|---|
| `Communications link failure` on startup | MySQL isn't running, or the port/host in the datasource URL is wrong |
| `Access denied for user` | Wrong username/password in `application.properties` |
| Trip creation returns 400 with a `departureTime` error | The date/time you sent is in the past — use a future date |
| Booking returns 409 Conflict | The seat is already booked — pick a different `seatId` from the seat map |


## Frontend v2 and Admin Security

The application now has separate passenger and administrator experiences.

### Passenger
- Search uses `GET /api/trips?source=...&destination=...&date=yyyy-MM-dd`, matching `TripController`.
- Passengers can search published trips, inspect seats, register, book, pay in demo mode, view bookings and cancel bookings.
- No passenger screen exposes bus/route/trip creation.
- A search with no matching trip stays on the search/results experience; it never opens the management dashboard.

### Administrator
- Bus, route and trip creation are available only after administrator authentication.
- Spring Security protects every `/api/admin/**` endpoint except `/api/admin/login`.
- Default local-development credentials are:
  - Username: `admin`
  - Password: `Admin@123`
- For a safer deployment, set `BUSGO_ADMIN_USERNAME` and `BUSGO_ADMIN_PASSWORD` as environment variables rather than using the defaults.
- The frontend keeps the Basic Authentication credential only in `sessionStorage`; for production, deploy over HTTPS and use a server-side session or token strategy.

### API mapping checked
- `GET /api/trips` — passenger trip search
- `GET /api/trips/{tripId}/seats` — passenger seat map
- `POST /api/users/register` — passenger registration
- `POST /api/bookings` — booking
- `GET /api/bookings/user/{userId}` — booking history
- `DELETE /api/bookings/{bookingId}` — cancellation
- `POST /api/payments/{bookingId}` — simulated payment
- `POST /api/admin/login` — admin authentication
- `GET/POST /api/admin/buses` — protected admin fleet API
- `GET/POST /api/admin/routes` — protected admin route API
- `POST /api/admin/trips` — protected admin trip API

The passenger search call in `app.js` is intentionally the exact query shape required by `TripController`.


## Search troubleshooting

Passenger search uses `GET /api/trips?source=...&destination=...&date=yyyy-MM-dd`. Route matching is case-insensitive and trims leading/trailing spaces. The admin dashboard also exposes a protected `GET /api/admin/trips` view so an administrator can verify the exact route and departure date stored in the database.

After updating the project in Eclipse, run **Maven > Update Project**, then **Project > Clean**, restart Spring Boot, and hard-refresh the browser (`Ctrl+Shift+R`). The frontend assets use a version query string to reduce stale browser-cache problems.
