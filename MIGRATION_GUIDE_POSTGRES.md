# Guide de Migration MySQL vers PostgreSQL

## Problèmes Résolus

### 1. Type TINYINT n'existe pas dans PostgreSQL
**Solution:** Suppression de `columnDefinition = "TINYINT(1) DEFAULT 0"` dans les entités. Hibernate utilise maintenant automatiquement le type approprié selon le dialecte :
- MySQL : TINYINT(1)
- PostgreSQL : BOOLEAN

**Fichiers modifiés:**
- `User.java` : Champ `two_factor_enabled`
- `Customer.java` : Champ `two_factor_enabled`

### 2. "user" est un mot réservé dans PostgreSQL
**Solution:** La table a été renommée de `user` à `users`

**Fichier modifié:**
- `User.java` : `@Table(name = "users")`

## Migration des Données

Si vous avez des données existantes dans MySQL et que vous voulez les migrer vers PostgreSQL, suivez ces étapes :

### Option 1 : Hibernate gère tout (Recommandé)
Avec `spring.jpa.hibernate.ddl-auto=update`, Hibernate créera automatiquement les tables avec la structure correcte pour PostgreSQL.

**Étapes:**
1. Exportez vos données MySQL :
   ```sql
   mysqldump -u username -p database_name > backup.sql
   ```

2. Modifiez le fichier SQL exporté :
   - Remplacez `CREATE TABLE user` par `CREATE TABLE users`
   - Remplacez `TINYINT(1)` par `BOOLEAN`
   - Remplacez `AUTO_INCREMENT` par `SERIAL`
   - Ajustez les types de données selon PostgreSQL

3. Importez dans PostgreSQL :
   ```bash
   psql -U username -d database_name -f modified_backup.sql
   ```

### Option 2 : Nouvelle base de données
Si vous démarrez avec une nouvelle base PostgreSQL, aucune action supplémentaire n'est nécessaire. Hibernate créera automatiquement les tables.

## Vérification après Migration

Après avoir démarré l'application avec PostgreSQL :

1. Vérifiez que toutes les tables sont créées :
   ```sql
   \dt
   ```

2. Vérifiez la structure de la table users :
   ```sql
   \d users
   ```

3. Vérifiez le type du champ two_factor_enabled (doit être BOOLEAN) :
   ```sql
   SELECT column_name, data_type 
   FROM information_schema.columns 
   WHERE table_name = 'users' AND column_name = 'two_factor_enabled';
   ```

## Notes Importantes

- Les fichiers de migration SQL dans `src/main/resources/db/migration/` sont spécifiques à MySQL et ne sont pas utilisés (Liquibase désactivé)
- Assurez-vous que votre profil actif est `prod` avec `ACTIVE_PROFILE=prod`
- Vérifiez que les variables d'environnement PostgreSQL sont correctement configurées :
  - `DB_URL_POSTGRES`
  - `DB_USERNAME_POSTGRES`
  - `DB_PASSWORD_POSTGRES`

## Compatibilité Multi-Base de Données

Les entités ont été modifiées pour être compatibles avec MySQL et PostgreSQL sans utiliser de `columnDefinition` spécifique. Hibernate sélectionne automatiquement le type de colonne approprié en fonction du dialecte configuré.
