package com.augustana.golf.config;

import java.time.OffsetDateTime;

import javax.sql.DataSource;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.azure.core.credential.AccessToken;
import com.azure.core.credential.TokenRequestContext;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.microsoft.sqlserver.jdbc.SQLServerDataSource;

/**
 * Production data source that authenticates to Azure SQL with Microsoft Entra
 * credentials instead of a database username and password.
 */
@Configuration
@org.springframework.context.annotation.Profile("!test & !local")
public class AzureSqlEntraDataSourceConfig {

    private static final String SCOPE = "https://database.windows.net//.default";

    @Bean
    public DataSource dataSource() {
        var credential = new DefaultAzureCredentialBuilder().build();

        AccessToken token = credential
                .getToken(new TokenRequestContext().addScopes(SCOPE))
                .block();

        if (token == null || token.getExpiresAt().isBefore(OffsetDateTime.now())) {
            throw new IllegalStateException("Failed to acquire access token for Azure SQL.");
        }

        SQLServerDataSource ds = new SQLServerDataSource();
        ds.setServerName("golf-sql-server.database.windows.net");
        ds.setDatabaseName("golfdb");
        ds.setHostNameInCertificate("*.database.windows.net");
        ds.setLoginTimeout(30);

        ds.setAccessToken(token.getToken());

        return ds;
    }
}
