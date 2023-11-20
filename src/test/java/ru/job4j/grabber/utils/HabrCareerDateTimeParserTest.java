package ru.job4j.grabber.utils;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class HabrCareerDateTimeParserTest {

    @Test
    void whenStringContainsCorrectTemplateOfDateTimeThenExpectedEqualsToResult() {
        HabrCareerDateTimeParser parser = new HabrCareerDateTimeParser();
        String dateTime = "2023-11-17T18:15:42+03:00";
        LocalDateTime expected = LocalDateTime.parse("2023-11-17T18:15:42");
        assertThat(parser.parse(dateTime)).isEqualTo(expected);
    }

    @Test
    void whenStringContainsIncorrectTemplateOfDateTimeThenExpectedIsNotEqualsToResult() {
        HabrCareerDateTimeParser parser = new HabrCareerDateTimeParser();
        String dateTime = "2023-11-17T18:15:42.630+03:00";
        LocalDateTime expected = LocalDateTime.parse("2023-11-17T18:15:42");
        assertThat(parser.parse(dateTime)).isNotEqualTo(expected);
    }

}