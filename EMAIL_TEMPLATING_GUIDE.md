# 📧 Guide du Système de Templating d'Emails - SendByOp

## 🎯 Vue d'ensemble

Ce système utilise **Thymeleaf** comme moteur de template pour générer des emails HTML professionnels et maintenables. Fini le code HTML hardcodé dans les services Java !

## ✨ Avantages du Système de Templating

### 1. **Séparation des Préoccupations**
- ✅ Le HTML est dans des fichiers `.html` séparés
- ✅ La logique métier reste dans les services Java
- ✅ Les designers peuvent modifier les templates sans toucher au code Java

### 2. **Maintenabilité**
- ✅ Modification facile du design des emails
- ✅ Réutilisation des templates
- ✅ Gestion centralisée des styles

### 3. **Professionnalisme**
- ✅ Templates HTML complets avec CSS
- ✅ Design responsive
- ✅ Cohérence visuelle

### 4. **Testabilité**
- ✅ Templates testables indépendamment
- ✅ Prévisualisation facile dans un navigateur
- ✅ Validation HTML

## 🏗️ Architecture

```
src/main/resources/templates/emails/
├── email-verification.html      # Template de vérification d'email
├── password-reset.html          # Template de réinitialisation de mot de passe
└── welcome.html                 # Template de bienvenue (à créer)

src/main/java/.../services/impl/
├── EmailTemplateService.java    # Service de génération de templates
├── SendMailService.java         # Service d'envoi d'emails
└── UserRegistrationService.java # Utilise les templates

src/main/java/.../config/
└── ThymeleafConfig.java         # Configuration Thymeleaf
```

## 📝 Création d'un Nouveau Template

### Étape 1 : Créer le fichier HTML

Créez un fichier dans `src/main/resources/templates/emails/mon-template.html` :

```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Mon Template</title>
    <style>
        /* Vos styles CSS ici */
        body {
            font-family: Arial, sans-serif;
            color: #333;
        }
        .container {
            max-width: 600px;
            margin: 0 auto;
        }
    </style>
</head>
<body>
    <div class="container">
        <h1>Bonjour <span th:text="${customerName}">Client</span>!</h1>
        <p th:text="${message}">Votre message ici</p>
        
        <!-- Variables automatiques -->
        <p>Année: <span th:text="${currentYear}">2024</span></p>
        <a th:href="${websiteUrl}">Visitez notre site</a>
    </div>
</body>
</html>
```

### Étape 2 : Ajouter une méthode dans EmailTemplateService

```java
public String generateMonTemplate(String customerName, String message) {
    log.info("Génération de mon template pour: {}", customerName);
    
    Map<String, Object> variables = Map.of(
        "customerName", customerName,
        "message", message
    );
    
    return generateEmailContent("mon-template", variables);
}
```

### Étape 3 : Utiliser le template

```java
String htmlContent = emailTemplateService.generateMonTemplate("John Doe", "Bienvenue!");
emailService.sendHtmlEmail("john@example.com", "Sujet", htmlContent);
```

## 🎨 Syntaxe Thymeleaf

### Variables

```html
<!-- Afficher une variable -->
<span th:text="${nomVariable}">Valeur par défaut</span>

<!-- Utiliser dans un attribut -->
<a th:href="${lienUrl}">Cliquez ici</a>
<img th:src="${imageUrl}" alt="Image"/>
```

### Conditions

```html
<!-- If simple -->
<div th:if="${condition}">
    Affiché si condition est true
</div>

<!-- If/Else -->
<div th:if="${user.isPremium}">
    Contenu premium
</div>
<div th:unless="${user.isPremium}">
    Contenu standard
</div>
```

### Boucles

```html
<ul>
    <li th:each="item : ${items}" th:text="${item.name}">Item</li>
</ul>
```

### Formatage

```html
<!-- Dates -->
<span th:text="${#dates.format(date, 'dd/MM/yyyy')}">01/01/2024</span>

<!-- Nombres -->
<span th:text="${#numbers.formatDecimal(price, 1, 2)}">10.50</span>

<!-- Texte -->
<span th:text="${#strings.toUpperCase(text)}">TEXTE</span>
```

## 📦 Templates Disponibles

### 1. Email de Vérification (`email-verification.html`)

**Variables requises :**
- `customerName` : Nom complet du client
- `verificationUrl` : URL complète de vérification

**Variables automatiques :**
- `currentYear` : Année actuelle
- `websiteUrl` : URL du site SendByOp

**Utilisation :**
```java
String htmlContent = emailTemplateService.generateVerificationEmail(
    "John Doe",
    "https://sendbyop.com/verify?token=abc123"
);
```

### 2. Réinitialisation de Mot de Passe (`password-reset.html`)

**Variables requises :**
- `customerName` : Nom complet du client
- `resetUrl` : URL de réinitialisation

**Utilisation :**
```java
String htmlContent = emailTemplateService.generatePasswordResetEmail(
    "John Doe",
    "https://sendbyop.com/reset?token=xyz789"
);
```

## 🛠️ Configuration

### Configuration Thymeleaf

