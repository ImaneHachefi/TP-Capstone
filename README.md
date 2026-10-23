# TP – Application de réservation d'espaces (JPA / Hibernate)

## Objectif
Application Java de réservation de salles avec :
- modèle JPA (Membre, Espace, Creneau) et verrouillage optimiste `@Version`
- cache de second niveau Hibernate (JCache + Ehcache 3)
- jeu de données, script de migration SQL exécuté via JDBC
- requêtes JPQL et Criteria API
- gestion des transactions, des conflits de créneaux et des logs
- rapport de statistiques Hibernate
- tests unitaires JUnit 5

## Technologies
Java 17, Maven, Hibernate 5.4.32.Final, Ehcache 3.8.1, PostgreSQL 16 (Docker),
JUnit 5, H2 (tests uniquement), DBeaver.

## Structure
```
reservation-salles-lab
├── docker-compose.yml
├── pom.xml
└── src
    ├── main
    │   ├── java/com/example
    │   │   ├── ApplicationPrincipale.java
    │   │   ├── model/      Membre, Espace, Creneau
    │   │   ├── service/    GestionReservations, ConflitReservationException
    │   │   └── util/       AmorceDonnees, ExecuteurMigration, RapportStatistiques
    │   └── resources
    │       ├── META-INF/persistence.xml
    │       ├── ehcache.xml
    │       └── db/migration/v2_0_ajout_description.sql
    └── test
        ├── java/com/example    BaseTestPersistance, ModelePersistanceTest, GestionReservationsTest
        └── resources/META-INF/persistence.xml   (H2)
```

## Modèle de données
- `Membre` (email unique, nom, version) — mis en cache L2
- `Espace` (nom, capacité, description, version) — mis en cache L2
- `Creneau` (espace, membre, dateDebut, dateFin, version) — relations `@ManyToOne(LAZY)`

<img width="1684" height="631" alt="Capture d&#39;écran 2026-10-23 132830" src="https://github.com/user-attachments/assets/4d23d21f-08dc-48b1-b0be-7466afc2b0b0" />
<img width="1438" height="526" alt="Capture d&#39;écran 2026-10-23 132841" src="https://github.com/user-attachments/assets/cceee69b-b69e-42dc-9c70-3706f3dfe4c9" />
<img width="1416" height="689" alt="Capture d&#39;écran 2026-10-23 132855" src="https://github.com/user-attachments/assets/a84465f6-75da-41e1-924d-ad53e4a70748" />

## Lancement

### 1. Base de données
```bash
docker compose up -d
docker ps
```
<img width="1919" height="706" alt="Capture d&#39;écran 2026-10-23 132307" src="https://github.com/user-attachments/assets/792abf02-450b-43bb-b4d6-6ebb8047b903" />

### 2. Connexion DBeaver
| Paramètre | Valeur |
|---|---|
| Host | localhost |
| Port | 5432 |
| Database | sallesdb |
| Username | salles |
| Password | salles |

<img width="804" height="1079" alt="Capture d&#39;écran 2026-10-23 132817" src="https://github.com/user-attachments/assets/419484b4-c7ce-4d76-a6f9-548db29ee692" />

### 3. Exécution de l'application
```bash
mvn clean compile exec:java
```
<img width="1784" height="876" alt="Capture d&#39;écran 2026-10-23 132533" src="https://github.com/user-attachments/assets/fb92ee36-87e6-424e-86f7-645ba841eddd" />
<img width="1490" height="924" alt="Capture d&#39;écran 2026-10-23 132549" src="https://github.com/user-attachments/assets/ac64bc5b-eed1-4848-ba8c-aa706d49631a" />
<img width="1244" height="624" alt="Capture d&#39;écran 2026-10-23 132602" src="https://github.com/user-attachments/assets/6ead2184-575f-431b-b72b-11040ee99fea" />
<img width="953" height="774" alt="Capture d&#39;écran 2026-10-23 132630" src="https://github.com/user-attachments/assets/b09a7f17-bd28-4ab9-b67d-ebd24085f08a" />
<img width="1105" height="796" alt="Capture d&#39;écran 2026-10-23 132656" src="https://github.com/user-attachments/assets/51333f2f-f7a3-4681-9df7-d71e19c236fa" />
<img width="1076" height="784" alt="Capture d&#39;écran 2026-10-23 132725" src="https://github.com/user-attachments/assets/0045e48a-7a2b-429d-920e-dca0ec21a5ed" />
<img width="1063" height="772" alt="Capture d&#39;écran 2026-10-23 132752" src="https://github.com/user-attachments/assets/3c32dd5d-188f-4a81-ae14-c332aba3e4bc" />
<img width="1040" height="755" alt="Capture d&#39;écran 2026-10-23 132759" src="https://github.com/user-attachments/assets/47e0f978-0cf4-4a13-be50-21f1d814b357" />

### 4. Vérification dans DBeaver
```sql
SELECT * FROM membres;
SELECT * FROM espaces;       -- la colonne description vient de la migration
SELECT * FROM creneaux;
```
<img width="901" height="741" alt="image" src="https://github.com/user-attachments/assets/9838b74e-9799-405d-ab25-ef693b61c9c6" />
<img width="1109" height="871" alt="image" src="https://github.com/user-attachments/assets/8b5dab65-dc8f-4382-9feb-14d81d9ed85d" />
<img width="1022" height="994" alt="image" src="https://github.com/user-attachments/assets/d116a419-aa42-45a0-8939-828f77778438" />

### 5. Tests unitaires (sans Docker, base H2 en mémoire)
```bash
mvn test
```
<img width="1780" height="803" alt="image" src="https://github.com/user-attachments/assets/c618bd4c-a1cd-489a-b1ba-4da155330456" />
<img width="1826" height="479" alt="image" src="https://github.com/user-attachments/assets/c3c2f32b-80ed-4d34-9ab2-02448039fe9d" />

## Tests réalisés
| Test | Vérifie |
|---|---|
| persisterMembre_attribueUnId | Génération de l'id et version initiale |
| emailDuplique_leveUneException | Contrainte d'unicité sur l'email |
| verrouillageOptimiste_detecteLeConflit | `OptimisticLockException` sur modification concurrente |
| chevauchement_leveUnConflit | Refus d'un créneau qui chevauche |
| creneauxConsecutifs_sontAutorises | Fin = début suivant accepté |
| datesInvalides_sontRejetees | Début après fin refusé |
| jpqlEtCriteria_donnentLeMemeResultat | Équivalence JPQL / Criteria |

## Migration
`v2_0_ajout_description.sql` : ajoute la colonne `description` à `espaces`, crée l'index
`idx_creneaux_espace_debut` et renseigne une valeur par défaut. Le script est rejouable (`IF NOT EXISTS`).

## Résultats

<img width="1040" height="755" alt="Capture d&#39;écran 2026-10-23 132759" src="https://github.com/user-attachments/assets/477224af-0091-4048-8801-4af94365f1bd" />

## Analyse
- **Conflits** : une requête `COUNT` détecte les chevauchements (`debut < fin_existante AND fin > debut_existant`). Un verrou pessimiste sur l'espace évite les réservations simultanées.
- **Cache L2** : la 1ʳᵉ lecture de l'espace 1 produit un miss + put, la 2ᵉ (autre `EntityManager`) un hit sans requête SQL.
- **JOIN FETCH** dans les recherches JPQL/Criteria évite le problème N+1 sur `espace` et `membre`.
- **@Version** : une modification concurrente lève `OptimisticLockException` au lieu d'écraser silencieusement les données.

## Arrêt
```bash
docker compose down        # garde les données
docker compose down -v     # supprime aussi le volume
```
