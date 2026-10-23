package com.example.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.stream.Collectors;

/**
 * Lit un script SQL du classpath et l'exécute via JDBC.
 */
public class ExecuteurMigration {

    private final String urlJdbc;
    private final String utilisateur;
    private final String motDePasse;

    public ExecuteurMigration(String urlJdbc, String utilisateur, String motDePasse) {
        this.urlJdbc = urlJdbc;
        this.utilisateur = utilisateur;
        this.motDePasse = motDePasse;
    }

    public void executer(String cheminScript) throws IOException, SQLException {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream(cheminScript)) {
            if (is == null) {
                throw new IOException("Script introuvable dans le classpath : " + cheminScript);
            }

            String sql;
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
                sql = reader.lines().collect(Collectors.joining("\n"));
            }

            try (Connection conn = DriverManager.getConnection(urlJdbc, utilisateur, motDePasse);
                 Statement stmt = conn.createStatement()) {
                conn.setAutoCommit(false);
                try {
                    stmt.execute(sql);
                    conn.commit();
                } catch (SQLException e) {
                    conn.rollback();
                    throw e;
                }
            }
        }
        System.out.println(">>> Migration exécutée : " + cheminScript);
    }
}
