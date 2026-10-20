-- V15: Fix team logo URLs that are stored as bare relative paths.
--
-- Before this fix, TeamServiceImpl stored the raw relative path returned by
-- storageService.store() (e.g. "teams/<id>/logo/<uuid>.jpg") directly in the
-- logo_url column, and mapToResponse() prepended "/storage/" at read time,
-- producing a frontend-relative URL (e.g. http://localhost:5173/storage/teams/...)
-- instead of a backend-absolute URL (e.g. http://localhost:8082/storage/teams/...).
--
-- This migration rewrites:
--   1. Rows that are bare relative paths (no http:// prefix) →
--      prepend the configured base URL + /storage/
--   2. Rows that are already absolute URLs but missing /storage/ are left alone
--      (none expected, but the WHERE guards against it).
--
-- NOTE: The base URL value here must match app.base-url in application.properties.
-- Update the SET clause below if your base URL differs in any environment.

UPDATE teams
SET logo_url = CONCAT('http://localhost:8082/storage/', logo_url)
WHERE logo_url IS NOT NULL
  AND logo_url NOT REGEXP '^https?://';
