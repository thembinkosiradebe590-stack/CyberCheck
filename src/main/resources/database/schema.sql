PRAGMA foreign_keys = ON;

CREATE TABLE IF NOT EXISTS monitored_folders (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id INTEGER NOT NULL,
    folder_path TEXT NOT NULL UNIQUE,
    active INTEGER NOT NULL DEFAULT 1
        CHECK (active IN (0, 1)),
    created_at TEXT NOT NULL
);

CREATE TABLE IF NOT EXISTS file_baselines (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    monitored_folder_id INTEGER NOT NULL,
    file_path TEXT NOT NULL,
    sha256_hash TEXT NOT NULL,
    file_size INTEGER NOT NULL
        CHECK (file_size >= 0),
    last_modified_at TEXT NOT NULL,
    captured_at TEXT NOT NULL,

    FOREIGN KEY (monitored_folder_id)
        REFERENCES monitored_folders(id)
        ON DELETE CASCADE,

    UNIQUE (monitored_folder_id, file_path)
);

CREATE TABLE IF NOT EXISTS security_events (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    monitored_folder_id INTEGER NOT NULL,
    file_path TEXT NOT NULL,
    event_type TEXT NOT NULL
        CHECK (
            event_type IN (
                'FILE_CREATED',
                'FILE_MODIFIED',
                'FILE_DELETED'
            )
        ),
    severity TEXT NOT NULL
        CHECK (
            severity IN (
                'LOW',
                'MEDIUM',
                'HIGH'
            )
        ),
    description TEXT NOT NULL,
    occurred_at TEXT NOT NULL,
    status TEXT NOT NULL
        CHECK (
            status IN (
                'OPEN',
                'ACKNOWLEDGED',
                'RESOLVED'
            )
        ),

    FOREIGN KEY (monitored_folder_id)
        REFERENCES monitored_folders(id)
        ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_baselines_folder
    ON file_baselines(monitored_folder_id);

CREATE INDEX IF NOT EXISTS idx_events_folder
    ON security_events(monitored_folder_id);

CREATE INDEX IF NOT EXISTS idx_events_status
    ON security_events(status);