CREATE TABLE users (
                       id UUID DEFAULT RANDOM_UUID() PRIMARY KEY,
                       name VARCHAR(255),
                       email VARCHAR(255) NOT NULL,
                       password VARCHAR(255),
                       created TIMESTAMP,
                       modified TIMESTAMP,
                       last_login TIMESTAMP,
                       token VARCHAR(500),
                       active BOOLEAN,
                       CONSTRAINT uk_users_email UNIQUE (email)
);