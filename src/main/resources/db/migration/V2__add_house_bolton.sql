UPDATE player
SET house = 'BOLTON'
WHERE house = 'STARK'
  AND 'A_DANCE_WITH_DRAGONS' = (SELECT type
                                FROM game
                                WHERE game.id = player.game_id)