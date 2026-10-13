-- =====================================================================
--  Smart Campus Gardening & Irrigation System  -  MySQL setup script
--  File: database/smart_campus_gardening.sql
--  Run once in MySQL Workbench or phpMyAdmin (Import tab).
--  WARNING: it DROPs and recreates the database 'smart_campus_gardening'.
-- =====================================================================
DROP DATABASE IF EXISTS smart_campus_gardening;
CREATE DATABASE smart_campus_gardening CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE smart_campus_gardening;

CREATE TABLE users (
  user_id        INT AUTO_INCREMENT PRIMARY KEY,
  full_name      VARCHAR(100) NOT NULL,
  email          VARCHAR(120) NOT NULL UNIQUE,
  password_hash  VARCHAR(255) NOT NULL,
  role           ENUM('ADMIN','STAFF') NOT NULL DEFAULT 'STAFF',
  status         ENUM('ACTIVE','INACTIVE') NOT NULL DEFAULT 'ACTIVE',
  phone          VARCHAR(20) NULL,
  created_at     TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at     TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX idx_users_role_status (role, status)
) ENGINE=InnoDB;

CREATE TABLE gardens (
  garden_id    INT AUTO_INCREMENT PRIMARY KEY,
  garden_name  VARCHAR(100) NOT NULL,
  location     VARCHAR(150) NOT NULL,
  area_sqm     DECIMAL(10,2) NOT NULL,
  garden_type  VARCHAR(50) NOT NULL,
  description  TEXT NULL,
  status       ENUM('ACTIVE','UNDER_MAINTENANCE','INACTIVE') NOT NULL DEFAULT 'ACTIVE',
  created_at   TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at   TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX idx_gardens_status (status),
  INDEX idx_gardens_name (garden_name)
) ENGINE=InnoDB;

CREATE TABLE plants (
  plant_id                 INT AUTO_INCREMENT PRIMARY KEY,
  garden_id                INT NOT NULL,
  plant_name               VARCHAR(100) NOT NULL,
  category                 VARCHAR(50) NOT NULL,
  quantity                 INT NOT NULL DEFAULT 1,
  watering_frequency_days  INT NOT NULL DEFAULT 2,
  water_requirement_liters DECIMAL(8,2) NOT NULL DEFAULT 1.00,  -- litres per plant per watering
  description              TEXT NULL,
  created_at               TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at               TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT fk_plants_garden FOREIGN KEY (garden_id) REFERENCES gardens(garden_id) ON DELETE CASCADE,
  INDEX idx_plants_garden (garden_id),
  INDEX idx_plants_category (category)
) ENGINE=InnoDB;

CREATE TABLE irrigation_schedules (
  schedule_id            INT AUTO_INCREMENT PRIMARY KEY,
  garden_id              INT NOT NULL,
  scheduled_at           DATETIME NOT NULL,
  estimated_water_liters DECIMAL(10,2) NOT NULL,
  assigned_user_id       INT NULL,
  status                 ENUM('PENDING','COMPLETED','RESCHEDULED','CANCELLED') NOT NULL DEFAULT 'PENDING',
  notes                  VARCHAR(255) NULL,
  created_by             INT NULL,
  created_at             TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at             TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT fk_sched_garden FOREIGN KEY (garden_id) REFERENCES gardens(garden_id) ON DELETE CASCADE,
  CONSTRAINT fk_sched_user   FOREIGN KEY (assigned_user_id) REFERENCES users(user_id) ON DELETE SET NULL,
  CONSTRAINT fk_sched_creator FOREIGN KEY (created_by) REFERENCES users(user_id) ON DELETE SET NULL,
  INDEX idx_sched_when (scheduled_at),
  INDEX idx_sched_status (status),
  INDEX idx_sched_garden (garden_id),
  INDEX idx_sched_user (assigned_user_id)
) ENGINE=InnoDB;

