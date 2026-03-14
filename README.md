# Compte Rendu Technique : Application Ko-List

**Auteurs :** Milan LOI & Jules GAYET

## Introduction

Ko-List est une application Android de gestion de tâches (To-Do List) intégrant des mécaniques de ludification (gamification) inspirées des jeux de rôle (RPG). L'objectif est de transformer la gestion robotique des corvées quotidiennes en une expérience interactive et gratifiante, encourageant la régularité par l'évolution d'un avatar.

## Spécifications Fonctionnelles

### Gestion des objectifs

- **Cycle de vie complet :** L'utilisateur peut créer, consulter, modifier et supprimer des tâches de manière intuitive.
- **Attributs détaillés :** Chaque tâche peut être enrichie d'une description, d'une photo, d'une date d'échéance et d'une récurrence (quotidienne, hebdomadaire, mensuelle).
- **Gestion des priorités :** Trois niveaux d'importance (Basse, Moyenne, Haute) permettent une hiérarchisation claire des priorités.
- **Support multimédia :** Possibilité d'associer une image ou une photo à chaque objectif pour une identification visuelle rapide.

### Organisation et Ergonomie

- **Filtrage dynamique :** L'interface propose des vues filtrées par état (tâches en cours, terminées ou vue exhaustive).
- **Algorithme de tri :** Les tâches sont automatiquement ordonnées par niveau de priorité, puis par proximité de la date d'échéance.
- **Maintenance de la liste :** Une fonction de purge permet de supprimer massivement les tâches archivées.

### Système de Progression (Gamification)

- **Calcul d'expérience (XP) :** L'accomplissement d'une tâche génère un gain d'XP proportionnel à sa difficulté (priorité).
- **Hiérarchie des rangs :** La progression est segmentée en cinq paliers honorifiques :
  1. Novice de l'Organisation
  2. Apprenti Planificateur
  3. Expert en Productivité
  4. Maître des Tâches
  5. Légende du Temps
- **Feedback visuel :** Des animations (système de particules) viennent souligner la réussite d'un objectif.

## Analyse Technique et Comparatif

| Domaine technique     | Solution implémentée (Moderne) | Approche traditionnelle      | Justification du choix                                                      |
| :-------------------- | :----------------------------- | :--------------------------- | :-------------------------------------------------------------------------- |
| Interface Utilisateur | Jetpack Compose                | XML Layouts                  | Développement déclaratif plus rapide et maintenance simplifiée.             |
| Gestion d'état        | StateFlow (Coroutines)         | LiveData                     | Interface réactive plus performante et meilleure gestion de la concurrence. |
| Design System         | Material 3                     | Material 2                   | Accès aux derniers standards ergonomiques et thèmes dynamiques.             |
| Navigation            | Jetpack Compose Navigation     | Intent / Multiple Activities | Fluidité de navigation accrue et typage des arguments.                      |
| Architecture          | MVVM                           | MVC                          | Séparation stricte de la logique métier et de l'interface graphique.        |

## Démonstration

Une séquence vidéo illustrant l'ensemble du parcours utilisateur, incluant la création d'une tâche et le passage de niveau, est jointe aux livrables de ce projet.
