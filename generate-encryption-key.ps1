# Script pour générer une clé de chiffrement AES-256 valide
# La clé générée fait 32 bytes (256 bits) encodée en Base64

Write-Host "=== GÉNÉRATION DE CLÉ DE CHIFFREMENT AES-256 ===" -ForegroundColor Cyan
Write-Host ""

# Générer 32 bytes aléatoires (256 bits pour AES-256)
$keyBytes = New-Object byte[] 32
$rng = [System.Security.Cryptography.RNGCryptoServiceProvider]::Create()
$rng.GetBytes($keyBytes)

# Encoder en Base64
$base64Key = [System.Convert]::ToBase64String($keyBytes)

Write-Host "✅ Clé de chiffrement AES-256 générée avec succès!" -ForegroundColor Green
Write-Host ""
Write-Host "Longueur de la clé: 32 bytes (256 bits)" -ForegroundColor Gray
Write-Host "Format: Base64" -ForegroundColor Gray
Write-Host ""
Write-Host "Votre clé de chiffrement:" -ForegroundColor Yellow
Write-Host $base64Key -ForegroundColor White
Write-Host ""

# Copier dans le presse-papiers
try {
    Set-Clipboard -Value $base64Key
    Write-Host "📋 Clé copiée dans le presse-papiers!" -ForegroundColor Green
} catch {
    Write-Host "⚠️  Impossible de copier dans le presse-papiers" -ForegroundColor Yellow
}

Write-Host ""
Write-Host "=== COMMENT L'UTILISER ===" -ForegroundColor Cyan
Write-Host ""
Write-Host "1. Ouvrez votre fichier .env (ou créez-le depuis .env.example)" -ForegroundColor White
Write-Host "2. Ajoutez ou modifiez la ligne:" -ForegroundColor White
Write-Host "   ENCRYPTION_SECRET_KEY=$base64Key" -ForegroundColor Yellow
Write-Host ""
Write-Host "3. IMPORTANT: Ne JAMAIS commiter cette clé dans Git!" -ForegroundColor Red
Write-Host "4. Utilisez une clé différente pour chaque environnement (dev/prod)" -ForegroundColor Yellow
Write-Host ""
Write-Host "Appuyez sur Entrée pour terminer..."
Read-Host
