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
import java.util.Objects;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "comments")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PostComment extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "comments_id", nullable = false, updatable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "community_posts_id", nullable = false)
    private CommunityPost post;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "users_id", nullable = false)
    private User user;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @Column(name = "is_active", nullable = false)
    private boolean active;

    @Builder(access = AccessLevel.PRIVATE)
    private PostComment(CommunityPost post, User user, String content, Boolean active) {
        this.post = post;
        this.user = user;
        this.content = content;
        this.active = active == null || active;
    }

    public static PostComment create(CommunityPost post, User user, String content) {
        PostComment comment = PostComment.builder()
                .post(post)
                .user(user)
                .content(content)
                .build();
        if (post != null) {
            post.addComment(comment);
        }
        return comment;
    }

    public void deactivate() {
        this.active = false;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof PostComment)) {
            return false;
        }
        PostComment other = (PostComment) o;
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
