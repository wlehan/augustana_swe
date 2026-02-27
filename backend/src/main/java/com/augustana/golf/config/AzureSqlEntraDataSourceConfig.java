package com.augustana.golf.config;

import com.azure.core.credential.AccessToken;
import com.azure.core.credential.TokenRequestContext;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.microsoft.sqlserver.jdbc.SQLServerDataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;
import java.time.OffsetDateTime;

@Configuration
@org.springframework.context.annotation.Profile("!test")
public class AzureSqlEntraDataSourceConfig {

    private static final String SCOPE = "https://database.windows.net//.default";

    @Bean
    public DataSource dataSource() {
        var credential = new DefaultAzureCredentialBuilder().build();

        // Get token for Azure SQL
        AccessToken token = credential
                .getToken(new TokenRequestContext().addScopes(SCOPE))
                .block();

        if (token == null || token.getExpiresAt().isBefore(OffsetDateTime.now())) {
            throw new IllegalStateException("Failed to acquire access token for Azure SQL.");
        }

        SQLServerDataSource ds = new SQLServerDataSource();
        ds.setServerName("golf-sql-server.database.windows.net");
        ds.setDatabaseName("golfdb");
        ds.setEncrypt(true);
        ds.setHostNameInCertificate("*.database.windows.net");
        ds.setLoginTimeout(30);

        // This is the key: Entra token instead of username/password
        ds.setAccessToken(token.getToken());

        return ds;
    }
}