package com.ganlansi.chat.bean.airesp;

import lombok.Data;

@Data
public class Choice {
    private int index;
    private Message message;
    private String finish_reason;

}
