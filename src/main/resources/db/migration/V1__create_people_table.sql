CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TABLE people (
                        id BIGSERIAL PRIMARY KEY,
                        uuid UUID NOT NULL UNIQUE DEFAULT gen_random_uuid(),
                        first_name VARCHAR(100) NOT NULL,
                        last_name VARCHAR(100) NOT NULL,
                        document_type VARCHAR(20),
                        document_number VARCHAR(50) NOT NULL UNIQUE,
                        phone VARCHAR(50),
                        address TEXT,
                        created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
                        synced_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_people_document ON people(document_number);
CREATE INDEX idx_people_uuid ON people(uuid);