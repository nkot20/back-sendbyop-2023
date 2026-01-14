#!/bin/bash

# ========================================
# Script de Génération de JWT Secret
# SendByOp - Authentification Sécurisée
# ========================================

# Couleurs
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
CYAN='\033[0;36m'
WHITE='\033[1;37m'
GRAY='\033[0;37m'
NC='\033[0m' # No Color

echo -e "${CYAN}========================================${NC}"
echo -e "${CYAN}  Générateur de JWT Secret - SendByOp  ${NC}"
echo -e "${CYAN}========================================${NC}"
echo ""

# Fonction pour générer un secret aléatoire
generate_jwt_secret() {
    openssl rand -base64 64 | tr -d '\n'
}

# Générer le secret
echo -e "${YELLOW}🔐 Génération d'un JWT secret sécurisé...${NC}"
echo ""

JWT_SECRET=$(generate_jwt_secret)

echo -e "${GREEN}✅ JWT Secret généré avec succès !${NC}"
echo ""
echo -e "${GRAY}Longueur : ${#JWT_SECRET} caractères${NC}"
echo -e "${GRAY}Algorithme : HS512 (512 bits)${NC}"
echo ""

# Afficher le secret
echo -e "${CYAN}========================================${NC}"
echo -e "${YELLOW}VOTRE JWT SECRET :${NC}"
echo -e "${CYAN}========================================${NC}"
echo -e "${WHITE}${JWT_SECRET}${NC}"
echo -e "${CYAN}========================================${NC}"
echo ""

# Copier dans le presse-papiers (si possible)
if command -v pbcopy &> /dev/null; then
    echo "$JWT_SECRET" | pbcopy
    echo -e "${GREEN}📋 Le secret a été copié dans le presse-papiers (macOS) !${NC}"
elif command -v xclip &> /dev/null; then
    echo "$JWT_SECRET" | xclip -selection clipboard
    echo -e "${GREEN}📋 Le secret a été copié dans le presse-papiers (Linux) !${NC}"
elif command -v xsel &> /dev/null; then
    echo "$JWT_SECRET" | xsel --clipboard
    echo -e "${GREEN}📋 Le secret a été copié dans le presse-papiers (Linux) !${NC}"
else
    echo -e "${YELLOW}⚠️  Copiez manuellement le secret ci-dessus${NC}"
fi

echo ""

# Instructions
echo -e "${CYAN}📝 PROCHAINES ÉTAPES :${NC}"
echo ""
echo -e "${WHITE}1. Créez un fichier .env à la racine du projet (si pas déjà fait)${NC}"
echo -e "${GRAY}   > cp .env.example .env${NC}"
echo ""
echo -e "${WHITE}2. Ouvrez le fichier .env et ajoutez :${NC}"
echo -e "${GRAY}   JWT_SECRET=${JWT_SECRET}${NC}"
echo ""
echo -e "${WHITE}3. Configurez les durées de validité :${NC}"
echo -e "${GRAY}   JWT_EXPIRATION=86400000        # 24 heures${NC}"
echo -e "${GRAY}   JWT_REFRESH_EXPIRATION=604800000  # 7 jours${NC}"
echo ""
echo -e "${WHITE}4. Vérifiez que .env est dans .gitignore${NC}"
echo ""

# Avertissements de sécurité
echo -e "${RED}⚠️  IMPORTANT - SÉCURITÉ :${NC}"
echo ""
echo -e "${RED}❌ Ne JAMAIS commiter ce secret dans Git${NC}"
echo -e "${RED}❌ Ne JAMAIS partager ce secret publiquement${NC}"
echo -e "${RED}❌ Ne JAMAIS utiliser le même secret pour dev et prod${NC}"
echo -e "${GREEN}✅ Générer un nouveau secret pour chaque environnement${NC}"
echo -e "${GREEN}✅ Changer le secret tous les 3-6 mois${NC}"
echo ""

# Option pour générer plusieurs secrets
echo -e "${CYAN}========================================${NC}"
read -p "Voulez-vous générer des secrets pour tous les environnements ? (o/N) " -n 1 -r
echo ""

if [[ $REPLY =~ ^[Oo]$ ]]; then
    echo ""
    echo -e "${YELLOW}🔐 Génération des secrets pour tous les environnements...${NC}"
    echo ""
    
    DEV_SECRET=$(generate_jwt_secret)
    STAGING_SECRET=$(generate_jwt_secret)
    PROD_SECRET=$(generate_jwt_secret)
    
    echo -e "${CYAN}========================================${NC}"
    echo -e "${YELLOW}DÉVELOPPEMENT (.env.dev)${NC}"
    echo -e "${CYAN}========================================${NC}"
    echo -e "${WHITE}JWT_SECRET=${DEV_SECRET}${NC}"
    echo ""
    
    echo -e "${CYAN}========================================${NC}"
    echo -e "${YELLOW}STAGING (.env.staging)${NC}"
    echo -e "${CYAN}========================================${NC}"
    echo -e "${WHITE}JWT_SECRET=${STAGING_SECRET}${NC}"
    echo ""
    
    echo -e "${CYAN}========================================${NC}"
    echo -e "${YELLOW}PRODUCTION (.env.prod)${NC}"
    echo -e "${CYAN}========================================${NC}"
    echo -e "${WHITE}JWT_SECRET=${PROD_SECRET}${NC}"
    echo ""
    
    # Sauvegarder dans des fichiers
    read -p "Voulez-vous sauvegarder ces secrets dans des fichiers ? (o/N) " -n 1 -r
    echo ""
    
    if [[ $REPLY =~ ^[Oo]$ ]]; then
        echo "JWT_SECRET=${DEV_SECRET}" > .env.dev.secret
        echo "JWT_SECRET=${STAGING_SECRET}" > .env.staging.secret
        echo "JWT_SECRET=${PROD_SECRET}" > .env.prod.secret
        
        echo -e "${GREEN}✅ Secrets sauvegardés dans :${NC}"
        echo -e "${GRAY}   - .env.dev.secret${NC}"
        echo -e "${GRAY}   - .env.staging.secret${NC}"
        echo -e "${GRAY}   - .env.prod.secret${NC}"
        echo ""
        echo -e "${YELLOW}⚠️  N'oubliez pas d'ajouter ces fichiers à .gitignore !${NC}"
        
        # Ajouter automatiquement à .gitignore si possible
        if [ -f .gitignore ]; then
            if ! grep -q ".env.*.secret" .gitignore; then
                echo "" >> .gitignore
                echo "# JWT Secrets" >> .gitignore
                echo ".env.*.secret" >> .gitignore
                echo -e "${GREEN}✅ Ajouté à .gitignore automatiquement${NC}"
            fi
        fi
    fi
fi

echo ""
echo -e "${CYAN}========================================${NC}"
echo -e "${GREEN}✅ Terminé ! Bonne configuration !${NC}"
echo -e "${CYAN}========================================${NC}"
echo ""
