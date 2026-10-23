package com.example.util;

import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;

import javax.persistence.EntityManagerFactory;

/**
 * Affiche un résumé des statistiques Hibernate (requêtes, cache L2, cache de requêtes).
 */
public class RapportStatistiques {

    private final EntityManagerFactory emf;

    public RapportStatistiques(EntityManagerFactory emf) {
        this.emf = emf;
    }

    public void afficher() {
        Statistics s = emf.unwrap(SessionFactory.class).getStatistics();

        System.out.println("\n=========== RAPPORT STATISTIQUES HIBERNATE ===========");
        System.out.println("Requêtes SQL préparées        : " + s.getPrepareStatementCount());
        System.out.println("Requêtes HQL/JPQL exécutées   : " + s.getQueryExecutionCount());
        System.out.println("Requête la plus lente (ms)    : " + s.getQueryExecutionMaxTime());
        System.out.println("Entités chargées              : " + s.getEntityLoadCount());
        System.out.println("Entités insérées              : " + s.getEntityInsertCount());
        System.out.println("Transactions                  : " + s.getTransactionCount());
        System.out.println("Cache L2 - hits               : " + s.getSecondLevelCacheHitCount());
        System.out.println("Cache L2 - miss               : " + s.getSecondLevelCacheMissCount());
        System.out.println("Cache L2 - puts               : " + s.getSecondLevelCachePutCount());
        System.out.println("Cache de requêtes - hits      : " + s.getQueryCacheHitCount());
        System.out.println("Cache de requêtes - miss      : " + s.getQueryCacheMissCount());
        System.out.println("======================================================\n");
    }
}
