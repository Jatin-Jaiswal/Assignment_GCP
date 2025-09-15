-- Create items table
CREATE TABLE items (
    id VARCHAR(255) PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create index on name for better query performance
CREATE INDEX idx_items_name ON items(name);

-- Create index on created_at for sorting
CREATE INDEX idx_items_created_at ON items(created_at);
