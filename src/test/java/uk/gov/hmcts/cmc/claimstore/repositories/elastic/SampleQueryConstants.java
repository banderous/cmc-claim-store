package uk.gov.hmcts.cmc.claimstore.repositories.elastic;

public interface SampleQueryConstants {
    String mediationQuery = "{"
        + "\"size\": 1000,"
        + "\"query\": {\n"
        + "  \"bool\" : {\n"
        + "    \"must\" : [\n"
        + "      {\n"
        + "        \"term\" : {\n"
        + "          \"data.respondents.value.responseFreeMediationOption\" : {\n"
        + "            \"value\" : \"YES\",\n"
        + "            \"boost\" : 1.0\n"
        + "          }\n"
        + "        }\n"
        + "      },\n"
        + "      {\n"
        + "        \"term\" : {\n"
        + "          \"data.respondents.value.claimantResponse.freeMediationOption\" : {\n"
        + "            \"value\" : \"YES\",\n"
        + "            \"boost\" : 1.0\n"
        + "          }\n"
        + "        }\n"
        + "      },\n"
        + "      {\n"
        + "        \"range\" : {\n"
        + "          \"data.respondents.value.claimantResponse.submittedOn\" : {\n"
        + "            \"from\" : \"2019-07-07T00:00:00.000Z\",\n"
        + "            \"to\" : \"2019-07-07T23:59:59.999999999Z\",\n"
        + "            \"include_lower\" : true,\n"
        + "            \"include_upper\" : true,\n"
        + "            \"boost\" : 1.0\n"
        + "          }\n"
        + "        }\n"
        + "      }\n"
        + "    ],\n"
        + "    \"adjust_pure_negative\" : true,\n"
        + "    \"boost\" : 1.0\n"
        + "  }\n"
        + "}}";

    String stayableCaseQuery = "{\n"
        + "  \"bool\" : {\n"
        + "    \"must\" : [\n"
        + "      {\n"
        + "        \"term\" : {\n"
        + "          \"state\" : {\n"
        + "            \"value\" : \"open\",\n"
        + "            \"boost\" : 1.0\n"
        + "          }\n"
        + "        }\n"
        + "      },\n"
        + "      {\n"
        + "        \"range\" : {\n"
        + "          \"data.respondents.value.responseSubmittedOn\" : {\n"
        + "            \"from\" : null,\n"
        + "            \"to\" : \"2019-07-07\",\n"
        + "            \"include_lower\" : true,\n"
        + "            \"include_upper\" : true,\n"
        + "            \"boost\" : 1.0\n"
        + "          }\n"
        + "        }\n"
        + "      },\n"
        + "      {\n"
        + "        \"range\" : {\n"
        + "          \"data.submittedOn\" : {\n"
        + "            \"from\" : \"2019-09-09T15:12:00.000Z\",\n"
        + "            \"to\" : null,\n"
        + "            \"include_lower\" : true,\n"
        + "            \"include_upper\" : true,\n"
        + "            \"boost\" : 1.0\n"
        + "          }\n"
        + "        }\n"
        + "      }\n"
        + "    ],\n"
        + "    \"must_not\" : [\n"
        + "      {\n"
        + "        \"exists\" : {\n"
        + "          \"field\" : \"data.respondents.value.paidInFullDate\",\n"
        + "          \"boost\" : 1.0\n"
        + "        }\n"
        + "      },\n"
        + "      {\n"
        + "        \"exists\" : {\n"
        + "          \"field\" : \"data.respondents.value.claimantResponse.submittedOn\",\n"
        + "          \"boost\" : 1.0\n"
        + "        }\n"
        + "      }\n"
        + "    ],\n"
        + "    \"adjust_pure_negative\" : true,\n"
        + "    \"boost\" : 1.0\n"
        + "  }\n"
        + "}";

    String waitingTransferQuery = "{\n"
        + "  \"bool\" : {\n"
        + "    \"must\" : [\n"
        + "      {\n"
        + "        \"term\" : {\n"
        + "          \"state\" : {\n"
        + "            \"value\" : \"orderdrawn\",\n"
        + "            \"boost\" : 1.0\n"
        + "          }\n"
        + "        }\n"
        + "      },\n"
        + "      {\n"
        + "        \"range\" : {\n"
        + "          \"data.directionOrder.createdOn\" : {\n"
        + "            \"from\" : null,\n"
        + "            \"to\" : \"2019-07-07\",\n"
        + "            \"include_lower\" : true,\n"
        + "            \"include_upper\" : true,\n"
        + "            \"boost\" : 1.0\n"
        + "          }\n"
        + "        }\n"
        + "      }\n"
        + "    ],\n"
        + "    \"adjust_pure_negative\" : true,\n"
        + "    \"boost\" : 1.0\n"
        + "  }\n"
        + "}";

    String defaultCCJCases10DaysBefore = "{\"size\": 1000,\"query\": {\n"
        + "  \"bool\" : {\n"
        + "    \"must\" : [\n"
        + "      {\n"
        + "        \"match\" : {\n"
        + "          \"data.respondents.value.countyCourtJudgmentRequest.type\" : {\n"
        + "            \"query\" : \"DEFAULT\",\n"
        + "            \"operator\" : \"OR\",\n"
        + "            \"prefix_length\" : 0,\n"
        + "            \"max_expansions\" : 50,\n"
        + "            \"fuzzy_transpositions\" : true,\n"
        + "            \"lenient\" : false,\n"
        + "            \"zero_terms_query\" : \"NONE\",\n"
        + "            \"auto_generate_synonyms_phrase_query\" : true,\n"
        + "            \"boost\" : 1.0\n"
        + "          }\n"
        + "        }\n"
        + "      },\n"
        + "      {\n"
        + "        \"range\" : {\n"
        + "          \"data.respondents.value.countyCourtJudgmentRequest.requestedDate\" : {\n"
        + "            \"from\" : \"2020-01-10T00:00:00.000Z\",\n"
        + "            \"to\" : \"2020-01-10T23:59:59.999999999Z\",\n"
        + "            \"include_lower\" : true,\n"
        + "            \"include_upper\" : true,\n"
        + "            \"boost\" : 1.0\n"
        + "          }\n"
        + "        }\n"
        + "      }\n"
        + "    ],\n"
        + "    \"adjust_pure_negative\" : true,\n"
        + "    \"boost\" : 1.0\n"
        + "  }\n"
        + "}}";

    String readyForTransfer = "{\"size\": 1000,\"query\": {\n" +
        "  \"bool\" : {\n" +
        "    \"must\" : [\n" +
        "      {\n" +
        "        \"term\" : {\n" +
        "          \"state\" : {\n" +
        "            \"value\" : \"readyfortransfer\",\n" +
        "            \"boost\" : 1.0\n" +
        "          }\n" +
        "        }\n" +
        "      },\n" +
        "      {\n" +
        "        \"exists\" : {\n" +
        "          \"field\" : \"data.hearingCourt\",\n" +
        "          \"boost\" : 1.0\n" +
        "        }\n" +
        "      },\n" +
        "      {\n" +
        "        \"exists\" : {\n" +
        "          \"field\" : \"data.hearingCourtName\",\n" +
        "          \"boost\" : 1.0\n" +
        "        }\n" +
        "      }\n" +
        "    ],\n" +
        "    \"adjust_pure_negative\" : true,\n" +
        "    \"boost\" : 1.0\n" +
        "  }\n" +
        "}}";
}
