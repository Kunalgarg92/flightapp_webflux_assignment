Flight Management WebFlux Application

A fully reactive Airline Flight Booking Management System built using Spring WebFlux, designed to demonstrate high-quality code standards, complete validation workflows, accurate exception handling, and robust architecture.
The project includes 92%+ unit test coverage and follows SonarQube quality rules to ensure clean, maintainable, and production-grade code.

 Project Expectations & Objectives

This project has been built with the following core goals:

1)Full Reactive Programming

Use Spring WebFlux, Mono, and Flux across all layers (controllers, services, repositories).

2)Clean Architecture & Modularity

Separate each layer responsibly:

DTOs

Models

Repositories

Services

Implementations

Controllers

Global Exception Handling

3)Strong Validation

Strict request validation using:

@NotBlank

@Min

@Email

@Future

@Pattern

@NotNull

@Valid

4)High Code Quality

92% test coverage

All critical flows covered

SonarQube analyzed and passed

Duplicates removed, code smells resolved

Clear separation of concerns

5)Professional Documentation & Clean Structure

Every module/class described

Detailed booking logic

Flight search scenarios

Exception workflow mapping

src/main/java/com/flightapp/webflux
│
├─ Controller
├─ Service
│   ├─ implementation
├─ DTO
├─ Model
├─ Repository
├─ Exception
└─ Test (92% test coverage)

 Core Functionalities
11) Flight Inventory Management (Admin)

Admin can:

Add flights

Provide airline info

Set timings

Set base fare & special category fare

Define fare category rules

Validation includes:

Airline name between 2–50 chars

Flight number regex (e.g., 6E101, AI202)

Departure & arrival must be future

Special fare > 0

Total seats ≥ 1

Fare category must be one of:
STUDENT, SENIOR, REGULAR, ARMY, CORPORATE


2) Flight Search Service (User)

Users can search flights via:

Origin & destination

Travel date

Travel time (optional)

One-way

Round-trip

Handles:

No flights found

Only onward flights found

Missing return-flight scenarios

Result combined with flight metadata


3) Flight Booking System

Booking includes:

 3.1)Validations:

Email mandatory and valid

Passengers list must not be empty

Number of seats must match passenger count

Duplicate seats rejected

Seat number out-of-range rejected

Insufficient seats rejected

Fare category applied accurately

Special fare applied if matching inventory fare category

3.2) Booking Workflow:

Validate input

Check flight availability

Validate seat numbers

Check already booked seats

Generate unique PNR

Save booking & passenger details

Deduct seats from flight inventory

Return structured BookingResponse

3.3) Special Fare Rules:

If passenger category matches flight fare category:

Special fare applied
Else:

Regular fare


DTOs Included : 
1) BookingRequest

email

numberOfSeats

List of PassengerRequest

validations included

2) PassengerRequest

name

gender (MALE/FEMALE/OTHER)

age

seatNumber

fareCategory

meal (VEG/NON_VEG)

strong field validations

3) FlightInventoryRequest

airlineName

flightNumber

fromPlace / toPlace

departureTime / arrivalTime

totalSeats

price / specialFare

fareCategory

4) FlightSearchRequest

travelDate

returnDate

travelTime

fromPlace / toPlace

tripType

5) FlightSearchResponse

airlineName

flightNumber

price

message in cases like missing return flight

Models :
1) BookingTicket

Represents complete booking metadata including:

bookingTime

numberOfSeats

totalPrice

email

cancelled flag

PNR

2) Passenger

Represents stored passenger info including special fare applied.

3) FlightInventory

Represents complete flight details including seat tracking.

Services & Implementations:

1) FlightInventoryService

Add Inventory

Search Flights (one-way, round-trip)

Time-range logic

Flight filtering

2)  BookingService

Book flight

Generate unique PNR

Validate seats

Save booking + passengers

Cancel booking

Fetch ticket by PNR

Fetch booking history

3) Service Implementation Details

Fully reactive

Uses Mono/Flux everywhere

Contains pipeline-based logic

Proper error propagation

Structured response builders

Centralized Exception Handling

Using @RestControllerAdvice:

Handled Exceptions:
Exception	                            Status	                Description
WebExchangeBindException	             400	                Body validation errors
MethodArgumentNotValidException	         400	                DTO validation
HttpMessageNotReadableException	         400	                Bad or malformed JSON
ConstraintViolationException	         400	                Path variable / request param violations
IllegalArgumentException	             400	                Business validation errors
ResourceNotFoundException	             404	                Missing resource
NullPointerException	                 500	                Unexpected null
Exception	                             500	                Global fallback

Response Body:

timestamp

status

error

message

path


Test Coverage: 92%
Included tests:

Controllers
 Services
 Service Implementations
 GlobalErrorHandler
 DTO validation
 Booking scenarios
 PNR generation logic
 Special fare calculations
 Duplicate seat handling
 Round-trip logic
 Search service
 Admin inventory controller tests

Coverage Achieved:

92% Line Coverage

95% Class Coverage

All critical branches tested

No flaky tests

No unnecessary stubbing

Mockito + WebTestClient + JUnit 5


SonarQube Integration : 

The project passes SonarQube Quality Gate with:

 0 Bugs
 0 Vulnerabilities
 0 Security Hotspots
 0 Code Smells
 No duplicate code
 Clean architecture
 High test coverage (92%)
 Proper naming & coding standard
 
 Endpoints Summary :
 
BookingController
POST  /api/v1.0/flight/booking/{flightId}
GET   /api/v1.0/flight/booking/history/{email}
DELETE /api/v1.0/flight/booking/cancel/{pnr}

TicketController
GET /api/v1.0/flight/ticket/{pnr}

AdminController
POST /api/v1.0/flight/airline/inventory/add

SearchController
POST /api/v1.0/flight/search

Technologies Used :

Java 17

Spring Boot WebFlux

Reactor Core

Reactive Repositories

Reactive MongoDB / R2DBC (based on implementation)

JUnit 5

Mockito

WebTestClient

SonarQube

Maven