package com.ssasinsa.wearagain.domain.community.entity;

import com.ssasinsa.wearagain.domain.auth.entity.User;
import com.ssasinsa.wearagain.common.entity.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.util.Objects;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(
        name = "likes",
        uniqueConstraints = @UniqueConstraint(name = "uk_likes_unique", columnNames = {"community_posts_id", "users_id"})
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PostLike extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "likes_id", nullable = false, updatable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "community_posts_id", nullable = false)
    private CommunityPost post;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "users_id", nullable = false)
    private User user;

    @Builder(access = AccessLevel.PRIVATE)
    private PostLike(CommunityPost post, User user) {
        this.post = post;
        this.user = user;
    }

    public static PostLike create(CommunityPost post, User user) {
        PostLike like = PostLike.builder()
                .post(post)
                .user(user)
                .build();
        if (post != null) {
            post.addLike(like);
        }
        return like;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof PostLike)) {
            return false;
        }
        PostLike other = (PostLike) o;
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
