package org.example.Services;

import org.example.Entities.Annonce;
import org.example.Entities.User;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class RecommendationService {

    private final AnnonceReactionService reactionService = new AnnonceReactionService();

    public List<Annonce> rankForUser(List<Annonce> annonces, User user) {
        if (annonces == null || annonces.isEmpty()) {
            return List.of();
        }
        if (user == null || user.getId() <= 0) {
            return new ArrayList<>(annonces);
        }

        Map<String, Integer> categoryScores;
        try {
            categoryScores = reactionService.getUserCategoryScores(user.getId());
        } catch (SQLException e) {
            // Fallback non bloquant: garder l'ordre actuel si la recommendation n'est pas dispo.
            return new ArrayList<>(annonces);
        }

        List<ScoredAnnonce> scored = new ArrayList<>();
        for (Annonce annonce : annonces) {
            double score = 0.0;

            int catScore = categoryScores.getOrDefault(safe(annonce.getCategorie()), 0);
            score += 0.7 * catScore;

            if (safe(annonce.getRegion()).equalsIgnoreCase(safe(user.getRegion()))) {
                score += 2.0;
            }

            if (annonce.getDatePub() != null && annonce.getDatePub().isAfter(LocalDateTime.now().minusDays(7))) {
                score += 1.0;
            }

            scored.add(new ScoredAnnonce(annonce, score));
        }

        scored.sort(Comparator.comparingDouble(ScoredAnnonce::score).reversed()
                .thenComparing(sa -> sa.annonce().getDatePub(), Comparator.nullsLast(Comparator.reverseOrder())));

        List<Annonce> ranked = new ArrayList<>();
        for (ScoredAnnonce item : scored) {
            ranked.add(item.annonce());
        }
        return ranked;
    }

    private String safe(String value) {
        return value == null ? "" : value.trim();
    }

    private record ScoredAnnonce(Annonce annonce, double score) {}
}

