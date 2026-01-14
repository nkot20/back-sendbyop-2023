# Script de nettoyage complet et recompilation
# Résout les problèmes de cache corrompu dans IntelliJ

Write-Host "=== NETTOYAGE COMPLET DU PROJET SendByOp ===" -ForegroundColor Cyan
Write-Host ""

# Configuration Java
$env:JAVA_HOME = "C:\Users\Honore\.jdks\corretto-17.0.12"
$env:PATH = "$env:JAVA_HOME\bin;$env:PATH"

# Étape 1: Vérifier que IntelliJ est fermé
Write-Host "⚠️  IMPORTANT: Fermez IntelliJ IDEA avant de continuer!" -ForegroundColor Yellow
Write-Host "Appuyez sur Entrée une fois IntelliJ fermé..."
Read-Host

# Étape 2: Nettoyer le dossier target
Write-Host ""
Write-Host "1️⃣  Suppression du dossier target..." -ForegroundColor Green
if (Test-Path "target") {
    Remove-Item -Path "target" -Recurse -Force
    Write-Host "   ✅ Dossier target supprimé" -ForegroundColor Green
} else {
    Write-Host "   ℹ️  Dossier target déjà supprimé" -ForegroundColor Gray
}

# Étape 3: Nettoyer les caches IntelliJ
Write-Host ""
Write-Host "2️⃣  Suppression des caches IntelliJ..." -ForegroundColor Green

if (Test-Path ".idea") {
    Remove-Item -Path ".idea" -Recurse -Force
    Write-Host "   ✅ Dossier .idea supprimé" -ForegroundColor Green
}

Get-ChildItem -Path . -Filter "*.iml" -File | ForEach-Object {
    Remove-Item $_.FullName -Force
    Write-Host "   ✅ Fichier $($_.Name) supprimé" -ForegroundColor Green
}

# Étape 4: Recompiler avec Maven
Write-Host ""
Write-Host "3️⃣  Recompilation avec Maven..." -ForegroundColor Green
Write-Host ""

.\mvnw.cmd clean compile -DskipTests

if ($LASTEXITCODE -eq 0) {
    Write-Host ""
    Write-Host "✅ COMPILATION RÉUSSIE!" -ForegroundColor Green
    Write-Host ""
    Write-Host "=== PROCHAINES ÉTAPES ===" -ForegroundColor Cyan
    Write-Host ""
    Write-Host "1. Rouvrir IntelliJ IDEA" -ForegroundColor Yellow
    Write-Host "2. File → Invalidate Caches / Restart..." -ForegroundColor Yellow
    Write-Host "3. Cocher TOUTES les options" -ForegroundColor Yellow
    Write-Host "4. Cliquer sur 'Invalidate and Restart'" -ForegroundColor Yellow
    Write-Host "5. Après le redémarrage: Build → Rebuild Project" -ForegroundColor Yellow
    Write-Host "6. Lancer l'application" -ForegroundColor Yellow
    Write-Host ""
    Write-Host "Appuyez sur Entrée pour terminer..."
    Read-Host
} else {
    Write-Host ""
    Write-Host "❌ ERREUR DE COMPILATION!" -ForegroundColor Red
    Write-Host "Consultez les erreurs ci-dessus pour plus de détails" -ForegroundColor Red
    Write-Host ""
    Write-Host "Appuyez sur Entrée pour terminer..."
    Read-Host
    exit 1
}
