CREATE TABLE phones (
                        id UUID DEFAULT RANDOM_UUID() PRIMARY KEY,
                        number VARCHAR(50),
                        city_code VARCHAR(10),
                        contry_code VARCHAR(10),
                        user_id UUID,
                        CONSTRAINT fk_phone_user
                            FOREIGN KEY (user_id) REFERENCES users(id)
);