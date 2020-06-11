package uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.caseworker.PaperResponseTests;

import com.google.common.collect.ImmutableList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.cmc.ccd.domain.CCDApplicant;
import uk.gov.hmcts.cmc.ccd.domain.CCDCase;
import uk.gov.hmcts.cmc.ccd.domain.CCDClaimDocument;
import uk.gov.hmcts.cmc.ccd.domain.CCDClaimDocumentType;
import uk.gov.hmcts.cmc.ccd.domain.CCDCollectionElement;
import uk.gov.hmcts.cmc.ccd.domain.CCDDocument;
import uk.gov.hmcts.cmc.ccd.domain.defendant.CCDRespondent;
import uk.gov.hmcts.cmc.ccd.sample.data.SampleData;
import uk.gov.hmcts.cmc.claimstore.events.EventProducer;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.PrintableDocumentService;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.caseworker.paperdefence.DocumentPublishService;
import uk.gov.hmcts.cmc.claimstore.services.ccd.callbacks.caseworker.paperdefence.PaperResponseLetterService;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.cmc.domain.models.sampledata.SampleClaimData;
import uk.gov.hmcts.reform.sendletter.api.Document;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
public class DocumentPublishServiceTest {
    private static final LocalDateTime DATE = LocalDateTime.parse("2020-11-16T13:15:30");
    private static final String DOC_URL = "http://success.test";
    private static final String DOC_URL_BINARY = "http://success.test/binary";
    private static final String DOC_NAME = "doc-name";
    private static Map<String, Object> VALUES = Collections.emptyMap();
    private static final Document COVER_DOCUMENT = new Document(DOC_URL, VALUES);
    private static final Document OCON_DOCUMENT = new Document(DOC_URL, VALUES);
    private static final CCDDocument COVER_LETTER = CCDDocument
            .builder()
            .documentUrl(DOC_URL)
            .documentBinaryUrl(DOC_URL_BINARY)
            .documentFileName(DOC_NAME)
            .build();
    private static final CCDDocument OCON_FORM = CCDDocument
            .builder()
            .documentUrl(DOC_URL)
            .documentBinaryUrl(DOC_URL_BINARY)
            .documentFileName(DOC_NAME)
            .build();
    private static final CCDCollectionElement<CCDClaimDocument> CLAIM_DOCUMENT =
            CCDCollectionElement.<CCDClaimDocument>builder()
                    .value(CCDClaimDocument.builder()
                            .documentLink(COVER_LETTER)
                            .createdDatetime(DATE)
                            .documentType(CCDClaimDocumentType.GENERAL_LETTER)
                            .build())
                    .build();
    private static final String AUTHORISATION = "auth";

    @Mock
    private PaperResponseLetterService paperResponseLetterService;
    @Mock
    private PrintableDocumentService printableDocumentService;
    @Mock
    private EventProducer eventProducer;

    private DocumentPublishService documentPublishService;

    @BeforeEach
    void setUp() {
        documentPublishService = new DocumentPublishService(
                paperResponseLetterService,
                printableDocumentService,
                eventProducer
        );
    }

    @Test
    void shouldPublishDocuments() {
        CCDCase ccdCase = CCDCase.builder()
                .previousServiceCaseReference("000MC001")
                .respondents(ImmutableList.of(
                        CCDCollectionElement.<CCDRespondent>builder()
                                .value(SampleData.getIndividualRespondentWithDQInClaimantResponse())
                                .build()
                ))
                .applicants(List.of(
                        CCDCollectionElement.<CCDApplicant>builder()
                                .value(SampleData.getCCDApplicantIndividual())
                                .build()
                ))
                .build();

        Claim claim = Claim.builder()
                .claimData(SampleClaimData.builder().build())
                .defendantEmail("email@email.com")
                .defendantId("id")
                .submitterEmail("email@email.com")
                .referenceNumber("ref. number")
                .build();

        CCDCase updatedCCDCase = documentPublishService.publishDocuments(ccdCase,
                claim, AUTHORISATION, DATE.toLocalDate());
        when(paperResponseLetterService.createCoverLetter(ccdCase, AUTHORISATION, DATE.toLocalDate()))
                .thenReturn(COVER_LETTER);
        when(paperResponseLetterService.createOconForm(ccdCase, claim, AUTHORISATION, DATE.toLocalDate()))
                .thenReturn(OCON_FORM);
        when(printableDocumentService.process(COVER_LETTER, AUTHORISATION)).thenReturn(COVER_DOCUMENT);
        when(printableDocumentService.process(OCON_FORM, AUTHORISATION)).thenReturn(OCON_DOCUMENT);

        verify(paperResponseLetterService).createCoverLetter(eq(ccdCase), eq(AUTHORISATION), eq(DATE.toLocalDate()));
        verify(printableDocumentService).process(COVER_LETTER, AUTHORISATION);
        verify(printableDocumentService).process(OCON_FORM, AUTHORISATION);
        verify(eventProducer).createPaperDefenceEvent(claim, any(Document.class), any(Document.class));
        assertThat(updatedCCDCase.getCaseDocuments()).contains(CLAIM_DOCUMENT);
    }
}
