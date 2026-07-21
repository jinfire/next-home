ALTER TABLE apartment ADD COLUMN road_address VARCHAR(300);

UPDATE apartment a
SET road_address = cache.road_address
FROM geocoding_cache cache
WHERE cache.normalized_address = REGEXP_REPLACE(TRIM(a.address), '\\s+', ' ', 'g')
  AND cache.road_address IS NOT NULL
  AND cache.road_address <> '';
