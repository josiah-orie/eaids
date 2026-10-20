-- V14: Fix athlete profile/icon image URLs that are missing the /storage/ prefix.
--
-- Before the fix in AthleteServiceImpl, store() returned a relative path
-- (e.g. "athletes/<id>/profile/<uuid>.jpg") and that was saved directly,
-- which produced absolute URLs without the /storage/ segment
-- (e.g. "http://localhost:8082/athletes/.../...jpg").
-- WebMvcConfig serves files under /storage/**, so the correct URL must include it.
--
-- This migration rewrites every affected row in-place by inserting "/storage/"
-- immediately after the host:port prefix.
-- Rows that already contain "/storage/" are left untouched.

UPDATE athlete_profiles
SET profile_image_url = CONCAT(
    REGEXP_SUBSTR(profile_image_url, '^https?://[^/]+'),   -- e.g. http://localhost:8082
    '/storage/',
    REGEXP_SUBSTR(profile_image_url, '[^/]+/.*$')          -- e.g. athletes/...
)
WHERE profile_image_url IS NOT NULL
  AND profile_image_url REGEXP '^https?://'
  AND profile_image_url NOT REGEXP '^https?://[^/]+/storage/';

UPDATE athlete_profiles
SET icon_image_url = CONCAT(
    REGEXP_SUBSTR(icon_image_url, '^https?://[^/]+'),
    '/storage/',
    REGEXP_SUBSTR(icon_image_url, '[^/]+/.*$')
)
WHERE icon_image_url IS NOT NULL
  AND icon_image_url REGEXP '^https?://'
  AND icon_image_url NOT REGEXP '^https?://[^/]+/storage/';