CREATE TABLE irrigation_records (
  record_id                  INT AUTO_INCREMENT PRIMARY KEY,
  schedule_id                INT NOT NULL UNIQUE,
  completed_at               DATETIME NOT NULL,
  estimated_water_used_liters DECIMAL(10,2) NOT NULL,   -- ESTIMATE: no flow sensors are used
  notes                      VARCHAR(255) NULL,
  recorded_by                INT NULL,
  created_at                 TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_rec_schedule FOREIGN KEY (schedule_id) REFERENCES irrigation_schedules(schedule_id) ON DELETE CASCADE,
  CONSTRAINT fk_rec_user FOREIGN KEY (recorded_by) REFERENCES users(user_id) ON DELETE SET NULL,
  INDEX idx_rec_completed (completed_at)
) ENGINE=InnoDB;

CREATE TABLE weather_records (
  weather_id           INT AUTO_INCREMENT PRIMARY KEY,
  campus_location      VARCHAR(120) NOT NULL,
  temperature_c        DECIMAL(5,2) NOT NULL,
  conditions           VARCHAR(100) NOT NULL,
  humidity             INT NOT NULL DEFAULT 0,
  rain_mm_current      DECIMAL(6,2) NOT NULL DEFAULT 0,
  rain_mm_forecast_24h DECIMAL(6,2) NOT NULL DEFAULT 0,
  forecast_summary     TEXT NULL,            -- JSON array of forecast entries
  is_demo              TINYINT(1) NOT NULL DEFAULT 0,
  retrieved_at         TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  INDEX idx_weather_time (retrieved_at)
) ENGINE=InnoDB;

CREATE TABLE notifications (
  notification_id   INT AUTO_INCREMENT PRIMARY KEY,
  user_id           INT NOT NULL,
  message           VARCHAR(300) NOT NULL,
  notification_type ENUM('IRRIGATION','MAINTENANCE','WEATHER','SYSTEM') NOT NULL DEFAULT 'SYSTEM',
  is_read           TINYINT(1) NOT NULL DEFAULT 0,
  created_at        TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_notif_user FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
  INDEX idx_notif_user_read (user_id, is_read)
) ENGINE=InnoDB;

CREATE TABLE maintenance_tasks (
  task_id           INT AUTO_INCREMENT PRIMARY KEY,
  garden_id         INT NOT NULL,
  assigned_user_id  INT NULL,
  title             VARCHAR(150) NOT NULL,
  description       TEXT NULL,
  priority          ENUM('LOW','MEDIUM','HIGH') NOT NULL DEFAULT 'MEDIUM',
  due_date          DATE NOT NULL,
  status            ENUM('PENDING','IN_PROGRESS','COMPLETED') NOT NULL DEFAULT 'PENDING',
  completed_at      DATETIME NULL,
  created_by        INT NULL,
  created_at        TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at        TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT fk_task_garden FOREIGN KEY (garden_id) REFERENCES gardens(garden_id) ON DELETE CASCADE,
  CONSTRAINT fk_task_user FOREIGN KEY (assigned_user_id) REFERENCES users(user_id) ON DELETE SET NULL,
  CONSTRAINT fk_task_creator FOREIGN KEY (created_by) REFERENCES users(user_id) ON DELETE SET NULL,
  INDEX idx_task_status_due (status, due_date),
  INDEX idx_task_user (assigned_user_id)
) ENGINE=InnoDB;

CREATE TABLE activity_logs (
  log_id         INT AUTO_INCREMENT PRIMARY KEY,
  user_id        INT NULL,
  activity_type  VARCHAR(30) NOT NULL,        -- GARDEN, PLANT, IRRIGATION, MAINTENANCE, USER, AUTH, WEATHER
  description    VARCHAR(300) NOT NULL,
  created_at     TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_log_user FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE SET NULL,
  INDEX idx_log_time (created_at),
  INDEX idx_log_type (activity_type)
) ENGINE=InnoDB;

