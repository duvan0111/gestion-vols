# Application de Gestion de Vols - Blue Sky

## Description du Projet

Cette application web a été développée pour le consortium de compagnies aériennes Blue Sky afin de gérer les vols, les avions, les billets et l'enregistrement des passagers.

### Fonctionnalités principales

- **Gestion des vols et des avions** : Permet aux responsables de vols de gérer les vols et les avions des compagnies aériennes.
- **Gestion des billets** : Permet l'achat de billets par les clients ou par une hôtesse à un guichet.
- **Enregistrement des passagers** : Permet l'enregistrement des passagers, que ce soit en ligne, à un guichet tenu par une hôtesse ou à un guichet automatique.
- **Génération de rapports** : Permet de générer des tableaux de données pour les responsables clientèle et les services de sécurité, notamment la liste des passagers d'un vol.

### Caractéristiques spécifiques

- Un vol peut être ouvert ou fermé à l'achat de billets et à l'enregistrement sur ordre de la compagnie.
- Un client peut acheter plusieurs billets pour différents passagers en fournissant un numéro de passeport pour chaque billet.
- L'enregistrement peut donner lieu au paiement d'un supplément bagage.
- Un billet peut être annulé tant que le vol n'a pas eu lieu.
- Un billet peut comporter plusieurs vols avec des escales.

## Technologies Utilisées

### Backend
- **Framework** : Spring Boot
- **Langage** : Java
- **ORM** : Hibernate (JPA)
- **Gestion des dépendances** : Maven

### Frontend
- **Template Engine** : Thymeleaf
- **CSS** : Tailwind CSS (via CDN)

### Base de données
- **SGBD** : MySQL
- **Interface de gestion** : phpMyAdmin

## Configuration requise

- Java 11 ou supérieur
- Maven 3.6 ou supérieur
- MySQL 8.0 ou supérieur

## Installation et démarrage

### 1. Cloner le dépôt

```bash
git clone <url-du-depot>
cd GestionVols
```

### 2. Configuration de la base de données

Assurez-vous que MySQL est installé et en cours d'exécution sur votre machine.

Par défaut, l'application est configurée pour se connecter à une base de données MySQL avec les paramètres suivants :
- URL : `jdbc:mysql://localhost:3306/mydb`
- Utilisateur : `root`
- Mot de passe : `` (vide)

Si vous souhaitez modifier ces paramètres, vous pouvez le faire dans le fichier `src/main/resources/application.properties`.

### 3. Compiler et exécuter l'application

```bash
mvn clean install
mvn spring-boot:run
```

L'application sera accessible à l'adresse : http://localhost:8080

### 4. Accès à l'application

Deux utilisateurs sont pré-configurés dans l'application :

1. **Hôtesse**
   - Email : `hostess@bluesky.com`
   - Mot de passe : `password`
   - Rôle : Gestion des billets et enregistrement des passagers

2. **Responsable de vols**
   - Email : `manager@bluesky.com`
   - Mot de passe : `password`
   - Rôle : Gestion des vols, des avions et des compagnies

## Structure du projet

```
GestionVols/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/
│   │   │       └── bluesky/
│   │   │           └── gestionvols/
│   │   │               ├── config/       # Configuration Spring
│   │   │               ├── controller/   # Contrôleurs MVC
│   │   │               ├── model/        # Entités JPA
│   │   │               ├── repository/   # Repositories Spring Data
│   │   │               ├── service/      # Services métier
│   │   │               └── GestionVolsApplication.java
│   │   └── resources/
│   │       ├── static/              # Ressources statiques (CSS, JS, images)
│   │       ├── templates/           # Templates Thymeleaf
│   │       └── application.properties # Configuration de l'application
│   └── test/                        # Tests unitaires et d'intégration
├── pom.xml                          # Configuration Maven
└── README.md                        # Documentation du projet
```

## Spécifications de l'interface utilisateur

- Interfaces intuitives, conviviales et dynamiques
- Listing paginé à hauteur de 100 éléments par page
- Formulaires d'ajout et de modification dans des modales
- Confirmation de l'utilisateur avant toute suppression

## Licence

Ce projet a été développé dans le cadre d'un projet scolaire.
