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
-  [Technical Challenges & Solutions](#6)

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
  ![springboot](https://img.shields.io/badge/-spring%20boot%203.4-333333?style=flat&logo=springboot)
  ![springdataJPA](https://img.shields.io/badge/-spring%20Data%20JPA-333333?style=flat&logo=spring)
  ![springsecurity](https://img.shields.io/badge/-spring%20security-333333?style=flat&logo=springsecurity)
  ![JWT](https://img.shields.io/badge/-JWT-333333?style=flat&logo=JSON%20web%20tokens)

 
- **Data Layer** 
  
  ![MySQL](https://img.shields.io/badge/-MySQL-333333?style=flat&logo=mysql)
  ![Redis](https://img.shields.io/badge/-Redis%207.0-333333?style=flat&logo=Redis)
 
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

<img width="800" src="https://github.com/user-attachments/assets/b1a51dee-d3ce-4a8c-93c4-26a46ca8bc5e" />

<br/>

----
<h2 id="3">
    <b>📌 ERD</b>
</h2>

<img width="979" src="https://github.com/user-attachments/assets/7f4df895-2c2f-4c5a-8eb8-5dc1b9695b17" />

<br/>

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

- registration, login, logout, and profile management
- Real-time access to both regular and flash sale items with live inventory tracking
- Order options: individual items or multiple items through shopping cart
- Order history and cancel order

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
        - Daily DB-Redis inventory synchronization
        - Flash sale event management
            - Automatic status updates based on start/end times
            - Auto-termination upon inventory depletion
</br>

----
<h2 id="6">
    <b>📌 Technical Challenges & Solutions</b>
</h2>

To be added
