package main

import (
	"fmt"
	"math/rand/v2"
)

type PlayerInfo struct {
	id int
}

type MatchResult struct {
	matchIdx int
	winner   PlayerInfo
}

func player(id int, moveChan chan<- int) {
	move := rand.IntN(2)
	fmt.Printf("[Player %d] has picked: %d\n", id, move)
	moveChan <- move
}

func coord(nPlayers int) {
	players := make([]PlayerInfo, nPlayers)
	for i := 0; i < nPlayers; i++ {
		players[i] = PlayerInfo{id: i + 1}
	}

	roundNum := 1

	for len(players) > 1 {
		fmt.Printf("\n--- Start Round %d (Players left %d) ---\n", roundNum, len(players))

		nMatches := len(players) / 2
		nextRoundPlayers := make([]PlayerInfo, nMatches)

		// Buffered channel to receive match messages
		resultChan := make(chan MatchResult, nMatches)

		// launch matches in parallel
		for i := 0; i < len(players); i += 2 {
			matchIdx := i / 2
			p1 := players[i]
			p2 := players[i+1]

			go playMatch(matchIdx, p1, p2, roundNum, resultChan)
		}

		// wait messages of end match reading from channel nMatches times
		for j := 0; j < nMatches; j++ {
			res := <-resultChan
			nextRoundPlayers[res.matchIdx] = res.winner
		}

		players = nextRoundPlayers
		roundNum++
	}

	fmt.Printf("\n !!! THE WINNER OF THE CHAMPIONSHIP IS %d! 🏆\n", players[0].id)
}

func playMatch(matchIdx int, p1, p2 PlayerInfo, roundNum int, resultChan chan MatchResult) {
	matchName := fmt.Sprintf("Round %d, Match %d", roundNum, matchIdx+1)
	moveChan1 := make(chan int, 1)
	moveChan2 := make(chan int, 1)

	var winner PlayerInfo
	var loser PlayerInfo

	for {
		coord_move := rand.IntN(2)

		go player(p1.id, moveChan1)
		go player(p2.id, moveChan2)

		m1 := <-moveChan1
		m2 := <-moveChan2

		if m1 == m2 {
			fmt.Printf("[%s] Players picked same move %d vs %d. Replay...\n", matchName, m1, m2)
			continue
		}

		if m1 == coord_move {
			winner, loser = p1, p2
		} else {
			winner, loser = p2, p1
		}

		fmt.Printf("[%s] Coord has picked %d. Player %d (move %d) beats Player %d (move %d)\n",
			matchName, coord_move, winner.id, coord_move, loser.id, func() int {
				if winner.id == p1.id {
					return m2
				}
				return m1
			}())
		break
	}

	// send completion message
	resultChan <- MatchResult{matchIdx: matchIdx, winner: winner}
}

func main() {
	nPlayers := 8
	fmt.Printf("Start heads & tails: %d players (Centralized Solution)\n", nPlayers)

	coord(nPlayers)

	for {
	}

}
