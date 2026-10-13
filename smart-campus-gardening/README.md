# Smart Campus Gardening & Irrigation System

Java web application (JSP + Servlets + JDBC + MySQL) for managing campus gardens, plants, irrigation schedules,
weather, notifications, water usage and maintenance tasks.

**Stack:** Java 17 · Jakarta Servlet 6 / JSP 3.1 / JSTL 3 · Apache Tomcat **10.1** · MySQL (XAMPP or Workbench) · JDBC · Bootstrap 5 · Chart.js · Maven

> **Important honesty notes**
> * This is a **software** scheduling/record-keeping system. It does **not** control valves, pumps or sensors.
> * Water volumes are **estimates** (no flow sensors). The UI labels them as estimated.
> * Without an OpenWeather API key the Weather module runs in a clearly labelled **DEMO MODE** (sample data, flagged `DEMO DATA`).

---------------------------------------------------------------------

## 0. Fastest way: no Tomcat install needed
The project contains its own web server. You only need **JDK 17**, **XAMPP (MySQL)** and **Eclipse**.
1. Start MySQL in XAMPP and import `database/smart_campus_gardening.sql` in phpMyAdmin.
2. Import the project in Eclipse (**File -> Import -> Maven -> Existing Maven Projects**) and wait for the downloads.
3. Right-click `src/main/java/com/smartcampus/Main.java` -> **Run As -> Java Application**.
4. Open **http://localhost:8080/** and log in with `admin@campus.edu` / `Admin@123`.

Terminal alternative (needs Maven): `mvn compile exec:java`

(The older Tomcat / Docker instructions below are optional and no longer needed. The `pom.xml` now builds a runnable app, not a WAR.)

---------------------------------------------------------------------

## 1. Architecture

```
Browser ──► AuthFilter (login check, admin check, CSRF token, security headers)
        ──► Servlet controller (validates input, checks role)
        ──► Service (business rules, transactions, notifications, recommendation engine)
        ──► DAO (PreparedStatement SQL only)
        ──► MySQL (via DBUtil, JDBC)
        ◄── forwards to JSP view in /WEB-INF/views (JSTL + Bootstrap)
```

| Layer | Package / folder | Notes |
|---|---|---|
| Model | `com.smartcampus.model` | Plain Java beans (User, Garden, Plant, ...) |
| DAO | `com.smartcampus.dao` | Only place that contains SQL. Always `PreparedStatement`. |
| Service | `com.smartcampus.service` | Business rules; `Tx.run(...)` wraps multi-step work in one DB transaction |
| Controller | `com.smartcampus.servlet` | One servlet per module, `@WebServlet` annotations (no web.xml mapping needed) |
| Security | `com.smartcampus.filter.AuthFilter` | Authentication, admin-only URLs, CSRF |
| Utilities | `com.smartcampus.util` | `DBUtil`, `PasswordUtil` (PBKDF2), `Validator`, `Tx`, `AppConfig` |
| Views | `src/main/webapp/WEB-INF/views` | JSPs are under WEB-INF so they can only be reached through servlets |
| Static | `src/main/webapp/assets` | `css/style.css`, `js/app.js` |
| Database | `database/smart_campus_gardening.sql` | Schema + sample data |

### Pages and URLs

| JSP (in `WEB-INF/views`) | URL | Who |
|---|---|---|
| `login.jsp` | `/login` | everyone |
| `dashboard.jsp` | `/dashboard` | all users |
| `gardens.jsp` | `/gardens` | view: all · add/edit/delete: admin |
| `plants.jsp` | `/plants` | view: all · add/edit/delete: admin |
| `irrigation.jsp` | `/irrigation` | admin: full control · staff: own schedules, mark completed |
| `weather.jsp` | `/weather` | view/refresh: all · location: admin |
| `notifications.jsp` | `/notifications` | each user sees only their own |
| `water-analytics.jsp` | `/water-analytics` | all users |
| `maintenance.jsp` | `/maintenance` | admin: full · staff: own tasks, update status |
| `reports.jsp` | `/reports` (+ `&format=csv`) | all users |
| `users.jsp` | `/users` | **admin only** |
| `profile.jsp` | `/profile` | own profile and password |

