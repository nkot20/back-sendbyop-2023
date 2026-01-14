# ========================================
# Script de Génération de JWT Secret
# SendByOp - Authentification Sécurisée
# ========================================

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  Générateur de JWT Secret - SendByOp  " -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Fonction pour générer un secret aléatoire
function Generate-JWTSecret {
    param (
        [int]$ByteLength = 64
    )
    
    $bytes = New-Object byte[] $ByteLength
    $rng = [Security.Cryptography.RNGCryptoServiceProvider]::Create()
    $rng.GetBytes($bytes)
    $rng.Dispose()
    
    return [Convert]::ToBase64String($bytes)
}

# Générer le secret
Write-Host "🔐 Génération d'un JWT secret sécurisé..." -ForegroundColor Yellow
Write-Host ""

$jwtSecret = Generate-JWTSecret -ByteLength 64

Write-Host "✅ JWT Secret généré avec succès !" -ForegroundColor Green
Write-Host ""
Write-Host "Longueur : $($jwtSecret.Length) caractères" -ForegroundColor Gray
Write-Host "Algorithme : HS512 (512 bits)" -ForegroundColor Gray
Write-Host ""

# Afficher le secret
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "VOTRE JWT SECRET :" -ForegroundColor Yellow
Write-Host "========================================" -ForegroundColor Cyan
Write-Host $jwtSecret -ForegroundColor White
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Copier dans le presse-papiers
try {
    Set-Clipboard -Value $jwtSecret
    Write-Host "📋 Le secret a été copié dans le presse-papiers !" -ForegroundColor Green
} catch {
    Write-Host "⚠️  Impossible de copier dans le presse-papiers" -ForegroundColor Yellow
}

Write-Host ""

# Instructions
Write-Host "📝 PROCHAINES ÉTAPES :" -ForegroundColor Cyan
Write-Host ""
Write-Host "1. Créez un fichier .env à la racine du projet (si pas déjà fait)" -ForegroundColor White
Write-Host "   > cp .env.example .env" -ForegroundColor Gray
Write-Host ""
Write-Host "2. Ouvrez le fichier .env et ajoutez :" -ForegroundColor White
Write-Host "   JWT_SECRET=$jwtSecret" -ForegroundColor Gray
Write-Host ""
Write-Host "3. Configurez les durées de validité :" -ForegroundColor White
Write-Host "   JWT_EXPIRATION=86400000        # 24 heures" -ForegroundColor Gray
Write-Host "   JWT_REFRESH_EXPIRATION=604800000  # 7 jours" -ForegroundColor Gray
Write-Host ""
Write-Host "4. Vérifiez que .env est dans .gitignore" -ForegroundColor White
Write-Host ""

# Avertissements de sécurité
Write-Host "⚠️  IMPORTANT - SÉCURITÉ :" -ForegroundColor Red
Write-Host ""
Write-Host "❌ Ne JAMAIS commiter ce secret dans Git" -ForegroundColor Red
Write-Host "❌ Ne JAMAIS partager ce secret publiquement" -ForegroundColor Red
Write-Host "❌ Ne JAMAIS utiliser le même secret pour dev et prod" -ForegroundColor Red
Write-Host "✅ Générer un nouveau secret pour chaque environnement" -ForegroundColor Green
Write-Host "✅ Changer le secret tous les 3-6 mois" -ForegroundColor Green
Write-Host ""

# Option pour générer plusieurs secrets
Write-Host "========================================" -ForegroundColor Cyan
$response = Read-Host "Voulez-vous générer des secrets pour tous les environnements ? (o/N)"

if ($response -eq 'o' -or $response -eq 'O') {
    Write-Host ""
    Write-Host "🔐 Génération des secrets pour tous les environnements..." -ForegroundColor Yellow
    Write-Host ""
    
    $devSecret = Generate-JWTSecret -ByteLength 64
    $stagingSecret = Generate-JWTSecret -ByteLength 64
    $prodSecret = Generate-JWTSecret -ByteLength 64
    
    Write-Host "========================================" -ForegroundColor Cyan
    Write-Host "DÉVELOPPEMENT (.env.dev)" -ForegroundColor Yellow
    Write-Host "========================================" -ForegroundColor Cyan
    Write-Host "JWT_SECRET=$devSecret" -ForegroundColor White
    Write-Host ""
    
    Write-Host "========================================" -ForegroundColor Cyan
    Write-Host "STAGING (.env.staging)" -ForegroundColor Yellow
    Write-Host "========================================" -ForegroundColor Cyan
    Write-Host "JWT_SECRET=$stagingSecret" -ForegroundColor White
    Write-Host ""
    
    Write-Host "========================================" -ForegroundColor Cyan
    Write-Host "PRODUCTION (.env.prod)" -ForegroundColor Yellow
    Write-Host "========================================" -ForegroundColor Cyan
    Write-Host "JWT_SECRET=$prodSecret" -ForegroundColor White
    Write-Host ""
    
    # Sauvegarder dans des fichiers
    $saveFiles = Read-Host "Voulez-vous sauvegarder ces secrets dans des fichiers ? (o/N)"
    
    if ($saveFiles -eq 'o' -or $saveFiles -eq 'O') {
        try {
            "JWT_SECRET=$devSecret" | Out-File -FilePath ".env.dev.secret" -Encoding UTF8
            "JWT_SECRET=$stagingSecret" | Out-File -FilePath ".env.staging.secret" -Encoding UTF8
            "JWT_SECRET=$prodSecret" | Out-File -FilePath ".env.prod.secret" -Encoding UTF8
            
            Write-Host "✅ Secrets sauvegardés dans :" -ForegroundColor Green
            Write-Host "   - .env.dev.secret" -ForegroundColor Gray
            Write-Host "   - .env.staging.secret" -ForegroundColor Gray
            Write-Host "   - .env.prod.secret" -ForegroundColor Gray
            Write-Host ""
            Write-Host "⚠️  N'oubliez pas d'ajouter ces fichiers à .gitignore !" -ForegroundColor Yellow
        } catch {
            Write-Host "❌ Erreur lors de la sauvegarde des fichiers" -ForegroundColor Red
        }
    }
}

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "✅ Terminé ! Bonne configuration !" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Pause pour lire les instructions
Read-Host "Appuyez sur Entrée pour quitter"
