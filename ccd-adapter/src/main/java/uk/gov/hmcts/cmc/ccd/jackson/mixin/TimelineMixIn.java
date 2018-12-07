package uk.gov.hmcts.cmc.ccd.jackson.mixin;

import uk.gov.hmcts.cmc.domain.models.TimelineEvent;

import java.util.List;

@SuppressWarnings("squid:S1610")
public abstract class TimelineMixIn {

    List<TimelineEvent> events;
}