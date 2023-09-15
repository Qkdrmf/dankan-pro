package com.dankan.dto.response.review;

import com.dankan.domain.Image;
import com.dankan.domain.RoomReview;
import com.dankan.domain.User;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OtherReviewResponseDto {
    private Long reviewId;
    private String nickname;
    private String univ;
    private LocalDate startedAt;
    private LocalDate endAt;
    private Double totalRate;
    private String content;
    private LocalDate createdAt;
    private String imgUrl;
    private String roomType;

    public static OtherReviewResponseDto of(User user, RoomReview roomReview, List<Image> imageList) {
        String imgUrls = "";

        for (Image img : imageList) {
            imgUrls += img.getImageUrl()+" ";
        }

        return OtherReviewResponseDto.builder()
                .reviewId(roomReview.getReviewId())
                .nickname(user.getNickname())
                .totalRate(roomReview.getTotalRate())
                .startedAt(roomReview.getResidencePeriod().getStartedAt())
                .endAt(roomReview.getResidencePeriod().getEndAt())
                .content(roomReview.getContent())
                .createdAt(roomReview.getCreatedAt())
                .imgUrl(imgUrls)
                .build();
    }
}
