# 🚀 Guide d'Installation - Système d'Emails avec Thymeleaf

## ⚠️ Problème : Dépendance Thymeleaf Manquante

Si vous voyez des erreurs comme :
```
java: package org.thymeleaf.spring6 does not exist
java: cannot find symbol - SpringResourceTemplateResolver
```

C'est parce que la dépendance Thymeleaf n'était pas dans le projet.

## ✅ Solution : Ajouter la Dépendance

### Étape 1 : Vérifier le pom.xml

La dépendance suivante a été ajoutée au `pom.xml` :

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-thymeleaf</artifactId>
</dependency>
```

### Étape 2 : Recharger les Dépendances Maven

#### Option A : Via l'IDE (Recommandé)

**IntelliJ IDEA :**
1. Clic droit sur le projet → Maven → Reload Project
2. Ou cliquez sur l'icône Maven (M) dans la barre latérale → Cliquez sur le bouton "Reload"

**Eclipse :**
1. Clic droit sur le projet → Maven → Update Project
2. Cochez "Force Update of Snapshots/Releases"
3. Cliquez sur OK

**VS Code :**
1. Ouvrez la palette de commandes (Ctrl+Shift+P)
2. Tapez "Java: Clean Java Language Server Workspace"
3. Puis "Maven: Update Project"

#### Option B : Via la Ligne de Commande

```bash
# Nettoyer et recompiler
mvn clean install

# Ou simplement télécharger les dépendances
mvn dependency:resolve
```

### Étape 3 : Vérifier l'Installation

Après le rechargement, vérifiez que les imports fonctionnent :

```java
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.spring6.templateresolver.SpringResourceTemplateResolver;
import org.thymeleaf.templatemode.TemplateMode;
```

Si ces imports ne montrent plus d'erreurs, c'est bon ! ✅

## 🔧 Compilation et Tests

### Compiler le Projet

```bash
# Nettoyer et compiler
mvn clean compile

# Compiler et lancer les tests
mvn clean test

# Créer le package JAR
mvn clean package
```

### Lancer l'Application

```bash
# Via Maven
mvn spring-boot:run

# Ou via le JAR
java -jar target/expedition-2.0.0.jar
```

## 📦 Dépendances Complètes du Système d'Emails

Le système d'emails nécessite les dépendances suivantes (toutes présentes maintenant) :

```xml
<!-- Email -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-mail</artifactId>
</dependency>

<!-- Thymeleaf pour les templates -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-thymeleaf</artifactId>
</dependency>
```

## 🎯 Vérification Rapide

### Test 1 : Compilation

```bash
mvn clean compile
```

**Résultat attendu :** `BUILD SUCCESS`

### Test 2 : Génération d'un Template

```java
@Autowired
private EmailTemplateService emailTemplateService;

@Test
void testTemplateGeneration() {
    String html = emailTemplateService.generateVerificationEmail(
        "Test User",
        "https://test.com/verify?token=abc123"
    );
    
    assertNotNull(html);
    assertTrue(html.contains("Test User"));
}
```

### Test 3 : Démarrage de l'Application

```bash
mvn spring-boot:run
```

**Résultat attendu :** Application démarre sans erreur

## 🐛 Dépannage

### Problème : Les erreurs persistent après rechargement

**Solution 1 : Nettoyer le cache de l'IDE**

**IntelliJ :**
```
File → Invalidate Caches / Restart → Invalidate and Restart
```

**Eclipse :**
```
Project → Clean → Clean all projects
```

**Solution 2 : Supprimer les fichiers de cache**

```bash
# Supprimer le dossier target
rm -rf target/

# Supprimer le cache Maven local (si nécessaire)
rm -rf ~/.m2/repository/org/thymeleaf/
```

**Solution 3 : Forcer la mise à jour Maven**

```bash
mvn clean install -U
```

### Problème : Version de Thymeleaf incompatible

Si vous voyez des erreurs de version, vérifiez que vous utilisez Spring Boot 3.1.4 :

```xml
<parent>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-parent</artifactId>
    <version>3.1.4</version>
</parent>
```

Spring Boot 3.x utilise automatiquement Thymeleaf 3.1.x compatible avec Spring 6.

### Problème : Erreurs de compilation Java

Les erreurs comme "java.lang.Object cannot be resolved" sont des problèmes de cache de l'IDE.

**Solution :**
1. Fermer l'IDE
2. Supprimer les dossiers `.idea/` ou `.settings/`
3. Exécuter `mvn clean install`
4. Rouvrir l'IDE

## 📋 Checklist d'Installation

- [ ] Dépendance Thymeleaf ajoutée au `pom.xml`
- [ ] Dépendances Maven rechargées
- [ ] Projet compilé avec succès (`mvn clean compile`)
- [ ] Templates HTML créés dans `src/main/resources/templates/emails/`
- [ ] `EmailTemplateService.java` créé
- [ ] `ThymeleafConfig.java` créé
- [ ] `UserRegistrationService` refactorisé
- [ ] Tests passent avec succès
- [ ] Application démarre sans erreur

## 🎉 Prochaines Étapes

Une fois l'installation terminée :

1. **Tester l'envoi d'email** : Utilisez l'endpoint `/api/v1/auth/register`
2. **Vérifier les logs** : Consultez les logs pour voir les emails générés
3. **Personnaliser les templates** : Modifiez les fichiers HTML selon vos besoins
4. **Créer de nouveaux templates** : Suivez le guide dans `EMAIL_TEMPLATING_GUIDE.md`

## 📞 Support

Si vous rencontrez des problèmes :

1. Vérifiez les logs : `logs/application.log`
2. Consultez la documentation : `EMAIL_TEMPLATING_GUIDE.md`
3. Vérifiez la version de Java : `java -version` (doit être 17+)
4. Vérifiez la version de Maven : `mvn -version` (doit être 3.6+)

---

**Dernière mise à jour** : 2024
**Version** : 1.0