Le fichier `ThymeleafConfig.java` configure :
- **Préfixe** : `classpath:/templates/`
- **Suffixe** : `.html`
- **Mode** : HTML
- **Encodage** : UTF-8
- **Cache** : Désactivé en développement

### Variables Globales

Toutes les variables suivantes sont automatiquement ajoutées à chaque template :

| Variable | Description | Exemple |
|----------|-------------|---------|
| `currentYear` | Année actuelle | 2024 |
| `websiteUrl` | URL du site | https://www.sendbyop.com |

## 🎯 Bonnes Pratiques

### 1. **Design Responsive**

```html
<style>
    @media only screen and (max-width: 600px) {
        .container {
            width: 100% !important;
        }
    }
</style>
```

### 2. **Styles Inline pour Compatibilité**

Certains clients email ne supportent pas les styles `<style>`. Utilisez des styles inline pour les éléments critiques :

```html
<div style="background-color: #4CAF50; padding: 20px;">
    Contenu important
</div>
```

### 3. **Texte Alternatif**

Toujours fournir un texte alternatif pour les images :

```html
<img th:src="${logoUrl}" alt="Logo SendByOp" style="max-width: 200px;"/>
```

### 4. **Liens Absolus**

Utilisez toujours des URLs absolues :

```html
<!-- ✅ Bon -->
<a th:href="'https://sendbyop.com/verify?token=' + ${token}">Vérifier</a>

<!-- ❌ Mauvais -->
<a th:href="'/verify?token=' + ${token}">Vérifier</a>
```

### 5. **Fallback pour Boutons**

Fournissez toujours un lien textuel en plus du bouton :

```html
<a th:href="${verificationUrl}" class="button">Vérifier mon email</a>
<p>Ou copiez ce lien : <span th:text="${verificationUrl}"></span></p>
```

## 🧪 Tests

### Test d'un Template

```java
@Test
void testEmailVerificationTemplate() {
    String htmlContent = emailTemplateService.generateVerificationEmail(
        "Test User",
        "https://test.com/verify?token=test123"
    );
    
    assertThat(htmlContent).contains("Test User");
    assertThat(htmlContent).contains("https://test.com/verify?token=test123");
    assertThat(htmlContent).contains("Vérifier mon email");
}
```

### Prévisualisation dans un Navigateur

1. Générez le HTML :
```java
String html = emailTemplateService.generateVerificationEmail("John Doe", "https://test.com");
Files.writeString(Path.of("preview.html"), html);
```

2. Ouvrez `preview.html` dans votre navigateur

## 🔧 Dépannage

### Problème : Template non trouvé

**Erreur :** `TemplateInputException: Error resolving template`

**Solution :**
- Vérifiez que le fichier existe dans `src/main/resources/templates/emails/`
- Vérifiez l'extension `.html`
- Vérifiez le nom du template (sans le préfixe `emails/`)

### Problème : Variable non résolue

**Erreur :** Variable `${maVariable}` affiche `null`

**Solution :**
- Vérifiez que la variable est bien passée dans le `Map<String, Object>`
- Vérifiez l'orthographe du nom de variable
- Utilisez `th:if` pour gérer les valeurs nulles

### Problème : Styles CSS non appliqués

**Solution :**
- Utilisez des styles inline pour la compatibilité maximale
- Testez avec différents clients email (Gmail, Outlook, etc.)
- Utilisez des outils comme [Litmus](https://litmus.com/) pour tester

## 📊 Comparaison Avant/Après

### ❌ Avant (HTML Hardcodé)

```java
String content = "<!DOCTYPE html><html><head><style>"
    + "body { font-family: Arial; }"
    + ".button { background: #4CAF50; }"
    + "</style></head><body>"
    + "<h1>Bonjour " + customer.getName() + "</h1>"
    + "<a href='" + verifyUrl + "'>Vérifier</a>"
    + "</body></html>";
```

**Problèmes :**
- 😞 Difficile à lire
- 😞 Difficile à maintenir
- 😞 Mélange HTML et Java
- 😞 Pas de coloration syntaxique
- 😞 Erreurs difficiles à détecter

### ✅ Après (Template Thymeleaf)

**Template HTML :**
```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
<head>
    <style>
        body { font-family: Arial; }
        .button { background: #4CAF50; }
    </style>
</head>
<body>
    <h1>Bonjour <span th:text="${customerName}">Client</span></h1>
    <a th:href="${verificationUrl}" class="button">Vérifier</a>
</body>
</html>
```

**Code Java :**
```java
String htmlContent = emailTemplateService.generateVerificationEmail(
    customer.getName(),
    verifyUrl
);
```

**Avantages :**
- 😊 Code propre et lisible
- 😊 Séparation des préoccupations
- 😊 Facile à maintenir
- 😊 Coloration syntaxique HTML
- 😊 Validation HTML automatique

## 🚀 Prochaines Étapes

1. **Créer plus de templates** : Bienvenue, confirmation de réservation, etc.
2. **Internationalisation** : Templates multilingues
3. **Composants réutilisables** : Header, footer communs
4. **Tests automatisés** : Tests de rendu des templates
5. **Prévisualisation** : Interface admin pour prévisualiser les emails

---

**Dernière mise à jour** : 2024
**Version** : 1.0
**Auteur** : Équipe SendByOp
