-- Migration v2.0 (PostgreSQL) - script idempotent : peut être rejoué sans erreur

-- 1. Ajout de la colonne "description" sur la table "espaces"
ALTER TABLE espaces ADD COLUMN IF NOT EXISTS description VARCHAR(255);

-- 2. Index pour accélérer la recherche de conflits de créneaux
CREATE INDEX IF NOT EXISTS idx_creneaux_espace_debut ON creneaux (espace_id, date_debut);

-- 3. Valeur par défaut pour les espaces existants
UPDATE espaces SET description = 'Aucune description' WHERE description IS NULL;
