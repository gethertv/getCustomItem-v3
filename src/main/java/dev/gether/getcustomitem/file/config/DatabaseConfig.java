package dev.gether.getcustomitem.file.config;

import dev.gether.getcustomitem.storage.DatabaseType;
import dev.gether.getutils.GetConfig;
import dev.gether.getutils.annotation.Comment;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class DatabaseConfig extends GetConfig {

    @Comment("MYSQL, SQLITE")
    private DatabaseType databaseType = DatabaseType.SQLITE;
    private String host = "localhost";

    private String username = "user";

    private String password = "pass";

    private String database = "database_name";

    private String port = "3306";

    private boolean ssl = false;
}
