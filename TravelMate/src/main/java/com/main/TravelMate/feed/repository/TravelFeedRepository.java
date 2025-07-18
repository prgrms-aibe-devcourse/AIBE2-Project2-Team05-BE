package com.main.TravelMate.feed.repository;

import com.main.TravelMate.feed.entity.TravelFeed;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TravelFeedRepository extends JpaRepository<TravelFeed, Long> {
    List<TravelFeed> findAllByOrderByCreatedAtDesc();
}
