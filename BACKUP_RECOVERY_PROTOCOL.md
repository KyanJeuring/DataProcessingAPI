# FleetMaster Backup & Recovery Protocol

## Overview

This document outlines the backup and recovery strategy for the FleetMaster Logistics database system. The strategy is designed to minimize data loss and ensure business continuity.

## Backup Strategy

### Automated Daily Backups

**Schedule:** Daily at 2:00 AM (Server Time)

**Implementation:** 
- Automated via Spring Boot `@Scheduled` annotation in `BackupService.java`
- Uses PostgreSQL's `pg_dump` utility
- Backup files stored in `/tmp/` with timestamp naming: `backup_YYYYMMDD_HHmmss.sql`

**Backup Command:**
```bash
pg_dump -h postgres -U ${POSTGRES_USER} --no-password -f /tmp/backup_YYYYMMDD_HHmmss.sql dataprocessingapi_db
```

### Backup Types

#### 1. **Full Database Backup** (Daily)
- Complete schema and data export
- Includes all tables, views, stored procedures, triggers
- Size estimate: ~50-500 MB depending on data volume
- Retention: 30 days

#### 2. **Transaction Log Backup** (Recommended for production)
For production environments, enable PostgreSQL WAL (Write-Ahead Logging):
```sql
-- Enable WAL archiving in postgresql.conf
wal_level = replica
archive_mode = on
archive_command = 'cp %p /backup/wal/%f'
```

### Backup Verification

**Automated Verification:**
```bash
# Test backup integrity
pg_restore --list /tmp/backup_20260115_020000.sql
```

**Manual Verification (Weekly):**
1. Restore backup to test database
2. Run integrity checks
3. Verify critical data present

## Recovery Strategy

### Recovery Time Objective (RTO)
**Target:** 2 hours maximum downtime

### Recovery Point Objective (RPO)
**Target:** Maximum 24 hours of data loss (1 daily backup cycle)

### Recovery Procedures

#### Scenario 1: Complete Database Loss

**Steps:**
```bash
# 1. Stop the application
docker compose down

# 2. Remove existing database volume
docker volume rm dataprocessingapi_postgres

# 3. Restart PostgreSQL
docker compose up -d postgres

# 4. Restore from backup
docker exec -i dataprocessingapi_db psql -U ${POSTGRES_USER} -d dataprocessingapi_db < /tmp/backup_20260115_020000.sql

# 5. Verify restoration
docker exec dataprocessingapi_db psql -U ${POSTGRES_USER} -d dataprocessingapi_db -c "SELECT COUNT(*) FROM company;"

# 6. Restart application
docker compose up -d
```

**Estimated Time:** 30-60 minutes

#### Scenario 2: Corrupted Table

**Steps:**
```bash
# 1. Create backup of current state
pg_dump -h postgres -U ${POSTGRES_USER} -d dataprocessingapi_db -f /tmp/pre_recovery_backup.sql

# 2. Drop corrupted table
psql -U ${POSTGRES_USER} -d dataprocessingapi_db -c "DROP TABLE IF EXISTS table_name CASCADE;"

# 3. Restore specific table from backup
pg_restore -h postgres -U ${POSTGRES_USER} -d dataprocessingapi_db -t table_name /tmp/backup_20260115_020000.sql

# 4. Verify data integrity
psql -U ${POSTGRES_USER} -d dataprocessingapi_db -c "SELECT COUNT(*) FROM table_name;"
```

**Estimated Time:** 10-20 minutes

#### Scenario 3: Accidental Data Deletion

**Steps:**
```bash
# 1. Identify the affected data and time of deletion
# 2. Restore to temporary database
psql -U ${POSTGRES_USER} -c "CREATE DATABASE temp_restore;"
pg_restore -h postgres -U ${POSTGRES_USER} -d temp_restore /tmp/backup_20260115_020000.sql

# 3. Extract specific records
psql -U ${POSTGRES_USER} -d temp_restore -c "COPY (SELECT * FROM company WHERE id IN (1,2,3)) TO '/tmp/recovery_data.csv' WITH CSV HEADER;"

# 4. Import to production database
psql -U ${POSTGRES_USER} -d dataprocessingapi_db -c "COPY company FROM '/tmp/recovery_data.csv' WITH CSV HEADER;"

# 5. Cleanup
psql -U ${POSTGRES_USER} -c "DROP DATABASE temp_restore;"
```

