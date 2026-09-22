-- liquibase formatted sql

-- changeset staynest:V013-remove-default-seeded-admin
-- Removes the well-known seeded SUPER_ADMIN (admin@staynest.com) created by V012.
-- Reason: the V012 seed contains a publicly known default credential, which must
-- not exist on fresh (e.g. Supabase) deployments. Admin accounts must now be
-- bootstrapped via the ADMIN_EMAIL / ADMIN_PASSWORD environment variables
-- handled by DataInitializer (opt-in only, no default password).
-- Existing environments that relied on the default local admin should set
-- ADMIN_EMAIL / ADMIN_PASSWORD before upgrading, then the account is recreated
-- securely by the application on startup.
DELETE FROM users WHERE email = 'admin@staynest.com';
