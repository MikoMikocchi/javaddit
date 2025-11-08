package com.example.javaddit.features.post.repository;

import com.example.javaddit.features.post.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {

    @Query("SELECT p FROM Post p WHERE p.community.name = :communityName ORDER BY p.createdAt DESC")
    List<Post> findByCommunityName(@Param("communityName") String communityName);

    @Query("SELECT p FROM Post p ORDER BY p.createdAt DESC")
    List<Post> findAllOrderByCreatedAtDesc();

    @Query("SELECT CASE WHEN COUNT(p) > 0 THEN true ELSE false END FROM Post p WHERE p.community.id = :communityId AND p.slug = :slug")
    boolean existsByCommunityIdAndSlug(@Param("communityId") Long communityId, @Param("slug") String slug);

    @Query("SELECT p.slug FROM Post p WHERE p.community.id = :communityId AND p.slug LIKE :slugPattern")
    List<String> findSlugsByCommunityIdAndSlugPattern(@Param("communityId") Long communityId, @Param("slugPattern") String slugPattern);
}
