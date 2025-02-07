# Dealify - Catch Your Deal ⚡
Dealify transforms ordinary online shopping into an exciting time-based experience.
<br>While offering regular e-commerce features, it specializes in flash deals where limited-quantity premium items become available at specific times.

----
## 🗂️ Table of Contents
-  [Project Introduction](#0)
-  [Tech Stack](#1)
-  [System Architecture](#2)
-  [ERD](#3)
-  [API Documentation](#4)
-  [Key Features](#5)
-  [Performance Optimization](#6)
-  [Technical Challenges & Solutions](#7)

----
<h2 id="0">
    <b>📌 Project Introduction</b>
</h2>

- Personal Project
- Development Period: Dec. 2024 - Present
- This project implements a scalable E-Commerce system designed to address technical challenges commonly encountered in real-world services, including:
    - Concurrency control for limited inventory management
    - Performance optimization using caching strategies
    - Data consistency in high-traffic sales environment
   
----
<h2 id="1">
    <b>📌 Tech Stack</b>
</h2>

- **Application Layer**

  ![Java](https://img.shields.io/badge/-Java%2021-333333?style=flat&logo=OpenJDK&logoColor=007396)
  ![springboot](https://img.shields.io/badge/-Spring%20Boot%203.4.0-333333?style=flat&logo=springboot)
  ![springdataJPA](https://img.shields.io/badge/-Spring%20Data%20JPA-333333?style=flat&logo=spring)
  ![springsecurity](https://img.shields.io/badge/-Spring%20Security-333333?style=flat&logo=springsecurity)
  ![JWT](https://img.shields.io/badge/-JWT-333333?style=flat&logo=JSON%20web%20tokens)

 
- **Data Layer** 
  
  ![MySQL](https://img.shields.io/badge/-MySQL%208.0-333333?style=flat&logo=mysql)
  ![Redis](https://img.shields.io/badge/-Redis%207.4.2-333333?style=flat&logo=Redis)
 
- **DevOps & Infrastructure**
  
  ![Gradle](https://img.shields.io/badge/-Gradle-333333?style=flat&logo=gradle&logoColor=white)
  ![Docker](https://img.shields.io/badge/-Docker-333333?style=flat&logo=docker)
  ![Docker Compose](https://img.shields.io/badge/-Docker%20Compose-333333?style=flat&logo=docker&logoColor=2496ED)
  ![K6](https://img.shields.io/badge/-k6-333333?style=flat&logo=k6)

 
- **External Services**

  ![Gmail SMTP](https://img.shields.io/badge/-Gmail%20SMTP-333333?style=flat&logo=gmail&logoColor=white)
  
----
<h2 id="2">
    <b>📌 System Architecture</b>
</h2>

![System Architecture](https://github.com/user-attachments/assets/1212881a-3a61-4eb0-91b2-6cbd10cad844)

----
<h2 id="3">
    <b>📌 ERD</b>
</h2>

![ERD](https://github.com/user-attachments/assets/44e4e83c-b2c8-4d84-98ea-eaaa54f26648)


----
<h2 id="4">
    <b>📌 API Documentation</b>
</h2>

- ### Explore all the API endpoints and their usage in the [Postman API Documentation](https://documenter.getpostman.com/view/37464460/2sAYJ6BenC).

<br/>

----
<h2 id="5">
    <b>📌 Key Features</b>
</h2>

### 👥 User Features

- Registration, login, logout, and profile management
- Real-time access to both regular and flash sale items with live inventory tracking
- Order options: individual items or multiple items through shopping cart
- View order history and cancel order

### 🛠 Technical Highlights

- **Security & Authentication**
    - User registration with email verification through Google SMTP
    - JWT-based authentication/authorization system
    - Enhanced security with dual-token architecture (Access + Refresh)
    - Automatic token expiration handling with Redis TTL
- **Product & Inventory System**
    - Optimized product listing with cursor-based pagination
    - Enhanced performance through Redis caching for product/inventory data retrieval
- **Order & Payment Processing**
    - Concurrency control using Redisson distributed locks
    - Automated management with Spring Scheduler
        - Auto-cancellation and inventory restoration for incomplete orders (5-minute timeout)
        - Flash sale event management
            - Automatic status updates based on start/end times
            - Auto-sale ends upon inventory depletion
          
</br>

----
<h2 id="6">
    <b>📌 Performance Optimization</b>
</h2>

### 1. Improving Real-Time Inventory Reads

**[Issues]**

- **Single MySQL DB Bottleneck**: Handling all read requests through one database led to surging load
- **High Concurrency Impact** : Elevated traffic caused performance degradation and slower response times

**[Solutions]**

- **Cache-Aside Pattern** : Frequently accessed stock data is stored in Redis. If a cache miss occurs, the system fetches from the DB and then updates the cache
- **Write-Through Pattern** : Synchronous updates to both DB and Redis to ensure data consistency
- **Dynamic TTL Strategy** : Cache expiration times vary with inventory levels for real-time accuracy

**[Outcomes]** [🔗Performance Test Results](https://github.com/mjyoo0353/dealify/wiki/Performance-Test-Results#1-improving-real-time-inventory-reads)

- **Reduced DB Load** : Optimized database load management
- **Inventory query response times dropped by 92% with 156% increased throughput**
  
<img width="650" alt="재고 조회 성능 테스트 표_최종" src="https://github.com/user-attachments/assets/b327616e-2bff-4676-a517-eb1a919c78f3" />

<h2></h2>

### 2. Performance Improvements for Flash Sale Event Listing 

**[Issues]**

- **High DB Load**: All read requests were routed to a single database, causing overload
- **N+1 Queries** : Fetching related product information for each flash sale event triggered excessive queries
- **Large Data Overhead** : Handling bulk data led to unnecessary queries and performance bottlenecks

**[Solutions]**

- **Replica DB Reads** : Distribute read traffic to replica databases to reduce load on the primary DB
- **Batch Fetching** : Eliminate N+1 query overhead by loading related entities in a single, batched query
- **Cursor-Based Pagination** : Efficiently manage large datasets and improve throughput by using cursor-based pagination

**[Outcomes]** [🔗Performance Test Results](https://github.com/mjyoo0353/dealify/wiki/Performance-Test-Results#2-performance-improvements-for-flash-sale-event-listing)

- **N+1 Queries Removed** : Decreased query count from N+1 to a single query per request
- **Achieved a 64% reduction in average response time** under a 5,000 concurrent user load test

<img width="650" alt="행사 조회 성능 테스트 표" src="https://github.com/user-attachments/assets/e9f91962-f35f-4a45-9853-c1e47dcda122" />

<h2></h2>

### **3. Resolving Data Consistency Issues and Improving Response Performance in Order-Payment Process**

**[Issues]**

- **Pessimistic Lock Limitation** : Ineffective concurrency control leading to overselling in Primary-Replica DB structure
- **Race Condition** : Data consistency issues in high-concurrency order processing

**[Solutions]**

- **Atomic Operation** : Implementation of Redis Lua Script for verification and deduction operations
- **Fault Tolerance** : DB fallback logic implementation for Redis failure scenarios

**[Outcomes]**

- **Data Consistency** : **Achieved 100% accuracy** in concurrent order processing
- **Lock Contention** : Eliminated timeout failures with 0% occurrence rate
- **70.3% reduction in response time with 205% increased throughput**

<img width="970" alt="image" src="https://github.com/user-attachments/assets/953169bf-d96e-4893-9a01-5e550515de31" />

<br>

----
<h2 id="7">
    <b>📌 Technical Challenges & Solutions</b>
</h2>

To be added
