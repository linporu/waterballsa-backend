package waterballsa.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import waterballsa.dto.JourneyDetailResponse;
import waterballsa.dto.JourneyListResponse;
import waterballsa.service.JourneyService;

@RestController
@RequestMapping("/journeys")
public class JourneyController {

  private static final Logger logger = LoggerFactory.getLogger(JourneyController.class);

  private final JourneyService journeyService;

  public JourneyController(JourneyService journeyService) {
    this.journeyService = journeyService;
  }

  /**
   * Get all journeys.
   *
   * @return Journey list response
   */
  @GetMapping
  public ResponseEntity<JourneyListResponse> getJourneys() {
    logger.debug("GET /journeys request received");

    JourneyListResponse response = journeyService.getJourneys();

    logger.info("Successfully returned journey list with {} journeys", response.journeys().size());

    return ResponseEntity.ok(response);
  }

  /**
   * Get journey details with chapters and missions.
   *
   * @param journeyId Journey ID
   * @return Journey details response
   */
  @GetMapping("/{journeyId}")
  public ResponseEntity<JourneyDetailResponse> getJourneyDetail(@PathVariable Long journeyId) {
    logger.debug("GET /journeys/{} request received", journeyId);

    JourneyDetailResponse response = journeyService.getJourneyDetail(journeyId);

    logger.info("Successfully returned journey details for journeyId: {}", journeyId);

    return ResponseEntity.ok(response);
  }
}
