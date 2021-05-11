package com.mongdok.websocket.repository;

import com.mongdok.websocket.model.Seat;
import com.mongdok.websocket.model.SeatInfo;
import com.mongdok.websocket.model.Timestamp;
import com.mongdok.websocket.model.enums.StudyType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@Repository
@Slf4j
public class SeatRepository {

    private final RoomRepository roomRepository;

    // Redis
    private static final String SEAT_INFO = "SEAT_INFO"; // 열람실의 좌석정보 저장
    private static final String SEAT_NO_INFO = "SEAT_NO_INFO"; // 열람실의 좌석번호 저장
    private static final String DELIMITER= "_"; // 구분자

    @Resource(name = "redisTemplate")
    private HashOperations<String, String, Seat> hashOpsSeatInfo;
    @Resource(name = "redisTemplate")
    private HashOperations<String, Integer, String> hashOpsSeatNoInfo;

    // 해당 열람실의 좌석정보 리스트 조회
    public List<Seat> findAllSeatInfo(String roomId) {
        return hashOpsSeatInfo.values(SEAT_INFO+DELIMITER+roomId);
    }

    // 특정 좌석정보 조회
    public Seat findSeatByUserId(String roomId, String userId) {
        Seat seat =  hashOpsSeatInfo.get(SEAT_INFO+DELIMITER+roomId, userId);
        if(seat != null) {
            log.info("[SEAT 조회] : {}", seat.toString());
        }
        return seat;
    }

    // 좌석정보 저장
    public boolean setSeatInfo(String roomId, String userId, String userName, SeatInfo seatInfo) {
        // 1. 좌석이 꽉찬 경우
        if(hashOpsSeatInfo.size(SEAT_INFO+DELIMITER+roomId) >= roomRepository.getRoomById(roomId).getLimitUserCount()) {
            return false;
        }

        // 2. 해당 좌석번호를 타 사용자가 사용하고 있는지 확인
        if(!isAllocate(roomId, seatInfo.getSeatNo(), userId)) {
            return false;
        }

        List<Timestamp> timestampList = new ArrayList<>();
        LocalDateTime nowTime = LocalDateTime.now(ZoneOffset.UTC);
        timestampList.add(new Timestamp(nowTime, StudyType.PAUSE));

        Seat newSeat = Seat.builder()
                .seatNo(seatInfo.getSeatNo())
                .userId(userId)
                .userName(userName)
                .studyType(StudyType.PAUSE)
                .timestampList(timestampList)
                .allocateTime(nowTime)
                .build();
        hashOpsSeatInfo.put(SEAT_INFO+DELIMITER+roomId, userId, newSeat);
        return true;
    }

    // 좌석정보 수정
    public void updateSeatInfo(String roomId, String userId, SeatInfo info) {
        log.info("seatInfo : {}", info.toString());

        if(hashOpsSeatInfo.hasKey(SEAT_INFO+DELIMITER+roomId, userId)) {
            Seat seat = findSeatByUserId(roomId, userId);
            if(seat != null) {
                log.info("seat {}", seat.toString());
            } else {
                log.info("seat null");
            }

            List<Timestamp> timestampList = seat.getTimestampList();
            timestampList.add(Timestamp
                    .builder()
                    .time(LocalDateTime.now(ZoneOffset.UTC))
                    .type(info.getStudyType())
                    .build());
            seat.setStudyType(info.getStudyType());
            hashOpsSeatInfo.put(SEAT_INFO+DELIMITER+roomId, userId, seat);
        }
    }

    // 좌석정보 삭제
    public void removeSeatInfo(String roomId, String userId) {
        log.info("=============== seat info 삭제 ===============");
        Seat removeSeat = hashOpsSeatInfo.get(SEAT_INFO+DELIMITER+roomId, userId);
        if(removeSeat != null) {
            int removeSeatNo = removeSeat.getSeatNo();
            log.info("SEAT NO : {}", removeSeatNo);
            hashOpsSeatNoInfo.delete(SEAT_NO_INFO+DELIMITER+roomId, removeSeatNo);
        } else {
            log.info("SEAT NULL");
        }
        hashOpsSeatInfo.delete(SEAT_INFO+DELIMITER+roomId, userId);
    }

    // 좌석번호 저장
    public boolean isAllocate(String roomId, int seatNo, String userId) {
        if(hashOpsSeatNoInfo.hasKey(SEAT_NO_INFO+DELIMITER+roomId, seatNo)) {
            return false;
        }
        hashOpsSeatNoInfo.put(SEAT_NO_INFO+DELIMITER+roomId, seatNo, userId);
        log.info("** SEAT ALLOCATED **");
        return true;
    }
}