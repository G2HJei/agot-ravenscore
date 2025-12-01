package xyz.zlatanov.ravenscore.controller;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import java.util.Random;

import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Profile("local")
public class SnrStubRestController {

	private final Random random = new Random();

	@GetMapping(value = "/api/public/game/{gameId}", produces = APPLICATION_JSON_VALUE)
	public String getGame(@PathVariable String gameId) {
		return RESPONSES[0];
		// return RESPONSES[random.nextInt(0, RESPONSES.length)];
	}

	// language=json
	private static final String[] RESPONSES = { """
			{
				"id": "b590456c-a7a2-4fef-bcfc-8efb4b8e5f40",
				"name": "🏆 Crows and Dragons Tourney 2025-26, 1st Round, ADWD G6",
				"view_of_game": {
					"maxPlayerCount": 6,
					"settings": {
						"setupId": "a-dance-with-dragons",
						"playerCount": 6,
						"pbem": true,
						"randomHouses": true,
						"adwdHouseCards": true,
						"ironBank": true
					},
					"waitingFor": "",
					"winner": "Valentiss (Tyrell)",
					"victoryTrack": [
						{
							"house": "Tyrell",
							"player": "Valentiss",
							"victoryPoints": 4,
							"totalLandAreas": 9,
							"supplyLevel": 4,
							"ironThronePosition": 5
						},
						{
							"house": "Lannister",
							"player": "Darwag10",
							"victoryPoints": 4,
							"totalLandAreas": 7,
							"supplyLevel": 6,
							"ironThronePosition": 6
						},
						{
							"house": "Baratheon",
							"player": "TFkGuiscard",
							"victoryPoints": 4,
							"totalLandAreas": 6,
							"supplyLevel": 4,
							"ironThronePosition": 4
						},
						{
							"house": "Bolton",
							"player": "branthreeeyedraven",
							"victoryPoints": 3,
							"totalLandAreas": 9,
							"supplyLevel": 5,
							"ironThronePosition": 3
						},
						{
							"house": "Martell",
							"player": "Bogglewonks",
							"victoryPoints": 2,
							"totalLandAreas": 4,
							"supplyLevel": 4,
							"ironThronePosition": 1
						},
						{
							"house": "Greyjoy",
							"player": "ValP1109",
							"victoryPoints": 2,
							"totalLandAreas": 3,
							"supplyLevel": 3,
							"ironThronePosition": 2
						}
					],
					"round": 6
				},
				"state": "FINISHED"
			}""",
			"""
					           {
					    "id": "a29c9b14-19a9-498d-8752-37ea9004676a",
					    "name": "🏆 Crows and Dragons Tourney 2025-26, 1st Round, MoD G2",
					    "view_of_game": {
					        "maxPlayerCount": 8,
					        "settings": {
					            "setupId": "mother-of-dragons",
					            "playerCount": 8,
					            "pbem": true,
					            "randomHouses": true,
					            "vassals": true,
					            "seaOrderTokens": true,
					            "allowGiftingPowerTokens": true,
					            "ironBank": true,
					            "faceless": true
					        },
					        "waitingFor": "Stark",
					        "victoryTrack": [
					            {
					                "house": "Baratheon",
					                "player": "The starved man",
					                "victoryPoints": 5,
					                "totalLandAreas": 7,
					                "supplyLevel": 2,
					                "ironThronePosition": 1
					            },
					            {
					                "house": "Martell",
					                "player": "Jaqen H'ghar",
					                "victoryPoints": 4,
					                "totalLandAreas": 10,
					                "supplyLevel": 3,
					                "ironThronePosition": 3
					            },
					            {
					                "house": "Tyrell",
					                "player": "Plague face",
					                "victoryPoints": 4,
					                "totalLandAreas": 7,
					                "supplyLevel": 2,
					                "ironThronePosition": 5
					            },
					            {
					                "house": "Greyjoy",
					                "player": "The fat fellow",
					                "victoryPoints": 4,
					                "totalLandAreas": 6,
					                "supplyLevel": 3,
					                "ironThronePosition": 2
					            },
					            {
					                "house": "Stark",
					                "player": "The handsome man",
					                "victoryPoints": 3,
					                "totalLandAreas": 6,
					                "supplyLevel": 1,
					                "ironThronePosition": 6
					            },
					            {
					                "house": "Targaryen",
					                "player": "The kindly man",
					                "victoryPoints": 3,
					                "totalLandAreas": 1,
					                "supplyLevel": 4,
					                "ironThronePosition": 8
					            },
					            {
					                "house": "Arryn",
					                "player": "The squinter",
					                "victoryPoints": 2,
					                "totalLandAreas": 4,
					                "supplyLevel": 2,
					                "ironThronePosition": 7
					            },
					            {
					                "house": "Lannister",
					                "player": "The stern face",
					                "victoryPoints": 1,
					                "totalLandAreas": 2,
					                "supplyLevel": 3,
					                "ironThronePosition": 4
					            }
					        ],
					        "round": 5
					    },
					    "state": "ONGOING"
					}""",
			"""
					{
					     "id": "f2125ff9-1632-4e0a-9a75-6c6b65b127b4",
					     "name": "This fire is out of control",
					     "view_of_game": {
					         "maxPlayerCount": 6,
					         "settings": {
					             "setupId": "base-game",
					             "playerCount": 6,
					             "pbem": true,
					             "private": true,
					             "randomHouses": true
					         },
					         "waitingFor": "",
					         "winner": "liza21111998 (Lannister)",
					         "victoryTrack": [
					             {
					                 "house": "Lannister",
					                 "player": "liza21111998",
					                 "victoryPoints": 5,
					                 "totalLandAreas": 10,
					                 "supplyLevel": 6,
					                 "ironThronePosition": 2
					             },
					             {
					                 "house": "Stark",
					                 "player": "trinidon6",
					                 "victoryPoints": 4,
					                 "totalLandAreas": 8,
					                 "supplyLevel": 3,
					                 "ironThronePosition": 6
					             },
					             {
					                 "house": "Martell",
					                 "player": "paroman3",
					                 "victoryPoints": 4,
					                 "totalLandAreas": 7,
					                 "supplyLevel": 4,
					                 "ironThronePosition": 4
					             },
					             {
					                 "house": "Baratheon",
					                 "player": "Arklane",
					                 "victoryPoints": 3,
					                 "totalLandAreas": 6,
					                 "supplyLevel": 4,
					                 "ironThronePosition": 5
					             },
					             {
					                 "house": "Greyjoy",
					                 "player": "sininyoursoul",
					                 "victoryPoints": 3,
					                 "totalLandAreas": 4,
					                 "supplyLevel": 3,
					                 "ironThronePosition": 3
					             },
					             {
					                 "house": "Tyrell",
					                 "player": "grebennikov.artur9",
					                 "victoryPoints": 1,
					                 "totalLandAreas": 3,
					                 "supplyLevel": 3,
					                 "ironThronePosition": 1
					             }
					         ],
					         "round": 10
					     },
					     "state": "FINISHED"
					 }""" };
}
