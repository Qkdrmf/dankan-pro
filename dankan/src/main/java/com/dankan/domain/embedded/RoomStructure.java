package com.dankan.domain.embedded;

import lombok.*;
import org.springframework.stereotype.Service;

import javax.persistence.Column;
import javax.persistence.Embeddable;

@Getter
@Setter
@Embeddable
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoomStructure {

    @Column(name = "room_type",nullable = false,columnDefinition = "int")
    private Long roomType;

    @Column(name = "total_floor",nullable = false,columnDefinition = "int")
    private Long totalFloor;

    @Column(name = "floor",nullable = false,columnDefinition = "int")
    private Long floor;

    @Column(name = "structure",nullable = false,length = 24,columnDefinition = "varchar")
    private String structure;

    @Column(name = "room_size",nullable = false,columnDefinition = "int")
    private Double roomSize;

    @Column(name = "real_room_size",nullable = false,columnDefinition = "double")
    private Double realRoomSize;
}
