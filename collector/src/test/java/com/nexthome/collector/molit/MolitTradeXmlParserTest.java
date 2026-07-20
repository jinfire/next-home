package com.nexthome.collector.molit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class MolitTradeXmlParserTest {

    private final MolitTradeXmlParser parser = new MolitTradeXmlParser();

    @Test
    void parsesTradeItemsAndNormalizesPrice() {
        String xml = """
                <response><header><resultCode>000</resultCode><resultMsg>OK</resultMsg></header>
                <body><totalCount>1</totalCount><items><item>
                  <aptSeq>11110-100</aptSeq><aptNm>테스트 아파트</aptNm><umdNm>청운동</umdNm>
                  <jibun>10</jibun><dealAmount>125,000</dealAmount><excluUseAr>84.91</excluUseAr>
                  <dealYear>2026</dealYear><dealMonth>1</dealMonth><dealDay>5</dealDay>
                  <floor>12</floor><buildYear>2015</buildYear><cdealDay></cdealDay>
                </item></items></body></response>
                """;

        MolitTradePage page = parser.parse(xml);

        assertThat(page.totalCount()).isEqualTo(1);
        assertThat(page.items()).singleElement().satisfies(item -> {
            assertThat(item.apartmentName()).isEqualTo("테스트 아파트");
            assertThat(item.priceKrw()).isEqualTo(1_250_000_000L);
            assertThat(item.exclusiveAreaSqm()).isEqualByComparingTo("84.91");
            assertThat(item.contractDate().toString()).isEqualTo("2026-01-05");
        });
    }

    @Test
    void rejectsApiErrors() {
        String xml = "<response><header><resultCode>30</resultCode><resultMsg>등록되지 않은 키</resultMsg></header></response>";

        assertThatThrownBy(() -> parser.parse(xml))
                .isInstanceOf(MolitApiException.class)
                .hasMessageContaining("등록되지 않은 키");
    }
}