### Project structure
```
```
database/smart_campus_gardening.sql
pom.xml
src/main/java/com/smartcampus/dao/ActivityLogDAO.java
src/main/java/com/smartcampus/dao/AnalyticsDAO.java
src/main/java/com/smartcampus/dao/GardenDAO.java
src/main/java/com/smartcampus/dao/IrrigationDAO.java
src/main/java/com/smartcampus/dao/MaintenanceDAO.java
src/main/java/com/smartcampus/dao/NotificationDAO.java
src/main/java/com/smartcampus/dao/PlantDAO.java
src/main/java/com/smartcampus/dao/SettingsDAO.java
src/main/java/com/smartcampus/dao/UserDAO.java
src/main/java/com/smartcampus/dao/WeatherDAO.java
src/main/java/com/smartcampus/filter/AuthFilter.java
src/main/java/com/smartcampus/model/ActivityLog.java
src/main/java/com/smartcampus/model/Garden.java
src/main/java/com/smartcampus/model/IrrigationRecord.java
src/main/java/com/smartcampus/model/IrrigationSchedule.java
src/main/java/com/smartcampus/model/LabelValue.java
src/main/java/com/smartcampus/model/MaintenanceTask.java
src/main/java/com/smartcampus/model/Notification.java
src/main/java/com/smartcampus/model/Plant.java
src/main/java/com/smartcampus/model/Recommendation.java
src/main/java/com/smartcampus/model/ReportRow.java
src/main/java/com/smartcampus/model/User.java
src/main/java/com/smartcampus/model/WeatherRecord.java
src/main/java/com/smartcampus/service/AnalyticsService.java
src/main/java/com/smartcampus/service/GardenService.java
src/main/java/com/smartcampus/service/IrrigationService.java
src/main/java/com/smartcampus/service/MaintenanceService.java
src/main/java/com/smartcampus/service/NotificationService.java
src/main/java/com/smartcampus/service/PlantService.java
src/main/java/com/smartcampus/service/UserService.java
src/main/java/com/smartcampus/service/WeatherService.java
src/main/java/com/smartcampus/servlet/BaseServlet.java
src/main/java/com/smartcampus/servlet/DashboardServlet.java
src/main/java/com/smartcampus/servlet/GardenServlet.java
src/main/java/com/smartcampus/servlet/IrrigationServlet.java
src/main/java/com/smartcampus/servlet/LoginServlet.java
src/main/java/com/smartcampus/servlet/LogoutServlet.java
src/main/java/com/smartcampus/servlet/MaintenanceServlet.java
src/main/java/com/smartcampus/servlet/NotificationServlet.java
src/main/java/com/smartcampus/servlet/PlantServlet.java
src/main/java/com/smartcampus/servlet/ProfileServlet.java
src/main/java/com/smartcampus/servlet/ReportServlet.java
src/main/java/com/smartcampus/servlet/UserServlet.java
src/main/java/com/smartcampus/servlet/WaterAnalyticsServlet.java
src/main/java/com/smartcampus/servlet/WeatherServlet.java
src/main/java/com/smartcampus/util/AppConfig.java
src/main/java/com/smartcampus/util/BusinessException.java
src/main/java/com/smartcampus/util/DBUtil.java
src/main/java/com/smartcampus/util/DataAccessException.java
src/main/java/com/smartcampus/util/PasswordUtil.java
src/main/java/com/smartcampus/util/Tx.java
src/main/java/com/smartcampus/util/Validator.java
src/main/resources/db.properties
src/main/webapp/WEB-INF/views/dashboard.jsp
src/main/webapp/WEB-INF/views/error.jsp
src/main/webapp/WEB-INF/views/gardens.jsp
src/main/webapp/WEB-INF/views/irrigation.jsp
src/main/webapp/WEB-INF/views/layout/footer.jspf
src/main/webapp/WEB-INF/views/layout/header.jspf
src/main/webapp/WEB-INF/views/login.jsp
src/main/webapp/WEB-INF/views/maintenance.jsp
src/main/webapp/WEB-INF/views/notifications.jsp
src/main/webapp/WEB-INF/views/plants.jsp
src/main/webapp/WEB-INF/views/profile.jsp
src/main/webapp/WEB-INF/views/reports.jsp
src/main/webapp/WEB-INF/views/users.jsp
src/main/webapp/WEB-INF/views/water-analytics.jsp
src/main/webapp/WEB-INF/views/weather.jsp
src/main/webapp/WEB-INF/web.xml
src/main/webapp/assets/css/style.css
src/main/webapp/assets/js/app.js
src/main/webapp/index.jsp
```

---------------------------------------------------------------------

## 2. Step-by-step: run it on your computer

### Step 1 - Install Java JDK and Apache Tomcat
1. Install **JDK 17 or newer** (e.g. Eclipse Temurin). Check with `java -version`.
2. Download **Apache Tomcat 10.1.x** (the "zip"/"Core" download) and unzip it, e.g. `C:\apache-tomcat-10.1.x`.
   **Use Tomcat 10.1 (not 9).** This project uses the `jakarta.*` packages, which Tomcat 9 does not support.
3. Install **Eclipse IDE for Enterprise Java and Web Developers** (includes Maven support) *or* IntelliJ IDEA.

### Step 2 - Install MySQL / start it through XAMPP
* **XAMPP:** open the XAMPP Control Panel and click **Start** next to **MySQL** (and **Apache** if you want phpMyAdmin).
* **or MySQL Server + Workbench:** install them and make sure the server is running (default port 3306).

### Step 3 & 4 - Create the database and import the SQL script
The script creates the database itself (`smart_campus_gardening`), all tables, indexes, foreign keys and sample data.

* **phpMyAdmin:** open `http://localhost/phpmyadmin` → **Import** tab → *Choose file* → `database/smart_campus_gardening.sql` → **Import**.
* **MySQL Workbench:** *File → Open SQL Script…* → pick the file → click the lightning-bolt **Execute** button.

