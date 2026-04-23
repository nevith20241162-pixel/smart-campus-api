# Smart Campus Sensor API

A RESTful web service built with Java, JAX-RS (Jersey), and Grizzly for managing campus rooms and IoT sensor data.

## How to Run the Server

This project uses a lightweight embedded Grizzly server, meaning no standalone Tomcat installation is required.

1. Open the project folder in your IDE (e.g., VS Code).
2. Ensure you have Java 11 (or higher) and Maven installed.
3. Navigate to `src/main/java/com/smartcampus/Main.java`.
4. Run the `Main` class.
5. The server will start instantly and listen on: `http://localhost:8080/api/v1/`

## Sample `curl` Commands

# 1. API Discovery

curl -X GET http://localhost:8080/api/v1/

# 2. Get All Rooms

curl -X GET http://localhost:8080/api/v1/rooms

# 3. Create a New Room

curl -X POST http://localhost:8080/api/v1/rooms -H "Content-Type: application/json" -d "{\"name\": \"Tech Lab\", \"capacity\": 30}"

# 4. Filter Sensors by Type

curl -X GET "http://localhost:8080/api/v1/sensors?type=CO2"

# 5. Add a Sensor Reading

curl -X POST http://localhost:8080/api/v1/sensors/SENS-123/readings -H "Content-Type: application/json" -d "{\"value\": 415.5}"

---

# Conceptual Report

## Part 1: Service Architecture & Setup

**Q: Explain the default lifecycle of a JAX-RS Resource class. Is a new instance instantiated for every incoming request, or does the runtime treat it as a singleton? Elaborate on how this architectural decision impacts the way you manage and synchronize your in-memory data structures.**
By default, the JAX-RS runtime treats resource classes as per-request entities, meaning a brand new instance of the class is instantiated for every single incoming HTTP request. Because of this architectural decision, any in-memory data structures (like maps or lists used to store Rooms and Sensors) cannot be standard instance variables, or they would be wiped out after every request. Instead, they must be declared as `static` variables (or managed through a Singleton DAO pattern) so they persist in the server's memory across the application's lifecycle. Furthermore, because multiple requests (and therefore multiple threads) can access these static collections simultaneously, we must ensure thread safety to prevent race conditions or data loss.

**Q: Why is the provision of "Hypermedia" (links and navigation within responses) considered a hallmark of advanced RESTful design (HATEOAS)? How does this approach benefit client developers compared to static documentation?**
Hypermedia As The Engine Of Application State (HATEOAS) is an advanced RESTful principle where the API guides the client through the application by returning dynamic navigation links alongside the data. This benefits client developers because it decouples the client from the server's specific URI structure. Instead of hardcoding URLs based on static documentation (which breaks if the backend routing changes), the client can dynamically discover valid actions and endpoints directly from the server's response.

## Part 2: Room Management

**Q: When returning a list of rooms, what are the implications of returning only IDs versus returning the full room objects? Consider network bandwidth and client side processing.**
Returning only IDs minimizes the payload size, which saves network bandwidth. However, it forces the client to make subsequent API requests (the N+1 problem) to fetch the details for each specific room, which drastically increases client-side processing overhead and network latency. Returning full objects increases the initial payload size but provides all necessary data in a single round-trip, which is generally more efficient for modern applications.

**Q: Is the DELETE operation idempotent in your implementation? Provide a detailed justification by describing what happens if a client mistakenly sends the exact same DELETE request for a room multiple times.**
Yes, the DELETE operation is idempotent. If a client sends a DELETE request for a specific room, the server removes the room and returns a 204 No Content success status. If the client mistakenly sends the exact same DELETE request again, the server simply returns a 404 Not Found, because the resource no longer exists. In both cases, the final state of the server is identical (the room is absent), fulfilling the definition of idempotency.

## Part 3: Sensor Operations & Linking

**Q: Explain the technical consequences if a client attempts to send data in a different format, such as text/plain or application/xml. How does JAX-RS handle this mismatch?**
Because the endpoint is strictly annotated with `@Consumes(MediaType.APPLICATION_JSON)`, JAX-RS intercepts the request before it even reaches the Java method. When it detects that the client's `Content-Type` header (e.g., `text/plain`) does not match the required media type, JAX-RS automatically blocks the request and returns an HTTP 415 Unsupported Media Type error to the client.

**Q: Why is the query parameter approach generally considered superior for filtering and searching collections compared to the URL path approach?**
Query parameters (e.g., `?type=CO2`) are inherently optional and composable, which aligns perfectly with filtering a collection without changing the resource's fundamental identity. Placing the filter in the URL path (e.g., `/type/CO2`) implies a strict, rigid hierarchical relationship. If a client simply wants to view all sensors without a filter, omitting a query parameter is seamless, whereas omitting a path parameter would break the routing structure or require writing entirely redundant endpoint methods.

## Part 4: Deep Nesting with Sub - Resources

**Q: Discuss the architectural benefits of the Sub-Resource Locator pattern. How does delegating logic to separate classes help manage complexity in large APIs compared to defining every nested path in one massive controller class?**
The Sub-Resource Locator pattern enforces the principle of Separation of Concerns. In a massive API, defining every deeply nested path (like `sensors/{id}/readings`) in a single controller creates a bloated, unmaintainable "god class". By delegating logic, the parent class only handles locating the resource, while a dedicated `SensorReadingResource` class handles the specific logic for historical logs. This keeps the codebase highly modular, easier to read, and simpler to test.

## Part 5: Advanced Error Handling, Exception Mapping & Logging

**Q: Why is HTTP 422 often considered more semantically accurate than a standard 404 when the issue is a missing reference inside a valid JSON payload?**
A 404 Not Found semantically implies that the target URL endpoint itself does not exist. Conversely, an HTTP 422 Unprocessable Entity accurately communicates that the server successfully reached the endpoint and the JSON payload syntax was perfectly valid, but the business logic failed to process it because of a logical error inside the data (such as referencing a `roomId` that isn't in the system).

**Q: From a cybersecurity standpoint, explain the risks associated with exposing internal Java stack traces to external API consumers. What specific information could an attacker gather from such a trace?**
Exposing internal Java stack traces leaks sensitive architectural blueprints to potential attackers. An attacker can read the trace to identify exactly what frameworks the server is running, the specific versions of libraries in use (which can be cross-referenced with known CVE vulnerabilities), database connection structures, and internal file paths, essentially providing them with a roadmap to construct targeted exploits.

**Q: Why is it advantageous to use JAX-RS filters for cross-cutting concerns like logging, rather than manually inserting Logger.info() statements inside every single resource method?**
Using JAX-RS filters centralizes cross-cutting concerns, preventing code duplication. If logging is inserted manually into every resource method, it pollutes the core business logic, creating messy code that is incredibly tedious to update. A centralized filter guarantees that every single request and response is automatically caught and logged uniformly across the entire API, without the developer having to remember to add it to new endpoints.