**Estimated Time:** 15-30 minutes

## Disaster Recovery

### High Availability Setup (Production Recommendation)

**Primary-Replica Configuration:**
```yaml
# docker-compose.prod.yml
services:
  postgres-primary:
    image: postgres:latest
    environment:
      - POSTGRES_REPLICATION_MODE=master
  
  postgres-replica:
    image: postgres:latest
    environment:
      - POSTGRES_REPLICATION_MODE=slave
      - POSTGRES_MASTER_HOST=postgres-primary
```

### Off-Site Backup Storage

**Recommended Providers:**
- AWS S3
- Azure Blob Storage
- Google Cloud Storage

**Implementation:**
```bash
# Upload to S3
aws s3 cp /tmp/backup_20260115_020000.sql s3://fleetmaster-backups/daily/

# Download from S3
aws s3 cp s3://fleetmaster-backups/daily/backup_20260115_020000.sql /tmp/
```

## Monitoring & Alerts

### Backup Monitoring

**Check Backup Success:**
```bash
# View backup logs
docker compose logs backend | grep "Backup"

# Expected output:
# "Backup successful: /tmp/backup_20260115_020000.sql"
```

**Alert Conditions:**
- Backup fails 2 consecutive times
- Backup file size < 1 MB (indicates incomplete backup)
- No backup file created in 25 hours

### Database Health Checks

```sql
-- Check database size
SELECT pg_size_pretty(pg_database_size('dataprocessingapi_db'));

-- Check table row counts
SELECT 
    schemaname,
    tablename,
    n_live_tup as row_count
FROM pg_stat_user_tables
ORDER BY n_live_tup DESC;

-- Check last backup timestamp
SELECT current_timestamp - pg_stat_file('/tmp/backup_latest.sql').modification AS time_since_backup;
```

## Backup Retention Policy

| Backup Type | Retention Period | Storage Location |
|-------------|------------------|------------------|
| Daily Full | 30 days | Local + S3 |
| Weekly Full | 12 weeks | S3 |
| Monthly Full | 12 months | S3 Glacier |
| Annual Full | 7 years | S3 Glacier Deep Archive |

## Testing Schedule

| Test Type | Frequency | Responsibility |
|-----------|-----------|----------------|
| Backup Integrity Check | Weekly | DevOps Team |
| Full Recovery Test | Monthly | Database Admin |
| Disaster Recovery Drill | Quarterly | IT Management |

## Emergency Contacts

| Role | Name | Contact |
|------|------|---------|
| Database Administrator | [Name] | [Email/Phone] |
| DevOps Lead | [Name] | [Email/Phone] |
| IT Manager | [Name] | [Email/Phone] |

## Recovery Documentation

### Post-Recovery Checklist

- [ ] Verify all tables restored
- [ ] Check row counts match expected values
- [ ] Test application connectivity
- [ ] Verify user authentication works
- [ ] Run smoke tests on critical features
- [ ] Check data integrity (foreign keys, constraints)
- [ ] Review application logs for errors
- [ ] Notify stakeholders of completion

### Lessons Learned Template

After each recovery incident, document:
1. **Incident Description:** What happened?
2. **Root Cause:** Why did it happen?
3. **Recovery Steps Taken:** What was done?
4. **Time to Recover:** How long did it take?
5. **Data Loss:** Was any data lost?
6. **Improvements:** What can be improved?

## Version History

| Version | Date | Changes | Author |
|---------|------|---------|--------|
| 1.0 | 2026-01-15 | Initial protocol | FleetMaster Team |

## References

- PostgreSQL Official Documentation: https://www.postgresql.org/docs/current/backup.html
- Docker Volume Backup Guide: https://docs.docker.com/storage/volumes/#backup-restore-or-migrate-data-volumes
- BackupService Implementation: `/backend/src/main/java/com/fleetmaster/services/BackupService.java`
