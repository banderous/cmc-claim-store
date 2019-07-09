package uk.gov.hmcts.cmc.claimstore.utils;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import uk.gov.hmcts.cmc.ccd.adapter.config.CCDMapperConfig;
import uk.gov.hmcts.cmc.ccd.adapter.mapper.CaseMapper;
import uk.gov.hmcts.cmc.ccd.domain.CCDCase;
import uk.gov.hmcts.cmc.claimstore.repositories.mapping.JsonMapperFactory;
import uk.gov.hmcts.cmc.domain.models.Claim;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.cmc.claimstore.utils.ResourceLoader.successfulCoreCaseDataStoreSubmitResponse;

@SpringBootTest
@ContextConfiguration(classes = CCDMapperConfig.class)
@RunWith(SpringJUnit4ClassRunner.class)
public class CaseDetailsConverterTest {

    private CaseDetailsConverter caseDetailsConverter;

    @Autowired
    private CaseMapper caseMapper;

    @Before
    public void setup() {
        caseDetailsConverter = new CaseDetailsConverter(caseMapper, JsonMapperFactory.create());
    }

    @Test
    public void convertsCaseDetailsToCCDCase() {
        CaseDetails caseDetails = successfulCoreCaseDataStoreSubmitResponse();
        CCDCase ccdCase = caseDetailsConverter.extractCCDCase(caseDetails);
        assertThat(ccdCase.getId()).isEqualTo(caseDetails.getId());
        assertThat(ccdCase.getState()).isEqualTo(caseDetails.getState());
    }

    @Test
    public void convertsCaseDetailsToClaim() {
        CaseDetails caseDetails = successfulCoreCaseDataStoreSubmitResponse();
        Claim claim = caseDetailsConverter.extractClaim(caseDetails);
        assertThat(claim.getId()).isEqualTo(caseDetails.getId());
        assertThat(claim.getState()).isPresent();
        assertThat(claim.getState().orElseThrow(AssertionError::new).getValue()).isEqualTo(caseDetails.getState());
    }
}