Afterwards you should see 10 tables (`users`, `gardens`, `plants`, `irrigation_schedules`, `irrigation_records`,
`weather_records`, `notifications`, `maintenance_tasks`, `activity_logs`, `app_settings`).

> Re-running the script **drops and recreates** the database (handy to reset demo data).

### Step 5 - Configure the JDBC connection
Edit `src/main/resources/db.properties`:
```properties
db.url=jdbc:mysql://localhost:3306/smart_campus_gardening?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Kolkata&characterEncoding=UTF-8
db.user=root
db.password=
```
* XAMPP's default `root` password is **empty**. If you set one (or use MySQL Server), put it in `db.password`.
* If XAMPP's MySQL runs on port 3307, change `3306` in the URL.
* Optional: the environment variables `DB_URL`, `DB_USER`, `DB_PASSWORD` override the file (useful so passwords stay out of source control).

### Step 6 - Import the project into Eclipse
1. **File → Import → Maven → Existing Maven Projects** → select the `smart-campus-gardening` folder → **Finish**.
2. Wait for the bottom-right progress bar (Maven downloads the dependencies once - internet needed).
   Dependencies (from `pom.xml`): MySQL Connector/J 8.3, Gson 2.10, JSTL (Jakarta) - Servlet/JSP API come from Tomcat.
3. If Eclipse shows errors on the project: right-click → **Maven → Update Project…** (tick *Force Update*).
   If it complains about the Java version: **Project → Properties → Java Compiler** → 17.

*(IntelliJ IDEA: **File → Open** → choose `pom.xml` → "Open as Project".)*

### Step 7 - Configure Tomcat
**Eclipse**
1. **Window → Preferences → Server → Runtime Environments → Add… → Apache Tomcat v10.1** → point to your Tomcat folder and choose your JDK 17.
2. Right-click the project → **Run As → Run on Server** → choose the Tomcat 10.1 server → **Finish**.

**IntelliJ IDEA Ultimate:** *Run → Edit Configurations → + → Tomcat Server → Local* → set the Tomcat 10.1 home → *Deployment* tab → add
`smart-campus-gardening:war exploded` with application context `/smart-campus-gardening`.

**No IDE alternative:** run `mvn clean package`, copy `target/smart-campus-gardening.war` into Tomcat's `webapps/` folder and run `bin/startup.bat` (or `startup.sh`).

### Step 8 - Open the website
Browse to **http://localhost:8080/smart-campus-gardening/** - you will land on the login page.

### Step 9 - Log in with the demo accounts
| Role | Email | Password |
|---|---|---|
| Administrator | `admin@campus.edu` | `Admin@123` |
| Maintenance staff | `ravi@campus.edu` | `Staff@123` |
| Maintenance staff | `meena@campus.edu` | `Staff@123` |
| Inactive (cannot log in) | `karthik@campus.edu` | `Staff@123` |

### Step 10 - Test every module (and verify MySQL)
Keep phpMyAdmin / Workbench open, and press refresh on the table after each action.

| Module | What to do | Verify in MySQL |
|---|---|---|
| Login | Sign in as admin; try a wrong password; sign out | `activity_logs` gets an `AUTH` row on sign-in |
| Gardens | Add a garden → edit it → search for it → delete it (confirm dialog) | rows appear/change/disappear in `gardens` |
| Plants | Add 20 "Lettuce" to a garden; filter by garden and category; edit; delete | `plants.garden_id` = that garden's id |
| Irrigation | Look at the **recommendations**; click *Schedule this*; pick date → save; edit the time (status becomes *Rescheduled*); cancel one | `irrigation_schedules` rows + `status`; staff get `notifications` rows |
| Complete irrigation | Log in as **ravi**; open Irrigation → *Complete* on his schedule | `irrigation_schedules.status=COMPLETED` **and** a new `irrigation_records` row (one transaction) |
| Weather | Open Weather (demo banner shows without API key); as admin change the location | new `weather_records` row; `app_settings.campus_location` |
| Notifications | Open as ravi: mark one read, then *Mark all read*; watch the bell counter | `notifications.is_read` flips to 1 |
| Water analytics | Change the date range and garden filter | totals match `SUM(estimated_water_used_liters)` of `irrigation_records` |
| Maintenance | Admin: create + assign a task; staff: change its status; admin: edit/delete | `maintenance_tasks` rows and `status` |
| Reports | Filter by date/garden/type; click **Print**; click **Export CSV** | CSV opens in Excel |
| Users | Add a staff user; deactivate; try logging in as them; reset password | `users.status`; `password_hash` is a long hash, never plain text |
| Security | As ravi open `/users` → "403". Log out, use the Back button → redirected to login | - |

