-- V2__created_at_sysdatetime.sql
-- Update created_at defaults from CURRENT_TIMESTAMP to SYSDATETIME() (SQL Server/Azure SQL)

-- USERS.created_at
DECLARE @df_users sysname;
DECLARE @sql nvarchar(max);

SELECT @df_users = dc.name
FROM sys.default_constraints dc
JOIN sys.columns c
  ON c.default_object_id = dc.object_id
JOIN sys.tables t
  ON t.object_id = c.object_id
JOIN sys.schemas s
  ON s.schema_id = t.schema_id
WHERE s.name = 'dbo'
  AND t.name = 'users'
  AND c.name = 'created_at';

IF @df_users IS NOT NULL
BEGIN
    SET @sql = N'ALTER TABLE dbo.users DROP CONSTRAINT ' + QUOTENAME(@df_users) + N';';
    EXEC sp_executesql @sql;
END;

ALTER TABLE dbo.users
ADD CONSTRAINT DF_users_created_at
DEFAULT SYSDATETIME() FOR created_at;


-- GAMES.created_at
DECLARE @df_games sysname;

SELECT @df_games = dc.name
FROM sys.default_constraints dc
JOIN sys.columns c
  ON c.default_object_id = dc.object_id
JOIN sys.tables t
  ON t.object_id = c.object_id
JOIN sys.schemas s
  ON s.schema_id = t.schema_id
WHERE s.name = 'dbo'
  AND t.name = 'games'
  AND c.name = 'created_at';

IF @df_games IS NOT NULL
BEGIN
    SET @sql = N'ALTER TABLE dbo.games DROP CONSTRAINT ' + QUOTENAME(@df_games) + N';';
    EXEC sp_executesql @sql;
END;

ALTER TABLE dbo.games
ADD CONSTRAINT DF_games_created_at
DEFAULT SYSDATETIME() FOR created_at;