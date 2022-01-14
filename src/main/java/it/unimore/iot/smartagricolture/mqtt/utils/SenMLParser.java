package it.unimore.iot.smartagricolture.mqtt.utils;

import com.google.gson.Gson;

import java.util.Optional;

/**
 * @author Alberto Moretti, 272804@studenti.unimore.it
 * @version 1.0.0
 * @project smart-agriculture
 * @created 11/01/2022 - 7:47
 */
public class SenMLParser {

    /**
     * Function to parse a senMLPack instance to json, creating a senml+json media type
     *
     * @param senMLPack the senml package to parse
     * @return Optional<String> Optional, it will be an empty optional in case of error
     */
    public static Optional<String> toSenMLJson(SenMLPack senMLPack) {
        try {
            return Optional.of(new Gson().toJson(senMLPack));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    /**
     * Function to parse a json to senMLPack instance, parsing a senml+json media type
     *
     * @param json the json to parse
     * @return Optional<SenMLPack> Optional, it will be an empty optional in case of error
     */
    public static Optional<SenMLPack> parseSenMLJson(String json) {
        try {
            return Optional.of(new Gson().fromJson(json, SenMLPack.class));
        } catch (Exception e) {
            return Optional.empty();
        }
    }
}
