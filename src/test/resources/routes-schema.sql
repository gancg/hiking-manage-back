PRAGMA foreign_keys = OFF;

DROP TABLE IF EXISTS trip_feedback;

DROP TABLE IF EXISTS traffic_profiles;

DROP TABLE IF EXISTS route_parking_points;

DROP TABLE IF EXISTS transport_cost_items;

DROP TABLE IF EXISTS route_cost_items;

DROP TABLE IF EXISTS routes;

CREATE TABLE routes (
    id TEXT PRIMARY KEY,
    name TEXT NOT NULL,
    start_location TEXT NOT NULL,
    end_location TEXT NOT NULL,
    latitude REAL,
    longitude REAL,
    distance_km REAL NOT NULL CHECK(distance_km > 0),
    ascent_m INTEGER NOT NULL CHECK(ascent_m >= 0),
    highest_altitude_m INTEGER NOT NULL CHECK(highest_altitude_m >= 0),
    hiking_minutes INTEGER NOT NULL CHECK(hiking_minutes > 0),
    difficulty TEXT NOT NULL CHECK(difficulty IN ('easy','moderate','hard','expert')),
    duration_days INTEGER NOT NULL CHECK(duration_days > 0),
    route_type TEXT NOT NULL,
    best_seasons_json TEXT NOT NULL,
    scenery_json TEXT NOT NULL,
    risks_json TEXT NOT NULL,
    transport_modes_json TEXT NOT NULL,
    cost_min_cny REAL NOT NULL,
    cost_max_cny REAL NOT NULL,
    parking TEXT,
    supplies TEXT,
    signal TEXT,
    camping TEXT,
    source_url TEXT NOT NULL,
    source_name TEXT NOT NULL,
    collected_at TEXT NOT NULL,
    updated_at TEXT NOT NULL,
    confidence REAL NOT NULL CHECK(confidence >= 0 AND confidence <= 1),
    reviewed INTEGER NOT NULL DEFAULT 0 CHECK(reviewed IN (0,1))
, has_toilet INTEGER NOT NULL DEFAULT 0 CHECK(has_toilet IN (0,1)), has_supply_shop INTEGER NOT NULL DEFAULT 0 CHECK(has_supply_shop IN (0,1)), is_traverse INTEGER NOT NULL DEFAULT 0 CHECK(is_traverse IN (0,1)), traverse_transfer_minutes INTEGER NOT NULL DEFAULT 0 CHECK(traverse_transfer_minutes >= 0), group_tour_search_terms_json TEXT NOT NULL DEFAULT '[]');

CREATE TABLE route_cost_items (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    route_id TEXT NOT NULL REFERENCES routes(id) ON DELETE CASCADE,
    name TEXT NOT NULL,
    cost_type TEXT NOT NULL CHECK(cost_type IN ('ticket','shuttle','waste','parking','other')),
    billing_unit TEXT NOT NULL CHECK(billing_unit IN ('person','vehicle','group')),
    min_cny REAL NOT NULL CHECK(min_cny >= 0),
    max_cny REAL NOT NULL CHECK(max_cny >= min_cny),
    source_url TEXT NOT NULL,
    updated_at TEXT NOT NULL
);

CREATE TABLE transport_cost_items (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    route_id TEXT NOT NULL REFERENCES routes(id) ON DELETE CASCADE,
    transport_mode TEXT NOT NULL CHECK(transport_mode IN ('self_drive','public_transit','carpool','group_tour')),
    name TEXT NOT NULL,
    cost_type TEXT NOT NULL CHECK(cost_type IN ('fuel','toll','train','bus','other')),
    billing_unit TEXT NOT NULL CHECK(billing_unit IN ('person','vehicle','group')),
    min_cny REAL NOT NULL CHECK(min_cny >= 0),
    max_cny REAL NOT NULL CHECK(max_cny >= min_cny),
    source_url TEXT NOT NULL,
    updated_at TEXT NOT NULL
);

CREATE TABLE route_parking_points (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    route_id TEXT NOT NULL REFERENCES routes(id) ON DELETE CASCADE,
    name TEXT NOT NULL,
    latitude REAL NOT NULL CHECK(latitude >= -90 AND latitude <= 90),
    longitude REAL NOT NULL CHECK(longitude >= -180 AND longitude <= 180),
    note TEXT,
    is_recommended INTEGER NOT NULL DEFAULT 0 CHECK(is_recommended IN (0,1)),
    is_reviewed INTEGER NOT NULL DEFAULT 0 CHECK(is_reviewed IN (0,1)),
    source_url TEXT NOT NULL,
    updated_at TEXT NOT NULL,
    UNIQUE(route_id, name)
);

CREATE TABLE traffic_profiles (
    route_id TEXT PRIMARY KEY REFERENCES routes(id) ON DELETE CASCADE,
    base_one_way_minutes INTEGER NOT NULL,
    weekday_extra_min INTEGER NOT NULL DEFAULT 0,
    weekday_extra_max INTEGER NOT NULL DEFAULT 0,
    weekend_extra_min INTEGER NOT NULL DEFAULT 0,
    weekend_extra_max INTEGER NOT NULL DEFAULT 0,
    holiday_extra_min INTEGER NOT NULL DEFAULT 0,
    holiday_extra_max INTEGER NOT NULL DEFAULT 0,
    morning_extra_minutes INTEGER NOT NULL DEFAULT 0,
    evening_extra_minutes INTEGER NOT NULL DEFAULT 0,
    common_bottlenecks_json TEXT NOT NULL,
    best_departure_time TEXT,
    suggested_return_time TEXT,
    source_url TEXT NOT NULL,
    updated_at TEXT NOT NULL,
    confidence REAL NOT NULL CHECK(confidence >= 0 AND confidence <= 1)
);

CREATE TABLE trip_feedback (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    route_id TEXT NOT NULL REFERENCES routes(id) ON DELETE CASCADE,
    traveled_at TEXT NOT NULL,
    direction TEXT NOT NULL CHECK(direction IN ('outbound','return')),
    actual_minutes INTEGER NOT NULL CHECK(actual_minutes > 0),
    congestion_level TEXT NOT NULL CHECK(congestion_level IN ('low','medium','high','severe')),
    source TEXT NOT NULL,
    notes TEXT,
    created_at TEXT NOT NULL
);

PRAGMA foreign_keys = ON;
