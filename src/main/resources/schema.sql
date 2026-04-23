-- STO Character Data Table
CREATE TABLE IF NOT EXISTS sto_data (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT UNIQUE NOT NULL,
    dilithium INTEGER DEFAULT 0,
    credits INTEGER DEFAULT 0,
    recruitment_time DATETIME,
    convertion_time DATETIME,
    event_time DATETIME,
    updated_at DATETIME
);