---------------------------------------------------------------------

## 3. Weather API key (live data)
1. Create a free account at <https://openweathermap.org/api> and copy your API key (new keys can take up to a couple of hours to activate).
2. Provide it **outside the source code** in one of two ways:
   * environment variable `OPENWEATHER_API_KEY` (in Eclipse: *Run → Run Configurations → Apache Tomcat → Environment*), **or**
   * a file `smartcampus-secrets.properties` in your home folder (`C:\Users\<you>\` or `~/`) containing
     `openweather.api.key=YOUR_KEY`
3. Restart Tomcat. The Weather page then shows "OpenWeather API (live)". The admin can set the campus location (e.g. `Chennai,IN`).

If the API fails, the page shows a clear fallback message and the last saved reading (if any).

## 4. How the irrigation recommendation works
For each garden the engine (`IrrigationService.recommend`) combines:
1. **Plant needs:** litres = Σ(quantity × litres per plant); due when *days since last irrigation ≥ shortest watering interval*.
2. **Irrigation history:** the most recent row in `irrigation_records`.
3. **Weather:** latest `weather_records` row - rain (now + next 24 h) ≥ 5 mm ⇒ **postpone**; ≥ 2 mm ⇒ halve the quantity; ≥ 35 °C ⇒ +10 %.

It returns a headline plus a list of plain-English reasons (and says when the weather is demo data).

## 5. Security features
* Passwords: salted **PBKDF2-HMAC-SHA256** (120 000 iterations), constant-time comparison.
* Sessions: new session at login (anti session-fixation), 30-minute timeout, HttpOnly cookie, user re-checked in the DB on every request (deactivation is immediate).
* Role-based access: `AuthFilter` protects `/users`; servlets re-check admin rights for every write; staff can only change *their own* schedules/tasks (enforced in the service layer).
* CSRF token on every POST; SQL injection prevented by `PreparedStatement` everywhere (no string-built values); output escaped with `<c:out>`.
* Server-side validation (`Validator`) for every form; login rate-limit (5 failures ⇒ 5-minute lock).
* DB errors are logged on the server and shown to users as a generic message; CSV export neutralises spreadsheet formulas.
* Transactions (`Tx.run`) for: completing irrigation (status + record + notifications + log), creating/updating schedules, tasks, user changes.

## 6. Optional: e-mail notifications
In-app notifications are the default. To add e-mail later:
1. Add to `pom.xml`: `org.eclipse.angus:jakarta.mail:2.0.3` (and `jakarta.mail:jakarta.mail-api:2.1.3`).
2. Create `EmailService` with `send(String to, String subject, String body)` using `jakarta.mail.Session` + SMTP
   (e.g. Gmail SMTP with an **App Password**). Read host/user/password from **environment variables**, never from source code.
3. In `NotificationService.notifyUser(...)`, after the DB insert, look up the user's e-mail and call `EmailService.send(...)`
   (ideally on a background thread so a slow mail server never blocks a request).
4. Add a "send me e-mail alerts" checkbox column to `users` if you want per-user opt-in.

## 7. Troubleshooting
| Problem | Fix |
|---|---|
| HTTP 404 on `/smart-campus-gardening/` | Check the server uses Tomcat **10.1** and the context path is `/smart-campus-gardening` |
| `ClassNotFoundException: jakarta.servlet...` / `javax.servlet` errors | You are on Tomcat 9 - switch to 10.1 |
| "A database error occurred" | Is MySQL running? Check `db.properties`; see the Tomcat console for the real stack trace |
| `Access denied for user 'root'` | Put your MySQL root password in `db.password` |
| Login always fails | Re-import the SQL script (it holds the demo password hashes) |
| Page has no styling/icons | The browser needs internet for Bootstrap/Chart.js CDNs (see below to self-host) |
| Login/dashboard banner photo is missing | It is hot-linked from Unsplash; a green gradient shows instead. Save your own photo as `assets/img/garden.jpg` and change the two `url(...)` lines in `style.css` |

## 8. Known limits (kept simple on purpose for a college project)
* Plain `DriverManager` connections (no pool). For production use HikariCP.
* The login throttle is in memory (resets when Tomcat restarts).
* Bootstrap, icons, Chart.js and Google Fonts load from CDNs, so the browser needs internet.
* Water usage is estimated; no hardware integration.
