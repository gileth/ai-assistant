package com.ganlansi.chat.bean.airesp;

import lombok.Data;

@Data
public class Usage {

    private int prompt_tokens;
    private int completion_tokens;
    private int total_tokens;
}