-- Additional table: key/value application settings (e.g. campus location for weather)
CREATE TABLE app_settings (
  setting_key   VARCHAR(60) PRIMARY KEY,
  setting_value VARCHAR(255) NOT NULL,
  updated_at    TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- =====================================================================
--  SAMPLE DATA
--  Demo logins:  admin@campus.edu / Admin@123   (ADMIN)
--                ravi@campus.edu  / Staff@123   (STAFF)
--                meena@campus.edu / Staff@123   (STAFF)
--  Passwords are stored as salted PBKDF2-HMAC-SHA256 hashes (never plain text).
-- =====================================================================

INSERT INTO users (full_name,email,password_hash,role,status,phone) VALUES
('Dr. Anita Raman','admin@campus.edu','120000:F8rQnmYtVFP8qX4QDzGszQ==:XHMA5YYZIQZ6DNmDyNBYVpPxovWre5EZ50wfx5b/ekI=','ADMIN','ACTIVE','9840011111'),
('Ravi Kumar','ravi@campus.edu','120000:Mb4OJQcK1O357cEkXB2XsA==:M48ihbbqu9JF7DAUKsG45VasO+rnBe7tM61WwgpHiLo=','STAFF','ACTIVE','9840022222'),
('Meena Selvam','meena@campus.edu','120000:+ns30mgla4nEl9zG4xp/eg==:RBChsncJTBwuqdUGjQffpRv1fXzo0E8WEbVz4jto0Vg=','STAFF','ACTIVE','9840033333'),
('Karthik Raja','karthik@campus.edu','120000:69TukcIoZNfPfvIyv7EplQ==:BX9T2A0aKijxEPV1ghDRc5lYwMqz1bzCwxqBh+jUGwQ=','STAFF','INACTIVE','9840044444');

INSERT INTO gardens (garden_name,location,area_sqm,garden_type,description,status) VALUES
('Main Lawn','In front of the Administration Block',1200.00,'Lawn','Large ceremonial lawn used for college events.','ACTIVE'),
('Herbal Garden','Behind the Science Block',350.50,'Herbal','Medicinal and culinary herbs maintained by the Botany department.','ACTIVE'),
('Rose Court','Between Library and Auditorium',220.00,'Flower','Ornamental rose beds and seasonal flowers.','ACTIVE'),
('Library Courtyard','Inside the Central Library',150.00,'Ornamental','Shaded courtyard with potted ornamental plants.','ACTIVE'),
('Hostel Vegetable Patch','Near Boys Hostel Block B',480.00,'Vegetable','Student-run organic vegetable patch.','UNDER_MAINTENANCE');

INSERT INTO plants (garden_id,plant_name,category,quantity,watering_frequency_days,water_requirement_liters,description) VALUES
(1,'Bermuda Grass','Grass',1,2,600.00,'Dense lawn grass; needs deep watering.'),
(1,'Neem Tree','Tree',12,7,20.00,'Drought-tolerant shade trees along the lawn border.'),
(2,'Tulsi (Holy Basil)','Herb',60,1,0.50,'Needs light daily watering.'),
(2,'Aloe Vera','Herb',45,5,0.40,'Succulent; avoid overwatering.'),
(2,'Mint','Herb',30,1,0.60,'Prefers consistently moist soil.'),
(3,'Hybrid Tea Rose','Flower',80,2,1.50,'Ornamental roses; water at the base.'),
(3,'Marigold','Flower',120,2,0.70,'Seasonal flowering border.'),
(3,'Hibiscus','Shrub',25,3,2.00,'Flowering shrubs.'),
(4,'Money Plant','Ornamental',40,4,0.50,'Indoor-style climbers in pots.'),
(4,'Areca Palm','Ornamental',18,3,3.00,'Potted palms.'),
(5,'Tomato','Vegetable',90,1,1.20,'Staked tomato rows.'),
(5,'Brinjal','Vegetable',70,2,1.50,'Eggplant rows.'),
(5,'Spinach','Vegetable',200,1,0.30,'Leafy greens, shallow roots.');

INSERT INTO irrigation_schedules (garden_id,scheduled_at,estimated_water_liters,assigned_user_id,status,created_by) VALUES
(1,DATE_SUB(DATE_ADD(CURDATE(),INTERVAL 6 HOUR),INTERVAL 58 DAY),568.3,2,'COMPLETED',1),
(2,DATE_SUB(DATE_ADD(CURDATE(),INTERVAL 6 HOUR),INTERVAL 55 DAY),62.7,3,'COMPLETED',1),
(3,DATE_SUB(DATE_ADD(CURDATE(),INTERVAL 6 HOUR),INTERVAL 51 DAY),212.3,2,'COMPLETED',1),
(4,DATE_SUB(DATE_ADD(CURDATE(),INTERVAL 6 HOUR),INTERVAL 48 DAY),95.4,3,'COMPLETED',1),
(1,DATE_SUB(DATE_ADD(CURDATE(),INTERVAL 6 HOUR),INTERVAL 44 DAY),516.7,2,'COMPLETED',1),
(2,DATE_SUB(DATE_ADD(CURDATE(),INTERVAL 6 HOUR),INTERVAL 41 DAY),52.3,3,'COMPLETED',1),
(3,DATE_SUB(DATE_ADD(CURDATE(),INTERVAL 6 HOUR),INTERVAL 37 DAY),205.2,2,'COMPLETED',1),
(5,DATE_SUB(DATE_ADD(CURDATE(),INTERVAL 6 HOUR),INTERVAL 34 DAY),266.1,2,'COMPLETED',1),
(1,DATE_SUB(DATE_ADD(CURDATE(),INTERVAL 6 HOUR),INTERVAL 30 DAY),622.9,2,'COMPLETED',1),
(2,DATE_SUB(DATE_ADD(CURDATE(),INTERVAL 6 HOUR),INTERVAL 27 DAY),61.4,3,'COMPLETED',1),
(3,DATE_SUB(DATE_ADD(CURDATE(),INTERVAL 6 HOUR),INTERVAL 24 DAY),240.0,2,'COMPLETED',1),
(4,DATE_SUB(DATE_ADD(CURDATE(),INTERVAL 6 HOUR),INTERVAL 20 DAY),121.8,3,'COMPLETED',1),
(1,DATE_SUB(DATE_ADD(CURDATE(),INTERVAL 6 HOUR),INTERVAL 17 DAY),536.0,2,'COMPLETED',1),
(2,DATE_SUB(DATE_ADD(CURDATE(),INTERVAL 6 HOUR),INTERVAL 14 DAY),56.6,3,'COMPLETED',1),
(3,DATE_SUB(DATE_ADD(CURDATE(),INTERVAL 6 HOUR),INTERVAL 11 DAY),189.9,2,'COMPLETED',1),
(5,DATE_SUB(DATE_ADD(CURDATE(),INTERVAL 6 HOUR),INTERVAL 8 DAY),312.5,2,'COMPLETED',1),
(2,DATE_SUB(DATE_ADD(CURDATE(),INTERVAL 6 HOUR),INTERVAL 5 DAY),60.9,3,'COMPLETED',1),
(3,DATE_SUB(DATE_ADD(CURDATE(),INTERVAL 6 HOUR),INTERVAL 2 DAY),182.3,2,'COMPLETED',1),
(3,DATE_ADD(DATE_ADD(CURDATE(),INTERVAL 6 HOUR),INTERVAL 1 DAY),215,2,'PENDING',1),
(2,DATE_ADD(DATE_ADD(CURDATE(),INTERVAL 6 HOUR),INTERVAL 1 DAY),60,3,'PENDING',1),
(1,DATE_ADD(DATE_ADD(CURDATE(),INTERVAL 6 HOUR),INTERVAL 2 DAY),600,2,'PENDING',1),
(4,DATE_ADD(DATE_ADD(CURDATE(),INTERVAL 6 HOUR),INTERVAL 3 DAY),110,3,'RESCHEDULED',1),
(5,DATE_ADD(DATE_ADD(CURDATE(),INTERVAL 6 HOUR),INTERVAL 4 DAY),300,2,'PENDING',1),
(3,DATE_ADD(DATE_ADD(CURDATE(),INTERVAL 6 HOUR),INTERVAL 0 DAY),210,3,'CANCELLED',1);

INSERT INTO irrigation_records (schedule_id,completed_at,estimated_water_used_liters,notes,recorded_by) VALUES
(1,DATE_SUB(DATE_ADD(CURDATE(),INTERVAL 7 HOUR),INTERVAL 58 DAY),548.5,'Watered as scheduled (estimated).',2),
(2,DATE_SUB(DATE_ADD(CURDATE(),INTERVAL 7 HOUR),INTERVAL 55 DAY),60.0,'Watered as scheduled (estimated).',3),
(3,DATE_SUB(DATE_ADD(CURDATE(),INTERVAL 7 HOUR),INTERVAL 51 DAY),209.4,'Watered as scheduled (estimated).',2),
(4,DATE_SUB(DATE_ADD(CURDATE(),INTERVAL 7 HOUR),INTERVAL 48 DAY),95.5,'Watered as scheduled (estimated).',3),
(5,DATE_SUB(DATE_ADD(CURDATE(),INTERVAL 7 HOUR),INTERVAL 44 DAY),513.3,'Watered as scheduled (estimated).',2),
(6,DATE_SUB(DATE_ADD(CURDATE(),INTERVAL 7 HOUR),INTERVAL 41 DAY),50.2,'Watered as scheduled (estimated).',3),
(7,DATE_SUB(DATE_ADD(CURDATE(),INTERVAL 7 HOUR),INTERVAL 37 DAY),211.9,'Watered as scheduled (estimated).',2),
(8,DATE_SUB(DATE_ADD(CURDATE(),INTERVAL 7 HOUR),INTERVAL 34 DAY),258.7,'Watered as scheduled (estimated).',2),
(9,DATE_SUB(DATE_ADD(CURDATE(),INTERVAL 7 HOUR),INTERVAL 30 DAY),650.8,'Watered as scheduled (estimated).',2),
(10,DATE_SUB(DATE_ADD(CURDATE(),INTERVAL 7 HOUR),INTERVAL 27 DAY),60.8,'Watered as scheduled (estimated).',3),
(11,DATE_SUB(DATE_ADD(CURDATE(),INTERVAL 7 HOUR),INTERVAL 24 DAY),229.1,'Watered as scheduled (estimated).',2),
(12,DATE_SUB(DATE_ADD(CURDATE(),INTERVAL 7 HOUR),INTERVAL 20 DAY),119.2,'Watered as scheduled (estimated).',3),
(13,DATE_SUB(DATE_ADD(CURDATE(),INTERVAL 7 HOUR),INTERVAL 17 DAY),515.5,'Watered as scheduled (estimated).',2),
(14,DATE_SUB(DATE_ADD(CURDATE(),INTERVAL 7 HOUR),INTERVAL 14 DAY),58.4,'Watered as scheduled (estimated).',3),
(15,DATE_SUB(DATE_ADD(CURDATE(),INTERVAL 7 HOUR),INTERVAL 11 DAY),191.4,'Watered as scheduled (estimated).',2),
(16,DATE_SUB(DATE_ADD(CURDATE(),INTERVAL 7 HOUR),INTERVAL 8 DAY),308.5,'Watered as scheduled (estimated).',2),
(17,DATE_SUB(DATE_ADD(CURDATE(),INTERVAL 7 HOUR),INTERVAL 5 DAY),58.2,'Watered as scheduled (estimated).',3),
(18,DATE_SUB(DATE_ADD(CURDATE(),INTERVAL 7 HOUR),INTERVAL 2 DAY),176.9,'Watered as scheduled (estimated).',2);

INSERT INTO maintenance_tasks (garden_id,assigned_user_id,title,description,priority,due_date,status,completed_at,created_by) VALUES
(1,2,'Mow the Main Lawn','Mow and collect clippings before the annual day.','HIGH',DATE_ADD(CURDATE(),INTERVAL 2 DAY),'IN_PROGRESS',NULL,1),
(2,3,'Prune mint and tulsi beds','Remove dry stems and flower buds.','MEDIUM',DATE_ADD(CURDATE(),INTERVAL 5 DAY),'PENDING',NULL,1),
(3,2,'Apply organic fertiliser to roses','Use vermicompost around each bush.','MEDIUM',DATE_SUB(CURDATE(),INTERVAL 2 DAY),'PENDING',NULL,1),
(4,3,'Repot overgrown money plants','Move to larger pots and refresh soil.','LOW',DATE_ADD(CURDATE(),INTERVAL 10 DAY),'PENDING',NULL,1),
(5,2,'Repair drip line (manual hose fitting)','Replace broken hose connectors in rows 3-5.','HIGH',DATE_SUB(CURDATE(),INTERVAL 1 DAY),'IN_PROGRESS',NULL,1),
(1,3,'Trim neem tree branches','Clear branches touching the compound wall.','LOW',DATE_SUB(CURDATE(),INTERVAL 6 DAY),'COMPLETED',DATE_SUB(NOW(),INTERVAL 6 DAY),1),
(2,2,'Weed the herbal garden','Manual weeding of all beds.','MEDIUM',DATE_SUB(CURDATE(),INTERVAL 12 DAY),'COMPLETED',DATE_SUB(NOW(),INTERVAL 12 DAY),1),
(3,3,'Replace marigold seedlings','Fill gaps in the border.','MEDIUM',DATE_ADD(CURDATE(),INTERVAL 1 DAY),'PENDING',NULL,1);

INSERT INTO notifications (user_id,message,notification_type,is_read) VALUES
(1,'Welcome to the Smart Campus Gardening system. Demo data has been loaded.','SYSTEM',0),
(1,'Maintenance task "Apply organic fertiliser to roses" is overdue.','MAINTENANCE',0),
(1,'Irrigation for Library Courtyard was rescheduled.','IRRIGATION',1),
(2,'New irrigation schedule assigned: Rose Court.','IRRIGATION',0),
(2,'You have been assigned the task "Mow the Main Lawn".','MAINTENANCE',0),
(3,'Irrigation for Library Courtyard was rescheduled.','IRRIGATION',0),
(3,'You have been assigned the task "Prune mint and tulsi beds".','MAINTENANCE',1);

INSERT INTO weather_records (campus_location,temperature_c,conditions,humidity,rain_mm_current,rain_mm_forecast_24h,forecast_summary,is_demo) VALUES
('Chennai,IN',31.0,'Partly cloudy (DEMO DATA)',68,0,1.2,'[{"time":"Demo +3h","temp":32,"conditions":"Sunny","rainMm":0},{"time":"Demo +6h","temp":30,"conditions":"Cloudy","rainMm":0.4},{"time":"Demo +9h","temp":28,"conditions":"Light rain","rainMm":0.8}]',1);
INSERT INTO app_settings (setting_key,setting_value) VALUES ('campus_location','Chennai,IN');
INSERT INTO activity_logs (user_id,activity_type,description) VALUES
(1,'GARDEN','Added garden "Hostel Vegetable Patch"'),
(1,'PLANT','Added 90 Tomato plants to Hostel Vegetable Patch'),
(1,'IRRIGATION','Created irrigation schedule for Rose Court'),
(2,'IRRIGATION','Completed irrigation for Herbal Garden (estimated 61.2 L)'),
(1,'MAINTENANCE','Created task "Mow the Main Lawn"'),
(3,'MAINTENANCE','Completed task "Trim neem tree branches"'),
(1,'USER','Created staff account for Meena Selvam'),
(1,'AUTH','Admin signed in');
