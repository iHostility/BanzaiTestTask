package com.ihostility.banzai.server.entity;

import com.ihostility.banzai.server.enums.Action;
import io.netty.channel.Channel;
import lombok.Data;

@Data
public class Player {
    private Channel channel;
    private String nickname;
    private Action action;
}
