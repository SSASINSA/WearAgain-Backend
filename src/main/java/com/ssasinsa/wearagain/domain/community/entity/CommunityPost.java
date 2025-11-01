package com.ssasinsa.wearagain.domain.community.entity;

import com.ssasinsa.wearagain.auth.domain.User;
import com.ssasinsa.wearagain.common.entity.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "community_posts")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
public class CommunityPost extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "community_posts_id", nullable = false, updatable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "users_id", nullable = false)
    private User user;

    @Column(length = 255)
    private String title;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "community_categories_id", nullable = false)
    private CommunityCategory category;

    @Column(name = "like_count", nullable = false)
    @Builder.Default
    private int likeCount = 0;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private boolean active = true;

    @OneToMany(mappedBy = "post", fetch = FetchType.LAZY)
    @Builder.Default
    private List<PostLike> likes = new ArrayList<>();

    @OneToMany(mappedBy = "post", fetch = FetchType.LAZY)
    @Builder.Default
    private List<PostComment> comments = new ArrayList<>();

    @OneToMany(mappedBy = "post", fetch = FetchType.LAZY)
    @Builder.Default
    private List<CommunityPostImage> images = new ArrayList<>();

    public static CommunityPost create(User user, CommunityCategory category, String title, String content, List<String> imageUrls) {
        CommunityPost post = CommunityPost.builder()
                .user(user)
                .title(title)
                .content(content)
                .category(category)
                .build();

        if (imageUrls != null) {
            int order = 0;
            for (String imageUrl : imageUrls) {
                CommunityPostImage.create(post, imageUrl, order++);
            }
        }

        return post;
    }

    void addLike(PostLike like) {
        likes.add(like);
        likeCount = likeCount + 1;
    }

    void removeLike() {
        likeCount = Math.max(0, likeCount - 1);
    }

    void addComment(PostComment comment) {
        comments.add(comment);
    }

    void addImage(CommunityPostImage image) {
        images.add(image);
    }

    public void assignCategory(CommunityCategory category) {
        this.category = category;
    }

    public void deactivate() {
        this.active = false;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof CommunityPost)) {
            return false;
        }
        CommunityPost other = (CommunityPost) o;
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
