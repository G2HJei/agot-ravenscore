package xyz.zlatanov.ravenscore.controller;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.net.URLEncoder;
import java.util.UUID;

public class ControllerConstants {

	// Cookies
	public static final String	ADMIN_COOKIE_NAME			= "tournament-key-hash";
	public static final String	TOURNAMENT_ID_COOKIE_NAME	= "tournament-id";

	// Path variables
	public static final String	TOURNAMENT_ID				= "tournamentId";
	public static final String	STAGE_ID					= "stageId";
	public static final String	GAME_ID						= "gameId";
	public static final String	PLAYER_ID					= "playerId";
	public static final String	PARTICIPANT_ID				= "participantId";
	public static final String	SUBSTITUTE_ID				= "substituteId";
	public static final String	ROUND						= "round";

	// MVC
	public static final String	ROOT						= "/";
	public static final String	TOURNAMENT					= ROOT + "tournament";
	public static final String	TOURNAMENT_DETAILS			= ROOT + "tournament/{" + TOURNAMENT_ID + "}";
	public static final String	DELETE_TOURNAMENT			= TOURNAMENT_DETAILS + "/delete";
	public static final String	DOWNLOAD_BACKUP				= TOURNAMENT_DETAILS + "/download-backup";
	public static final String	RESTORE_BACKUP				= TOURNAMENT_DETAILS + "/restore-backup";
	public static final String	UPSERT_STAGE				= TOURNAMENT_DETAILS + "/stage";
	public static final String	REMOVE_STAGE				= UPSERT_STAGE + "/remove/{" + STAGE_ID + "}";
	public static final String	FINALIZE_STAGE_RANKINGS		= UPSERT_STAGE + "/{" + STAGE_ID + "}/rankings";
	public static final String	IMPORT_PARTICIPANTS			= TOURNAMENT_DETAILS + "/stage/{" + STAGE_ID + "}/import-participants";
	public static final String	SUBSTITUTE_PLAYER			= //
			TOURNAMENT_DETAILS + "/participant/{" + PARTICIPANT_ID + "}/substitute/{" + SUBSTITUTE_ID + "}";
	public static final String	UPSERT_PLAYER				= TOURNAMENT_DETAILS + "/player";
	public static final String	REMOVE_PLAYER				= UPSERT_PLAYER + "/remove/{" + STAGE_ID + "}/{" + PLAYER_ID + "}";
	public static final String	UPSERT_GAME					= TOURNAMENT_DETAILS + "/stage/{" + STAGE_ID + "}/game";
	public static final String	REMOVE_GAME					= UPSERT_GAME + "/remove/{" + GAME_ID + "}";

	// REST
	public static final String	UNLOCK_TOURNAMENT			= TOURNAMENT_DETAILS + "/unlock-tournament";
	public static final String	UPDATE_ROUND				= UPSERT_GAME + "/{" + GAME_ID + "}/round/{" + ROUND + "}";
	public static final String	UPDATE_GAME_RANKINGS		= UPSERT_GAME + "/{" + GAME_ID + "}/rankings";
	public static final String	REFRESH_SNR_GAME			= TOURNAMENT_DETAILS + "/stage/{" + STAGE_ID + "}/refresh-snr-game/{" + GAME_ID
			+ "}";

	public static String redirectToTournamentWithMessage(UUID tournamentId, String message) {
		return redirectToTournament(tournamentId) + "?message=" + URLEncoder.encode(message, UTF_8);
	}

	public static String redirectToTournament(UUID tournamentId, String errorMessage) {
		return redirectToTournament(tournamentId) + "?error=" + URLEncoder.encode(errorMessage, UTF_8);
	}

	public static String redirectToTournament(UUID tournamentId) {
		return "redirect:" + TOURNAMENT_DETAILS.replace("{" + TOURNAMENT_ID + "}", tournamentId.toString());
	}
}
