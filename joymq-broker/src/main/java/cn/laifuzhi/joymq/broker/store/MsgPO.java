package cn.laifuzhi.joymq.broker.store;

import cn.laifuzhi.joymq.common.model.SendMsgReq;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class MsgPO {
    private String topic;
    private long storeTime;
    private long crc32;
    private byte[] body;

    public MsgPO(SendMsgReq sendMsgReq) {

    }

}
