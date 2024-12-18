package fr.flolec.alpacabot.alpacaapi.clockinfo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ClockInfoModel {

    @JsonProperty("is_open")
    boolean isOpen;

    @JsonProperty("next_open")
    Date nextOpen;

    @JsonProperty("next_close")
    Date nextClose;

}
