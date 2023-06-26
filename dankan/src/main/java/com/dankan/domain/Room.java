package com.dankan.domain;

import com.dankan.domain.embedded.RoomAddress;
import com.dankan.domain.embedded.RoomCost;
import com.dankan.domain.embedded.RoomDiscussion;
import com.dankan.domain.embedded.RoomOption;
import com.dankan.domain.embedded.RoomStructure;
import com.dankan.dto.request.post.PostRoomRequestDto;
import com.dankan.enum_converter.PriceTypeEnum;
import com.dankan.enum_converter.RoomTypeEnum;
import io.swagger.annotations.ApiModel;
import lombok.*;

import javax.persistence.*;

@ApiModel(value = "매물 엔티티")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "room")
public class Room {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "room_id", columnDefinition = "int")
    private Long roomId;

    @Column(name = "user_id",nullable = false, columnDefinition = "bigint")
    private Long userId;

    @Embedded
    private RoomCost roomCost;

    @Embedded
    private RoomStructure roomStructure;

    @Embedded
    private RoomOption roomOption;

    @Embedded
    private RoomDiscussion roomDiscussion;

    @Embedded
    private RoomAddress roomAddress;

    public static Room of(PostRoomRequestDto postRoomRequestDto, Long userId) {
        String[] addressParts = postRoomRequestDto.getAddress().split(" ");

        RoomCost cost = RoomCost.builder()
                .dealType(postRoomRequestDto.getDealType())
                .priceType(PriceTypeEnum.getPriceTypeValue(postRoomRequestDto.getPriceType()))
                .deposit(postRoomRequestDto.getDeposit())
                .price(postRoomRequestDto.getPrice())
                .managementCost(postRoomRequestDto.getManagementCost())
                .managementType(postRoomRequestDto.getManagementType())
                .build();

        RoomStructure structure = RoomStructure.builder()
                .roomType(RoomTypeEnum.getRoomTypeValue(postRoomRequestDto.getRoomType()))
                .totalFloor(postRoomRequestDto.getTotalFloor())
                .floor(postRoomRequestDto.getFloor())
                .structure(postRoomRequestDto.getStructure())
                .roomSize(postRoomRequestDto.getRoomSize())
                .realRoomSize(postRoomRequestDto.getRealRoomSize())
                .build();

        RoomOption option = RoomOption.builder()
                .elevatorOption(postRoomRequestDto.getElevators())
                .roomOptions(postRoomRequestDto.getOptions())
                .roomEtcOptions(postRoomRequestDto.getEtcOptions())
                .build();

        RoomDiscussion discussion = RoomDiscussion.builder()
                .isDiscussion(postRoomRequestDto.getIsDiscussion())
                .moveInStart(postRoomRequestDto.getMoveInStart())
                .moveInEnd(postRoomRequestDto.getMoveInEnd())
                .build();

        RoomAddress address = RoomAddress.builder()
                .doo(addressParts[0])
                .si(addressParts[1])
                .gu(addressParts[2])
                .dong(addressParts[3])
                .latitude(postRoomRequestDto.getLatitude())
                .longitude(postRoomRequestDto.getLongitude())
                .address(postRoomRequestDto.getAddress())
                .addressDetail(postRoomRequestDto.getAddressDetails())
                .build();

        return Room.builder()
                .userId(userId)
                .roomCost(cost)
                .roomStructure(structure)
                .roomOption(option)
                .roomDiscussion(discussion)
                .roomAddress(address)
                .build();
    }
}
