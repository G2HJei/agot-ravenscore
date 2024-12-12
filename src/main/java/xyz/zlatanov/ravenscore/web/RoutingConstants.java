package xyz.zlatanov.ravenscore.web;

import java.util.UUID;

public class RoutingConstants {

	public static final String	ROOT				= "/";
	public static final String	NEW_TOURNAMENT		= ROOT + "new-tournament";
	public static final String	TOURNAMENT_DETAILS	= ROOT + "tournament/{tournamentId}";
	public static final String	UPSERT_STAGE		= TOURNAMENT_DETAILS + "/stage";
	public static final String	REMOVE_STAGE		= TOURNAMENT_DETAILS + "/remove/{stageId}";
	public static final String	UPSERT_PLAYER		= TOURNAMENT_DETAILS + "/player";
	public static final String	REMOVE_PLAYER		= UPSERT_PLAYER + "/remove/{stageId}/{playerId}";
	public static final String	UPSERT_GAME			= TOURNAMENT_DETAILS + "/stage/{stageId}/game";
	public static final String	REMOVE_GAME			= UPSERT_GAME + "/remove/{gameId}";

	public static String redirectToTournament(UUID tournamentId) {
		return "redirect+" + TOURNAMENT_DETAILS.replace("{tournamentId}", tournamentId.toString());
	}
}
