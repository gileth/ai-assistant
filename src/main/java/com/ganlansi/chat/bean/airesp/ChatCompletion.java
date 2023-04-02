package com.ganlansi.chat.bean.airesp;

import lombok.Data;

import java.util.List;

@Data
public class ChatCompletion {
    private String id;
    private String object;
    private long created;
    private List<Choice> choices;
    private Usage usage;

}
